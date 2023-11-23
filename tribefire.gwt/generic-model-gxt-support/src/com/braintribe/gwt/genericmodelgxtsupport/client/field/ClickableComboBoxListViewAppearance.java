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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.theme.base.client.listview.ListViewDefaultAppearance;

/**
 * Extension of the {@link ListViewDefaultAppearance} for exporting a new class in the items.
 * @author michel.docouto
 */
public class ClickableComboBoxListViewAppearance<M> extends ListViewDefaultAppearance<M> {

	@Override
	public void renderItem(SafeHtmlBuilder sb, SafeHtml content) {
		sb.appendHtmlConstant("<div class='" + style.item() + " gmeComboItem'>");
		sb.append(content);
		sb.appendHtmlConstant("</div>");
	}
	
}
