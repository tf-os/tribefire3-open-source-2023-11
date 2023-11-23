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
package com.braintribe.devrock.artifactcontainer.ui.tb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
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

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.TbRunnerPreferencesCodec;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.tb.TbRunnerPreferences;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;

public class TbWizardDialog extends Dialog implements SelectionListener, Listener, ArtifactSelectionListener {
	private static final String AC_TB_WIZARD = "TB runner wizard";
	

	private static final int HEIGHT = 25;
	private static final int WIDTH = 115;
	private static final String TARGET_ARTIFACT_S = "Target artifact [%s]";
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;

	//private static final String paddingString = "                       ";
	//private static final int padding = 12;
	private Shell parentShell;
	private Font bigFont;	
	private List<Artifact> selectedTargetArtifacts;
	
	
	private Button loadProjectAfterRun;
	
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
	
	
	public TbWizardDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		setShellStyle(SHELL_STYLE);
		
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( TbWizardDialog.class, "add.gif");
		addItemImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( TbWizardDialog.class, "remove.gif");
		removeItemImage = imageDescriptor.createImage();
		
		
	}
	
	@Configurable
	public void setSelectedTargetArtifacts(List<Artifact> selectedTargetArtifacts) {
		this.selectedTargetArtifacts = selectedTargetArtifacts;
	}
	
	@Override
	protected Point getInitialSize() {	
		PixelConverter pc = new PixelConverter( parentShell);
		return new Point( pc.convertWidthInCharsToPixels(WIDTH), pc.convertHeightInCharsToPixels(HEIGHT));
	}

	@Override
	public boolean close() {
		bigFont.dispose();
				
		addItemImage.dispose();		
		removeItemImage.dispose();
		
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText( AC_TB_WIZARD);
	}
