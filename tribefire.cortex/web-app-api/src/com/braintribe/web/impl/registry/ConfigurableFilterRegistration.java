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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import com.braintribe.cfg.Required;
import com.braintribe.web.api.registry.FilterConfiguration;
import com.braintribe.web.api.registry.FilterMapping;
import com.braintribe.web.api.registry.FilterRegistration;
import com.braintribe.web.api.registry.WebRegistries;

public class ConfigurableFilterRegistration extends ConfigurableDynamicRegistration implements FilterRegistration, FilterConfiguration {

	protected String name;
	protected Filter filter;
	private Class<? extends Filter> filterClass;
	protected List<FilterMapping> mappings = new ArrayList<>();

	public void setUrlPatterns(List<String> urlPatterns) {
		this.mappings = new ArrayList<>();
		addUrlPatterns(urlPatterns);
	}
	
	private void addMapping(FilterMapping mapping) {
		if (this.mappings == null)
			this.mappings = new ArrayList<>();
		
		this.mappings.add(mapping);
	}
	
	public void addUrlPatterns(List<String> urlPatterns) {
		ConfigurableUrlPatternFilterMapping mapping = new ConfigurableUrlPatternFilterMapping();
		mapping.setUrlPatterns(urlPatterns);
		addMapping(mapping);
	}

	@Override
	public String getName() {
		return name;
	}

	@Required
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	public Class<? extends Filter> getFilterClass() {
		return filterClass;
	}

	public void setFilterClass(Class<? extends Filter> filterClass) {
		this.filterClass = filterClass;
	}

	@Override
	public List<FilterMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<FilterMapping> mappings) {
		this.mappings = mappings;
	}

	/* builder methods */

	public ConfigurableFilterRegistration name(String filterName) {
		setName(filterName);
		return this;
	}

	public ConfigurableFilterRegistration instance(Filter instance) {
		setFilter(instance);
		return this;
	}

	public ConfigurableFilterRegistration type(Class<? extends Filter> type) {
		setFilterClass(type); 
		return this;
	}

	public ConfigurableFilterRegistration mappings(FilterMapping ... filterMappings) {
		setMappings(Arrays.asList(filterMappings));
		return this;
	}

	public ConfigurableFilterRegistration pattern(String urlPattern) {
		setUrlPatterns(Arrays.asList(urlPattern));
		return this;
	}

	public ConfigurableFilterRegistration patterns(String ... urlPatterns) {
		setUrlPatterns(Arrays.asList(urlPatterns));
		return this;
	}

	public ConfigurableFilterRegistration initParams(Map<String, String> initParams) {
		setInitParameters(initParams);
		return this;
	}

	public ConfigurableFilterRegistration order(int order) {
		setOrder(order);
		return this;
	}

	@Override
	public void addPattern(String pattern) {
		addUrlPatterns(Arrays.asList(pattern));
		
	}

	@Override
	public void addPatterns(String... patterns) {
		addUrlPatterns(Arrays.asList(patterns));
		
	}

	@Override
	public void addServletName(String servletName) {
		ConfigurableServletNamesFilterMapping nameMapping = WebRegistries.nameMapping().names(servletName);
		addMapping(nameMapping);
	}

	@Override
	public void addServletNames(String... servletNames) {
		for (String name : servletNames) {
			addServletName(name);
		}
	}

}
