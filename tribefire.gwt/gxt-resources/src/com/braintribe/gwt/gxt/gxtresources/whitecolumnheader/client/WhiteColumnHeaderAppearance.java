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
package com.braintribe.gwt.gxt.gxtresources.whitecolumnheader.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.theme.base.client.grid.ColumnHeaderDefaultAppearance;

public class WhiteColumnHeaderAppearance extends ColumnHeaderDefaultAppearance {
	
	public interface WhiteColumnHeaderStyles extends DefaultColumnHeaderStyles {
		//NOP
    }
	
	public interface whiteColumnHeaderResources extends ColumnHeaderResources {
        @Source("com/braintribe/gwt/gxt/gxtresources/images/blackMenu.png")
        ImageResource blackMenu();		
		
        @Override
        @Source({"com/sencha/gxt/theme/base/client/grid/ColumnHeader.gss", "WhiteColumnHeader.gss"})
        WhiteColumnHeaderStyles style();
		
    }
	
	public WhiteColumnHeaderAppearance() {
		super(GWT.<whiteColumnHeaderResources>create(whiteColumnHeaderResources.class));
	}

}
