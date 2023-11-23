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

import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.generic.path.ModelPath;

import jsinterop.annotations.JsType;

@SuppressWarnings("unusable-by-js")
@JsType(namespace = InteropConstants.VIEW_NAMESPACE)
public interface GmCheckSupport {
	
	public void addCheckListener(GmCheckListener cl);

    public void removeCheckListener(GmCheckListener cl);

    public ModelPath getFirstCheckedItem();

    public List<ModelPath> getCurrentCheckedItems();

    public boolean isChecked(Object element);
    
    public boolean uncheckAll();
    
}
