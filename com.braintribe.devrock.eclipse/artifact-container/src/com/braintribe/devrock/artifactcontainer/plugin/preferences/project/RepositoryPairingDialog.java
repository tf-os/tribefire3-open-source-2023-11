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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.braintribe.build.process.repository.process.SourceRepositoryAccess;
import com.braintribe.build.process.repository.process.SourceRepositoryAccessException;
import com.braintribe.build.process.repository.process.git.GitRepositoryAccess;
import com.braintribe.build.process.repository.process.svn.SvnRepositoryAccess;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryKind;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.plugin.commons.preferences.DirectoryEditor;
import com.braintribe.plugin.commons.preferences.StringEditor;

public class RepositoryPairingDialog extends Dialog implements ModifyListener {

	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static final String REPOSITORY_WIZARD = "Repository pairing wizard";
	private Shell parentShell;
	private Font bigFont;
	
	private Image svnImage;
	private Image gitImage;
	private Image searchImage;
	private Image warningImage;
	
	private SourceRepositoryPairing selection;
	private List<SourceRepositoryPairing> pairings;
	private Text message;
	private Label messageLabel;
	
	private StringEditor nameEditor;
	private DirectoryEditor currentWorkingCopyDirectoryEditor;
	private StringEditor urlEditor;
	private Button svnButton;
	private Button gitButton;

	
	private static final String PROTOCOL_FILE = "file:";
	

	public SourceRepositoryPairing getSelection() {
		return selection;
	}

	
	@Configurable
	public void setSelection(SourceRepositoryPairing selection) {
		this.selection = selection;
	}

	@Configurable @Required
	public void setPairings(List<SourceRepositoryPairing> pairings) {
		this.pairings = pairings;
	}


