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
package com.braintribe.devrock.artifactcontainer.ui.wizard;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.NameColumnLabelProvider;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.PathColumnLabelProvider;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.RepositoryContentProvider;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.RepositoryKindColumnLabelProvider;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class RepositoryPairingSelectionDialog  extends Dialog implements SelectionListener {
	
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static final String SOURCE_LOCAL_REPRESENTATION_CHOICE = "Select working copy location for copied/cloned artifact";
	private Shell parentShell;
	private Font bigFont;
	
	private Image svnImage;
	private Image gitImage;
	
	private SourceRepositoryPairing selection;
	private List<SourceRepositoryPairing> pairings;
	
	private CommonTableViewer repositoryTableViewer;
	private Table repositoryTable;
	
	
	@Configurable @Required
	public void setPairings(List<SourceRepositoryPairing> pairings) {
		this.pairings = pairings;
	}
	
	public SourceRepositoryPairing getSelection() {
		return selection;
	}


	public RepositoryPairingSelectionDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_svn.gif");
		svnImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_git.gif");
		gitImage = imageDescriptor.createImage();
		
		

	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        Composite svnGroup = new Composite( composite, SWT.NONE);
		svnGroup.setLayout( layout);
		svnGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
	
		Label svnLabel = new Label( svnGroup, SWT.NONE);
    	svnLabel.setText( "Source repository pairings");
    	svnLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	svnLabel.setFont(bigFont);
    	    	
    	
    	Composite repositoryComposite = new Composite( svnGroup, SWT.NONE);
    	GridLayout repoCompositelayout = new GridLayout();
    	repoCompositelayout.numColumns = 2;
    	repositoryComposite.setLayout( repoCompositelayout);
    	repositoryComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
    	
    	Composite tableComposite = new Composite(repositoryComposite, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1));
		
    	repositoryTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[4];
		columnData[0] = new CommonTableColumnData("name", 100, 100, "Repository name", new NameColumnLabelProvider());
		columnData[1] = new CommonTableColumnData("local", 200, 200, "Working copy directory", new PathColumnLabelProvider(true));
		columnData[2] = new CommonTableColumnData("url", 200, 200, "Remote URL", new PathColumnLabelProvider(false));		
		columnData[3] = new CommonTableColumnData("kind", 50, 50, "Kind of source repository", new RepositoryKindColumnLabelProvider());
		
		repositoryTableViewer.setup(columnData);
		
		repositoryTable = repositoryTableViewer.getTable();
		repositoryTable.setHeaderVisible(true);
		repositoryTable.setLinesVisible(true);
		
		
		repositoryTable.addSelectionListener( this);
	
		RepositoryContentProvider contentProvider = new RepositoryContentProvider();    
		contentProvider.setPairings( pairings);
		repositoryTableViewer.setContentProvider(contentProvider);
		repositoryTableViewer.setInput( pairings);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	int ht = (repositoryTable.getItemHeight() * 15) + repositoryTable.getHeaderHeight();
    	layoutData.heightHint = repositoryTable.computeSize(SWT.DEFAULT, ht).y;
    	repositoryTable.setLayoutData( layoutData);
        
        parentShell.layout(true);
		return composite;
	}
	

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( SOURCE_LOCAL_REPRESENTATION_CHOICE);
		super.configureShell(newShell);
	}

	@Override
	protected Point getInitialSize() {		
		return new Point( 600, 350);
	}

	@Override
	public boolean close() {
		bigFont.dispose();
		svnImage.dispose();
		gitImage.dispose();	
		//
		//
		//
		
		return super.close();
	}

	

	@Override
	protected void okPressed() {
		if (selection != null)
			super.okPressed();
		else {
			MessageDialog.openError(getShell(), SOURCE_LOCAL_REPRESENTATION_CHOICE, "A source repository pair needs to be selected");
		}
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		if (arg0.widget == repositoryTable) {
			int index  = repositoryTable.getSelectionIndex();
			if (index >= 0) {
				selection = pairings.get(index);
			}
		}		
	}
	
	

}
