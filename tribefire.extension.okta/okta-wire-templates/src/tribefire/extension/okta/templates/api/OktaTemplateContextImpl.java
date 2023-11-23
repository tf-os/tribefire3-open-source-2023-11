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
package tribefire.extension.okta.templates.api;

import com.braintribe.logging.Logger;

import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.templates.api.TemplateContextImpl;

public class OktaTemplateContextImpl extends TemplateContextImpl<OktaTemplateContext> implements OktaTemplateContext, OktaTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(OktaTemplateContextImpl.class);

	private String context;

	private String serviceBaseUrl;

	private OktaAuthenticationSupplier accessAuthenticationSupplier;

	private OktaAuthenticationSupplier defaultAuthenticationSupplier;

	@Override
	public OktaTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public OktaTemplateContextBuilder setServiceBaseUrl(String serviceBaseUrl) {
		this.serviceBaseUrl = serviceBaseUrl;
		return this;
	}

	@Override
	public OktaTemplateContextBuilder setAccessAuthenticationSupplier(OktaAuthenticationSupplier accessAuthenticationSupplier) {
		this.accessAuthenticationSupplier = accessAuthenticationSupplier;
		return this;
	}

	@Override
	public String getServiceBaseUrl() {
		return serviceBaseUrl;
	}

	@Override
	public OktaAuthenticationSupplier getAccessAuthenticationSupplier() {
		return accessAuthenticationSupplier;
	}

	@Override
	public OktaTemplateContextBuilder setDefaultAuthenticationSupplier(OktaAuthenticationSupplier defaultAuthenticationSupplier) {
		this.defaultAuthenticationSupplier = defaultAuthenticationSupplier;
		return this;
	}

	@Override
	public OktaAuthenticationSupplier getDefaultAuthenticationSupplier() {
		return defaultAuthenticationSupplier;
	}

}
