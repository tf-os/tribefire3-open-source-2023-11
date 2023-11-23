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
package com.braintribe.devrock.artifactcontainer.ui.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.AntTarget;
import com.braintribe.model.malaclypse.cfg.preferences.ac.AntRunnerPreferences;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;

public class AntWizardDialog extends Dialog implements SelectionListener, Listener, ArtifactSelectionListener {
	private static final String TARGET_ARTIFACT_S = "Target artifact [%s]";
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static final String ANT_TARGET = "ANT_TARGET";
	private static final String ANT_RUNNER_WIZARD = "ANT runner wizard";
	private static final String paddingString = "                       ";
	private static final int padding = 12;
	private Shell parentShell;
	private Font bigFont;	
	private List<Artifact> selectedTargetArtifacts;
	private boolean preprocess = false;
	
	private Tree availableTargets;	
	private Button addTargetButton;
	private Button downTargetButton;
	private Button upTargetButton;
	private Button removeTargetButton;
	private Tree selectedTargets;
	
	private Button loadProjectAfterRun;
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private AntRunnerPreferences arPreferences = plugin.getArtifactContainerPreferences(false).getAntRunnerPreferences();
	
	private StringEditor skipPositionEditor;

	private Image availableAntTargetImage;
	private Image selectedAntTargetImage;
	
	private Image leftArrowImage;
	private Image upArrowImage;
	private Image rightArrowImage;
	private Image downArrowImage;
	
	private Map<Integer, CTabItem> indexToItemMap = new HashMap<Integer, CTabItem>();
	private Map<CTabItem, Integer> itemToIndexMap = new HashMap<CTabItem, Integer>();
	private Map<CTabItem, AntWizardArtifactTab> itemToTabMap = new HashMap<CTabItem, AntWizardArtifactTab>();
	private Map<AntWizardArtifactTab, CTabItem> tabToItemMap = new HashMap<AntWizardArtifactTab, CTabItem>();
	
	private int maxTabs = 0;
	
	private CTabFolder tabFolder;
	private Button addTabItem; 
	private Button removeTabItem;
	
