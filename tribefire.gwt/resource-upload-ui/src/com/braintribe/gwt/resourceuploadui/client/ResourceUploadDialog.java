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

import java.util.List;

import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gmview.client.ResourceUploadViewListener;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.resourceuploadui.client.resources.ResourceUploadResources;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class ResourceUploadDialog extends ClosableWindow implements InitializableBean, EntityFieldDialog<Resource> {
	
	private ResourceUploadPanel resourceUploadPanel;
	private TextButton closeButton;
	private boolean isUploading;
	
	@Configurable @Required
	public void setResourceUploadPanel(ResourceUploadPanel resourceUploadPanel) {
		this.resourceUploadPanel = resourceUploadPanel;
		this.resourceUploadPanel.addResourceUploadViewListener(new ResourceUploadViewListener() {
			@Override
			public void onUploadFinished(List<Resource> resources) {
				closeButton.setEnabled(true);
				isUploading = false;
			}
			
			@Override
			public void onUploadCanceled() {
				closeButton.setEnabled(true);
				isUploading = false;
			}
			
			@Override
			public void onUploadStarted() {
				closeButton.setEnabled(false);
				isUploading = true;
			}
		});
	}
	
	public ResourceUploadDialog() {
//		setLayout(new FitLayout());
		setHeaderVisible(true);
		setHeading(ResourceUploadLocalizedText.INSTANCE.uploadHeader());
		setBodyBorder(false);
		setResizable(true);
		setClosable(false);
		setModal(true);
		setSize("400px", "350px");
		addButton(getCloseButton());
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		// noop
	}
	
	@Override
	public void intializeBean() throws Exception {
		add(resourceUploadPanel);
	}
	
	public TextButton getCloseButton() {
		if (closeButton != null)
			return closeButton;
		
		closeButton = new TextButton(ResourceUploadLocalizedText.INSTANCE.close());
		closeButton.setIconAlign(IconAlign.TOP);
		closeButton.setScale(ButtonScale.LARGE);
		closeButton.setIcon(ResourceUploadResources.INSTANCE.delete());
		closeButton.addSelectHandler(event -> hide());
		
		return closeButton;
	}
	
	@Override
	public void hide() {
		if (isUploading)
			return;
		
		super.hide();
	}

	@Override
	public void setEntityValue(Resource entityValue) {
		// noop
	}

	@Override
	public void performManipulations() {
		// noop
	}

	@Override
	public boolean hasChanges() {
		return false;
	}

	@Override
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		// NOP		
	}

}