/*
	private String pad( String tag) {
		int l = tag.length();
		if (l < padding) {
			return tag + paddingString.substring(0, padding - l);
		}
		return tag;
	}
*/
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
    					TbWizardArtifactTab tab = itemToTabMap.get(tabItem);
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
		label.setText( "mode");
		label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, true, 4, 1));
		label.setFont(bigFont);
	
    	
    	// mode selection
    	Composite modeComposite = new Composite( composite, SWT.NONE);    	
    	GridLayout modeCompositeLayout= new GridLayout();
    	modeCompositeLayout.numColumns = 4;
    	modeComposite.setLayout( modeCompositeLayout);       
    	modeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,1));

    	
    	transitive = new Button( modeComposite, SWT.CHECK);
    	transitive.setText( "transitive");
    	transitive.setSelection( ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getTbRunnerPreferences().getTransitiveBuild());
    	transitive.addSelectionListener(this);
		
        group = new Button( modeComposite, SWT.RADIO);
        group.setText( "group-wide");
        group.setSelection( true);
            	
    	codebase = new Button( modeComposite, SWT.RADIO);
    	codebase.setText( "codebase-wide");
    	codebase.setSelection(false);
    	
	 
        skipPositionEditor = new StringEditor();
        Composite skipPositionComposite = skipPositionEditor.createControl( modeComposite,  "Skip at position:");
        skipPositionComposite.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, true, false, 1, 1));        
        skipPositionEditor.setEnabled( true);
      
                        
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
		TbWizardArtifactTab tab = new TbWizardArtifactTab( parentShell, artifact);
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
		if (event.widget == transitive) {
			if (transitive.getSelection()) {
				group.setEnabled(true);
				codebase.setEnabled( true);
			}
			else {
				group.setEnabled( false);
				codebase.setEnabled( false);
			}
		}
	}
		
	
	private List<Artifact> getSelectedArtifacts() {
		List<Artifact> artifacts = new ArrayList<>();
		for (int i = 0; i < maxTabs; i++) {
			CTabItem item = indexToItemMap.get( i);
			TbWizardArtifactTab tab = itemToTabMap.get( item);
			
			Artifact selectedArtifact = tab.getSelectedArtifact();
			if (selectedArtifact != null) {
				artifacts.add(selectedArtifact);
			}
		}				
		return artifacts;
	}
		
	
	@Override
	protected void okPressed() {
		final List<Artifact> selectedArtifacts = getSelectedArtifacts();
		if (selectedArtifacts == null) {
			MessageDialog.openError(getShell(), AC_TB_WIZARD, "No valid artifact selected");
			return;
		}
			
		final boolean loadAfterRun = loadProjectAfterRun.getSelection();
		
		//
		// turn selected artifacts in source artifacts (containing project information)
		//
		
		// build the query 
		StringBuilder queryExpressionBuilder = new StringBuilder();		
		for (Artifact artifact : selectedArtifacts) {
			if (queryExpressionBuilder.length() > 0) {
				queryExpressionBuilder.append( "|");
			}
			String artifactExpression = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + VersionProcessor.toString(artifact.getVersion()); 
			queryExpressionBuilder.append( artifactExpression);
		}	
		
		// run the query
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		List<SourceArtifact> sourceArtifacts = plugin.getQuickImportScanController().runSourceArtifactQuery( queryExpressionBuilder.toString());
		if (sourceArtifacts == null || sourceArtifacts.size() == 0) {
			MessageDialog.openError(getShell(), AC_TB_WIZARD, "No valid artifact found");
			return;
		}
		// process the result 
		String sourceRepositoryRoot = null;
		String groupRepositoryRoot = null;
		boolean enforceCodebaseWide = false;		
		
		for (SourceArtifact sourceArtifact : sourceArtifacts) {
			String path = new File(sourceArtifact.getPath()).getParent();
			if (groupRepositoryRoot == null) {
				groupRepositoryRoot = path;
			}
			else {
				if (!groupRepositoryRoot.equalsIgnoreCase( path)) {
					enforceCodebaseWide = true;
				}
			}
			String repoUrl = sourceArtifact.getRepository().getRepoUrl();
			if (sourceRepositoryRoot == null) {
				sourceRepositoryRoot = repoUrl;
			}
			else {
				if (!repoUrl.equalsIgnoreCase(sourceRepositoryRoot)) {
					MessageDialog.openError(getShell(), AC_TB_WIZARD, "The artifacts selected do not have a common source repository origin");
					return;
				}
			}
		}
		
		boolean codebaseWide = codebase.getSelection() || enforceCodebaseWide;
		boolean useBrackets = !transitive.getSelection();
		
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
		
		
		String skipExpression = skipPositionEditor.getSelection();
		
		URL repoUrl;
		try {
			repoUrl = new URL(sourceArtifacts.get(0).getRepository().getRepoUrl());
		} catch (MalformedURLException e1) {
			MessageDialog.openError(getShell(), AC_TB_WIZARD, "invalid source repository encountered");
			return;
		}
		File workingCopy = new File(repoUrl.getFile());
				
		final File workingDirectory = codebaseWide ? new File( workingCopy, sourceRepositoryRoot) : new File( workingCopy, groupRepositoryRoot);  		
		
		Job job = new Job(AC_TB_WIZARD) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MessageMonitorBridge bridge = new MessageMonitorBridge( AC_TB_WIZARD, monitor);
		    	
				AntRunPerCommandLine antRun = new AntRunPerCommandLine();
				try {
					antRun.run( expressions, skipExpression, workingDirectory, bridge);					
					if (loadAfterRun) {
						List<ProjectImporterTuple> tuples = new ArrayList<ProjectImporterTuple>();
						for (Artifact artifact : selectedArtifacts) {
							if (ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(artifact) == null) {
								File projectFile = new File( NameParser.buildPartialPath( artifact, artifact.getVersion(), workingCopy.getAbsolutePath()) + File.separator +  ".project");
								if (projectFile.exists()) {
									ProjectImporterTuple importerTuple = new ProjectImporterTuple(projectFile.getAbsolutePath(), artifact);
									tuples.add(importerTuple);
								}
							}
						}
						if (tuples.size() > 0) {
							ProjectImporter.importProjects( false, new TargetProviderImpl(), ArtifactContainerPlugin.getWorkspaceProjectRegistry(), tuples.toArray( new ProjectImporterTuple[0]));
						}
					}									
					return Status.OK_STATUS;
				} catch (Exception e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot run targets", e);
					plugin.log(status);		
					return Status.CANCEL_STATUS;
				} 
			}
		};
		job.schedule();
		
		TbRunnerPreferences tbRunnerPreferences = plugin.getArtifactContainerPreferences(false).getTbRunnerPreferences();
		tbRunnerPreferences.setTransitiveBuild( transitive.getSelection());
		new TbRunnerPreferencesCodec( plugin.getPreferenceStore()).decode(tbRunnerPreferences);
		
		super.okPressed();
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
