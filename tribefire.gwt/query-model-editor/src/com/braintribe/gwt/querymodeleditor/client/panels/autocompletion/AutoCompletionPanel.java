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
package com.braintribe.gwt.querymodeleditor.client.panels.autocompletion;

import java.util.Map;

import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.querymodeleditor.client.panels.editor.QueryModelEditorAdvancedPanel;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorTemplates;
import com.braintribe.gwt.querymodeleditor.client.resources.TemplateConfigurationBean;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.widget.core.client.ContentPanel;

public class AutoCompletionPanel extends ContentPanel implements InitializableBean, ResizeHandler, DisposableBean {

	/********************************** Constants **********************************/

	private final String autoCompletionDialogTableHeaderId = this.hashCode() + "_autoCompletionDialogTableHeader";
	private final String autoCompletionDialogItemAreaId = this.hashCode() + "_autoCompletionDialogItemArea";

	private final String selectAutoCompletionDialogTableHeader = "div[@id='" + this.autoCompletionDialogTableHeaderId + "']";
	private final String selectAutoCompletionDialogItemArea = "div[@id='" + this.autoCompletionDialogItemAreaId + "']";

	/********************************** Variables **********************************/

	private QueryModelEditorAdvancedPanel queryModelEditorAdvancedPanel = null;

	private DivElement autoCompletionDialogTableHeader = null;
	private DivElement autoCompletionDialogItemArea = null;
	
	private HandlerRegistration resizeHandlerRegistration;
	
	private int selectedRow = 0;

	/***************************** AutoCompletionDialog ****************************/

	public AutoCompletionPanel() {
		this.setId("AutoCompletionDialog");
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
	}

	@Required
	public void setAdvancedQueryModelEditorPanel(final QueryModelEditorAdvancedPanel queryModelEditorAdvancedPanel) {
		this.queryModelEditorAdvancedPanel = queryModelEditorAdvancedPanel;
	}

	public void setPossibleHints(Map<String, String> possibleHints) {
		if (autoCompletionDialogItemArea == null)
			return;
		
		// Remove all items from item area
		autoCompletionDialogItemArea.removeAllChildren();

		boolean selectedSet = false;
		// Add new items to item area
		for (Map.Entry<String, String> possibleHint : possibleHints.entrySet()) {
			DivElement hintItem = createPossibleHintElement(possibleHint.getValue(), possibleHint.getKey(), false);
			if (!selectedSet) {
				hintItem.addClassName("selectedItem");
				selectedRow = 0;
				selectedSet = true;
			}

			DOM.sinkEvents(hintItem, Event.ONCLICK | Event.ONDBLCLICK);
			DOM.setEventListener(hintItem, new EventListener() {
				@Override
				public void onBrowserEvent(final Event event) {
					if (event.getTypeInt() == Event.ONCLICK) {
						((DivElement) autoCompletionDialogItemArea.getChild(selectedRow)).removeClassName("selectedItem");
						for (int i = 0; i < autoCompletionDialogItemArea.getChildCount(); i++) {
							if (autoCompletionDialogItemArea.getChild(i) == hintItem) {
								hintItem.addClassName("selectedItem");
								selectedRow = i;
							}
						}
					} else if (event.getTypeInt() == Event.ONDBLCLICK && queryModelEditorAdvancedPanel != null) {
						queryModelEditorAdvancedPanel.setAutoCompletionValue(possibleHint.getKey());
					}
				}
			});

			this.autoCompletionDialogItemArea.appendChild(hintItem);
		}
		
		if (possibleHints.isEmpty()) {
			DivElement hintItem = createPossibleHintElement(LocalizedText.INSTANCE.noSuggestionsAvailable(), LocalizedText.INSTANCE.noSuggestionsAvailable(), true);
			this.autoCompletionDialogItemArea.appendChild(hintItem);
		}
	}

	public DivElement getTableHeader() {
		return autoCompletionDialogTableHeader;
	}

	public DivElement getItemArea() {
		return autoCompletionDialogItemArea;
	}
	
	public void selectNext() {
		if (selectedRow < autoCompletionDialogItemArea.getChildCount() - 1) {
			((DivElement) autoCompletionDialogItemArea.getChild(selectedRow++)).removeClassName("selectedItem");
			DivElement element = ((DivElement) autoCompletionDialogItemArea.getChild(selectedRow));
			element.addClassName("selectedItem");
			element.scrollIntoView();
		}
	}
	
	public void selectPrevious() {
		if (selectedRow > 0) {
			((DivElement) autoCompletionDialogItemArea.getChild(selectedRow--)).removeClassName("selectedItem");
			DivElement element = ((DivElement) autoCompletionDialogItemArea.getChild(selectedRow));
			element.addClassName("selectedItem");
			element.scrollIntoView();
		}
	}
	
	public void chooseSelected() {
		String value = ((DivElement) autoCompletionDialogItemArea.getChild(selectedRow++)).getTitle();
		if (!LocalizedText.INSTANCE.noSuggestionsAvailable().equals(value))
			queryModelEditorAdvancedPanel.setAutoCompletionValue(value);
	}
	
	private DivElement createPossibleHintElement(String display, String value, boolean emptyIndicator) {
		DivElement hintItem = DOM.createDiv().cast();
		hintItem.setClassName("autoCompletionDialogItem");
		if (!display.equals(value))
			hintItem.setInnerText(display + " (" + value + ")");
		else
			hintItem.setInnerText(value);
		hintItem.setTitle(value);
		
		if (emptyIndicator)
			hintItem.addClassName("autoCompletionEmptyItem");
		
		return hintItem;
	}

	/******************************* InitializableBean *****************************/

	@Override
	public void intializeBean() throws Exception {
		// Get and initialize template Elements with Paging
		TemplateConfigurationBean bean = new TemplateConfigurationBean();
		bean.setAutoCompletionDialogTableHeaderId(autoCompletionDialogTableHeaderId);
		bean.setAutoCompletionDialogItemAreaId(autoCompletionDialogItemAreaId);
		add(new HTML(QueryModelEditorTemplates.INSTANCE.qacDialogPanel(bean)));

		autoCompletionDialogTableHeader = DomQuery.selectNode(selectAutoCompletionDialogTableHeader, getElement()).cast();
		autoCompletionDialogItemArea = DomQuery.selectNode(selectAutoCompletionDialogItemArea, getElement()).cast();

		// Draw layout
		forceLayout();
		resizeHandlerRegistration = addHandler(this, ResizeEvent.getType());
	}

	@Override
	public void onResize(final ResizeEvent event) {
		final int itemAreaHeight = getOffsetHeight() - autoCompletionDialogTableHeader.getOffsetHeight();
		autoCompletionDialogItemArea.getStyle().setHeight(itemAreaHeight, Unit.PX);
	}
	
	@Override
	public void disposeBean() throws Exception {
		resizeHandlerRegistration.removeHandler();
	}
}
