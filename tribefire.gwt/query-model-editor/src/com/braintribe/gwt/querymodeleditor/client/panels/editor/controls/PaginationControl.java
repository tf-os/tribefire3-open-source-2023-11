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
package com.braintribe.gwt.querymodeleditor.client.panels.editor.controls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorResources;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorTemplates;
import com.braintribe.gwt.querymodeleditor.client.resources.TemplateConfigurationBean;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class PaginationControl extends ContentPanel implements ClickHandler, ResizeHandler {

	/********************************** Constants **********************************/

	private final String pageSizeDropDownCellId = this.hashCode() + "_pageSizeDropDownCell";
	private final String decreasePageButtonId = this.hashCode() + "_decreasePageButton";
	private final String currentPageLabelId = this.hashCode() + "_currentPageLabel";
	private final String increasePageButtonId = this.hashCode() + "_increasePageButton";

	private final String selectPageDropDownMenuCell = "div[@id='" + this.pageSizeDropDownCellId + "']";
	private final String selectDecreasePageButton = "img[@id='" + this.decreasePageButtonId + "']";
	private final String selectCurrentPageLabel = "p[@id='" + this.currentPageLabelId + "']";
	private final String selectIncreasePageButton = "img[@id='" + this.increasePageButtonId + "']";

	public static final List<Integer> pageSizeValues = Arrays.asList(5, 15, 30, 50, 100, 200);

	/********************************** Variables **********************************/

	private DivElement pageSizeDropDownCell = null;
	private DropDownControl pageSizeDropDown = null;
	private Menu pageSizeValuesMenu = null;

	private ImageElement decreasePageButton = null;
	private ParagraphElement currentPageLabel = null;
	private ImageElement increasePageButton = null;

	private boolean isControlEnabled = true;
	private boolean isIncreasePageButtonEnabled = false;
	private boolean isDecreasePageButtonEnabled = false;

	private SelectionHandler<Item> pageSizeSelectedHandler = null;
	private ClickHandler increasePageClickedHandler = null;
	private ClickHandler decreasePageClickedHandler = null;

	private int pageSize = pageSizeValues.get(2);
	private int pageIndex = 0;
	private Set<Integer> pageSizeAdded = new HashSet<>();

	/****************************** PaginationControl ******************************/

	@Configurable
	public void setPageSizeSelected(final SelectionHandler<Item> pageSizeSelectedHandler) {
		this.pageSizeSelectedHandler = pageSizeSelectedHandler;
	}

	@Configurable
	public void setIncreasePageClicked(final ClickHandler increasePageClickedHandler) {
		this.increasePageClickedHandler = increasePageClickedHandler;
	}

	@Configurable
	public void setDecreasePageClicked(final ClickHandler decreasePageClickedHandler) {
		this.decreasePageClickedHandler = decreasePageClickedHandler;
	}

	public int getPageIndex() {
		return this.pageIndex;
	}

	public void setPageIndex(final int startIndex) {
		this.pageIndex = startIndex;
		setPageLabelText();
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		getPageSizeDropDown().setHTML(getPageSizeHTML());
		
		boolean addMenuItem = !PaginationControl.pageSizeValues.contains(pageSize) && pageSizeAdded.add(pageSize);
		if (!addMenuItem)
			return;
		
		MenuItem menuItem = preparePageSizeMenuItem(pageSize);
		
		int beforeIndex = -1;
		for (int i = 0; i < pageSizeValuesMenu.getWidgetCount(); i++) {
			MenuItem item = (MenuItem) pageSizeValuesMenu.getWidget(i);
			if (Integer.parseInt(item.getText()) > pageSize) {
				beforeIndex = i;
				break;
			}
		}
		
		if (beforeIndex != -1)
			pageSizeValuesMenu.insert(menuItem, beforeIndex);
		else
			pageSizeValuesMenu.add(menuItem);
	}

	public PaginationControl() {
		super();

		this.setHeaderVisible(false);
		this.setDeferHeight(false);
		this.setBodyBorder(false);
		this.setBorders(false);

		// Needed to enable events
		addWidgetToRootPanel(this);
		initializeControl();
	}

	private static void addWidgetToRootPanel(final Widget widget) {
		// Add to RootPanel
		RootPanel.get().add(widget);
	}

	private void initializeControl() {
		// Get and initialize template Elements
		TemplateConfigurationBean bean = new TemplateConfigurationBean();
		bean.setPageSizeDropDownCellId(this.pageSizeDropDownCellId);
		bean.setDecreasePageButtonId(this.decreasePageButtonId);
		bean.setCurrentPageLabelId(this.currentPageLabelId);
		bean.setIncreasePageButtonId(this.increasePageButtonId);
		this.add(new HTML(QueryModelEditorTemplates.INSTANCE.pagenationControl(bean)));

		this.pageSizeDropDownCell = DomQuery.selectNode(this.selectPageDropDownMenuCell, this.getElement()).cast();
		this.pageSizeDropDownCell.appendChild(getPageSizeDropDown().getElement());

		this.decreasePageButton = DomQuery.selectNode(this.selectDecreasePageButton, this.getElement()).cast();
		this.decreasePageButton.setSrc(QueryModelEditorResources.INSTANCE.arrowLeft().getSafeUri().asString());

		this.currentPageLabel = DomQuery.selectNode(this.selectCurrentPageLabel, this.getElement()).cast();
		setPageLabelText();

		this.increasePageButton = DomQuery.selectNode(this.selectIncreasePageButton, this.getElement()).cast();
		this.increasePageButton.setSrc(QueryModelEditorResources.INSTANCE.arrowRight().getSafeUri().asString());

		// Draw layout
		forceLayout();

		// Add Events to Element
		this.addDomHandler(this, ClickEvent.getType());
		this.addHandler(this, ResizeEvent.getType());

		// Disable paging buttons
		enableIncreasePageButton(this.isIncreasePageButtonEnabled);
		enableDecreasePageButton(this.isDecreasePageButtonEnabled);
	}

	/************************** Create SubControl Methods **************************/

	private DropDownControl getPageSizeDropDown() {
		if (this.pageSizeDropDown == null) {
			// Set style to overwrite default!
			this.pageSizeDropDown = new DropDownControl();
			this.pageSizeDropDown.setMenu(getPageSizeValuesMenu());
			this.pageSizeDropDown.setHTML(getPageSizeHTML());

			// Needed to enable events
			addWidgetToRootPanel(this.pageSizeDropDown);
		}

		return this.pageSizeDropDown;
	}

	private Menu getPageSizeValuesMenu() {
		if (pageSizeValuesMenu != null)
			return pageSizeValuesMenu;
		
		pageSizeValuesMenu = new Menu();
		for (Integer menuPageSize : pageSizeValues)
			pageSizeValuesMenu.add(preparePageSizeMenuItem(menuPageSize));

		return pageSizeValuesMenu;
	}
	
	private MenuItem preparePageSizeMenuItem(Integer menuPageSize) {
		MenuItem menuItem = new MenuItem(menuPageSize.toString());
		menuItem.addSelectionHandler(event -> {
			pageSize = menuPageSize;
			getPageSizeDropDown().setHTML(getPageSizeHTML());

			if (pageSizeSelectedHandler != null)
				pageSizeSelectedHandler.onSelection(event);
		});
		
		return menuItem;
	}
	

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		// Get target Element
		final NativeEvent nativeEvent = event.getNativeEvent();
		final Element targetElement = nativeEvent.getEventTarget().cast();

		// Check target Element
		if (this.increasePageButton.isOrHasChild(targetElement)) {
			if (this.isControlEnabled == true && this.isIncreasePageButtonEnabled == true) {
				this.pageIndex += this.pageSize;
				setPageLabelText();

				if (this.increasePageClickedHandler != null) {
					this.increasePageClickedHandler.onClick(event);
				}
			}
		} else if (this.decreasePageButton.isOrHasChild(targetElement)) {
			if (this.isControlEnabled == true && this.isDecreasePageButtonEnabled == true) {
				this.pageIndex = Math.max(0, this.pageIndex - this.pageSize);
				setPageLabelText();

				if (this.decreasePageClickedHandler != null) {
					this.decreasePageClickedHandler.onClick(event);
				}
			}
		}
	}

	@Override
	public void onResize(final ResizeEvent event) {
		// Draw layout
		forceLayout();
	}

	/******************************* Control Methods *******************************/

	public void enableIncreasePageButton(final boolean value) {
		this.isIncreasePageButtonEnabled = value;

		if (this.increasePageButton != null) {
			this.increasePageButton.getStyle().setOpacity(value == true ? 1d : 0.5d);
		}
	}

	public boolean isIncreasePageButtonEnabled() {
		return this.isIncreasePageButtonEnabled;
	}

	public void enableDecreasePageButton(final boolean value) {
		this.isDecreasePageButtonEnabled = value;

		if (this.decreasePageButton != null) {
			this.decreasePageButton.getStyle().setOpacity(value == true ? 1d : 0.5d);
		}
	}

	public boolean isDecreasePageButtonEnabled() {
		return this.isDecreasePageButtonEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isControlEnabled = enabled;

		if (this.pageSizeDropDown != null) {
			this.pageSizeDropDown.setEnabled(enabled);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.isControlEnabled;
	}

	/******************************** Helper Methods *******************************/

	private void setPageLabelText() {
		this.currentPageLabel.setInnerText(String.valueOf(this.pageIndex + 1));
	}

	private String getPageSizeHTML() {
		return "<div class='paginationPageSizeLabel'>" + // NoFormat
				"<span style='color:silver;'>" + LocalizedText.INSTANCE.resultsPerPage() + "</span>&ensp;" + // NoFormat
				Integer.toString(this.pageSize) + // NoFormat
				"</div>";
	}
}
