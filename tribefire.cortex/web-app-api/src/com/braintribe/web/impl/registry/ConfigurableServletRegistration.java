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
package com.braintribe.web.impl.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import com.braintribe.cfg.Required;
import com.braintribe.web.api.registry.MultipartConfig;
import com.braintribe.web.api.registry.ServletRegistration;

public class ConfigurableServletRegistration extends ConfigurableDynamicRegistration implements ServletRegistration {

	protected String name;
	protected Servlet servlet;
	private Class<? extends Servlet> servletClass;
	protected List<String> mappings = Collections.emptyList();
	protected int loadOnStartup;
	protected MultipartConfig configurableMultipartConfig;
	protected String runAsRole;

	@Override
	public String getName() {
		return name;
	}

	@Required
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Servlet getServlet() {
		return servlet;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}

	public void setServletClass(Class<? extends Servlet> servletClass) {
		this.servletClass = servletClass;
	}

	@Override
	public Class<? extends Servlet> getServletClass() {
		return servletClass;
	}

	@Override
	public List<String> getMappings() {
		return mappings;
	}

	public void setMappings(List<String> mappings) {
		this.mappings = mappings;
	}

	@Override
	public String[] getMappingsArray() {
		return this.mappings.toArray(new String[this.mappings.size()]);
	}

	@Override
	public int getLoadOnStartup() {
		return loadOnStartup;
	}

	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	@Override
	public MultipartConfig getMultipartConfig() {
		return configurableMultipartConfig;
	}

	public void setMultipartConfig(MultipartConfig configurableMultipartConfig) {
		this.configurableMultipartConfig = configurableMultipartConfig;
	}

	@Override
	public String getRunAsRole() {
		return runAsRole;
	}

	public void setRunAsRole(String runAsRole) {
		this.runAsRole = runAsRole;
	}

	/* builder methods */

	public ConfigurableServletRegistration name(String filterName) {
		setName(filterName);
		return this;
	}

	public ConfigurableServletRegistration instance(Servlet instance) {
		setServlet(instance);
		return this;
	}

	public ConfigurableServletRegistration type(Class<? extends Servlet> type) {
		setServletClass(type);
		return this;
	}

	public ConfigurableServletRegistration pattern(String urlPattern) {
		setMappings(Arrays.asList(urlPattern));
		return this;
	}

	public ConfigurableServletRegistration patterns(String... urlPatterns) {
		setMappings(Arrays.asList(urlPatterns));
		return this;
	}

	public ConfigurableServletRegistration initParams(Map<String, String> initParams) {
		setInitParameters(initParams);
		return this;
	}

	public ConfigurableServletRegistration multipart() {
		setMultipartConfig(new ConfigurableMultipartConfig());
		return this;
	}

	public ConfigurableServletRegistration multipart(MultipartConfig multipartConfig) {
		setMultipartConfig(multipartConfig);
		return this;
	}

	public ConfigurableServletRegistration order(int order) {
		setOrder(order);
		return this;
	}

	/* // builder methods */

}
