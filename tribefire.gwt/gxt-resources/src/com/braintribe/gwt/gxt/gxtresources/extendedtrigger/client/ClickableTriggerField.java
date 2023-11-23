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
package com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client;

import com.braintribe.gwt.gxt.gxtresources.css.GxtResources;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.widget.core.client.form.TriggerField;

/**
 * Interface for fields which provides an icon for hovering effect.
 * @author michel.docouto
 *
 */
public interface ClickableTriggerField {
	
	default public ImageResource getImageResource() {
		return GxtResources.INSTANCE.edit();
	}
	
	public TriggerField<?> getTriggerField();
	
	public void fireTriggerClick(NativeEvent event);
	
	public void setHideTrigger(boolean hideTrigger);

}
