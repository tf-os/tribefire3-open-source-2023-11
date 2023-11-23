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
package tribefire.extension.hydrux.servlet;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static java.util.Collections.emptySet;
import static tribefire.extension.hydrux.model.deployment.prototyping.HxMainView.PROTOTYPING_DOMAIN_ID;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.template.Template;

import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.processor.HxRequestProcessor;

/**
 * @author peter.gazdik
 */
public class HydruxServlet extends HttpServlet {

	private static final long serialVersionUID = -2977779991163691729L;

	private String servicesUrl;
	private String webSocketUrl;

	private Template hydruxAppHtmlTemplate;

	private File platformHtmlTemplate;

	private static final String HX_RESOURCE = "hydrux-resource";
	private static final String HX_DEBUG_JS = "debug.js";

	private static final String HX_PROTOTYPING_PARAM = "hxproto";

	private static final String HX_DEBUG_PARAM = "hxdebug";
	private static final String HX_FRESH_TEMPLATE_PARAM = "hxfresh";

	private static final String tfJsVersion = "3.0";

	private ModelAccessoryFactory modelAccessoryFactory;

	@Required
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}

	public void setWebSocketUrl(String webSocketUrl) {
		this.webSocketUrl = webSocketUrl;
	}

	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Required
	public void setHydruxPlatformHtmlTemplate(File platformHtmlTemplate) {
		// TODO error handling (just in case no file exists)
		this.platformHtmlTemplate = platformHtmlTemplate;
		String templateText = FileTools.read(platformHtmlTemplate).asString();
		this.hydruxAppHtmlTemplate = Template.parse(templateText);
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		new HydruxServletRequestHandler(req, res).handle();
	}

	private class HydruxServletRequestHandler {

		private final HttpServletRequest req;
		private final HttpServletResponse res;

		private DomainAndUsecases settings;

		public HydruxServletRequestHandler(HttpServletRequest req, HttpServletResponse res) {
			this.req = req;
			this.res = res;
		}

		public void handle() throws IOException {
			// System.out.println(req.getHeader("Last-Modified"));

			String pathInfo = req.getPathInfo();
			if (pathInfo == null || pathInfo.length() == 1 /* i.e. just '/' */) {
				String maybeSlash = pathInfo == null ? "/" : "";
				set404("DomainId was not specified in the URL. Expecting something like: " + req.getRequestURI() + maybeSlash + "${domainId}");
				return;
			}

			settings = resolveDomainAndUsecases(pathInfo);

			if (HX_RESOURCE.equals(settings.domainId)) {
				if (renderHxResource())
					return;

				set404("Cannot resolve Hydrux resource for URL path: " + pathInfo);
				return;
			}

			Maybe<HxApplication> hxApplication = resolveHxApplication();
			if (hxApplication.isUnsatisfied()) {
				set404("HxApplication cannot be resolved. Reason: " + hxApplication.whyUnsatisfied().getText());
				return;
			}

			Maybe<String> protoModule = resolvePrototypingModule();
			if (protoModule.isUnsatisfied()) {
				set404("HxApplication cannot be resolved. Reason: " + protoModule.whyUnsatisfied().getText());
				return;
			}

			String text = renderHtml(hxApplication.get(), protoModule.get());

			res.setContentType("text/html;charset=UTF-8");
			res.getWriter().append(text);
			res.flushBuffer();
		}

		private DomainAndUsecases resolveDomainAndUsecases(String pathInfo) {
			pathInfo = pathInfo.substring(1);
			String[] split = pathInfo.split("/");

			DomainAndUsecases result = new DomainAndUsecases();
			result.domainId = split[0];

			if (split.length > 1)
				result.usecases = split[1].split("\\+");

			return result;
		}

		private boolean renderHxResource() throws IOException {
			if (settings.usecases.length != 1)
				return false;

			String resourceName = settings.usecases[0];

			String contentType = contentTypeFor(resourceName);
			if (contentType == null)
				return false;

			File resourceFile = new File(platformHtmlTemplate.getParent(), resourceName);

			res.setContentType(contentType);
			FileTools.read(resourceFile).toWriter(res.getWriter());
			res.flushBuffer();

			return true;
		}

		private String contentTypeFor(String resourceName) {
			switch (resourceName) {
				case HX_DEBUG_JS:
					return "application/javascript";
				default:
					return null;
			}
		}

		private Maybe<HxApplication> resolveHxApplication() {
			Set<String> useCases = settings.usecases == null ? emptySet() : asSet(settings.usecases);
			return HxRequestProcessor.resolveHxApplication(modelAccessoryFactory, settings.domainId, useCases);
		}

		private void set404(String msg) throws IOException {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			res.getWriter().append("<h2>404</h2>" + msg);
			res.setContentType("text/html;charset=UTF-8");
		}

		private String renderHtml(HxApplication hxApplication, String protoModule) {
			TfUxHostSettingsBuilder settingsBuilder = new TfUxHostSettingsBuilder();
			settingsBuilder.add("servicesUrl", servicesUrl);
			settingsBuilder.add("webSocketUrl", webSocketUrl);
			settingsBuilder.add("domainId", settings.domainId);
			settingsBuilder.add("usecases", settings.usecases);
			settingsBuilder.add("prototypingModule", protoModule);
			settingsBuilder.add("queryString", req.getQueryString());

			String debugScript = req.getParameter(HX_DEBUG_PARAM) == null ? ""
					: "<script src=\"" + servicesUrl + "/hydrux/" + HX_RESOURCE + "/" + HX_DEBUG_JS + "\"></script>";

			String text = hydruxAppHtmlTemplate().merge(asMap( //
					"HYDRUX_TITLE", hxApplication.getTitle(), //
					"TF_UX_HOST_SETTINGS", settingsBuilder.build(), //
					"SERVICES_URL", servicesUrl, //
					"TRIBEFIRE_JS_ARTIFACT", "tribefire.js.tf-js-" + tfJsVersion + "~", //
					"HYDRUX_PLATFORM_ARTIFACT", "tribefire.extension.hydrux.hydrux-platform-2.1~", //
					"DEBUG_SCRIPT", debugScript));
			return text;
		}

		private Maybe<String> resolvePrototypingModule() {
			String prototypingModule = req.getParameter(HX_PROTOTYPING_PARAM);

			if (PROTOTYPING_DOMAIN_ID.equals(settings.domainId)) {
				if (prototypingModule != null)
					return Maybe.complete(prototypingModule);
				else
					return InvalidArgument.create("Prototyping module not specified. " + prototypingParamExplanation()).asMaybe();
			}

			if (prototypingModule == null)
				return Maybe.complete(prototypingModule);
			else
				return InvalidArgument.create("Parameter [" + HX_PROTOTYPING_PARAM + "] is only valid for the prototyping domain ["
						+ PROTOTYPING_DOMAIN_ID + "], but you have specified the domain: " + settings.domainId).asMaybe();
		}

		private Template hydruxAppHtmlTemplate() {
			if (req.getParameter(HX_FRESH_TEMPLATE_PARAM) == null)
				return hydruxAppHtmlTemplate;
			else
				return Template.parse(FileTools.read(platformHtmlTemplate).asString());
		}
	}

	private static class DomainAndUsecases {
		public String domainId;
		public String[] usecases;
	}

	private static class TfUxHostSettingsBuilder {
		private final StringJoiner sj = new StringJoiner(",");

		public void add(String name, String value) {
			if (value != null)
				sj.add(name + ": \"" + value + "\"");
		}

		public void add(String name, String[] values) {
			if (values == null)
				return;

			StringJoiner vsj = new StringJoiner(", ", "[", "]");
			for (String value : values)
				vsj.add("\"" + value + "\"");

			sj.add(name + ": " + vsj.toString());
		}

		public String build() {
			return sj.toString();
		}
	}

	public static String prototypingParamExplanation() {
		return "The value is set as a URL parameter (" + HydruxServlet.HX_PROTOTYPING_PARAM
				+ "=my-hx-module) and is mandatory for the prototyping domain (" + PROTOTYPING_DOMAIN_ID
				+ "), whose purpose is to allow testing client applications with no configuration on the server.";
	}

}
