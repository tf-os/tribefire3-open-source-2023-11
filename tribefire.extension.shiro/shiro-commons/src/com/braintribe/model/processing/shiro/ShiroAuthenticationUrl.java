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
package com.braintribe.model.processing.shiro;

public class ShiroAuthenticationUrl {

	private String authenticationUrl;
	private String name;
	private String imageUrl;
	private String iconResourceId;

	public ShiroAuthenticationUrl(String authenticationUrl, String name, String imageUrl) {
		super();
		this.authenticationUrl = authenticationUrl;
		this.name = name;
		this.imageUrl = imageUrl;
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}
	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getIconResourceId() {
		return iconResourceId;
	}
	public void setIconResourceId(String iconResourceId) {
		this.iconResourceId = iconResourceId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" / URL: ");
		sb.append(authenticationUrl);
		sb.append(" / Image: ");
		sb.append(imageUrl);
		sb.append(" / Icon Resource: ");
		sb.append(iconResourceId);
		return sb.toString();
	}
}
