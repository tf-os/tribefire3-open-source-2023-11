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
package com.braintribe.gwt.gxt.gxtresources.flatgroupingview.client;

import org.vectomatic.dom.svg.ui.SVGResource;
import org.vectomatic.dom.svg.ui.SVGResource.Validated;

import com.google.gwt.resources.client.CssResource.Import;
import com.sencha.gxt.theme.base.client.grid.GroupingViewDefaultAppearance;
import com.sencha.gxt.widget.core.client.grid.GridView.GridStateStyles;

public class GroupingViewFlatAppearance extends GroupingViewDefaultAppearance {
	
	public interface FlatGroupingViewStyle extends GroupingViewStyle {
		//NOP
	}
	
	public interface GroupingViewFlatResources extends GroupingViewResources {

		@Source ("com/braintribe/gwt/gxt/gxtresources/images/arrow-up.svg") @Validated(validated = false)
		SVGResource arrowUp();
		
		@Source ("com/braintribe/gwt/gxt/gxtresources/images/arrow-down.svg") @Validated(validated = false)
		SVGResource arrowDown();
		
		@Source ("com/braintribe/gwt/gxt/gxtresources/images/arrow-right.svg") @Validated(validated = false)
		SVGResource arrowRight();
		
		@Import(GridStateStyles.class)
		@Override
		@Source({"com/sencha/gxt/theme/base/client/grid/GroupingView.gss", "FlatGroupingView.gss"})
		FlatGroupingViewStyle style();
	}

}
