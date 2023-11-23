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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.model.generic.path.ModelPathElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for special fields that are actually panels which should be rendered together with the properties.
 * It is displayed in a line line bellow the actual property, or inlined.
 * @author michel.docouto
 *
 */
public interface ExtendedInlineField {
	
	boolean isAvailable(ModelPathElement modelPath);
	boolean isAvailableInline(ModelPathElement modelPath);
	Supplier<Widget> getWidgetSupplier();
	void prepareInlineElement(Element element);
	void configurePropertyPanel(PropertyPanel propertyPanel);

}
