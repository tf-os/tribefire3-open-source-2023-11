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
package com.braintribe.devrock.preferences.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.devrock.commons.tableviewer.CommonTableColumnData;
import com.braintribe.devrock.commons.tableviewer.CommonTableViewer;
import com.braintribe.devrock.preferences.contributer.PreferencesContributer;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerImplementation;
import com.braintribe.devrock.preferences.contributer.PreferencesContributionDeclaration;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;



public class PreferencesControlPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener, ModifyListener {
	
	private static final String DEVROCK_PREFERENCES = "Braintribe Devrock Preferences Management";
	private static final String METADATA = ".metadata";
	protected Font bigFont;
	private Image banner;

	
	private Button localSource;
	private Text wsSource;
	private Button localTarget;
	private Text wsTarget;
	private Button scanSource;
	private Button scanTarget;
	private Button actionButton;
	private List<ContributerTuple> contributerTuples;
	private Button structuredSourceButton;
	private Button structuredTargetButton;
	
	public PreferencesControlPage() {
		setPreferenceStore(VirtualEnvironmentPlugin.getInstance().getPreferenceStore());
		setDescription(DEVROCK_PREFERENCES);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( PreferencesControlPage.class, "bt.banner.png");
		banner = imageDescriptor.createImage();		
				
	}
	
	
	@Override
	public void dispose() {
		bigFont.dispose();
		banner.dispose();
		super.dispose();
	}



