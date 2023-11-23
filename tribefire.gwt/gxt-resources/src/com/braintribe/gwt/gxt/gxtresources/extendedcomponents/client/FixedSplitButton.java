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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import com.braintribe.gwt.gxt.gxtresources.whitebutton.client.WhiteButtonTableFrameResources;
import com.google.gwt.core.client.GWT;
import com.sencha.gxt.widget.core.client.button.SplitButton;

/**
 * This implementation of the SplitButton is prepared for being disabled. It won't have the square border around it.
 * SplitButtons that need to be disabled, must use this implementation. The reason is, GXT's BlueStyles is not public.
 * Also, the click on the arrow area has been fixed. It is treated as a normal click if the menu is null.
 * @author michel.docouto
 *
 */
public class FixedSplitButton extends SplitButton {
	
	public FixedSplitButton() {
		super(new FixedSplitButtonCell());
		disabledStyle = ((WhiteButtonTableFrameResources) GWT.create(WhiteButtonTableFrameResources.class)).style().disabledStyle();
	}
	
	public FixedSplitButton(String text) {
		super(new FixedSplitButtonCell(), text);
		disabledStyle = ((WhiteButtonTableFrameResources) GWT.create(WhiteButtonTableFrameResources.class)).style().disabledStyle();
	}
	
	public FixedSplitButton(FixedSplitButtonCell cell) {
		super(cell);
		disabledStyle = ((WhiteButtonTableFrameResources) GWT.create(WhiteButtonTableFrameResources.class)).style().disabledStyle();
	}

}
