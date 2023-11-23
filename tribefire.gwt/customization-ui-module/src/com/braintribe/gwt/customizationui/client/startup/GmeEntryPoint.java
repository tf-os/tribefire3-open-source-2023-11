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
package com.braintribe.gwt.customizationui.client.startup;

import com.braintribe.gwt.gxt.gxtresources.whitemask.client.MaskController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;

/**
 * Default implementation for loading the logo and title for the client loading message.
 * @author michel.docouto
 *
 */
public abstract class GmeEntryPoint implements EntryPoint {
	
	protected boolean logoSet = false;
	protected boolean titleSet = false;
	private static final long TIMEOUT = 5000;
	
	protected void waitForLogoAndText(String servicesUrl, String accessId, String applicationId) {
		long start = System.currentTimeMillis();
		Scheduler.get().scheduleFixedDelay(() -> {
			if (!titleSet) {
				String title = TribefireRuntime.getProperty("TRIBEFIRE_LOADING_TITLE", null, true);
				if (title != null) {
					titleSet = true;
					MaskController.setProgressBarTitle(title);
				}
			}
			
			if (!logoSet) {
				String logoUrl = TribefireRuntime.getProperty("TRIBEFIRE_LOGO_URL", null, true);
				if (logoUrl != null) {
					logoSet = true;
					if (!logoUrl.equals(getDefaultLogoUrl(servicesUrl, accessId, applicationId)))
						MaskController.setProgressBarImage(logoUrl);
				}
			}
			
			if (logoSet && titleSet)
				return false;
			
			return System.currentTimeMillis() - start <= TIMEOUT;
		}, 100);
	}
	
	protected String getDefaultLogoUrl(String servicesUrl, String accessId, String applicationId) {
		String imageSource = servicesUrl + "publicResource/dynamic/gme-logo?";
		if (accessId != null)
			imageSource = imageSource + "accessId=" + accessId + "&";
			
		imageSource = imageSource + "applicationId=" + applicationId;
		
		return imageSource;
	}
	
}
