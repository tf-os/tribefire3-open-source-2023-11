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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;

public class MenuWithSearch extends Menu {

	private boolean useSearch = true;
	private int minItemsCountToSearch = 5;
	private TextField searchField;
	private SeparatorMenuItem searchSeparator;
	private List<MenuItem> availableItems = new ArrayList<>(); 
	
	public MenuWithSearch() {
		    this(GWT.<MenuAppearance>create(MenuAppearance.class));
	}

	public MenuWithSearch(MenuAppearance create) {
		super(create);
		addStyleName("menuWithSearch");
		searchField = new TextField();
		searchField.addStyleName("menuWithSearchField");
		searchField.addKeyUpHandler(event -> updateMenuItems(searchField.getCurrentValue()));
		searchField.setEmptyText(LocalizedText.INSTANCE.search());
		searchField.setAllowTextSelection(true);
		searchField.setContextMenu(null);
		add(searchField);	
		
		searchSeparator = new SeparatorMenuItem();
		add(searchSeparator);
		
		addBeforeShowHandler(event -> {
			searchField.clear();
			XElement xElement = searchField.getElement();
			XElement inputElement = xElement.child("input:first-child");
			if (inputElement != null)
				inputElement.setAttribute("autocomplete", "off");
			
			searchField.setVisible(useSearch && getSearchableItemsCount() >= minItemsCountToSearch);
			searchSeparator.setVisible(useSearch && getSearchableItemsCount() >= minItemsCountToSearch);
			
			updateMenuItems(searchField.getCurrentValue());
		});
		
		addShowHandler(event -> {
			prepareSearchableItems();										
			searchField.focus();
		});
	}

	public boolean getUseSearch() {
		return useSearch;
	}

	public void setUseSearch(boolean useSearch) {
		this.useSearch = useSearch;
		searchField.setVisible(useSearch);
		searchSeparator.setVisible(useSearch);
	}

	public int getMinItemsCountToSearch() {
		return minItemsCountToSearch;
	}

	public void setMinItemsCountToSearch(int minItemsCountToSearch) {
		this.minItemsCountToSearch = minItemsCountToSearch;
	}	
	
	private void prepareSearchableItems() {		
		availableItems.clear();
		for (int i = 0; i < getWidgetCount(); i++) {
		    Widget widget = getWidget(i);
		    if (widget instanceof MenuItem) {
		    	MenuItem menuItem = (MenuItem) widget;
		    	if (menuItem.isVisible())
		    		availableItems.add(menuItem);		    	
		    }
		}		
	}	
	
	private boolean subMenuVisibeItem(Menu menu, String filter) {
		boolean visible = false;
		
		String filterVal = "";
		if (filter != null)
			filterVal = filter.toUpperCase();
		
		Iterator<Widget> it = menu.iterator();
	    while (it.hasNext()) {	    	
		      Widget widget = it.next();
		      if (widget instanceof MenuItem) {
		    	  MenuItem menuItem = (MenuItem) widget;
		    	  String text = menuItem.getText();
		    	  boolean itemVisible = false;
		    	  boolean subMenuVisible = false;
		    	  
		    	  if (menuItem.getSubMenu() != null) {
		    		  subMenuVisible = subMenuVisibeItem(menuItem.getSubMenu(), filter);
		    		  menuItem.setVisible(subMenuVisible);
		    	  } else {
		    		  itemVisible = text.toUpperCase().indexOf(filterVal) > -1 ;
		    		  menuItem.setVisible(itemVisible); 
		    	  }
		    	  
                  visible = visible || itemVisible || subMenuVisible;
		      }
		    }		
		
		return visible;
	}
	
	private void updateMenuItems(String filter) {
		String filterVal = "";
		if (filter != null)
			filterVal = filter.toUpperCase();
		
		for (MenuItem menuItem : availableItems) {
		   String text = menuItem.getText();
		   
		   if (menuItem.getSubMenu() != null) {
			   menuItem.setVisible(subMenuVisibeItem(menuItem.getSubMenu(), filter)); 
		   } else {
			   menuItem.setVisible(text.toUpperCase().indexOf(filterVal) > -1);			   
		   }
		}		
		
		constrainScroll(getElement().getY());
		sync(true);		
	}
	
	private int getSearchableItemsCount() {
		int count = 0;
	    Iterator<Widget> it = iterator();
	    while (it.hasNext()) {	    	
	      Widget widget = it.next();
	      if (widget instanceof MenuItem)	
	    	  count++;
	    }
		return count;
	}
	
	/**
	* Clears the container's children
	* 
	*/
    @Override
	public void clear() {
	    Iterator<Widget> it = iterator();
	    while (it.hasNext()) {	    	
	      Widget widget = it.next();
	      if (!widget.equals(searchField) && !widget.equals(searchSeparator))	
	    	  it.remove();
	    }
	}
	
}
