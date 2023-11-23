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
package com.braintribe.devrock.artifactcontainer.commands.dynamic.purge;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.wire.api.context.WireContext;

public class IndicesSubmenu extends ContributionItem {
	
	private Map<MenuItem, Pair<PurgeAction,List<String>>> args = new HashMap<>();
	private String localRepository;
	private LockFactory lockFactory;
	private PurgeAction purgeAction;
	
	private Map<PurgeAction,String> actionToTextMap = new HashMap<>();
	private Map<PurgeAction,String> actionToTooltipMap = new HashMap<>();
	
	{
		
		actionToTextMap.put( PurgeAction.rebuild, "Rebuilds the index of ");
		actionToTooltipMap.put( PurgeAction.rebuild, "Updates the .index file, identifies missed groups and cleans the affected maven-metadata.xml files for");
		
		actionToTextMap.put( PurgeAction.all, "Clean all indices of ");
		actionToTooltipMap.put( PurgeAction.all, "cleans the .index file and associated maven-metadata.xml files of ");
		
		actionToTextMap.put( PurgeAction.index, "Clean RH indices of ");
		actionToTooltipMap.put( PurgeAction.index, "cleans the .index files of ");
		
		actionToTextMap.put( PurgeAction.metadata, "Clean maven-metadata indices of ");
		actionToTooltipMap.put( PurgeAction.metadata, "cleans the associated maven-metadata.xml files of ");
		
		
	}
	
	protected IndicesSubmenu( PurgeAction action) {
		this.purgeAction = action;		
	}
		
	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		 public void widgetSelected(SelectionEvent e) {
			Pair<PurgeAction,List<String>> arg = args.get(e.widget);
			if (arg != null) {
				System.out.println("processing [" + arg.second.stream().collect(Collectors.joining(","))  + " : " + arg.first.toString());
			}
			// call processor 
			switch (arg.first) {
			case rebuild:
				rebuildIndex( arg.second);
				break;
			case all:
					purgeIndex( arg.second);
					purgeMetadata( arg.second);
				break;
			case index:
				purgeIndex( arg.second);
				break;
			case metadata:
				purgeMetadata( arg.second);
				break;
			default:
				break;
			
			}
		 }
	};
	

	@Override
	public void fill(Menu menu, int index) {

		WireContext<ClasspathResolverContract> resolverContext = MalaclypseWirings.basicClasspathResolverContract();
		
		MavenSettingsReader settingsReader = resolverContext.contract().settingsReader();
		localRepository = settingsReader.getLocalRepository();
		lockFactory = resolverContext.contract().lockFactory();
		
		List<RemoteRepository> activeRemoteRepositories = settingsReader.getActiveRemoteRepositories();
		
		List<String> repos = activeRemoteRepositories.stream().map( r -> r.getName()).sorted().collect( Collectors.toList());
			
		String repoNames = repos.stream().collect(Collectors.joining(","));
				
	
		MenuItem miAllAny = new MenuItem(menu, SWT.CHECK, index);
	    miAllAny.setText( actionToTextMap.get( purgeAction) + repoNames);
	    miAllAny.setToolTipText( actionToTooltipMap.get( purgeAction) + repoNames);
	    args.put( miAllAny, Pair.of( purgeAction,repos));
	    miAllAny.addSelectionListener(selectionAdapter);
	    
	    new MenuItem(menu, SWT.SEPARATOR, index);
	    

	    for (String repo : repos) {	  
	    	MenuItem mi = new MenuItem(menu, SWT.CHECK, index);
		    mi.setText( actionToTextMap.get( purgeAction) + repo);
		    mi.setToolTipText( actionToTooltipMap.get( purgeAction) + repo);
		    args.put( mi, Pair.of( purgeAction, Collections.singletonList(repo)));
		    mi.addSelectionListener(selectionAdapter);		    		
	    }
	    
	    
	    
	}

	
	protected void rebuildIndex(List<String> reposToPurge) {
		RepositoryReflection reflection = MalaclypseWirings.basicClasspathResolverContract().contract().repositoryReflection();						
		for (String repo : reposToPurge) {
			List<String> grps = reflection.correctLocalRepositoryStateOf( repo);
			if (grps.size() > 0) {
				String msg = "rebuild of [" + repo + "] detected the following missed groups [" + grps.stream().collect(Collectors.joining(",")) + "]";
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
				ArtifactContainerPlugin.getInstance().log(status);	
			}
			else {
				String msg = "rebuild of repo [" + repo + "] detected no missed groups ";
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
				ArtifactContainerPlugin.getInstance().log(status);	
			}
		}
		
	}


	protected void purgeMetadata(List<String> repos) {		
		RepositoryReflectionHelper.purgeMetadataByName( lockFactory, new File( localRepository), repos);
	}


	protected void purgeIndex(List<String> repos) {
		RepositoryReflectionHelper.purgeIndexByName( lockFactory, new File( localRepository), repos);
	}
	
	

}
