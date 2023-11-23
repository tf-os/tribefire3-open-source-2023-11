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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.ioc.client.Required;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provider used for getting the current {@link GmContentView} within the {@link ExplorerConstellation}
 * @author michel.docouto
 *
 */
public class CurrentContentViewProvider implements Supplier<GmContentView> {
	
	private ExplorerConstellation explorerConstellation;
	
	/**
	 * Configures the required {@link ExplorerConstellation} for checking which is the current view.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Override
	public GmContentView get() throws RuntimeException {
		VerticalTabElement element = explorerConstellation.getVerticalTabPanel().getSelectedElement();
		Widget widget = element.getWidget();
		
		if (widget instanceof GmContentView)
			return (GmContentView) widget;
		
		return null;
	}

}