	public RepositoryPairingDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_svn.gif");
		svnImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_git.gif");
		gitImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "search.gif");
		searchImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "warning.png");
		warningImage = imageDescriptor.createImage();

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
        // kind 
        //
        
        Composite kindComposite = new Composite(composite, SWT.NONE);
        kindComposite.setLayout( layout);
        kindComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        Label kindLabel = new Label( kindComposite, SWT.NONE);
        kindLabel.setText("Source repository kind");
        kindLabel.setFont( bigFont);
        		
        
        svnButton = new Button( kindComposite, SWT.RADIO);
        svnButton.setImage(svnImage);
        svnButton.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, false, false, 4, 1) );
        svnButton.setText("SVN driven repository");
        
        gitButton = new Button( kindComposite, SWT.RADIO);
        gitButton.setImage(gitImage);
        gitButton.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, false, false, 4, 1) );
        gitButton.setText("GIT driven repository");
        
        Composite repositoryComposite = new Composite(composite, SWT.NONE);
        repositoryComposite.setLayout( layout);
        repositoryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        
        Label repositoryLabel = new Label( repositoryComposite, SWT.NONE);
        repositoryLabel.setText("Repository");
        repositoryLabel.setFont( bigFont);
        
        nameEditor = new StringEditor();
    	Composite nameComposite = nameEditor.createControl( composite, "Name:");
		nameComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));	
		if (selection != null) {
			nameEditor.setSelection( selection.getName());
		}
    	nameEditor.addModifyListener( this);
		
    	currentWorkingCopyDirectoryEditor = new DirectoryEditor(getShell());
		currentWorkingCopyDirectoryEditor.setSelection( "");		
		Composite wcdComposite = currentWorkingCopyDirectoryEditor.createControl( composite, "Local path:");		
		wcdComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		if (selection != null) {
			currentWorkingCopyDirectoryEditor.setSelection( selection.getLocalRepresentation().getRepoUrl().substring( PROTOCOL_FILE.length()));
		}
	    
		Composite remoteComposite = new Composite(composite, SWT.NONE);
        remoteComposite.setLayout( layout);
        remoteComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		
		urlEditor = new StringEditor();
    	Composite urlComposite = urlEditor.createControl( remoteComposite, "Remote URL: ");
		urlComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		if (selection != null) {
			urlEditor.setSelection( selection.getRemoteRepresentation().getRepoUrl());
		}
		
		Button detectButton = new Button( remoteComposite, SWT.NONE);
		detectButton.setImage( searchImage);
		detectButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		detectButton.setToolTipText("Auto detect from local woring copy path");
		
		detectButton.addSelectionListener( new SelectionAdapter() {						

			@Override
			public void widgetSelected(SelectionEvent e) {
				String workingCopy = currentWorkingCopyDirectoryEditor.getSelection();
				SourceRepositoryAccess repositoryAccess = null;
				
				if (svnButton.getSelection()) {
					repositoryAccess = new SvnRepositoryAccess();									
				}
				else {
					repositoryAccess = new GitRepositoryAccess();					
					//MessageDialog.openInformation(getShell(), "Devrock's Artifact Container", "Currently, no support for GIT. Please enter the URL manually if at all");
				}			
				try {					
					String url = repositoryAccess.getBackingUrlOfWorkingCopy(workingCopy);
					urlEditor.setSelection(url); 
				} catch (SourceRepositoryAccessException e1) {
					MessageDialog.openError(getShell(), REPOSITORY_WIZARD, "cannot retrieve information about the svn repository backing [" + workingCopy +"]\n\n " + e1.getMessage());
				}				
			}
			
		});
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
		
		if (selection != null && selection.getRemoteRepresentationKind() == SourceRepositoryKind.svn) {
        	svnButton.setSelection(true);
        }
		else if (selection != null && selection.getRemoteRepresentationKind() == SourceRepositoryKind.git) {
	        gitButton.setSelection(true);
	    }
		else {
			gitButton.setSelection(true);
		}
	       
		
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
		String repositoryName = nameEditor.getSelection();
		if (repositoryName == null || repositoryName.length() == 0) {
			MessageDialog.openError(getShell(),REPOSITORY_WIZARD, "a name for the repository is required");
			return;
		}
		// working copy 
		String workingCopyLocation = currentWorkingCopyDirectoryEditor.getSelection();
		if (workingCopyLocation == null || workingCopyLocation.length() == 0 || !new File(workingCopyLocation).exists()) {
			MessageDialog.openError(getShell(),REPOSITORY_WIZARD, "the working copy directory must exist");
			return;
		}
		QuickImportPreferences quickImportPreferences = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences();
		if (!quickImportPreferences.getLocalOnlyNature()) {
			// url
			try {
				new URL ( urlEditor.getSelection());
			} catch (MalformedURLException e) {
				// invalid entry
				MessageDialog.openError(getShell(),REPOSITORY_WIZARD, "invalid data entered for remote url of the repository");
				return;
			}
		}
		
		// write back to SoureRepositoryPairing
		if (selection == null) {
			selection = SourceRepositoryPairing.T.create();
		}
		selection.setName( repositoryName);
		
		SourceRepository remoteRepository = selection.getRemoteRepresentation();
		if (remoteRepository == null) {
			remoteRepository = SourceRepository.T.create();
			selection.setRemoteRepresentation(remoteRepository);
		}
		remoteRepository.setName( repositoryName);
		
		remoteRepository.setRepoUrl( urlEditor.getSelection());		
		
		SourceRepository localRepository = selection.getLocalRepresentation();
		if (localRepository == null) {
			localRepository = SourceRepository.T.create();
			selection.setLocalRepresentation(localRepository);
		}
		localRepository.setName(repositoryName);
		localRepository.setRepoUrl( PROTOCOL_FILE + workingCopyLocation);
		
		if (svnButton.getSelection()) {
			selection.setRemoteRepresentationKind( SourceRepositoryKind.svn);
		}
		else {
			selection.setRemoteRepresentationKind( SourceRepositoryKind.git);
		}
		
		super.okPressed();
	}


	@Override
	public boolean close() {
		bigFont.dispose();
		svnImage.dispose();
		gitImage.dispose();
		warningImage.dispose();
		searchImage.dispose();
		//
		//
		//
		
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( REPOSITORY_WIZARD);
		super.configureShell(newShell);
	}


	@Override
	public void modifyText(ModifyEvent event) {
		if (event.widget == nameEditor.getWidget()) { 
			// validate
			boolean valid = true;
			if (pairings == null)
				return;
			for (SourceRepositoryPairing pairing : pairings) {
				if (pairing == selection) {
					continue;
				}
				if (pairing.getName().equalsIgnoreCase( nameEditor.getSelection())) {
					valid = false;
					break;
				}
			}
			if (!valid) {
				message.setText("A pairing with this name exists. Choose another one");		
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
