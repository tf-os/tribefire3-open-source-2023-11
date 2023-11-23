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
package com.braintribe.gwt.gmview.client.js;

import java.util.List;

import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.MODULE_NAMESPACE)
public class ComponentCreateContext {
	
	private String modulePath;
	private String accessId;
	private List<String> cssStyles;
	private JsPersistenceSessionFactory persistenceSessionFactory;
	private String rootUrl;
	private String servicesUrl;
	
	@JsConstructor
	public ComponentCreateContext() {
	}
	
	@JsProperty
	public void setModulePath(String modulePath) {
		this.modulePath = modulePath;
	}
	
	@JsProperty
	public String getModulePath() {
		return modulePath;
	}
	
	@JsProperty
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	@JsProperty
	public String getAccessId() {
		return accessId;
	}
	
	@JsProperty
	public void setCssStyles(List<String> cssStyles) {
		this.cssStyles = cssStyles;
	}
	
	@JsProperty
	public List<String> getCssStyles() {
		return cssStyles;
	}
	
	@JsProperty
	public void setPersistenceSessionFactory(JsPersistenceSessionFactory persistenceSessionFactory) {
		this.persistenceSessionFactory = persistenceSessionFactory;
	}
	
	@JsProperty
	public JsPersistenceSessionFactory getPersistenceSessionFactory() {
		return persistenceSessionFactory;
	}
	
	@JsProperty
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}
	
	@JsProperty
	public String getRootUrl() {
		return rootUrl;
	}
	
	@JsProperty
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	
	@JsProperty
	public String getServicesUrl() {
		return servicesUrl;
	}

}
