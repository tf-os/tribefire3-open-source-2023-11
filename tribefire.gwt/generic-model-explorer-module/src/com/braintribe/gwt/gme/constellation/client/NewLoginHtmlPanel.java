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
package com.braintribe.gwt.gme.constellation.client;

import com.braintribe.gwt.gme.cssresources.client.FavIconCssLoader;
import com.braintribe.gwt.gme.cssresources.client.TitleCssLoader;
import com.braintribe.gwt.gme.uitheme.client.UiThemeCssLoader;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;

public class NewLoginHtmlPanel extends HtmlPanel{
	
	private String accessId;
    private UiThemeCssLoader uiThemeCssLoader;
    private FavIconCssLoader favIconCssLoader;
    private TitleCssLoader titleCssLoader;

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	public void setUiThemeLoader(UiThemeCssLoader uiThemeCssLoader) {
		this.uiThemeCssLoader = uiThemeCssLoader;
		if ((this.uiThemeCssLoader != null) && (this.accessId != null)) {
			this.uiThemeCssLoader.loadUiThemeCss(this.accessId);
		}
	}

	public void setFavIconLoader(FavIconCssLoader favIconCssLoader) {
		this.favIconCssLoader = favIconCssLoader;
		if ((this.favIconCssLoader != null) && (this.accessId != null)) {
			this.favIconCssLoader.loadFavIcon(this.accessId);
		}
	}
	
	public void setTitleLoader(TitleCssLoader titleCssLoader) {
		this.titleCssLoader = titleCssLoader;
		if ((this.titleCssLoader != null) && (this.accessId != null)) {
			this.titleCssLoader.loadTitle(this.accessId);
		}
	}		
}
