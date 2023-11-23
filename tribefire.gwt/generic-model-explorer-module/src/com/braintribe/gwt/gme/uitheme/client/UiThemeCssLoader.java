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
package com.braintribe.gwt.gme.uitheme.client;

import com.braintribe.gwt.gme.cssresources.client.CssLinkLoader;

public class UiThemeCssLoader implements UiThemeInterface {
	private String url = "../tribefire-services/publicResource/dynamic/UiTheme";	//Default value
	
	//DB Query for get folder structure of UITheme
	public UiThemeCssLoader() {
		//this.entityQuery = EntityQueryBuilder.from(Folder.class).tc(TC.create().negation().joker().done()).where().property("name").eq("uitheme").done();
		//prepareDefaultUiTheme();
	}
	
	public void loadUiThemeCss (String accessId) {
		setCssSetting(accessId, null);
	}

	public void loadUiThemeCss (String accessId, String applicationId) {
		setCssSetting(accessId, applicationId);
	}	
	
	public void setUiThemeUrl(String url) {
        this.url = url;		
	}
		
	public String rgbColorToHex(int red, int green, int blue) {
		String hex;
		String strRed   = Integer.toHexString(red);
		if (strRed.length() == 1) { strRed = "0" + strRed; }		
		String strGreen = Integer.toHexString(green);
		if (strGreen.length() == 1) { strGreen = "0" + strGreen; }
		String strBlue  = Integer.toHexString(blue);		
		if (strBlue.length() == 1) { strBlue = "0" + strBlue; }
		hex = "#" + strRed + strGreen + strBlue;				 		
		return hex;
	}
	
	private String addParameterSeparator(Boolean isFirst) {
		if (isFirst)
			return "?";
		else			
			return "&";
	} 	
	
	private void setCssSetting(String accessId, String applicationId) {
				
		//if (cssLinkLoader == null) {
			/*
			if (this.stylesheetResource != null) {				
				String downloadName = this.stylesheetResource.getName() != null ? this.stylesheetResource.getName() : this.stylesheetResource.getResourceSource().getId();
				CssLinkLoader cssLinkLoader = new CssLinkLoader();
				String urlString = GWT.getHostPageBaseURL() + workbenchSession.resources().buildStreamingUrl(this.stylesheetResource).forDownload(false).asString();
				cssLinkLoader.loadNewCss("cssUiThemeStylesheet", urlString);					
			}
			*/
			
			//String downloadName = this.stylesheetResource.getName() != null ? this.stylesheetResource.getName() : this.stylesheetResource.getResourceSource().getId();
		
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
			//String urlString = "../tribefire-services/publicResource/dynamic/DynamicCSS.css";
			CssLinkLoader.unloadCss("cssUiThemeStylesheet");
			CssLinkLoader.loadNewCss("cssUiThemeStylesheet", "stylesheet", "text/css", urlString);			
		//}
	}

}
