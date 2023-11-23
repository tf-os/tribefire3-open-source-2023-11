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
package com.braintribe.gwt.gme.cssresources.client;

public class FavIconCssLoader {
	private String url = "../tribefire-services/publicResource/dynamic/gme-favicon";	//Default value
	
	public void loadFavIcon (String accessId) {
		setSetting(accessId, null);
	}

	public void loadFavIcon (String accessId,  String applicationId) {
		setSetting(accessId, applicationId);
	}
	
	public void setFavIconUrl(String url) {
        this.url = url;		
	}
			
	private String addParameterSeparator(Boolean isFirst) {
		if (isFirst)
			return "?";
		else			
			return "&";
	} 
	
	private void setSetting(String accessId, String applicationId) {				
		String urlString = this.url;			
		Boolean isFirstParameter = true;
	    if (accessId != null) {
		   urlString = urlString + addParameterSeparator(isFirstParameter) + "accessId=" + accessId;
		   isFirstParameter = false;
	    }
	    if (applicationId != null) { 
		   urlString = urlString + addParameterSeparator(isFirstParameter) +"applicationId=" + applicationId;
		   isFirstParameter = false;
	    }	    
	    
		//TEST
		//urlString = "../tribefire-services/publicResource/dynamic/favicon.ico";
		CssLinkLoader.unloadCss("faviIcon");
		CssLinkLoader.loadNewCss("faviIcon", "shortcut icon", "image/x-icon", urlString);
	}
}
