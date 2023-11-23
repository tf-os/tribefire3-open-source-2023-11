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
package tribefire.platform.impl.binding;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.UndeploymentContext;
import com.braintribe.servlet.FilterServlet;

import tribefire.platform.impl.web.ComponentDispatcherServlet;

public class WebTerminalBinder implements DirectComponentBinder<WebTerminal, HttpServlet> {

	private ComponentDispatcherServlet dispatcherServlet;
	private Filter authFilter;

	@Required
	public void setDispatcherServlet(ComponentDispatcherServlet dispatcherServlet) {
		this.dispatcherServlet = dispatcherServlet;
	}

	@Configurable
	public void setAuthFilter(Filter authFilter) {
		this.authFilter = authFilter;
	}

	@Override
	public EntityType<WebTerminal> componentType() {
		return WebTerminal.T;
	}

	@Override
	public HttpServlet bind(MutableDeploymentContext<WebTerminal, HttpServlet> context) throws DeploymentException {
		HttpServlet httpServlet = context.getInstanceToBeBound();
		WebTerminal webTerminal = context.getDeployable();

		if (authFilter != null)
			httpServlet = applyFilter(httpServlet, authFilter);

		dispatcherServlet.registerAndInitServlet(webTerminal, httpServlet);

		return httpServlet;
	}

	private static HttpServlet applyFilter(HttpServlet httpServlet, Filter filter) {
		FilterServlet filterServlet = new FilterServlet();
		filterServlet.setDelegate(httpServlet);
		filterServlet.setFilter(filter);

		return filterServlet;
	}

	@Override
	public void unbind(UndeploymentContext<WebTerminal, HttpServlet> context) {
		dispatcherServlet.unregisterAndDestroy(context.getDeployable());
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { javax.servlet.Servlet.class };
	}

}
