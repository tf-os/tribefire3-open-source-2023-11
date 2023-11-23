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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.cell.core.client.SplitButtonCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

/**
 * Fixed a problem that the ArrowSelectEvent was fired even when the button had no menu configured.
 * @author michel.docouto
 *
 */
public class FixedSplitButtonCell extends SplitButtonCell {
	
	public FixedSplitButtonCell() {
		super();
	}
	
	public FixedSplitButtonCell(ButtonCellAppearance<String> appearance) {
		super(appearance);
	}
	
	@Override
	protected void onClick(Context context, XElement p, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
		if (menu != null)
			super.onClick(context, p, value, event, valueUpdater);
		else
			fixedOnClick(context, event);
	}
	
	protected void fixedOnClick(Context context, NativeEvent event) {
		event.preventDefault();

		if (!isDisableEvents() && fireCancellableEvent(context, new BeforeSelectEvent(context)))
			fireEvent(context, new SelectEvent(context));
	}

}
