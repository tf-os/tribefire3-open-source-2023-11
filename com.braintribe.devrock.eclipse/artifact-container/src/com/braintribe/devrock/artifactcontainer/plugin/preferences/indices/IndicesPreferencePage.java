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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.indices;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceInitializer;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.DynamicContainerPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.GwtPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.RavenhurstPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.indices.GenericColumnLabelProvider;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.indices.RepositoryContentProvider;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ArtifactContainerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.container.DynamicContainerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ravenhurst.RavenhurstPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.plugin.commons.preferences.BooleanEditor;
import com.braintribe.plugin.commons.preferences.EnumEditor;
import com.braintribe.plugin.commons.preferences.IntegerEditor;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;


public class IndicesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static Logger log = Logger.getLogger(IndicesPreferencePage.class);
	protected Font bigFont;
	
	private Button clearIndex;
	private Button clearMeta;
	private Button rebuildIndex;
	
	private CommonTableViewer repositoryTableViewer;
	private Table repositoryTable;
	
	private Image pruneUpdateProtocolImage;
	private Image rebuildIndexImage;
	private Image clearIndexImage;
	private Image clearMetadataImage;
	private Label status;

	public IndicesPreferencePage() {
		setPreferenceStore(ArtifactContainerPlugin.getInstance().getPreferenceStore());
		setDescription("Braintribe Artifact Container Ravenhurst indices features");
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(IndicesPreferencePage.class, "clear.gif");
		pruneUpdateProtocolImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile(IndicesPreferencePage.class, "rebuild_index.gif");
		rebuildIndexImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile(IndicesPreferencePage.class, "clear-index.gif");
		clearIndexImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile(IndicesPreferencePage.class, "maven_indexes.gif");
		clearMetadataImage = imageDescriptor.createImage();		
		
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
		
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		
		
		
		
		//
		// INDICES 
		// 
		
		Composite mvGroup = new Composite( composite, SWT.NONE);
		mvGroup.setLayout( layout);
		mvGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label mvLabel = new Label( mvGroup, SWT.NONE);
		mvLabel.setText( "Rebuild/clean repository indices");
		mvLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		mvLabel.setFont(bigFont);
		

    	Composite repositoryComposite = new Composite( mvGroup, SWT.NONE);
    	GridLayout repoCompositelayout = new GridLayout();
    	repoCompositelayout.numColumns = 2;
    	repositoryComposite.setLayout( repoCompositelayout);
    	repositoryComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
    	
    	Composite tableComposite = new Composite(repositoryComposite, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1));
		
    	repositoryTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.MULTI);
		CommonTableColumnData [] columnData = new CommonTableColumnData[3];
		columnData[0] = new CommonTableColumnData("repository", 80, 100, "Repository name", new GenericColumnLabelProvider(0));
		columnData[1] = new CommonTableColumnData("last updated", 150, 200, "last RH timestamp", new GenericColumnLabelProvider(1));
		columnData[2] = new CommonTableColumnData("url", 200, 300, "Remote URL",  new GenericColumnLabelProvider(2));		
		
		
		repositoryTableViewer.setup(columnData);
		
		repositoryTable = repositoryTableViewer.getTable();
		repositoryTable.setHeaderVisible(true);
		repositoryTable.setLinesVisible(true);
		
		ClasspathResolverContract resolverContract = MalaclypseWirings.basicClasspathResolverContract().contract();
		List<RavenhurstBundle> bundles = null;
		try {
			bundles = resolverContract.ravenhurstScope().getRavenhurstBundles();
		} catch (RavenhurstException e) {		
			e.printStackTrace();
		}
	
		RepositoryContentProvider contentProvider = new RepositoryContentProvider();    
		contentProvider.setBundles( bundles);
		repositoryTableViewer.setContentProvider(contentProvider);
		repositoryTableViewer.setInput( bundles);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	int ht = (repositoryTable.getItemHeight() * 15) + repositoryTable.getHeaderHeight();
    	layoutData.heightHint = repositoryTable.computeSize(SWT.DEFAULT, ht).y;
    	repositoryTable.setLayoutData( layoutData);

    	Composite buttonComposite = new Composite( repositoryComposite, SWT.NONE);

		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout( buttonLayout);
		buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.LEFT, true, false, 1,1));
		
		rebuildIndex = new Button( buttonComposite, SWT.NONE);
		rebuildIndex.setImage(rebuildIndexImage);
		rebuildIndex.setToolTipText("rebuilds the group-indices of the selected repositories, detects missed groups and clears associated maven-metadata");
		rebuildIndex.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		rebuildIndex.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<RavenhurstBundle> bundles = contentProvider.getBundlesAtPositions( repositoryTable.getSelectionIndices());
				StringBuilder sb = new StringBuilder();
				for (RavenhurstBundle bundle : bundles) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append( bundle.getRepositoryId() + "(");
					List<String> newGroups = resolverContract.repositoryReflection().correctLocalRepositoryStateOf(bundle);
					if (newGroups.size() > 0) {
						sb.append( newGroups.stream().collect( Collectors.joining(",")));
					}
					else {
						sb.append("no new groups");
					}
					sb.append( ")");
				}
				String msg = "rebuilt :" + sb.toString();
				status.setText( msg);
				logResult( msg);
			}			
		});
		
		
		clearIndex = new Button( buttonComposite, SWT.NONE);
		clearIndex.setImage( clearIndexImage);
		clearIndex.setToolTipText("clears the group-indices of the selected repositories");
		clearIndex.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		clearIndex.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<RavenhurstBundle> bundles = contentProvider.getBundlesAtPositions( repositoryTable.getSelectionIndices());				
				RepositoryReflectionHelper.purgeIndex( resolverContract.lockFactory(), new File(resolverContract.settingsReader().getLocalRepository()), bundles);
				String msg = "purged .index of " + bundles.stream().map( b -> b.getRepositoryId()).collect( Collectors.joining(","));
				status.setText( msg);
				logResult( msg);
			}			
		});
		
		clearMeta = new Button( buttonComposite, SWT.NONE);
		clearMeta.setToolTipText("clears all maven-metadata.xml associated with the selected repositories");
		clearMeta.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		clearMeta.setImage(clearMetadataImage);
		clearMeta.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<RavenhurstBundle> bundles = contentProvider.getBundlesAtPositions( repositoryTable.getSelectionIndices());				
				RepositoryReflectionHelper.purgeMetadata( resolverContract.lockFactory(), new File(resolverContract.settingsReader().getLocalRepository()), bundles);
				String msg = "purged all maven-metadata of " + bundles.stream().map( b -> b.getRepositoryId()).collect( Collectors.joining(","));
				status.setText( msg);
				logResult( msg);
			}			
		});
		
		Composite statusComposite = new Composite( composite, SWT.NONE);
		statusComposite.setLayout( layout);
		statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));
		
		        
	    status = new Label( statusComposite, SWT.NONE);	
	    status.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));		
				        	
    	composite.pack();
		return composite;
	}

	
	private void logResult( String msg) {
		log.info(msg);
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
		ArtifactContainerPlugin.getInstance().log(status);	
	}


	@Override
	public void dispose() {
		rebuildIndexImage.dispose();
		clearIndexImage.dispose();
		clearMetadataImage.dispose();
		pruneUpdateProtocolImage.dispose();
		super.dispose();
	}
	
	

	

}
