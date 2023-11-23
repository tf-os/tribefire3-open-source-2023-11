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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.MavenPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.maven.connectivity.ConnectivityChecker;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.maven.validator.MavenSettingsValidator;
import com.braintribe.devrock.artifactcontainer.validator.ArtifactContainerPluginValidatorDialog;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.plugin.commons.preferences.DirectoryEditor;
import com.braintribe.plugin.commons.preferences.FileEditor;
import com.braintribe.plugin.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;
import com.braintribe.plugin.commons.ui.tree.TreeViewerColumnResizer;


public class MavenPreferencesPage extends PreferencePage implements ModificationNotificationListener, IWorkbenchPreferencePage, HasMavenTokens, VirtualEnvironmentNotificationListener{

	private static final String OPEN_INSTALLATION_SETTINGS= "open installation settings [%s]";
	private static final String OPEN_USER_SETTINGS = "open user settings [%s]";
	private Button checkLocalButton;
	private FileEditor userSettingsFileEditor;
	private Button checkRemoteButton;
	private FileEditor installationSettingsFileEditor;
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private MavenPreferences mvPreferences = plugin.getArtifactContainerPreferences(false).getMavenPreferences();    	
	private Font bigFont;	
	protected static final String CODE_TOOLTIP = "_TOOLTIP";
	private Image dynamicUpdatePolicyImage;
	private Image undoEditImage;
	private Image connectivityImage; 
	private Image openSettings;
	private MavenSettingsReader reader;
	private DirectoryEditor localRepoPathEditor;
	private Button checkLocalRepoButton;
	private String localRepositoryExpression;
	private TreeViewer treeViewer;
	private MavenPreferencesTreeRegistry registry;
	private ClasspathResolverContract contract;
	private Button testConnectivity;
	private MavenPreferences orgMvPreferences;
	
	
	@Override
	public void init(IWorkbench workbench) {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "arrow_refresh_small.png");
		dynamicUpdatePolicyImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( getClass(), "undo_edit_gimped.png");
		undoEditImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile(getClass(), "testConnectivity.gif");
		connectivityImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( getClass(), "pom_obj.gif");
		openSettings = imageDescriptor.createImage();

		contract = MalaclypseWirings.fullClasspathResolverContract().contract();
		reader = contract.settingsReader();
		MavenPreferences mvPreferences = plugin.getArtifactContainerPreferences(false).getMavenPreferences();
		
