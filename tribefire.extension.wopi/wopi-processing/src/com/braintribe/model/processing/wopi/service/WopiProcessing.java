// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.wopi.service;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.servlet.ServletInputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.wopi.WopiQueryingUtil;
import com.braintribe.model.processing.wopi.app.WopiHeader;
import com.braintribe.model.processing.wopi.app.WopiHttpStatusMessage;
import com.braintribe.model.processing.wopi.app.WopiRequest;
import com.braintribe.model.processing.wopi.misc.HttpResponseMessage;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.wopi.WopiAccessToken;
import com.braintribe.model.wopi.WopiLock;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.WopiStatus;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.integration.NotifyUpdateCurrentResource;
import com.braintribe.model.wopi.service.integration.NotifyUpdateCurrentResourceResult;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.stream.FileStreamProviders;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 *
 */
public class WopiProcessing implements AbstractWopiProcessing {

	private static final Logger logger = Logger.getLogger(WopiProcessing.class);

	private PersistenceGmSessionFactory localSessionFactory;

	private LockManager lockManager;

	private StreamPipeFactory streamPipeFactory;

	public WopiSession getWopiResourceRequest(String correlationId, IncrementalAccess access) {
		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		CommonTools.requireNonEmpty(correlationId, "correlationId must not be empty");
		CommonTools.objNotNull(access, "access must not be null");

		WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);

