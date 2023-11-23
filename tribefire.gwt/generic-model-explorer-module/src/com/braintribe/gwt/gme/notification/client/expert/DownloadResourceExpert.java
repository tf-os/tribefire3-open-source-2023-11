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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.List;

import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.uicommand.DownloadResource;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadResourceExpert implements CommandExpert<DownloadResource> {
	
	private PersistenceGmSession session;
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	@Override
	public void handleCommand(DownloadResource command) {
		if (command.getResources() == null || command.getResources().isEmpty())
			return;
		
		List<Resource> resources =  command.getResources();
		
		resources.forEach(r -> {
			String extension = (r.getMimeType() != null && r.getMimeType().contains("/")) ? r.getMimeType().split("/")[1] : "";
			String fileName = r.getName() != null ? r.getName() : r.getId().toString() + "." + extension;
			String url = session.resources().url(r).download(true).asString();
			AnchorElement a = Document.get().createAnchorElement();
			a.setHref(url);
			a.setAttribute("download", fileName);
			RootPanel.get().getElement().appendChild(a);
			click(a);
			RootPanel.get().getElement().removeChild(a);
			/*
			if (r.getResourceSource() instanceof TransientSource) {					
				AnchorElement a = Document.get().createAnchorElement();
				a.setHref(url);
				a.setAttribute("download", fileName);
				RootPanel.get().getElement().appendChild(a);
				click(a);
				RootPanel.get().getElement().removeChild(a);			
			} else {
				Window.open(url, "_new", "");
//				Location.assign(url);
			}
			*/				
		});			
	}
	
	public static native void click(AnchorElement a) /*-{
    	a.click();
	}-*/;
	
}
