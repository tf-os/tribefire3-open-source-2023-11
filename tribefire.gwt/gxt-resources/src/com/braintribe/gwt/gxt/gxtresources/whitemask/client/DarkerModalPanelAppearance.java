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
package com.braintribe.gwt.gxt.gxtresources.whitemask.client;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.widget.core.client.ModalPanel.ModalPanelDefaultAppearance;

public class DarkerModalPanelAppearance extends ModalPanelDefaultAppearance {
	
	public interface DarkerModalPanelResources extends ModalPanelResources {
        @Override
        @Source({"com/sencha/gxt/widget/core/client/ModalPanel.gss", "DarkerModalPanel.gss"})
        ModalPanelStyle css();
    } 
	
	public DarkerModalPanelAppearance() {
		super(GWT.create(DarkerModalPanelResources.class));
	}

}
