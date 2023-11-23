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

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HasMargins;

/**
 * Extends the {@link BorderLayoutContainer} by exposing the doLayout.
 * @author michel.docouto
 *
 */
public class ExtendedBorderLayoutContainer extends BorderLayoutContainer {

	@Override
	public void doLayout() {
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if (w instanceof ContentPanel && w.getLayoutData() instanceof BorderLayoutData) {
				BorderLayoutData data = (BorderLayoutData) w.getLayoutData();
				if (getRegion((Component) w) != LayoutRegion.CENTER) {
					if (data.isCollapsed())
						switchPanels((ContentPanel) w);
				}
			}
		}

		XElement containerTarget = getContainerTarget();

		Size size = containerTarget.getStyleSize();

		int w = size.getWidth();
		int h = size.getHeight();

		String paddingLeft = containerTarget.getStyle().getPaddingLeft();
		int sLeft = 0;
		if (paddingLeft != null && !paddingLeft.isEmpty())
			sLeft = containerTarget.getPadding(Side.LEFT); //somehow using this takes much more time than getting it from style

		String paddingTop = containerTarget.getStyle().getPaddingTop();
		int sTop = 0;
		if (paddingTop != null && !paddingTop.isEmpty())
			sTop = containerTarget.getPadding(Side.TOP);

		int centerW = w, centerH = h, centerY = 0, centerX = 0;

		if (north != null) {
			BorderLayoutData data = getLayoutData(north);
			north.setVisible(!data.isHidden());
			if (north.isVisible()) {
				Rectangle b = new Rectangle();
				Margins m = data.getMargins() != null ? data.getMargins() : new Margins();
				double s = data.getSize() <= 1 ? data.getSize() * size.getHeight() : data.getSize();
				b.setHeight((int) s);
				b.setWidth(w - (m.getLeft() + m.getRight()));
				b.setX(m.getLeft());
				b.setY(m.getTop());
				centerY = b.getHeight() + b.getY() + m.getBottom();
				centerH -= centerY;
				b.setX(b.getX() + sLeft);
				b.setY(b.getY() + sTop);
				applyLayout(north, b);
			}
		}
		
		if (south != null) {
			BorderLayoutData data = getLayoutData(south);
			south.setVisible(!data.isHidden());
			if (south.isVisible()) {
				Rectangle b = new Rectangle();
				Margins m = data.getMargins() != null ? data.getMargins() : new Margins();
				double s = data.getSize() <= 1 ? data.getSize() * size.getHeight() : data.getSize();
				b.setHeight((int) s);

				b.setWidth(w - (m.getLeft() + m.getRight()));
				b.setX(m.getLeft());

				int totalHeight = (b.getHeight() + m.getTop() + m.getBottom());
				b.setY(h - totalHeight + m.getTop());
				centerH -= totalHeight;

				b.setX(b.getX() + sLeft);
				b.setY(b.getY() + sTop);

				applyLayout(south, b);
			}
		}

		if (west != null) {
			BorderLayoutData data = getLayoutData(west);
			west.setVisible(!data.isHidden());
			if (west.isVisible()) {
				Rectangle box = new Rectangle();
				Margins m = data.getMargins() != null ? data.getMargins() : new Margins();
				double s = data.getSize() <= 1 ? data.getSize() * size.getWidth() : data.getSize();
				box.setWidth((int) s);
				box.setHeight(centerH - (m.getTop() + m.getBottom()));
				box.setX(m.getLeft());
				box.setY(centerY + m.getTop());
				int totalWidth = (box.getWidth() + m.getLeft() + m.getRight());
				centerX += totalWidth;
				centerW -= totalWidth;
				box.setX(box.getX() + sLeft);
				box.setY(box.getY() + sTop);
				applyLayout(west, box);
			}
		}
		
		if (east != null) {
			BorderLayoutData data = getLayoutData(east);
			east.setVisible(!data.isHidden());
			if (east.isVisible()) {
				Rectangle b = new Rectangle();
				Margins m = data.getMargins() != null ? data.getMargins() : new Margins();
				double s = data.getSize() <= 1 ? data.getSize() * size.getWidth() : data.getSize();
				b.setWidth((int) s);
				b.setHeight(centerH - (m.getTop() + m.getBottom()));
				int totalWidth = (b.getWidth() + m.getLeft() + m.getRight());
				b.setX(w - totalWidth + m.getLeft());
				b.setY(centerY + m.getTop());
				centerW -= totalWidth;
				b.setX(b.getX() + sLeft);
				b.setY(b.getY() + sTop);
				applyLayout(east, b);
			}
		}

		if (widget != null) {
			Object data = widget.getLayoutData();
			Margins m = null;
			if (data instanceof HasMargins)
				m = ((HasMargins) data).getMargins();
			if (m == null)
				m = new Margins(0);
			Rectangle lastCenter = new Rectangle(centerX, centerY, centerW, centerH);
			lastCenter.setX(centerX + (m.getLeft() + sLeft));
			lastCenter.setY(centerY + (m.getTop() + sTop));
			lastCenter.setWidth(centerW - (m.getLeft() + m.getRight()));
			lastCenter.setHeight(centerH - (m.getTop() + m.getBottom()));
			setLastCenter(lastCenter);
			applyLayout(widget, lastCenter);
		}
	}
	
	private BorderLayoutData getLayoutData(Widget w) {
		Object o = w.getLayoutData();
		return (BorderLayoutData) ((o instanceof BorderLayoutData) ? o : new BorderLayoutData(100));
	}

	private native void switchPanels(ContentPanel panel) /*-{
		this.@com.sencha.gxt.widget.core.client.container.BorderLayoutContainer::switchPanels(Lcom/sencha/gxt/widget/core/client/ContentPanel;)(panel);
	}-*/;

	private native void setLastCenter(Rectangle rectangle) /*-{
		this.@com.sencha.gxt.widget.core.client.container.BorderLayoutContainer::lastCenter = rectangle;
	}-*/;

}
