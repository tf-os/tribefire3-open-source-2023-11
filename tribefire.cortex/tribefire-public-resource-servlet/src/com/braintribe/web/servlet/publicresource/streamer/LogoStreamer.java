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

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.web.servlet.publicresource.PublicResourceStreamer;

public class LogoStreamer implements PublicResourceStreamer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LogoStreamer.class);
	
	private UserSessionScoping userSessionScoping;
	private WorkbenchConfigurationProvider configurationProvider;
	
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
	
	@Override
	public boolean streamResource(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String accessId = request.getParameter("accessId");
		if (accessId != null) {
			Resource logo = this.configurationProvider.getFolderImage(accessId, "tb_Logo");
			if (logo != null) {

				String mimeType = logo.getMimeType();
				response.setContentType(mimeType);

				UserSessionScope scope = userSessionScoping.forDefaultUser().push();

				InputStream resourceStream = logo.openStream();
				try {
					IOTools.pump(resourceStream, response.getOutputStream());
					return true;
				} finally {
					resourceStream.close();
					scope.pop();
				}
			}
		}		
		
		
		return false;
	}
	
	
}
