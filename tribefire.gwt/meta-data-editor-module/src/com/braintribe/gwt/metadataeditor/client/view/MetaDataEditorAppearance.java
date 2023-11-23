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
package com.braintribe.gwt.metadataeditor.client.view;

import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedListViewDefaultResources;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.theme.base.client.listview.ListViewDefaultAppearance.ListViewDefaultStyle;

public class MetaDataEditorAppearance extends ListViewCustomAppearance<MetaDataEditorModel> {

	protected static final ListViewDefaultStyle style = MetaDataEditorResources.INSTANCE.listViewDefaultStyle();

	public MetaDataEditorAppearance() {
		super("." + style.item(), style.over(), style.sel());
	}

	@Override
	public void render(SafeHtmlBuilder builder) {
		builder.appendHtmlConstant("<div class=\"" + style.view() + "\" x-type=\"mde-view\"></div>");
	}

	@Override
	public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
		builder.appendHtmlConstant("<div class=\"" + style.item() + " " + ExtendedListViewDefaultResources.GME_LIST_VIEW_ITEM + "\" x-type=\"mde-item\">");
		builder.append(content);
		builder.appendHtmlConstant("</div>");
	}
	
	@Override
	public void onSelect(XElement item, boolean select) {
		super.onSelect(item, select);
		item.setClassName(ExtendedListViewDefaultResources.GME_LIST_VIEW_SEL, select);
	}
}
