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

import com.google.gwt.xhr.client.XMLHttpRequest;

public class TitleCssLoader {
	private String url = "../tribefire-services/publicResource/dynamic/gme-title";	//Default value
	private Boolean titleIsSet = false;
	
	public void loadTitle (String accessId) {
		setSetting(accessId, null);
	}

	public void loadTitle (String accessId, String applicationId) {
		setSetting(accessId, applicationId);
	}
	
	public void setTitleUrl(String url) {
        this.url = url;		
	}
			
	public boolean isTitleSet() {
		return titleIsSet;
	}
	
	private String addParameterSeparator(Boolean isFirst) {
		return isFirst ? "?" : "&";
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

		if (urlString == null || urlString.isEmpty())
			return;
		
		XMLHttpRequest request = XMLHttpRequest.create();
		request.open("get", urlString);
		request.setRequestHeader("Accept", "gm/jse");
		
		request.setOnReadyStateChange(xhr -> {
			if (xhr.getReadyState() != XMLHttpRequest.DONE || xhr.getStatus() != 200)
				return;
			
			String title = xhr.getResponseText();
			if (title != null && !title.isEmpty()) {
				CssLinkLoader.setDocumentTitle(title);
				titleIsSet = true;
			}
		});			
		request.send();			
	}	
}
