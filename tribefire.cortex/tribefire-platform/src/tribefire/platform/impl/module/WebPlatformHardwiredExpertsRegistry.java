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
package tribefire.platform.impl.module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.web.servlet.auth.AuthFilter;
import com.braintribe.web.servlet.auth.WebCredentialsProvider;
import com.braintribe.web.servlet.home.HomeServlet;
import com.braintribe.web.servlet.home.model.LinkCollection;

import tribefire.cortex.module.loading.PlatformHardwiredExpertsRegistry;
import tribefire.module.wire.contract.WebPlatformHardwiredExpertsContract;

/**
 * @author peter.gazdik
 */
public class WebPlatformHardwiredExpertsRegistry extends PlatformHardwiredExpertsRegistry implements WebPlatformHardwiredExpertsContract {

	private HomeServlet homeServlet;
	private final List<AuthFilter> authFilters = new ArrayList<>();

	@Required
	public void setHomeServlet(HomeServlet homeServlet) {
		this.homeServlet = homeServlet;
	}

	@Configurable
	public void addAuthFilter(AuthFilter authFilter) {
		authFilters.add(authFilter);
	}

	@Override
	public <T extends GenericEntity> void bindLandingPageLinkConfigurer(String groupPattern, EntityType<T> type,
			BiConsumer<T, LinkCollection> configurer) {
		homeServlet.addLinkConfigurer(groupPattern, type, configurer);
	}

	@Override
	public void registerWebCredentialsProvider(String key, WebCredentialsProvider webCredentialsProvider) {
		for (AuthFilter authFilter : authFilters) {
			authFilter.addWebCredentialProvider(key, webCredentialsProvider);
		}
	}

}