	private Image addItemImage;
	private Image removeItemImage;
	
	
	public AntWizardDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "ant.png");
		availableAntTargetImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "ant_targets.png");
		selectedAntTargetImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "arrow-000-small.png");
		leftArrowImage = imageDescriptor.createImage();
	
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "arrow-090-small.png");
		upArrowImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "arrow-180-small.png");
		rightArrowImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "arrow-270-small.png");
		downArrowImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "add.gif");
		addItemImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AntWizardDialog.class, "remove.gif");
		removeItemImage = imageDescriptor.createImage();
		
		
	}
	
	@Configurable
	public void setSelectedTargetArtifacts(List<Artifact> selectedTargetArtifacts) {
		this.selectedTargetArtifacts = selectedTargetArtifacts;
	}
	
	@Override
	protected Point getInitialSize() {
		
		return new Point( 700, 600);
	}

	@Override
	public boolean close() {
		bigFont.dispose();
		
		availableAntTargetImage.dispose();
		selectedAntTargetImage.dispose();
		leftArrowImage.dispose();
		upArrowImage.dispose();
		rightArrowImage.dispose();
		downArrowImage.dispose();
		
		addItemImage.dispose();		
		removeItemImage.dispose();
		
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText( ANT_RUNNER_WIZARD);
	}

	private String pad( String tag) {
		int l = tag.length();
		if (l < padding) {
			return tag + paddingString.substring(0, padding - l);
		}
		return tag;
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
        artifactComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
                        
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
    	tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
    	tabFolder.setLayout( new FillLayout());
    	int i = 0;
    	if (selectedTargetArtifacts != null && selectedTargetArtifacts.size() > 0) {
	    	for (Artifact artifact : selectedTargetArtifacts) {
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
    					AntWizardArtifactTab tab = itemToTabMap.get(tabItem);
    					Artifact selectedArtifact = tab.getSelectedArtifact();
    					if (selectedArtifact != null) {
        					tabItem.setText( NameParser.buildName(selectedArtifact));
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
		
        label = new Label( composite, SWT.NONE);
    	label.setText( "Target selection");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	label.setFont(bigFont);
		
		// target selection
    	Composite targetComposite = new Composite( composite, SWT.NONE);    	
    	GridLayout targetCompositeLayout= new GridLayout();
    	targetCompositeLayout.numColumns = 3;
        targetComposite.setLayout( targetCompositeLayout);       
        targetComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
           
        Composite availableTargetsComposite = new Composite( targetComposite, SWT.NONE);
        availableTargetsComposite.setLayout( layout);
        availableTargetsComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1,1));
      
        label = new Label( availableTargetsComposite, SWT.NONE);
    	label.setText( "Available");    	
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
	     
        availableTargets = new Tree( availableTargetsComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        availableTargets.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,4));
        
        for (AntTarget setting: arPreferences.getTargets()) {
        	TreeItem item = new TreeItem( availableTargets, SWT.NONE);
        	item.setText( setting.getName());
        	item.setImage( availableAntTargetImage);
        	item.setData( ANT_TARGET, setting);
        }        
        availableTargets.addListener(SWT.MouseDoubleClick, this);
        
        Composite buttonRowComposite = new Composite( targetComposite, SWT.NONE);
        buttonRowComposite.setLayout( layout);
        buttonRowComposite.setLayoutData( new GridData( SWT.CENTER, SWT.FILL, false, true, 1,1));
        
        label = new Label( buttonRowComposite, SWT.NONE);
    
        addTargetButton = new Button( buttonRowComposite, SWT.NONE);
        addTargetButton.setImage( rightArrowImage);
        addTargetButton.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
        addTargetButton.addSelectionListener(this);
        addTargetButton.setToolTipText("add target to sequence");
        
        downTargetButton = new Button( buttonRowComposite, SWT.NONE);        
        downTargetButton.setImage(downArrowImage);
        downTargetButton.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
        downTargetButton.addSelectionListener(this);
        downTargetButton.setToolTipText("move target down to a later position in sequence");
        
        upTargetButton = new Button( buttonRowComposite, SWT.NONE);        
        upTargetButton.setImage( upArrowImage);
        upTargetButton.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
        upTargetButton.addSelectionListener(this);
        upTargetButton.setToolTipText("move target up to an earlier position in sequence");
        
        removeTargetButton = new Button( buttonRowComposite, SWT.NONE);
        removeTargetButton.setImage( leftArrowImage);
        removeTargetButton.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));
        removeTargetButton.addSelectionListener( this);
        removeTargetButton.setToolTipText("remove target from sequence");
        
        Composite selectedTargetsComposite = new Composite( targetComposite, SWT.NONE);
        selectedTargetsComposite.setLayout( layout);
        selectedTargetsComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1,1));
        
        label = new Label( selectedTargetsComposite, SWT.NONE);
    	label.setText( "Selected");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	        
    	selectedTargets = new Tree( selectedTargetsComposite, SWT.NONE);
        selectedTargets.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,4));
        selectedTargets.addListener(SWT.MouseDoubleClick, this);
                   
        skipPositionEditor = new StringEditor();
        Composite skipPositionComposite = skipPositionEditor.createControl( targetComposite, pad( "Skip at position:"));
        skipPositionComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        skipPositionEditor.setEnabled( false);
                        
        label = new Label( composite, SWT.NONE);
    	label.setText( "Process settings");
    	label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	label.setFont(bigFont);
    	
        loadProjectAfterRun = new Button( composite, SWT.CHECK); 
        loadProjectAfterRun.setText( "Load project after task's run");
        loadProjectAfterRun.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
        
        // activate per default
        loadProjectAfterRun.setSelection(true);
       
        parentShell.layout(true);
        
        return composite;
	}

	private void addTab(int i, Artifact artifact, Color background) {
		// create tab items
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		AntWizardArtifactTab tab = new AntWizardArtifactTab( parentShell, artifact);
		Composite pageComposite = tab.createControl( tabFolder);
		tab.addArtifactSelectionListener(this);
		pageComposite.setBackground( background);
		item.setControl( pageComposite);
		if (artifact != null) {
			String name = NameParser.buildName(artifact);
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
		
		if (event.widget == addTargetButton) {
			TreeItem [] items = availableTargets.getSelection();
			if (items == null || items.length == 0)
				return;
			TreeItem leftItem = items[0];
			TreeItem rightItem = new TreeItem( selectedTargets, SWT.NONE);
			rightItem.setText( leftItem.getText());
			rightItem.setImage(selectedAntTargetImage);
			rightItem.setData(ANT_TARGET, leftItem.getData(ANT_TARGET));
			selectedTargets.setSelection( new TreeItem [] {rightItem});
			leftItem.dispose();			
			
			// check the state of skip editor
			if (checkSkipAvailablity()) {
				skipPositionEditor.setEnabled(true);
			}
			else {
				skipPositionEditor.setEnabled( false);
			}
		}
		if (event.widget == removeTargetButton) {
			TreeItem [] items = selectedTargets.getSelection();
			if (items == null || items.length == 0)
				return;
			TreeItem rightItem = items[0];
			TreeItem leftItem = new TreeItem( availableTargets, SWT.NONE);
			leftItem.setText( rightItem.getText());
			leftItem.setImage(availableAntTargetImage);
			leftItem.setData(ANT_TARGET, rightItem.getData( ANT_TARGET));
			availableTargets.setSelection( new TreeItem [] {leftItem});
			rightItem.dispose();			
			
			// check the state of skip editor
			if (checkSkipAvailablity()) {
				skipPositionEditor.setEnabled(true);
			}
			else {
				skipPositionEditor.setEnabled( false);
			}
		}
		
		if (event.widget == upTargetButton){
			TreeItem [] items = selectedTargets.getSelection();
			if (items == null || items.length == 0)
				return;
			// get index of selected item 
			TreeItem rightItem = items[0];
			TreeItem movedItem = moveTreeItem( rightItem, selectedTargets, true);
			selectedTargets.setSelection( new TreeItem [] {movedItem});			
		}
		if (event.widget == downTargetButton){
			TreeItem [] items = selectedTargets.getSelection();
			if (items == null || items.length == 0)
				return;
			TreeItem rightItem = items[0];
			TreeItem movedItem = moveTreeItem( rightItem, selectedTargets, false);
			selectedTargets.setSelection( new TreeItem [] {movedItem});
		}
	}
	
	private TreeItem moveTreeItem( TreeItem item, Tree tree, boolean upwards) {
		for (int i = 0; i < tree.getItemCount(); i++) {
			TreeItem suspect = tree.getItem(i);
			if (suspect != item) {
				continue;
			}
			if (upwards) {
				if (i == 0)
					return item;
				int p = i-1;
				TreeItem newItem = new TreeItem( tree, SWT.NONE, p);
				newItem.setText( item.getText());
				newItem.setImage( item.getImage());
				newItem.setData(ANT_TARGET, item.getData( ANT_TARGET));
				item.dispose();
				return newItem;
			}
			else {
				if (i == tree.getItemCount()-1) 
					return item;
				int p = i+2;
				TreeItem newItem = new TreeItem( tree, SWT.NONE, p);
				newItem.setText( item.getText());
				newItem.setImage( item.getImage());
				newItem.setData(ANT_TARGET, item.getData( ANT_TARGET));
				item.dispose();
				return newItem;
			}		
		}
		return item;
	}
	
	private boolean checkSkipAvailablity() {		
		List<AntTarget> targets = getSelectedTargets();
		if (targets.size() == 0) {
			return false;
		}
		for (AntTarget target : targets) {
			if (target.getTransitiveNature()) {
				return true;
			}			
		}
		return false;
	}
	
	
	
	private List<Artifact> getSelectedArtifacts() {
		List<Artifact> artifacts = new ArrayList<>();
		for (int i = 0; i < maxTabs; i++) {
			CTabItem item = indexToItemMap.get( i);
			AntWizardArtifactTab tab = itemToTabMap.get( item);
			
			Artifact selectedArtifact = tab.getSelectedArtifact();
			if (selectedArtifact != null) {
				artifacts.add(selectedArtifact);
			}
		}				
		return artifacts;
	}
	
	private List<AntTarget> getSelectedTargets() {
		List<AntTarget> result = new ArrayList<AntTarget>();
		TreeItem[] items = selectedTargets.getItems();
		if (items == null || items.length == 0)
			return result;
		
		for (TreeItem item : items) {
			AntTarget target = (AntTarget) item.getData( ANT_TARGET);
			result.add( target);
		}
		return result;
	}
	
	private List<String> extractTargetNames( List<AntTarget> targets) {
		List<String> result = new ArrayList<String>();
		for (AntTarget target : targets) {
			result.add( target.getTarget());
		}
		return result;
	}
	
	@Override
	protected void okPressed() {
		final List<Artifact> selectedArtifacts = getSelectedArtifacts();
		if (selectedArtifacts == null) {
			MessageDialog.openError(getShell(), "AC Antwizard", "No valid artifact selected");
			return;
		}
		
		final List<String> selectedTargets = extractTargetNames(getSelectedTargets());
		if (selectedTargets.size() == 0) {
			MessageDialog.openError(getShell(), "AC Antwizard", "No tasks selected");
			return;
		}
		final boolean loadAfterRun = loadProjectAfterRun.getSelection();
		
		
		// find matching root of selected artifact
		StringBuilder queryExpressionBuilder = new StringBuilder();
		StringBuilder runExpressionBuilder = new StringBuilder();
		for (Artifact artifact : selectedArtifacts) {
			if (queryExpressionBuilder.length() > 0) {
				queryExpressionBuilder.append( "|");
				runExpressionBuilder.append( "+");
			}
			String artifactExpression = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + VersionProcessor.toString(artifact.getVersion()); 
			runExpressionBuilder.append(artifactExpression);
			queryExpressionBuilder.append( artifactExpression);
		}	
		
		List<SourceArtifact> sourceArtifacts = ArtifactContainerPlugin.getInstance().getQuickImportScanController().runSourceArtifactQuery( queryExpressionBuilder.toString());
		if (sourceArtifacts == null || sourceArtifacts.size() == 0) {
			MessageDialog.openError(getShell(), "AC Antwizard", "No valid artifact found");
			return;
		}
		// 
		String sourceRepositoryRoot = null;
		for (SourceArtifact sourceArtifact : sourceArtifacts) {
			String repoUrl = sourceArtifact.getRepository().getRepoUrl();
			if (sourceRepositoryRoot == null) {
				sourceRepositoryRoot = repoUrl;
			}
			else {
				if (!repoUrl.equalsIgnoreCase(sourceRepositoryRoot)) {
					MessageDialog.openError(getShell(), "AC Antwizard", "The artifacts selected do not have a common source repository origin");
					return;
				}
			}
		}
		
		URL repoUrl;
		try {
			repoUrl = new URL(sourceArtifacts.get(0).getRepository().getRepoUrl());
		} catch (MalformedURLException e1) {
			MessageDialog.openError(getShell(), "AC Antwizard", "invalid source repository encountered");
			return;
		}
		final String workingCopy = repoUrl.getFile();
		final Map<String, String> properties = new HashMap<String, String>();
		if (checkSkipAvailablity()) {
			String trimmedSkip = skipPositionEditor.getSelection().trim();
			if (trimmedSkip.length()> 0) {
				properties.put( "skip", trimmedSkip);
			}
		}
		
		Job job = new Job(ANT_RUNNER_WIZARD) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MessageMonitorBridge bridge = new MessageMonitorBridge( ANT_RUNNER_WIZARD, monitor);
		    	String runExpression = runExpressionBuilder.toString();
		    	System.out.println( "Expression to run " + runExpression);
				AntRunPerCommandLine antRun = new AntRunPerCommandLine( runExpression, properties, workingCopy);
				try {
					File workingCopyDirectory = new File( workingCopy);
					antRun.run( selectedTargets, workingCopyDirectory, bridge);					
					if (loadAfterRun) {
						List<ProjectImporterTuple> tuples = new ArrayList<ProjectImporterTuple>();
						for (Artifact artifact : selectedArtifacts) {
							if (ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(artifact) == null) {
								File projectFile = new File( NameParser.buildPartialPath( artifact, artifact.getVersion(), workingCopy) + File.separator +  ".project");
								if (projectFile.exists()) {
									ProjectImporterTuple importerTuple = new ProjectImporterTuple(projectFile.getAbsolutePath(), artifact);
									tuples.add(importerTuple);
								}
							}
						}
						if (tuples.size() > 0) {
							ProjectImporter.importProjects( preprocess, new TargetProviderImpl(), ArtifactContainerPlugin.getWorkspaceProjectRegistry(), tuples.toArray( new ProjectImporterTuple[0]));
						}
					}									
					return Status.OK_STATUS;
				} catch (AntRunException e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot run targets", e);
					ArtifactContainerPlugin.getInstance().log(status);		
					return Status.CANCEL_STATUS;
				} 
			}
		};
		job.schedule();
		
		super.okPressed();
	}

	@Override
	public void handleEvent(Event event) {
		SelectionEvent selectionEvent = new SelectionEvent(event);
		if (event.widget == availableTargets) {
			selectionEvent.widget = addTargetButton;
		}
		if (event.widget == selectedTargets) {
			selectionEvent.widget = removeTargetButton;
		}
		widgetSelected(selectionEvent);
	}

	@Override
	public void acknowledgeArtifactSelection(AntWizardArtifactTab tab, String name) {
		CTabItem item = tabToItemMap.get(tab);
		item.setText( name);				
		item.setToolTipText( String.format(TARGET_ARTIFACT_S, name));
	}
	
	
	
}
