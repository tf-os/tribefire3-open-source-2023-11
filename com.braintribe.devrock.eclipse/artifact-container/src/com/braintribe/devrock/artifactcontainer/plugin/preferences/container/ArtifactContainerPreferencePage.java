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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.container;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceInitializer;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.DynamicContainerPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.GwtPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.RavenhurstPreferencesCodec;
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


public class ArtifactContainerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final int POOL_SIZE = 5;
	
	protected Font bigFont;
	
	private IntegerEditor pruneEditor;
	private IntegerEditor batchSizeEditor;
	private BooleanEditor autoChainEditor;
	private StringEditor gwtFileEditor;
	private EnumEditor<ResolvingInstant> resolvingInstantEditor;
	private Image pruneUpdateProtocolImage;
	

	public ArtifactContainerPreferencePage() {
		//super(GRID);
		setPreferenceStore(ArtifactContainerPlugin.getInstance().getPreferenceStore());
		setDescription("Braintribe Artifact Container Preferences");
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ArtifactContainerPreferencePage.class, "clear.gif");
		pruneUpdateProtocolImage = imageDescriptor.createImage();
		
	}
	
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
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
		
		Composite acGroup = new Composite( composite, SWT.NONE);
		acGroup.setLayout( layout);
		acGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label acLabel = new Label( acGroup, SWT.NONE);
		acLabel.setText( "Dynamic classpath container settings");
		acLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		acLabel.setFont(bigFont);
		
		batchSizeEditor = new IntegerEditor();
		batchSizeEditor.setSelection( plugin.getArtifactContainerPreferences(false).getDynamicContainerPreferences().getConcurrentWalkBatchSize());
		Composite batchSizeComposite = batchSizeEditor.createControl(acGroup, "Maximum number of concurrent dependency walks: ");
		batchSizeComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		batchSizeComposite.setToolTipText("-1 for unlimited concurrent walks in workspace synchronization");
		
		
		autoChainEditor = new BooleanEditor();
		autoChainEditor.setSelection( plugin.getArtifactContainerPreferences(false).getDynamicContainerPreferences().getChainArtifactSync());
		Composite autoSyncComposite = autoChainEditor.createControl(acGroup, "Automatically sync depender projects:");
		autoSyncComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		autoSyncComposite.setToolTipText("whether AC should automatically determine depender projects and sync them as well");
				
		
		//
		// RH protocol
		//
		Composite rhGroup = new Composite( composite, SWT.NONE);
		rhGroup.setLayout( layout);
		rhGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label rhLabel = new Label( rhGroup, SWT.NONE);
		rhLabel.setText( "Dynamic update policy settings");
		rhLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		rhLabel.setFont(bigFont);
		
		pruneEditor = new IntegerEditor();
		pruneEditor.setSelection( plugin.getArtifactContainerPreferences(false).getRavenhurstPreferences().getPrunePeriod());
		Composite pruneComposite = pruneEditor.createControl(rhGroup, "Dynamic update protocol expiration (days): ");
		pruneComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Button pruneNow = new Button( rhGroup, SWT.NONE);
		pruneNow.setImage(pruneUpdateProtocolImage);
		pruneNow.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1));
		pruneNow.setToolTipText( "remove any expired update protocol files");
		pruneNow.addSelectionListener( new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent arg0) {			
				

				ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
				List<RavenhurstBundle> bundles;
				try {
					bundles = contract.ravenhurstScope().getRavenhurstBundles();
				} catch (RavenhurstException e1) {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot retrieve bundles to purge their protocol files", e1);					
					ArtifactContainerPlugin.getInstance().log(status);
					return;
				}
				
				ExecutorService es =  Executors.newFixedThreadPool( Math.max( bundles.size(), POOL_SIZE));
				List<Future<?>> futures = new ArrayList<>();
				
				for (RavenhurstBundle bundle : bundles) {
					Future<?> future = es.submit( () -> {
						List<String> messages = RavenhurstPersistenceHelper.purge( contract.settingsReader(), bundle, pruneEditor.getSelection());
						for (String msg : messages) {
							ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
							ArtifactContainerPlugin.getInstance().log(status);
						}						
					});
					futures.add(future);
				}
				for (Future<?> future : futures) {
					try {
						future.get();
					} catch (Throwable e1) {				
						;
					} 
				}
				
				es.shutdown();
				try {
					es.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "exception while waiting for thread pool shutdown", e);					
					ArtifactContainerPlugin.getInstance().log(status);
				}
												
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		
		
		// 
		// GWT
		//
		
				
		Composite gwtGroup = new Composite( composite, SWT.NONE);
		gwtGroup.setLayout( layout);
		gwtGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label gwtLabel = new Label( gwtGroup, SWT.NONE);
		gwtLabel.setText( "Settings for GWT");
		gwtLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		gwtLabel.setFont(bigFont);
		
		String gwtFile = plugin.getArtifactContainerPreferences(false).getGwtPreferences().getAutoInjectLibrary();
		gwtFileEditor = new StringEditor();
		gwtFileEditor.setSelection( gwtFile);
		Composite gwtComposite = gwtFileEditor.createControl(gwtGroup, "&GWT model auto inject gwt-user jar");
		gwtComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		//
		// DEBUG 
		//
		
		if (ArtifactContainerPlugin.isDebugActive()) {
			Composite resolvingInstantGroup = new Composite( composite, SWT.NONE);
			resolvingInstantGroup.setLayout( layout);
			resolvingInstantGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
			
			Label resolvingInstantLabel = new Label( resolvingInstantGroup, SWT.NONE);
			resolvingInstantLabel.setText( "Additional settings");
			resolvingInstantLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
			resolvingInstantLabel.setFont(bigFont);
			
		
			resolvingInstantEditor = new EnumEditor<>();
			
			Map<String, ResolvingInstant> choices = new HashMap<>();
			choices.put("adhoc", ResolvingInstant.adhoc);
			choices.put("posthoc", ResolvingInstant.posthoc);
			resolvingInstantEditor.setChoices(choices);
			
			Map<ResolvingInstant, String> tooltips = new HashMap<>();
			tooltips.put( ResolvingInstant.adhoc, "dependency clashes are resolved when they occur while traversing");
			tooltips.put( ResolvingInstant.posthoc, "dependency clashes are resolved after the tree has been fully traversed");
			resolvingInstantEditor.setToolTips(tooltips);
			
			resolvingInstantEditor.setSelection( plugin.getArtifactContainerPreferences(false).getDynamicContainerPreferences().getClashResolvingInstant());
			Composite resolvingInstantEditorComposite = resolvingInstantEditor.createControl(resolvingInstantGroup, "clash resolving instant", bigFont);
			resolvingInstantEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
			resolvingInstantEditorComposite.setFont( bigFont);
		}
				        	
    	composite.pack();
		return composite;
	}


	@Override
	public boolean performOk() {
		performApply();	
		return super.performOk();
	}

	

	@Override
	protected void performApply() {
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		ArtifactContainerPreferences preferences = plugin.getArtifactContainerPreferences(false);
		
		DynamicContainerPreferences dynContainerPreferences = preferences.getDynamicContainerPreferences();
		dynContainerPreferences.setConcurrentWalkBatchSize( batchSizeEditor.getSelection());		
		if (resolvingInstantEditor != null) {
			dynContainerPreferences.setClashResolvingInstant( resolvingInstantEditor.getSelection());
		}
		
		dynContainerPreferences.setChainArtifactSync( autoChainEditor.getSelection());
		
		try {
			new DynamicContainerPreferencesCodec(plugin.getPreferenceStore()).decode(dynContainerPreferences);
		} catch (CodecException e) {
			String msg = "cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
		RavenhurstPreferences rhPreferences = preferences.getRavenhurstPreferences();
		rhPreferences.setPrunePeriod( pruneEditor.getSelection());
		
		try {
			new RavenhurstPreferencesCodec(plugin.getPreferenceStore()).decode(rhPreferences);
		} catch (CodecException e) {
			String msg = "cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
		GwtPreferences gwtPreferences = preferences.getGwtPreferences();
		gwtPreferences.setAutoInjectLibrary( gwtFileEditor.getSelection());
		try {
			new GwtPreferencesCodec( plugin.getPreferenceStore()).decode(gwtPreferences);
		} catch (CodecException e) {

			String msg = "cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			
		}
			
	}


	@Override
	public boolean performCancel() {
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		ArtifactContainerPreferences preferences = plugin.getArtifactContainerPreferences(false); 
		preferences.setDynamicContainerPreferences( ArtifactContainerPreferenceInitializer.initializeDynamicContainerPreferences());
		preferences.setGwtPreferences( ArtifactContainerPreferenceInitializer.initializeGwtPreferences());
		preferences.setRavenhurstPreferences( ArtifactContainerPreferenceInitializer.initializeRavenhurstPreferences());	
		return super.performCancel();
	}


	@Override
	public void dispose() {
		pruneUpdateProtocolImage.dispose();
		super.dispose();
	}
	
	

	

}
