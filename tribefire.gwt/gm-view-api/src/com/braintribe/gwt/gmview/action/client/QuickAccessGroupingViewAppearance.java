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
package com.braintribe.gwt.gmview.action.client;

import com.braintribe.gwt.gxt.gxtresources.flatgroupingview.client.GroupingViewFlatAppearance.FlatGroupingViewStyle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.theme.base.client.grid.GroupingViewDefaultAppearance;
import com.sencha.gxt.widget.core.client.grid.GridView.GridStateStyles;
import com.sencha.gxt.widget.core.client.grid.GroupingView.GroupingData;

public class QuickAccessGroupingViewAppearance extends GroupingViewDefaultAppearance {
	
	public interface QuickAccessHeaderTemplate extends XTemplates, GroupHeaderTemplate<Object> {
		@Override
		@XTemplate("<div class='propertyGroupRuler'>{groupInfo.value}</div>")
		public SafeHtml renderGroupHeader(GroupingData<Object> groupInfo);
	}
	
	private GroupHeaderTemplate<?> headerTemplate = GWT.create(QuickAccessHeaderTemplate.class);
	
	public interface QuickAccessFlatGroupingViewStyle extends FlatGroupingViewStyle {
		String quickAccessPanel();
	}
	
	public interface QuickAccessGroupingViewFlatResources extends GroupingViewResources {

		@Import(GridStateStyles.class)
		@Override
		@Source({"com/sencha/gxt/theme/base/client/grid/GroupingView.gss", "QuickAccessFlatGroupingView.gss"})
		QuickAccessFlatGroupingViewStyle style();
	}
	
	public QuickAccessGroupingViewAppearance() {
		super(GWT.<QuickAccessGroupingViewFlatResources>create(QuickAccessGroupingViewFlatResources.class));
		setHeaderTemplate(headerTemplate);
	}

}
