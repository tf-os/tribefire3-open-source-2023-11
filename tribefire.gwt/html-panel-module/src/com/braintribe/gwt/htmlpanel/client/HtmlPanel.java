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
package com.braintribe.gwt.htmlpanel.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.braintribe.gwt.action.adapter.common.client.LabelActionAdapter;
import com.braintribe.gwt.action.adapter.gxt.client.ButtonActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.logging.client.Logger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;

/**
 * This panel can be freely customized. It takes a URL
 * from which the layout HTML is loaded. This HTML contains
 * PlaceHolder tags (id attribute must be set) in which
 * configured widgets are placed.
 * Also, HTML can be configured directly, without the use of URL.
 * 
 * @see #setHtmlSourceUrl(String)
 * @see #setWidgetMap(Map)
 * @see #setHtml(String)
 *
 */
public class HtmlPanel extends ContentPanel implements DisposableBean {
	private static Logger logger = new Logger(HtmlPanel.class);
	private HTMLPanel htmlPanel;
	private String htmlSourceUrl = null;
	private Map<String, Widget> widgetMap;
	private String htmlExcerpt;
	private List<HtmlPanelListener> listeners;
	private String lastHtmlSourceUrl = null;
	
	private class Callback implements RequestCallback {
		@Override
		public void onResponseReceived(Request request, Response response) {
			String html = response.getText();
			
			if (htmlSourceUrl == null)
				htmlSourceUrl = lastHtmlSourceUrl;
			int index = htmlSourceUrl.lastIndexOf('/');
			if (index != -1) {
				String path = htmlSourceUrl.substring(0, index);
				html = html.replaceAll("\\./\\.", path);
			}
			
			buildLayout(html);
		}
		
		@Override
		public void onError(Request request, Throwable exception) {
			logger.error("Error while loading Html layout.", exception);
		}
	}
	
	public HtmlPanel() {
		getElement().getStyle().setProperty("height", "auto");
		setBorders(false);
		setHeaderVisible(false);
		setStyleName("htmlPanel");
	}
	
	/**
	 * Configures the URL used for loading the HTML.
	 * Required if {@link #setHtml(String)} is not set.
	 */
	@Configurable
	public void setHtmlSourceUrl(String htmlSourceUrl) {
		lastHtmlSourceUrl = this.htmlSourceUrl;
		this.htmlSourceUrl = htmlSourceUrl;
	}
	
	/**
	 * Configures the HTML excerpt to create the Panel from.
	 * This is another way of configuring the HTML directly, as opposed to {@link #setHtmlSourceUrl(String)},
	 * which configures a URL for loading the HTML instead. If both are set, which is a misconfiguration,
	 * then only the one from the URL is used.
	 * Required if {@link #setHtmlSourceUrl(String)} is not set.
	 */
	@Configurable
	public void setHtml(String html) {
		this.htmlExcerpt = html;
	}
	
	@Configurable
	public void setWidgetMap(Map<String, Widget> widgetMap) {
		if (this.widgetMap == null)
			this.widgetMap = widgetMap;
		else
			this.widgetMap.putAll(widgetMap);
	}
	
	/**
	 * This method adds a Widget to the HtmlPanel, in the slot passed as parameter.
	 */
	@Configurable
	public void addWidget(String slotId, Widget widget) {
		if (this.widgetMap == null)
			this.widgetMap = new FastMap<>();
		this.widgetMap.put(slotId, widget);
	}

	/**
	 * This method adds an Action (which will be adapted to a Label) to the HtmlPanel, in the slot passed as parameter.
	 */
	@Configurable
	public void addLabelAction(String slotId, Action action) {
		if (this.widgetMap == null)
			this.widgetMap = new FastMap<>();
		Label label = new Label();
		LabelActionAdapter.linkActionToLabel(action, label);
		this.widgetMap.put(slotId, label);
	}
	
	/**
	 * This method adds an Action (which will be adapted to a Button) to the HtmlPanel, in the slot passed as parameter.
	 */
	@Configurable
	public void addButtonAction(String slotId, Action action) {
		if (this.widgetMap == null)
			this.widgetMap = new FastMap<>();
		TextButton button = new TextButton();
		ButtonActionAdapter.linkActionToButton(action, button);
		this.widgetMap.put(slotId, button);
	}
	
	public void clearWidgets() {
		if (widgetMap != null) {
			widgetMap.clear();
			widgetMap = null;
		}
	}
	
	public void addWidgetsAfterRender(Map<String, Widget> widgetMap) {
		if (widgetMap == null)
			return;
		
		setWidgetMap(widgetMap);
		for (Map.Entry<String, Widget> entry: widgetMap.entrySet()) {
			String id = entry.getKey();
			Widget widget = entry.getValue();
			htmlPanel.add(widget, id);
		}
	}
	
	public String getHtml() {
		return htmlExcerpt;
	}
	
	public Map<String, Widget> getWidgetMap() {
		return widgetMap;
	}
	
	public void addHtmlPanelListener(HtmlPanelListener listener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		listeners.add(listener);
	}
	
	public void removeHtmlPanelListener(HtmlPanelListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}
	
	public void init() {
		if (htmlSourceUrl == null) {
			buildLayout(htmlExcerpt);
			return;
		}
		
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, htmlSourceUrl);
		try {
			requestBuilder.sendRequest(null, new Callback());
		}
		catch (Exception e) {
			//NOP
		}
	}
	
	private void fireLayoutBuilt() {
		if (listeners != null)
			listeners.forEach(l -> l.onLayoutBuilt());
	}
	
	private static Element getElementById(Element element, String id) {
		if (id.equals(element.getId()))
			return element;
		
		Element childElement = element.getFirstChildElement(); 
		while (childElement != null) {
			Element result = getElementById(childElement, id);
			if (result != null)
				return result;
			childElement = childElement.getNextSiblingElement();
		}
		
		return null;
	}
	
	protected void buildLayout(String html) {
		if (htmlPanel != null)
			remove(htmlPanel);
		
		htmlPanel = new HTMLPanel(html) {
			@SuppressWarnings("deprecation")
			@Override
			public com.google.gwt.user.client.Element getElementById(String id) {
				return (com.google.gwt.user.client.Element) HtmlPanel.getElementById(getElement(), id);
			}
		};
		
		htmlPanel.setHeight("auto");
		htmlPanel.addStyleName("htmlPanel");
				
		if (widgetMap != null) {
			for (Map.Entry<String, Widget> entry: widgetMap.entrySet()) {
				try {
					htmlPanel.add(entry.getValue(), entry.getKey());
				} catch (Exception ex) { //Sometimes, the above line was bringing issues in Chrome. See BTT-6460.
					if (ex instanceof NoSuchElementException)
						logger.error("The given element was not found: " + entry.getKey());
					else
						logger.error("Error while building the htmlPanel.", ex);
				}
			}
		}
		add(htmlPanel);
		
		forceLayout();
		fireLayoutBuilt();
	}

	@Override
	public void disposeBean() throws Exception {
		if (isRendered() && htmlPanel != null)
			remove(htmlPanel);
		if (widgetMap != null)
			widgetMap.clear();
		if (listeners != null)
			listeners.clear();
	}
}
