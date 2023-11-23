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
package com.braintribe.model.processing.wopi.app;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.SessionNotFoundException;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.wopi.WopiConnectorUtil;
import com.braintribe.model.processing.wopi.WopiWacConnector;
import com.braintribe.model.processing.wopi.misc.HttpResponse;
import com.braintribe.model.processing.wopi.misc.HttpResponseMessage;
import com.braintribe.model.processing.wopi.model.AccessToken;
import com.braintribe.model.processing.wopi.model.CheckFileInfo;
import com.braintribe.model.processing.wopi.service.WopiProcessing;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.web.servlet.auth.providers.RequestOrCookieSessionIdProvider;

import tribefire.extension.wopi.WopiMimeTypes;

/**
 * 
 * Expert for WopiApp
 */
//@formatter:off
//  ------------------       --------------------
// |                  | ---> | WopiServlet (TF) | (e.g. request get file (e.g. .doc))
// | IIS / MS Web App |      --------------------
// |                  |      --------------------
// |                  | <----| WopiServlet (TF) | (e.g. get file (e.g. .doc))
//  ------------------       --------------------
//
//@formatter:on
public class WopiApp extends HttpServlet implements LifecycleAware {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(WopiApp.class);

	private com.braintribe.model.wopi.service.WopiApp deployable;
	private WopiWacConnector wopiWacConnector;
	private Map<String, WopiAction> delegates = new HashMap<String, WopiAction>();

	private RequestOrCookieSessionIdProvider sessionIdProvider = new RequestOrCookieSessionIdProvider();

	private Evaluator<ServiceRequest> requestEvaluator;

	private WopiProcessing wopiProcessing;

	// -----------------------------------------------------------------------
	// CONSTRUCTOR
	// -----------------------------------------------------------------------

	public WopiApp() {
		super();
		addDelegate(HttpGet.METHOD_NAME, null, new GetWopiIndex());
		// TODO: is the get still necessary? From the example we do a POST - which should be the way to integrate
		addDelegate(HttpGet.METHOD_NAME, WopiEvent.RESOURCE, new ViewDocument());
		addDelegate(HttpPost.METHOD_NAME, WopiEvent.RESOURCE, new ViewDocument());
		addDelegate(HttpGet.METHOD_NAME, WopiEvent.FILES, new GetFileInfo());
		addDelegate(HttpPost.METHOD_NAME, WopiEvent.FILES, new FileOperations());
		addDelegate(HttpGet.METHOD_NAME, WopiEvent.CONTENTS, new GetFileContents());
		addDelegate(HttpPost.METHOD_NAME, WopiEvent.CONTENTS, new PutFileContents());
	}

	private void addDelegate(String method, WopiEvent event, WopiAction delegate) {
		delegates.put(toString(method, event), delegate);
	}

	private String toString(String method, WopiEvent event) {
		StringBuilder sb = new StringBuilder(method);
		if (event != null) {
			sb.append('#').append(event.name());
		}
		return sb.toString();
	}

