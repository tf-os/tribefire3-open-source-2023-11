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
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.uicommand.PrintResource;
import com.google.gwt.user.client.Window;

public class PrintResourceExpert implements CommandExpert<PrintResource> {
	
	private PersistenceGmSession session;
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	@Override
	public void handleCommand(PrintResource command) {
		if (command.getResources() == null || command.getResources().isEmpty())
			return;
		
		List<Resource> resources =  command.getResources();
		
		resources.forEach(r -> {
			String extension = (r.getMimeType() != null && r.getMimeType().contains("/")) ? r.getMimeType().split("/")[1] : "";
			String fileName = r.getName() != null ? r.getName() : r.getId().toString() + "." + extension;
			String url = session.resources().url(r).download(true).fileName(fileName).asString();
			if (r.getResourceSource() instanceof TransientSource)
				openAndPrint(url);					
			else
				System.err.println("not supported yet");
		});			
	}
	
	private native void print(Window w) /*-{
		w.print();
	}-*/;

	private native Window openAndPrint(String url) /*-{
		var oReq = new XMLHttpRequest();
        oReq.open("GET", url,  true);
        oReq.responseType = "blob";
        oReq.onload = function(oEvent) {
            var gwt_print_dialog_frame_id = "gwt-print-dialog-frame";

            el = document.getElementById(gwt_print_dialog_frame_id);
            if(el)
                el.parentNode.removeChild(el);

            var blob = oReq.response;		  
            var newBlob = new Blob([blob], {type: "application/pdf"});
            var data = window.URL.createObjectURL(newBlob);

            var a = document.createElement("iframe");
            a.setAttribute("id", gwt_print_dialog_frame_id);			
            setTimeout(function(){	
                a.contentWindow.focus();
                a.contentWindow.print();				
            },1000);					
            document.body.appendChild(a);			
            a.src = data;
        };
        oReq.send();		
	}-*/;
	
}
