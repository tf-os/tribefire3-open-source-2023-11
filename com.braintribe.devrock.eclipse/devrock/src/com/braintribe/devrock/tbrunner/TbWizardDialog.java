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
package com.braintribe.devrock.tbrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.editors.StringEditor;
import com.braintribe.devrock.api.ui.selection.CustomFileSelector;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.reason.devrock.PluginReason;
import com.braintribe.devrock.importer.ProjectImporter;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;


public class TbWizardDialog extends DevrockDialog implements SelectionListener, Listener, ArtifactSelectionListener {
	private static final String AC_TB_WIZARD = "DR runner wizard";
	
	
	private static final String TARGET_ARTIFACT_S = "Target artifact [%s]";
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;

	//private static final String paddingString = "                       ";
	//private static final int padding = 12;
	private Shell parentShell;
	private Font bigFont;	
	private List<EnhancedCompiledArtifactIdentification> selectedTargetArtifacts;
	
	

	
	private Map<Integer, CTabItem> indexToItemMap = new HashMap<Integer, CTabItem>();
	private Map<CTabItem, Integer> itemToIndexMap = new HashMap<CTabItem, Integer>();
	private Map<CTabItem, TbWizardArtifactTab> itemToTabMap = new HashMap<CTabItem, TbWizardArtifactTab>();
	private Map<TbWizardArtifactTab, CTabItem> tabToItemMap = new HashMap<TbWizardArtifactTab, CTabItem>();
	
	private int maxTabs = 0;
	
	private CTabFolder tabFolder;
	private Button addTabItem; 
	private Button removeTabItem;
	
	private Image addItemImage;
	private Image removeItemImage;
	
	private Button transitive;
	private Button group;
	private Button codebase;

	private StringEditor skipPositionEditor;
	private UiSupport uiSupport = DevrockPlugin.instance().uiSupport();


	private CustomFileSelector customFileSelector;


	private BooleanEditor loadProject;


	private BooleanEditor refreshProject;
	
