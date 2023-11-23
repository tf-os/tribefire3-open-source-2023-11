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
package com.braintribe.devrock.mungojerry.dialog.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.devrock.mungojerry.dialog.MungojerryDialog;

public class GwtModuleScannerMasterTab extends GenericGwtViewerTab implements SelectionListener{
		
	private Image warning;
	private TreeItem lastSelectedItem;

	
	public GwtModuleScannerMasterTab(Display display) {		
		super(display);
		setColumnNames(new String [] {"Modules", });
		setColumnWeights( new int [] {300,});		
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( MungojerryDialog.class, "warning.png");
		warning = imageDescriptor.createImage();
	}
	
	@Override
	public void dispose() {
		warning.dispose();
	}
	
	
	@Override
	public Composite createControl(Composite parent) {
		Composite composite = super.createControl(parent);		
		tree.addSelectionListener( this);
		return composite;		
	}
	
	
	private void buildItem( Object parent, ModuleCheckProtocol protocol) {
		
		TreeItem item = null;
		if (parent instanceof Tree)
			item = new TreeItem( (Tree) parent, SWT.NONE);
		else
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		
		List<String> texts = new ArrayList<String>();
		texts.add( protocol.getModuleName());		
		item.setText( texts.toArray( new String[0]));
		
		item.setData( "protocol", protocol);
		
		if (protocol.getUnsatisfiedDependencies().size() > 0) {		
			item.setImage( warning);
		}				
	}
	
	public void setup() {	
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				tree.removeAll();							
				// get scan result here... 				
				Collection<ModuleCheckProtocol> protocols = analysisController.getProtocols();
				if (protocols != null) {		
					for (ModuleCheckProtocol protocol : protocols) {
						buildItem( tree, protocol);
					}
				}
				
				// 
				if (tree.getItemCount() > 0) {
					tree.setSelection( tree.getItem(0));		
					ModuleCheckProtocol protocol = getSelectedProtocol();
					broadcastSelection(protocol);
				}
			}
		});
	}
						

	@Override
	public void setVisible( boolean visible) {
		if (visible == false)
			return;
		
		setup();

	}
	

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {				
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == tree) {
			ModuleCheckProtocol protocol = getSelectedProtocol();
			if (protocol != null) {
				broadcastSelection(protocol);
			}			
		}			
	}
	
	private ModuleCheckProtocol getSelectedProtocol() {
		TreeItem [] items = tree.getSelection();
		if (
				(items != null) &&
				(items.length >= 1) &&
				(lastSelectedItem != items[0])
			   ) {
				final TreeItem suspect = items[0];
				ModuleCheckProtocol protocol = (ModuleCheckProtocol) suspect.getData( "protocol");
				// store last
				lastSelectedItem = suspect;
				return protocol;
			}
		return null;
	}
	
	public ModuleCheckProtocol getCurrentlySelectedProtocol() {
		if (lastSelectedItem != null) {
			return (ModuleCheckProtocol) lastSelectedItem.getData( "protocol");
		}
		else {
			return getSelectedProtocol();
		}
	}

	private void broadcastSelection(ModuleCheckProtocol protocol) {		
		String moduleName = protocol.getModuleName();
		// display selected modules' data
		parentPage.showDependencies(moduleName);
		// display errors.. 
		parentPage.showErrors( protocol);

	}
	  

	
	
	

}
