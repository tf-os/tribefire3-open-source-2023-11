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
package com.braintribe.gwt.gmview.client.js.interop;

import java.util.List;

import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.GenericModelType;

import jsinterop.annotations.JsMethod;

/**
 * Interface used for exporting {@link GmListView} via JsInterop.
 */
@SuppressWarnings("unusable-by-js")
public interface GmListViewInterfaceInterop extends GmContentViewInterfaceInterop, GmListView {
	
	@Override
	@JsMethod
	public void configureTypeForCheck(GenericModelType typeForCheck);
	
	@Override
	@JsMethod
	public void addContent(ModelPath modelPath);
    
	@Override
	@JsMethod
    public List<ModelPath> getAddedModelPaths();

}
