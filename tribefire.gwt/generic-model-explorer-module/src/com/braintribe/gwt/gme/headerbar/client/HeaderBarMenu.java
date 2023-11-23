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
package com.braintribe.gwt.gme.headerbar.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.htmlpanel.client.HtmlPanel;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;

public class HeaderBarMenu {
	private int itemSize = 2;
	private static final int ELEMENT_HEIGHT = 57;
	private static final int ELEMENT_WIDTH = 74;
	//private Element toolTipElement;
	private HtmlPanel viewHtmlPanel;
	private List<Action> actionList;
	
	public HeaderBarMenu() {
		this.actionList = new ArrayList<>();
	}

	public void setActionList(List<Action> actionList) {
		this.actionList = actionList;
	}
	
	public void addAction(Action action) {
		if (action != null)
			this.actionList.add(action);
	}
	
	public void setItemSize (int itemSize) {
		this.itemSize = itemSize;
	}
	
	public Window getMenuWindow() {
		final Window menuWindow = new Window();
		menuWindow.setClosable(false);
		menuWindow.setAutoHide(true);
		menuWindow.setShadow(true);
		menuWindow.setHeaderVisible(false);
		menuWindow.setResizable(false);
		menuWindow.setStyleName("Headerbar Link Menu");
		menuWindow.setBorders(true);
		
		//border: 1px solid rgb(153, 187, 232) !important
		menuWindow.show();
		
		HorizontalLayoutContainer layoutContainer = new HorizontalLayoutContainer();
		layoutContainer.setStyleName("HeaderBarLayoutLinkMenu");
		
		//boolean useToolTip = false;
		int panelsMaxHeight = 0;
		
		//VerticalLayoutContainer optionsView = null;
		//VerticalLayoutContainer condensationView = null;
		int condensationsContainerWidth = 0;
		int optionsContainerWidth = 0;
		/*boolean checkTop = false;
		
		if (checkTop) {
			if (toolTipElement != null)
				toolTipElement.getStyle().setTop(panelsMaxHeight + 2, Unit.PX);
		}*/
		
		int factor = this.itemSize == 0 ? 1 : this.itemSize % 3 == 0 ? this.itemSize / 3 : (this.itemSize / 3) + 1;
		int columns = this.itemSize <= 1 ? 0 : this.itemSize == 2 ? 2 : 3;
		if (this.itemSize > 1) {
			this.viewHtmlPanel = getViewHtmlPanel(menuWindow, this.actionList, factor, panelsMaxHeight);
			layoutContainer.setWidth(this.viewHtmlPanel.getOffsetWidth());
			layoutContainer.setHeight(this.viewHtmlPanel.getOffsetHeight());
			layoutContainer.add(this.viewHtmlPanel, new HorizontalLayoutData(columns * ELEMENT_WIDTH + 8, -1));
		} else
			this.viewHtmlPanel = null;
		
		
		/*if (optionsView != null) {
			HorizontalLayoutData optionsContainerData = new HorizontalLayoutData(optionsContainerWidth, panelsMaxHeight + 2, new Margins(0, 2, 0, 2));
			layoutContainer.add(optionsView, optionsContainerData);
		}
		
		if (condensationView != null) {
			HorizontalLayoutData condensationsContainerData = new HorizontalLayoutData(condensationsContainerWidth, panelsMaxHeight + 2, new Margins(0, 0, 0, 2));
			layoutContainer.add(condensationView, condensationsContainerData);
		}*/
		
		menuWindow.add(layoutContainer);
		menuWindow.setWidth((columns * ELEMENT_WIDTH) + 22 + optionsContainerWidth + condensationsContainerWidth);
		//menuWindow.setHeight(Math.max((factor * ELEMENT_HEIGHT) + 21, panelsMaxHeight + 17));
		menuWindow.setHeight(Math.max((factor * ELEMENT_HEIGHT) + 17, panelsMaxHeight + 17));
		return menuWindow;
	}

	private HtmlPanel getViewHtmlPanel(final Window menuWindow, final List<Action> actionList, int factor, int panelsMaxHeight) {
		HtmlPanel viewHtmlPanel = new HtmlPanel();
		viewHtmlPanel.setBorders(false);
		viewHtmlPanel.setBodyBorder(false);
		viewHtmlPanel.setStyleName("HeaderBarHtmlPanelLinkMenu");

		
		StringBuilder htmlString = new StringBuilder();

        //small arrow 
		/*
		htmlString.append("<div class='").append(AssemblyPanelResources.INSTANCE.css().toolTip()).append("' style='bottom: ");
		htmlString.append(Math.max((factor * ELEMENT_HEIGHT) + 7, panelsMaxHeight + 3));
		htmlString.append("px'></div>");
		*/
		
		/*
		htmlString.append("<div class='").append(AssemblyPanelResources.INSTANCE.css().toolBarParentStyle()).append("' style='height: ");
		htmlString.append(Math.max((factor * ELEMENT_HEIGHT) + 4, panelsMaxHeight)).append("px;'><ul class='").append(AssemblyPanelResources.INSTANCE.css().toolBarStyle());
		htmlString.append("'>");
		*/
		htmlString.append("<ul class='gxtReset ").append(ConstellationResources.INSTANCE.css().toolBarStyle());
		htmlString.append("'>");
		
		viewHtmlPanel.setHeight(Math.max((factor * ELEMENT_HEIGHT) + 13, panelsMaxHeight + 11));
		
		//int counter = 0;
		for (Action action : actionList) {
			htmlString.append("<li>");
			htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolBarElement());
			htmlString.append("' style='position: relative;'");
			//htmlString.append(" id='").append(contentViewContext.getName()).append("gmContentViewContext").append(idCounter++).append("'");
			htmlString.append(" id='").append(action.getName()).append("gmContentViewContext").append("'");
			htmlString.append("><img src='").append(action.getHoverIcon().getSafeUri().asString()).append("' class='");
			htmlString.append(ConstellationResources.INSTANCE.css().toolBarElementImage()).append("'/>");
			htmlString.append("<div class='").append(ConstellationResources.INSTANCE.css().toolBarElementText()).append("'");
			htmlString.append(">").append(action.getName()).append("</div></div>");
			htmlString.append("</li>");
			//counter++;
		}
		htmlString.append("</ul>");
		
		/*
		htmlString.append("</ul></div>");
		*/
		
		viewHtmlPanel.setHtml(htmlString.toString());
		viewHtmlPanel.clearWidgets();
		viewHtmlPanel.init();
		
		viewHtmlPanel.sinkEvents(Event.ONCLICK);
		viewHtmlPanel.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
			
			Element actionElement = getActionElement(Element.as(target), 3, ConstellationResources.INSTANCE.css().toolBarElement());
			if (actionElement != null) {
				for (Action action : actionList) {
					if (actionElement.getId().startsWith(action.getName())) {
						//Fire Action
						action.perform(null);
						menuWindow.hide();
						break;
					}
				}
			}
		}, ClickEvent.getType());
		
		return viewHtmlPanel;
	}
	
	private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth <= 0 || clickedElement == null)
			return null;
		
		if (className.equals(clickedElement.getClassName()))
			return clickedElement;
		
		return getActionElement(clickedElement.getParentElement(), --depth, className);
	}
	
}
