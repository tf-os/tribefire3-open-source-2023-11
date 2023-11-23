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
package com.braintribe.gwt.resourceuploadui.client;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.KnownProperties;
import com.braintribe.gwt.gme.constellation.client.DualSectionButton;
import com.google.gwt.resources.client.ImageResource;

public class ShowUploadDialogsAction extends DualSectionButton {
	
	/*
	private ShowWindowAction showResourceUpload;
	private ShowDocumentUploadDialogAction showDocumentUpload;
	
	public void setShowResourceUpload(ShowWindowAction showResourceUpload) {
		this.showResourceUpload = showResourceUpload;
	}
	
	public void setShowDocumentUpload(ShowDocumentUploadDialogAction showDocumentUpload) {
		this.showDocumentUpload = showDocumentUpload;
		
		showDocumentUpload.addPropertyListener((source, property) -> {
			if (KnownProperties.PROPERTY_HIDDEN.equals(property)) {
				setDualSectionVisible(!showDocumentUpload.getHidden());
			}
		});
	}
	*/
	
	@Override
	public void setPrimaryAction(Action primaryAction) {
		super.setPrimaryAction(primaryAction);
	}
	
	@Override
	public void setSecondaryAction(Action secondaryAction) {
		super.setSecondaryAction(secondaryAction);				
		secondaryAction.addPropertyListener((source, property) -> {
			if (KnownProperties.PROPERTY_HIDDEN.equals(property)) 
				setDualSectionVisible(!secondaryAction.getHidden());
		});		
	}	
	
	public ShowUploadDialogsAction(ImageResource icon, ImageResource dualIcon, String text, String qTip) {
		super(icon, dualIcon, text, qTip);
		/*
		addDualSectionButtonListener(new DualSectionButtonListener() {
			@Override
			public void onUpperSectionClicked(ClickEvent event) {
				showResourceUpload.perform(null);
			}
			
			@Override
			public void onLowerSectionClicked(ClickEvent event) {
				showDocumentUpload.perform(null);
			}
		});
		*/
	}
	

}
