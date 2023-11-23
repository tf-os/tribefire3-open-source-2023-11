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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static tribefire.extension.wopi.WopiMimeTypes.DOCX_EXTENSION;
import static tribefire.extension.wopi.WopiMimeTypes.DOC_EXTENSION;
import static tribefire.extension.wopi.WopiMimeTypes.PPTX_EXTENSION;
import static tribefire.extension.wopi.WopiMimeTypes.PPT_EXTENSION;
import static tribefire.extension.wopi.WopiMimeTypes.XLSX_EXTENSION;
import static tribefire.extension.wopi.WopiMimeTypes.XLS_EXTENSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.apache.velocity.shaded.commons.io.FilenameUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.processing.wopi.WopiConnectorUtil;
import com.braintribe.model.processing.wopi.WopiQueryingUtil;
import com.braintribe.model.processing.wopi.model.AccessToken;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.user.Role;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiAccessToken;
import com.braintribe.model.wopi.WopiLock;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.WopiStatus;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.integration.AddDemoDocs;
import com.braintribe.model.wopi.service.integration.AddDemoDocsResult;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessions;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.CloseWopiSession;
import com.braintribe.model.wopi.service.integration.CloseWopiSessionResult;
import com.braintribe.model.wopi.service.integration.DownloadCurrentResource;
import com.braintribe.model.wopi.service.integration.DownloadCurrentResourceResult;
import com.braintribe.model.wopi.service.integration.EnsureTestDoc;
import com.braintribe.model.wopi.service.integration.EnsureTestDocResult;
import com.braintribe.model.wopi.service.integration.ExpireWopiSessions;
import com.braintribe.model.wopi.service.integration.ExpireWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.ExportWopiSession;
import com.braintribe.model.wopi.service.integration.ExportWopiSessionResult;
import com.braintribe.model.wopi.service.integration.FindWopiSession;
import com.braintribe.model.wopi.service.integration.FindWopiSessionBySourceReference;
import com.braintribe.model.wopi.service.integration.FindWopiSessionBySourceReferenceResult;
import com.braintribe.model.wopi.service.integration.FindWopiSessionResult;
import com.braintribe.model.wopi.service.integration.GetWopiResource;
import com.braintribe.model.wopi.service.integration.GetWopiResourceResult;
import com.braintribe.model.wopi.service.integration.NotifyUpdateCurrentResource;
import com.braintribe.model.wopi.service.integration.NotifyUpdateCurrentResourceResult;
import com.braintribe.model.wopi.service.integration.OpenWopiDocument;
import com.braintribe.model.wopi.service.integration.OpenWopiDocumentResult;
import com.braintribe.model.wopi.service.integration.OpenWopiSession;
import com.braintribe.model.wopi.service.integration.OpenWopiSessionResult;
import com.braintribe.model.wopi.service.integration.RemoveAllWopiSessions;
import com.braintribe.model.wopi.service.integration.RemoveAllWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocs;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocsResult;
import com.braintribe.model.wopi.service.integration.RemoveWopiSession;
import com.braintribe.model.wopi.service.integration.RemoveWopiSessionResult;
import com.braintribe.model.wopi.service.integration.WopiHealthCheck;
import com.braintribe.model.wopi.service.integration.WopiHealthCheckResult;
import com.braintribe.model.wopi.service.integration.WopiRequest;
import com.braintribe.model.wopi.service.integration.WopiResult;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.FileStreamProviders;
import com.braintribe.utils.stream.api.StreamPipeFactory;

import tribefire.extension.wopi.WopiMimeTypes;

/**
 * Service processing related to WOPI
 * 
 * @see <a href= "https://wopi.readthedocs.io/projects/wopirest/en/latest/">WOPI integration documentation</a>
 * @see <a href= "https://wopi.readthedocs.io/en/latest/scenarios/coauth.html#word-web-co-authoring-behavior/">WOPI
 *      co-authoring documentation</a>
 * @see <a href= "https://wopi.readthedocs.io/en/latest/scenarios/postmessage.html/">WOPI post messages
 *      documentation</a>
 * @see <a href= "https://wopi.readthedocs.io/en/latest/hostpage.html#host-page-example/">WOPI building host page</a>
 * 
 *
 */
public class WopiServiceProcessor extends AbstractWopiServiceProcessor implements AccessRequestProcessor<WopiRequest, WopiResult> {

	private static final Logger logger = Logger.getLogger(WopiServiceProcessor.class);

	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private static final Set<String> mainTypes = asSet(DOC_EXTENSION, DOCX_EXTENSION, XLS_EXTENSION, XLSX_EXTENSION, PPT_EXTENSION, PPTX_EXTENSION);

	private PersistenceGmSessionFactory localSessionFactory;
	private PersistenceGmSessionFactory localSystemSessionFactory;
	private File demoDocsWriteRootFolder;
	private File demoDocsReadRootFolder;
	private File testDocsRootFolder;
	private com.braintribe.model.wopi.service.WopiServiceProcessor deployable;
	private LockManager lockManager;
	private String testDocCommand;

	private StreamPipeFactory streamPipeFactory;

	private Marshaller marshaller;

	// -----------------------------------------------------------------------
	// DELEGATES
	// -----------------------------------------------------------------------

	private AccessRequestProcessor<WopiRequest, WopiResult> delegate = AccessRequestProcessors.dispatcher(dispatching -> {

		// WopiSession related
		dispatching.register(OpenWopiSession.T, this::openWopiSession); // in workbench
		dispatching.register(FindWopiSession.T, this::findWopiSession);
		dispatching.register(FindWopiSessionBySourceReference.T, this::findWopiSessionBySourceReference);
		dispatching.register(GetWopiResource.T, this::getWopiResource);
		dispatching.register(CloseWopiSession.T, this::closeWopiSession); // in workbench
		dispatching.register(RemoveWopiSession.T, this::removeWopiSession); // in workbench
		dispatching.register(CloseAllWopiSessions.T, this::closeAllWopiSessions); // in workbench
		dispatching.register(RemoveAllWopiSessions.T, this::removeAllWopiSessions); // in workbench
		dispatching.register(ExportWopiSession.T, this::exportWopiSession); // in workbench
		dispatching.register(NotifyUpdateCurrentResource.T, this::notifyUpdateWopiResource);

		// only Worker related
		dispatching.register(ExpireWopiSessions.T, this::expireWopiSessions);

		// Demo/testing documents
		dispatching.register(AddDemoDocs.T, this::addDemoDocs); // in workbench
		dispatching.register(RemoveDemoDocs.T, this::removeDemoDocs); // in workbench
		dispatching.register(EnsureTestDoc.T, this::ensureTestDoc); // in workbench

		// Actions - only called via workbench
		dispatching.register(OpenWopiDocument.T, this::openWopiDocument); // in workbench
		dispatching.register(DownloadCurrentResource.T, this::downloadCurrentResource); // in workbench

		// Health check
		dispatching.register(WopiHealthCheck.T, this::wopiHealthCheck); // in workbench
	});

	@Override
	public WopiResult process(AccessRequestContext<WopiRequest> context) {

		WopiRequest request = context.getRequest();

		StopWatch stopWatch = new StopWatch();

		WopiResult result;
		try {
			result = delegate.process(context);
		} catch (IllegalArgumentException | IllegalStateException e) {
			// rollback session
			PersistenceGmSession session = context.getSession();
			PersistenceGmSession systemSession = context.getSystemSession();
			rollback(session);
			rollback(systemSession);

			Failure failure = FailureCodec.INSTANCE.encode(e);

			String typeSignature = request.entityType().getTypeSignature();
			// the assumption is that every request fits to the naming convention
			typeSignature = typeSignature + "Result";
			EntityType<? extends WopiResult> responseEntityType = typeReflection.getEntityType(typeSignature);

			//@formatter:off
			result = responseBuilder(responseEntityType, context.getRequest())
				.notifications(builder -> 
					builder	
					.add()
						.message().confirmError(failure.getMessage(), e)
					.close()
				).build();
			//@formatter:on 
			result.setFailure(failure);
		}
		long elapsedTime = stopWatch.getElapsedTime();

		LogLevel logLevel = LogLevel.TRACE;
		if (elapsedTime > deployable.getLogWarningThresholdInMs()) {
			logLevel = LogLevel.WARN;
		} else if (elapsedTime > deployable.getLogErrorThresholdInMs()) {
			logLevel = LogLevel.ERROR;
		}
		if (logger.isLevelEnabled(logLevel)) {
			logger.log(logLevel, "Executed request: '" + request + "' with failure: '" + result.getFailure() + "' in '" + elapsedTime + "'ms");
		}
		return result;
	}

