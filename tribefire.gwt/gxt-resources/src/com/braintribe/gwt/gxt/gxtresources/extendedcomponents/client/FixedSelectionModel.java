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

import java.util.Collections;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent.BeforeShowContextMenuHandler;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridSelectionModel;

/**
 * Fixes problem when selecting row and a horizontal scroll is performed in a wrong way.
 * @author michel.docouto
 */
public class FixedSelectionModel<M> extends TreeGridSelectionModel<M> {
	
	private HandlerRegistration rightClickHandlerRegistration;

	@Override
	protected void onKeyDown(NativeEvent ne) {
		XEvent e = ne.<XEvent> cast();
		if (Element.is(ne.getEventTarget()) && !grid.getView().isSelectableTarget(Element.as(ne.getEventTarget()))) {
			return;
		}
		if (listStore.size() == 0) {
			return;
		}
		if (!e.getCtrlOrMetaKey() && selected.size() == 0 && getLastFocused() == null) {
			select(0, false);
		} else {
			int idx = listStore.indexOf(getLastFocused());
			if (idx >= 0 && (idx + 1) < listStore.size()) {
				XElement scroller = grid.getView().getScroller();
				int left = scroller.getScrollLeft();
				
				if (e.getCtrlOrMetaKey() || (e.getShiftKey() && isSelected(listStore.get(idx + 1)))) {
					if (!e.getCtrlOrMetaKey()) {
						deselect(idx);
					}

					M lF = listStore.get(idx + 1);
					if (lF != null) {
						setLastFocused(lF);
						grid.getView().focusCell(idx + 1, 0, false);
					}

				} else {
					if (e.getShiftKey() && lastSelected != getLastFocused()) {
						grid.getView().focusCell(idx + 1, 0, false);
						select(listStore.indexOf(lastSelected), idx + 1, true);
					} else {
						if (idx + 1 < listStore.size()) {
							grid.getView().focusCell(idx + 1, 0, false);
							selectNext(e.getShiftKey());
						}
					}
				}
				
				scroller.setScrollLeft(left);
			} else {
				//grid.getView().onNoNext(idx);
			}
		}

		e.preventDefault();
	}
	
	@Override
	protected void onKeyUp(NativeEvent e) {
		XEvent xe = e.<XEvent> cast();
		if (Element.is(e.getEventTarget()) && !grid.getView().isSelectableTarget(Element.as(e.getEventTarget()))) {
			return;
		}
		int idx = listStore.indexOf(getLastFocused());
		if (idx >= 1) {
			GridView<M> view = grid.getView();
			Element lastFocusedCell = getFocusedCell(view);
			int left = view.getScroller().getScrollLeft();
			int colIndex = 0;
			if (lastFocusedCell != null)
				colIndex = view.findCellIndex(lastFocusedCell, null);
			
			if (xe.getCtrlOrMetaKey() || (e.getShiftKey() && isSelected(listStore.get(idx - 1)))) {
				if (!xe.getCtrlOrMetaKey()) {
					deselect(idx);
				}

				M lF = listStore.get(idx - 1);
				if (lF != null) {
					setLastFocused(lF);
					grid.getView().focusCell(idx - 1, colIndex, false);
				}
			} else {
				if (e.getShiftKey() && lastSelected != getLastFocused()) {
					grid.getView().focusCell(idx - 1, colIndex, false);
					select(listStore.indexOf(lastSelected), idx - 1, true);
				} else {
					if (idx > 0) {
						grid.getView().focusCell(idx - 1, colIndex, false);
						selectPrevious(e.getShiftKey());
					}
				}

			}
			
			view.getScroller().setScrollLeft(left);
		} else {
			//grid.getView().onNoPrev();
		}
		e.preventDefault();
	}
	
