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
package com.braintribe.web.servlet.publicresource.streamer;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.utils.IOTools;
import com.braintribe.web.servlet.publicresource.PublicResourceStreamer;

public class LocaleStreamer implements PublicResourceStreamer {
	
	private WorkbenchConfigurationProvider configurationProvider;
	private String defaultLocale = "en";
	
	
	@Required
	@Configurable
	public void setConfigurationProvider(WorkbenchConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}
	
	@Configurable
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	
	@Override
	public boolean streamResource(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String locale = this.defaultLocale;
		String accessId = request.getParameter("accessId");
		if (accessId != null) {
			WorkbenchConfiguration configuration = this.configurationProvider.getConfiguration(accessId);
			if (configuration != null && configuration.getLocale() != null) {
				locale = configuration.getLocale();
			}
		}		
		
		response.setContentType("text/plain");
		IOTools.pump(new ByteArrayInputStream(locale.getBytes()), response.getOutputStream());
		return true;
	}
}