	// -----------------------------------------------------------------------
	// LIFECYCLEAWARE
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		logger.info(() -> "Starting: '" + WopiApp.class.getSimpleName() + "'...");
	}

	@Override
	public void preDestroy() {
		logger.info(() -> "Stopping: '" + WopiApp.class.getSimpleName() + "'...");
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		StopWatch stopWatch = new StopWatch();

		try {
			WopiRequest wopiRequest = new WopiRequest(request);

			wopiRequest.log();

			String method = request.getMethod();
			WopiEvent event = wopiRequest.getEvent();

			// get specified event {@link WopiEvent}
			String key = toString(method, event);
			WopiAction action = delegates.get(key);

			if (action == null) {
				logger.warn(() -> "No action registered for: " + key);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No action registered for: " + key);
			} else {
				String wopiActionInfo = action.wopiActionInfo();

				logger.debug(() -> "Request: " + key + " (" + wopiRequest.getEvent() + ") -- " + wopiActionInfo);
				try (HttpResponse result = action.service(wopiRequest)) {
					logger.debug(() -> "Returning HTTP response: '" + result + "'");
					if (result != null) {
						result.write(response);
					} else {
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				} catch (NotFoundException e) {
					WopiHttpStatusMessage.returnFileUnknown(e.getMessage()).write(response);
				} catch (GmSessionException e) {
					WopiHttpStatusMessage.returnServerError(e).write(response);
				} catch (Exception e) {
					WopiHttpStatusMessage.returnServerError(e).write(response);
				}
			}

		} finally {
			long elapsedTime = stopWatch.getElapsedTime();

			LogLevel logLevel = LogLevel.TRACE;
			if (elapsedTime > deployable.getLogWarningThresholdInMs()) {
				logLevel = LogLevel.WARN;
			} else if (elapsedTime > deployable.getLogErrorThresholdInMs()) {
				logLevel = LogLevel.ERROR;
			}
			if (logger.isLevelEnabled(logLevel)) {
				logger.log(logLevel, "Executed request: '" + request + "' in '" + elapsedTime + "'ms");
			}
		}
	}

	// -----------------------------------------------------------------------
	// WOPPI PROTOCOL IMPLEMENTATION
	// -----------------------------------------------------------------------

	private URL getRequestURL() throws MalformedURLException {
		String requestURI = "/component/" + deployable.getPathIdentifier();
		String publicServicesUrl = WopiConnectorUtil.getPublicServicesUrl(deployable.getWopiWacConnector().getCustomPublicServicesUrl());
		URL url = new URL(publicServicesUrl + requestURI);
		logger.debug(() -> "Prepared RequestURL: '" + url + "'");
		return url;
	}

	/**
	 * Get remote {@link Resource} from integrated system
	 * 
	 * @param request
	 *            {@link WopiRequest} of request
	 * @return {@link Resource} from integrated system
	 */
	private WopiSession retrieveWopiSession(WopiRequest request) {

		WopiSession wopiSession = wopiProcessing.getWopiResourceRequest(request.getFileId(), deployable.getAccess());
		Resource resource = wopiSession.getCurrentResource();

		logger.debug(() -> "Retrieved resource with id: '" + resource.getId() + "' name: '" + resource.getName() + "' fileSize: '"
				+ resource.getFileSize() + "' - complete resource in trace level");
		logger.trace(() -> "Retrieved resource: '" + resource + "'");
		return wopiSession;
	}

	// -----------------------<WOPI actions as classes >-----------------------

	protected class GetWopiIndex implements WopiAction {

		@Override
		public HttpResponse service(WopiRequest request) throws IOException, GmSessionException {
			try (PipedOutputStream out = new PipedOutputStream(); PrintStream print = new PrintStream(out)) {

				logger.debug(() -> "Retrieved '" + GetWopiIndex.class.getSimpleName() + "' - return dummy output");

				HttpResponseMessage msg = WopiHttpStatusMessage.returnSuccess();
				msg.setContentType(ContentType.TEXT_HTML.toString());
				msg.setContentStream(new PipedInputStream(out, 0x2000));

				print.println("tribefire WOPI interface");
				print.flush();
				out.flush();
				return msg;
			}

		}
	}

	protected class ViewDocument implements WopiAction {

		@Override
		public HttpResponse service(final WopiRequest request) throws GmSessionException, IOException {
			UserSession userSession = validateUserSession(request);
			if (userSession == null) {
				throw new IllegalArgumentException("No sessionId available during preparing for viewing document");
			}
			String sessionId = userSession.getSessionId();

			if (sessionId == null) {
				throw new IllegalArgumentException("No sessionId available during preparing for viewing document");
			}
			String correlationId = request.getParameter("correlationId");
			String mimetype = request.getParameter("mimetype");
			String name = request.getParameter("name");
			String action = request.getParameter("action");
			if (StringUtils.isNotEmpty(sessionId) && StringUtils.isNotEmpty(correlationId) && StringUtils.isNotEmpty(mimetype)
					&& StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(action)) {

				AccessToken _accessToken = new AccessToken();
				_accessToken.setSessionId(sessionId);
				_accessToken.setUserName(userSession.getUser().getName());
				String accessToken = _accessToken.encode();
				// URL reqUrl = getRequestURL(request);
				URL reqUrl = getRequestURL();

				String accessTokenTtl = String.valueOf(System.currentTimeMillis() + deployable.getAccessTokenTtlInSec() * 1000);
				URL docLink = wopiWacConnector.wopiWacClient().getDocumentLink(action, reqUrl, correlationId, mimetype, name, accessToken,
						accessTokenTtl);
				HttpResponseMessage msg = new HttpResponseMessage(HttpServletResponse.SC_FOUND);
				msg.addHeader("Location", docLink.toString());

				User user = userSession.getUser();
				String userName = user.getName();
				// save accessToken for later comparison
				wopiProcessing.retrieveAccessTokenRequest(correlationId, accessToken, deployable, userName);

				logger.debug(() -> "Redirecting to: '" + docLink + "'");

				return msg;
			} else {
				logger.warn(() -> "Action '" + ViewDocument.class.getSimpleName() + "' - validation failed! sessionId: '" + sessionId
						+ "' correlationId: '" + correlationId + "' mimetype: '" + mimetype + "' name: '" + name + "' action: '" + action + "'");
				return WopiHttpStatusMessage.returnUnsupported("<correlationId>, <mimetype>, <name> and <action> required");
			}
		}
	}

	/**
	 * The id represents the file that the WOPI server is providing information about. The token represents the
	 * credentials that the WOPI server MUST use when determining the permissions the WOPI client has when interacting
	 * with the file.
	 */
	protected class GetFileInfo implements WopiAction {
		@Override
		public HttpResponse service(WopiRequest request) throws GmSessionException, IOException {

			String accessToken = request.getAccessToken();
			AccessToken decode = AccessToken.decode(accessToken);
			if (decode == null) {
				// received invalid access token
				return WopiHttpStatusMessage.returnInvalidToken("AccessToken '" + accessToken + "' is invalid");
			}
			String sessionIdFromRequest = decode.getSessionId();

			UserSession userSession = validatedUserSession(sessionIdFromRequest);

			if (userSession == null) {
				throw new IllegalStateException("No sessionId available during retrieving actual file. Tried to use sessionId: '"
						+ sessionIdFromRequest + "' which is not valid any more.");
			}

			String correlationId = request.getFileId();

			WopiSession wopiSession = wopiProcessing.validateAccessTokenRequest(accessToken, correlationId, deployable);

			Resource resource = wopiSession.getCurrentResource();

			CheckFileInfo cfi = new CheckFileInfo();

			cfi.OwnerId = deployable.getExternalId();
			cfi.Size = resource.getFileSize().intValue();
			cfi.Version = Long.toString(wopiSession.getVersion());

			cfi.SupportsExtendedLockLength = true;

			cfi.UserId = userSession.getUser().getId();

			// add extension if filename does not have an extension
			String fileExtension = resolveFileExtension(resource);
			String fileName = resource.getName();
			if (!fileName.endsWith(fileExtension)) {
				fileName = fileName + fileExtension;
			}
			cfi.BaseFileName = fileName;
			cfi.FileExtension = fileExtension;

			cfi.SupportsLocks = true;
			cfi.SupportsUpdate = true;
			cfi.UserCanNotWriteRelative = true;
			if (wopiSession.getDocumentMode() == DocumentMode.edit) {
				cfi.ReadOnly = false;
				cfi.UserCanWrite = true;
			} else {
				cfi.ReadOnly = true;
				cfi.UserCanWrite = false;
			}

			cfi.RestrictedWebViewOnly = true;

			// UI Customization
			if (wopiSession.getShowUserFriendlyName()) {
				cfi.UserFriendlyName = userSession.getUser().getName();
			}
			if (wopiSession.getShowBreadcrumbBrandName()) {
				String breadcrumbBrandName = wopiSession.getBreadcrumbBrandName();
				if (!CommonTools.isEmpty(breadcrumbBrandName)) {
					cfi.BreadcrumbBrandName = breadcrumbBrandName;
				}
			}
			String breadcrumbBrandNameUrl = wopiSession.getBreadcrumbBrandNameUrl();
			if (!CommonTools.isEmpty(breadcrumbBrandNameUrl)) {
				cfi.BreadcrumbBrandUrl = breadcrumbBrandNameUrl;
			}
			if (wopiSession.getShowBreadcrumbDocName()) {
				String breadcrumbDocName = wopiSession.getBreadcrumbDocName();
				if (CommonTools.isEmpty(breadcrumbDocName)) {
					cfi.BreadcrumbDocName = fileName;
				} else {
					cfi.BreadcrumbDocName = breadcrumbDocName;
				}
			}
			if (wopiSession.getShowBreadcrumbFolderName()) {
				String breadcrumbFolderName = wopiSession.getBreadcrumbFolderName();
				if (!CommonTools.isEmpty(breadcrumbFolderName)) {
					cfi.BreadcrumbFolderName = breadcrumbFolderName;
				}
			}
			String breadcrumbFolderNameUrl = wopiSession.getBreadcrumbFolderNameUrl();
			if (!CommonTools.isEmpty(breadcrumbFolderNameUrl)) {
				cfi.BreadcrumbFolderUrl = breadcrumbFolderNameUrl;
			}
			cfi.DisablePrint = wopiSession.getDisablePrint();
			cfi.DisableTranslation = wopiSession.getDisableTranslation();

			cfi.SupportsGetLock = true;
			cfi.SupportsUserInfo = false;
			cfi.SupportsDeleteFile = false;
			cfi.SupportsContainers = false;
			cfi.SupportedShareUrlTypes = new String[0];

			logger.debug(() -> "Applying GetFileInfo: '" + cfi + "' on correlationId: '" + correlationId + "'");
			return cfi;
		}
		// }

		private String resolveFileExtension(Resource resource) {
			String fileExtension = "." + WopiMimeTypes.mimeTypeExtensionMap.get(resource.getMimeType());
			return fileExtension;
		}

	}

	protected class GetFileContents implements WopiAction {
		@Override
		public HttpResponse service(WopiRequest request) throws GmSessionException, IOException {
			WopiSession wopiSession = retrieveWopiSession(request);
			Resource resource = wopiSession.getCurrentResource();

			String accessToken = request.getAccessToken();
			AccessToken decode = AccessToken.decode(accessToken);
			if (decode == null) {
				// received invalid access token
				return WopiHttpStatusMessage.returnInvalidToken("AccessToken '" + accessToken + "' is invalid");
			}

			HttpResponseMessage response = WopiHttpStatusMessage.returnSuccess();
			response.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
			response.setContentLength(resource.getFileSize().intValue());
			response.addHeader(WopiHeader.ItemVersion.key(), Long.toString(wopiSession.getVersion()));

			response.setContentStream(resource.openStream());

			logger.debug(() -> "GetFileContents for resource with id: '" + resource.getId() + "' name: '" + resource.getName() + "' fileSize: '"
					+ resource.getFileSize() + "' - full resource information on trace");
			logger.trace(() -> "GetFileContents for resource: '" + resource + "'");
			return response;
		}
		// }
	}

	protected class PutFileContents implements WopiAction {
		@Override
		public HttpResponse service(WopiRequest request) throws GmSessionException, IOException {
			String accessToken = request.getAccessToken();
			AccessToken decode = AccessToken.decode(accessToken);
			if (decode == null) {
				// received invalid access token
				return WopiHttpStatusMessage.returnInvalidToken("AccessToken '" + accessToken + "' is invalid");
			}
			String sessionIdFromRequest = decode.getSessionId();

			UserSession userSession = validatedUserSession(sessionIdFromRequest);
			if (userSession == null) {
				throw new IllegalStateException("No sessionId available during retrievingactual file");
			}

			switch (request.getWopiOverride()) {
				case PUT:

					HttpResponseMessage httpResponseMessage = wopiProcessing.putWopiResourceRequest(request, deployable, accessToken, userSession);

					return httpResponseMessage;
				default:
					logger.warn(() -> "PutFileContents:  " + request.getWopiOverride() + " not implemented (yet)");
					return WopiHttpStatusMessage.returnUnsupported(String.valueOf(request.getWopiOverride()));
			}
		}
	}

	protected class FileOperations implements WopiAction {
		@Override
		public HttpResponse service(WopiRequest request) throws GmSessionException {
			String accessToken = request.getAccessToken();
			AccessToken decode = AccessToken.decode(accessToken);
			if (decode == null) {
				// received invalid access token
				return WopiHttpStatusMessage.returnInvalidToken("AccessToken '" + accessToken + "' is invalid");
			}

			WopiOperation wopiOperation = request.getWopiOverride();

			String oldLock = request.getWopiOldLock();

			switch (wopiOperation) {

				case LOCK:
					if (oldLock != null) {
						return wopiProcessing.unlockAndRelock(request, deployable);
					} else {
						return wopiProcessing.lock(request, deployable);
					}
				case REFRESH_LOCK:
					return wopiProcessing.refreshLock(request, deployable);
				case UNLOCK:
					return wopiProcessing.unlock(request, deployable);
				case GET_LOCK:
					return wopiProcessing.getLock(request, deployable);
				default:
					logger.warn(() -> "FileOperations: " + request.getWopiOverride() + " not implemented (yet)");
					return WopiHttpStatusMessage.returnUnsupported(String.valueOf(request.getWopiOverride()));

			}
		}
	} // FileOperations

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private UserSession validateUserSession(WopiRequest wopiRequest) {
		String sessionId = sessionIdProvider.apply(wopiRequest);
		if (StringTools.isBlank(sessionId)) {
			return null;
		}

		UserSession validatedUserSession = validatedUserSession(sessionId);
		if (validatedUserSession == null) {
			return null;
		}
		return validatedUserSession;
	}

	private UserSession validatedUserSession(String sessionId) {
		try {
			ValidateUserSession validateSessionRequest = ValidateUserSession.T.create();
			validateSessionRequest.setSessionId(sessionId);
			EvalContext<UserSession> evalValidateUserSession = requestEvaluator.eval(validateSessionRequest);
			UserSession validatedUserSession = evalValidateUserSession.get();
			return validatedUserSession;
		} catch (SessionNotFoundException e) {
			logger.trace(() -> "Could not find UserSession for sessionId: '" + sessionId + "'");
			return null;
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Required
	@Configurable
	public void setDeployable(com.braintribe.model.wopi.service.WopiApp deployable) {
		this.deployable = deployable;
	}

	@Required
	@Configurable
	public void setWopiWacConnector(WopiWacConnector wopiWacConnector) {
		this.wopiWacConnector = wopiWacConnector;
	}

	@Configurable
	@Required
	public void setWopiProcessing(WopiProcessing wopiProcessing) {
		this.wopiProcessing = wopiProcessing;
	}

}