	private void rollback(PersistenceGmSession session) {
		try {
			Transaction t = session.getTransaction();
			if (t.hasManipulations()) {
				t.undo(t.getManipulationsDone().size());
			}
		} catch (Exception e) {
			logger.warn(() -> "Rollback of session did not succeed", e);
		}
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	/**
	 * Open a {@link WopiSession}
	 * 
	 * <ul>
	 * <li>REQUEST: {@link OpenWopiSession}</li>
	 * <li>RESPONSE: {@link OpenWopiSessionResult}</li>
	 * </ul>
	 */
	public OpenWopiSessionResult openWopiSession(AccessRequestContext<OpenWopiSession> context) {

		PersistenceGmSession session = context.getSession();

		OpenWopiSession request = context.getRequest();
		String correlationId = request.getCorrelationId();
		String sourceReference = request.getSourceReference();
		Resource resource = request.getResource();
		Set<String> allowedRoles = request.getAllowedRoles();
		DocumentMode documentMode = request.getDocumentMode();

		Arguments.notEmpty(correlationId, "CorrelationId must not be empty");
		Arguments.notNull(resource, "resource must not be null");
		Arguments.notEmpty(allowedRoles, "allowedRoles must not be empty");
		Arguments.notNull(documentMode, "documentMode must not be null");

		String name = resource.getName();

		try {
			String encodedCorrelationId = URLEncoder.encode(correlationId, StandardCharsets.UTF_8.toString());
			if (!correlationId.equals(encodedCorrelationId)) {
				throw new IllegalArgumentException(
						"correlationId must be URL compatible - it is: '" + correlationId + "', it would be encoded: '" + encodedCorrelationId + "'");
			}
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}

		WopiSession existingWopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);

		if (existingWopiSession != null) {
			WopiStatus status = existingWopiSession.getStatus();
			if (status == WopiStatus.expired || status == WopiStatus.closed) {
				// if the WOPI session is expired or closed: delete it and create a new one
				RemoveWopiSession removeWopiSession = RemoveWopiSession.T.create();
				removeWopiSession.setWopiSession(existingWopiSession);
				RemoveWopiSessionResult removeWopiSessionResult = removeWopiSession.eval(session).get();
				Failure failure = removeWopiSessionResult.getFailure();
				if (failure != null) {
					Throwable t = FailureCodec.INSTANCE.decode(failure);
					throw new IllegalStateException("Error while removing WopiSession with correlationId: '" + correlationId
							+ "' - therefore could not create a new WopiSession", t);
				}
			} else {
				throw new IllegalStateException("There already exists an '" + WopiSession.T.getShortName() + "' for correlationId: '" + correlationId
						+ "'. Use '" + FindWopiSession.T.getShortName() + "' for checking for open '" + WopiSession.T.getShortName() + "'");
			}
		}

		Resource uploadedResource = uploadResource(session, resource, correlationId, documentMode);
		String mimeType = uploadedResource.getMimeType();

		// validate roles
		validateAllowedRolesOnCreation(allowedRoles);

		WopiApp wopiApp = deployable.getWopiApp();

		String action = documentMode.name();

		Date now = new Date();

		WopiSession wopiSession = session.create(WopiSession.T);
		wopiSession.setContext(wopiApp.getContext());

		wopiSession.setStatus(WopiStatus.open);
		wopiSession.setAllowedRoles(allowedRoles);
		wopiSession.setCorrelationId(correlationId);
		wopiSession.setSourceReference(sourceReference);
		wopiSession.setCreationDate(now);
		wopiSession.setDocumentMode(documentMode);

		String creatorId = request.getCreatorId();
		if (CommonTools.isEmpty(creatorId)) {
			creatorId = session.getSessionAuthorization().getUserId();
		}
		wopiSession.setCreatorId(creatorId);

		String creator = request.getCreator();
		if (CommonTools.isEmpty(creator)) {
			creator = session.getSessionAuthorization().getUserName();
		}
		wopiSession.setCreator(creator);

		wopiSession.setLastUpdated(now);
		wopiSession.setLastUpdatedUserId(creatorId);
		wopiSession.setLastUpdatedUser(creator);

		wopiSession.setTenant(resolveSetting(wopiApp.getTenant(), request.getTenant()));
		wopiSession.setMaxVersions(resolveSetting(wopiApp.getMaxVersions(), request.getMaxVersions()));
		wopiSession.setVersion(1);
		wopiSession.setWopiLockExpirationInMs(resolveSetting(wopiApp.getWopiLockExpirationInMs(), request.getWopiLockExpirationInMs()));
		wopiSession.setTags(request.getTags());
		wopiSession.setUpdateNotificationMessage(request.getUpdateNotificationMessage());
		wopiSession.setUpdateNotificationAccessId(request.getUpdateNotificationAccessId());

		// some settings can be configured globally in the WopiApp, some can't
		wopiSession.setShowUserFriendlyName(resolveSetting(wopiApp.getShowUserFriendlyName(), request.getShowUserFriendlyName()));
		wopiSession.setShowBreadcrumbBrandName(resolveSetting(wopiApp.getShowBreadcrumbBrandName(), request.getShowBreadcrumbBrandName()));
		wopiSession.setBreadcrumbBrandName(resolveUnmodifiable(request.getBreadcrumbBrandName()));
		wopiSession.setBreadcrumbBrandNameUrl(resolveUnmodifiable(request.getBreadcrumbBrandNameUrl()));
		wopiSession.setShowBreadcrumbDocName(resolveSetting(wopiApp.getShowBreadCrumbDocName(), request.getShowBreadcrumbDocName()));
		wopiSession.setBreadcrumbDocName(resolveUnmodifiable(request.getBreadcrumbDocName()));
		wopiSession.setShowBreadcrumbFolderName(resolveSetting(wopiApp.getShowBreadcrumbFolderName(), request.getShowBreadcrumbFolderName()));
		wopiSession.setBreadcrumbFolderName(resolveUnmodifiable(request.getBreadcrumbFolderName()));
		wopiSession.setBreadcrumbFolderNameUrl(resolveUnmodifiable(request.getBreadcrumbFolderNameUrl()));
		wopiSession.setDisablePrint(resolveSetting(wopiApp.getDisablePrint(), request.getDisablePrint()));
		wopiSession.setDisableTranslation(resolveSetting(wopiApp.getDisableTranslation(), request.getDisableTranslation()));

		// use relative URL
		String url = "/component/wopi/tf-resource=?correlationId=" + correlationId + "&mimetype=" + mimeType + "&name=" + name + "&action=" + action;

		logger.debug(() -> "Triggered viewing WOPI document using url: '" + url + "'");

		// wopiSession.setCreator(creator);
		wopiSession.setCurrentResource(uploadedResource);
		wopiSession.setWopiUrl(url);
		session.commit();

		WopiSession wopiSessionReturn = cloneWopiSession(wopiSession);

		OpenWopiSessionResult result = prepareSimpleNotification(OpenWopiSessionResult.T, request, Level.SUCCESS,
				"Successfully opened WOPI Session: '" + correlationId + "'");
		result.setWopiSession(wopiSessionReturn);

		return result;
	}

