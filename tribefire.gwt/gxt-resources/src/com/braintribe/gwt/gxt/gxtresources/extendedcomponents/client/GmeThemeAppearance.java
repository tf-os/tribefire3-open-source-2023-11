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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sencha.gxt.core.client.resources.StyleInjectorHelper;
import com.sencha.gxt.core.client.resources.ThemeStyles.Styles;
import com.sencha.gxt.core.client.resources.ThemeStyles.ThemeAppearance;

/**
 * {@link ThemeAppearance} for extending GXT Blue theme.
 * @author michel.docouto
 *
 */
public class GmeThemeAppearance implements ThemeAppearance {

	static interface Bundle extends ClientBundle {

		@Source({ "com/sencha/gxt/theme/base/client/BaseTheme.gss", "com/sencha/gxt/theme/blue/client/BlueTheme.gss", "gmeTheme.gss" })
		GmeStyles css();
	}

	interface GmeStyles extends Styles {
		String borderColor();

		String borderColorLight();

		String backgroundColorLight();
	}

	private Bundle bundle;
	private GmeStyles style;

	@Override
	public Styles style() {
		return style;
	}

	public GmeThemeAppearance() {
	    this.bundle = GWT.create(Bundle.class);
	    this.style = bundle.css();

	    StyleInjectorHelper.ensureInjected(this.style, true);
	  }

	@Override
	public String borderColor() {
		return style.borderColor();
	}

	@Override
	public String borderColorLight() {
		return style.borderColorLight();
	}

	@Override
	public String backgroundColorLight() {
		return style.backgroundColorLight();
	}

}