	@Override
	public void init(IWorkbench workbench) {		
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
		//
		Label imageLabel = new Label(composite, SWT.NONE);
		imageLabel.setLayoutData(new GridData( SWT.CENTER, SWT.TOP, false, false, 4, 1));
		imageLabel.setImage(banner);
		
		Composite pluginGroup = new Composite( composite, SWT.NONE);
		pluginGroup.setLayout( layout);
		pluginGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));		
		
		Label pluginLabel = new Label( pluginGroup, SWT.NONE);
		pluginLabel.setText( "Loaded Devrock plugins");
		pluginLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		pluginLabel.setFont(bigFont);
		
		Composite tableComposite = new Composite( pluginGroup, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;	
		tableComposite.setLayout(gridLayout);
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		
		CommonTableViewer tableViewer = new CommonTableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		
		CommonTableColumnData [] tableColumnData = new CommonTableColumnData[2];
		tableColumnData[0] = new CommonTableColumnData("plugin", 100, 200, "Devrock's plugin identification", new PluginColumnLabelProvider(), new PluginColumnEditingSupport(tableViewer));
		tableColumnData[1] = new CommonTableColumnData("active", 30, 50, "Can reflect changes in preferences runtime", new ActivePluginLabelProvider());
				
		tableViewer.setup( tableColumnData);
		    	
    	contributerTuples = new ArrayList<ContributerTuple>();
    	VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		Set<PreferencesContributionDeclaration> preferenceContributers = virtualEnvironmentPlugin.getPreferenceContributers();
    	
    	for (PreferencesContributionDeclaration contributer : preferenceContributers) {
    		ContributerTuple contributerTuple = new ContributerTuple(contributer);
    		for (PreferencesContributerImplementation activeContributer : virtualEnvironmentPlugin.getActivePreferenceContributers()) {
    			if (activeContributer.getName().equalsIgnoreCase( contributer.getName())) {
    				contributerTuple.setContributerImplementation(activeContributer);
    				break;
    			}
    		}
			contributerTuples.add( contributerTuple);    		
    	}
    	ContributerContentProvider contentProvider = new ContributerContentProvider();    
    	contentProvider.setTuples(contributerTuples);
    	tableViewer.setContentProvider(contentProvider);
    	tableViewer.setInput( contributerTuples);
    	
    	
    	Composite wsGroup = new Composite( composite, SWT.NONE);
    	wsGroup.setLayout( layout);
		wsGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
								
 		//
 		// source workspace
 		//
 		Composite sourceWsGroup = new Composite( wsGroup, SWT.NONE);
		sourceWsGroup.setLayout( layout);
		sourceWsGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
				
		
		Label sourceWsLabel = new Label( sourceWsGroup, SWT.NONE);
		sourceWsLabel.setText( "Source");
		sourceWsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		sourceWsLabel.setFont(bigFont);
 			
		localSource = new Button( sourceWsGroup, SWT.CHECK);
		localSource.setText("current");
		localSource.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		localSource.addSelectionListener( this);
		localSource.setToolTipText("If activated, the source location is the currently active workspace");
		
		wsSource = new Text( sourceWsGroup, SWT.NONE);
		wsSource.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));		
		wsSource.addModifyListener( this);
		wsSource.setToolTipText("Path the the source workspace or directory");
		
		scanSource = new Button( sourceWsGroup, SWT.NONE);
		scanSource.setText("..");
		scanSource.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		scanSource.addSelectionListener(this);
		scanSource.setToolTipText("Scan for the source workspace or directory");
		
		//
		// target workspace
		//
		Composite targetWsGroup = new Composite( wsGroup, SWT.NONE);
		targetWsGroup.setLayout( layout);
		targetWsGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));			
		
		Label targetWsLabel = new Label( targetWsGroup, SWT.NONE);
		targetWsLabel.setText( "Target");
		targetWsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		targetWsLabel.setFont(bigFont);
		
	

		localTarget = new Button( targetWsGroup, SWT.CHECK);
		localTarget.setText("current");
		localTarget.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		localTarget.addSelectionListener( this);
		localTarget.setToolTipText("If activated, the target location is the currently active workspace");
		
		wsTarget = new Text( targetWsGroup, SWT.NONE);
		wsTarget.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));
		wsTarget.addModifyListener( this);
		wsTarget.setToolTipText("The path to the target workspace or directory");
		
		scanTarget = new Button( targetWsGroup, SWT.NONE);
		scanTarget.setText("..");
		scanTarget.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		scanTarget.addSelectionListener(this);
		scanTarget.setToolTipText("Scan for the target workspace or directory");
	
		Composite actionGroup = new Composite( composite, SWT.NONE);
		actionGroup.setLayout( layout);
		actionGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		structuredSourceButton = new Button( actionGroup, SWT.CHECK);
		structuredSourceButton.setText("Source is workspace");
		structuredSourceButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		structuredSourceButton.setSelection( true);
		structuredSourceButton.setToolTipText("If set, a workspace structure is assumed. If not set, the files are assumed to be in a single directory");
		structuredSourceButton.addSelectionListener(this);
		
		structuredTargetButton = new Button( actionGroup, SWT.CHECK);
		structuredTargetButton.setText("target is workspace");
		structuredTargetButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		structuredTargetButton.setSelection( true);
		structuredTargetButton.setToolTipText("If set, a workspace structure is assumed. If not set, the files are assumed to be in a single directory");
		structuredTargetButton.addSelectionListener( this);
				
		actionButton = new Button( actionGroup, SWT.NONE);
		actionButton.setText("transfer preferences from source to target");
		actionButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		actionButton.addSelectionListener( this);	
		actionButton.setToolTipText("If disabled, no sufficiant data has been specified");
		// 
		// initial settings 
		setInitialState();
		
	
		
    	composite.pack();
    	tableViewer.refresh();
		return composite;
	}



	private void setInitialState() {
		
		// if none of the plugins have local data, then it's probably an empty workspace,
		// so set it to import
		// otherwise 
		// set it to export
		boolean nothingFound = true;
		for (PreferencesContributionDeclaration contributer : VirtualEnvironmentPlugin.getInstance().getPreferenceContributers()) {
			File preferencesFile = new File(contributer.getFullFilePath());
			if (preferencesFile.exists()) {
				nothingFound = false;
				break;
			}
		}
		if (nothingFound) {
			// import :

			// local is target, so no local source 
			localSource.setSelection( false);
			wsSource.setEnabled(true);
			scanSource.setEnabled( true);
			
			
			// target 
			localTarget.setSelection(true);
			wsTarget.setEnabled( false);
			wsTarget.setText( getCurrentWorkspace());
			scanTarget.setEnabled(false);
			structuredTargetButton.setSelection(true);
			structuredTargetButton.setEnabled(false);
			
		}
		else {		
			// export 
			localSource.setSelection( true);
			wsSource.setEnabled( false);
			scanSource.setEnabled( false);
			wsSource.setText( getCurrentWorkspace());
			structuredSourceButton.setSelection(true);
			structuredSourceButton.setEnabled(false);
			
			localTarget.setSelection(false);		
			wsTarget.setEnabled( true);
			scanTarget.setEnabled( true);			
			
		}
		
	}


	@Override
	public boolean performOk() {
		performApply();	
		return super.performOk();
	}

	

	@Override
	protected void performApply() {
	}


	@Override
	public boolean performCancel() {
		return super.performCancel();
	}



	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {			
	}



	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == localSource) {			
			if (localSource.getSelection()) {
				wsTarget.setEnabled(true);
				structuredTargetButton.setEnabled(true);
				scanTarget.setEnabled(true);				
				localTarget.setSelection( false);
				
				structuredSourceButton.setSelection(true);
				structuredSourceButton.setEnabled(false);
				
				scanSource.setEnabled(false);
				wsSource.setEnabled( false);
				wsSource.setText(getCurrentWorkspace());
			}
			else {
				wsSource.setEnabled( true);
				scanSource.setEnabled(true);
				structuredSourceButton.setEnabled(true);
			}			
			switchActionState();
			return;
		}
		if (event.widget == scanSource) {
			DirectoryDialog dialog = new DirectoryDialog( getShell());
			dialog.setFilterPath(getWorkspacesDirectory());
			dialog.setMessage("Select source workspace");
			String selected = dialog.open();
			if (selected != null) {
				String current = getCurrentWorkspace();
				if (!selected.equalsIgnoreCase( current)) { 
					wsSource.setText(selected);
					setMessage(null);
				}
				else {
					setMessage("cannot select current workspace");
				}
			}			
			return;
		}
		if (event.widget == localTarget) {
			if (localTarget.getSelection()) {
				wsSource.setEnabled(true);
				scanSource.setEnabled(true);
				localSource.setSelection( false);				
				structuredSourceButton.setEnabled(true);
				wsTarget.setEnabled( false);
				wsTarget.setText(getCurrentWorkspace());
				scanTarget.setEnabled(false);
				structuredTargetButton.setSelection(true);
				structuredTargetButton.setEnabled(false);
			}
			else {
				wsTarget.setEnabled( true);
				scanTarget.setEnabled(true);
				structuredTargetButton.setEnabled(true);
			}
			switchActionState();
			return;
		}
		if (event.widget == scanTarget) {
			DirectoryDialog dialog = new DirectoryDialog( getShell());
			dialog.setMessage("Select target workspace");
			dialog.setFilterPath(getWorkspacesDirectory());			
			String selected = dialog.open();
			if (selected != null) {
				if (!selected.equalsIgnoreCase(getCurrentWorkspace())) {
					wsTarget.setText(selected);
					setMessage(null);
				}
				else {
					setMessage("cannot select current workspace");
				}
			}
		}
		if (event.widget == actionButton) {
			// select 
			transfer();
		}
		if (event.widget == structuredSourceButton || event.widget == structuredTargetButton) {
			switchActionState();
		}
	}
	
	private String sanitizeFilePath( String path) {
		return path.replace("\\", "/");
	}
	
	private String getCurrentWorkspace() {
		return sanitizeFilePath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
	}
	
	private String getWorkspacesDirectory( ) {
		File file = new File(getCurrentWorkspace());
		return sanitizeFilePath( file.getParentFile().getAbsolutePath());
	}

	private void switchButtons( boolean valid, String msg) {
		if (valid) {
			actionButton.setEnabled(true);
		}
		else {
			actionButton.setEnabled(false);
		}
		setMessage( msg);
	}
	
	/**
	 * logic is : 
	 * action is enabled if 
	 * target != source && target !empty && source !empty
	 * if target structured -> target must exist. 
	 * 
	 */
	private void switchActionState() {
		
		String target = wsTarget.getText();
		String source = wsSource.getText();
		
		if (target.length() == 0) {
			switchButtons(false, "no value specified for target");
			return;
		}
		
		if (source.length() == 0) {
			switchButtons(false, "no value specified for source");
			return;
		}
				
		if (sanitizeFilePath(target).equalsIgnoreCase(sanitizeFilePath(source))) {
			switchButtons(false, "target is same as source");
			return;
		}
		
	
		File sourcefile = new File( source);
		if (!sourcefile.exists()) {
			switchButtons(false, "source workspace [" + source + "] doesn't exist");
			return;
		}
		if (structuredSourceButton.getSelection()) {
			File workspaceIdentificationFile = new File( sourcefile, METADATA);
			if (!workspaceIdentificationFile.exists()) {
				switchButtons(false, "source [" + source + "] is not a workspace");
				return;
			}
		}
					
		if (structuredTargetButton.getSelection()) {
			File file = new File( target);
			if (!file.exists()) {
				switchButtons(false, "target workspace [" + target + "] doesn't exist");
				return;
			}
			File workspaceIdentificationFile = new File( file, METADATA);
			if (!workspaceIdentificationFile.exists()) {
				switchButtons(false, "target [" + target + "] is not a workspace");
				return;
			}
		}
			
		
		switchButtons(true, "");
		
	}
	

	@Override
	public void modifyText(ModifyEvent event) {		
		switchActionState();
	}

	private void transfer() {
		String source;
		if (localSource.getSelection()) {
			source = sanitizeFilePath( getCurrentWorkspace());
		}
		else {
			source = sanitizeFilePath( wsSource.getText());
		}
		String target;
		if (localTarget.getSelection()) {
			target = sanitizeFilePath( getCurrentWorkspace());
		}
		else {
			target = sanitizeFilePath( wsTarget.getText());
		}
		
		boolean structuredSource = structuredSourceButton.getSelection();
		boolean structuredTarget = structuredTargetButton.getSelection();
		
		

		StringBuilder builder = new StringBuilder(); 
		ContributerTuple veTuple = null;
		for (ContributerTuple tuple : contributerTuples) {
			if (tuple.getSelected()) {
				
				PreferencesContributer contributer = tuple.getContributerDeclaration();
				if (contributer.getName().equalsIgnoreCase( VirtualEnvironmentPlugin.PLUGIN_ID)) {
					veTuple = tuple;
					continue;
				}
				if (processContributerTuple( tuple, source, target, structuredSource, structuredTarget)) {
					if (builder.length() > 0) {
						builder.append("\n");
					}
					builder.append(contributer.getName());
				}
			}
		}
		// 
		if (veTuple != null) {
			if (processContributerTuple( veTuple, source, target, structuredSource, structuredTarget)) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(veTuple.getContributerDeclaration().getName());
			}
		}
		//
		
		MessageDialog.openInformation(getShell(), DEVROCK_PREFERENCES,  "The preferences of \n\n" + builder.toString() + "\n\n have been successfully processed");
	}
	
	private boolean processContributerTuple( ContributerTuple tuple, String source, String target, boolean structuredSource, boolean structuredTarget) {
		PreferencesContributionDeclaration contributer = tuple.getContributerDeclaration();
		
		String partialSource = structuredSource ? contributer.getPartialFilePath() : File.separator + contributer.getLocalFileName();				
		File fileSource = new File( source + partialSource);
		if (fileSource.exists() == false) {
			return false;
		}
		String partialTarget = structuredTarget ? contributer.getPartialFilePath() : File.separator + contributer.getLocalFileName();
		File fileTarget = new File( target + partialTarget);
		if (fileTarget.exists()) {
			boolean overwrite = MessageDialog.openConfirm(getShell(), DEVROCK_PREFERENCES, "Preferences file of [" + contributer.getName() + "] exists. Overwrite?");
			if (!overwrite) {
				return false;
			}
		}
		else {
			fileTarget.getParentFile().mkdirs();					
		}
		FileTools.copyFile(fileSource, fileTarget);
		
		
		PreferencesContributerImplementation activeContributer = tuple.getContributerImplementation();				
		if (activeContributer != null && target.equalsIgnoreCase( sanitizeFilePath( getCurrentWorkspace()))) {
			try {
				String contents = IOTools.slurp(fileSource, "UTF-8");
				activeContributer.importContents(contents);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), DEVROCK_PREFERENCES, "cannot inject preferences into plugin [" + contributer.getName() + "] as [" + e.getMessage() + "]");
			}
		}		
		return true;
	}
				
}
