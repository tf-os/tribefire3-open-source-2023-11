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

import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.google.gwt.user.client.ui.Widget;

public class InstanceSelectionData {

	private Widget parentWidget;
	private List<GMTypeInstanceBean> selections;

	public InstanceSelectionData(Widget parentWidget, List<GMTypeInstanceBean> selections) {
		this.parentWidget = parentWidget;
		this.selections = selections;
	}

	public Widget getParentWidget() {
		return parentWidget;
	}

	public void setParentWidget(Widget parentWidget) {
		this.parentWidget = parentWidget;
	}

	public List<GMTypeInstanceBean> getSelections() {
		return selections;
	}

	public void setSelections(List<GMTypeInstanceBean> selections) {
		this.selections = selections;
	}
}