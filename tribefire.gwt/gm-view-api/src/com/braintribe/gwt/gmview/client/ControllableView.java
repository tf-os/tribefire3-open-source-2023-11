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

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface which must be implemented by dialogs which contain toolBars with buttons that can be all disabled.
 * This is used to have a better mask option, other than just masking the whole thing.
 *
 */
public interface ControllableView {
	
	public void enableComponents();
	
	public void disableComponents();
	
	public static ControllableView getParentControllableView(Widget widget) {
		if (widget instanceof ControllableView)
			return (ControllableView) widget;
		
		if (widget != null)
			return getParentControllableView(widget.getParent());
		
		return null;
	}

}