	/**
	 * Overriding the method to fix the problem with the selection with control, which I've filled a bug report and it is fixed in 4.0.3
	 */
	@Override
	protected void onRowMouseDown(RowMouseDownEvent event) {
		if (Element.is(event.getEvent().getEventTarget()) && !grid.getView().isSelectableTarget(Element.as(event.getEvent().getEventTarget()))) {
			return;
		}

		if (isLocked()) {
			return;
		}

		int rowIndex = event.getRowIndex();
		int colIndex = event.getColumnIndex();
		if (rowIndex == -1) {
			deselectAll(); //Change1
			return;
		}

		setFocusCellCalled(false);
		mouseDown = true;

		XEvent e = event.getEvent().<XEvent> cast();

		// it is important the focusCell be called once, and only once in onRowMouseDown and onRowMouseClick
		// everything but multi select with the control key pressed is handled in mouse down

		if (event.getEvent().getButton() == Event.BUTTON_RIGHT) {
			if (selectionMode != SelectionMode.SINGLE && isSelected(listStore.get(rowIndex))) {
				mouseDown = false;
				return;
			}
			grid.getView().focusCell(rowIndex, colIndex, false);
			select(rowIndex, false);
			setFocusCellCalled(true);
		} else {
			M sel = listStore.get(rowIndex);
			if (sel == null) {
				mouseDown = false;
				return;
			}

			boolean isSelected = isSelected(sel);
			boolean isMeta = e.getCtrlOrMetaKey();
			boolean isShift = event.getEvent().getShiftKey();

			switch (selectionMode) {
				case SIMPLE:
					grid.getView().focusCell(rowIndex, colIndex, false);
					setFocusCellCalled(true);
					if (!isSelected) {
						select(sel, true);
					} else if (isSelected && deselectOnSimpleClick) {
						deselect(sel);
					}
					break;

				case SINGLE:
					grid.getView().focusCell(rowIndex, colIndex, false);
					setFocusCellCalled(true);
					if (isSelected && isMeta) {
						deselect(sel);
					} else if (!isSelected) {
						select(sel, false);
					}
					break;

				case MULTI:
					if (isSelected && isMeta) { //Change2
						setFocusCellCalled(true);
			          // reset the starting location of the click
			          setIndexOnSelectNoShift(rowIndex);
			          //doDeselect(Collections.singletonList(sel), false);

			        } else if (isMeta) { //Change3
			        	setFocusCellCalled(true);
			            // reset the starting location of the click
			            setIndexOnSelectNoShift(rowIndex);
			            //doSelect(Collections.singletonList(sel), true, false);
			        } else if (isSelected && !isMeta && !isShift && selected.size() > 1) { //change4
						doSelect(Collections.singletonList(sel), false, false);
						setIndexOnSelectNoShift(rowIndex);
					} else if (isShift && lastSelected != null) { //change5
						int start;
						int end;
						// This deals with flipping directions
						if (getIndexOnSelectNoShift() < rowIndex) {
							start = getIndexOnSelectNoShift();
							end = rowIndex;
						} else {
							start = rowIndex;
							end = getIndexOnSelectNoShift();
						}
						setFocusCellCalled(true);
						select(start, end, false);
						rowIndex = end; // focus on the last selected row
						
						int last = listStore.indexOf(lastSelected);
						grid.getView().focusCell(last, colIndex, false);
					} else if (!isSelected) { //change6
						// reset the starting location of multi select
						setIndexOnSelectNoShift(rowIndex);
						setFocusCellCalled(true);
						doSelect(Collections.singletonList(sel), false, false);
					}
					
					if (!getFocusCellCalled()) { // change7
						grid.getView().focusCell(rowIndex, colIndex, false);
					}
					
					break;
			}
		}

		mouseDown = false;
	}
	
	/**
	 * Overriding to fix the problem where the right mouse click wouldn't fire the selection event
	 */
	@Override
	public void bindGrid(Grid<M> grid) {
		if (rightClickHandlerRegistration != null) {
			rightClickHandlerRegistration.removeHandler();
			rightClickHandlerRegistration = null;
		}
		
		super.bindGrid(grid);
		
		if (grid == null)
			return;
		
		rightClickHandlerRegistration = grid.addBeforeShowContextMenuHandler(new BeforeShowContextMenuHandler() {
			@Override
			public void onBeforeShowContextMenu(BeforeShowContextMenuEvent event) {
				if (fireSelectionChangeOnClick) {
					fireSelectionChange();
					fireSelectionChangeOnClick = false;
				}
			}
		});
	}
	
	// @formatter:off
	private native Element getFocusedCell(GridView<M> view) /*-{
		return view.@com.sencha.gxt.widget.core.client.grid.GridView::focusedCell;
	}-*/;
	
	private native void setFocusCellCalled(boolean called) /*-{
		this.@com.sencha.gxt.widget.core.client.grid.GridSelectionModel::focusCellCalled = called;
	}-*/;
	
	private native boolean getFocusCellCalled() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.GridSelectionModel::focusCellCalled;
	}-*/;
	
	private native void setIndexOnSelectNoShift(int index) /*-{
		this.@com.sencha.gxt.widget.core.client.grid.GridSelectionModel::indexOnSelectNoShift = index;
	}-*/;
	
	private native int getIndexOnSelectNoShift() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.GridSelectionModel::indexOnSelectNoShift;
	}-*/;
	// @formatter:on

}
