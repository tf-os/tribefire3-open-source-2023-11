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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.PasswordInputCell;
import com.sencha.gxt.cell.core.client.form.ViewData;

/**
 * Added attributes for not showing auto completion suggestions for password fields.
 * @author michel.docouto
 *
 */
public class FixedPasswordInputCell extends PasswordInputCell {
	
	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		ViewData viewData = checkViewData(context, value);
	    String s = (viewData != null) ? viewData.getCurrentValue() : value;

	    FieldAppearanceOptions options = new FieldAppearanceOptions(getWidth(), getHeight(), isReadOnly(), getEmptyText());
	    options.setName("password'  autocomplete='new-password"); //hack to add the parameter
	    options.setEmptyText(getEmptyText());
	    options.setDisabled(isDisabled());
	    getAppearance().render(sb, "password", s == null ? "" : s, options);
	}

}
