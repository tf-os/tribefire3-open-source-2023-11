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
package com.braintribe.model.processing.wopi.model;

import com.braintribe.model.processing.wopi.misc.HttpResponseJSON;

/*
	{
	"Name":{"type":"string","optional":false},
	"Url":{"type":"string","default":"","optional":false},
	"HostViewUrl":{"type":"string","default":"","optional":true},
	"HostEditUrl":{"type":"string","default":"","optional":true},
	}
 */
public class PutRelativeFile extends HttpResponseJSON {

	private String name;
	private String url;
	private String hostViewUrl;
	private String hostEditUrl;

	public PutRelativeFile(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHostViewUrl() {
		return hostViewUrl;
	}

	public void setHostViewUrl(String hostViewUrl) {
		this.hostViewUrl = hostViewUrl;
	}

	public String getHostEditUrl() {
		return hostEditUrl;
	}

	public void setHostEditUrl(String hostEditUrl) {
		this.hostEditUrl = hostEditUrl;
	}

}
