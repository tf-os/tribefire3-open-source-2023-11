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
package com.braintribe.gwt.gme.notification.client;

import java.util.Collections;

import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ListViewSelectionModel;
import com.sencha.gxt.widget.core.client.event.XEvent;

public class NotificationListViewSelectionModel<M> extends ListViewSelectionModel<M>{
	  private int indexOnSelectNoShift;

	  @Override
	  protected void onMouseClick(NativeEvent event) {
		    //RVE - just get indexOnSelectNoShift
		    XEvent e = event.cast();
		    XElement target = e.getEventTargetEl();

		    if (isLocked() || isInput(target)) {
		      return;
		    }

		    int index = listView.findElementIndex(target);

		    if (selectionMode == SelectionMode.MULTI) {
		      M sel = listStore.get(index);
		      if (e.getCtrlOrMetaKey() && isSelected(sel)) {
		        indexOnSelectNoShift = index;
		      } else if (e.getCtrlOrMetaKey()) {
		        indexOnSelectNoShift = index;
		      } 
		    }
		    
		    super.onMouseClick(event);
		  }
	
	  @Override
	  protected void onMouseDown(NativeEvent event) {
		    XEvent e = event.<XEvent>cast();
		    XElement target = e.getEventTargetEl();
		    int selIndex = listView.findElementIndex(target);

		    if (selIndex == -1 || isLocked() || isInput(target)) {
		      return;
		    }

		    mouseDown = true;

		    if (e.isRightClick()) {
		      if (selectionMode != SelectionMode.SINGLE && isSelected(listStore.get(selIndex))) {
		        return;
		      }
		      select(selIndex, false);
              //RVE commented out - makes flickering and scrolling problems
		      //listView.focusItem(selIndex);
		    } else {
		      M sel = listStore.get(selIndex);
		      if (sel == null) {
		        return;
		      }
		      
		      boolean isSelected = isSelected(sel);
		      boolean isMeta = e.getCtrlOrMetaKey();
		      boolean isShift = e.getShiftKey();
		      
		      switch (selectionMode) {
		        case SIMPLE:
                  //RVE commented out - makes flickering and scrolling problems
		          //listView.focusItem(selIndex);
		          if (!isSelected) {
		            select(sel, true);
		          } else if (isSelected && deselectOnSimpleClick) {
		            deselect(sel);
		          }
		          break;
		          
		        case SINGLE:
		          if (isMeta && isSelected) {
		            deselect(sel);
		          } else if (!isSelected) {
                    //RVE commented out - makes flickering and scrolling problems
		            //listView.focusItem(selIndex);
		            select(sel, false);
		          }
		          break;
		          
		        case MULTI:
		          if (isMeta) {
		            break;
		          }
		          
		          if (isShift && lastSelected != null) {
		            //int last = listStore.indexOf(lastSelected);
                    //RVE commented out - makes flickering and scrolling problems
		            //listView.focusItem(last);

		            int start;
		            int end;
		            // This deals with flipping directions
		            if (indexOnSelectNoShift < selIndex) {
		              start = indexOnSelectNoShift;
		              end = selIndex;
		            } else {
		              start = selIndex;
		              end = indexOnSelectNoShift;
		            }
		            
		            select(start, end, false);
		          } else if (!isSelected) {
		            // reset the starting location of multi select
		            indexOnSelectNoShift = selIndex;

                    //RVE commented out - makes flickering and scrolling problems
		            //listView.focusItem(selIndex);
		            doSelect(Collections.singletonList(sel), false, false);
		          }
		          break;
		      }
		    }

		    mouseDown = false;
		  }	
}
