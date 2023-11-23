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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Image;

public class UserImageProvider implements Supplier<Future<Image>> {
	
	private Future<Image> future;
	private String servicesUrl;
	private String userImageUrl = "user-image";
	
	/**
	 * Configures the required url for the tribefire services.
	 */
	@Required
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	
	/**
	 * Configures the url for the user. Defaults to "user-image".
	 */
	@Configurable
	public void setUserImageUrl(String userImageUrl) {
		this.userImageUrl = userImageUrl;
	}
	
	@Override
	public Future<Image> get() throws RuntimeException {
		if (this.future == null) {
			this.future = new Future<>();
			
			Scheduler.get().scheduleDeferred(() -> {
				Image image = new Image();
				
				StringBuilder url = new StringBuilder();
				url.append(servicesUrl);
				if (!servicesUrl.endsWith("/"))
					url.append("/");
				
				url.append(userImageUrl);
				image.setUrl(url.toString());
				image.setStyleName("");
				image.setWidth("32px");
				image.setHeight("32px");
				image.getElement().getStyle().setProperty("borderRadius", "50%");
				future.onSuccess(image);
			});
		}
	
		return future;
	}

}
