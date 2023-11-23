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
package com.braintribe.model.openapi.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;

public class OpenapiUiServlet extends BasicTemplateBasedServlet {

	public static final String RUNTIME_HIDE_SWAGGER20_UI = "TRIBEFIRE_HIDE_SWAGGER20_UI";
	private static final long serialVersionUID = -3597570947500170486L;
	private static final Logger logger = Logger.getLogger(OpenapiUiServlet.class);
	
	private String servicesUrl;
	private Function<HttpServletRequest, String> swaggerfileUrlFactory = this::swaggerfileUrlFactory;
	private Supplier<PersistenceGmSession> cortexSessionFactory;
	private ModelAccessoryFactory modelAccessoryFactory;

	private boolean wasInitialized;

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
		// Uncomment the following lines during development:
		// templatesModified = true;
		// refreshFileBasedTemplates = true;

		VelocityContext context = new VelocityContext();
		ensureServicesUrl();

		String currentSwaggerfileUrl = servicesUrl + swaggerfileUrlFactory.apply(request);

		List<HeaderLink> headerLinks = createHeaderLinks(request);

		context.put("swaggerfileUrl", currentSwaggerfileUrl);
		context.put("servicesUrl", servicesUrl); // needed for display resources like css
		context.put("headerLinks", headerLinks);

		return context;
	}

	private void ensureServicesUrl() {
		if (servicesUrl == null) {
			servicesUrl = "../../.."; // relative tfs path: e.g. tfs/openapi/ui/cortex/services -> tfs
		}
	}

	private List<HeaderLink> createHeaderLinks(HttpServletRequest request) {
		String[] splitPathInfo = request.getPathInfo().split("/");
		int pathElementCount = splitPathInfo.length - 1;

		if (pathElementCount != 2) {
			throw new IllegalArgumentException(
					"The url path should end with exactly 2 elements describing mode and domain of the model that should be exported but found only "
							+ pathElementCount);
		}

		String delegate = splitPathInfo[1];
		String domainId = splitPathInfo[2];

		EntityQuery domainQuery = EntityQueryBuilder.from(ServiceDomain.T).where().property(ServiceDomain.externalId).eq(domainId).done();
		PersistenceGmSession session = cortexSessionFactory.get();

		ServiceDomain serviceDomain = session.query().entities(domainQuery).unique();

		if (serviceDomain == null) {
			throw new IllegalArgumentException("'" + domainId + "' is no valid access or service domain.");
		}

		String[] useCases = request.getParameterValues("useCases");
		String queryParameter = useCases == null ? "" : "?useCases=" + String.join("&useCases=", useCases);

		boolean shouldReflectServices = delegate.equals("services");

		List<HeaderLink> headerLinks = new ArrayList<>();

		if (serviceDomain instanceof IncrementalAccess) {
			ModelAccessory accessory = modelAccessoryFactory.getForAccess(domainId);
			if (modelIsVisible(accessory, useCases)) {
				headerLinks.add(headerLink("CRUD Entities", "entities", delegate, domainId, queryParameter));
				headerLinks.add(headerLink("CRUD Properties", "properties", delegate, domainId, queryParameter));
			} else if (!shouldReflectServices) {
				logger.debug("No permission to reflect meta model for access '" + serviceDomain.getExternalId() +"'.");
			}
		}

		if (serviceDomain.getServiceModel() != null) {
			if (modelIsVisible(modelAccessoryFactory.getForServiceDomain(domainId), useCases)) {
				headerLinks.add(headerLink("Service Requests", "services", delegate, domainId, queryParameter));
			} else if (shouldReflectServices) {
				logger.debug("No permission to reflect service model for domain '" +serviceDomain.getExternalId() + "'. ");
			}
		}

		String hideSwagger20 = TribefireRuntime.getProperty(RUNTIME_HIDE_SWAGGER20_UI);
		if (!"true".equalsIgnoreCase(hideSwagger20)) {
			if (shouldReflectServices) {
				headerLinks.add(new HeaderLink("Swagger 2.0", servicesUrl + "/api/v1/" + domainId, false, true));
			} else {
				headerLinks.add(new HeaderLink("Swagger 2.0", servicesUrl + "/rest/v2/" + delegate + "/" + domainId, false, true));
			}
		}
		return headerLinks;
	}

	private static boolean modelIsVisible(ModelAccessory modelAccessory, String[] useCases) {
		ModelMdResolver modelMdResolver = modelAccessory.getMetaData();

		if (useCases != null) {
			modelMdResolver.useCases(useCases);
		}

		return modelMdResolver.useCase("openapi").is(Visible.T);
	}

	private HeaderLink headerLink(String title, String linkDelegate, String currentDelegate, String domain, String queryParameter) {
		return new HeaderLink(title, "../" + linkDelegate + "/" + domain + queryParameter, linkDelegate.equals(currentDelegate), false);
	}

	private void assertNotInitialized() {
		if (wasInitialized) {
			throw new IllegalStateException("Servlet was already initialized and can't be configured any more.");
		}
	}

	private String swaggerfileUrlFactory(HttpServletRequest request) {
		String apiV1 = "/api/v1/openapi/";
		String[] splitPathInfo = request.getPathInfo().split("/");

		if (splitPathInfo.length != 3) {
			throw new IllegalArgumentException(
					"Openapi UI servlet expects two path elements: one for the request kind and one for the domainId to reflect but got: "
							+ request.getPathInfo());
		}

		// splitPathInfo[0] is empty because pathInfo always starts with a slash if its present
		String requestKind = splitPathInfo[1];
		String domainId = splitPathInfo[2];
		String domainProperty = "services".equals(requestKind) ? "serviceDomain" : "accessId";

		String baseRequest = apiV1 + requestKind + "?" + domainProperty + "=" + domainId;

		if (request.getQueryString() != null) {
			return baseRequest + "&" + request.getQueryString();
		}

		return baseRequest;
	}

	public static class HeaderLink {
		public String title;
		public String url;
		public boolean active;
		public boolean newWindow;

		public HeaderLink(String title, String url, boolean active, boolean newWindow) {
			super();
			this.title = title;
			this.url = url;
			this.active = active;
			this.newWindow = newWindow;
		}

		public String getTitle() {
			return title;
		}

		public String getUrl() {
			return url;
		}

		public boolean getActive() {
			return active;
		}

		public boolean isActive() {
			return active;
		}

		public String getTarget() {
			return newWindow ? "_blank" : "_self";
		}
	}

	@Override
	public void init() {
		wasInitialized = true;

		setContentType("text/html;charset=UTF-8");
	}

	@Configurable
	public void setServicesUrl(String servicesUrl) {
		assertNotInitialized();
		this.servicesUrl = servicesUrl;
	}

	@Configurable
	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Configurable
	@Required
	public void setSwaggerfileUrlFactory(Function<HttpServletRequest, String> swaggerfileUrlFactory) {
		this.swaggerfileUrlFactory = swaggerfileUrlFactory;
	}
}