	/**
	 * Find a {@link WopiSession}
	 * 
	 * <ul>
	 * <li>REQUEST: {@link FindWopiSession}</li>
	 * <li>RESPONSE: {@link FindWopiSessionResult}</li>
	 * </ul>
	 */
	public FindWopiSessionResult findWopiSession(AccessRequestContext<FindWopiSession> context) {
		PersistenceGmSession session = context.getSession();
		FindWopiSessionResult result = FindWopiSessionResult.T.create();

		FindWopiSession request = context.getRequest();
		String correlationId = request.getCorrelationId();
		boolean findClosed = request.getFindClosed();
		boolean findExpired = request.getFindExpired();
		boolean includeCurrentResource = request.getIncludeCurrentResource();
		boolean includeResourceVersions = request.getIncludeResourceVersions();
		boolean includePostOpenResourceVersions = request.getIncludePostOpenResourceVersions();

		Arguments.notEmpty(correlationId, "CorrelationId must not be empty");

		WopiSession existingWopiSession;

		if (findClosed && findExpired) {
			// open, closed and expired
			existingWopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);
		} else if (findClosed) {
			// open and closed
			existingWopiSession = WopiQueryingUtil.queryOpenOrExpiredWopiSession(session, correlationId);
		} else if (findExpired) {
			// open and expired
			existingWopiSession = WopiQueryingUtil.queryOpenOrClosedWopiSession(session, correlationId);
		} else {
			// open
			existingWopiSession = WopiQueryingUtil.queryOpenWopiSession(session, correlationId);
		}

