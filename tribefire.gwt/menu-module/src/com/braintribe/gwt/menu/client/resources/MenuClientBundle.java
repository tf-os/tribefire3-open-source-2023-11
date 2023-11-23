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
package com.braintribe.gwt.menu.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface MenuClientBundle extends ClientBundle {
	
	public static final MenuClientBundle INSTANCE = GWT.create(MenuClientBundle.class);
	
	@Source ("Logout_16x16.png")
	public ImageResource logout();
	@Source ("bwLogout.png")
	public ImageResource logoutHover();
	@Source ("Settings_16x16.png")
	public ImageResource settings();
	@Source ("Settings_16x16.png")
	public ImageResource settingsHover();
	@Source ("Settings_32x32.png")
	public ImageResource settingsBig();
	@Source ("Refresh_16x16.png")
	public ImageResource refresh();
	@Source ("Refresh_32x32.png")
	public ImageResource refreshHover();
	@Source ("User_16x16.png")
	public ImageResource user();
	@Source ("User_16x16.png")
	public ImageResource userHover();
	
	@Source ("menuBarStyle.gss")
	public MenuCss css();

}
