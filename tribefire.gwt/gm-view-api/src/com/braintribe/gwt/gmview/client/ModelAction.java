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
package com.braintribe.gwt.gmview.client;

import java.util.List;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionPropertyHolder;
import com.braintribe.gwt.gmresourceapi.client.GmImageResourceListener;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.resources.client.ImageResource;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public abstract class ModelAction extends Action implements GmImageResourceListener {
	public static final String PROPERTY_POSITION = "position";
	public static final String PROPERTY_READONLY = "readOnly";
	
	protected List<List<ModelPath>> modelPaths;
	protected GmContentView gmContentView;
	
	public void configureGmContentView(GmContentView gmContentView) {
		this.gmContentView = gmContentView;
	}
	
	public void updateState(List<List<ModelPath>> modelPaths) {
		this.modelPaths = modelPaths;
		updateVisibility();
	}
	
	protected abstract void updateVisibility(/*SelectorContext selectorContext*/);
	
	@Override
	@JsIgnore
	public void onImageChanged(ImageResource imageResource) {		
		if (imageResource.getWidth() >= 0 && imageResource.getWidth() <= 8)
			put(PROPERTY_ICON, imageResource, true);
		else if(imageResource.getWidth() > 9 && imageResource.getWidth() <= 16)
			put(PROPERTY_ICON, imageResource, true);
		else if(imageResource.getWidth() > 17 && imageResource.getWidth() <= 32)
			put(PROPERTY_HOVERICON, imageResource, true);		
	}
	
	public static boolean actionBelongsToPosition(ActionPropertyHolder action, ModelActionPosition modelActionPosition) {
		Object position = action.get(ModelAction.PROPERTY_POSITION);
		if (position != null) {
			if (position instanceof ModelActionPosition) {
				return modelActionPosition.equals(position);
			} else if (position instanceof List) {
				for (Object object : (List<Object>) position) {
					if (modelActionPosition.equals(object))
						return true;
				}
			}
		}
		
		return false;
	}
}
