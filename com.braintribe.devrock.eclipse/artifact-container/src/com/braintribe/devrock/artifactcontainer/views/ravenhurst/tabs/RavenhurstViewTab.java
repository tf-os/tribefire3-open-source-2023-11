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
package com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.listener.RavenhurstNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.FilesystemBasedPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;
import com.braintribe.plugin.commons.views.tabbed.tabs.AbstractTreeViewTab;

public class RavenhurstViewTab extends AbstractTreeViewTab implements RavenhurstNotificationListener{	
	private static final String DATAKEY_ARTIFACTS = "artifacts";	

	
	private SimpleDateFormat format = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss");
	private RemoteRepository repository;
	private boolean populated = false;
	private boolean deferred = false;
	
	private Image updateInfoImage;
	private Image interogationImage;

	private RavenhurstBundle bundle;
	private ClasspathResolverContract contract;
	private MavenSettingsReader reader;
	private FilesystemBasedPersistenceExpertForRavenhurstBundle bundleExpert;

	
	public RavenhurstViewTab(Display display, RemoteRepository repository, ClasspathResolverContract contract, RavenhurstBundle bundle) {
		super(display);
		this.contract = contract;
		this.repository = repository;
		this.reader = this.contract.settingsReader();
		
		this.bundleExpert = new FilesystemBasedPersistenceExpertForRavenhurstBundle();
		this.bundleExpert.setLockFactory( new FilesystemSemaphoreLockFactory());

		this.bundle = bundle;
		setColumnNames( new String [] { "Date", "Artifact", "Group", "Version"});
		setColumnWeights( new int [] { 150, 100, 200, 50});
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(RavenhurstViewTab.class, "arrow_refresh.png");
		updateInfoImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RavenhurstViewTab.class, "info_obj.gif");
		interogationImage = imageDescriptor.createImage();	
	}

	@Override
	public void acknowledgeProjectChanged(IProject project) {}
	
	@Override
	public void acknowledgeExternalMonitorResult(WalkMonitoringResult result) {}

	@Override
	protected void addAdditionalButtons(Composite composite, Layout layout) {}

	@Override
	protected void initializeTree() {}
	
	@Override
	public void acknowledgeLockTerminal(boolean lock) {}

	@Override
	public void dispose() {
		updateInfoImage.dispose();
		interogationImage.dispose();
		super.dispose();
	}

	@Override
	public void acknowledgeVisibility(String key) {
		super.acknowledgeVisibility(key);
		populateAsJob();
	}

	@Override
	public void acknowledgeActivation() {
		super.acknowledgeActivation();		
		populateAsJob();		
	}
	
	private void populateAsJob() {
		Job job = new Job("Updating protocol") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				populate();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private void populate() {
		if (visible && active) {
			if (!populated || deferred) {
				populate( true);
			}
		}
		else {
			deferred = true;
		}
	}
	
	
	/**
	 * @param interactive
	 */
	private void populate( boolean interactive) {
		List<File> files = RavenhurstPersistenceHelper.getPersistedFilesForBundle( reader.getLocalRepository(null), bundle);
		Date emptyIntervalStartDate = null;
		Date emptyIntervalEndDate = null;
		
		if (files != null && files.size()>0) {
			populated = true;
			deferred = false;
			int size = files.size();
			for (int i = size-1; i >= 0; i--) {
				File file = files.get(i);
				if (!file.exists())
					continue;
				if (file.getName().endsWith( ".index")) {
					continue;
				}
				RavenhurstBundle bundle;
				try {
					bundle = bundleExpert.decode(file);
				} catch (RavenhurstException e) {
					String msg="cannot decode file [" + file.getAbsolutePath() + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
					ArtifactContainerPlugin.getInstance().log(status);	
					continue;
				}
				RavenhurstResponse response = bundle.getRavenhurstResponse();
				Date responseDate = response.getResponseDate();
				
				List<Artifact> touchedArtifacts = response.getTouchedArtifacts();
				
				Date intervalDate = responseDate != null ? responseDate : bundle.getDate();
				
				if (touchedArtifacts == null || touchedArtifacts.size() == 0) {
					if (emptyIntervalEndDate == null) {
						emptyIntervalEndDate = intervalDate;
						if  (emptyIntervalStartDate == null) {
							emptyIntervalStartDate = intervalDate;
						}
					}
					else {
						emptyIntervalStartDate = intervalDate;
					}					
					continue;
				}								
				
			
				// if we have an empty interval stored, add an entry with it
				if (emptyIntervalStartDate != null) {
					
					final Date startDate = emptyIntervalStartDate;
					final Date endDate = emptyIntervalEndDate;
					
					Runnable task = () -> {
					
						TreeItem item = new TreeItem( tree, SWT.NONE);
						List<String> texts = new ArrayList<String>();
						if (endDate == null) {
							texts.add( format.format( startDate));
						}
						else {
							if (endDate.equals( startDate)) {
								texts.add( format.format( startDate));
							}
							else {
								texts.add( format.format( startDate) + " - " + format.format( endDate));
							}
						}
						texts.add("");
						item.setText( texts.toArray( new String[0]));
						item.setImage( interogationImage);
						item.setData( TreeItemPainter.PainterKey.image.toString(), interogationImage);
					
					};
					
					display.asyncExec(task);
					
					emptyIntervalStartDate = null; 
					emptyIntervalEndDate = null;
				}
				
				// create entry with data 
				Runnable task = () -> {
					TreeItem item = new TreeItem( tree,SWT.NONE);
					List<String> texts = new ArrayList<String>();
					if (responseDate != null) {
						texts.add( format.format( responseDate));
					}
					else {				
						texts.add( "'" + format.format( bundle.getDate()) + "'");
					}
					texts.add("");
					item.setText( texts.toArray( new String[0]));
					item.setImage(updateInfoImage);
					item.setData( TreeItemPainter.PainterKey.image.toString(), updateInfoImage);
					
					if (interactive) {
						item.setData(DATAKEY_ARTIFACTS, touchedArtifacts);					
						TreeItem dummyItem = new TreeItem( item, SWT.NONE);
						dummyItem.setText( MARKER_DEFERRED);					
						return;
					}
					
					buildEntriesForTouchedArtifacts(touchedArtifacts, item);
				};
				display.asyncExec(task);
				
			}
			if (emptyIntervalEndDate != null) {
				final Date startDate = emptyIntervalStartDate;
				final Date endDate = emptyIntervalEndDate;
			
				Runnable task = () -> {
					TreeItem item = new TreeItem( tree, SWT.NONE);
					List<String> texts = new ArrayList<String>();
					if (startDate == null) {
						texts.add( format.format( endDate));
					}
					else {
						texts.add( format.format( startDate) + " - " + format.format( endDate));
					}
					texts.add("");
					item.setText( texts.toArray( new String[0]));
					item.setImage( interogationImage);
					item.setData( TreeItemPainter.PainterKey.image.toString(), interogationImage);
				};
				
				display.asyncExec(task);
				
				emptyIntervalStartDate = null; 
				emptyIntervalEndDate = null;
				
			}
		}
	}

	private void buildEntriesForTouchedArtifacts(List<Artifact> touchedArtifacts, TreeItem item) {
		for (Artifact artifact : touchedArtifacts) {
			TreeItem artifactItem = new TreeItem( item, SWT.NONE);
			List<String> artifactTexts = new ArrayList<String>();
			artifactTexts.add("");
			artifactTexts.add( artifact.getArtifactId());
			artifactTexts.add( artifact.getGroupId());
			artifactTexts.add( artifact.getVersion());				
			artifactItem.setText( artifactTexts.toArray( new String[0]));					
		}
	}

	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();
		result.add( DATAKEY_ARTIFACTS);		
		return result;
	}
	
	@Override
	public void handleTreeEvent(Event event) {
		final TreeItem item = (TreeItem) event.item;
		TreeItem [] children = item.getItems();
		if (
			(children != null) &&
			(children.length == 1) 
		   ) {
			final TreeItem suspect = children[0];
			String name = suspect.getText(0);
			if (name.equalsIgnoreCase( MARKER_DEFERRED) == false)
				return;
			// 
			display.asyncExec( new Runnable() {
				
				@Override
				public void run() {
					suspect.dispose();
					@SuppressWarnings("unchecked")
					List<Artifact> touchedArtifacts = (List<Artifact>) item.getData( DATAKEY_ARTIFACTS);
					buildEntriesForTouchedArtifacts(touchedArtifacts, item);
				}
			});					
		}
	}

	@Override
	public void acknowledgeInterrogation(String id) {
		if (!repository.getName().equalsIgnoreCase(id)) {
			return;
		}
		if (visible && active) {
					
			display.asyncExec( new Runnable() {
				@Override
				public void run() {
					tree.removeAll();
					populated = false;
					populate(true);						
				}			
			});
			
		} 
		else {
			deferred = true;
		}
	}
	
	public boolean canPurge() {
		//return tree.getItemCount() > 0;
		return true;
	}

	/**
	 * purge the data in the ravenhurst information files acc the prune period in the preferences 
	 */
	public void purge() {
		int days = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getRavenhurstPreferences().getPrunePeriod();
		List<String> msgs = RavenhurstPersistenceHelper.purge( reader, bundle, days);		
		// post process error msgs .. 
		if (!msgs.isEmpty()) {
			for (String msg : msgs) {
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);
			}
		}

		tree.removeAll();
		populated = false;
		// clear
		populate();
	}


		
	public void purgeMetadata() {
		RepositoryReflectionHelper.purgeMetadata( this.contract.lockFactory(), new File( reader.getLocalRepository(null)), Collections.singletonList( bundle));
	}
	
	public void purgeIndex() {		
		RepositoryReflectionHelper.purgeIndex( this.contract.lockFactory(), new File( reader.getLocalRepository(null)), Collections.singletonList( bundle));
	}

	public void rebuildIndex() {
		List<String> grps = MalaclypseWirings.basicClasspathResolverContract().contract().repositoryReflection().correctLocalRepositoryStateOf( bundle);
		String repo = bundle.getRepositoryId();
		if (grps.size() > 0) {
			String msg = "rebuild of [" + repo + "] detected the following missed groups [" + grps.stream().collect(Collectors.joining(",")) + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		else {
			String msg = "rebuild of repo [" + repo + "] detected no missed groups";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}
}