	public TbWizardDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);		
			
		addItemImage = uiSupport.images().addImage("addBuildTarget", TbWizardDialog.class, "add.gif");				
		removeItemImage = uiSupport.images().addImage("removeBuildTarget", TbWizardDialog.class, "remove.gif");
		 				
	}
	
	@Configurable
	public void setSelectedTargetArtifacts(List<EnhancedCompiledArtifactIdentification> selectedTargetArtifacts) {
		this.selectedTargetArtifacts = selectedTargetArtifacts;
	}
	
	
	
	@Override
	protected Point getDrInitialSize() {
		return new Point( 800, 600);
	}

	@Override
	public boolean close() {
		bigFont.dispose();
								
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText( AC_TB_WIZARD);
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
        artifactComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
                        
        Label label = new Label( artifactComposite, SWT.NONE);
    	label.setText( "Target artifacts");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 2, 1));
    	label.setFont(bigFont);
    	
    	
    	addTabItem = new Button( artifactComposite, SWT.NONE);
    	addTabItem.setToolTipText( "add a target tab");
    	addTabItem.setImage(addItemImage);
    	addTabItem.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    	
    	removeTabItem = new Button( artifactComposite, SWT.NONE);
    	removeTabItem.setToolTipText( "remove selected target tab");
    	removeTabItem.setImage(removeItemImage);
    	removeTabItem.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    	
    	// tab folder     	  
    	tabFolder = new CTabFolder( artifactComposite, SWT.NONE);
    	tabFolder.setBackground( parent.getBackground());
    	tabFolder.setSimple( false);		
    	tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
    	tabFolder.setLayout( new FillLayout());
    	int i = 0;
    	if (selectedTargetArtifacts != null && selectedTargetArtifacts.size() > 0) {
	    	for (EnhancedCompiledArtifactIdentification artifact : selectedTargetArtifacts) {
	    		addTab(i++, artifact, parent.getBackground());
	    	}
	    	maxTabs = i;
    	}
    	else {
	    	// add empty one
	    	addTab(i++, null, parent.getBackground());
	    	maxTabs = 1;
    	}
    	addTabItem.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				addTab(maxTabs++, null, parent.getBackground());
				tabFolder.setSelection( maxTabs-1);
			}    		
		});    	    			
    	
    	removeTabItem.addSelectionListener( new SelectionAdapter() {
    		@Override
			public void widgetSelected(SelectionEvent e) {			
    			if (tabFolder.getItemCount() < 2) {
    				return;
    			}
    			int indexToRemove = tabFolder.getSelectionIndex();
    			if (indexToRemove < 0) { 
    				return;
    			}
    			CTabItem item = tabFolder.getItem(indexToRemove);    			
    			for (int i = 0; i < maxTabs; i++) {
    				if (i == indexToRemove) {
    					continue;
    				}
    				CTabItem tabItem = null;
    				if (i < indexToRemove) {
    					tabItem = indexToItemMap.get( i);
    				}
    				else {
    					if (i+1 < maxTabs) {
    						tabItem = indexToItemMap.get( i+1);
    					}
    				}    				
    				if (tabItem != null) {
    					TbWizardArtifactTab tab = itemToTabMap.get(tabItem);
    					EnhancedCompiledArtifactIdentification selectedArtifact = tab.getSelectedArtifact();
    					if (selectedArtifact != null) {
        					tabItem.setText( selectedArtifact.asString());
    					}
    					tabItem.setText("new target");
    					indexToItemMap.put(i, tabItem);    					
    				}
    			}
    			maxTabs--;
    			item.dispose();
    			tabFolder.setSelection( indexToRemove);
    		}    		
		});
		tabFolder.setSelection(0);
		
		
		
		  // 
		Composite fComposite = new Composite(composite, SWT.BORDER);
		fComposite.setLayout(layout);
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		
		customFileSelector = new CustomFileSelector();
		customFileSelector.setBigFont(bigFont);
		customFileSelector.setTitle( "build file");
		customFileSelector.setToolTip("Allows the selection of the build file to be used");

		customFileSelector.setStandardLabel( "default");
		customFileSelector.setStandardLabelTip( "use default build file (build.xml in working directory)");
		customFileSelector.setStandardCheckTip( "if on, the standard build file is used, if off, a custom build file can be used");
		customFileSelector.setStandardSlot(StorageLockerSlots.SLOT_TBR_STANDARD_BUILDFILE);		
		customFileSelector.setCustomAssociatedOverrideSlot( StorageLockerSlots.SLOT_TBR_STANDARD_ASSOCIATION);
		customFileSelector.setInitialStandardValue(true);
		

		customFileSelector.setCustomLabel( "custom build file");
		customFileSelector.setCustomLabelTip( "use an alternative build file");
		customFileSelector.setCustomCheckTip( "select a custom build file");
		customFileSelector.setExtensions(null);		
		customFileSelector.setCustomAssociationSlot(StorageLockerSlots.SLOT_TBR_CUSTOM_BUILDFILE_ASSOCIATION);
		
		// show custom value if present? 
		List<EnhancedCompiledArtifactIdentification> selectedArtifacts = getSelectedArtifacts();
		if (selectedArtifacts.size() == 1) {
			EnhancedCompiledArtifactIdentification ecai = selectedArtifacts.get(0);					
			customFileSelector.setCustomArtifactKey( ecai);
		}

		Composite customFileSelectorComposite = customFileSelector.createControl(fComposite);
		customFileSelectorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		
	
    	
    	// mode selection
    	Composite modeComposite = new Composite( composite, SWT.BORDER);
    	modeComposite.setToolTipText("Allows to specify what is the base directory of the build and whether it should build required artifacts as well");
    	
    	
    	
    	GridLayout modeCompositeLayout= new GridLayout();
    	modeCompositeLayout.numColumns = 4;
    	modeComposite.setLayout( modeCompositeLayout);       
    	modeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,2));

    	label = new Label( modeComposite, SWT.NONE);
    	label.setText( "mode");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, true, 4, 1));
    	label.setFont(bigFont);
    	
    	transitive = new Button( modeComposite, SWT.CHECK);
    	transitive.setText( "transitive");
    	transitive.setToolTipText("if activated, the build will work on codebase/group-level and use the build range feature to build all required artifacts. Otherwise it will build the artifacts singularily");
    	transitive.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	boolean transitiveBuildOn = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_TBR_TRANSITIVE, false);
		transitive.setSelection( transitiveBuildOn);
    	transitive.addSelectionListener(this);
    	if (transitiveBuildOn) {

    	}
	
    	    	
		
        group = new Button( modeComposite, SWT.RADIO);
        group.setText( "group-wide");
        group.setSelection( true);
        group.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
        group.setToolTipText("standard transitive build within the group directory");
        
            	
    	codebase = new Button( modeComposite, SWT.RADIO);
    	codebase.setText( "codebase-wide");
    	codebase.setSelection(false);
    	codebase.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	codebase.setToolTipText("standard transitive build across groups");
    		 
        skipPositionEditor = new StringEditor();
        Composite skipPositionComposite = skipPositionEditor.createControl( modeComposite,  "Skip at position:");
        skipPositionComposite.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 4, 1));        
        
        
        // options codebase/group-wise and skip are only available if transitive build's on
        if (transitiveBuildOn) {
        	group.setEnabled(true);
        	codebase.setEnabled(true);
        	skipPositionEditor.setEnabled( true);
        } else {
        	group.setEnabled( false);
        	codebase.setEnabled( false);
        	skipPositionEditor.setEnabled( false);
        }
      
                        
        label = new Label( composite, SWT.NONE);
    	label.setText( "Process settings");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	label.setFont(bigFont);
    	
    	loadProject = new BooleanEditor();
    	loadProject.setLabelToolTip("Sets whether an external project was built, it should be loaded into the workspace");
    	loadProject.setCheckToolTip("If activated, a built project will be added to the workspace, depending on whether it was already present");
    	Composite control = loadProject.createControl(composite, "Load project after build has completed");
    	control.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
    
    	refreshProject = new BooleanEditor();
    	refreshProject.setLabelToolTip("Sets whether the built projects of the workspace should be refreshed");
    	refreshProject.setCheckToolTip("If activated, a built project of the workspace is refreshed");
    	control = refreshProject.createControl(composite, "Refresh project after build has completed");
    	control.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
    	
        // deactivate per default
        loadProject.setSelection(false);
        // activate per default
        refreshProject.setSelection(true);
       
        parentShell.layout(true);
        
        // activate scan 
        Job scanJob = new Job("dr-runner autoscan for sources") {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				runscan();
				return Status.OK_STATUS;
			}
		}; 
		scanJob.schedule();
        
        return composite;
	}

	private void runscan() {
		 parentShell.getDisplay().asyncExec( new Runnable() {				
				@Override
				public void run() {	
					DevrockPlugin.instance().quickImportController().scheduleRescan();						
				}
			});	
	}
	private void addTab(int i, EnhancedCompiledArtifactIdentification artifact, Color background) {
		// create tab items
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		TbWizardArtifactTab tab = new TbWizardArtifactTab( parentShell, artifact);
		Composite pageComposite = tab.createControl( tabFolder);
		tab.addArtifactSelectionListener(this);
		pageComposite.setBackground( background);
		item.setControl( pageComposite);
		if (artifact != null) {
			String name = artifact.asString();
			item.setText( name);
			item.setToolTipText( String.format(TARGET_ARTIFACT_S, name));
		}
		else {
			item.setText("no target");
		}
		indexToItemMap.put( i, item);
		itemToIndexMap.put( item,  i);
		itemToTabMap.put( item, tab);
		tabToItemMap.put( tab, item);
	}
		

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}
	@Override
	public void widgetSelected(SelectionEvent event) {										
		if (event.widget == transitive) {
			if (transitive.getSelection()) {
				group.setEnabled(true);
				codebase.setEnabled( true);
				customFileSelector.setEnabled(false);				
			}
			else {
				group.setEnabled( false);
				codebase.setEnabled( false);
				customFileSelector.setEnabled(true);
			}
		}
	}
		
	
	private List<EnhancedCompiledArtifactIdentification> getSelectedArtifacts() {
		List<EnhancedCompiledArtifactIdentification> artifacts = new ArrayList<>();
		for (int i = 0; i < maxTabs; i++) {
			CTabItem item = indexToItemMap.get( i);
			TbWizardArtifactTab tab = itemToTabMap.get( item);
			
			EnhancedCompiledArtifactIdentification selectedArtifact = tab.getSelectedArtifact();
			if (selectedArtifact != null) {
				artifacts.add(selectedArtifact);
			}
		}				
		return artifacts;
	}
		
	
	@Override
	protected void okPressed() {
		final List<EnhancedCompiledArtifactIdentification> selectedArtifacts = getSelectedArtifacts();
		if (selectedArtifacts == null) {
			MessageDialog.openError(getShell(), AC_TB_WIZARD, "No valid artifact selected");
			return;
		}
		
		final IWorkingSet workingset = null; 
			
		final boolean loadAfterRun = loadProject.getSelection();
		final boolean refreshAfterRun = refreshProject.getSelection();
		
		//
		// turn selected artifacts in source artifacts (containing project information)
		//
		
		// build the query 
		StringBuilder queryExpressionBuilder = new StringBuilder();		
		for (EnhancedCompiledArtifactIdentification artifact : selectedArtifacts) {
			if (queryExpressionBuilder.length() > 0) {
				queryExpressionBuilder.append( "|");
			}
			String artifactExpression = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion().asString(); 
			queryExpressionBuilder.append( artifactExpression);
		}	
		
		// run the query
		DevrockPlugin plugin = DevrockPlugin.instance();
		
		List<EnhancedCompiledArtifactIdentification> sourceArtifacts = plugin.quickImportController().runProjectToSourceQuery( queryExpressionBuilder.toString());
		
		if (sourceArtifacts == null || sourceArtifacts.size() == 0) {
			MessageDialog.openError(getShell(), AC_TB_WIZARD, "No valid artifact found");
			return;
		}
	
		boolean transitiveBuild = transitive.getSelection();
		
		File buildWorkingDirectory;
		List<String> artifactExpressions = null;
		String singleArtifact = null;
				
		if (transitiveBuild) {
			Maybe<Pair<List<String>,File>> maybe = determineExpressionAndWorkingDirectoryForTransitiveBuilds(sourceArtifacts);			
			if (maybe.isUnsatisfied()) {
				MessageDialog.openError(getShell(), AC_TB_WIZARD, "Cannot determine build parameters as " + maybe.whyUnsatisfied().stringify());
				return;
			}
			Pair<List<String>, File> pair = maybe.get();
			artifactExpressions = pair.first;
			buildWorkingDirectory = pair.second;
		}
		else {			
			Maybe<File> maybe = determineExpressionAndWorkingDirectoryForSingleBuilds(sourceArtifacts);
			if (maybe.isUnsatisfied()) {
				MessageDialog.openError(getShell(), AC_TB_WIZARD, "Cannot determine build parameters as " + maybe.whyUnsatisfied().stringify());
				return;
			}
			buildWorkingDirectory = maybe.get();
			singleArtifact = sourceArtifacts.get(0).asString();
		}
		
		File selectedBuildFile = null;
		if (!customFileSelector.getCurrentlySelectedUsageOfStandardFile() && customFileSelector.getCurrentlySelectedCustomFile() != null) {
			selectedBuildFile = new File( customFileSelector.getCurrentlySelectedCustomFile());
		}
		else {
			selectedBuildFile = new File( buildWorkingDirectory, "build.xml");
		}
				
		final String skipExpression = skipPositionEditor.getSelection();
		final File workingDirectory = buildWorkingDirectory;
		final List<String> expressions = artifactExpressions;
		final String finalArtifact = singleArtifact;							
		
		// store select
		DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_TBR_TRANSITIVE, transitive.getSelection());
		customFileSelector.storeValues();
		
		final File buildFile = selectedBuildFile;

		Job job = new Job(AC_TB_WIZARD) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MessageMonitorBridge bridge = new MessageMonitorBridge( AC_TB_WIZARD, monitor);
		    	
				DrRunPerCommandLine antRun = new DrRunPerCommandLine();
				try {
					antRun.run( finalArtifact, expressions, skipExpression, buildFile, workingDirectory, bridge);					
					if (loadAfterRun) {
						ProjectImporter.importProjects(workingset, sourceArtifacts, null);
					}						
					
					if (refreshAfterRun) {
						for (EnhancedCompiledArtifactIdentification ecai : sourceArtifacts) {
							BasicWorkspaceProjectInfo info = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(ecai);
							if (info != null) {
								IProject project = info.getProject();
								project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
							}
						}
					}
					return Status.OK_STATUS;
				} catch (Exception e) {
					DevrockPluginStatus status = new DevrockPluginStatus("cannot run targets", e);
					plugin.log(status);		
					return Status.CANCEL_STATUS;
				} 
			}
		};
		job.schedule();
		
		
		super.okPressed();
	}
	
	private Maybe<File> determineExpressionAndWorkingDirectoryForSingleBuilds( List<EnhancedCompiledArtifactIdentification> sourceArtifacts) {
		if (sourceArtifacts.size() > 1) {
			// too many artifacts for single project build
			return Maybe.empty( Reasons.build(PluginReason.T).assign(PluginReason::setText, "too many projects for non-transitive build").toReason());
		}		
		EnhancedCompiledArtifactIdentification ecai = sourceArtifacts.get(0);
		File workingDir = new File(ecai.getOrigin()); 
		return Maybe.complete( workingDir);
			
	}

	Maybe<Pair<List<String>,File>> determineExpressionAndWorkingDirectoryForTransitiveBuilds(List<EnhancedCompiledArtifactIdentification> sourceArtifacts) {
		// process the result 			
		String groupRepositoryRoot = null;
		boolean enforceCodebaseWide = false;		
		
		String commonGroup = null, commonParent = null;
		for (EnhancedCompiledArtifactIdentification sourceArtifact : sourceArtifacts) {
			if (commonGroup == null) {
				commonGroup = sourceArtifact.getGroupId();
			}
			else {
			//	if (sourceArtifact.)
			}
			if (commonParent == null) {
				commonParent = new File( sourceArtifact.getOrigin()).getParent();
			}		
			
			
			String path = new File(sourceArtifact.getOrigin()).getParent();
			if (groupRepositoryRoot == null) {
				groupRepositoryRoot = path;
			}
			else {
				if (!groupRepositoryRoot.equalsIgnoreCase( path)) {
					enforceCodebaseWide = true;
				}
			}
		}
		
		boolean codebaseWide = codebase.getSelection() || enforceCodebaseWide;
		boolean useBrackets = !transitive.getSelection();
		
		
		/*
		if (codebaseWide) {		
			//  a) only one project -> parent of project
			if (sourceArtifacts.size() > 0) {
			//  b) multiple project -> common parent of projects				
				File devEnvScanRoot = DevrockPlugin.envBridge().getDevEnvScanRoot().orElse(null);
				if (devEnvScanRoot == null) {
					MessageDialog.openError(getShell(), AC_TB_WIZARD, "Multiple builds on codebase are only supported if a 'dev-env' is used");
					return;
				}												
			}
		}
		*/
		
		List<String> expressions = sourceArtifacts.stream().map( 
					s -> { 
							String expression;
							if (codebaseWide) {
								expression = s.getGroupId() + ":" + s.getArtifactId();
							}
							else {
								expression = s.getArtifactId();
							}
							if (useBrackets) {
								expression = "[" + expression + "]";
							}
							return expression;
						}
				).collect( Collectors.toList());
		
	
		File workingDirectory;
		if ( codebaseWide) {
			workingDirectory = DevrockPlugin.envBridge().getDevEnvBuildRoot().orElse(null);
			if (workingDirectory == null) {
				MessageDialog.openError(getShell(), AC_TB_WIZARD, "Multiple builds across groups (codebase) are only supported if a 'dev-env' is used");
				return null;		
			}
		}
		else {
			workingDirectory = new File( groupRepositoryRoot);  		
		}
		
		return Maybe.complete(Pair.of( expressions, workingDirectory));
	}

	@Override
	public void handleEvent(Event event) {
		SelectionEvent selectionEvent = new SelectionEvent(event);		
		widgetSelected(selectionEvent);
	}

	@Override
	public void acknowledgeArtifactSelection(TbWizardArtifactTab tab, String name) {
		CTabItem item = tabToItemMap.get(tab);
		item.setText( name);				
		item.setToolTipText( String.format(TARGET_ARTIFACT_S, name));
	}
	
	
	
}
