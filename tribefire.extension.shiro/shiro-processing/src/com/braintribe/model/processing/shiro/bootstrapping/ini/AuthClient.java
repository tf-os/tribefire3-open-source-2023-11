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
package com.braintribe.model.processing.shiro.bootstrapping.ini;

import java.util.LinkedHashMap;
import java.util.Map;

import com.braintribe.model.shiro.deployment.ShiroClient;

/**
 * Internal data structure that is use to convey data from the Shiro configuration deployables to the {@link ShiroIniFactory}.
 */
public class AuthClient {

	private String name;
	private Map<String,String> configuration = new LinkedHashMap<>();
	private Map<String,String> filters = new LinkedHashMap<>();
	private String urlPart;
	
	public void setClient(ShiroClient shiroClient) {
		
		this.name = shiroClient.getName();
		this.urlPart = "";
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}

	public Map<String, String> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, String> filters) {
		this.filters = filters;
	}

	public String getUrlPart() {
		return urlPart;
	}

	public void setUrlPart(String urlPart) {
		this.urlPart = urlPart;
	}
}
