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
package com.braintribe.gwt.gxt.gxtresources.whitebutton.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.base.client.button.ButtonCellDefaultAppearance;
import com.sencha.gxt.theme.base.client.frame.TableFrame;

public class WhiteButtonCellAppearance<C> extends ButtonCellDefaultAppearance<C> {
	
	public interface WhiteButtonCellStyle extends ButtonCellStyle {
		//NOP
	}
	
	public interface WhteButtonCellResources extends ButtonCellResources {
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/button/ButtonCell.gss", "WhiteButtonCell.gss"})
		WhiteButtonCellStyle style();
		
		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/arrow.png")
		ImageResource arrow();
		
	}
	
	public WhiteButtonCellAppearance() {
		super(GWT.<WhteButtonCellResources>create(WhteButtonCellResources.class), GWT.<ButtonCellTemplates> create(ButtonCellTemplates.class),
				new TableFrame(GWT.<WhiteButtonTableFrameResources> create(WhiteButtonTableFrameResources.class)));
	}
	
	/*
	 * Overriding this to export a new text style.
	 */
	@Override
	protected void writeValue(SafeHtmlBuilder builder, SafeHtml value, int width, int height) {
		SafeStylesBuilder sb = new SafeStylesBuilder();
		if (height > 0) {
			int adjustedHeight = height - getHeightOffset();
			sb.append(SafeStylesUtils.fromTrustedString("height:" + adjustedHeight + "px;"));
		}
		if (width > 0)
			sb.append(SafeStylesUtils.fromTrustedString("width:" + width + "px;"));
		//This is the only change
		builder.append(templates.textWithStyles(style.text() + " gmeButtonText", sb.toSafeStyles(), value));
	}
	
	/*
	 * Overriding this to export a new icon style. 
	 */
	@Override
	protected void writeIcon(SafeHtmlBuilder builder, ImageResource icon, int height) {
		SafeHtml iconHtml = AbstractImagePrototype.create(icon).getSafeHtml();
		if (height == -1) {
			builder.append(templates.icon(style.iconWrap() + " gmeButtonIcon", iconHtml));
		} else {
			int adjustedHeight = height - getHeightOffset();
			SafeStyles heightStyle = SafeStylesUtils.fromTrustedString("height:" + adjustedHeight + "px;");
			builder.append(templates.iconWithStyles(style.iconWrap() + " gmeButtonIcon", heightStyle, iconHtml));
		}
	}
	
	/*
	 * Overriding to export a new sytle.
	 */
	@Override
	public void onOver(XElement parent, boolean over) {
		super.onOver(parent, over);
		if (frame instanceof TableFrame) {
			XElement contentAreaElement = parent.child("." + ((TableFrame) frame).getResources().style().contentArea());
			if (contentAreaElement != null)
				contentAreaElement.setClassName("gmeButtonOver", over);
		}
	}
	
	private native int getHeightOffset() /*-{
		return this.@com.sencha.gxt.theme.base.client.button.ButtonCellDefaultAppearance::heightOffset;
	}-*/;

}
