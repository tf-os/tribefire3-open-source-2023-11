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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.devrock.artifactcontainer.quickImport.ui.QuickImportDialog;
import com.braintribe.devrock.artifactcontainer.ui.ant.MessageMonitorBridge;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportAction;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;

/**
 * a dialog for the artifact cloner
 * @author pit
 *
 */
public class ArtifactWizardDialog extends Dialog implements SelectionListener {


	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static final String ARTIFACT_PROJECT_WIZARD = "Artifact project wizard";
	private static final String paddingString = "                       ";
	private static final int padding = 12;
	private Shell parentShell;
	private Font bigFont;
	private Button targetBrowseButton;

	private Button cloneArtifact;
	private Button createArtifact;
	private Composite createComposite;
	private Button templateBrowseButton;	
	private Button loadProjectAfterRun;
	
	private Artifact selectedTargetArtifact;
	private Artifact selectedSourceArtifact;
	
	private StringEditor targetGroupIdEditor;
	private StringEditor targetArtifactIdEditor;
	private StringEditor targetVersionEditor;
	
	private StringEditor sourceGroupIdEditor;
	private StringEditor sourceArtifactIdEditor;
	private StringEditor sourceVersionEditor;	
	private StringEditor cloneVersionEditor;
	
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();

	private enum Action { clone, copyFrom}
	
	private Action actionOnOk = Action.copyFrom;
	
	@Configurable
	public void setSelectedSourceArtifact(Artifact selectedSourceArtifact) {
		this.selectedSourceArtifact = selectedSourceArtifact;
	}
	
	@Configurable
	public void setSelectedTargetArtifact(Artifact selectedTargetArtifact) {
		this.selectedTargetArtifact = selectedTargetArtifact;
	}

	public ArtifactWizardDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
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
        // artifact 
        //
        Composite artifactComposite = new Composite( composite, SWT.NONE);
        artifactComposite.setLayout( layout);
        artifactComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
                        
        Label label = new Label( artifactComposite, SWT.NONE);
    	label.setText( "Target artifact");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
    	label.setFont(bigFont);
    	    	
