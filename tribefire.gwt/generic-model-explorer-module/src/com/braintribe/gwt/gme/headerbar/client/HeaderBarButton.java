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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.event.AddEvent;
import com.sencha.gxt.widget.core.client.event.AddEvent.AddHandler;
import com.sencha.gxt.widget.core.client.event.DisableEvent;
import com.sencha.gxt.widget.core.client.event.DisableEvent.DisableHandler;
import com.sencha.gxt.widget.core.client.event.EnableEvent;
import com.sencha.gxt.widget.core.client.event.EnableEvent.EnableHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.RemoveEvent;
import com.sencha.gxt.widget.core.client.event.RemoveEvent.RemoveHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class HeaderBarButton extends HtmlPanel {
    private String id;
    private String imageUrl;
    private String menuImageUrl;
    private boolean enabled = true;
    private boolean visible = true;
    @SuppressWarnings("hiding")
	private String toolTip = "";
    private String text = "";
    private Menu menu = null;
    private boolean useMenuButton = false;
    private boolean enabledMenuButton = true;
    private List<HeaderBarButtonListener> listeners = new ArrayList<>();
    private boolean showMenuOnClick = false;
    
    /* *******************   PUBLIC   ****************************/
    
	public HeaderBarButton() {
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
		this.setBodyStyleName("headerBarButtonBody");
		this.addStyleName("headerBarButtonPanel");
		
		this.sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT );
	
		this.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
			
			Element element = getParentElement(Element.as(target), 4, "headerbar-button-main enabled");
			if (element != null) {
				if (showMenuOnClick && menu != null && useMenuButton && enabledMenuButton)
					menu.show(this.getElement(), new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT));					
				else
					fireOnMouseClickListeners();
			}
			
			element = getParentElement(Element.as(target), 4, "headerbar-button-menu enabled");
			if (element != null) {
				if (menu != null)
					menu.show(this.getElement(), new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT));
			}			
			
		}, ClickEvent.getType());

		//ONMOUSEOVER Event Handler
		this.addHandler(event -> {
			fireOnMouseOverListeners();
		}, MouseOverEvent.getType());

		
		//ONMOUSEOUT Event Handler
		this.addHandler(event -> {
			//if (menu != null && menu.isVisible())
			//	menu.hide();
			fireOnMouseOutListeners();			
		}, MouseOutEvent.getType());
		
	}
	
	public void update() {		
		StringBuilder builder = new StringBuilder();
		String enabledStr = enabled ? "enabled" : "disabled";
		
		builder.append("<div class='headerbar-button ").append(enabledStr).append("' style='position: relative;");
			
		if (!visible)
			builder.append(" display: none;");
		
		builder.append("' id='").append(id).append("'");
		
		//Tootip - show only in case with just icon (without text button)
		if (toolTip != null && toolTip.isEmpty() && text.isEmpty())
			builder.append(" title='").append(toolTip).append("'");
		
		builder.append(">");	
		
		//Main button part
		builder.append("<div class='headerbar-button-main ").append(enabledStr).append("' id='" + id + "'>");		
		builder.append("<a class='topBannerAnchor' href='javascript:void(0)'>");
		builder.append("<div class='headerbar-button-action' id='" + id + "'>");
		
		//image
		if (imageUrl != null && !imageUrl.isEmpty())  
			builder.append("<img src='").append(imageUrl).append("' class='headerbar-button-image'/>");
		
		if (text != null && !text.isEmpty()) 
			builder.append("<div class='headerbar-button-text' id='" + id + "'>").append(text).append("</div>");
		
		builder.append("</div>");
		builder.append("</a>");
		builder.append("</div>");
		
		//Menu button part
		if (useMenuButton) {
			enabledStr = (enabled && enabledMenuButton) ? "enabled" : "disabled";		
			builder.append("<div class='headerbar-button-menu ").append(enabledStr).append("' id='" + id + "'>");
			builder.append("<a class='topBannerAnchor' href='javascript:void(0)'>");
			if (menuImageUrl != null && !menuImageUrl.isEmpty() && enabled && enabledMenuButton) {  
				builder.append("<img src='").append(menuImageUrl).append("' class='headerbar-button-menu-image'/>");
			} else {
				builder.append("<img src='").append(ConstellationResources.INSTANCE.emptySmall().getSafeUri().asString()).append("' class='headerbar-button-empty-image'/>");			
			}
			builder.append("</a>");	
			builder.append("</div>");
		}
		
		builder.append("</div>");
				
		builder.append("</div>");
				
		this.setHtml(builder.toString());
		this.init();
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
		update();
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
		update();
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
		update();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		update();
	}	

	public void setTooltip(String toolTip) {
		this.toolTip = toolTip;
		update();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		update();
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
		prepareMenuListeners();
		checkMenuItems();
	}

	public boolean useMenuButton() {
		return useMenuButton;
	}

	public void setUseMenuButton(boolean useMenuButton) {
		this.useMenuButton = useMenuButton;
		update();
	}

	public void addListener(HeaderBarButtonListener listener) {
		this.listeners.add(listener);
	}	

	public void removeListener(HeaderBarButtonListener listener) {
		this.listeners.remove(listener);
	}	

	public String getMenuImageUrl() {
		return menuImageUrl;
	}

	public void setMenuImageUrl(String menuImageUrl) {
		this.menuImageUrl = menuImageUrl;
		update();
	}
	
	/* *******************   PRIVATE   ****************************/
	
	private Element getParentElement(Element element, int depth, String className) {
		if (depth <= 0 || element == null)
			return null;
		
		if (className.equals(element.getClassName()))
			return element;
		
		return getParentElement(element.getParentElement(), --depth, className);
	}

	public void fireOnMouseClickListeners() {
		for (HeaderBarButtonListener listener : listeners) 
			if (listener != null)
				listener.onClickButton(this);		
	}

	public void fireOnMouseOutListeners() {
		for (HeaderBarButtonListener listener : listeners) 
			if (listener != null)
				listener.onMouseOutButton(this);		
	}

	public void fireOnMouseOverListeners() {
		for (HeaderBarButtonListener listener : listeners) 
			if (listener != null)
				listener.onMouseOverButton(this);		
	}

	private void setEnabledMenuButton(boolean enabledMenuButton) {
		this.enabledMenuButton = enabledMenuButton;
		update();
	}
	
	private void prepareMenuListeners() {
		if (menu == null)
			return;
		
		menu.addAddHandler(new AddHandler() {			
			@Override
			public void onAdd(AddEvent event) {
				addMenuWidgetListeners(event.getWidget());
				checkMenuItems();
			}
		});
		
		menu.addRemoveHandler(new RemoveHandler() {			
			@Override
			public void onRemove(RemoveEvent event) {
				checkMenuItems();
			}
		});
		
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget widget = menu.getWidget(i);
			addMenuWidgetListeners(widget);
		}
	}

	private void addMenuWidgetListeners(Widget widget) {
		if (widget == null || !(widget instanceof MenuItem))
			return;
		
		MenuItem menuItem = (MenuItem) widget;
		menuItem.addEnableHandler(new EnableHandler() {				
			@Override
			public void onEnable(EnableEvent event) {
				checkMenuItems();
			}
		});
		menuItem.addDisableHandler(new DisableHandler() {				
			@Override
			public void onDisable(DisableEvent event) {
				checkMenuItems();					
			}
		});
		menuItem.addShowHandler(new ShowHandler() {				
			@Override
			public void onShow(ShowEvent event) {
				checkMenuItems();
			}
		});
		menuItem.addHideHandler(new HideHandler() {				
			@Override
			public void onHide(HideEvent event) {
				checkMenuItems();					
			}
		});
	}

	private void checkMenuItems() {
		boolean doMenuEnable = false;
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget widget = menu.getWidget(i);
			if (widget == null || !(widget instanceof MenuItem))
				continue;
			
			MenuItem menuItem = (MenuItem) widget;
			if (menuItem.isEnabled()) {
				Action action = menuItem.getData("action");
				if (action == null)
					doMenuEnable = true;
				else
					doMenuEnable = !action.getHidden();
					
				if (doMenuEnable)
					break;
			}
		}
		setEnabledMenuButton(doMenuEnable);
	}

	public boolean isShowMenuOnClick() {
		return showMenuOnClick;
	}

	public void setShowMenuOnClick(boolean showMenuOnClick) {
		this.showMenuOnClick = showMenuOnClick;
	}

}
