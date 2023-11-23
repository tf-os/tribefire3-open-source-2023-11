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
package com.braintribe.devrock.greyface.view.tab.parameter;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.generics.commons.CommonConversions;
import com.braintribe.devrock.greyface.generics.tree.TreeColumnResizer;
import com.braintribe.devrock.greyface.generics.tree.TreeDragger;
import com.braintribe.devrock.greyface.generics.tree.TreeDragger.DragParentType;
import com.braintribe.devrock.greyface.process.ProcessControl;
import com.braintribe.devrock.greyface.process.ProcessId;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.SelectionContext;
import com.braintribe.devrock.greyface.process.notification.SelectionContextListener;
import com.braintribe.devrock.greyface.process.scan.Scanner;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.devrock.greyface.settings.preferences.GreyfacePreferenceConstants;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.devrock.greyface.view.tab.parameter.LocalFileSystemScanner.Tuple;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.preferences.gf.GreyFacePreferences;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.dom.iterator.FilteringElementIterator;
import com.braintribe.utils.xml.dom.iterator.filters.TagNameFilter;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class ParameterTab extends GenericViewTab implements ModifyListener, SelectionListener, HasTreeTokens, 
															ScanProcessListener, SelectionContextListener, VirtualEnvironmentNotificationListener {

	private static final String ARTIFACTORY_BRAINTRIBE_PWD = "secret";
	private static final String ARTIFACTORY_BRAINTRIBE_USER = "bt_developer";
	private static final String ARTIFACTORY_BRAINTRIBE = "https://artifactory.server/artifactory/third-party";
	private static final String TAB_ID = "Parameter";
	private Text artifactExpressionText;
		
	private Image activeImage;
	private Image inactiveImage;
	
	private Button fileSystemImportButton;
	private Button scanButton;
	private Text importFile;
	
	private Button localFileSystemImportButton;
	private Button directoryScanButton;
	private Text localImportFile;
	
	private Button skipOptional;
	private Button skipTestScope;
	private Button stopScanOnKnownInTarget;
	private Button applyCompileScope;
	private Button validatePoms;
	
	private Button overwriteInTarget;
	private Button repairInTarget;
	private Tree sourceTree;
	
	private Button scan;
	private Text msg;
		
	private SelectionContext selectionContext = SelectionContext.T.create();
	private SelectionContextListener selectionContextListener;
	
	List<String> artifactsToScan;
	private TreeColumnResizer sourceColumnResizer;
	private TreeColumnResizer targetColumnResizer;
	private Scanner scanner;
	private Tree targetTree;
	
	private ProcessControl processControl;
	
	private LocalFileSystemScanner localFileSystemScanner = new LocalFileSystemScanner();

	private Button abortScan;
	private GreyfacePlugin plugin = GreyfacePlugin.getInstance();
	private GreyFacePreferences gfPreferences = plugin.getGreyfacePreferences( false);
	private VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();		
	
	@Configurable
	public void setProcessControl(ProcessControl processControl) {
		this.processControl = processControl;
	}
		
	@Configurable @Required
	public void setSelectionContextListener( SelectionContextListener selectionContextListener) {
		this.selectionContextListener = selectionContextListener;
	}
		
	@Override
	public String getId() {		
		return id;
	}

	public ParameterTab(Display display, Scanner scanner) {
		super(display);	 
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "add.png");
		activeImage = imageDescriptor.createImage();
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "delete.png");
		inactiveImage = imageDescriptor.createImage();	
		
		this.scanner = scanner;
		id = TAB_ID;
		
		virtualEnvironmentPlugin.addListener( this);
	}
	
	
	
	@Override
	public void dispose() {		
		activeImage.dispose();
		inactiveImage.dispose();
		virtualEnvironmentPlugin.removeListener(this);
		super.dispose();
	}


	
	@Override
	public Composite createControl(Composite parent) {
	    
		Composite composite = super.createControl(parent);
		
		GridLayout layout= new GridLayout();
        layout.numColumns = 4;
        composite.setLayout( layout);
        
    	GridLayout tightLayout = new GridLayout();
    	tightLayout.numColumns = 4;
    	tightLayout.marginHeight = 0;
    	tightLayout.verticalSpacing = 0;
    	tightLayout.marginWidth=0;
    	tightLayout.horizontalSpacing=0;
        				
		// field for artifact expression
		Composite artifactGroup = new Composite(composite, SWT.NONE);
		artifactGroup.setLayout( layout);
        artifactGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
		
		Label artifactLabel = new Label( artifactGroup, SWT.NONE);
    	artifactLabel.setText( "artifact expression");
    	artifactLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
    	artifactLabel.setFont( bigFont);
       		
        
        
        artifactExpressionText = new Text( artifactGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
    	GridData artifactExpressionGridData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	artifactExpressionGridData.minimumHeight = 100;
    	artifactExpressionGridData.heightHint = 100;
		artifactExpressionText.setLayoutData( artifactExpressionGridData);
    	artifactExpressionText.addModifyListener( this);
    
    	
    	Composite sourcesGroup = new Composite( composite, SWT.NONE); 
    	sourcesGroup.setLayout(layout);
    	sourcesGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
    	
    	Label sourcesLabel = new Label( sourcesGroup, SWT.NONE);
    	sourcesLabel.setText( "sources");
    	sourcesLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	sourcesLabel.setFont( bigFont);
  
    	Composite sourceTreeComposite = new Composite(sourcesGroup, SWT.NONE);
    	sourceTreeComposite.setLayout(tightLayout);
    	sourceTreeComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
    	
		sourceTree = new Tree ( sourceTreeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		sourceTree.setHeaderVisible( true);
		sourceTree.setLayout(tightLayout);
		GridData sourceTreeGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2);
		sourceTreeGridData.heightHint = 200;
		sourceTreeGridData.minimumHeight = 100;
		sourceTree.setLayoutData( sourceTreeGridData);
			
		String [] sourcesColumnNames = new String [] {"name", "url"};
		int [] sourcesColumnWeights = new int [] {100, 200};
		
		List<TreeColumn> sourceColumns = new ArrayList<TreeColumn>();		
		for (int i = 0; i < sourcesColumnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( sourceTree, SWT.LEFT);
			treeColumn.setText( sourcesColumnNames[i]);
			treeColumn.setWidth( sourcesColumnWeights[i]);
			treeColumn.setResizable( true);
			//treeColumn.addSelectionListener(treeSortListener);
			sourceColumns.add( treeColumn);
		}
	
		sourceColumnResizer = new TreeColumnResizer();
		sourceColumnResizer.setColumns( sourceColumns);
		sourceColumnResizer.setColumnWeights( sourcesColumnWeights);
		sourceColumnResizer.setParent( sourceTreeComposite);
		sourceColumnResizer.setTree( sourceTree);		
		sourceTree.addControlListener(sourceColumnResizer);
	
		List<RemoteRepository> settings = GreyfacePlugin.getInstance().getGreyfacePreferences(false).getSourceRepositories();
		for (RemoteRepository setting : settings) {
			TreeItem item = new TreeItem(sourceTree, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( setting.getName());
			texts.add( setting.getUrl());			
			item.setText( texts.toArray( new String[0]));
			item.setImage(activeImage);
			item.setData(KEY_ACTIVE, Boolean.TRUE);
			item.setData(KEY_REPOSITORY_SETTING, setting);
		}
		// activation / deactivation of sources 
		sourceTree.addListener( SWT.MouseDoubleClick, new Listener() {			
			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
		        TreeItem item = sourceTree.getItem(point);
		        if (item != null) {
		        	Boolean active = (Boolean) item.getData(KEY_ACTIVE);
		        	if (Boolean.TRUE.equals(active)) {
		        		item.setImage( inactiveImage);
		        		item.setData(KEY_ACTIVE, Boolean.FALSE);
		        		item.setFont( italicFont);
		        	} 
		        	else {
		        		item.setImage( activeImage);
		        		item.setData(KEY_ACTIVE, Boolean.TRUE);
		        		item.setFont( null);
		        	}
		        	// at least one entry should be active
		        	boolean oneActive = false;
		        	for (TreeItem suspect : sourceTree.getItems()) {
		        		if (Boolean.TRUE.equals( suspect.getData( KEY_ACTIVE))) {
		        			oneActive = true;
		        			break;
		        		}
		        	}
		        	if (!oneActive && fileSystemImportButton.getEnabled() == false) {
		        		msg.setText("at least one remote repository must be selected as source");
		        		scan.setEnabled( false);
		        	}
		        	else {
		        		if (artifactsToScan != null && artifactsToScan.size() > 0)
		        			scan.setEnabled( true);
		        	}
		        }
				
			}
		});
		
		// file system import 
		Composite fileSystemImport = new Composite(sourcesGroup, SWT.BORDER);
		fileSystemImport.setLayout(layout);
		fileSystemImport.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		    	  
		fileSystemImportButton = new Button( fileSystemImport, SWT.CHECK);
		fileSystemImportButton.setText("Import from a single file system directory");
		fileSystemImportButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
		fileSystemImportButton.addSelectionListener(this);
    	

    	importFile = new Text( fileSystemImport, SWT.BORDER);
    	importFile.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));

    	scanButton = new Button( fileSystemImport, SWT.NONE);
    	scanButton.setText( "..");
    	scanButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false));
    	scanButton.addSelectionListener( this);
   	    	    
    	
    	Composite fileLocalSystemImport = new Composite(sourcesGroup, SWT.BORDER);
		fileLocalSystemImport.setLayout(layout);
		fileLocalSystemImport.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		    	  
		localFileSystemImportButton = new Button( fileLocalSystemImport, SWT.CHECK);
		localFileSystemImportButton.setText("Import from a local Maven compatible file system repository");
		localFileSystemImportButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
		localFileSystemImportButton.addSelectionListener(this);
    	
    	localImportFile = new Text( fileLocalSystemImport, SWT.BORDER);
    	localImportFile.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));

    	directoryScanButton = new Button( fileLocalSystemImport, SWT.NONE);
    	directoryScanButton.setText( "..");
    	directoryScanButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false));
    	directoryScanButton.addSelectionListener( this);
    	
		//
		// targets 
		//
    	Composite targetGroup = new Composite(composite, SWT.NONE);    	
    	targetGroup.setLayout(layout);
    	targetGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
    	    
    	Label targetsLabel = new Label( targetGroup, SWT.NONE);
    	targetsLabel.setText( "targets");
    	targetsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	targetsLabel.setFont( bigFont);
    	
    	Composite targetTreeComposite = new Composite( targetGroup, SWT.NONE);
    	targetTreeComposite.setLayout(tightLayout);
    	targetTreeComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
		
		targetTree = new Tree ( targetTreeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		targetTree.setHeaderVisible( true);
		targetTree.setLayout( tightLayout);
		GridData targetGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2);
		targetGridData.minimumHeight = 100;
		targetGridData.heightHint = 100;
		targetTree.setLayoutData( targetGridData);
			
		String [] targetColumnNames = new String [] {"name", "url"};
		int [] targetColumnWeights = new int [] { 100, 200};
		
		List<TreeColumn> targetColumns = new ArrayList<TreeColumn>();		
		for (int i = 0; i < targetColumnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( targetTree, SWT.LEFT);
			treeColumn.setText( targetColumnNames[i]);
			treeColumn.setWidth( targetColumnWeights[i]);
			treeColumn.setResizable( true);
			//treeColumn.addSelectionListener(treeSortListener);
			targetColumns.add( treeColumn);
		}
		
		targetColumnResizer = new TreeColumnResizer();
		targetColumnResizer.setColumns( targetColumns);
		targetColumnResizer.setColumnWeights( targetColumnWeights);
		targetColumnResizer.setParent( targetTreeComposite);
		targetColumnResizer.setTree( targetTree);		
		targetTree.addControlListener(targetColumnResizer);
		
		setupHomeRepositoryTree();
		
		targetTree.addListener( SWT.MouseDoubleClick, new Listener() {			
			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
		        TreeItem targetItem = targetTree.getItem(point);
		        if (targetItem != null) {		        
		        	// deactivate  
		        	for (TreeItem item : targetTree.getItems()) {
		        		if (item == targetItem)
		        			continue;
		        		item.setImage( (Image)null);
		        		item.setData( KEY_ACTIVE, Boolean.FALSE);		        		
		        	}
		        	// activate selected
		        	targetItem.setImage( activeImage);
		        	targetItem.setData(KEY_ACTIVE, Boolean.TRUE);
		        	RepositorySetting setting = (RepositorySetting) targetItem.getData(KEY_REPOSITORY_SETTING);
		        	GreyfacePlugin.getInstance().getGreyfacePreferences(false).setLastSelectedTargetRepo( setting.getName());		        	
		        }				
			}
		});
				
		TreeDragger.attach(sourceTree, display, new String [] {KEY_ACTIVE, KEY_REPOSITORY_SETTING}, DragParentType.tree);
	
		Composite settingsGroup = new Composite( composite, SWT.NONE);
		settingsGroup.setLayout( layout);
		settingsGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		// scan settings 
		Composite scanSettingsGroup = new Composite( settingsGroup, SWT.NONE);		
		scanSettingsGroup.setLayout(layout);
    	scanSettingsGroup.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, true, 2, 1));
    	
    	Label settingsLabel = new Label( scanSettingsGroup, SWT.NONE);
    	settingsLabel.setText( "scan settings");
    	settingsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	settingsLabel.setFont( bigFont);
		
		skipOptional = new Button( scanSettingsGroup, SWT.CHECK);
		skipOptional.setText( GreyfacePreferenceConstants.SKIP_ARTIFACTS_MARKED_AS_OPTIONAL);
		skipOptional.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		skipOptional.setSelection( gfPreferences.getExcludeOptionals());
		skipOptional.addSelectionListener(this);
		
		skipTestScope = new Button( scanSettingsGroup, SWT.CHECK);
		skipTestScope.setText( GreyfacePreferenceConstants.SKIP_ARTIFACTS_WITH_SCOPE_TEST);
		skipTestScope.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		skipTestScope.setSelection( gfPreferences.getExcludeTest());
		skipTestScope.addSelectionListener(this);
		
		applyCompileScope  = new Button( scanSettingsGroup, SWT.CHECK);
		applyCompileScope.setText( GreyfacePreferenceConstants.ACCEPT_COMPILE_SCOPE);
		applyCompileScope.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		applyCompileScope.setSelection( gfPreferences.getApplyCompileScope());
		applyCompileScope.addSelectionListener(this);
		
		validatePoms = new Button( scanSettingsGroup, SWT.CHECK);
		validatePoms.setText( GreyfacePreferenceConstants.VALIDATE_POMS_DURING_SCAN);
		validatePoms.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		validatePoms.setSelection( gfPreferences.getValidatePoms());
		validatePoms.addSelectionListener(this);
		
		stopScanOnKnownInTarget = new Button( scanSettingsGroup, SWT.CHECK);
		stopScanOnKnownInTarget.setText( GreyfacePreferenceConstants.DO_NOT_SCAN_ARTIFACTS_THAT_EXIST_IN_TARGET_REPOSITORY);
		stopScanOnKnownInTarget.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		stopScanOnKnownInTarget.setSelection( gfPreferences.getExcludeExisting());
		stopScanOnKnownInTarget.addSelectionListener(this);
		
		// scan settings 
		Composite uploadSettingsGroup = new Composite( settingsGroup, SWT.NONE);		
		uploadSettingsGroup.setLayout(layout);
		uploadSettingsGroup.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, true, 2, 1));
		    	
		
		Label targetLabel = new Label( uploadSettingsGroup, SWT.NONE);
		targetLabel.setText( "upload settings");
		targetLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		targetLabel.setFont( bigFont);
		
		overwriteInTarget = new Button( uploadSettingsGroup, SWT.CHECK);
		overwriteInTarget.setText( GreyfacePreferenceConstants.OVERWRITE_EXISTING_ARTIFACT_IN_TARGET_REPOSITORY);
		overwriteInTarget.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		overwriteInTarget.setSelection( gfPreferences.getOverwrite());
		overwriteInTarget.addSelectionListener(this);
		
		repairInTarget = new Button( uploadSettingsGroup, SWT.CHECK);
		repairInTarget.setText( GreyfacePreferenceConstants.REPAIR_PARTS_OF_EXISTING_ARTIFACT_IN_TARGET_REPOSITORY);
		repairInTarget.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		repairInTarget.setSelection( gfPreferences.getRepair());
		repairInTarget.addSelectionListener(this);
		


		// trigger.. 
		
		msg = new Text(composite, SWT.READ_ONLY);
		msg.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		scan = new Button( composite, SWT.PUSH);
		scan.setText( "start scan");
		scan.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		scan.addSelectionListener( this);
		
		abortScan = new Button( composite, SWT.PUSH);
		abortScan.setText( "cancel");
		abortScan.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1));
		abortScan.addSelectionListener( this);
		
		
		// setup current state
		abortScan.setEnabled(false);
		scan.setEnabled(false);
		
		switchLocalSingleImportMode( false);
		switchSingleImportMode( false);
		
		return composite;
	}

	private void setupHomeRepositoryTree() {
		Set<RepositorySetting> targetSettings = GreyfacePlugin.getInstance().getHomeRepositorySettings();
		String lastSelectedTarget = GreyfacePlugin.getInstance().getGreyfacePreferences(false).getLastSelectedTargetRepo();		
		if (targetSettings != null) {
			for (RepositorySetting setting : targetSettings) {
				TreeItem item = new TreeItem( targetTree, SWT.NONE);
				List<String> texts = new ArrayList<String>();
				texts.add( setting.getName());
				texts.add( setting.getUrl());			
				item.setText( texts.toArray( new String[0]));
				if (setting.getName().equalsIgnoreCase( lastSelectedTarget)) {
					item.setData(KEY_ACTIVE, Boolean.TRUE);
					item.setImage(activeImage);
				}
				else {
					item.setData(KEY_ACTIVE, Boolean.FALSE);
				}
								
				item.setData(KEY_REPOSITORY_SETTING, setting);
			}		
		} 
		else {
			// inject default? 
			RepositorySetting target = RepositorySetting.T.create();
			target.setName("auto injected braintribe home");
			target.setUrl(ARTIFACTORY_BRAINTRIBE);
			target.setUser(ARTIFACTORY_BRAINTRIBE_USER);
			target.setPassword(ARTIFACTORY_BRAINTRIBE_PWD);
			
			TreeItem item = new TreeItem( targetTree, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( target.getName());
			texts.add( target.getUrl());			
			item.setText( texts.toArray( new String[0]));
			item.setImage(activeImage);
			item.setData(KEY_ACTIVE, Boolean.TRUE);
			item.setData(KEY_REPOSITORY_SETTING, target);
			GreyfacePlugin.getInstance().getGreyfacePreferences(false).setLastSelectedTargetRepo("auto injected braintribe home");
			
		}
		
		
	}
	
	
	public void setArtifactExpression( String expression) {
		artifactExpressionText.setText(expression);
	}
	

	@Override
	public void adjustSize() {
		sourceColumnResizer.resize();
		targetColumnResizer.resize();
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		// toggle sources
		if (e.widget == fileSystemImportButton) {
			if (fileSystemImportButton.getSelection()) {			
				switchLocalSingleImportMode( true);				
				switchSingleImportMode(false);
				localFileSystemImportButton.setSelection( false);
			}
			else {
				switchLocalSingleImportMode( false);
			}
			return;
		}
		// toggle sources
		if (e.widget == localFileSystemImportButton) {
			if (localFileSystemImportButton.getSelection()) {			
				switchLocalSingleImportMode( false);				
				switchSingleImportMode( true);
				fileSystemImportButton.setSelection( false);
				
				if (localImportFile.getText() == null || localImportFile.getText().length() == 0) {
					String root = null;
					try {
						root = GreyfaceScope.getScope().getSettingsReader().getLocalRepository(null);
					} catch (RepresentationException e1) {						
					}
					if (root == null) {
						root = System.getenv( "M2_REPO");
					}	
					if (root != null)
						localImportFile.setText( root);
				}
			}
			else {
				switchSingleImportMode( false);
			}
			return;
		}
		
		
					
		// run local file system scan 
		if (e.widget == scanButton) {
			Tuple tuple = localFileSystemScanner.scanLocalFileSystem(display);
			if (tuple == null) {
				artifactExpressionText.setText("");
				importFile.setText("");
				scan.setEnabled(false);
			} else {
				importFile.setText( tuple.directory);	
				if (tuple.condensedArtifactName != null) {
					artifactExpressionText.setText( tuple.condensedArtifactName);
					scan.setEnabled(true);
				}			
			}
		}
		
		// run maven compatible local file system scan 
		if (e.widget == directoryScanButton) {
			// 
			Tuple tuple = localFileSystemScanner.scanForLocalMavenCompatibleRoot(display);
			if (tuple == null) {
				localFileSystemImportButton.setText("");
				scan.setEnabled(false);
			} else {
				localImportFile.setText( tuple.directory);		
			}
		}
		
		// broadcast any changes to the overwrite switch 
		if (e.widget == overwriteInTarget) {
			broadCastSelectionContext();
		}
		// broadcast any changes to the repair switch 
		if (e.widget == repairInTarget) {
			broadCastSelectionContext();
		}
		// actually run scan 
		if (e.widget == scan) {			
			ScanContext scanContext = ScanContext.T.create();
			scanContext.setCondensedNames( artifactsToScan);
			
			scanContext.setTargetRepository( extractActiveRepositories(targetTree).get(0));
			scanContext.setStopScanIfKnownInTarget(stopScanOnKnownInTarget.getSelection());
			scanContext.setOverwriteInTarget( overwriteInTarget.getSelection());
			scanContext.setSkipOptional( skipOptional.getSelection());
			scanContext.setSkipTestScope( skipTestScope.getSelection());
			scanContext.setApplyCompileScope( applyCompileScope.getSelection());
			scanContext.setValidatePoms( validatePoms.getSelection());
			scanContext.setContextId( UUID.randomUUID().toString());
			
			List<RepositorySetting> sources = extractActiveRepositories(sourceTree);
			// see if we must add a single directory to the sources 
			if (fileSystemImportButton.getSelection()) {			
				String name = importFile.getText();
				if (name == null || name.length() == 0) {
					msg.setText("no file selected");
					return;
				}			
				File file = new File( name);
				if (!file.exists() || !file.isDirectory()) {
					msg.setText( "selected file [" + name + "] is not a valid file");
				}
				
				RepositorySetting localRepositorySetting = RepositorySetting.T.create();
				localRepositorySetting.setName("local");
				localRepositorySetting.setUrl( file.getAbsolutePath());
				localRepositorySetting.setPhonyLocal(true);
				sources.add(0, localRepositorySetting);			
			}
			// see if we must a maven compatible root repository
			if (localFileSystemImportButton.getSelection()) {
				String name = localImportFile.getText();
				if (name == null || name.length() == 0) {
					msg.setText("no file selected");
					return;
				}			
				File file = new File( name);
				if (!file.exists() || !file.isDirectory()) {
					msg.setText( "selected file [" + name + "] is not a valid file");
				}
				
				RepositorySetting localRepositorySetting = RepositorySetting.T.create();
				localRepositorySetting.setName("local");
				localRepositorySetting.setUrl( file.getAbsolutePath());
				localRepositorySetting.setPhonyLocal(true);
				localRepositorySetting.setMavenCompatible(true);
				sources.add(0, localRepositorySetting);			
			}
			scanContext.setSourceRepositories( sources);
			abortScan.setEnabled(true);
			
			// prime selection by broadcasting the current context
			broadCastSelectionContext();
			scanner.scan(null, scanContext);
		}
		
		if (e.widget == abortScan) {
			if (processControl != null)
				processControl.cancelCurrentProcess(ProcessId.scan);
		}
		
	}
	
	public void broadCastSelectionContext(){
		if (overwriteInTarget.getSelection()) {
			selectionContext.setOverwriteExistingInTarget( true);
			selectionContext.setRepairExistingInTarget(false);
			repairInTarget.setEnabled(false);			
		}
		else {
			selectionContext.setOverwriteExistingInTarget( false);
			selectionContext.setRepairExistingInTarget( repairInTarget.getSelection());
			repairInTarget.setEnabled( true);
		}		
		selectionContextListener.acknowledgeSelectionContext( getId(), selectionContext);
	}
	
	private List<RepositorySetting> extractActiveRepositories(Tree tree) {
		List<RepositorySetting> result = new ArrayList<RepositorySetting>();
		for (TreeItem item : tree.getItems()) {
    		if (Boolean.TRUE.equals( item.getData( KEY_ACTIVE))) {
    			Object data = item.getData(KEY_REPOSITORY_SETTING);
    			if (data instanceof RepositorySetting) {
    				result.add( (RepositorySetting) data);
    			} 
    			else {
    				RepositorySetting setting = CommonConversions.repositorySettingFrom( (RemoteRepository) data);
    				result.add( setting);
    			}
    			
    		}
    	}
		return result;
	}
	
	private void switchLocalSingleImportMode( boolean mode) {			
		scanButton.setEnabled( mode);
		importFile.setEnabled( mode);
	}
	private void switchSingleImportMode( boolean mode) {			
		directoryScanButton.setEnabled( mode);
		localImportFile.setEnabled( mode);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void modifyText(ModifyEvent e) {		
		String contents = artifactExpressionText.getText();		  
		String valueToProcess = contents.trim();
		// simple test - no condensed name, no maven declaration, not file system reference, no XML snippet, no joy
		if (
				valueToProcess.length() == 0 ||
				(
					!valueToProcess.contains( ":") &&
					!valueToProcess.contains( "/") &&
					!valueToProcess.contains( "\\") &&
					!valueToProcess.contains("<")
				)
			) {
			msg.setText("Nothing valid to scan found in text");
			scan.setEnabled(false);
			return;			
		}
		
		artifactsToScan = new ArrayList<String>();
		scan.setEnabled( false);

		// xml snippet ? 
		if (valueToProcess.contains("<")) {
		
			Document doc;
			try {
				doc = DomParser.load().from(valueToProcess);
			} catch (DomParserException e1) {
				valueToProcess = "<container>" + valueToProcess + "</container>";		
				try {
					doc = DomParser.load().from(valueToProcess);
				} catch (DomParserException e2) {
					msg.setText("Nothing valid to scan found in text as XML is not well formed");
					return;
				}
			}
			Element documentElement = doc.getDocumentElement();
			
			// 
			// single definition, i.e. a single project, dependency 
			// 
			String tagName = documentElement.getTagName();
			if (tagName.equalsIgnoreCase( "project") || tagName.equalsIgnoreCase( "dependency") || tagName.equalsIgnoreCase( "container")) {
				String expression = extractElement( documentElement);
				if (expression != null) {
		
					artifactsToScan.add( expression);
					msg.setText("extracted : " + artifactsToScan + " from expression");
					scan.setEnabled(true);
					return;
				}
			}			
			// try to extract artifacts  from a multiple definition									
			Iterator<Element> iterator = new FilteringElementIterator( documentElement, new TagNameFilter("dependency").or(	new TagNameFilter( "project")));
			
			while (iterator.hasNext()) {
				Element dependency = iterator.next();
				
				Element projectParent = DomUtils.getAncestor(dependency, "project");			
				// we do not want any child from a dependency or a dependency management parent
				// as this means this is a pom we're extracting, and in this case, the project's setting is fine enought.
				if (projectParent != null) 
					continue;
				String expression = extractElement(dependency);
				if (expression == null)
					continue;
				artifactsToScan.add(expression);
			}					
		}
		else {
			// simple condensed names.. 
			String [] values = valueToProcess.split( "\n");
			for (String value : values) {
				
					value = value.trim();					
					if (value.contains("#")) {
						try {
							// could it be a condensed name?
							// <group>:<artifact>#<version>
							NameParser.parseCondensedDependencyName(value);
						} catch (Exception e1) {
							continue;
						}
					}
					else if (value.contains(":")){
						// could it be a maven style token?
						// <group>:<artifact>:<packaging>:<version>
						String [] tokens = value.split(":");
						if (tokens.length < 4)
							continue;
						
					}
					else {
						value = value.replace( '\\', '/');
						String [] tokens1 = value.split( "/");
						if (tokens1.length < 2) {						
								continue;
						}
						
					}
					artifactsToScan.add( value);								
			}										
		}
		
		if (artifactsToScan.size() > 0) {
			scan.setEnabled(true);			
			msg.setText("extracted : " + artifactsToScan + " from expression");
		}
		else {	
			msg.setText("nothing extracted from expression");			
		}		
	}
	
	/**
	 * extract the relevant data from an element (representing artifact or dependency) 	
	 */
	private String extractElement( Element element) {
		String groupId = DomUtils.getElementValueByPath(element, "groupId", false);
		String artifactId = DomUtils.getElementValueByPath(element, "artifactId", false);
		String version = DomUtils.getElementValueByPath(element, "version", false);
		if (groupId == null || artifactId == null || version == null)
			return null;
		String expression = groupId + ":" + artifactId + "#" + version;
		return expression;
	}

	@Override
	public void acknowledgeStartScan() {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {	
				scan.setEnabled(false);
				abortScan.setEnabled(true);
			}
		});
		
	}

	@Override
	public void acknowledgeStopScan() {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {	
				scan.setEnabled( true);
				abortScan.setEnabled( false);
			}
		});
	}
	
	

	@Override
	public void acknowledgeSelectionContext(String id, final SelectionContext context) {
		if (id.equalsIgnoreCase(TAB_ID))
			return;
		// actually process it
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {			
				overwriteInTarget.setSelection(context.getOverwriteExistingInTarget());
				repairInTarget.setSelection(context.getRepairExistingInTarget());
				repairInTarget.setEnabled( !context.getOverwriteExistingInTarget());
			}
		});		
	}
	
	

	@Override
	public void acknowledgeOverrideChange() {
		targetTree.removeAll();
		setupHomeRepositoryTree();
	}

	@Override
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( RepositorySetting target, Solution artifact, Set<Artifact> parent) {}

	@Override
	public void acknowledgeScannedArtifact(RepositorySetting setting, Solution artifact, Set<Artifact> parents, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedParentArtifact(RepositorySetting setting, Solution artifact, Artifact child, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedRootArtifact(RepositorySetting setting, Solution artifact, boolean presentInTarget) {}

	@Override
	public void acknowledgeUnresolvedArtifact(List<RepositorySetting> sources, Dependency dependency, Collection<Artifact> requestors) {}
	
	
}
