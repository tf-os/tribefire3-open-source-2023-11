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

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.cell.core.client.ButtonCell;
import com.sencha.gxt.cell.core.client.SplitButtonCell;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.theme.base.client.button.ButtonCellDefaultAppearance;
import com.sencha.gxt.theme.base.client.frame.Frame;
import com.sencha.gxt.theme.base.client.frame.TableFrame;

public class SplitArrowUpWhiteButtonCellAppearance<C> extends ButtonCellDefaultAppearance<C> {
	
	public interface WhiteButtonCellStyle extends ButtonCellStyle {
		//NOP
	}
	
	public interface WhiteButtonCellResources extends ButtonCellResources {
		
		@Override
		@Source({"com/sencha/gxt/theme/base/client/button/ButtonCell.gss", "WhiteButtonCell.gss", "SplitArrowUpWhiteButtonCell.gss"})
		WhiteButtonCellStyle style();
		
		@Override
		@Source("com/braintribe/gwt/gxt/gxtresources/images/splitUp.gif")
		ImageResource split();
		
	}
	
	public SplitArrowUpWhiteButtonCellAppearance() {
		super(GWT.<WhiteButtonCellResources>create(WhiteButtonCellResources.class), GWT.<ButtonCellTemplates> create(ButtonCellTemplates.class),
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
		builder.append(templates.textWithStyles(style.text() + " gmeSplitArrowUpButtonText", sb.toSafeStyles(), value));
	}
	
	/*
	 * Overriding this to export a new split style.
	 */
	@Override
	public void render(ButtonCell<C> cell, Context context, C value, SafeHtmlBuilder sb) {
		String constantHtml = cell.getHTML();
		boolean hasConstantHtml = constantHtml != null && constantHtml.length() != 0;
		boolean isBoolean = value != null && value instanceof Boolean;
		// is a boolean always a toggle button?
		SafeHtml valueHtml = SafeHtmlUtils.fromTrustedString(
				hasConstantHtml ? cell.getText() : (value != null && !isBoolean) ? SafeHtmlUtils.htmlEscape(value.toString()) : "");

		ImageResource icon = cell.getIcon();
		IconAlign iconAlign = cell.getIconAlign();

		String cls = style.button();
		String arrowCls = "";
		if (cell.getMenu() != null) {
			if (cell instanceof SplitButtonCell) {
				switch (cell.getArrowAlign()) {
					case RIGHT:
						//This is the only change here
						arrowCls = style.split() + " gmeSplitArrowUpButtonSplit";
						break;
					case BOTTOM:
						arrowCls = style.splitBottom();
						break;
					default:
						// empty
				}
			} else {
				switch (cell.getArrowAlign()) {
					case RIGHT:
						arrowCls = style.arrow();
						break;
					case BOTTOM:
						arrowCls = style.arrowBottom();
						break;
				}
			}
		}

		ButtonScale scale = cell.getScale();

		switch (scale) {
			case SMALL:
				cls += " " + style.small();
				break;
			case MEDIUM:
				cls += " " + style.medium();
				break;
			case LARGE:
				cls += " " + style.large();
				break;
			default:
				// empty
		}

		SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();

		int width = -1;

		if (cell.getWidth() != -1) {
			int w = cell.getWidth();
			if (w < cell.getMinWidth())
				w = cell.getMinWidth();
			stylesBuilder.appendTrustedString("width:" + w + "px;");
			cls += " " + style.hasWidth() + " x-has-width";
			width = w;
		} else {
			if (cell.getMinWidth() != -1) {
				TextMetrics.get().bind(style.text());
				int length = TextMetrics.get().getWidth(valueHtml);
				length += 6; // frames

				if (icon != null) {
					switch (iconAlign) {
						case LEFT:
						case RIGHT:
							length += icon.getWidth();
							break;
						default:
							// empty
					}
				}

				if (cell.getMinWidth() > length) {
					stylesBuilder.appendTrustedString("width:" + cell.getMinWidth() + "px;");
					cls += " " + style.hasWidth() + " x-has-width";
					width = cell.getMinWidth();
				}
			}
		}

		final int height = cell.getHeight();
		if (height != -1)
			stylesBuilder.appendTrustedString("height:" + height + "px;");

		if (icon != null) {
			switch (iconAlign) {
				case TOP:
					arrowCls += " " + style.iconTop();
					break;
				case BOTTOM:
					arrowCls += " " + style.iconBottom();
					break;
				case LEFT:
					arrowCls += " " + style.iconLeft();
					break;
				case RIGHT:
					arrowCls += " " + style.iconRight();
					break;
			}
		} else
			arrowCls += " " + style.noIcon();

		// toggle button
		if (value == Boolean.TRUE)
			cls += " " + frame.pressedClass();

		sb.append(templates.outer(cls, new SafeStylesBuilder().toSafeStyles()));

		SafeHtmlBuilder inside = new SafeHtmlBuilder();

		String innerWrap = arrowCls;

		inside.appendHtmlConstant("<div class='" + innerWrap + "'>");
		inside.appendHtmlConstant("<table cellpadding=0 cellspacing=0 class='" + style.mainTable() + "'>");

		boolean hasText = valueHtml != null && !valueHtml.asString().isEmpty();

		if (icon != null) {
			switch (iconAlign) {
				case LEFT:
					inside.appendHtmlConstant("<tr>");
					writeIcon(inside, icon, height);
					if (hasText) {
						int w = width - icon.getWidth() - 4;
						writeValue(inside, valueHtml, w, height);
					}
					inside.appendHtmlConstant("</tr>");
					break;
				case RIGHT:
					inside.appendHtmlConstant("<tr>");
					if (hasText) {
						int w = width - icon.getWidth() - 4;
						writeValue(inside, valueHtml, w, height);
					}
					writeIcon(inside, icon, height);
					inside.appendHtmlConstant("</tr>");
					break;
				case TOP:
					inside.appendHtmlConstant("<tr>");
					writeIcon(inside, icon, height);
					inside.appendHtmlConstant("</tr>");
					if (hasText) {
						inside.appendHtmlConstant("<tr>");
						writeValue(inside, valueHtml, width, height);
						inside.appendHtmlConstant("</tr>");
					}
					break;
				case BOTTOM:
					if (hasText) {
						inside.appendHtmlConstant("<tr>");
						writeValue(inside, valueHtml, width, height);
						inside.appendHtmlConstant("</tr>");
					}
					inside.appendHtmlConstant("<tr>");
					writeIcon(inside, icon, height);
					inside.appendHtmlConstant("</tr>");
					break;
			}

		} else {
			inside.appendHtmlConstant("<tr>");
			if (valueHtml != null) {
				writeValue(inside, valueHtml, width, height);
			}
			inside.appendHtmlConstant("</tr>");
		}
		inside.appendHtmlConstant("</table>");
		inside.appendHtmlConstant("</div>");

		frame.render(sb, new Frame.FrameOptions(0, CommonStyles.get().noFocusOutline(), stylesBuilder.toSafeStyles()), inside.toSafeHtml());

		sb.appendHtmlConstant("</div>");
	}
	
	/*
	 * Overriding to export a new sytle.
	 */
	@Override
	public void onOver(XElement parent, boolean over) {
		super.onOver(parent, over);
		parent.setClassName("gmeSplitArrowUpButtonOver", over);
	}
	
	private native int getHeightOffset() /*-{
		return this.@com.sencha.gxt.theme.base.client.button.ButtonCellDefaultAppearance::heightOffset;
	}-*/;

}
