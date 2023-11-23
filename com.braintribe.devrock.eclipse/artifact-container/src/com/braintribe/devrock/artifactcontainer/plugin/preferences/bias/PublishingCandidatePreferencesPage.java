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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.bias;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBiasPersitenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.plugin.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;


public class PublishingCandidatePreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, VirtualEnvironmentNotificationListener, ModificationNotificationListener {	
	protected Font bigFont;	
		
	
	private CommonTableViewer biasTableViewer;
	private Table biasTable;
	private Button addBiasButton;
	private Button editBiasButton;
	private Button removeBiasButton;
	
	private Image addImage;
	private Image editImage;
	private Image removeImage;
	

	private List<ArtifactBias> biasedIdentifications;

	private MavenSettingsReader reader;
	private ArtifactBiasPersitenceExpert publishingCandiateBiasPersitenceExpert;
	
	public PublishingCandidatePreferencesPage() {
		super();
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( PublishingCandidatePreferencesPage.class, "add.gif");
		addImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( PublishingCandidatePreferencesPage.class, "editconfig.gif");
		editImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( PublishingCandidatePreferencesPage.class, "remove.gif");
		removeImage = imageDescriptor.createImage();
		
	}


	@Override
	public void init(IWorkbench arg0) {		
	
		reader = MalaclypseWirings.fullClasspathResolverContract().contract().settingsReader();
	
		publishingCandiateBiasPersitenceExpert = new ArtifactBiasPersitenceExpert();
		publishingCandiateBiasPersitenceExpert.setLocalRepositoryLocationProvider(reader);
		publishingCandiateBiasPersitenceExpert.setLockFactory( new FilesystemSemaphoreLockFactory());
		
		try {
			biasedIdentifications = publishingCandiateBiasPersitenceExpert.decode();
		} catch (RepositoryPersistenceException e) {
			String msg = "cannot load publishing candidate bias data";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		VirtualEnvironmentPlugin.getInstance().addListener(this);
		
		
	}

	
	@Override
	public void dispose() {
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		virtualEnvironmentPlugin.removeListener(this);
		
		addImage.dispose();
		editImage.dispose();
		removeImage.dispose();
		
		
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
		
		Composite svnGroup = new Composite( composite, SWT.NONE);
		svnGroup.setLayout( layout);
		svnGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
	
		Label svnLabel = new Label( svnGroup, SWT.NONE);
    	svnLabel.setText( "Artifacts with a bias to their origin");
    	svnLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	svnLabel.setFont(bigFont);
    	    	
    	
    	Composite repositoryComposite = new Composite( svnGroup, SWT.NONE);
    	GridLayout repoCompositelayout = new GridLayout();
    	repoCompositelayout.numColumns = 2;
    	repositoryComposite.setLayout( repoCompositelayout);
    	repositoryComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
    	
    	Composite tableComposite = new Composite(repositoryComposite, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1));
		
    	biasTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[3];
		columnData[0] = new CommonTableColumnData("groupId", 200, 200, "Group id", new GroupIdColumnLabelProvider());
		columnData[1] = new CommonTableColumnData("artifactId", 100, 100, "Artifact id (can remain empty)", new ArtifactIdColumnLabelProvider());
		columnData[2] = new CommonTableColumnData("repositories", 200, 200, "List of repository expressions", new RepositoryExpressionColumnLabelProvider());
		biasTableViewer.setup(columnData);
		
		biasTable = biasTableViewer.getTable();
		biasTable.setHeaderVisible(true);
		biasTable.setLinesVisible(true);
		
		
	
		PublishingCandidateBiasContentProvider contentProvider = new PublishingCandidateBiasContentProvider();    
		contentProvider.setBiases(biasedIdentifications);
		biasTableViewer.setContentProvider(contentProvider);
		biasTableViewer.setInput( biasedIdentifications);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	int ht = (biasTable.getItemHeight() * 15) + biasTable.getHeaderHeight();
    	layoutData.heightHint = biasTable.computeSize(SWT.DEFAULT, ht).y;
    	biasTable.setLayoutData( layoutData);

    	Composite buttonComposite = new Composite( repositoryComposite, SWT.NONE);

		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout( buttonLayout);
		buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.LEFT, true, false, 1,1));
		
		
		// add button
		addBiasButton = new Button( buttonComposite, SWT.NONE);
		addBiasButton.setImage(addImage);
		addBiasButton.setToolTipText( "add a new bias");
		addBiasButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		addBiasButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PublishingCandidateBiasDialog pairingDialog = new PublishingCandidateBiasDialog( getShell());				
				pairingDialog.open();
				ArtifactBias pairing = pairingDialog.getSelection();
				if (pairing != null) {
					biasedIdentifications.add(pairing);
					pairingDialog.setPairings(biasedIdentifications);
					biasTableViewer.setInput(biasedIdentifications);
					biasTableViewer.refresh();
				}
			}
			
		});
		
		
		// edit button
		editBiasButton = new Button( buttonComposite, SWT.NONE);
		editBiasButton.setImage(editImage);
		editBiasButton.setToolTipText( "edit the selected bias");
		editBiasButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		editBiasButton.addSelectionListener( new SelectionAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				PublishingCandidateBiasDialog pairingDialog = new PublishingCandidateBiasDialog( getShell());
				// prime with selected pairing
				int index = biasTable.getSelectionIndex();
				if (index >= 0) {
					pairingDialog.setSelection( ((List<ArtifactBias>) biasTableViewer.getInput()).get(index));
					pairingDialog.setPairings( biasedIdentifications);
					pairingDialog.open();					
					biasTableViewer.refresh();
					
				}
			}			
		});
		
		

		
		// remove button 
		removeBiasButton = new Button( buttonComposite, SWT.NONE);
		removeBiasButton.setToolTipText( "remove the selected bias");
		removeBiasButton.setImage(removeImage);
		removeBiasButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		removeBiasButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = biasTable.getSelectionIndex();
				
				if (index >= 0) {
					ArtifactBias selected = ((List<ArtifactBias>) biasTableViewer.getInput()).get(index);
					biasedIdentifications.remove( selected);
					biasTableViewer.setInput( biasedIdentifications);
					biasTableViewer.refresh();
				}
			}
			
		});
		
		removeBiasButton.setEnabled( false);
		editBiasButton.setEnabled( false);
		
		biasTable.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (biasTable.getSelectionIndex() >= 0) {
					removeBiasButton.setEnabled( true);
					editBiasButton.setEnabled( true);
				}
				else {
					removeBiasButton.setEnabled( false);
					editBiasButton.setEnabled( false);
				}
				super.widgetSelected(e);
			}
			
		});
		/*	    	    	   		
		Composite qiGroup = new Composite( composite, SWT.NONE);
		qiGroup.setLayout( layout);
		qiGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label qiLabel = new Label( qiGroup, SWT.NONE);
		qiLabel.setText( "Quick Import options");
    	qiLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	qiLabel.setFont(bigFont);
    	
  
    	    	
		*/
		return composite;
	}

	
	@Override
	public void acknowledgeChange(Object sender, String value) {			
	}
	


	@Override
	public void acknowledgeOverrideChange() {		
	}


	@Override
	public boolean okToLeave() {
		return super.okToLeave();						
	}


	@Override
	protected void performApply() {
		// 		
		if (!okToLeave())
			return;
	 
		try {			
			publishingCandiateBiasPersitenceExpert.encode( biasedIdentifications);
		} catch (RepositoryPersistenceException e) {
			String msg = "cannot write publishing candidate bias data";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
	}

	
	

	@Override
	protected void performDefaults() {
		
		// 
		// 
		biasTableViewer.setInput( biasedIdentifications);
		biasTableViewer.refresh();
		super.performDefaults();
	}


	@Override
	public boolean performCancel() {		
		performDefaults();
		return super.performCancel();
	}


	@Override
	public boolean performOk() {
		if (okToLeave() == false) {			
			return false;
		}
		performApply();
		
		return super.performOk();
	}
	
	
	
	
}
