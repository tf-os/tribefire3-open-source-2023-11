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
package com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client;

import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringCell.ExtendedStringCellAppearance;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.FieldCell.FieldAppearanceOptions;
import com.sencha.gxt.theme.base.client.field.TriggerFieldDefaultAppearance;

public class ExtendedStringCellDefaultAppearance extends TriggerFieldDefaultAppearance implements ExtendedStringCellAppearance {
	
	public interface ExtendedStringCellResources extends TriggerFieldResources {
		
		@Override
		@Source({ "com/sencha/gxt/theme/base/client/field/ValueBaseField.gss", "com/sencha/gxt/theme/base/client/field/TextField.gss",
				"com/braintribe/gwt/gxt/gxtresources/flattextfield/client/FlatTextField.gss", "com/sencha/gxt/theme/base/client/field/TriggerField.gss" })
		ExtendedStringCellStyle css();

		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/addTriggerArrow.png")
		ImageResource triggerArrow();

		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/addTriggerArrowOver.png")
		ImageResource triggerArrowOver();

		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/addTriggerArrowClick.png")
		ImageResource triggerArrowClick();

		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/addTriggerArrowFocus.png")
		ImageResource triggerArrowFocus();
	}
	
	public interface ExtendedStringCellStyle extends TriggerFieldStyle {
		//NOP
	}

	public ExtendedStringCellDefaultAppearance() {
		this(GWT.<ExtendedStringCellResources> create(ExtendedStringCellResources.class));
	}

	public ExtendedStringCellDefaultAppearance(ExtendedStringCellResources resources) {
		super(resources);
	}
	
	/**
	 * Extended to export the trigger style
	 */
	@Override
	public void render(SafeHtmlBuilder sb, String value, FieldAppearanceOptions options) {
		int width = options.getWidth();
		boolean hideTrigger = options.isHideTrigger();

		if (width == -1) {
			width = 150;
		}

		String wrapStyles = "width:" + width + "px;";

		// 6px margin, 2px border
		width -= 8;

		if (!hideTrigger) {
			width -= getResources().triggerArrow().getWidth();
		}
		width = Math.max(0, width);
		SafeStyles inputStyles = SafeStylesUtils.fromTrustedString("width:" + width + "px;");

		sb.appendHtmlConstant("<div style='" + wrapStyles + "' class='" + getStyle().wrap() + "'>");

		if (!hideTrigger) {
			sb.appendHtmlConstant("<table cellpadding=0 cellspacing=0><tr><td class='gxtReset'>");
			renderInput(sb, value, inputStyles, options);
			sb.appendHtmlConstant("</td>");
			sb.appendHtmlConstant("<td class='gxtReset'><div class='" + getStyle().trigger() + " gmeExtendedTrigger'/></td>");
			sb.appendHtmlConstant("</table>");
		} else {
			renderInput(sb, value, inputStyles, options);
		}

		sb.appendHtmlConstant("</div>");
	}

}