		orgMvPreferences = (MavenPreferences) MavenPreferences.T.clone(mvPreferences, null, null);		
	}
	
	

	@Override
	public void dispose() {
		if (dynamicUpdatePolicyImage != null)
			dynamicUpdatePolicyImage.dispose();
		if (undoEditImage != null)
			undoEditImage.dispose();
		if (connectivityImage != null)
			connectivityImage.dispose();
		
		if (bigFont != null)
			bigFont.dispose();

		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		virtualEnvironmentPlugin.removeListener(installationSettingsFileEditor);
		virtualEnvironmentPlugin.removeListener(userSettingsFileEditor);
		virtualEnvironmentPlugin.removeListener(localRepoPathEditor);
		
				
		super.dispose();
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
		
		final VirtualPropertyResolver virtualPropertyResolver = plugin.getVirtualPropertyResolver();
		
		Composite mavenSettingsGroup = new Composite( composite, SWT.NONE);
		GridLayout mavenSettingsGroupLayout = new GridLayout();
		mavenSettingsGroupLayout.numColumns = 5;
		mavenSettingsGroup.setLayout( mavenSettingsGroupLayout);
		mavenSettingsGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label mavenSettingsLabel = new Label( mavenSettingsGroup, SWT.NONE);
    	mavenSettingsLabel.setText( "Currently active settings.xml");
    	mavenSettingsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 5,1));
    	mavenSettingsLabel.setFont(bigFont);
    	
    	
    	/*
    	 * user settings.xml
    	 */
    	
    	// check and open 
    	checkLocalButton = new Button( mavenSettingsGroup, SWT.NONE);
    	checkLocalButton.setImage(openSettings);
    	checkLocalButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	checkLocalButton.addSelectionListener( new SelectionAdapter() {
    		
			@Override
			public void widgetSelected(SelectionEvent e) {			
				File file = new File( virtualPropertyResolver.resolve(userSettingsFileEditor.getSelection()));
				openExternalFile(file);				
			}
						
		});
    	
    	// actual settings editor
    	userSettingsFileEditor = new FileEditor( getShell());    	
    	String initialUserSettings = mvPreferences.getUserSettingsOverride();
    	if (initialUserSettings == null) {
    		initialUserSettings = LOCAL_SETTINGS;
    	} 
    	
    	userSettingsFileEditor.setSelection( initialUserSettings);    	
		userSettingsFileEditor.setResolver( virtualPropertyResolver);
		userSettingsFileEditor.addListener( this);
    	Composite userSettingsEditorComposite = userSettingsFileEditor.createControl(mavenSettingsGroup, "Currently active user file");
    	userSettingsEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( userSettingsFileEditor);
    	
    	// restore feature 
    	Button restoreLocalDefault = new Button( mavenSettingsGroup, SWT.NONE);
    	restoreLocalDefault.setImage(undoEditImage);
    	restoreLocalDefault.setToolTipText("Restore to default [" + LOCAL_SETTINGS + "]");
    	restoreLocalDefault.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreLocalDefault.addSelectionListener( new SelectionAdapter() {		
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				userSettingsFileEditor.setSelection( LOCAL_SETTINGS);
				mvPreferences.setUserSettingsOverride(null);
			}
						
		});
    	
    	// setup check button 
    	String localName = virtualPropertyResolver.resolve( initialUserSettings);
    	if (new File( localName).exists()) {
        	checkLocalButton.setToolTipText( String.format(OPEN_USER_SETTINGS, localName));
    		checkLocalButton.setEnabled( true);
    	} else {    
    		checkLocalButton.setEnabled( false);
    	}
    	 
    	/*
    	 * installation settings.xml 
    	 */
    	
    	// check and open 
    	checkRemoteButton = new Button( mavenSettingsGroup, SWT.NONE);
    	checkRemoteButton.setImage(openSettings);
    	checkRemoteButton.setEnabled(false);    
    	checkRemoteButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	checkRemoteButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {			
				
				File file = new File( virtualPropertyResolver.resolve(installationSettingsFileEditor.getSelection()));
				openExternalFile(file);				
			}
    		
		});
    	String initalInstallationSettings = mvPreferences.getConfSettingsOverride();
    	if (initalInstallationSettings == null) {
    		initalInstallationSettings = REMOTE_SETTINGS;
    	}
    			
    	// actual file editor 
    	installationSettingsFileEditor = new FileEditor( getShell());
    	installationSettingsFileEditor.setSelection( initalInstallationSettings);
    	installationSettingsFileEditor.setResolver( virtualPropertyResolver);
    	installationSettingsFileEditor.addListener( this);
    	Composite installationSettingsFileEditorComposite = installationSettingsFileEditor.createControl(mavenSettingsGroup, "Currently active installation file");
    	installationSettingsFileEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( installationSettingsFileEditor);
    	
    	// restore to default 
    	Button restoreRemoteDefault = new Button( mavenSettingsGroup, SWT.NONE);
    	restoreRemoteDefault.setImage(undoEditImage);
    	restoreRemoteDefault.setToolTipText("Restore to default [" + REMOTE_SETTINGS + "]");
    	restoreRemoteDefault.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreRemoteDefault.addSelectionListener( new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				installationSettingsFileEditor.setSelection( REMOTE_SETTINGS);
				mvPreferences.setConfSettingsOverride(null);
			}					
		});
    	
    	
    	String installationName = virtualPropertyResolver.resolve( initalInstallationSettings);
    	if (new File( installationName).exists()) {    		
    		checkRemoteButton.setToolTipText( String.format(OPEN_INSTALLATION_SETTINGS, installationName));
    		checkRemoteButton.setEnabled(true);
    	}
    	else {
    		checkRemoteButton.setEnabled( false);
    	}
    	
    	/*
    	 * local repository 
    	 */
    	Composite localRepoGroup = new Composite( composite, SWT.NONE);
    	GridLayout localRepoLayout = new GridLayout();
    	localRepoLayout.numColumns = 5;		
		localRepoGroup.setLayout( localRepoLayout);
		localRepoGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label localRepoLabel = new Label( localRepoGroup, SWT.NONE);
    	localRepoLabel.setText( "Currently active local repository");
    	localRepoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 5,1));
    	localRepoLabel.setFont(bigFont);
    	
    	// check button 
    	checkLocalRepoButton = new Button( localRepoGroup, SWT.CHECK);
    	//checkLocalButton.setImage(openSettings);
    	checkLocalRepoButton.setEnabled(false);
    	checkLocalRepoButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));    	
    	
    	// directory edito
    	localRepoPathEditor = new DirectoryEditor( getShell());
    	localRepoPathEditor.setResolver(virtualPropertyResolver);
    	localRepoPathEditor.addListener( this);
    	
    	localRepositoryExpression = mvPreferences.getLocalRepositoryOverride();
    	String standardLocalRepositoryExpression = null;
    	try {
    		standardLocalRepositoryExpression = reader.getLocalRepositoryExpression();			
		} catch (RepresentationException e1) {			
			String msg = "Cannot retrieve raw local repository path expression";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e1);
			ArtifactContainerPlugin.getInstance().log(status);	
		}  
    	if (localRepositoryExpression == null) {
    		localRepositoryExpression = standardLocalRepositoryExpression;
    	}
    	if (localRepositoryExpression == null) {
    		String msg = "no expression found in either settings.xml, required";
    		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
    		ArtifactContainerPlugin.getInstance().log(status);	
    		localRepositoryExpression = ".";
    	}
   
    	localRepoPathEditor.setSelection( localRepositoryExpression);
    	Composite localRepositoryEditorComposite = localRepoPathEditor.createControl( localRepoGroup, "Currently active local repository");
    	localRepositoryEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( localRepoPathEditor);
    	
    	// restore to default in settings.xml (any, both)
    	final String restoreExpression = standardLocalRepositoryExpression;
    	Button restoreLocalRepoPath = new Button( localRepoGroup, SWT.NONE);
    	restoreLocalRepoPath.setImage(undoEditImage);
    	restoreLocalRepoPath.setToolTipText("Restore to default in settings");
    	restoreLocalRepoPath.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreLocalRepoPath.addSelectionListener( new SelectionAdapter() {					
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				localRepoPathEditor.setSelection( restoreExpression);
				mvPreferences.setLocalRepositoryOverride(null);
			}					
		});
    	
    	if (new File( virtualPropertyResolver.resolve( localRepoPathEditor.getSelection())).exists()) {
    		checkLocalRepoButton.setSelection(true);
    	}
    	else {
    		checkLocalRepoButton.setSelection( false);
    	}
    	 
    	/*
    	 * active profiles from both settings.xml
    	 */
    	
    	// tree      	
    	// active profiles name (origin) 
    	//  - repositories : id, url, mirror, server, ravenhurst
    	// 
    	Composite treeGroup = new Composite( composite, SWT.NONE);
		treeGroup.setLayout( layout);
		treeGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label treeLabel = new Label( treeGroup, SWT.NONE);
    	treeLabel.setText( "Currently active profiles");
    	treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
    	treeLabel.setFont(bigFont);
    	
    	Composite treeComposite = new Composite( treeGroup, SWT.NONE);
        treeComposite.setLayout(layout);
 		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
 		
    	registry = new MavenPreferencesTreeRegistry();    	
    	
    	MavenPreferencesTreeContentProvider contentProvider = new MavenPreferencesTreeContentProvider();
    	contentProvider.setRegistry(registry);
    
    	treeViewer = new TreeViewer( treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    	treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    	
    	List<TreeViewerColumn> columns = new ArrayList<TreeViewerColumn>();
    	
    	
    	TreeViewerColumn mainColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        mainColumn.getColumn().setText("Profile");
        mainColumn.getColumn().setWidth(150);
        mainColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Profile")));
        mainColumn.getColumn().setResizable(false);
        columns.add(mainColumn);
        
        
        TreeViewerColumn repoNameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        repoNameColumn.getColumn().setText("Repository");
        repoNameColumn.getColumn().setWidth( 100);        
        repoNameColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Repository")));
        repoNameColumn.getColumn().setResizable(true);
        columns.add(repoNameColumn);
        
        TreeViewerColumn urlColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        urlColumn.getColumn().setText("Url");
        urlColumn.getColumn().setWidth(280);
        urlColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Url")));
        urlColumn.getColumn().setResizable(true);
        columns.add(urlColumn);
        
        TreeViewerColumn mirrorColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        mirrorColumn.getColumn().setText("Mirror");
        mirrorColumn.getColumn().setWidth(80);
        mirrorColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Mirror")));
        mirrorColumn.getColumn().setResizable(true);
        columns.add( mirrorColumn);
        
        TreeViewerColumn serverColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        serverColumn.getColumn().setText("Server");
        serverColumn.getColumn().setWidth(80);
        serverColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Server")));
        serverColumn.getColumn().setResizable(true);
        columns.add( serverColumn);
        
        TreeViewerColumn policyColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        policyColumn.getColumn().setText("Policy");
        policyColumn.getColumn().setWidth(80);
        policyColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreePolicyLabelProvider(registry)));
        policyColumn.getColumn().setResizable(true);
        columns.add( policyColumn);
   	
        
        ColumnViewerToolTipSupport.enableFor(treeViewer);
        
    	updateTreeViewer();
    	
    	
    	   
    	Tree tree = treeViewer.getTree();
    
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL);
		
		int ht = (tree.getItemHeight() * 10) + tree.getHeaderHeight();
		
    	Point computedSize = tree.computeSize(SWT.DEFAULT, ht);
		layoutData.heightHint = computedSize.y;    					
		layoutData.widthHint = computedSize.x;
    	tree.setLayoutData(layoutData);
 
    	TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
    	
    	Label checkLabel = new Label( treeGroup, SWT.NONE);
    	checkLabel.setText( "Check connectivity");
    	checkLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 3,1));
    	checkLabel.setFont(bigFont);
 
    	testConnectivity = new Button(treeGroup, SWT.NONE);
    	testConnectivity.setImage(connectivityImage);
    	testConnectivity.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	testConnectivity.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ConnectivityChecker connectivityChecker = new ConnectivityChecker(getShell());								
				connectivityChecker.open();												
			}
    		
		});
    	
    	composite.pack();	
		return composite;
	}
	
	private MavenSettingsReader reinitSettingsReader() {
		// changes in the editors on the page must reflect in the preferences, 
		// so that 
	
		reader = MalaclypseWirings.fullClasspathResolverContract().contract().settingsReader();
		return reader;
	}


	private void updateTreeViewer() {
		if (reader != null) {
			registry.setup(reader);			
			try {
				List<Profile> activeProfiles = reader.getActiveProfiles();
				treeViewer.setInput( activeProfiles);
			} catch (Exception e) {
				treeViewer.setInput( new ArrayList<Profile>());
			}
		}
		else {
			treeViewer.setInput( new ArrayList<Profile>());
		}		
		treeViewer.refresh();
	} 
	
	
	



	@Override
	public void acknowledgeChange(Object sender, String value) {
		VirtualPropertyResolver resolver = plugin.getVirtualPropertyResolver();
		if (sender == userSettingsFileEditor) {
			File file = new File( resolver.resolve(userSettingsFileEditor.getSelection()));
			if (file.exists()) {
				checkLocalButton.setEnabled( true);
				checkLocalButton.setToolTipText( String.format(OPEN_USER_SETTINGS, file.getAbsolutePath()));				
				plugin.getArtifactContainerPreferences(false).getMavenPreferences().setUserSettingsOverride( file.getAbsolutePath());
			}
			else {
				checkLocalButton.setEnabled( false);
			}
		}
		if (sender == installationSettingsFileEditor) {
			File file = new File (resolver.resolve( installationSettingsFileEditor.getSelection()));
			if (file.exists()) {
				checkRemoteButton.setEnabled(true);
				checkRemoteButton.setToolTipText( String.format( OPEN_INSTALLATION_SETTINGS, file.getAbsolutePath()));
				plugin.getArtifactContainerPreferences(false).getMavenPreferences().setConfSettingsOverride( file.getAbsolutePath());
			}
			else {
				checkRemoteButton.setEnabled(false);
			}
		}
		reinitSettingsReader();
		if (sender == localRepoPathEditor) {
			File file = new File (resolver.resolve( localRepoPathEditor.getSelection()));
			if (file.exists()) {
				checkLocalRepoButton.setSelection(true);
				plugin.getArtifactContainerPreferences(false).getMavenPreferences().setLocalRepositoryOverride( file.getAbsolutePath());
			}
			else {
				checkLocalRepoButton.setSelection(false);
			}
		}	
		else {
			try {
				localRepoPathEditor.setSelection(reader.getLocalRepositoryExpression());
			} catch (RepresentationException e1) {
				localRepoPathEditor.setSelection( plugin.getArtifactContainerPreferences(false).getMavenPreferences().getLocalRepositoryOverride());
			}			
		}
		updateTreeViewer();
	}



	@Override
	public boolean okToLeave() {

		MavenSettingsValidator validator = new MavenSettingsValidator( userSettingsFileEditor.getSelection(), installationSettingsFileEditor.getSelection(), localRepoPathEditor.getSelection());
		ValidationResult result = validator.validate();
		if (result.getValidationState()) {
			return super.okToLeave();		
		}
		else {				
			ArtifactContainerPluginValidatorDialog dlg = new ArtifactContainerPluginValidatorDialog(getShell());
			dlg.setResultsToDisplay( Collections.singletonList( result));
			dlg.open();	
			return false;
		}
		
	}

	


	@Override
	protected void performApply() {
		if (!okToLeave())
			return;
		
		MavenPreferences mavenPreferences = plugin.getArtifactContainerPreferences(false).getMavenPreferences();
		// find out if we need to override the settings.. 
		String userSettingsPath = userSettingsFileEditor.getSelection();
		if (!userSettingsPath.equalsIgnoreCase( LOCAL_SETTINGS)) {
			// set override value
			mavenPreferences.setUserSettingsOverride(userSettingsPath);			
		}
		else {
			mavenPreferences.setUserSettingsOverride(null);
		}
		String remoteSettingsPath = installationSettingsFileEditor.getSelection();
		if (!remoteSettingsPath.equalsIgnoreCase( REMOTE_SETTINGS)) {
			// set override value
			mavenPreferences.setConfSettingsOverride(remoteSettingsPath);
		}
		else {
			// remove override value 
			mavenPreferences.setConfSettingsOverride(null);
		}
		String localRepoPath = localRepoPathEditor.getSelection();
		if (!localRepoPath.equals(localRepositoryExpression)) {
			// set override value
			mavenPreferences.setLocalRepositoryOverride(localRepoPath);
		}
		else {
			// remove override value 
			mavenPreferences.setLocalRepositoryOverride(null);
		}
		
		try {
			new MavenPreferencesCodec(plugin.getPreferenceStore()).decode(mavenPreferences);
		} catch (CodecException e) {
			String msg = "cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}



	@Override
	public boolean performCancel() {
		plugin.getArtifactContainerPreferences(false).setMavenPreferences(orgMvPreferences);
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		if (!okToLeave())
			return false;
		performApply();
		return super.performOk();
	}



	@Override
	public void acknowledgeOverrideChange() {	
		// reset everything		
		acknowledgeChange(userSettingsFileEditor, userSettingsFileEditor.getSelection());
		acknowledgeChange(installationSettingsFileEditor, installationSettingsFileEditor.getSelection());
		
		
	}
	
	private void openExternalFile(File file) {		
		if (file.exists()) {
			if (file.exists() && file.isFile()) {
			    IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();					 
			    try {
			        IDE.openEditorOnFileStore( page, fileStore );
			    } catch ( PartInitException e ) {
			    	String msg = "cannot open file [" + file.getAbsolutePath() + "]";
			    	ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			    	ArtifactContainerPlugin.getInstance().log(status);	
			    }
			}
		}
	}
	

}
