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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.bias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.RepositoryKindColumnLabelProvider;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class PublishingCandidateBiasDialog extends Dialog implements ModifyListener {

	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static final String REPOSITORY_WIZARD = "Artifact origin bias wizard";
	private Shell parentShell;
	private Font bigFont;

	private ArtifactBias selection;
	private Collection<ArtifactBias> identifications;
	private Text message;
	private Label messageLabel;
	
	private StringEditor groupIdEditor;
	private StringEditor artifactIdEditor;
	private Image warningImage;
	
	private CommonTableViewer repoTableViewer;
	private Table repoTable;
	private Button addRepoButton;
	private Button removeRepoButton;
	
	private Image addImage;

	private Image removeImage;
	

	public ArtifactBias getSelection() {
		return selection;
	}

	
	@Configurable
	public void setSelection(ArtifactBias selection) {
		this.selection = selection;
	}

	@Configurable @Required
	public void setPairings(Collection<ArtifactBias> identifications) {
		this.identifications = identifications;
	}


	public PublishingCandidateBiasDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "warning.png");
		warningImage = imageDescriptor.createImage();
	
		imageDescriptor = ImageDescriptor.createFromFile( PublishingCandidatePreferencesPage.class, "add.gif");
		addImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( PublishingCandidatePreferencesPage.class, "remove.gif");
		removeImage = imageDescriptor.createImage();
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
                
        //
      
        Composite repositoryComposite = new Composite(composite, SWT.NONE);
        repositoryComposite.setLayout( layout);
        repositoryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        
        Label repositoryLabel = new Label( repositoryComposite, SWT.NONE);
        repositoryLabel.setText("Artifact origin bias");
        repositoryLabel.setFont( bigFont);
        
        groupIdEditor = new StringEditor();
    	Composite nameComposite = groupIdEditor.createControl( composite, "Group   :");
		nameComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));	
		if (selection != null) {
			groupIdEditor.setSelection( selection.getIdentification().getGroupId());
		}
		groupIdEditor.addModifyListener( this);

	    
		Composite remoteComposite = new Composite(composite, SWT.NONE);
        remoteComposite.setLayout( layout);
        remoteComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		
		artifactIdEditor = new StringEditor();
    	Composite urlComposite = artifactIdEditor.createControl( remoteComposite, "Artifact:");
		urlComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		if (selection != null) {
			if (selection.getIdentification().getArtifactId().equalsIgnoreCase(".*")) {
				artifactIdEditor.setSelection("");
			}
			else {
				artifactIdEditor.setSelection( selection.getIdentification().getArtifactId());
			}
		}
		
		// table ... 
	   	Composite tableComposite = new Composite(composite, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1));
		
    	repoTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[2];
		columnData[0] = new CommonTableColumnData("repository", 100, 100, "repositories", new RepositoryExpressionLabelProvider(), new RepositoryExpressionColumnEditingSupport(repoTableViewer));
		columnData[1] = new CommonTableColumnData("status", 50, 50, "stati", new RepositoryActivityColumnLabelProvider(), new RepositoryActivityColumnEditingSupport(repoTableViewer));
		repoTableViewer.setup(columnData);
		
		repoTable = repoTableViewer.getTable();
		repoTable.setHeaderVisible(true);
		repoTable.setLinesVisible(true);
		
		repoTable.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget == repoTable) {
					if (repoTable.getSelection() != null) {
						removeRepoButton.setEnabled(true);
					}
					else {
						removeRepoButton.setEnabled( false);
					}						
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
				
		
		RepoExpressionContentProvider contentProvider = new RepoExpressionContentProvider();
		List<RepositoryExpression> activeRepos = null;
		if (selection != null) { 		
			activeRepos = selection.getActiveRepositories().stream().map( s -> new RepositoryExpression( true, s)).collect(Collectors.toList());
			List<RepositoryExpression> inactiveRepos = selection.getInactiveRepositories().stream().map( s -> new RepositoryExpression( false, s)).collect(Collectors.toList());
			activeRepos.addAll(inactiveRepos);
		}
		else {
			activeRepos = new ArrayList<>();
		}
		contentProvider.setBiases( activeRepos);
		
		repoTableViewer.setContentProvider(contentProvider);
		repoTableViewer.setInput( activeRepos);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	int ht = (repoTable.getItemHeight() * 15) + repoTable.getHeaderHeight();
    	layoutData.heightHint = repoTable.computeSize(SWT.DEFAULT, ht).y;
    	repoTable.setLayoutData( layoutData);

    	
    	Composite buttonComposite = new Composite( composite, SWT.NONE);

		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout( buttonLayout);
		buttonComposite.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, false, 1,1));
		
		
		// add button
		addRepoButton = new Button( buttonComposite, SWT.NONE);
		addRepoButton.setImage(addImage);
		addRepoButton.setToolTipText( "add a new bias");
		addRepoButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		
		addRepoButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				RepositoryExpression repositoryExpression = new RepositoryExpression(true, "");
				List<RepositoryExpression> expressions = contentProvider.getBiases();
				expressions.add( repositoryExpression);
				contentProvider.setBiases( expressions);
				repoTableViewer.setInput( expressions);
				repoTableViewer.refresh();
			}
			
		});
		
	
		
		// remove button 
		removeRepoButton = new Button( buttonComposite, SWT.NONE);
		removeRepoButton.setToolTipText( "remove the selected bias");
		removeRepoButton.setImage(removeImage);
		removeRepoButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		removeRepoButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = repoTable.getSelectionIndex();
				
				if (index >= 0) {
					RepositoryExpression expression = ((List<RepositoryExpression>) repoTableViewer.getInput()).get( index);
					List<RepositoryExpression> expressions = contentProvider.getBiases();
					expressions.remove( expression);
					contentProvider.setBiases( expressions);
					repoTableViewer.setInput( expressions);
					repoTableViewer.refresh();
				}
			}
			
		});
		
		removeRepoButton.setEnabled( false);
		
		Composite messageComposite = new Composite(composite, SWT.NONE);
        messageComposite.setLayout( layout);
        messageComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        messageLabel = new Label( messageComposite, SWT.NONE);
        messageLabel.setText( "      ");
        messageLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        
        message = new Text( messageComposite, SWT.NONE);
        message.setEditable( false);
        message.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));		
        message.setFont(bigFont);
        message.setBackground( composite.getBackground());
		   
		
		parentShell.layout(true);
		return composite;
	}

	@Override
	protected Point getInitialSize() {		
		return new Point( 600, 350);
	}

	
	
	@Override
	protected void okPressed() {
		// validate 

		// name 
		String groupId = groupIdEditor.getSelection();
		if (groupId == null || groupId.length() == 0) {
			MessageDialog.openError(getShell(),REPOSITORY_WIZARD, "a name for the repository is required");
			return;
		}
		
		String artifactId = artifactIdEditor.getSelection();
		
		// write back to SoureRepositoryPairing
		if (selection == null) {
			selection = new ArtifactBias();			
		}
		selection.getIdentification().setGroupId( groupId);
		if (artifactId.length() == 0) {
			selection.getIdentification().setArtifactId(".*");
		}
		else {
			selection.getIdentification().setArtifactId( artifactId);
		}
		
		// 
		selection.getActiveRepositories().clear();
		selection.getInactiveRepositories().clear();
		List<RepositoryExpression> expressions = (List<RepositoryExpression>) repoTableViewer.getInput();
		for (RepositoryExpression expression : expressions) {
			if (!expression.getIsActive()) {
				selection.getInactiveRepositories().add(expression.getId());
			}
			else {
				selection.getActiveRepositories().add(expression.getId());
			}
		}
		
		
		super.okPressed();
	}


	@Override
	public boolean close() {
		bigFont.dispose();
		warningImage.dispose();
		//
		//
		//
		addImage.dispose();
		removeImage.dispose();
		
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( REPOSITORY_WIZARD);
		super.configureShell(newShell);
	}


	@Override
	public void modifyText(ModifyEvent event) {
		if (event.widget == groupIdEditor.getWidget()) { 
			// validate
			boolean valid = true;
			if (identifications == null)
				return;
			for (ArtifactBias pairing : identifications) {
				if (pairing == selection) {
					continue;
				}
				if (pairing.getIdentification().getGroupId().equalsIgnoreCase( groupIdEditor.getSelection())) {
					valid = false;
					break;
				}
			}
			if (!valid) {
				message.setText("A publishing candidate bias with this name already exists.");		
				messageLabel.setImage(warningImage);
				
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
			else {
				message.setText("");
				messageLabel.setImage(null);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				
			}
			
		}
		
	}
	
	

}
