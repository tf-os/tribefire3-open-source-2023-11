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
package com.braintribe.gwt.gme.verticaltabpanel.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;

public class OpenerDialog extends ClosableWindow implements Function<List<VerticalTabElement>, Future<VerticalTabElement>> {
	
	interface ElementBeanProperties extends PropertyAccess<ElementBean> {
		ModelKeyProvider<ElementBean> element();
	}
	private static ElementBeanProperties props = GWT.create(ElementBeanProperties.class);
	
	private Grid<ElementBean> itemsGrid;
	private Future<VerticalTabElement> future;
	
	public OpenerDialog() {
		setSize("400px", "250px");
		setClosable(false);
		setModal(true);
		//setBodyStyle("backgroundColor:white");
		//setHeading(LocalizedText.INSTANCE.selectOptionNewTab());
		add(prepareGrid());
		addButton(prepareCancelButton());
		addButton(prepareReplaceButton());
		addButton(prepareCreateNewTabButton());
		setBodyBorder(false);
		setBorders(false);
		getHeader().setHeight(20);
		//setFrame(false);
		//setPlain(true);
	}

	@Override
	public Future<VerticalTabElement> apply(List<VerticalTabElement> tabElements) {
		prepareGridElements(tabElements);
		show();
		future = new Future<>();
		return future;
	}
	
	private TextButton prepareCancelButton() {
		return new TextButton(LocalizedText.INSTANCE.cancel(), event -> hide());
	}
	
	private TextButton prepareReplaceButton() {
		final TextButton replaceButton = new TextButton(LocalizedText.INSTANCE.replaceSelectedTab(), event -> {
			hide();
			future.onSuccess(itemsGrid.getSelectionModel().getSelectedItem().getElement());
		});
		
		itemsGrid.getSelectionModel()
				.addSelectionChangedHandler(event -> replaceButton.setEnabled(event.getSelection() != null && !event.getSelection().isEmpty()));
		
		return replaceButton;
	}
	
	private TextButton prepareCreateNewTabButton() {
		return new TextButton(LocalizedText.INSTANCE.createAdditionalTab(), event -> {
			hide();
			future.onSuccess(null);
		});
	}
	
	private Grid<ElementBean> prepareGrid() {
		ColumnConfig<ElementBean, ElementBean> column = new ColumnConfig<>(new IdentityValueProvider<>(), 350,
				LocalizedText.INSTANCE.selectOptionNewTab());
		column.setCellPadding(false);
		column.setCell(new AbstractCell<ElementBean>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, ElementBean value, SafeHtmlBuilder sb) {
				sb.appendEscaped(value.getElement().getText());
			}
		});
		
		column.setFixed(true);
		column.setMenuDisabled(true);

		List<ColumnConfig<ElementBean, ?>> columns = new ArrayList<ColumnConfig<ElementBean, ?>>();
		columns.add(column);
		itemsGrid = new Grid<>(new ListStore<>(props.element()), new ColumnModel<>(columns));
		
		itemsGrid.setView(new GridView<ElementBean>() {
			@Override
			protected void onRowSelect(int rowIndex) {
				super.onRowSelect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.addClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onRowDeselect(int rowIndex) {
				super.onRowDeselect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.removeClassName("x-grid3-row-selected");
			}
		});
		itemsGrid.getView().setColumnHeader(new ExtendedColumnHeader<>(itemsGrid, itemsGrid.getColumnModel()));
		
		//itemsGrid.setHideHeaders(true);
		//itemsGrid.getView().setForceFit(true);
		
		itemsGrid.getView().setViewConfig(new GridViewConfig<ElementBean>() {
			@Override
			public String getRowStyle(ElementBean model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(ElementBean model, ValueProvider<? super ElementBean, ?> valueProvider, int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		return itemsGrid;
	}
	
	private void prepareGridElements(List<VerticalTabElement> tabElements) {
		itemsGrid.getStore().clear();
		tabElements.stream().filter(element -> !element.isStatic()).forEach(element -> {
			ElementBean model = new ElementBean(element);
			itemsGrid.getStore().add(model);
		});
		itemsGrid.getSelectionModel().select(0, false);
	}
	
	public class ElementBean {
		private VerticalTabElement element;
		
		public ElementBean(VerticalTabElement element) {
			setElement(element);
		}
		
		public VerticalTabElement getElement() {
			return element;
		}
		
		public void setElement(VerticalTabElement element) {
			this.element = element;
		}
	}

}
