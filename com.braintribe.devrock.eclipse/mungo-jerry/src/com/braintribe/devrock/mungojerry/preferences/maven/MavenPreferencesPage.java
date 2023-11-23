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
package com.braintribe.devrock.mungojerry.preferences.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.codec.CodecException;
import com.braintribe.commons.environment.MungojerryEnvironment;
import com.braintribe.commons.preferences.FileEditor;
import com.braintribe.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.devrock.mungojerry.preferences.MavenPreferencesCodec;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;
import com.braintribe.model.maven.settings.Profile;


public class MavenPreferencesPage extends PreferencePage implements ModificationNotificationListener, IWorkbenchPreferencePage, HasMavenTokens, VirtualEnvironmentNotificationListener{

	private Button checkLocalButton;
	private FileEditor userSettingsFileEditor;
	private Button checkRemoteButton;
	private FileEditor installationSettingsFileEditor;
	private Mungojerry plugin = Mungojerry.getInstance();
	private MavenPreferences mvPreferences = plugin.getMungojerryPreferences( false).getMavenPreferences();
	private Font bigFont;	
	protected static final String CODE_TOOLTIP = "_TOOLTIP";
	private Image dynamicUpdatePolicyImage;
	private Image undoEditImage;
	private MavenSettingsReader reader;
	private FileEditor localRepoPathEditor;
	private Button checkLocalRepoButton;
	private String localRepositoryExpression;
	private TreeViewer treeViewer;
	private MavenPreferencesTreeRegistry registry;
	
	@Override
	public void init(IWorkbench workbench) {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "arrow_refresh_small.png");
		dynamicUpdatePolicyImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( getClass(), "undo_edit_gimped.png");
		undoEditImage = imageDescriptor.createImage();

