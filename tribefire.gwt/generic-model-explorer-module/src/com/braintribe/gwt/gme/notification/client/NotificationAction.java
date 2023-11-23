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
package com.braintribe.gwt.gme.notification.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;

public class NotificationAction {

	private String id, text;
	private ImageResource icon;
	private String className;

	public NotificationAction(String id, String className, ImageResource icon, String text) {
		this.id = id;
		this.icon = icon;
		this.text = text;
		this.className = className;
	}

	public NotificationAction(String id, ImageResource icon, String text) {
		this.id = id;
		this.icon = icon;
		this.text = text;
		this.className = null;
	}
	
	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getClassName() {
		return className;
	}
	
	public SafeStyles getIconStyle() {
		return icon == null ? new SafeStylesBuilder().toSafeStyles() : new SafeStylesBuilder()//
				.backgroundImage(icon.getSafeUri()).width(icon.getWidth(), Unit.PX).height(icon.getHeight(), Unit.PX)//
				.toSafeStyles();
	}

}
