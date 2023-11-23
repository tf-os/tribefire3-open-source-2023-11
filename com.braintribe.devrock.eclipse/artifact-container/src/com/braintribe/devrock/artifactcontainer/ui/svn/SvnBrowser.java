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
package com.braintribe.devrock.artifactcontainer.ui.svn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.process.listener.MessageType;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.build.process.repository.process.SourceRepositoryAccessException;
import com.braintribe.build.process.repository.process.svn.SvnUtil;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter.PainterKey;
import com.braintribe.plugin.commons.ui.tree.TreeItemTooltipProvider;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.dom.iterator.FilteringElementIterator;
import com.braintribe.utils.xml.dom.iterator.filters.AttributeValueFilter;
import com.braintribe.utils.xml.dom.iterator.filters.TagNameFilter;

public class SvnBrowser extends Dialog implements ProcessNotificationListener {
	private static final String MARKER_SYMBOLIC_FILE = "SYMBOLIC_FILE";
	private final static String MARKER_DEFERRED = "loading..";
	private String initialStorage;
	private Font bigFont;
	private String selectedFile;
	private Tree tree;	
	private Display display;
	
	private Image fileImage;
	private Image folderImage;
	
	

	public SvnBrowser(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
		display = parentShell.getDisplay();
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( SvnBrowser.class, "file_obj.gif");
		fileImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SvnBrowser.class, "fldr_obj.png");
		folderImage = imageDescriptor.createImage();		
	}
	
	

	public String getSelectedFile() {
		return selectedFile;
	}
	@Configurable
	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
	}

	@Configurable @Required
	public void setInitialStorage(String initialStorage) {
		this.initialStorage = initialStorage;
	}
	
	@Override
	public boolean close() {
		bigFont.dispose();
		fileImage.dispose();
		folderImage.dispose();
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( "SVN contents of [" + initialStorage + "]");
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point( 400, 300);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {		

		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = new Composite( parent, SWT.NONE);
		
		int nColumns= 4;
	    GridLayout layout= new GridLayout();
	    layout.numColumns = nColumns;
	    composite.setLayout( layout);
	    composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
	    
		   // file list 
	    Composite fileListComposite = new Composite( composite, SWT.NONE);
	    fileListComposite.setLayout( layout);
	    fileListComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
	           
	    Label fileListLabel = new Label( fileListComposite, SWT.NONE);
	    fileListLabel.setText( "select file to adapt");
	    fileListLabel.setFont( bigFont);
	    fileListLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
	    		
	    
		tree = new Tree ( fileListComposite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible( false);
		tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,4));
							
		tree.addListener(SWT.Expand, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				final TreeItem item = (TreeItem) event.item;
				TreeItem [] children = item.getItems();
				if (
					(children != null) &&
					(children.length == 1) 
				   ) {
					final TreeItem suspect = children[0];
					String name = suspect.getText(0);
					if (name.equalsIgnoreCase( MARKER_DEFERRED) == false)
						return;
					// 
					display.asyncExec( new Runnable() {
						
						@Override
						public void run() {
							suspect.dispose();
							SymbolicFile file = (SymbolicFile) item.getData( MARKER_SYMBOLIC_FILE);
							final List<SymbolicFile> files = scan( file.path + "/" + file.name);
							if (files == null || files.size() == 0)
								return;
							display.asyncExec( new Runnable() {
								
								@Override
								public void run() {
									for (SymbolicFile symbolicFile : files) {
										buildEntry( item, symbolicFile);				
									}				
								}

							});
							
						}
					});					
				}
				
			}
		});
		
		TreeItemTooltipProvider.attach(tree, PainterKey.tooltip.name());
		
		initializeTree();
		// initialize		
		return composite;
	}
	
	private void initializeTree(){
		final List<SymbolicFile> files = scan( null);
		if (files == null || files.size() == 0)
			return;
		display.asyncExec( new Runnable() {
			
			@Override
			public void run() {
				for (SymbolicFile symbolicFile : files) {
					buildEntry(tree, symbolicFile);				
				}				
			}

		});
	}
	private void buildEntry(Object parent, SymbolicFile symbolicFile) {
		
		TreeItem item;
		if (parent instanceof Tree) {
			item = new TreeItem( (Tree) parent, SWT.NONE);
		}
		else {
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		}
		item.setText( symbolicFile.name);
		item.setData( MARKER_SYMBOLIC_FILE, symbolicFile);
		item.setData( PainterKey.tooltip.name(), symbolicFile.path + "/" + symbolicFile.name);
	
		
		if (symbolicFile.directory) {
			item.setImage( folderImage);
			TreeItem child = new TreeItem( item, SWT.NONE);
			child.setText( MARKER_DEFERRED);
		}
		else {
			item.setImage( fileImage);
		}
	}

	private class SymbolicFile {
		String name;
		boolean directory;
		String path;
	}
	
	private List<SymbolicFile> scan(String url) {
		
		Document svnResult;
		try {
			if (url == null){
				url = initialStorage;
			}			
			svnResult = SvnUtil.list( this, url);				
			
			List<SymbolicFile> result = new ArrayList<SymbolicFile>();
			Element parent = DomUtils.getElementByPath( svnResult.getDocumentElement(), "list", false);
			if (parent == null) {
				return result;
			}
		/*	@SuppressWarnings("unchecked")
			FilteringElementIterator iterator = new FilteringElementIterator( parent, 
						new ConjunctionFilter<>( 
								new TagNameFilter("entry"), 
								new DisjunctionFilter<>( 
										new AttributeValueFilter("kind", "file"),
										new AttributeValueFilter("kind", "dir")
								)
						)
			);
		*/	
			FilteringElementIterator iterator = new FilteringElementIterator( parent, 					
				new TagNameFilter("entry").and( 
						new AttributeValueFilter("kind", "file").or(
						new AttributeValueFilter("kind", "dir"))
				)
			);
			
			String path = parent.getAttribute( "path");
			// build list 
			while (iterator.hasNext()) {
				Element fileElement = iterator.next();
				SymbolicFile file = new SymbolicFile();
				if (fileElement.getAttribute("kind").equalsIgnoreCase("dir")) {
					file.directory = true;
				}
				else {
					file.directory = false;
				}
				String name = DomUtils.getElementValueByPath(fileElement, "name", false);					
				file.name = name;
				file.path = path;
				result.add( file);				
			}
			return result;
		} catch (SourceRepositoryAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	@Override
	protected void okPressed() {
		TreeItem [] items = tree.getSelection();
		if (items != null && items.length > 0) {
			SymbolicFile file = (SymbolicFile) items[0].getData(MARKER_SYMBOLIC_FILE);
			if (!file.directory) {
				String path = file.path;
				if (path.equalsIgnoreCase(initialStorage)) {
					selectedFile = file.name;
				} else {
					selectedFile = path.substring( initialStorage.length()) + "/" + file.name;
				}
			}
		}
		super.okPressed();
	}


	@Override
	public void acknowledgeProcessNotification(MessageType arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
}
