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
package com.braintribe.devrock.zed.ui.comparison;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.commands.zed.ZedRunnerTrait;
import com.braintribe.devrock.commands.zed.ZedTargetDialog;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zed.api.comparison.ComparisonIssueClassification;
import com.braintribe.devrock.zed.api.comparison.SemanticVersioningLevel;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.storage.ViewingContextStorageContainer;

/**
 * selector that allows to select (and extract) two artifacts for comparison
 * @author pit
 */
public class ZedComparisonTargetSelector  extends DevrockDialog implements IDisposable, SelectionListener, ZedRunnerTrait {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private final UiSupport uiSupport = DevrockPlugin.instance().uiSupport();
	
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private Image compareProjectImage;
	private Image compareJarImage;
	private Image compareFileImage;
	
	private Text baseArtifactName;
	private Button baseProjectSelect;
	private Button baseArtifactSelect;
	private Button baseFileSelect;

	private Text otherArtifactName;
	private Button otherProjectSelect;
	private Button otherArtifactSelect;
	private Button otherFileSelect;
	
	private Artifact baseArtifact;
	private Artifact otherArtifact;
	private Shell parentShell;
	private String lastSelectedFile;
	
	private Artifact currentlyProcessedArtifact;
	private CompiledTerminal currentCompiledTerminal;
	
	private boolean extractionReturned;
	private Shell shell;
	private Label statusLabel;
	
	private Button semanticCompareLevelNone;
	private Button semanticCompareLevelMajor;
	private Button semanticCompareLevelMinor;
	private Button semanticCompareLevelRevision;
	
	private SemanticVersioningLevel chosenSemanticCompareLevel = SemanticVersioningLevel.none;

	public ZedComparisonTargetSelector(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle( SHELL_STYLE);
		
		compareProjectImage = uiSupport.images().addImage("zc_project", ZedComparisonTargetSelector.class, "open_artifact.png");
		compareJarImage = uiSupport.images().addImage("zc_jar", ZedComparisonTargetSelector.class, "jar_obj.png");
		compareFileImage = uiSupport.images().addImage("zc_file", ZedComparisonTargetSelector.class, "open_file.png");
	}

	@Override
	protected Point getDrInitialSize() {	
		return new Point( 600, 300);
	}	

	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		this.shell = newShell;
		newShell.setText("zed's comparison target selection");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite composite = (Composite) super.createDialogArea(parent);							
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        //
        // two selections 
        // 
        
        // base
        Composite baseComposite = new Composite(composite, SWT.BORDER);
        baseComposite.setLayout(layout);
        baseComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
                        
        Label baseLabel = new Label( baseComposite, SWT.NONE);
        baseLabel.setText( "Base artifact");
        baseLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        
        baseArtifactName = new Text(baseComposite, SWT.NONE);
        baseArtifactName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        baseProjectSelect = new Button( baseComposite, SWT.NONE);
        baseProjectSelect.setImage(compareProjectImage);
        baseProjectSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        baseProjectSelect.addSelectionListener( this);
        baseProjectSelect.setToolTipText("use currently selected project as comparison basis");
        
        baseArtifactSelect = new Button( baseComposite, SWT.NONE);
        baseArtifactSelect.setImage(compareJarImage);
        baseArtifactSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        baseArtifactSelect.addSelectionListener( this);
        baseArtifactSelect.setToolTipText("select an artifact as comparison basis");
        
        baseFileSelect = new Button( baseComposite, SWT.NONE);
        baseFileSelect.setImage(compareFileImage);
        baseFileSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        baseFileSelect.addSelectionListener( this);
        baseFileSelect.setToolTipText("select a stored extraction as comparison basis");
        
