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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.List;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.ControllableView;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.ExpertUIContext;
import com.braintribe.gwt.gmview.client.ResourceUploadView;
import com.braintribe.gwt.gmview.client.ResourceUploadViewListener;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.Resource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * {@link ExpertUI} implementation for the {@link Resource} entity type.
 * @author michel.docouto
 *
 */
public class ResourceExpertUI extends BorderLayoutContainer implements ExpertUI<Resource> {
	
	private LocalizedString localizedName;
	private Future<List<Resource>> future;
	private ResourceUploadView resourceUploadView;
	private ControllableView controllableView;
	
	public ResourceExpertUI() {
		//getElement().getStyle().setBackgroundColor("white");
		//setHeaderVisible(false);
		setBorders(false);
		//setBodyBorder(false);
		//TODO: set in the south, a panel that shows a list of all uploaded files?
	}
	
	/**
	 * Configures the required {@link ResourceUploadView} that is responsible for displaying the resource upload.
	 */
	@Required
	public void setResourceUploadView(final ResourceUploadView resourceUploadView) {
		this.resourceUploadView = resourceUploadView;
		this.setCenterWidget(resourceUploadView.getWidget());
		
		resourceUploadView.addResourceUploadViewListener(new ResourceUploadViewListener() {
			@Override
			public void onUploadStarted() {
				controllableView = ControllableView.getParentControllableView(ResourceExpertUI.this);
				if (controllableView != null)
					controllableView.disableComponents();
				else
					mask(LocalizedText.INSTANCE.uploading());
			}
			
			@Override
			public void onUploadFinished(List<Resource> resources) {
				if (controllableView != null)
					controllableView.enableComponents();
				else
					unmask();
				future.onSuccess(resources);
				resourceUploadView.clearUploads();
			}
			
			@Override
			public void onUploadCanceled() {
				if (controllableView != null)
					controllableView.enableComponents();
				else
					unmask();
			}
		});
	}

	@Override
	public Future<List<Resource>> getFuture(ExpertUIContext context) {
		resourceUploadView.setMaxAmountOfFiles(context.getMaxElementsToReturn());
		
		future = new Future<>();
		return future;
	}

	@Override
	public Widget getComponent() {
		return this;
	}

	@Override
	public LocalizedString getName() {
		if (localizedName == null) {
			localizedName = LocalizedString.T.createPlain();
			Map<String, String> localizedValues = new FastMap<>();
			localizedValues.put("default", LocalizedText.INSTANCE.resourcesProvider());
			localizedName.setLocalizedValues(localizedValues);
		}
		
		return localizedName;
	}

	@Override
	public String getTechnicalName() {
		return "Resources Expert";
	}

	@Override
	public LocalizedString getDescription() {
		return null;
	}
	
	@Override
	public ImageResource getImageResource() {
		return ConstellationResources.INSTANCE.upload();
	}
	
}
