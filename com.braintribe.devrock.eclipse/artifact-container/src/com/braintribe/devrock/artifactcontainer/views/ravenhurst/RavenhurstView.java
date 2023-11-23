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
package com.braintribe.devrock.artifactcontainer.views.ravenhurst;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.listener.RavenhurstNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.watcher.FileWatchNotifier;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs.RavenhurstViewTab;
import com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs.capability.clearindex.ClearIndexActionContainer;
import com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs.capability.clearmetadata.ClearMetadataActionContainer;
import com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs.capability.purge.InfoPurgeActionContainer;
import com.braintribe.devrock.artifactcontainer.views.ravenhurst.tabs.capability.rebuildindex.RebuildIndexActionContainer;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.plugin.commons.views.tabbed.AbstractTabbedView;


public class RavenhurstView extends AbstractTabbedView<RavenhurstViewTab> implements VirtualEnvironmentNotificationListener, RavenhurstNotificationListener {
	private static final String KEY_UPDATE_POLICY_VIEW = "ArtifactContainer's Update policy view";

	private Color background;
	private boolean inheritedVisibility = false;
	private FileWatchNotifier watchNotifier;
	private ClasspathResolverContract contract;
	
	
	public RavenhurstView() {
		viewKey = KEY_UPDATE_POLICY_VIEW;
		contract = MalaclypseWirings.fullClasspathResolverContract().contract();			
	}

	@Override
	protected void addTabs(Composite composite) {
		// hook to 
		VirtualEnvironmentPlugin.getInstance().addListener( this);
		
		background = composite.getBackground();
		setupWithMalaclypseScope(background, contract);
	}

	@Override
	protected void addActions() {		
		// purge action
		InfoPurgeActionContainer projectContainer = new InfoPurgeActionContainer();
		initViewActionContainer(projectContainer);
		actionControllers.add(projectContainer.create());
		
		
		RebuildIndexActionContainer rebuildContainer = new RebuildIndexActionContainer();
		initViewActionContainer( rebuildContainer);
		actionControllers.add( rebuildContainer.create());
		
		// clear metadata
		ClearMetadataActionContainer clearMetadataContainer = new ClearMetadataActionContainer();
		initViewActionContainer( clearMetadataContainer);
		actionControllers.add( clearMetadataContainer.create());
		// clear .index 
		ClearIndexActionContainer clearIndexContainer = new ClearIndexActionContainer();
		initViewActionContainer(clearIndexContainer);
		actionControllers.add( clearIndexContainer.create());
	}

	@Override
	public void dispose() {
		VirtualEnvironmentPlugin.getInstance().removeListener( this);
		super.dispose();
	}
	
	

	@Override
	public void acknowledgeVisibility(String key) {
		super.acknowledgeVisibility(key);
		inheritedVisibility = true;
	}

	@Override
	public void acknowledgeInvisibility(String key){ 
		super.acknowledgeInvisibility(key);
		inheritedVisibility = false;
	}

	@Override
	public void acknowledgeOverrideChange() {		
		// tear down
		teardown();
		// reset 
		setupWithMalaclypseScope(background, MalaclypseWirings.fullClasspathResolverContract().contract());
		
		tabFolder.setSelection(0);
		if (inheritedVisibility) {
			RavenhurstViewTab tab = indexToTabMap.get( 0);
			if (tab != null) {
				tab.acknowledgeActivation();
			}
		}

	}

	
	private void setupWithMalaclypseScope( Color background, ClasspathResolverContract contract) {
		
		if (watchNotifier != null) {
			watchNotifier.removeAll();
			watchNotifier = null;
		}
		
		MavenSettingsReader reader = contract.settingsReader();
		RavenhurstPersistenceExpertForMainDataContainer expert = contract.ravenhurstMainContainerExpert();
		RavenhurstScope ravenhurstScope = contract.ravenhurstScope();
		
		try {
			watchNotifier = new FileWatchNotifier( reader.getLocalRepository(null));		
		} catch (RepresentationException e) {
			String msg = "cannot instantiate FileWatchNotifier on Ravenhurst files";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);		
		}
		
		
		try {
			RavenhurstMainDataContainer decoded = expert.decode();
			
			setPartName( viewKey);
		
			Map<String, Date> urlToLastAccessMap = decoded.getUrlToLastAccessMap();
			List<RemoteRepository> activeRemoteRepositories = reader.getActiveRemoteRepositories();
		
			for (Entry<String, Date> entry : urlToLastAccessMap.entrySet()) {
				// find the id of the repository matching the url
				RemoteRepository repository = null;
			
				for (RemoteRepository remoteRepository : activeRemoteRepositories) {					
					if (!reader.isDynamicRepository(remoteRepository.getProfileName(), remoteRepository.getName(), remoteRepository.getUrl())) {
						continue;
					}
					if (remoteRepository.getUrl().equalsIgnoreCase( entry.getKey())) {
						repository = remoteRepository;
					}
				}
				RavenhurstBundle ravenhurstBundle = ravenhurstScope.getRavenhurstBundleByUrl( entry.getKey());
				
				// only active repositories are shown
				if (repository == null){
					continue;
				}
				RavenhurstViewTab ravenhurstTab = new RavenhurstViewTab(display, repository, contract, ravenhurstBundle);		
				if (inheritedVisibility) {
					ravenhurstTab.acknowledgeVisibility(viewKey);
				}
				else {
					ravenhurstTab.acknowledgeInvisibility(viewKey);
				}
				// add to watch notifier 
				watchNotifier.addRavenhurstNotificationListener( repository.getName(), ravenhurstTab);
				
				// attach ourself to get event notification 
				watchNotifier.addRavenhurstNotificationListener( repository.getName(), this);
				
				initAndTabToFolder( ravenhurstTab, repository.getName(), "Update data for repository [" + repository.getName() + "] @ [" + entry.getKey() + "]", background);		
			}
			
		} catch (RavenhurstException e) {
			String msg = "cannot read ravenhurst information";		
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);		
			
		} catch (RepresentationException e) {
			String msg = "cannot retrieve active repositories from settings";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}

	@Override
	public void acknowledgeInterrogation(String repositoryId) {			
		setTitleToolTip("last updated [" + new Date() + "[ for repository [" + repositoryId + "]");
	}
	
	
}