        // other
        Composite otherComposite = new Composite(composite, SWT.BORDER);
        otherComposite.setLayout(layout);
        otherComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false,4,1));
        
        Label otherLabel = new Label( otherComposite, SWT.NONE);
        otherLabel.setText( "Other artifact");
        otherLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        
        otherArtifactName = new Text(otherComposite, SWT.NONE);
        otherArtifactName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        otherProjectSelect = new Button( otherComposite, SWT.NONE);
        otherProjectSelect.setImage(compareProjectImage);
        otherProjectSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        otherProjectSelect.addSelectionListener(this);
        otherProjectSelect.setToolTipText("use currently selected project as comparison target");
        
        otherArtifactSelect = new Button( otherComposite, SWT.NONE);
        otherArtifactSelect.setImage(compareJarImage);
        otherArtifactSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        otherArtifactSelect.addSelectionListener(this);
        otherArtifactSelect.setToolTipText("select an artifact as comparison target");
        
        otherFileSelect = new Button( otherComposite, SWT.NONE);
        otherFileSelect.setImage(compareFileImage);
        otherFileSelect.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        otherFileSelect.addSelectionListener( this);
        otherFileSelect.setToolTipText("select a stored extraction as comparison target");
        
        // status
        Composite statusComposite = new Composite(composite, SWT.NONE);
        statusComposite.setLayout(layout);
        statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        statusLabel = new Label(statusComposite, SWT.NONE);
        statusLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        
        Composite semanticLevelComposite = new Composite(composite, SWT.NONE);
        semanticLevelComposite.setLayout(layout);
        semanticLevelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        Label semLevLabel = new Label(semanticLevelComposite, SWT.NONE);
        semLevLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        semLevLabel.setText( "Semantic versioning rules for comparison");
        semLevLabel.setToolTipText( "Issues detected are rated following semantic versioning rules based on the artifact's versions");
        
        semanticCompareLevelNone = new Button(semanticLevelComposite, SWT.RADIO);
        semanticCompareLevelNone.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        semanticCompareLevelNone.setText("none");
        semanticCompareLevelNone.setToolTipText("no restrictions as ever");
        semanticCompareLevelNone.setSelection(true);
        semanticCompareLevelNone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenSemanticCompareLevel = SemanticVersioningLevel.none;				
			}        	
		});
        
        semanticCompareLevelMajor = new Button(semanticLevelComposite, SWT.RADIO);
        semanticCompareLevelMajor.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        semanticCompareLevelMajor.setText("major");
        semanticCompareLevelMajor.setToolTipText("major version comparison : no contract-changes restrictions as ever");
        semanticCompareLevelMajor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenSemanticCompareLevel = SemanticVersioningLevel.major;				
			}        	
		});
        
        semanticCompareLevelMinor = new Button(semanticLevelComposite, SWT.RADIO);
        semanticCompareLevelMinor.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        semanticCompareLevelMinor.setText("minor");
        semanticCompareLevelMinor.setToolTipText("minor version comparison : only contract additions allowed");
        semanticCompareLevelMinor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenSemanticCompareLevel = SemanticVersioningLevel.minor;				
			}        	
		});
        
        semanticCompareLevelRevision = new Button(semanticLevelComposite, SWT.RADIO);
        semanticCompareLevelRevision.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        semanticCompareLevelRevision.setText("revision");
        semanticCompareLevelRevision.setToolTipText("revision version comparison : no contract chanted allowed");
        semanticCompareLevelRevision.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenSemanticCompareLevel = SemanticVersioningLevel.revision;				
			}        	
		});
        
        semanticCompareLevelMajor.setEnabled(false);
		semanticCompareLevelMinor.setEnabled(false);
		semanticCompareLevelRevision.setEnabled(false);
		semanticCompareLevelNone.setEnabled(false);
		chosenSemanticCompareLevel = SemanticVersioningLevel.none;
        
                
        // button control
   	 	activateOk();
   	 	activateProjectButtons();
                
        return composite;
	}
	
	private void activateProjectButtons() {
		IProject currentProject = SelectionExtracter.currentProject();
		if (currentProject == null) {
			baseProjectSelect.setEnabled(false);
			otherProjectSelect.setEnabled(false);
		}
	}

	
	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {	
		super.createButtonsForButtonBar(parent);
		activateOk();
	}

	private void activateOk() {
		Button ok = getButton(IDialogConstants.OK_ID);
		
		if (ok == null)
			return;
		
		boolean valid = baseArtifact != null && otherArtifact != null;
		
		if (valid) {
			
			semanticCompareLevelMajor.setEnabled(true);
			semanticCompareLevelMinor.setEnabled(true);
			semanticCompareLevelRevision.setEnabled(true);
			semanticCompareLevelNone.setEnabled(true);
			
			
			Version baseVersion = Version.parse( baseArtifact.getVersion());
			Version otherVersion = Version.parse( otherArtifact.getVersion());			
			SemanticVersioningLevel semanticVersioningLevel = ComparisonIssueClassification.determineLevelFromVersions(baseVersion, otherVersion);
			
			switch (semanticVersioningLevel) {
			case major:
				semanticCompareLevelMajor.setSelection(true);
				chosenSemanticCompareLevel = SemanticVersioningLevel.major;
				break;
			case minor:
				semanticCompareLevelMinor.setSelection(true);
				chosenSemanticCompareLevel = SemanticVersioningLevel.minor;
				break;
			case revision:
				semanticCompareLevelRevision.setSelection(true);
				chosenSemanticCompareLevel = SemanticVersioningLevel.revision;
				break;
			default:
			case none:
				semanticCompareLevelNone.setSelection(true);
				chosenSemanticCompareLevel = SemanticVersioningLevel.none;
				break;				
			}					
			ok.setEnabled(true);
		}
		else {
			semanticCompareLevelMajor.setSelection(false);
			semanticCompareLevelMinor.setSelection(false);
			semanticCompareLevelRevision.setSelection(false);
			semanticCompareLevelNone.setSelection(false);
			ok.setEnabled(false);
			
		}
	}

    @Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}
    
    private Maybe<IProject> selectProject() {
    	List<IProject> selectedProjects = new ArrayList<>( SelectionExtracter.selectedProjects(SelectionExtracter.currentSelection()));
    	if (selectedProjects == null || selectedProjects.size() == 0) {
    		return Maybe.empty( Reasons.build( NotFound.T).text( "no selection").toReason());
    	}
    	
    	if (selectedProjects.size() == 1) {
    		return Maybe.complete(selectedProjects.get(0));
    	}
    	
    	ProjectSelectionChooser chooser = new ProjectSelectionChooser(parentShell);
    	chooser.setAvailableProjects( selectedProjects);
    	
    	int retval = chooser.open();
    	
    	if (retval == org.eclipse.jface.dialogs.Dialog.CANCEL) {
    		return Maybe.empty( Reasons.build( NotFound.T).text( "cancelled selection").toReason());
		}
    	
    	return Maybe.complete( chooser.getSelectedProject());    	    	
    }
    
	@Override
	public void widgetSelected(SelectionEvent arg0) {
		Widget widget = arg0.widget;
		
		Set<IProject> selectedProjects = SelectionExtracter.selectedProjects(SelectionExtracter.currentSelection());
		boolean allowDoubleSelection = selectedProjects.size() > 1;
		
		if (widget == baseProjectSelect) {
			Maybe<IProject> mSelectProject = selectProject();
			if (mSelectProject.isSatisfied()) {				
				baseArtifact = extractFromProject( mSelectProject.get());				
				if (baseArtifact != null) {
					baseArtifactName.setText( baseArtifact.asString());				
					otherProjectSelect.setEnabled( allowDoubleSelection);
				}
			}
		}
		else if (widget == baseArtifactSelect) {
			baseArtifact = extractFromArtifact(otherArtifact);
			if (baseArtifact != null) {
				baseArtifactName.setText( baseArtifact.asString());
				if (SelectionExtracter.currentProject() != null)  {
					otherProjectSelect.setEnabled(true);
				}
			}
		}
		else if (widget == baseFileSelect) {
			baseArtifact = extractFromFile();
			if (baseArtifact != null) {
				baseArtifactName.setText( baseArtifact.asString());
				if (SelectionExtracter.currentProject() != null)  {
					otherProjectSelect.setEnabled(true);
				}
			}
		}
		else if (widget == otherProjectSelect) {
			Maybe<IProject> mSelectProject = selectProject();
			if (mSelectProject.isSatisfied()) {			
				otherArtifact = extractFromProject( mSelectProject.get());								
				if (otherArtifact != null) {
					otherArtifactName.setText( otherArtifact.asString());
					baseProjectSelect.setEnabled(allowDoubleSelection);
				}		
			}
		}
		else if (widget == otherArtifactSelect) {
			otherArtifact = extractFromArtifact(baseArtifact);
			if (otherArtifact != null) {
				otherArtifactName.setText( otherArtifact.asString());
				if (SelectionExtracter.currentProject() != null)  {
					baseProjectSelect.setEnabled(true);
				}
			}		
		}
		else if (widget == otherFileSelect) {
			otherArtifact = extractFromFile();
			if (otherArtifact != null) {
				otherArtifactName.setText( otherArtifact.asString());
				if (SelectionExtracter.currentProject() != null)  {
					baseProjectSelect.setEnabled(true);
				}
			}
		}
		
		activateOk();
	}

	
	private void activateAllChoosers( boolean enable) {
		baseProjectSelect.setEnabled(enable);
		baseArtifactSelect.setEnabled(enable);
		baseFileSelect.setEnabled(enable);
		
		otherProjectSelect.setEnabled(enable);
		otherArtifactSelect.setEnabled(enable);
		otherFileSelect.setEnabled(enable);
	}
	
	/**
	 * gets a extraction result from zed of the selected project
	 * @param currentProject
	 * @return
	 */
	private Artifact extractFromProject(IProject currentProject) {
		
		extractionReturned = false;
		currentlyProcessedArtifact = null;
		statusLabel.setText( "processing project : " + currentProject.getName() + " .... ");
		// deactivate all chooser
		activateAllChoosers(false);
		shell.getDisplay().readAndDispatch();
		
		Thread thread = new Thread() {
			
			@Override
			public void run() {
				try {
					Maybe<ClassesProcessingRunnerContext> contextMaybe = ZedRunnerTrait.produceContext(currentProject);
					
					if (contextMaybe.isUnsatisfied()) {
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot process : " + currentProject.getName(), (Reason) contextMaybe.whyUnsatisfied());
						DevrockPlugin.instance().log(status);
						extractionReturned = true;
						return;
					}
					ClassesProcessingRunnerContext context = contextMaybe.get();
					ZedWireRunner runner = ZedRunnerTrait.runExtraction(context);			
					currentlyProcessedArtifact = runner.analyzedArtifact();
				} catch (Exception e) {
					;
				}				
				extractionReturned = true;
			}
		};
		
		thread.start();
		
		while (!extractionReturned) {
			shell.getDisplay().readAndDispatch();
		}
		// activate all choosers 
		activateAllChoosers(true);
		if (currentlyProcessedArtifact != null) {
			statusLabel.setText( "processing project : " + currentlyProcessedArtifact.asString() + " .... done");
		}
		else {
			statusLabel.setText( "processing project : " + currentProject.getName() + " .... failed");
		}
	
		return currentlyProcessedArtifact;
	}
	
	/**
	 * gets an extraction result from zed of the to be selected remote artifact
	 * @return
	 */
	private Artifact extractFromArtifact(Artifact artifact) {
						
		// Dialog to select analysis target	
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		///
		ZedTargetDialog targetSelector = new ZedTargetDialog( shell);
		
		if (artifact != null) {
			VersionedArtifactIdentification vai = VersionedArtifactIdentification.create( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
			targetSelector.setInitialIdentifications( Collections.singletonList(vai));
		}
		else {
			IProject project = SelectionExtracter.currentProject();
			if (project != null) {
				BasicWorkspaceProjectInfo projectInfo = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(project);
				if (projectInfo != null) {
					VersionedArtifactIdentification vai =  projectInfo.getVersionedArtifactIdentification();
					targetSelector.setInitialIdentifications( Collections.singletonList(vai));
				}
			}
		}
		
		int retval = targetSelector.open();
		if (retval == org.eclipse.jface.dialogs.Dialog.CANCEL) {			
			statusLabel.setText( "no selection to process");
			return null;
		}
		
		// process here, i.e get the extraction
		Maybe<List<CompiledTerminal>> selectionMaybe = targetSelector.getSelection();
		if (selectionMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("no selection", (Reason) selectionMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			statusLabel.setText( "no selection to process");
			return null;
		}
		List<CompiledTerminal> selection = selectionMaybe.get();
		
		extractionReturned = false;
		currentlyProcessedArtifact = null;
		
		currentCompiledTerminal = selection.get(0);
		statusLabel.setText( "processing artifact : " + currentCompiledTerminal.asString() + " .... ");

		Thread thread = new Thread() {			
			public void run() {
				try {
					
					McBridge mcBridge = ZedRunnerTrait.produceMcBridgeForExtraction( targetSelector.getCustomRepositoryConfiguration());
					Maybe<ClassesProcessingRunnerContext> contextMaybe = ZedRunnerTrait.produceContext(currentCompiledTerminal, mcBridge);
					if (contextMaybe.isUnsatisfied()) {
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot process : " + currentCompiledTerminal.asString(), (Reason) contextMaybe.whyUnsatisfied());
						DevrockPlugin.instance().log(status);
						extractionReturned = true;
						return;
					}
					
					ClassesProcessingRunnerContext context = contextMaybe.get();
					ZedWireRunner runner = ZedRunnerTrait.runExtraction(context);			
					
					currentlyProcessedArtifact = runner.analyzedArtifact();
				} catch (Exception e) {
					System.err.println(e);
				}				
				extractionReturned = true;				
			};
	
		};
		thread.start();
		
		while (!extractionReturned) {
			shell.getDisplay().readAndDispatch();
		}
		if (currentlyProcessedArtifact != null) {		
			statusLabel.setText( "processing selection : " + currentlyProcessedArtifact.asString() + " .... done");
		}
		else if (currentCompiledTerminal != null){ 
			statusLabel.setText( "processing selection : " + currentCompiledTerminal.asString() + " .... failed");
		}
		else {
			statusLabel.setText( "no selection to process");
		}
	
		return currentlyProcessedArtifact;
		
	}
	
	/**
	 * @return - an {@link Artifact} read from a persited extraction
	 */
	private Artifact extractFromFile() {		
			FileDialog dialog = new FileDialog( parentShell);
			String file = lastSelectedFile;	
			if (file != null) {
				dialog.setFileName( file);
				dialog.setFilterExtensions( new String [] {"*.yaml"});
				dialog.setFilterPath( new File(file).getAbsoluteFile().getParent());
			}
			String name = dialog.open();
			if (name == null)
				return null;
			
			lastSelectedFile = name;
			statusLabel.setText( "processing file : " + lastSelectedFile);
			extractionReturned = false;
			Thread thread = new Thread() {
				@Override
				public void run() {
					try (InputStream in = new FileInputStream( new File( lastSelectedFile))) {
						ViewingContextStorageContainer context = (ViewingContextStorageContainer) marshaller.unmarshall(in);
						currentlyProcessedArtifact = context.getArtifact();
						extractionReturned = true;
					} catch (Exception e) {
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot load zed extraction file: " + lastSelectedFile, IStatus.ERROR);
						DevrockPlugin.instance().log(status);
					}

					super.run();
				}
			};
			thread.start();
			while (!extractionReturned) {
				shell.getDisplay().readAndDispatch();
			}
			if (currentlyProcessedArtifact != null) {
				statusLabel.setText( "extracting artifact : " + currentlyProcessedArtifact.asString() + " .... done");
			}
			else {
				statusLabel.setText( "processing file : " + lastSelectedFile + " .... failed");
			}
		
			return currentlyProcessedArtifact;														
	}

	@Override
    public void dispose() {
    	
    }
	
	public Pair<Artifact,Artifact> getSelectedExtractions() {
		return Pair.of( baseArtifact, otherArtifact);
	}
	
	public SemanticVersioningLevel getSelectedSemanticLevel() {
		return chosenSemanticCompareLevel;
	}
}