		if (existingWopiSession != null) {

			WopiSession wopiSession = cloneWopiSession(existingWopiSession);
			result.setWopiSession(wopiSession);

			if (includeCurrentResource) {
				Resource currentResource = existingWopiSession.getCurrentResource();
				wopiSession.setCurrentResource(currentResource);
			} else {
				wopiSession.setCurrentResource(null);
			}

			if (includeResourceVersions) {
				List<Resource> resourceVersions = new ArrayList<>();
				existingWopiSession.getResourceVersions().forEach(resource -> {
					resourceVersions.add(resource);
				});
				wopiSession.setResourceVersions(resourceVersions);
			} else {
				wopiSession.setResourceVersions(new ArrayList<>());
			}

			if (includePostOpenResourceVersions) {
				List<Resource> postOpenResourceVersions = new ArrayList<>();
				existingWopiSession.getPostOpenResourceVersions().forEach(resource -> {
					postOpenResourceVersions.add(resource);
				});
				wopiSession.setPostOpenResourceVersions(postOpenResourceVersions);
			} else {
				wopiSession.setPostOpenResourceVersions(new ArrayList<>());
			}
		}
		return result;
	}

	/**
	 * Find a {@link WopiSession} by source reference
	 * 
	 * <ul>
	 * <li>REQUEST: {@link FindWopiSessionBySourceReference}</li>
	 * <li>RESPONSE: {@link FindWopiSessionBySourceReferenceResult}</li>
	 * </ul>
	 */
	public FindWopiSessionBySourceReferenceResult findWopiSessionBySourceReference(AccessRequestContext<FindWopiSessionBySourceReference> context) {
		PersistenceGmSession session = context.getSession();
		FindWopiSessionBySourceReferenceResult result = FindWopiSessionBySourceReferenceResult.T.create();

		FindWopiSessionBySourceReference request = context.getRequest();
		String sourceReference = request.getSourceReference();

		Arguments.notEmpty(sourceReference, "sourceReference must not be empty");

		WopiSession existingWopiSession = WopiQueryingUtil.queryOpenWopiSessionBySourceReference(session, sourceReference);
		result.setWopiSession(existingWopiSession);

		if (existingWopiSession != null) {

			WopiSession wopiSession = cloneWopiSession(existingWopiSession);
			Resource currentResource = existingWopiSession.getCurrentResource();
			wopiSession.setCurrentResource(currentResource);

			result.setWopiSession(wopiSession);
		}

		return result;
	}

	/**
	 * Get the actual {@link Resource} of a {@link WopiSession}
	 * 
	 * <ul>
	 * <li>REQUEST: {@link GetWopiResource}</li>
	 * <li>RESPONSE: {@link GetWopiResourceResult}</li>
	 * </ul>
	 */
	public GetWopiResourceResult getWopiResource(AccessRequestContext<GetWopiResource> context) {
		PersistenceGmSession session = context.getSession();

		GetWopiResourceResult result = GetWopiResourceResult.T.create();

		GetWopiResource request = context.getRequest();
		String correlationId = request.getCorrelationId();
		boolean includeCurrentResource = request.getIncludeCurrentResource();
		boolean includeResourceVersions = request.getIncludeResourceVersions();
		boolean includePostOpenResourceVersions = request.getIncludePostOpenResourceVersions();

		Arguments.notEmpty(correlationId, "CorrelationId must not be empty");

		logger.debug(() -> "GetWopiResource with includeCurrentResource: '" + includeCurrentResource + "' includeResourceVersions: '"
				+ includeResourceVersions + "' includePostOpenResourceVersions: '" + includePostOpenResourceVersions + "'");

		WopiSession existingWopiSession = WopiQueryingUtil.queryOpenWopiSession(session, correlationId);

		if (existingWopiSession != null) {

			if (includeCurrentResource) {
				Resource currentResource = existingWopiSession.getCurrentResource();
				result.setCurrentResource(currentResource);
			}

			if (includeResourceVersions) {
				List<Resource> resourceVersions = new ArrayList<>();
				existingWopiSession.getResourceVersions().forEach(resource -> {
					resourceVersions.add(resource);
				});
				result.setResourceVersions(resourceVersions);
			}

			if (includePostOpenResourceVersions) {
				List<Resource> postOpenResourceVersions = new ArrayList<>();
				existingWopiSession.getPostOpenResourceVersions().forEach(resource -> {
					postOpenResourceVersions.add(resource);
				});
				result.setPostOpenResourceVersions(postOpenResourceVersions);
			}
		}

		logger.debug(() -> "GetWopiResource returning currentResource: '" + result.getCurrentResource() + "' resourceVersions: '"
				+ result.getResourceVersions().size() + "' ('"
				+ result.getResourceVersions().stream().map(r -> r.getId()).collect(Collectors.toList()) + "' postOpenResourceVersions: '"
				+ result.getPostOpenResourceVersions().size() + "' ('"
				+ result.getPostOpenResourceVersions().stream().map(r -> r.getId()).collect(Collectors.toList()) + "')");

		return result;
	}

	/**
	 * Close a particular {@link WopiSession}
	 * 
	 * <ul>
	 * <li>REQUEST: {@link CloseWopiSession}</li>
	 * <li>RESPONSE: {@link CloseWopiSessionResult}</li>
	 * </ul>
	 */
	public CloseWopiSessionResult closeWopiSession(AccessRequestContext<CloseWopiSession> context) {
		PersistenceGmSession systemSession = context.getSystemSession();

		CloseWopiSession request = context.getRequest();
		WopiSession _wopiSession = request.getWopiSession();

		Arguments.notNull(_wopiSession, "wopiSession must not be null");
		Arguments.notEmpty(_wopiSession.getCorrelationId(), "correlationId must not be null");

		String msg;
		Level level;

		String correlationId = _wopiSession.getCorrelationId();
		WopiSession wopiSession = WopiQueryingUtil.queryNotClosedWopiSession(systemSession, correlationId);
		if (wopiSession != null) {

			wopiSession.setStatus(WopiStatus.closed);
			systemSession.commit();
			level = Level.SUCCESS;
			msg = "Successfully closed WOPI session: '" + correlationId + "'";
		} else {
			logger.debug(() -> "Could not find " + WopiSession.T.getShortName() + " with correlationId: '" + correlationId
					+ "' for closing - ignore and continue...");
			level = Level.INFO;
			msg = "Could not find '" + WopiSession.T.getShortName() + "' with correlationId: '" + correlationId + "' for closing";
		}

		CloseWopiSessionResult result;

		//@formatter:off
		result = responseBuilder(CloseWopiSessionResult.T, request)
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(level)
							.message(msg)
						.close()
						.command()
							.reloadView("Reload View")
					.close()
				).build();
		//@formatter:on

		return result;
	}

	/**
	 * Remove a particular {@link WopiSession} - no matter in which {@link WopiStatus} the {@link WopiSession} is
	 * 
	 * <ul>
	 * <li>REQUEST: {@link RemoveWopiSession}</li>
	 * <li>RESPONSE: {@link RemoveWopiSessionResult}</li>
	 * </ul>
	 */
	public RemoveWopiSessionResult removeWopiSession(AccessRequestContext<RemoveWopiSession> context) {
		PersistenceGmSession session = context.getSession();

		RemoveWopiSession request = context.getRequest();
		WopiSession _wopiSession = request.getWopiSession();

		Arguments.notNull(_wopiSession, "wopiSession must not be null");
		Arguments.notEmpty(_wopiSession.getCorrelationId(), "CorrelationId must not be empty");

		String msg;
		Level level;

		String correlationId = _wopiSession.getCorrelationId();

		WopiSession wopiSession;
		wopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);
		if (wopiSession != null) {

			deleteWopiSession(session, wopiSession, logger);

			level = Level.SUCCESS;
			msg = "Successfully removed WOPI session: '" + correlationId + "'";
		} else {
			logger.debug(() -> "Could not find " + WopiSession.T.getShortName() + " with correlationId: '" + correlationId
					+ "' for removing - ignore and continue...");
			level = Level.INFO;
			msg = "Could not find '" + WopiSession.T.getShortName() + "' with correlationId: '" + correlationId + "' for removing";
		}

		RemoveWopiSessionResult result;

		//@formatter:off
		result = responseBuilder(RemoveWopiSessionResult.T, request)
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(level)
							.message(msg)
						.close()
						.command()
							.reloadView("Reload View")
					.close()
				).build();
		//@formatter:on

		return result;
	}

	/**
	 * Closes all {@link WopiSession}s
	 * 
	 * <ul>
	 * <li>REQUEST: {@link CloseAllWopiSessions}</li>
	 * <li>RESPONSE: {@link CloseAllWopiSessionsResult}</li>
	 * </ul>
	 */
	public CloseAllWopiSessionsResult closeAllWopiSessions(AccessRequestContext<CloseAllWopiSessions> context) {
		PersistenceGmSession systemSession = context.getSystemSession();

		CloseAllWopiSessions request = context.getRequest();
		String _context = request.getContext();

		List<WopiSession> wopiSessions;

		int _closedEntities = 0;

		boolean finishedClosing = false;

		while (!finishedClosing) {

			if (CommonTools.isEmpty(_context)) {
				wopiSessions = WopiQueryingUtil.queryOpenOrExpiredWopiSessions(systemSession);
			} else {
				wopiSessions = WopiQueryingUtil.queryContextBasedOpenOrExpiredWopiSessions(systemSession, _context);
			}

			if (CommonTools.isEmpty(wopiSessions)) {
				finishedClosing = true;
			}

			for (WopiSession wopiSession : wopiSessions) {

				wopiSession.setStatus(WopiStatus.closed);
				_closedEntities++;
			}
			systemSession.commit();
		}

		int closedEntities = _closedEntities;

		logger.info(() -> "Closed '" + closedEntities + "' from access: '" + systemSession.getAccessId() + "'");

		CloseAllWopiSessionsResult result = prepareSimpleNotification(CloseAllWopiSessionsResult.T, request, Level.SUCCESS,
				"Successfully closed all WOPI sessions: '" + closedEntities + "'");

		return result;
	}

	/**
	 * Remove all {@link WopiSession}s
	 * 
	 * <ul>
	 * <li>REQUEST: {@link RemoveAllWopiSessions}</li>
	 * <li>RESPONSE: {@link RemoveAllWopiSessionsResult}</li>
	 * </ul>
	 */
	public RemoveAllWopiSessionsResult removeAllWopiSessions(AccessRequestContext<RemoveAllWopiSessions> context) {
		PersistenceGmSession session = context.getSession();

		RemoveAllWopiSessions request = context.getRequest();
		boolean forceRemove = request.getForceRemove();
		String _context = request.getContext();

		int _removedEntities = 0;

		List<WopiSession> wopiSessions;

		boolean finishedRemoving = false;

		while (!finishedRemoving) {
			if (forceRemove) {
				if (CommonTools.isEmpty(_context)) {
					wopiSessions = WopiQueryingUtil.queryWopiSessions(session);
				} else {
					wopiSessions = WopiQueryingUtil.queryContextBasedWopiSessions(session, _context);
				}
			} else {
				if (CommonTools.isEmpty(_context)) {
					wopiSessions = WopiQueryingUtil.queryExpiredOrClosedWopiSessions(session);
				} else {
					wopiSessions = WopiQueryingUtil.queryContextBasedExpiredOrClosedWopiSessions(session, _context);
				}
			}

			if (CommonTools.isEmpty(wopiSessions)) {
				finishedRemoving = true;
			}

			for (WopiSession wopiSession : wopiSessions) {

				deleteWopiSession(session, wopiSession, logger);
				_removedEntities++;
			}
		}
		int removedEntities = _removedEntities;

		logger.info(() -> "Removed '" + removedEntities + "' from access: '" + session.getAccessId() + "'");

		RemoveAllWopiSessionsResult result = prepareSimpleNotification(RemoveAllWopiSessionsResult.T, request, Level.SUCCESS,
				"Successfully removed all WOPI sessions: '" + removedEntities + "'");

		return result;
	}

	/**
	 * Notify update of current resource
	 * 
	 * <ul>
	 * <li>REQUEST: {@link NotifyUpdateCurrentResource}</li>
	 * <li>RESPONSE: {@link NotifyUpdateCurrentResourceResult}</li>
	 * </ul>
	 */
	public NotifyUpdateCurrentResourceResult notifyUpdateWopiResource(AccessRequestContext<NotifyUpdateCurrentResource> context) {
		PersistenceGmSession session = context.getSession();
		NotifyUpdateCurrentResource request = context.getRequest();

		WopiSession wopiSession = request.getWopiSession();
		String userName = request.getUserName();

		String accessId = wopiSession.getUpdateNotificationAccessId();

		NotifyUpdateCurrentResourceResult result = NotifyUpdateCurrentResourceResult.T.create();

		String msg = wopiSession.getUpdateNotificationMessage();
		if (!CommonTools.isEmpty(msg)) {

			//@formatter:off
			List<Notification> notifications =
					Notifications
						.build()
						.add()
							.message().info(msg)
						.close()
						.list();
			//@formatter:on

			Notify notify = Notify.T.create();
			notify.setNotifications(notifications);

			String _accessId = session.getAccessId();
			if (!CommonTools.isEmpty(accessId)) {
				_accessId = accessId;
			}

			String clientIdPattern = "tribefire-.*\\." + _accessId.replace(".", "\\.");

			PushRequest pushRequest = PushRequest.T.create();
			pushRequest.setServiceRequest(notify);
			pushRequest.setRolePattern("\\$user-" + userName);
			pushRequest.setClientIdPattern(clientIdPattern);
			pushRequest.eval(session).get();
		}

		return result;
	}

	/**
	 * Export {@link WopiSession}
	 * 
	 * <ul>
	 * <li>REQUEST: {@link ExportWopiSession}</li>
	 * <li>RESPONSE: {@link ExportWopiSessionResult}</li>
	 * </ul>
	 */
	public ExportWopiSessionResult exportWopiSession(AccessRequestContext<ExportWopiSession> context) {

		PersistenceGmSession session = context.getSession();
		ExportWopiSession request = context.getRequest();
		Set<WopiSession> _wopiSessions = request.getWopiSessions();
		boolean includeDiagnosticPackage = request.getIncludeDiagnosticPackage();
		boolean includeCurrentResource = request.getIncludeCurrentResource();
		boolean includeResourceVersions = request.getIncludeResourceVersions();
		boolean includePostOpenResourceVersions = request.getIncludePostOpenResourceVersions();

		List<WopiSession> wopiSessions = WopiQueryingUtil.queryWopiSessions(session,
				_wopiSessions.stream().map(ws -> ws.getCorrelationId()).collect(Collectors.toSet()));
		ZipContext exportZip = Archives.zip();

		Resource export = ExportWopiSessionUtil.export(wopiSessions, marshaller, exportZip, session, includeDiagnosticPackage, includeCurrentResource,
				includeResourceVersions, includePostOpenResourceVersions);

		//@formatter:off
		return responseBuilder(ExportWopiSessionResult.T, request)
				.responseEnricher(r -> {
					r.setExportPackage(export);
				}).notifications(builder -> {
					builder
						.add()
							.message()
								.info("Successfully exported " + wopiSessions.size() + " WopiSession(s)!")
							.command()
								.downloadResource("WopiSession Export Package", export)
						.close();
				}).build();
		//@formatter:on
	}

	/**
	 * Expire {@link WopiSession}s
	 * 
	 * <ul>
	 * <li>REQUEST: {@link ExpireWopiSessions}</li>
	 * <li>RESPONSE: {@link ExpireWopiSessionsResult}</li>
	 * </ul>
	 */
	public ExpireWopiSessionsResult expireWopiSessions(AccessRequestContext<ExpireWopiSessions> context) {
		PersistenceGmSession systemSession = context.getSystemSession(); // needs to be systemSession to adapt status

		ExpireWopiSessions request = context.getRequest();
		String _context = request.getContext();

		long wopiSessionExpirationInMs = deployable.getWopiApp().getWopiSessionExpirationInMs();

		int _expiredEntities = 0;

		List<WopiSession> wopiSessions;

		boolean finishedExpiring = false;

		while (!finishedExpiring) {
			if (CommonTools.isEmpty(_context)) {
				wopiSessions = WopiQueryingUtil.queryExpiredWopiSessionsCandidates(systemSession, wopiSessionExpirationInMs);
			} else {
				wopiSessions = WopiQueryingUtil.queryContextBasedExpiredWopiSessionsCandidates(systemSession, wopiSessionExpirationInMs, _context);
			}

			if (CommonTools.isEmpty(wopiSessions)) {
				finishedExpiring = true;
			}

			for (WopiSession wopiSession : wopiSessions) {

				wopiSession.setStatus(WopiStatus.expired);
				systemSession.commit();
				_expiredEntities++;
			}
		}
		int expiredEntities = _expiredEntities;

		logger.info(() -> "Expired '" + expiredEntities + "' from access: '" + systemSession.getAccessId() + "'");

		ExpireWopiSessionsResult result = prepareSimpleNotification(ExpireWopiSessionsResult.T, request, Level.SUCCESS,
				"Successfully expired all WOPI sessions: '" + expiredEntities + "'");

		return result;
	}

	/**
	 * Adds documents for demo purposes
	 * 
	 * <ul>
	 * <li>REQUEST: {@link AddDemoDocs}</li>
	 * <li>RESPONSE: {@link AddDemoDocsResult}</li>
	 * </ul>
	 */
	public AddDemoDocsResult addDemoDocs(AccessRequestContext<AddDemoDocs> context) {
		PersistenceGmSession session = context.getSession();

		AddDemoDocs request = context.getRequest();
		boolean onlyMainTypes = request.getOnlyMainTypes();

		RemoveDemoDocs removeDemoDocs = RemoveDemoDocs.T.create();
		removeDemoDocs.eval(session).get();

		List<File> editFiles = new ArrayList<>(Arrays.asList(demoDocsWriteRootFolder.listFiles()));
		List<File> viewFiles = new ArrayList<>(Arrays.asList(demoDocsReadRootFolder.listFiles()));

		if (onlyMainTypes) {
			editFiles = editFiles.stream().filter(f -> mainTypes.contains(FilenameUtils.getExtension(f.getName()))).collect(Collectors.toList());
			viewFiles = viewFiles.stream().filter(f -> mainTypes.contains(FilenameUtils.getExtension(f.getName()))).collect(Collectors.toList());
		}

		String fileNamesEdit = editFiles.stream().map(file -> file.getName()).collect(Collectors.joining(", "));
		String fileNamesView = viewFiles.stream().map(file -> file.getName()).collect(Collectors.joining(", "));

		List<WopiSession> wopiSessions = new ArrayList<>();
		wopiSessions.addAll(addDemoDocs(DocumentMode.edit, editFiles, session));
		wopiSessions.addAll(addDemoDocs(DocumentMode.view, viewFiles, session));

		logger.info(() -> "Successfully ensured demo resources: '" + fileNamesEdit + "', '" + fileNamesView + "'");

		AddDemoDocsResult result = prepareSimpleNotification(AddDemoDocsResult.T, request, Level.SUCCESS, "Successfully added demo docs");
		result.setWopiSessions(wopiSessions);
		return result;
	}

	/**
	 * Deletes documents for demo purposes
	 * 
	 * <ul>
	 * <li>REQUEST: {@link RemoveDemoDocs}</li>
	 * <li>RESPONSE: {@link RemoveDemoDocsResult}</li>
	 * </ul>
	 */
	public RemoveDemoDocsResult removeDemoDocs(AccessRequestContext<RemoveDemoDocs> context) {
		PersistenceGmSession session = context.getSession();

		RemoveDemoDocs request = context.getRequest();

		List<File> editFiles = new ArrayList<>(Arrays.asList(demoDocsWriteRootFolder.listFiles()));
		List<File> viewFiles = new ArrayList<>(Arrays.asList(demoDocsReadRootFolder.listFiles()));

		String fileNamesEdit = editFiles.stream().map(file -> file.getName()).collect(Collectors.joining(", "));
		String fileNamesView = viewFiles.stream().map(file -> file.getName()).collect(Collectors.joining(", "));

		removeDemoDocs(session, editFiles, DocumentMode.edit);
		removeDemoDocs(session, viewFiles, DocumentMode.view);
		session.commit();

		logger.info(() -> "Successfully removed demo resources: '" + fileNamesEdit + "', '" + fileNamesView + "'");

		RemoveDemoDocsResult result = prepareSimpleNotification(RemoveDemoDocsResult.T, request, Level.SUCCESS, "Successfully removed all demo docs");
		return result;
	}

	/**
	 * Ensure test document
	 * 
	 * <ul>
	 * <li>REQUEST: {@link EnsureTestDoc}</li>
	 * <li>RESPONSE: {@link EnsureTestDocResult}</li>
	 * </ul>
	 */
	public EnsureTestDocResult ensureTestDoc(AccessRequestContext<EnsureTestDoc> context) {
		PersistenceGmSession session = context.getSession();
		String sessionId = session.getSessionAuthorization().getSessionId();
		String userName = session.getSessionAuthorization().getUserName();

		EnsureTestDoc request = context.getRequest();
		DocumentMode documentMode = request.getDocumentMode();
		Set<String> testNames = request.getTestNames();

		File file = new File(testDocsRootFolder, "test.wopitest");

		String name = file.getName();

		WopiSession wopiSession;

		String correlationId = resolveDefaultContentCorrelationId(TEST_CORRELATION_ID_PREFIX, DEMO_CORRELATION_ID_POSTFIX, file.getName(),
				documentMode);

		// cleanup existing test document
		RemoveWopiSession removeWopiSession = RemoveWopiSession.T.create();
		wopiSession = WopiSession.T.create();
		wopiSession.setCorrelationId(correlationId);
		removeWopiSession.setWopiSession(wopiSession);
		removeWopiSession.eval(session).get();

		Resource resource;
		try (InputStream is = new FileInputStream(file)) {
			resource = Resource.T.create();
			resource.setName(name);
			resource.setTags(asSet(TEST_DOC_TAG));
			resource.assignTransientSource(FileStreamProviders.from(is));

			OpenWopiSession openWopiSession = OpenWopiSession.T.create();
			openWopiSession.setCorrelationId(correlationId);
			openWopiSession.setDocumentMode(documentMode);
			openWopiSession.setResource(resource);
			openWopiSession.setTenant(TEST_DOC_TAG);
			openWopiSession.setTags(asSet(TEST_DOC_TAG));
			openWopiSession.setUpdateNotificationMessage("Update from Office Online on '" + correlationId + "'");
			OpenWopiSessionResult openWopiSessionResult = openWopiSession.eval(session).get();
			wopiSession = openWopiSessionResult.getWopiSession();

			Failure failure = openWopiSessionResult.getFailure();
			if (failure != null) {
				Throwable throwable = FailureCodec.INSTANCE.decode(failure);
				throw Exceptions.unchecked(throwable, "Could not create WopiSession for file: '" + file + "'");
			}

			logger.debug(() -> "Added test file: '" + name + "' correlationId: '" + correlationId + "' documentMode: '" + documentMode + "'");
		} catch (Exception e) {
			throw new GenericRuntimeException("Could not find file: '" + file + "' for creating test document", e);
		}
		// String createPublicWopiUrl = createPublicWopiUrl(session, userSessionId, wopiSession.getWopiUrl());

		//@formatter:off
//		docker run -it --rm tylerbutler/wopi-validator -- -w http://82.218.250.141:31082/tribefire-services/component/wopi/files/aaa -t eyJ1c2VyTmFtZSI6ImNvcnRleCIsInNlc3Npb25JZCI6IjIwMjAxMTIwMDgzNzEyODAzLTY1NmYwZWNhLTUwMGQtNDYxZC1hZmJhLTcyMzkzNmZlNDI2NyJ9 -l 1605744362427 -g ProofKeys
		//@formatter:on

		AccessToken _accessToken = new AccessToken();
		_accessToken.setSessionId(sessionId);
		_accessToken.setUserName(userName);
		String accessToken = _accessToken.encode();

		String publicServicesUrl = WopiConnectorUtil.getPublicServicesUrl(deployable.getWopiApp().getWopiWacConnector().getCustomPublicServicesUrl());

		String fileUrl = publicServicesUrl + "/component/wopi/files/" + wopiSession.getCorrelationId();

		// String fileUrl = "http://82.218.250.141:31082/tribefire-services/component/wopi/files/aaa";
		List<String> commands = new ArrayList<>();
		if (CommonTools.isEmpty(testNames)) {
			// execute all tests
			String command = String.format(testDocCommand, fileUrl, accessToken, 0l, "");
			commands.add(command);
		} else {
			testNames.forEach(testName -> {
				String command = String.format(testDocCommand, fileUrl, accessToken, 0l, "-n " + testName);
				commands.add(command);
			});
		}

		// manually set AccessToken - simulate OpenWopiSession - using system session
		PersistenceGmSession systemSession = localSystemSessionFactory.newSession(session.getAccessId());
		WopiSession queryOpenWopiSession = WopiQueryingUtil.queryOpenWopiSession(systemSession, correlationId);
		WopiAccessToken wopiAccessToken = systemSession.create(WopiAccessToken.T);
		wopiAccessToken.setToken(accessToken);
		wopiAccessToken.setUser(userName);
		queryOpenWopiSession.getAccessTokens().add(wopiAccessToken);
		systemSession.commit();

		EnsureTestDocResult result = prepareSimpleNotification(EnsureTestDocResult.T, request, Level.SUCCESS, "Successfully added test document");
		result.setWopiSession(wopiSession);
		result.setAccessToken(accessToken);
		result.setCommands(commands);

		logger.info("WopiSession: '" + wopiSession + "'");
		logger.info("AccessToken: '" + accessToken + "'");
		logger.info("Commands: '" + commands.size() + "'");
		commands.forEach(command -> {
			logger.info("  " + command);
		});

		return result;
	}

	/**
	 * Open a WOPI document
	 * 
	 * <ul>
	 * <li>REQUEST: {@link OpenWopiDocument}</li>
	 * <li>RESPONSE: {@link OpenWopiDocumentResult}</li>
	 * </ul>
	 */
	public OpenWopiDocumentResult openWopiDocument(AccessRequestContext<OpenWopiDocument> context) {
		PersistenceGmSession session = context.getSession();

		OpenWopiDocument request = context.getRequest();
		WopiSession wopiSession = request.getWopiSession();
		String userSessionId = request.getUserSessionId();

		String wopiUrl = wopiSession.getWopiUrl();

		Level level;
		String msg;
		level = Level.SUCCESS;
		msg = "Successfully open WOPI document of WopiSession: '" + wopiSession.getCorrelationId() + "'";

		String publicWopiUrl = createPublicWopiUrl(session, userSessionId, wopiUrl);

		//@formatter:off
		OpenWopiDocumentResult result = responseBuilder(OpenWopiDocumentResult.T, request)
			.responseEnricher(r -> {
				r.setUrl(publicWopiUrl);
			})
			.notifications(builder -> 
				builder	
				.add()
					.message()
						.level(level)
						.message(msg)
					.close()
					.command()
						.gotoUrl(wopiSession.getCorrelationId()).target(wopiSession.getCorrelationId()).url(publicWopiUrl).close()
				.close()
			).build();
		//@formatter:on 

		return result;
	}

	/**
	 * Download a current resource of a WOPI document
	 * 
	 * <ul>
	 * <li>REQUEST: {@link DownloadCurrentResource}</li>
	 * <li>RESPONSE: {@link DownloadCurrentResourceResult}</li>
	 * </ul>
	 */
	public DownloadCurrentResourceResult downloadCurrentResource(AccessRequestContext<DownloadCurrentResource> context) {

		DownloadCurrentResource request = context.getRequest();

		WopiSession wopiSession = request.getWopiSession();
		Resource currentResource = wopiSession.getCurrentResource();

		String url = prepareStreamingUrl(request, currentResource);

		//@formatter:off
		DownloadCurrentResourceResult result = responseBuilder(DownloadCurrentResourceResult.T, request)
				.responseEnricher(r -> {
					r.setUrl(url);
				})
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(Level.SUCCESS)
							.message("Successfully download current resource: '" + currentResource.getId() + "'")
						.close()
						.command()
							.gotoUrl(currentResource.getId()).target(currentResource.getId()).url(url).close()
					.close()
				).build();
		//@formatter:on

		return result;
	}

	/**
	 * Executed a Health Check by opening all demo WOPI documents in browser tabs
	 * 
	 * <ul>
	 * <li>REQUEST: {@link WopiHealthCheck}</li>
	 * <li>RESPONSE: {@link WopiHealthCheckResult}</li>
	 * </ul>
	 */
	public WopiHealthCheckResult wopiHealthCheck(AccessRequestContext<WopiHealthCheck> context) {
		Lock lock = createLock(lockManager, "wopiHealthCheck", deployable.getWopiApp());
		try {
			lock.lock();

			WopiHealthCheckResult result = WopiHealthCheckResult.T.create();
			PersistenceGmSession session = context.getSession();
			WopiHealthCheck request = context.getRequest();
			boolean simple = request.getSimple();
			int numberOfChecks = request.getNumberOfChecks();

			AddDemoDocs addDemoDocs = AddDemoDocs.T.create();
			addDemoDocs.setOnlyMainTypes(false);
			addDemoDocs.eval(session).get();

			List<File> editFiles = new ArrayList<>(Arrays.asList(demoDocsWriteRootFolder.listFiles()));
			List<File> viewFiles = new ArrayList<>(Arrays.asList(demoDocsReadRootFolder.listFiles()));
			viewFiles.addAll(Arrays.asList(demoDocsWriteRootFolder.listFiles()));

			// fetch WopiSessions
			List<WopiSession> wopiSessions = new ArrayList<>();
			for (File editFile : editFiles) {
				String correlationId = resolveDefaultContentCorrelationId(DEMO_CORRELATION_ID_PREFIX, DEMO_CORRELATION_ID_POSTFIX, editFile.getName(),
						DocumentMode.edit);

				WopiSession wopiSession = WopiQueryingUtil.queryOpenExistingWopiSession(session, correlationId);

				wopiSessions.add(wopiSession);
			}
			for (File viewFile : viewFiles) {
				String correlationId = resolveDefaultContentCorrelationId(DEMO_CORRELATION_ID_PREFIX, DEMO_CORRELATION_ID_POSTFIX, viewFile.getName(),
						DocumentMode.view);

				WopiSession wopiSession = WopiQueryingUtil.queryOpenExistingWopiSession(session, correlationId);

				wopiSessions.add(wopiSession);
			}

			wopiSessions = wopiSessions.stream().sorted((o1, o2) -> o1.getCorrelationId().compareTo(o2.getCorrelationId()))
					.collect(Collectors.toList());
			if (simple) {
				wopiSessions.subList(1, wopiSessions.size()).clear();
			}

			for (int i = 0; i < numberOfChecks; i++) {
				// execute Multicast for each WopiSession to be opened
				for (WopiSession wopiSession : wopiSessions) {
					OpenWopiDocument openWopiDocument = OpenWopiDocument.T.create();
					openWopiDocument.setWopiSession(wopiSession);
					openWopiDocument.setSendNotifications(true);

					MulticastRequest mc = MulticastRequest.T.create();
					mc.setTimeout(60_000l);
					mc.setServiceRequest(openWopiDocument);

					EvalContext<? extends MulticastResponse> eval = mc.eval(session);
					MulticastResponse multicastResponse = eval.get();

					Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
					for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
						InstanceId sender = entry.getKey();
						String senderString = getInstanceIdString(sender);
						ServiceResult serviceResult = entry.getValue();
						ResponseEnvelope responseEnvelope = serviceResult.asResponse();

						if (responseEnvelope != null) {
							OpenWopiDocumentResult openWopiDocumentResult = (OpenWopiDocumentResult) responseEnvelope.getResult();
							List<Notification> notifications = openWopiDocumentResult.getNotifications();
							result.getNotifications().addAll(notifications);
						} else {
							throw new IllegalStateException(
									"Could not get any information from sender: '" + senderString + "' serviceResult: '" + serviceResult + "'");
						}
					}
				}
			}

			return result;
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private Resource uploadResource(PersistenceGmSession session, Resource resource, String correlationId, DocumentMode documentMode) {
		//@formatter:off
		Resource uploadedResource = 
					uploadResource(
						session,
						resource.getName(),
						resource.getMd5(),
						resource.getMimeType(),
						resource.getTags(),
						correlationId,
						streamPipeFactory,
						resource,
						null
					);
		//@formatter:on

		validateResourceFileSize(uploadedResource, documentMode, session);

		// first upload the resource and then check the mimetype
		String mimeType = uploadedResource.getMimeType();

		// check for readable and writable documents
		if (!WopiMimeTypes.SUPPORTED_EDIT_MIMETYPES.contains(mimeType)) {
			// check for readable documents
			if (!(documentMode == DocumentMode.view && WopiMimeTypes.SUPPORTED_READ_MIMETYPES.contains(mimeType))) {
				// check for test documents
				if (!(mimeType.equals(WopiMimeTypes.TEST_MIMETYPE)
						&& (uploadedResource.getName().equals("test.wopitest") || uploadedResource.getName().equals(".wopitest")))) {
					// cleanup
					deleteResource(session, uploadedResource, true, logger);
					throw new IllegalStateException("MimeType: '" + mimeType + "' not supported for documentMode: '" + documentMode + "' with name: '"
							+ uploadedResource.getName() + "'. '" + WopiMimeTypes.SUPPORTED_EDIT_MIMETYPES + "' supported for reading and writing, '"
							+ WopiMimeTypes.SUPPORTED_READ_MIMETYPES + "' supported for reading");
				}
			}
		}
		return uploadedResource;
	}

	protected static void writeAndCloseZip(ZipContext zip, OutputStream out, String context) {
		try {
			zip.to(out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while creating zip for: " + context);
		} finally {
			zip.close();
		}
	}

	private void validateResourceFileSize(Resource resource, DocumentMode documentMode, PersistenceGmSession session) {
		if (deployable.getWopiApp().getApplyEarlyFileSizeChecks()) {
			Long fileSize = resource.getFileSize();

			if (fileSize == null) {
				logger.warn(() -> "FileSize of resource: '" + resource + "' not set for validation. Ignore and continue...");
			} else {
				if (WopiMimeTypes.VIEW_MIMETYPES_WORD.contains(resource.getMimeType()) && documentMode == DocumentMode.view) {
					// Word view - 50MB
					if (fileSize > Numbers.MEBIBYTE * 50) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for viewing. Only 5MB are supported for Word documents.");
					}
				} else if (WopiMimeTypes.EDIT_MIMETYPES_WORD.contains(resource.getMimeType()) && documentMode == DocumentMode.edit) {
					// Word edit - 50MB
					if (fileSize > Numbers.MEBIBYTE * 50) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for editing. Only 5MB are supported for Word documents.");
					}
				} else if (WopiMimeTypes.VIEW_MIMETYPES_POWERPOINT.contains(resource.getMimeType()) && documentMode == DocumentMode.view) {
					// PowerPoint view - 300MB
					if (fileSize > Numbers.MEBIBYTE * 300) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for viewing. Only 300MB are supported for PowerPoint documents.");
					}
				} else if (WopiMimeTypes.EDIT_MIMETYPES_POWERPOINT.contains(resource.getMimeType()) && documentMode == DocumentMode.edit) {
					// PowerPoint edit - 300MB
					if (fileSize > Numbers.MEBIBYTE * 300) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for editing. Only 300MB are supported for PowerPoint documents.");
					}
				} else if (WopiMimeTypes.VIEW_MIMETYPES_EXCEL.contains(resource.getMimeType()) && documentMode == DocumentMode.view) {
					// Excel edit/view - 5MB
					if (fileSize > Numbers.MEBIBYTE * 5) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for viewing. Only 5MB are supported for Excel documents.");
					}
				} else if (WopiMimeTypes.EDIT_MIMETYPES_EXCEL.contains(resource.getMimeType()) && documentMode == DocumentMode.edit) {
					// Excel edit/view - 5MB
					if (fileSize > Numbers.MEBIBYTE * 5) {
						deleteResource(session, resource, true, logger);
						throw new IllegalArgumentException(
								"Resource: '" + resource + "' is too big for editing. Only 5MB are supported for Excel documents.");
					}
				} else {
					logger.info(() -> "File size of resource: '" + resource + "' should be validated but no validation found for mimetype: '"
							+ resource.getMimeType() + "'");
				}
			}
		}
	}

	/**
	 * Clone a {@link WopiSession} and includes {@link Resource}s is requested
	 * 
	 * @param existingWopiSession
	 *            existing {@link WopiSession}
	 * @return cloned {@link WopiSession}
	 */
	private WopiSession cloneWopiSession(WopiSession existingWopiSession) {
		WopiSession wopiSession = WopiSession.T.create();

		wopiSession.setAllowedRoles(existingWopiSession.getAllowedRoles());
		wopiSession.setBreadcrumbBrandName(existingWopiSession.getBreadcrumbBrandName());
		wopiSession.setBreadcrumbBrandNameUrl(existingWopiSession.getBreadcrumbBrandNameUrl());
		wopiSession.setBreadcrumbDocName(existingWopiSession.getBreadcrumbDocName());
		wopiSession.setBreadcrumbFolderName(existingWopiSession.getBreadcrumbFolderName());
		wopiSession.setBreadcrumbFolderNameUrl(existingWopiSession.getBreadcrumbFolderNameUrl());
		wopiSession.setContext(existingWopiSession.getContext());
		wopiSession.setCorrelationId(existingWopiSession.getCorrelationId());
		wopiSession.setSourceReference(existingWopiSession.getSourceReference());
		wopiSession.setCreationDate(existingWopiSession.getCreationDate());
		wopiSession.setLastUpdated(existingWopiSession.getLastUpdated());
		wopiSession.setLastUpdatedUser(existingWopiSession.getLastUpdatedUser());
		wopiSession.setLastUpdatedUserId(existingWopiSession.getLastUpdatedUserId());
		wopiSession.setCreator(existingWopiSession.getCreator());
		wopiSession.setCreatorId(existingWopiSession.getCreatorId());
		wopiSession.setDisablePrint(existingWopiSession.getDisablePrint());
		wopiSession.setDisableTranslation(existingWopiSession.getDisableTranslation());
		wopiSession.setDocumentMode(existingWopiSession.getDocumentMode());
		wopiSession.setMaxVersions(existingWopiSession.getMaxVersions());
		wopiSession.setVersion(existingWopiSession.getVersion());
		wopiSession.setShowBreadcrumbBrandName(existingWopiSession.getShowBreadcrumbBrandName());
		wopiSession.setShowBreadcrumbDocName(existingWopiSession.getShowBreadcrumbDocName());
		wopiSession.setShowBreadcrumbFolderName(existingWopiSession.getShowBreadcrumbFolderName());
		wopiSession.setShowUserFriendlyName(existingWopiSession.getShowUserFriendlyName());
		wopiSession.setStatus(existingWopiSession.getStatus());
		wopiSession.setTags(existingWopiSession.getTags());
		wopiSession.setUpdateNotificationMessage(existingWopiSession.getUpdateNotificationMessage());
		wopiSession.setUpdateNotificationAccessId(existingWopiSession.getUpdateNotificationAccessId());
		wopiSession.setTenant(existingWopiSession.getTenant());
		wopiSession.setWopiLockExpirationInMs(existingWopiSession.getWopiLockExpirationInMs());
		wopiSession.setWopiUrl(existingWopiSession.getWopiUrl());

		List<WopiAccessToken> accessTokens = existingWopiSession.getAccessTokens();
		if (accessTokens != null) {
			for (WopiAccessToken accessToken : accessTokens) {
				WopiAccessToken at = WopiAccessToken.T.create();
				at.setToken(accessToken.getToken());
				at.setUser(accessToken.getUser());
				wopiSession.getAccessTokens().add(at);
			}
		}

		WopiLock existingWopiLock = existingWopiSession.getLock();
		if (existingWopiLock != null) {
			WopiLock lock = WopiLock.T.create();
			lock.setCreationDate(existingWopiLock.getCreationDate());
			lock.setLock(existingWopiLock.getLock());
			wopiSession.setLock(lock);
		}

		return wopiSession;
	}

	private void validateAllowedRolesOnCreation(Set<String> allowedRoles) {
		PersistenceGmSession authSession = localSessionFactory.newSession("auth");
		List<Role> roles = WopiQueryingUtil.queryRoles(authSession);

		Set<String> roleNames = roles.stream().map(role -> role.getName()).collect(Collectors.toSet());
		roleNames.add("$all");//
		// TODO: add other general roles
		for (String allowedRole : allowedRoles) {
			if (!roleNames.contains(allowedRole)) {
				logger.warn(() -> "Attached allowed role: '" + allowedRole + "' which does not exists.");
			}
		}
	}

	/**
	 * Prepare the streaming URL of a resource
	 * 
	 * @param request
	 *            {@link WopiRequest}
	 * @param resource
	 *            {@link Resource} to be streamed
	 * @return URL as a {@link String}
	 */
	private String prepareStreamingUrl(WopiRequest request, Resource resource) {
		String sessionId = request.getSessionId();
		String accessId = request.getDomainId();

		String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();

		String url = publicServicesUrl + "/streaming?accessId=" + accessId + "&sessionId=" + sessionId + "&resourceId=" + resource.getId();
		return url;
	}

	private List<WopiSession> addDemoDocs(DocumentMode documentMode, List<File> files, PersistenceGmSession session) {
		List<WopiSession> wopiSessions = new ArrayList<>();
		for (File file : files) {
			String name = file.getName();

			Resource resource;
			try (InputStream is = new FileInputStream(file)) {
				resource = Resource.T.create();
				resource.setName(name);
				resource.setTags(asSet(DEMO_DOC_TAG));
				resource.assignTransientSource(FileStreamProviders.from(is));

				String correlationId = resolveDefaultContentCorrelationId(DEMO_CORRELATION_ID_PREFIX, DEMO_CORRELATION_ID_POSTFIX, file.getName(),
						documentMode);
				OpenWopiSession openWopiSession = OpenWopiSession.T.create();
				openWopiSession.setCorrelationId(correlationId);
				openWopiSession.setDocumentMode(documentMode);
				openWopiSession.setResource(resource);
				openWopiSession.setTenant(DEMO_DOC_TAG);
				openWopiSession.setTags(asSet(DEMO_DOC_TAG));
				openWopiSession.setUpdateNotificationMessage("Update from Office Online on '" + correlationId + "'");
				OpenWopiSessionResult openWopiSessionResult = openWopiSession.eval(session).get();
				wopiSessions.add(openWopiSessionResult.getWopiSession());

				logger.debug(() -> "Added demo file: '" + name + "' correlationId: '" + correlationId + "' documentMode: '" + documentMode + "'");
			} catch (Exception e) {
				throw new GenericRuntimeException("Could not find file: '" + file + "' for creating demo documents", e);
			}
		}
		return wopiSessions;
	}

	private void removeDemoDocs(PersistenceGmSession session, List<File> editFiles, DocumentMode documentMode) {
		for (File file : editFiles) {
			RemoveWopiSession removeWopiSession = RemoveWopiSession.T.create();
			WopiSession wopiSession = WopiSession.T.create();
			wopiSession.setCorrelationId(
					resolveDefaultContentCorrelationId(DEMO_CORRELATION_ID_PREFIX, DEMO_CORRELATION_ID_POSTFIX, file.getName(), documentMode));
			removeWopiSession.setWopiSession(wopiSession);
			removeWopiSession.eval(session).get();
		}
	}

	private String createPublicWopiUrl(PersistenceGmSession session, String userSessionId, String wopiUrl) {
		String sessionId;
		if (CommonTools.isEmpty(userSessionId)) {
			sessionId = session.getSessionAuthorization().getSessionId();
		} else {
			sessionId = userSessionId;
		}

		String publicServicesUrl = WopiConnectorUtil.getPublicServicesUrl(deployable.getWopiApp().getWopiWacConnector().getCustomPublicServicesUrl());
		String publicWopiUrl = publicServicesUrl + wopiUrl + "&sessionId=" + sessionId;
		return publicWopiUrl;
	}

	private <T, U> T resolveSetting(T deployableSetting, U requestSetting) {
		if (requestSetting != null) {
			return (T) requestSetting;
		} else {
			return deployableSetting;
		}
	}

	private String resolveUnmodifiable(String requestSetting) {
		if (requestSetting == null) {
			return "";
		} else {
			return requestSetting;
		}
	}

	private static String getInstanceIdString(InstanceId instanceId) {
		if (instanceId == null) {
			return "unknown";
		}
		return instanceId.stringify();
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
	public void setLocalSystemSessionFactory(PersistenceGmSessionFactory localSystemSessionFactory) {
		this.localSystemSessionFactory = localSystemSessionFactory;
	}

	@Configurable
	@Required
	public void setDemoDocsWriteRootFolder(File demoDocsWriteRootFolder) {
		this.demoDocsWriteRootFolder = demoDocsWriteRootFolder;
	}

	@Configurable
	@Required
	public void setDemoDocsReadRootFolder(File demoDocsReadRootFolder) {
		this.demoDocsReadRootFolder = demoDocsReadRootFolder;
	}

	@Configurable
	@Required
	public void setTestDocsRootFolder(File testDocsRootFolder) {
		this.testDocsRootFolder = testDocsRootFolder;
	}

	@Configurable
	@Required
	public void setDeployable(com.braintribe.model.wopi.service.WopiServiceProcessor deployable) {
		this.deployable = deployable;
	}

	@Configurable
	@Required
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Configurable
	@Required
	public void setTestDocCommand(String testDocCommand) {
		this.testDocCommand = testDocCommand;
	}

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Required
	@Configurable
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
}
