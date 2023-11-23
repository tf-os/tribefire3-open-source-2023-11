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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.dom.client.Element;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@SuppressWarnings("unusable-by-js")
@JsType(namespace = InteropConstants.VIEW_NAMESPACE)
public interface GmSelectionSupport {
	
	public void addSelectionListener(GmSelectionListener  sl);

    public void removeSelectionListener(GmSelectionListener  sl);

    public ModelPath getFirstSelectedItem();

    public List<ModelPath> getCurrentSelection();

    public boolean isSelected(Object element);
    
    public void select(int index, boolean keepExisting);
    
    @JsIgnore
    public GmContentView getView();
    
    /**
     * Selected the element if found.
     * @param element - the element to be selected
     * @param keepExisting - true for keeping the existing selected entries
     */
    @JsIgnore
    public default boolean select(Element element, boolean keepExisting) {
    	return false;
    }

    /**
     * Selected the element on Horizontal row.
     * @param next - true for next element, false for previous element
     * @param keepExisting - true for keeping the existing selected entries
     */
    @JsMethod (name = "selectHorizontal")
    public default boolean selectHorizontal(Boolean next, boolean keepExisting) {
    	return false;
    }    

    /**
     * Selected the element on Vertical column.
     * @param next - true for next element, false for previous element
     * @param keepExisting - true for keeping the existing selected entries
     */
    @JsMethod (name = "selectVertical")
    public default boolean selectVertical(Boolean next, boolean keepExisting) {
    	return false;
    }    
    
    /**
     * This must be overridden by views which are GmTreeView.
     */
    public default void selectRoot(int index, boolean keepExisting) {
    	select(index, keepExisting);
    }
    
    public default List<List<ModelPath>> transformSelection(List<ModelPath> selection) {
    	if (selection == null)
    		return null;
    	
		List<List<ModelPath>> list = newList();
		
		for (ModelPath modelPath : selection) {
			List<ModelPath> singleList = newList();
			singleList.add(modelPath);
			list.add(singleList);
		}
		
		return list;
	}
    
    public default int getFirstSelectedIndex() {
    	return -1;
    }
    
    /**
     * This must be implemented for every view interested in supporting the deselection
     */
    public default void deselectAll() {
    	//NOP
    }

}