    	targetBrowseButton = new Button( artifactComposite, SWT.NONE);
    	targetBrowseButton.setText( "..");
    	targetBrowseButton.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, false, 1, 1));
    	targetBrowseButton.addSelectionListener( this);
        
    	targetGroupIdEditor = new StringEditor();
    	Composite targetGroupIdComposite = targetGroupIdEditor.createControl( artifactComposite, pad( "Group Id:"));
		targetGroupIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));			
		if (selectedTargetArtifact != null) {
			targetGroupIdEditor.setSelection( selectedTargetArtifact.getGroupId());
		}
    	
		targetArtifactIdEditor = new StringEditor();
    	Composite targetArtifactIdComposite = targetArtifactIdEditor.createControl( artifactComposite, pad( "Artifact Id:"));
		targetArtifactIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		if (selectedTargetArtifact != null) {
			targetArtifactIdEditor.setSelection( selectedTargetArtifact.getArtifactId());
		}
		    	
		targetVersionEditor = new StringEditor();
    	Composite targetVersionComposite = targetVersionEditor.createControl( artifactComposite, pad( "Version:"));
		targetVersionComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		if (selectedTargetArtifact != null) {		
			targetVersionEditor.setSelection( VersionProcessor.toString( selectedTargetArtifact.getVersion()));		
		}
		
		   
    	Composite actionComposite = new Composite( composite, SWT.NONE);
		actionComposite.setLayout( layout);
		actionComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
		 
		label = new Label( actionComposite, SWT.NONE);
		label.setText( "Actions:");         
		label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		label.setFont(bigFont);
		
		
		Composite cloneComposite = new Composite( actionComposite, SWT.NONE);		
		cloneComposite.setLayout( layout);
		cloneComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
				
		cloneArtifact = new Button( cloneComposite, SWT.RADIO);
		cloneArtifact.setText( "Clone artifact from existing artifact");
		cloneArtifact.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		cloneArtifact.addSelectionListener( this);
		
		cloneVersionEditor = new StringEditor();
    	Composite cloneVersionComposite = cloneVersionEditor.createControl( actionComposite, pad( "Version:"));
		cloneVersionComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
		createComposite = new Composite( actionComposite, SWT.NONE);
		createComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		createComposite.setLayout( layout);
		
		createArtifact = new Button( createComposite, SWT.RADIO);
		createArtifact.setText( "Create new artifact using an artifact as template for the relevant files");
		createArtifact.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		createArtifact.addSelectionListener( this);
		
		label = new Label( createComposite, SWT.NONE);
		label.setText( "Template artifact:");         
		label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		label.setFont(bigFont);
		
		
		 
		templateBrowseButton = new Button( createComposite, SWT.NONE);
		templateBrowseButton.setText( "..");
		templateBrowseButton.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		templateBrowseButton.addSelectionListener( this);
              	
		
		sourceGroupIdEditor = new StringEditor();
    	Composite sourceGroupIdComposite = sourceGroupIdEditor.createControl( createComposite, pad( "Group Id:"));
		sourceGroupIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));			
		if (selectedSourceArtifact != null) {
			sourceGroupIdEditor.setSelection( selectedSourceArtifact.getGroupId());
		}
    	
		sourceArtifactIdEditor = new StringEditor();
    	Composite sourceArtifactIdComposite = sourceArtifactIdEditor.createControl( createComposite, pad( "Artifact Id:"));
    	sourceArtifactIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
    	if (selectedSourceArtifact != null) {
    		sourceArtifactIdEditor.setSelection( selectedSourceArtifact.getArtifactId());
    	}
		    	
		sourceVersionEditor = new StringEditor();
    	Composite sourceVersionComposite = sourceVersionEditor.createControl( createComposite, pad( "Version:"));
    	sourceVersionComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
    	if (selectedSourceArtifact != null) {    	
			sourceVersionEditor.setSelection( VersionProcessor.toString( selectedSourceArtifact.getVersion()));		
    	}
		
    	 				 
		Composite processComposite = new Composite( composite, SWT.NONE);
		processComposite.setLayout( layout);
		processComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
		 
		label = new Label( processComposite, SWT.NONE);
		label.setText( "Additional process settings:");         
		label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		label.setFont(bigFont);
		 
		loadProjectAfterRun = new Button( processComposite, SWT.CHECK);
		loadProjectAfterRun.setText( "Load artifact after run");
		loadProjectAfterRun.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
		// default is on
		loadProjectAfterRun.setSelection(true);
		
		// initialize
		createArtifact.setSelection(true);
		cloneArtifact.setSelection( false);
		cloneVersionEditor.setEnabled(false);
			
		parentShell.layout(true);
		return composite;
	}

	@Override
	protected Point getInitialSize() {
		
		return new Point( 600, 600);
	}

	@Override
	public boolean close() {
		bigFont.dispose();
		
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( ARTIFACT_PROJECT_WIZARD);
		super.configureShell(newShell);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == targetBrowseButton) {
			selectedTargetArtifact = getBrowseResult();
			if (selectedTargetArtifact != null) {
				targetGroupIdEditor.setSelection( selectedTargetArtifact.getGroupId());
				targetArtifactIdEditor.setSelection( selectedTargetArtifact.getArtifactId());				
				targetVersionEditor.setSelection( VersionProcessor.toString( selectedTargetArtifact.getVersion()));				
			}
			return;
		}
		
		if (event.widget == templateBrowseButton) {
			selectedSourceArtifact = getBrowseResult();
			if (selectedSourceArtifact != null) {
				sourceGroupIdEditor.setSelection( selectedSourceArtifact.getGroupId());
				sourceArtifactIdEditor.setSelection( selectedSourceArtifact.getArtifactId());
				sourceVersionEditor.setSelection( VersionProcessor.toString( selectedSourceArtifact.getVersion()));
			}
			return;
		}
			
		
		if (event.widget == cloneArtifact) {			
			actionOnOk = Action.clone;
			createArtifact.setSelection( false);
			simulateRadioGroup();
			return;
		}			
		
		if (event.widget == createArtifact) {			
			actionOnOk = Action.copyFrom;
			cloneArtifact.setSelection( false);
			simulateRadioGroup();			
			return;
		}						
	}
	
	
	private void simulateRadioGroup() {	
		cloneVersionEditor.setEnabled( cloneArtifact.getSelection());
		templateBrowseButton.setEnabled(createArtifact.getSelection());
		sourceGroupIdEditor.setEnabled(createArtifact.getSelection());
		sourceArtifactIdEditor.setEnabled(createArtifact.getSelection());
		sourceVersionEditor.setEnabled(createArtifact.getSelection());		
	}

	
	private Artifact getBrowseResult() {
		QuickImportDialog quickImportDialog = new QuickImportDialog( parentShell);
		quickImportDialog.setImportAction( QuickImportAction.selectOnly);
		if (selectedTargetArtifact != null) {
			quickImportDialog.setSelection(selectedTargetArtifact);
		}		
		quickImportDialog.open();
		return quickImportDialog.getSelection();
	}
	
	private String pad( String tag) {
		int l = tag.length();
		if (l < padding) {
			return tag + paddingString.substring(0, padding - l);
		}
		return tag;
	}
	
	/**
	 * prepares the target and source artifacts for the clone process and tests if all parameters are there 
	 * @return - true if cloning can proceed 
	 */
	private boolean canClone() {
		
		String targetGroupId = targetGroupIdEditor.getSelection();
		String targetArtifactId = targetArtifactIdEditor.getSelection();
		String targetVersion = targetVersionEditor.getSelection();
		String cloneVersion = cloneVersionEditor.getSelection();
		if (
				targetGroupId == null || targetGroupId.length() == 0 ||
				targetArtifactId == null || targetArtifactId.length() == 0 ||
				targetVersion == null || targetVersion.length() == 0 || 
				cloneVersion == null || cloneVersion.length() == 0
				) {
			MessageDialog.openWarning(getShell(), ARTIFACT_PROJECT_WIZARD, "insufficient parameters: target artifact and clone version must be specified");
			return false;
		}
		try {
			if (selectedSourceArtifact == null) {
				selectedSourceArtifact = Artifact.T.create();
			}
			selectedSourceArtifact.setGroupId( targetGroupId);
			selectedSourceArtifact.setArtifactId( targetArtifactId);
			selectedSourceArtifact.setVersion( VersionProcessor.createFromString( targetVersion));
		} catch (VersionProcessingException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("cannot clone", e);
			ArtifactContainerPlugin.getInstance().log(status);	
			MessageDialog.openError(getShell(), ARTIFACT_PROJECT_WIZARD, e.getLocalizedMessage());
			return false;
		}
		
		try {
			if (selectedTargetArtifact == null) {
				selectedTargetArtifact = Artifact.T.create();
			}
			selectedTargetArtifact.setGroupId( targetGroupId);
			selectedTargetArtifact.setArtifactId( targetArtifactId);
			selectedTargetArtifact.setVersion( VersionProcessor.createFromString( cloneVersion));
		} catch (VersionProcessingException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("cannot clone", e);
			ArtifactContainerPlugin.getInstance().log(status);		
			MessageDialog.openError(getShell(), ARTIFACT_PROJECT_WIZARD, e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * prepares the target and source artifacts for the copy process and tests if all parameters are there 
	 * @return - true if copy can proceed 
	 */
	private boolean canCopy() {
		
		String targetGroupId = targetGroupIdEditor.getSelection();
		String targetArtifactId = targetArtifactIdEditor.getSelection();
		String targetVersion = targetVersionEditor.getSelection();	
		
		String sourceGroupId = sourceGroupIdEditor.getSelection();
		String sourceArtifactId = sourceArtifactIdEditor.getSelection();
		String sourceVersion = sourceVersionEditor.getSelection();	
		if (
				targetGroupId == null || targetGroupId.length() == 0 ||
				targetArtifactId == null || targetArtifactId.length() == 0 ||
				targetVersion == null || targetVersion.length() == 0 ||
				sourceGroupId == null || sourceGroupId.length() == 0 ||
				sourceArtifactId == null || sourceArtifactId.length() == 0 ||
				sourceVersion == null || sourceVersion.length() == 0
				) {
			MessageDialog.openWarning(getShell(), "Cannot copy", "insufficient parameters: target and source artifact must be specified");
			return false;
		}
		
		try {
			if (selectedTargetArtifact == null) {
				selectedTargetArtifact = Artifact.T.create();
			}
			selectedTargetArtifact.setGroupId( targetGroupId);
			selectedTargetArtifact.setArtifactId( targetArtifactId);
			selectedTargetArtifact.setVersion( VersionProcessor.createFromString( targetVersion));
		} catch (VersionProcessingException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("cannot copy", e);
			ArtifactContainerPlugin.getInstance().log(status);	
			MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage());
			return false;
		}
		
		try {
			if (selectedSourceArtifact == null) {
				selectedSourceArtifact = Artifact.T.create();
			}
			selectedSourceArtifact.setGroupId( sourceGroupId);
			selectedSourceArtifact.setArtifactId( sourceArtifactId);
			selectedSourceArtifact.setVersion( VersionProcessor.createFromString( sourceVersion));
		} catch (VersionProcessingException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("cannot copy", e);
			ArtifactContainerPlugin.getInstance().log(status);	
			MessageDialog.openError(getShell(), "Error", e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	private File handleClone(File workingCopy, final Display display, ProcessNotificationListener listener) {						
		ArtifactCloner cloner = new ArtifactCloner();
		cloner.setListener(listener);
		cloner.setWorkingCopy( workingCopy);
	
		try {
			return cloner.clone(selectedSourceArtifact, selectedTargetArtifact);
		} catch (final ArtifactClonerException e) {
			display.asyncExec( new Runnable() {			
				@Override
				public void run() {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot clone", e);
					ArtifactContainerPlugin.getInstance().log(status);					
					MessageDialog.openError(display.getActiveShell(), ARTIFACT_PROJECT_WIZARD, e.getLocalizedMessage());					
				}
			});
		}
		return null;
	}
	
	private File extractWorkingCopy() {

		List<SourceRepositoryPairing> pairings = svnPreferences.getSourceRepositoryPairings();
		if (pairings == null || pairings.size() == 0) {
			String msg = "cannot clone as no source repository pairings were found";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);					
			MessageDialog.openError(parentShell, ARTIFACT_PROJECT_WIZARD, msg);
			return null;
		}
		
		SourceRepository sourceRepository = null;
		if (pairings.size() == 1) {
			SourceRepositoryPairing pairing = pairings.get(0);
			sourceRepository = pairing.getLocalRepresentation();
		}
		else {
			RepositoryPairingSelectionDialog selectionDialog = new RepositoryPairingSelectionDialog(parentShell);
			selectionDialog.setPairings(pairings);
			selectionDialog.open();
			SourceRepositoryPairing pairing = selectionDialog.getSelection();
			if (pairing != null) {
				sourceRepository = pairing.getLocalRepresentation();
			}
		}
		if (sourceRepository == null)
			return null;
		String repurl = sourceRepository.getRepoUrl();
		try {
			URL url = new URL(repurl);
			File workingCopy = new File(url.getFile());
			return workingCopy;
		} catch (MalformedURLException e) {
			String msg = "cannot clone as source repository pairing [" + sourceRepository.getName() + "]'s local URL is invalid";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);					
			MessageDialog.openError(parentShell, ARTIFACT_PROJECT_WIZARD, msg);
			return null;
		}					
	}

	private File handleCopy(File workingCopy, final Display display, ProcessNotificationListener listener){
		ArtifactCloner cloner = new ArtifactCloner();
		cloner.setListener(listener);				
		cloner.setWorkingCopy( workingCopy);
		try {
			return cloner.copy(selectedSourceArtifact, selectedTargetArtifact);
		} catch (final ArtifactClonerException e) {
			display.asyncExec( new Runnable() {			
				@Override
				public void run() {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot copy", e);
					ArtifactContainerPlugin.getInstance().log(status);				
					MessageDialog.openError(display.getActiveShell(), ARTIFACT_PROJECT_WIZARD, e.getLocalizedMessage());					
				}
			});
		}
		return null;
	}
	
	
		

	@Override
	protected void okPressed() {
		switch (actionOnOk) {
		case clone:
			if (!canClone()) {
				return ;
			}
			break;
		case copyFrom:
			if (!canCopy()) {
				return;							
			}
			break;
		}		
		final File workingCopy = extractWorkingCopy();		
		if (workingCopy == null) {
			return;
		}
		
		final boolean loadProject = loadProjectAfterRun.getSelection(); 
		// run it 	
		Job job = new Job(ARTIFACT_PROJECT_WIZARD) {						
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				MessageMonitorBridge bridge = new MessageMonitorBridge( ARTIFACT_PROJECT_WIZARD, arg0);
				File file = null;
				switch (actionOnOk) {
					case clone:
						file = handleClone( workingCopy, parentShell.getDisplay(), bridge);
						break;
					case copyFrom:
						file = handleCopy( workingCopy, parentShell.getDisplay(), bridge);
						break;
					}
				if (loadProject && file != null) {		
					ProjectImporterTuple importerTuple = new ProjectImporterTuple( file.getAbsolutePath(), selectedTargetArtifact);
					ProjectImporter.importProjects( false, new TargetProviderImpl(), ArtifactContainerPlugin.getWorkspaceProjectRegistry(), importerTuple);
				}

				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		super.okPressed();
	}

	
}
