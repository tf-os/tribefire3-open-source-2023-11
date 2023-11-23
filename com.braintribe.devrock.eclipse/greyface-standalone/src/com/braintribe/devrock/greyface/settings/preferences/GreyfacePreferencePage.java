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
package com.braintribe.devrock.greyface.settings.preferences;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.settings.codecs.GreyfacePreferencesCodec;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.NameColumnEditingSupport;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.NameColumnLabelProvider;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.PasswordColumnEditingSupport;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.PasswordColumnLabelProvider;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.RepositoryContentProvider;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.UrlColumnEditingSupport;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.UrlColumnLabelProvider;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.UserColumnEditingSupport;
import com.braintribe.devrock.greyface.settings.preferences.tableviewer.UserColumnLabelProvider;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.preferences.gf.GreyFacePreferences;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.plugin.commons.preferences.DirectoryEditor;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class GreyfacePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {	
	private static Logger log = Logger.getLogger(GreyfacePreferencePage.class);
	private GreyfacePlugin plugin = GreyfacePlugin.getInstance();
	private Font bigFont;	
	private DirectoryEditor temporaryDirectoryEditor;
	private GreyFacePreferences gfPreferences = plugin.getGreyfacePreferences(false);
	private List<RemoteRepository> sourceRepositories = gfPreferences.getSourceRepositories();


	
	@Override
	public void init(IWorkbench workbench) {			
		setDescription("DevRock Greyface Repository Preferences");		
	}
	
	@Override
	public void dispose() {
		bigFont.dispose();
		//VirtualEnvironmentPlugin.getInstance().removeListener( temporaryDirectoryEditor);
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
		
		Composite greyfaceStandardGroup = new Composite( composite, SWT.NONE);
		greyfaceStandardGroup.setLayout( layout);
		greyfaceStandardGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		VirtualPropertyResolver virtualPropertyResolver = plugin.getVirtualPropertyResolver();
    	temporaryDirectoryEditor = new DirectoryEditor( getShell());
    	temporaryDirectoryEditor.setSelection( gfPreferences.getTempDirectory());
    	temporaryDirectoryEditor.setPropertyResolver(virtualPropertyResolver);
    	//VirtualEnvironmentPlugin.getInstance().addListener( temporaryDirectoryEditor);
		Composite ahComposite = temporaryDirectoryEditor.createControl( greyfaceStandardGroup, "Directory for temporary files: ");
		ahComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		
		Label standardSettingsLabel = new Label( greyfaceStandardGroup, SWT.NONE);
		standardSettingsLabel.setText( "Scan repositories");
		standardSettingsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		standardSettingsLabel.setFont(bigFont);
	
	
		
		Composite tableComposite = new Composite(greyfaceStandardGroup, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;	
		tableComposite.setLayout(gridLayout);
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		
		final CommonTableViewer tableViewer = new CommonTableViewer(tableComposite,  SWT.V_SCROLL | SWT.SINGLE);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		
		CommonTableColumnData [] tableColumnData = new CommonTableColumnData[4];
		tableColumnData[0] = new CommonTableColumnData("Name", 100, 100, "Name of the repository", new NameColumnLabelProvider(), new NameColumnEditingSupport(tableViewer));
		tableColumnData[1] = new CommonTableColumnData("URL", 200, 200, "URL of the repository", new UrlColumnLabelProvider(), new UrlColumnEditingSupport(tableViewer));
		tableColumnData[2] = new CommonTableColumnData("User", 50, 100, "User required to access the repository", new UserColumnLabelProvider(), new UserColumnEditingSupport(tableViewer));
		tableColumnData[3] = new CommonTableColumnData("Password", 50, 100, "Password required to access the repository", new PasswordColumnLabelProvider(), new PasswordColumnEditingSupport(tableViewer));
		
		tableViewer.setup(tableColumnData);
		
		final Table targetTable = tableViewer.getTable();
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4);
    	targetTable.setLayoutData( layoutData);
    	int ht = (targetTable.getItemHeight() * 10) + targetTable.getHeaderHeight();
    	layoutData.heightHint = targetTable.computeSize(SWT.DEFAULT, ht).y;
    	
    	// 
    	RepositoryContentProvider contentProvider = new RepositoryContentProvider();
    	contentProvider.setSettings(sourceRepositories);
    	tableViewer.setContentProvider(contentProvider);
    	tableViewer.setInput(sourceRepositories);
		
    	//
    	// buttons
    	//
    
    	final Button addTargetButton = new Button( greyfaceStandardGroup, SWT.NONE);
    	addTargetButton.setText( "Add repository");
    	addTargetButton.setLayoutData( new GridData( SWT.LEFT,SWT.CENTER, true, true, 2, 1));
    	
    	final Button removeTargetButton = new Button( greyfaceStandardGroup, SWT.NONE);
    	removeTargetButton.setText( "Remove repository");
    	removeTargetButton.setLayoutData( new GridData( SWT.RIGHT,SWT.CENTER, true, true, 2, 1));
    	
    	SelectionListener listener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget == addTargetButton) {
					// add 
					RemoteRepository setting = RemoteRepository.T.create();
					setting.setName("name");
					setting.setUrl("url");
					setting.setUser("");
					setting.setPassword("");										
					sourceRepositories.add(setting);					
					tableViewer.refresh();
				}
				
				if (event.widget == removeTargetButton) {
					// remove
					TableItem [] items = targetTable.getSelection();
 					for (TableItem item : items) {
 						RemoteRepository setting = (RemoteRepository) item.getData();
 						sourceRepositories.remove(setting);
 					}
 					tableViewer.refresh();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {							
			}
		};
		
		addTargetButton.addSelectionListener( listener);
		removeTargetButton.addSelectionListener(listener);
		
    	return composite;
	}
	@Override
	protected void performApply() {
		gfPreferences.setSourceRepositories( sourceRepositories);
		gfPreferences.setTempDirectory( temporaryDirectoryEditor.getSelection());
		try {
			new GreyfacePreferencesCodec(plugin.getPreferenceStore()).decode(gfPreferences);
		} catch (CodecException e) {
			log.error("cannot store preferences", e);
		}		
	}
	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	

}
