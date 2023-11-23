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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiAccessToken;
import com.braintribe.model.wopi.WopiLock;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * Base for WOPI processing
 * 
 *
 */
public interface AbstractWopiProcessing {

	public static final String DEMO_CORRELATION_ID_PREFIX = "__demoWopiResources";
	public static final String DEMO_CORRELATION_ID_POSTFIX = "forTesting__";
	public static final String DEMO_DOC_TAG = "__demo__";
	public static final String TEST_CORRELATION_ID_PREFIX = "testWopiResources";
	public static final String TEST_DOC_TAG = "__test__";

	/**
	 * Upload an existing {@link Resource} to a {@link PersistenceGmSession}
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param name
	 *            name of the {@link Resource}
	 * @param md5
	 *            MD5 of the {@link Resource}
	 * @param mimeType
	 *            MimeType of the {@link Resource}
	 * @param tags
	 *            tags of the {@link Resource}
	 * @param correlationId
	 * @param streamPipeFactory
	 * @param _resource
	 * @param creator
	 *            creator of the {@link Resource}
	 * @return
	 */
	default public Resource uploadResource(PersistenceGmSession session, String name, String md5, String mimeType, Set<String> tags,
			String correlationId, StreamPipeFactory streamPipeFactory, Resource _resource, String creator) {

		String pipeName = correlationId + "_" + System.nanoTime();
		StreamPipe streamPipe = streamPipeFactory.newPipe(pipeName);
		streamPipe.feedFrom(_resource.openStream());

		Supplier<InputStream> isSupplier = () -> openInputStreamPipe(streamPipe);

		//@formatter:off
		ResourceCreateBuilder resourceBuilder = 
				session
				.resources()
				.create();
		//@formatter:on

		// TODO: name needs to be mandatory!!! - change implementation and test
		if (name != null) {
			resourceBuilder.name(name);
		}
		// TODO: mimeType needs to be mandatory!!! - change implemenation and test
		if (mimeType != null) {
			resourceBuilder.mimeType(mimeType);
		}
		// TODO: md5 needs to be mandatory!!! - change implemenation and test
		if (md5 != null) {
			resourceBuilder.md5(md5);
		}
		if (tags != null && !tags.isEmpty()) {
			resourceBuilder.tags(tags);
		}

		Resource resource = resourceBuilder.store(isSupplier::get);

		if (!CommonTools.isEmpty(creator)) {
			// if creator is not set manually then the session access is the creator
			resource.setCreator(creator);
		}

		return resource;
	}

	// TODO: remove this method after 'StreamPipe.openInputStream' is fixed
	default public InputStream openInputStreamPipe(StreamPipe pipe) {
		try {
			InputStream is = pipe.openInputStream();
			return is;
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not open pipe: '" + pipe + "' for multiple reads");
		}
	}

	/**
	 * Create a lock for a lockIdentifier (e.g. a correlationId)
	 * 
	 * @param lockManager
	 *            {@link LockManager}
	 * @param lockIdentifier
	 *            correlationId
	 * @param wopiApp
	 *            {@link WopiApp}
	 * @return {@link Lock}
	 */
	default public Lock createLock(LockManager lockManager, String lockIdentifier, WopiApp wopiApp) {
		Lock lock = lockManager.forIdentifier(lockIdentifier).lockTtl(wopiApp.getLockExpirationInMs(), TimeUnit.MILLISECONDS).exclusive();
		return lock;
	}

	/**
	 * Closed a {@link Lock} if it is not null
	 * 
	 * @param lock
	 *            {@link Lock} to be closed
	 */
	default public void closeLock(Lock lock) {
		if (lock != null) {
			lock.unlock();
		}
	}

	/**
	 * Delete a {@link WopiSession}
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param wopiSession
	 *            {@link WopiSession} to be deleted
	 */
	default public void deleteWopiSession(PersistenceGmSession session, WopiSession wopiSession, Logger logger) {
		if (wopiSession == null) {
			return;
		}
		Resource currentResource = wopiSession.getCurrentResource();
		List<Resource> resourceVersions = wopiSession.getResourceVersions();
		if (resourceVersions != null) {
			Iterator<Resource> it = resourceVersions.iterator();
			while (it.hasNext()) {
				Resource resourceVersion = it.next();
				it.remove();
				deleteResource(session, resourceVersion, false, logger);
			}
		}
		List<Resource> postOpenResourceVersions = wopiSession.getPostOpenResourceVersions();
		if (postOpenResourceVersions != null) {
			Iterator<Resource> it = postOpenResourceVersions.iterator();
			while (it.hasNext()) {
				Resource resourceVersion = it.next();
				it.remove();
				deleteResource(session, resourceVersion, false, logger);
			}
		}

		deleteResource(session, currentResource, false, logger);

		WopiLock lock = wopiSession.getLock();
		if (lock != null) {
			session.deleteEntity(lock, DeleteMode.dropReferences);
		}
		List<WopiAccessToken> accessTokens = wopiSession.getAccessTokens();
		if (accessTokens != null) {
			Iterator<WopiAccessToken> it = accessTokens.iterator();
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
		}
		session.deleteEntity(wopiSession, DeleteMode.dropReferences);
		session.commit();
		logger.debug(() -> "Successfully deleted WopiSession with correlationId: '" + wopiSession.getCorrelationId() + "'");
	}

	/**
	 * Delete a {@link Resource}
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param resource
	 *            {@link Resource} to be deleted
	 * @param commit
	 *            commit the delete
	 */
	default public void deleteResource(PersistenceGmSession session, Resource resource, boolean commit, Logger logger) {
		if (resource == null) {
			return;
		}
		ResourceSource resourceSource = resource.getResourceSource();
		if (resourceSource != null) {
			session.deleteEntity(resourceSource, DeleteMode.dropReferences);
		}
		ResourceSpecification specification = resource.getSpecification();
		if (specification != null) {
			session.deleteEntity(specification, DeleteMode.dropReferences);
		}

		try {
			DeleteResource deleteResource = DeleteResource.T.create();
			deleteResource.setResource(resource);
			deleteResource.eval(session).get();
		} catch (NotFoundException nfe) {
			logger.trace(() -> "Could not find a physical file/entry for resource " + resource.getId());
		} catch (Exception e) {
			logger.trace(() -> "Error while invoking DeleteResource for resource " + resource.getId(), e);
		}

		session.deleteEntity(resource, DeleteMode.dropReferences);

		if (commit) {
			session.commit();
		}
	}

	/**
	 * Resolve the correlationId for default documents (e.g. for demo)
	 * 
	 * @param prefix
	 *            prefix for the correlationId
	 * @param postfix
	 *            postfix for the correlationId
	 * @param fileName
	 *            name of the file
	 * @param documentMode
	 *            {@link DocumentMode}
	 * @return correlationId
	 */
	default public String resolveDefaultContentCorrelationId(String prefix, String postfix, String fileName, DocumentMode documentMode) {
		return prefix + "_" + documentMode + "_" + fileName + "_" + postfix;
	}

}
