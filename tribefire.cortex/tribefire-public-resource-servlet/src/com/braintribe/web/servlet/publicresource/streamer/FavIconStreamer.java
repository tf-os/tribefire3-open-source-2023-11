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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.web.servlet.publicresource.PublicResourceStreamer;

public class FavIconStreamer implements PublicResourceStreamer {
	
	private UserSessionScoping userSessionScoping;
	private WorkbenchConfigurationProvider configurationProvider;
	
	private File defaultFavicon;
	
	@Required
	@Configurable
	public void setConfigurationProvider(WorkbenchConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	@Required
	@Configurable
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}
	
	@Configurable
	public void setDefaultFavicon(File defaultFavicon) {
		this.defaultFavicon = defaultFavicon;
	}
	
	@Override
	public boolean streamResource(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String accessId = request.getParameter("accessId");
		if (accessId != null) {
			Resource favIcon = this.configurationProvider.getFavIcon(accessId);
			if (favIcon != null) {

				String mimeType = favIcon.getMimeType();
				if (StringTools.isEmpty(mimeType)) {
					mimeType = "image/x-icon";
				}
				response.setContentType(mimeType);

				UserSessionScope scope = userSessionScoping.forDefaultUser().push();

				InputStream resourceStream = favIcon.openStream();
				try {
					IOTools.pump(resourceStream, response.getOutputStream());
					return true;
				} finally {
					resourceStream.close();
					scope.pop();
				}
			}
		}		
		
		if (this.defaultFavicon != null) {
			response.setContentType("image/x-icon");
			
			try (InputStream is = new FileInputStream(this.defaultFavicon)) {
				IOTools.pump(is, response.getOutputStream());
			}
			
			return true;
		}
		
		return false;
	}
}
