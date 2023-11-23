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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeItemTooltipProvider {

	private Tree tree;
	private static final String CODE_TREEITEM = "_TREEITEM";
	private String tooltipCode;
	private Display display;
	private Shell shell;
	
	public static void attach( Tree tree, String code) {
		@SuppressWarnings("unused")
		TreeItemTooltipProvider pvd = new TreeItemTooltipProvider(tree, code);
	}
	
	public TreeItemTooltipProvider( Tree tree, String tooltipCode) {
		this.tree = tree;
		display = tree.getDisplay();
		shell = new Shell( display);
		this.tooltipCode = tooltipCode;
		setup();
	}
	
	private void setup() {

		 final Listener labelListener = new Listener() {
			 @Override
		      public void handleEvent(Event event) {
		        Label label = (Label) event.widget;
		        Shell shell = label.getShell();
		        switch (event.type) {
		        case SWT.MouseDown:
		          Event e = new Event();
		          TreeItem selectedItem = (TreeItem) label.getData( CODE_TREEITEM); 
		          e.item = selectedItem;
		          TreeItem [] selectedItems = tree.getSelection();
		          if (
		        		  selectedItems != null &&
		        		  selectedItems.length > 0
		        	 ) {
			          List<TreeItem> selectedItemList = new ArrayList<TreeItem>( selectedItems.length);		          
			          for (TreeItem item : selectedItems) {
			        	  selectedItemList.add( item);		        		  
			          }
			          if (selectedItemList.contains( selectedItem)) {
			        	  selectedItemList.remove( selectedItem); 
			          } 
			          else {
			        	  selectedItemList.add( selectedItem);
			          }		     
			          tree.setSelection( selectedItemList.toArray( new TreeItem[0]));
		          } 
		          else {
		        	  tree.setSelection( new TreeItem[] { selectedItem});
		          }
			          
		          tree.notifyListeners(SWT.Selection, e);
		          
		        // $FALL-THROUGH$		        
		        case SWT.MouseExit:
		          shell.dispose();
		          break;
		        }
		      }
		    };

		    Listener treeListener = new Listener() {
		      Shell tip = null;

		      Label label = null;
		      @Override
		      public void handleEvent(Event event) {
		        switch (event.type) {
		        case SWT.Dispose:
		        case SWT.KeyDown:
		        case SWT.MouseMove: {
		          if (tip == null)
		            break;
		          tip.dispose();
		          tip = null;
		          label = null;
		          break;
		        }
		        case SWT.MouseHover: {
		          TreeItem item = tree.getItem(new Point(event.x, event.y));
		          if (
		        		  item != null &&
		        		  item.getData( tooltipCode) != null
		        	 ){
		            if (tip != null && !tip.isDisposed()) {
		              tip.dispose();
		            }
		            tip = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
		            tip.setLayout(new FillLayout());
		            label = new Label(tip, SWT.NONE);
		            label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		            label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		            label.setData( CODE_TREEITEM, item);
		            label.setText( (String) item.getData( tooltipCode));
		            label.addListener(SWT.MouseExit, labelListener);
		            label.addListener(SWT.MouseDown, labelListener);
		            
		            Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		            Rectangle rect = item.getBounds(0);
		            Point pt = tree.toDisplay(rect.x, rect.y);
		            tip.setBounds(pt.x, pt.y, size.x, size.y);
		            tip.setVisible(true);
		          }
		        }
		        }
		      }
		    };
		    
		    tree.addListener(SWT.Dispose, treeListener);
		    tree.addListener(SWT.KeyDown, treeListener);
		    tree.addListener(SWT.MouseMove, treeListener);
		    tree.addListener(SWT.MouseHover, treeListener); 
	}
}
