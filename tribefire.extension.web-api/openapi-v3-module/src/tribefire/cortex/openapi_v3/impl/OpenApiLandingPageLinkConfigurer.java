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
package tribefire.cortex.openapi_v3.impl;

import java.net.URLEncoder;
import java.util.function.BiConsumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.web.servlet.home.model.Link;
import com.braintribe.web.servlet.home.model.LinkCollection;

public class OpenApiLandingPageLinkConfigurer implements BiConsumer<ServiceDomain, LinkCollection> {
	private static final Logger logger = Logger.getLogger(OpenApiLandingPageLinkConfigurer.class);
	
	private static final String USECASE_OPENAPI = "openapi";
	private static final String USECASE_SWAGGER = "swagger";
	private static final String USECASE_DDRA = "ddra";
	
	private final String simpleUseCase = "useCases=openapi:simple";
	private String relativeApiPath = "openapi/ui/services/{domainId}?" + simpleUseCase;
	private String relativeCrudPath = "openapi/ui/entities/{domainId}?" + simpleUseCase;
	
	private ModelAccessoryFactory modelAccessoryFactory;

	// *************************************************************
	// SETTERS
	// *************************************************************

	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}
	
	/**
	 * @param relativeCrudPath
	 *            path to a UI representation of the CRUD API of an access - e.g. a Swagger UI. The path is relative to
	 *            tf-services and should contain the String <tt>{domainId}</tt> as a placeholder for the actual domain id (=
	 *            externalId) of the access like <tt>openapi/ui/entities/{domainId}</tt>
	 */
	@Configurable
	public void setRelativeCrudPath(String relativeCrudPath) {
		this.relativeCrudPath = relativeCrudPath;
	}

	/**
	 * @param relativeApiPath
	 *            path to a UI representation of the API of a service domain - e.g. a Swagger UI. The path is relative to
	 *            tf-services and should contain the String <tt>{domainId}</tt> as a placeholder for the actual domain id
	 *            like <tt>openapi/ui/services/{domainId}</tt>
	 */
	@Configurable
	public void setRelativeApiPath(String relativeApiPath) {
		this.relativeApiPath = relativeApiPath;
	}

	
	@Override
	public void accept(ServiceDomain serviceDomain, LinkCollection linkCollection) {
		addApiLinks(serviceDomain, linkCollection);
	}
	
	private String tabLink(String title, String path) {
		return "./home?selectedTab=" + title + "&selectedTabPath=" + urlEncode(path);
	}
	
	private void addApiLink(String domainId, LinkCollection links) {
		String tabLink = tabLink("Service API", resolveApiPath(relativeApiPath, domainId));
		links.getNestedLinks().add(createLink("Service API", tabLink, "_self", null));
	}

	private void addCrudApiLink(LinkCollection links, String domainId) {
		String tabLink = tabLink("Access API", resolveApiPath(relativeCrudPath, domainId));
		links.getNestedLinks().add(createLink("Access API", tabLink, "_self", null));
	}

	private String resolveApiPath(String apiPath, String domainId) {
		return apiPath.replace("{domainId}", urlEncode(domainId));
	}
	
	public void addApiLinks(ServiceDomain domain, LinkCollection links) {
		String domainId = domain.getExternalId();

		try {
			if (isModelVisible(domain.getServiceModel(), USECASE_DDRA, USECASE_OPENAPI, USECASE_SWAGGER)) {
				addApiLink(domainId, links);
			} 
			
			if (domain instanceof IncrementalAccess) {
				GmMetaModel dataModel = ((IncrementalAccess) domain).getMetaModel();

				if (isModelVisible(dataModel, USECASE_DDRA, USECASE_OPENAPI, USECASE_SWAGGER)) {
					addCrudApiLink(links, domainId);
				}
			}
		} catch (Exception e) {
			logger.warn(() -> "Error while getting API links for domain " + domain, e);
			links.setHasErrors(true);
		}
	}
	
	private boolean isModelVisible(GmMetaModel model, String... useCases) {
		if (model == null)
			return false;

		ModelAccessory modelAccessory = modelAccessoryFactory.getForModel(model.getName());
		CmdResolver resolver = modelAccessory.getCmdResolver();

		ModelMdResolver mdResolver = resolver.getMetaData();
		if (useCases != null && useCases.length > 0) {
			mdResolver.useCases(useCases);
		}
		return mdResolver.is(Visible.T);
	}
	
	private static String urlEncode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (Exception e) {
			logger.warn("Could not URL encode text: " + text);
			return "Unknown";
		}
	}
	
	private static Link createLink(String displayName, String url, String target, String type) {
		return createLink(displayName, url, target, type, null);
	}

	private static Link createLink(String displayName, String url, String target, String type, String iconRef) {
		if (url == null || url.isEmpty())
			return null;

		Link repositoryLink = Link.T.create();
		repositoryLink.setDisplayName(displayName);
		repositoryLink.setUrl(url);
		repositoryLink.setTarget(target);
		repositoryLink.setType(type);
		repositoryLink.setIconRef(iconRef);
		return repositoryLink;
	}

}
