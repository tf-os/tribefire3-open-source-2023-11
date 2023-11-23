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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.cell.HandlerManagerContext;
import com.sencha.gxt.widget.core.client.event.CellBeforeSelectionEvent;
import com.sencha.gxt.widget.core.client.event.CellSelectionEvent;

public class CheckComboBoxCell<T> extends ComboBoxCell<T> {

	public CheckComboBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, SafeHtmlRenderer<T> renderer) {
		super(store, labelProvider, renderer);
	}

	@Override
	protected void onSelect(T item) {
	    //FieldViewData viewData = ensureViewData(lastContext, lastParent);

	    boolean cancelled = false;
	    if (lastContext instanceof HandlerManagerContext) {
	      HandlerManager manager = ((HandlerManagerContext) lastContext).getHandlerManager();
	      CellBeforeSelectionEvent<T> event = CellBeforeSelectionEvent.fire(manager, lastContext, item);
	      if (event != null && event.isCanceled()) {
	        cancelled = true;
	      }
	    } else {
	      CellBeforeSelectionEvent<T> event = CellBeforeSelectionEvent.fire(this, lastContext, item);
	      if (event.isCanceled()) {
	        cancelled = true;
	      }
	    }

	    if (!cancelled) {

	      if (lastContext instanceof HandlerManagerContext) {
	        HandlerManager manager = ((HandlerManagerContext) lastContext).getHandlerManager();
	        CellSelectionEvent.fire(manager, lastContext, item);
	      } else {
	        CellSelectionEvent.fire(this, lastContext, item);
	      }
	      
	      
		  String viewValue = "";
		  if (item instanceof CheckValueItem) {
			  for (T storeItem : store.getAll()) {
				  if (storeItem instanceof CheckValueItem) {
				  
					  if (((CheckValueItem) storeItem).getSelected()) {
						  if (!viewValue.isEmpty())
							  viewValue = viewValue + ", "; 
						  viewValue = viewValue + getRenderedValue(storeItem);
					  }
				  }
			  }			  
		  } else {
			  viewValue = getRenderedValue(item);
		  }
			
	      //this.lastSelectedValue = item;

	      //if (viewData != null) {
	      //  viewData.setCurrentValue(rv);
	      //}
	    	
	      getInputElement(lastParent).setValue(viewValue);
	      
	    }

	    // collapsing non deferred causes trigger field mouse down preview
	    // to think a focus click has occurred which causing the field to be
	    // blurred after value changed
	    /*
	    if (GXT.isIE()) {
	      Scheduler.get().scheduleDeferred(new ScheduledCommand() {

	        @Override
	        public void execute() {
	          collapse(lastContext, lastParent.<XElement>cast());
	        }
	      });

	    } else {
	      collapse(lastContext, lastParent.<XElement>cast());
	    }
	    */
	  }

	  @Override
	  public void finishEditing(Element parent, T value, Object key, ValueUpdater<T> valueUpdater) {
            //RVE prevent use ValueUpdater which can clear the value on blur
		    super.finishEditing(parent, value, key, null);
	  }
	  
	  @Override
	  protected void onNavigationKey(Context context, Element parent, T value, NativeEvent event,
	      ValueUpdater<T> valueUpdater) {

	    if (isReadOnly()) {
	      return;
	    }

	    if (event.getKeyCode() == KeyCodes.KEY_SPACE) {
	        if (isExpanded()) {
		          event.preventDefault();
	          event.stopPropagation();
	          T selectedItem = getListView().getSelectionModel().getSelectedItem();
	          onSelect(selectedItem);
	        }
	    } else
	    	super.onNavigationKey(context, parent, value, event, valueUpdater);	    
	  }
	  
	  private String getRenderedValue(T item) {
		    return getPropertyEditor().render(item);
	  }	
}
