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
package com.braintribe.devrock.greyface.generics.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * helper to enable drag and drop of {@link TreeItem} in a {@link Tree}
 * supports both dropping only on top level (direct tree children) and reordering (attaching to selected child)
 * 
 * @author pit
 *
 */
public class TreeDragger {
	public enum DragParentType {tree, all}
	private TreeItem draggedItem;
	
	public static void attach(Tree tree, Display display, String [] keys, DragParentType parentType) {
		@SuppressWarnings("unused")
		TreeDragger dragger = new TreeDragger(tree, display, keys, parentType);
	}
	
	public TreeDragger( final Tree tree, final Display display, final String [] keys, final DragParentType parentType) {
	
		// fake a text drag and drop transfer 
		Transfer[] types = new Transfer[] { TextTransfer.getInstance()};
	    int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
	
	    final DragSource source = new DragSource(tree, operations);
	    source.setTransfer(types);
	    
	    final TreeItem[] dragSourceItem = new TreeItem[1];
	    source.addDragListener(new DragSourceAdapter() {
	      @Override
			public void dragStart(DragSourceEvent event) {
		        TreeItem[] selection = tree.getSelection();
		        if (
		        		selection.length > 0 && 
		        		selection[0].getItemCount() == 0
		        	) {
		          event.doit = true;
		          dragSourceItem[0] = selection[0];
		        } 
		        else {
		          event.doit = false;
		        }
		      }
		      @Override
		      public void dragSetData(DragSourceEvent event) {
		    	// text to satisfy faked transfer 
		        event.data = dragSourceItem[0].getText();
		        // actual drag info 
		        draggedItem = dragSourceItem[0];
		      }
		      @Override
		      public void dragFinished(DragSourceEvent event) {
		        if (event.detail == DND.DROP_MOVE) {	        
		        	dragSourceItem[0].dispose();	          
		        }
		        dragSourceItem[0] = null;
		      }
		    });
	
	    DropTarget target = new DropTarget(tree, operations);
	    target.setTransfer(types);
	    target.addDropListener(new DropTargetAdapter() {
	    	@Override
		      public void dragOver(DropTargetEvent event) {
		        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
		        if (event.item != null) {
		        	TreeItem item = (TreeItem) event.item;
		        	Point pt = display.map(null, tree, event.x, event.y);
		        	Rectangle bounds = item.getBounds();
		        	if (pt.y < bounds.y + bounds.height / 3) {
		        		event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
		        	} 
		        	else if (pt.y > bounds.y + 2 * bounds.height / 3) {
		        		event.feedback |= DND.FEEDBACK_INSERT_AFTER;
		        	} 
		        	else {
		            event.feedback |= DND.FEEDBACK_SELECT;
		          }
		        }
		   }
	      @Override
	      public void drop(DropTargetEvent event) {	    	  
	        if (event.data == null) {
		         event.detail = DND.DROP_NONE;
		         return;
	        }        
	        //String text = (String) event.data;	        
	        if (event.item == null) {
	          TreeItem item = new TreeItem(tree, SWT.NONE);
	          transfer( draggedItem, item, keys);	          
	        } 
	        else {
	        	TreeItem item = (TreeItem) event.item;
	        	Point pt = display.map(null, tree, event.x, event.y);
	        	Rectangle bounds = item.getBounds();
	        	TreeItem parent = item.getParentItem();
	        	if (parent != null) {
	        		TreeItem[] items = parent.getItems();
	        		int index = 0;
	        		for (int i = 0; i < items.length; i++) {
	        			if (items[i] == item) {
	        				index = i;
	        				break;
	        			}
	        		}
	        		switch (parentType) {
	        			case tree:
	        				if (pt.y < bounds.y + bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else  {
			        			TreeItem newItem = new TreeItem(parent, SWT.NONE, index + 1);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
	        				break;
	        			case all:	        			
			        		if (pt.y < bounds.y + bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else if (pt.y > bounds.y + 2 * bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(parent, SWT.NONE, index + 1);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else {
			        			TreeItem newItem = new TreeItem(item, SWT.NONE);
			        			transfer( draggedItem, newItem, keys);	       
			        		}
			        		break;
	        		}
	        	} 
	        	else {
	        		TreeItem[] items = tree.getItems();
	        		int index = 0;
	        		for (int i = 0; i < items.length; i++) {
	        			if (items[i] == item) {
	        				index = i;
	        				break;
	        			}
	        		}
	        		switch (parentType) {
	        			case tree:
	        				if (pt.y < bounds.y + bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else  {
			        			TreeItem newItem = new TreeItem(tree, SWT.NONE, index + 1);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
	        				break;
	        			case all:
			        		if (pt.y < bounds.y + bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else if (pt.y > bounds.y + 2 * bounds.height / 3) {
			        			TreeItem newItem = new TreeItem(tree, SWT.NONE, index + 1);
			        			transfer( draggedItem, newItem, keys);	       
			        		} 
			        		else {
			        			TreeItem newItem = new TreeItem(item, SWT.NONE);
			        			transfer( draggedItem, newItem, keys);	       
			        		}
			        		break;
	        		}
	        	}
	        }
	      }
	    });    
	}
	
	private void transfer(TreeItem source, TreeItem target, String [] keys) {
		target.setText( source.getText());
		target.setImage( source.getImage());
		for (String key : keys) {
			target.setData( key, source.getData( key));
		}		
	}
}