		MungojerryEnvironment environment = Mungojerry.getInstance().getEnvironment();
		reader = environment.getMavenSettingsReader();
	}
	
	

	@Override
	public void dispose() {
		if (dynamicUpdatePolicyImage != null)
			dynamicUpdatePolicyImage.dispose();
		if (undoEditImage != null)
			undoEditImage.dispose();
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
		
		VirtualPropertyResolver virtualPropertyResolver = plugin.getVirtualPropertyResolver();
		
		Composite svnGroup = new Composite( composite, SWT.NONE);
		GridLayout svnLayout = new GridLayout();
		svnLayout.numColumns = 5;
		svnGroup.setLayout( svnLayout);
		svnGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label svnLabel = new Label( svnGroup, SWT.NONE);
    	svnLabel.setText( "Currently active settings.xml");
    	svnLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 5,1));
    	svnLabel.setFont(bigFont);
    	
    	checkLocalButton = new Button( svnGroup, SWT.CHECK);
    	checkLocalButton.setEnabled(false);
    	checkLocalButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	
    	userSettingsFileEditor = new FileEditor( getShell());    	
    	
    	String initialUserSettings = mvPreferences.getUserSettingsOverride();
    	if (initialUserSettings == null) {
    		initialUserSettings = LOCAL_SETTINGS;
    	}

    	userSettingsFileEditor.setSelection( initialUserSettings);
		userSettingsFileEditor.setResolver( virtualPropertyResolver);
		userSettingsFileEditor.addListener( this);
    	Composite userSettingsEditorComposite = userSettingsFileEditor.createControl(svnGroup, "Currently active user file");
    	userSettingsEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( userSettingsFileEditor);
    	
    	Button restoreLocalDefault = new Button( svnGroup, SWT.NONE);
    	restoreLocalDefault.setImage(undoEditImage);
    	restoreLocalDefault.setToolTipText("Restore to default [" + LOCAL_SETTINGS + "]");
    	restoreLocalDefault.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreLocalDefault.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				userSettingsFileEditor.setSelection( LOCAL_SETTINGS);
				mvPreferences.setUserSettingsOverride(null);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
    	
    	String localName = virtualPropertyResolver.resolve( initialUserSettings);
    	if (new File( localName).exists()) {
    		checkLocalButton.setSelection( true);
    	} else {
    		checkLocalButton.setSelection( false);
    	}
    	 
    	
    	
    	checkRemoteButton = new Button( svnGroup, SWT.CHECK);
    	checkRemoteButton.setEnabled(false);
    	checkRemoteButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));

    	String initialConfSettings = mvPreferences.getConfSettingsOverride();
    	if (initialConfSettings  == null) {
    		initialConfSettings = REMOTE_SETTINGS;
    	}
    	installationSettingsFileEditor = new FileEditor( getShell());
    	installationSettingsFileEditor.setSelection( initialConfSettings);
    	installationSettingsFileEditor.setResolver( virtualPropertyResolver);
    	installationSettingsFileEditor.addListener( this);
    	Composite installationSettingsFileEditorComposite = installationSettingsFileEditor.createControl(svnGroup, "Currently active installation file");
    	installationSettingsFileEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( installationSettingsFileEditor);
    	
    	Button restoreRemoteDefault = new Button( svnGroup, SWT.NONE);
    	restoreRemoteDefault.setImage(undoEditImage);
    	restoreRemoteDefault.setToolTipText("Restore to default [" + REMOTE_SETTINGS + "]");
    	restoreRemoteDefault.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreRemoteDefault.addSelectionListener( new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				installationSettingsFileEditor.setSelection( REMOTE_SETTINGS);
				mvPreferences.setConfSettingsOverride( null);
			}		
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
    	
    	
    	String installationName = virtualPropertyResolver.resolve( initialConfSettings);
    	if (new File( installationName).exists()) {
    		checkRemoteButton.setSelection(true);
    	}
    	else {
    		checkRemoteButton.setSelection( false);
    	}
    	
    	Composite localRepoGroup = new Composite( composite, SWT.NONE);
    	GridLayout localRepoLayout = new GridLayout();
    	localRepoLayout.numColumns = 5;		
		localRepoGroup.setLayout( localRepoLayout);
		localRepoGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label localRepoLabel = new Label( localRepoGroup, SWT.NONE);
    	localRepoLabel.setText( "Currently active local repository");
    	localRepoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 5,1));
    	localRepoLabel.setFont(bigFont);
    	
    	checkLocalRepoButton = new Button( localRepoGroup, SWT.CHECK);
    	checkLocalRepoButton.setEnabled(false);
    	checkLocalRepoButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	
    	localRepoPathEditor = new FileEditor( getShell());
    	localRepoPathEditor.setResolver(virtualPropertyResolver);	
    	localRepoPathEditor.addListener( this);
    	localRepositoryExpression = mvPreferences.getLocalRepositoryOverride();
    	String standardLocalRepositoryExpression = null;
    	try {
    		standardLocalRepositoryExpression = reader.getLocalRepositoryExpression();			
		} catch (RepresentationException e1) {			
			Mungojerry.log(Status.ERROR, "Cannot retrieve raw local repository path expression");
		}  
    	if (localRepositoryExpression == null) {
    		localRepositoryExpression = standardLocalRepositoryExpression;
    	}
    	if (localRepositoryExpression == null) {
    		Mungojerry.log( Status.ERROR, "no expression found in either settings.xml, required");
    		localRepositoryExpression = ".";
    	}
   
    	localRepoPathEditor.setSelection( localRepositoryExpression);
    	Composite localRepositoryEditorComposite = localRepoPathEditor.createControl( localRepoGroup, "Currently active local repository");
    	localRepositoryEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
    	VirtualEnvironmentPlugin.getInstance().addListener( localRepoPathEditor);
    	
    	final String restoreExpression = standardLocalRepositoryExpression;
    	Button restoreLocalRepoPath = new Button( localRepoGroup, SWT.NONE);
    	restoreLocalRepoPath.setImage(undoEditImage);
    	restoreLocalRepoPath.setToolTipText("Restore to default in settings");
    	restoreLocalRepoPath.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
    	restoreLocalRepoPath.addSelectionListener( new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				localRepoPathEditor.setSelection( restoreExpression);
			}			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
    	
    	if (new File( virtualPropertyResolver.resolve( localRepoPathEditor.getSelection())).exists()) {
    		checkLocalRepoButton.setSelection(true);
    	}
    	else {
    		checkLocalRepoButton.setSelection( false);
    	}
    	 
    	
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
    	
    	Composite treeComposite = new Composite( treeGroup, SWT.BORDER);
        treeComposite.setLayout(layout);
 		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
 		
    	registry = new MavenPreferencesTreeRegistry();    	
    	
    	MavenPreferencesTreeContentProvider contentProvider = new MavenPreferencesTreeContentProvider();
    	contentProvider.setRegistry(registry);
    
    	treeViewer = new TreeViewer( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    	treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    	
    	TreeViewerColumn mainColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        mainColumn.getColumn().setText("Profile");
        mainColumn.getColumn().setWidth(200);
        mainColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Profile")));
        
        
        TreeViewerColumn repoNameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        repoNameColumn.getColumn().setText("Repository");
        repoNameColumn.getColumn().setWidth( 100);        
        repoNameColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Repository")));
        
        TreeViewerColumn urlColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        urlColumn.getColumn().setText("Url");
        urlColumn.getColumn().setWidth(300);
        urlColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Url")));
        
        TreeViewerColumn mirrorColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        mirrorColumn.getColumn().setText("Mirror");
        mirrorColumn.getColumn().setWidth(100);
        mirrorColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Mirror")));
        
        TreeViewerColumn serverColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        serverColumn.getColumn().setText("Server");
        serverColumn.getColumn().setWidth(100);
        serverColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreeLabelProvider( registry, "Server")));
        
        TreeViewerColumn policyColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        policyColumn.getColumn().setText("Policy");
        policyColumn.getColumn().setWidth(100);
        policyColumn.setLabelProvider(new MavenPreferencesDelegatingLabelProvider( new MavenPreferencesTreePolicyLabelProvider(registry)));
    	
        
        
        ColumnViewerToolTipSupport.enableFor(treeViewer);
        
    	updateTreeViewer();
    	
    	
    	    
    	Tree tree = treeViewer.getTree();
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1	);
		int ht = (tree.getItemHeight() * 10) + tree.getHeaderHeight();
		
    	Point computedSize = tree.computeSize(SWT.DEFAULT, ht);
		layoutData.heightHint = computedSize.y;    			
		layoutData.widthHint = computedSize.x;
    	tree.setLayoutData(layoutData);
    	composite.pack();	
		return composite;
	}



	private void updateTreeViewer() {
		MungojerryEnvironment environment = Mungojerry.getInstance().getEnvironment();
		reader = environment.getMavenSettingsReader();
		if (reader != null) {
			registry.setup(reader);
			try {
				List<Profile> activeProfiles = reader.getAllProfiles();
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
		if (sender == null || sender == userSettingsFileEditor) {
			File file = new File( resolver.resolve(userSettingsFileEditor.getSelection()));
			if (file.exists()) {
				checkLocalButton.setSelection( true);
			}
			else {
				checkLocalButton.setSelection( false);
			}
		}
		if (sender == null || sender == installationSettingsFileEditor) {
			File file = new File (resolver.resolve( installationSettingsFileEditor.getSelection()));
			if (file.exists()) {
				checkRemoteButton.setSelection(true);
			}
			else {
				checkRemoteButton.setSelection(false);
			}
		}
		if (sender == null || sender == localRepoPathEditor) {
			File file = new File (resolver.resolve( localRepoPathEditor.getSelection()));
			if (file.exists()) {
				checkLocalRepoButton.setSelection(true);
			}
			else {
				checkLocalRepoButton.setSelection(false);
			}
		}
		
		updateTreeViewer();
	}



	@Override
	public boolean okToLeave() {
		VirtualPropertyResolver resolver = plugin.getVirtualPropertyResolver();
		File userFile = new File( resolver.resolve(userSettingsFileEditor.getSelection()));
		File remoteFile = new File (resolver.resolve(installationSettingsFileEditor.getSelection()));
		
		if (!userFile.exists() && !remoteFile.exists()) {
			this.setErrorMessage( "At least one of the settings file is required to exist");
			return true;
		}
		File localFile = new File( resolver.resolve( localRepoPathEditor.getSelection()));
		if (!localFile.exists()) {
			this.setErrorMessage( "The path of the local repository is required to exist");
			return true;
		}
		this.setErrorMessage( null);
		return super.okToLeave();
	}



	@Override
	protected void performApply() {
		if (!okToLeave())
			return;
		MavenPreferences mavenPreferences = plugin.getMungojerryPreferences( false).getMavenPreferences();
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
			Mungojerry.log(IStatus.ERROR, "cannot write preferences to IPreferencesStore as " + e.getLocalizedMessage());			
		}
	}



	@Override
	public boolean performCancel() {
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
	
	

}
