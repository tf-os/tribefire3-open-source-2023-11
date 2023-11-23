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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.DateTimeField;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ClickableTriggerField;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.widget.core.client.form.TriggerField;

public class ClickableDateTimeField extends DateTimeField implements ClickableTriggerField {
	
	public ClickableDateTimeField() {
		super(new ClickableDateCell());
	}
	
	@Override
	public ImageResource getImageResource() {
		return GMGxtSupportResources.INSTANCE.calendar();
	}

	@Override
	public TriggerField<?> getTriggerField() {
		return this;
	}
	
	@Override
	public void fireTriggerClick(NativeEvent event) {
		((ClickableDateCell) getCell()).onTriggerClick(createContext(), getElement(), event, getValue(), valueUpdater);
	}

}