		return wopiSession;
	}

	public HttpResponseMessage putWopiResourceRequest(WopiRequest request, WopiApp wopiApp, String actualAccessToken, UserSession userSession)
			throws IOException {

		boolean successfullySaved = false;

		IncrementalAccess access = wopiApp.getAccess();

		String newLock = request.getWopiLock();

		String correlationId = request.getFileId();
		ServletInputStream inputStream = request.getInputStream();

		Resource resource = Resource.T.create();
		resource.assignTransientSource(FileStreamProviders.from(inputStream));

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		String lockIdentifier = prepareLockIdentifier(correlationId, "update_versions", access);

		Lock lock = createLock(lockManager, lockIdentifier, wopiApp);

		try {
			logger.debug(() -> "Try to get lock [put] for correlationId: '" + correlationId + "'  on access: '" + access.getExternalId() + "'");
			lock.lock();
			logger.info(() -> "Retrieved lock [put] for correlationId: '" + correlationId + "'  on access: '" + access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);
			if (wopiSession == null) {
				String msg = "Could not find WopiSession for correlationId: '" + correlationId + "' while putWopiResource";
				logger.error(() -> msg);
				// No WopiSession found at all - illegal state
				return WopiHttpStatusMessage.returnFileUnknown(msg);
			}

			if (wopiSession.getCurrentResource() != null && wopiSession.getCurrentResource().getFileSize() != null
					&& wopiSession.getCurrentResource().getFileSize() != 0 && newLock == null) {
				return WopiHttpStatusMessage.returnLockMismatch(newLock,
						"Tried to store file on current resource that are not zero bytes without lock");
			}

			WopiStatus status = wopiSession.getStatus();
			wopiSession.setVersion(wopiSession.getVersion() + 1);
			wopiSession.setLastUpdated(new Date());
			wopiSession.setLastUpdatedUser(userSession.getUser().getName());
			wopiSession.setLastUpdatedUserId(userSession.getUser().getId());

			// check if the existing lock of the WopiSession matches to the received lock from Office Online
			WopiLock existingWopiLock = wopiSession.getLock();
			if (existingWopiLock != null) {
				String existingWopiLockString = existingWopiLock.getLock();
				if (!existingWopiLockString.equals(newLock)) {
					return WopiHttpStatusMessage.returnLockMismatch(existingWopiLockString, "Existing lock is not matching new lock");
				}
			}

			if (status == WopiStatus.closed || status == WopiStatus.expired) {
				String httpMessage = "The WopiSession of correlationId: '" + correlationId + "' is already in '" + wopiSession.getStatus()
						+ "' status";

				logger.info(() -> "Could not find 'open' WopiSession with correlationId: '" + correlationId
						+ "' - but has one for 'expired' or 'closed'");
				// the WopiSession is closed/expired - saving should to into post open resources - but only once; there
				// is a retry in the WOPI client which we eliminate here...
				String creator = fetchCreator(actualAccessToken, wopiSession);

				for (Resource r : wopiSession.getPostOpenResourceVersions()) {
					String storedCreator = r.getCreator();
					if (creator.equals(storedCreator)) {
						// this appears because of retries of the WOPI client...
						logger.debug(
								() -> "Already stored resource of WopiSession with correlationId: '" + correlationId + "' - ignore adding another");
						return WopiHttpStatusMessage.returnFileUnknown(httpMessage);
					}
				}

				// first store the resource in post open resources
				Resource currentResource = wopiSession.getCurrentResource();

				//@formatter:off
				Resource uploadResource = 
							uploadResource(
								session,
								currentResource.getName(),
								null, //md5 has changed
								currentResource.getMimeType(),
								currentResource.getTags(),
								correlationId,
								streamPipeFactory,
								resource,
								creator
							);
				//@formatter:on

				List<Resource> postOpenResourceVersions = wopiSession.getPostOpenResourceVersions();
				postOpenResourceVersions.add(uploadResource);

				session.commit();
				return WopiHttpStatusMessage.returnFileUnknown(httpMessage);
			} else if (status == WopiStatus.open) {
				logger.debug(() -> "Trying to save resource of WopiSession for correlationId: '" + correlationId + "'");
				// there exists an open WopiSession - adapt currentVersion and resourceVersions
				String creator = fetchCreator(actualAccessToken, wopiSession);

				List<Resource> resourceVersions = wopiSession.getResourceVersions();

				if (resourceVersions.size() < wopiSession.getMaxVersions()) {
					// add current resource to version, replace current resource with new resource
					Resource currentResource = wopiSession.getCurrentResource();
					resourceVersions.add(currentResource);

					//@formatter:off
					Resource uploadResource = 
								uploadResource(
									session,
									currentResource.getName(),
									null, //md5 has changed
									currentResource.getMimeType(),
									currentResource.getTags(),
									correlationId,
									streamPipeFactory,
									resource,
									creator
								);
					//@formatter:on
					wopiSession.setCurrentResource(uploadResource);
				} else {
					// remove first resource version, add current resource to version, replace current resource with new
					// resource
					Resource latestResourceVersion = resourceVersions.remove(0);
					deleteResource(session, latestResourceVersion, false, logger);
					Resource currentResource = wopiSession.getCurrentResource();
					resourceVersions.add(currentResource);
					//@formatter:off
					Resource uploadResource = 
								uploadResource(
									session,
									currentResource.getName(),
									null, //md5 has changed
									currentResource.getMimeType(),
									currentResource.getTags(),
									correlationId,
									streamPipeFactory,
									resource,
									creator
								);
					//@formatter:on
					wopiSession.setCurrentResource(uploadResource);
				}

				session.commit();
				successfullySaved = true;
				logger.debug(() -> "Successfully saved resource of WopiSession for correlationId: '" + correlationId + "'");

				// notify on successfully put file
				notifyUpdateCurrentResource(wopiSession, userSession, session);

				return WopiHttpStatusMessage.returnSuccess(itemVersionHeaderMap(wopiSession));
			} else {
				throw new IllegalStateException("WopiSession with correlationId: '" + correlationId + "' has illegal status: '" + status + "'");
			}
		} finally {
			lock.unlock();
			String msg = "### " + successfullySaved + " -- " + request + "";
			logger.info(() -> msg);
		}
	}

	private void notifyUpdateCurrentResource(WopiSession wopiSession, UserSession userSession, PersistenceGmSession session) {
		NotifyUpdateCurrentResource request = NotifyUpdateCurrentResource.T.create();
		request.setWopiSession(wopiSession);
		request.setUserName(userSession.getUser().getName());
		NotifyUpdateCurrentResourceResult result = request.eval(session).get();
		Failure failure = result.getFailure();
		if (failure != null) {
			Throwable throwable = FailureCodec.INSTANCE.decode(failure);
			logger.warn(() -> "Could not send notification on update current resource for correlationId: '" + wopiSession.getCorrelationId() + "' - '"
					+ throwable.getMessage() + "'. Stack trace on trace level.");
			logger.trace(throwable);
		}
	}

	public void retrieveAccessTokenRequest(String correlationId, String accessToken, WopiApp wopiApp, String userName) {

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		logger.info(() -> "Retrieved accessToken: '" + accessToken + "' for correlationId: '" + correlationId + "' on access: '"
				+ access.getExternalId() + "' to be stored");

		WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
		List<WopiAccessToken> accessTokens = wopiSession.getAccessTokens();
		boolean containsToken = accessTokens.stream().anyMatch(at -> at.getToken().contains(accessToken));
		if (containsToken) {
			logger.trace(() -> "AccessToken: '" + accessToken + "' already exists - continue...");
		} else {
			// if token is not already set - set it now
			WopiAccessToken _accessToken = session.create(WopiAccessToken.T);
			_accessToken.setToken(accessToken);
			_accessToken.setUser(userName);
			accessTokens.add(_accessToken);

			session.commit();
			logger.debug(() -> "Stored accessToken: '" + accessToken + "' for correlationId: '" + correlationId + "' on access: '"
					+ access.getExternalId() + "' to be stored");
		}
	}

	public WopiSession validateAccessTokenRequest(String accessToken, String correlationId, WopiApp wopiApp) {

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		WopiSession existingWopiSession = WopiQueryingUtil.queryWopiSessionWithAccessToken(session, correlationId, accessToken);

		if (existingWopiSession == null) {
			// calculate extra information
			WopiSession wopiSessionByCorrelationId = WopiQueryingUtil.queryOpenWopiSession(session, correlationId);
			// @formatter:off
			SelectQuery queryByAccessToken = new SelectQueryBuilder()
					.from(WopiSession.T, "ws")
					.where()
						.value(accessToken).in().property(WopiSession.accessTokens)
					.done();
			// @formatter:on
			WopiSession wopiSessionByAccessToken = session.query().select(queryByAccessToken).first();
			logger.error(() -> "Got '" + WopiSession.T.getShortName() + "' by correlationId: '" + wopiSessionByCorrelationId + "' - Got '"
					+ WopiSession.T.getShortName() + "' by accessToken: '" + wopiSessionByAccessToken + "'");
			throw new IllegalStateException("Could not validate accessToken: '" + accessToken + "' for correlationId: '" + correlationId + "'");
		}

		logger.debug(() -> "Successfully validated accessToken: '" + accessToken + "' for correlationId: '" + correlationId
				+ "' - Got resource with name: '" + existingWopiSession.getCurrentResource().getName() + "' and id: '"
				+ existingWopiSession.getCurrentResource().getId() + "'");
		return existingWopiSession;
	}

	public HttpResponseMessage lock(WopiRequest request, WopiApp wopiApp) throws GmSessionException {

		String correlationId = request.getFileId();
		String newLock = request.getWopiLock();

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		Lock lock = createLock(lockManager, correlationId, wopiApp);
		try {
			logger.debug(() -> "Try to get lock [lock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");
			lock.lock();
			logger.info(() -> "Retrieved lock [lock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
			WopiLock existingLock = resolveLock(wopiSession, session, wopiApp);

			if (existingLock != null) {
				// There is a valid existing lock on the file - this is fine

				String existingLockString = existingLock.getLock();
				if (newLock.equals(existingLockString)) {
					return WopiHttpStatusMessage.returnSuccess();
				} else {
					return WopiHttpStatusMessage.returnLockMismatch(existingLockString, "Existing lock is not matching new lock");
				}
			}
			// The file is not currently locked or the lock has already expired.
			applyLock(newLock, wopiSession, session);

			Map<String, String> headerMap = itemVersionHeaderMap(wopiSession);
			return WopiHttpStatusMessage.returnSuccess(headerMap);
		} finally {
			closeLock(lock);
		}
	}

	public HttpResponseMessage refreshLock(WopiRequest request, WopiApp wopiApp) throws GmSessionException {
		String correlationId = request.getFileId();
		String newLock = request.getWopiLock();

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		Lock lock = createLock(lockManager, correlationId, wopiApp);
		try {
			logger.debug(() -> "Try to get lock [refreshLock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");

			lock.lock();

			logger.info(() -> "Retrieved lock [refreshLock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
			WopiLock existingLock = resolveLock(wopiSession, session, wopiApp);

			if (existingLock != null) {
				if (existingLock.getLock().equals(newLock)) {
					// There is a valid lock on the file and the existing lock
					// matches the provided one
					renewLock(existingLock, session);
					return WopiHttpStatusMessage.returnSuccess();
				}
				// The existing lock doesn't match the requested one. Return a
				// lock mismatch error along with the current lock
				return WopiHttpStatusMessage.returnLockMismatch(existingLock.getLock(), "Existing lock mismatch");
			}
			// The requested lock does not exist. That's also a lock mismatch
			// error.
			return WopiHttpStatusMessage.returnLockMismatch(null, "File not locked");
		} finally {
			closeLock(lock);
		}
	}

	public HttpResponseMessage unlock(WopiRequest request, WopiApp wopiApp) throws GmSessionException {
		String correlationId = request.getFileId();
		String newLock = request.getWopiLock();

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		Lock lock = createLock(lockManager, correlationId, wopiApp);
		try {

			logger.debug(() -> "Try to get lock [unlock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");

			lock.lock();

			logger.info(() -> "Retrieved lock [unlock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' on access: '"
					+ access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
			WopiLock existingLock = resolveLock(wopiSession, session, wopiApp);

			if (existingLock != null) {
				if (existingLock.getLock().equals(newLock)) {
					// There is a valid lock on the file and the existing lock
					// matches the provided one
					deleteLock(wopiSession, session);
					return WopiHttpStatusMessage.returnSuccess(itemVersionHeaderMap(wopiSession));
				} else {
					// The existing lock doesn't match the requested one. Return
					// a lock mismatch error along with the current lock
					return WopiHttpStatusMessage.returnLockMismatch(existingLock.getLock(), "Existing lock mismatch");
				}
			}
			// The requested lock does not exist. That's also a lock mismatch
			// error.
			return WopiHttpStatusMessage.returnLockMismatch(null, "File not locked");
		} finally {
			closeLock(lock);
		}
	}

	public HttpResponseMessage unlockAndRelock(WopiRequest request, WopiApp wopiApp) throws GmSessionException {

		String correlationId = request.getFileId();
		String newLock = request.getWopiLock();
		String oldLock = request.getWopiOldLock();

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		Lock lock = createLock(lockManager, correlationId, wopiApp);
		try {

			logger.debug(() -> "Try to get lock [unlockAndRelock] for correlationId: '" + correlationId + "' newLock: '" + newLock + "' oldLock: '"
					+ oldLock + "' on access: '" + access.getExternalId() + "'");

			lock.lock();

			logger.info(() -> "Retrieved lock [unlockAndRelock] for correlationId: '" + correlationId + "' newLock: " + newLock + " oldLock: '"
					+ oldLock + "' on access: '" + access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
			WopiLock existingLock = resolveLock(wopiSession, session, wopiApp);

			if (existingLock != null) {
				if (existingLock.getLock().equals(oldLock)) {
					// There is a valid lock on the file and the existing lock
					// matches the provided one
					deleteLock(wopiSession, session);
					applyLock(newLock, wopiSession, session);
					HttpResponseMessage response = WopiHttpStatusMessage.returnSuccess();
					response.addHeader(WopiHeader.OldLock.key(), newLock);
					return response;
				} else {
					// The existing lock doesn't match the requested one. Return
					// a lock mismatch error
					// along with the current lock
					return WopiHttpStatusMessage.returnLockMismatch(existingLock.getLock(), "Existing lock mismatch");
				}
			}
			// The requested lock does not exist. That's also a lock mismatch
			// error.
			return WopiHttpStatusMessage.returnLockMismatch(null, "File not locked");

		} finally {
			closeLock(lock);
		}
	}

	public HttpResponseMessage getLock(WopiRequest request, WopiApp wopiApp) {
		String correlationId = request.getFileId();

		IncrementalAccess access = wopiApp.getAccess();

		PersistenceGmSession session = localSessionFactory.newSession(access.getExternalId());

		Lock lock = createLock(lockManager, correlationId, wopiApp);
		try {
			logger.debug(() -> "Try to get lock [getLock] for correlationId: '" + correlationId + "' on access: '" + access.getExternalId() + "'");

			lock.lock();

			logger.info(() -> "Retrieved lock [getLock] for correlationId: '" + correlationId + "' on access: '" + access.getExternalId() + "'");

			WopiSession wopiSession = WopiQueryingUtil.queryExistingWopiSession(session, correlationId);
			WopiLock existingLock = resolveLock(wopiSession, session, wopiApp);

			Map<String, String> headerMap = itemVersionHeaderMap(wopiSession);
			if (existingLock != null) {
				// The file is locked - return the lock
				headerMap.put(WopiHeader.Lock.key(), existingLock.getLock());
				return WopiHttpStatusMessage.returnSuccess(headerMap);
			}
			// The file is not locked - return success
			headerMap.put(WopiHeader.Lock.key(), "");
			return WopiHttpStatusMessage.returnSuccess(headerMap);
		} finally {
			closeLock(lock);
		}
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private Map<String, String> itemVersionHeaderMap(WopiSession wopiSession) {
		return asMap(WopiHeader.ItemVersion.key(), Long.toString(wopiSession.getVersion()));
	}

	private WopiLock resolveLock(WopiSession wopiSession, PersistenceGmSession session, WopiApp wopiApp) {
		WopiLock lock = wopiSession.getLock();
		if (lock != null) {
			long expirationTime = resolveWopiSessionExpiration(wopiSession, wopiApp);
			long expires = lock.getCreationDate().getTime() + expirationTime;
			if (expires < System.currentTimeMillis()) {
				deleteLock(wopiSession, session);
				return null;
			}
			return lock;
		}
		return null;
	}

	private void applyLock(String newLock, WopiSession wopiSession, PersistenceGmSession session) {
		WopiLock lock = session.create(WopiLock.T);
		lock.setLock(newLock);
		lock.setCreationDate(new Date());

		wopiSession.setLock(lock);
		session.commit();
	}

	private void renewLock(WopiLock wopiLock, PersistenceGmSession session) {
		wopiLock.setCreationDate(new Date());
		session.commit();
	}

	private void deleteLock(WopiSession wopiSession, PersistenceGmSession session) {
		WopiLock lock = wopiSession.getLock();
		if (lock != null) {
			wopiSession.setLock(null);
			session.deleteEntity(lock);
			session.commit();
		}
	}

	// -----------------------------------------------------------------------
	// STATIC HELPER METHODS
	// -----------------------------------------------------------------------

	private static String prepareLockIdentifier(String correlationId, String postfix, IncrementalAccess access) {
		String externalId = access.getExternalId();
		StringBuilder sb = new StringBuilder();
		sb.append(externalId);
		sb.append("_");
		sb.append(correlationId);
		sb.append("_");
		sb.append(postfix);
		return sb.toString();
	}

	private static String fetchCreator(String actualAccessToken, WopiSession wopiSession) {
		List<WopiAccessToken> accessTokens = wopiSession.getAccessTokens();
		String creator = "unknown";
		for (WopiAccessToken _accessToken : accessTokens) {
			if (_accessToken.getToken().equals(actualAccessToken)) {
				creator = _accessToken.getUser();
				break;
			}
		}
		return creator;
	}

	/**
	 * Resolves the {@link WopiSession} expiration in milliseconds - either configured per {@link WopiSession} or
	 * globally for all {@link WopiSession} - defined in {@link WopiApp}
	 * 
	 * @param wopiSession
	 *            actual {@link WopiSession}
	 * @param wopiApp
	 *            {@link WopiApp}
	 * @return expiration in milliseconds
	 */
	private static long resolveWopiSessionExpiration(WopiSession wopiSession, WopiApp wopiApp) {
		Long lockExpirationInMs = wopiSession.getWopiLockExpirationInMs();
		if (!CommonTools.isNull(lockExpirationInMs)) {
			return lockExpirationInMs;
		}
		return wopiApp.getWopiLockExpirationInMs();
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setLocalSessionFactory(PersistenceGmSessionFactory localSessionFactory) {
		this.localSessionFactory = localSessionFactory;
	}

	@Configurable
	@Required
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
}
