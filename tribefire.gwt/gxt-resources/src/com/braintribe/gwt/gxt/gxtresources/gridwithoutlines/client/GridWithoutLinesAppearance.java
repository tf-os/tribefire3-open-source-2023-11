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
package com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.Import;
import com.sencha.gxt.theme.blue.client.grid.BlueGridAppearance;
import com.sencha.gxt.widget.core.client.grid.GridView.GridStateStyles;

public class GridWithoutLinesAppearance extends BlueGridAppearance {
	
	public interface GridWithoutLinesStyle extends BlueGridStyle {
		String gridWithoutLines();
    }
	
	public interface GridWithoutLinesResources extends BlueGridResources {
		
		@Import(GridStateStyles.class)
        @Override
        @Source({"Grid.gss", "BlueGrid.gss", "GridWithoutLines.gss"})
        GridWithoutLinesStyle css();
		
    }
	
	public GridWithoutLinesAppearance() {
		this(GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class));
	}
	
	public GridWithoutLinesAppearance(GridWithoutLinesResources resources) {
		super(resources);
	}

}
