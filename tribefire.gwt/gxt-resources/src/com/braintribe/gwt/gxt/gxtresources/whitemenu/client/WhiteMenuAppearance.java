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
package com.braintribe.gwt.gxt.gxtresources.whitemenu.client;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.theme.blue.client.menu.BlueMenuAppearance;

public class WhiteMenuAppearance extends BlueMenuAppearance {
	
	public interface WhiteMenuStyle extends BlueMenuStyle {
		//NOP
    }
	
	public interface whiteMenuResources extends BlueMenuResources {
		
        @Override
        @Source({"com/sencha/gxt/theme/base/client/menu/Menu.gss", "com/sencha/gxt/theme/blue/client/menu/BlueMenu.gss", "WhiteMenu.gss"})
        WhiteMenuStyle style();
		
    }
	
	public WhiteMenuAppearance() {
		super(GWT.<whiteMenuResources>create(whiteMenuResources.class), GWT.<BaseMenuTemplate> create(ExtendedBaseMenuTemplate.class));
	}

}
