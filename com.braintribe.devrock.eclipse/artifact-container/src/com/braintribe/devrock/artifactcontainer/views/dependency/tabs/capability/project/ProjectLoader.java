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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.devrock.artifactcontainer.control.project.listener.ProjectImportListener;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.plugin.commons.selection.PantherSelectionHelper;
import com.braintribe.plugin.commons.selection.TargetProvider;

/**
 * @author pit
 *
 */
public class ProjectLoader implements HasProjectLoadingTokens {
	private static Logger log = Logger.getLogger(ProjectLoader.class);
	/**
	 * load project backing artifact (or dependency if possible)
	 * @param targetProvider
	 * @param owner
	 * @param items
	 * @return
	 */
	public static Set<TreeItem> loadProjects(TargetProvider targetProvider, final ProjectLoadingCapable owner, TreeItem ... items) {
		
		if (items == null || items.length == 0) {
			return null;
		}
		Set<TreeItem> loadedItems = new HashSet<TreeItem>();
		List<ProjectImporterTuple> projects = new ArrayList<ProjectImporterTuple>( items.length);
		
		final Map<String, TreeItem> projectToItemMap = new HashMap<String, TreeItem>();
		for (TreeItem item : items) {
			
			Artifact artifact = null;
			File file = (File) item.getData( DATAKEY_PROJECT);
			if (file == null) {
				item.setChecked( false);
				item.setData( DATAKEY_PROJECT, null);
				// find matching source artifact 
				artifact = (Artifact) item.getData( DATAKEY_SOLUTION);			

				SourceArtifact matchingSourceArtifact = null;
				
				if (artifact == null) {
				
					Dependency dependency = (Dependency) item.getData( DATAKEY_DEPENDENCY);
					if (dependency != null) {
						matchingSourceArtifact = determineSourceArtifactFromDependency( dependency);
					}
													
				}
				else  {
					matchingSourceArtifact = determineSourceArtifactFromArtifact( artifact);
				}
				if (matchingSourceArtifact == null) {
					continue;
				}
				
				file = PantherSelectionHelper.determineProjectFile(matchingSourceArtifact);
				artifact = Artifact.T.create();
				artifact.setGroupId( matchingSourceArtifact.getGroupId());
				artifact.setArtifactId( matchingSourceArtifact.getArtifactId());
				artifact.setVersion( VersionProcessor.createFromString( matchingSourceArtifact.getVersion()));	
				if (file == null) {
					continue;
				}
			}
			else {
				projectToItemMap.put( file.getName(), item);
				artifact = (Artifact) item.getData( DATAKEY_SOLUTION);
			}
			
			ProjectImporterTuple tuple = new ProjectImporterTuple(file.getAbsolutePath(), artifact);
			projects.add( tuple);
			loadedItems.add(item);
		}
		
		ProjectImportListener listener = new ProjectImportListener() {
			
			@Override
			public void acknowledgeImportedProject(ProjectImporterTuple tuple) {
				IProject project = tuple.getProject();
				TreeItem item = projectToItemMap.get( project.getName());
				if (item != null) {
					owner.acknowledgeProjectImport( item);
				}				
			}
		};
		
		ProjectImporter.importProjects( false, targetProvider, listener, projects.toArray( new ProjectImporterTuple[0]));
		return loadedItems;
	}
	
	/**
	 * TODO: modify to support publishing scheme (i.e. request #1.0.1 but find #1.0.2-PC).. see jarImport feature
	 * @param artifact
	 * @return
	 */
	private static SourceArtifact determineSourceArtifactFromArtifact(Artifact artifact) {
		SourceArtifact matchingSourceArtifact = null;
		List<SourceArtifact> sourceArtifacts = null;
		String query = artifact.getGroupId() + ":" + artifact.getArtifactId();
		
		 
		final VersionRange range = VersionRangeProcessor.autoRangify( VersionRangeProcessor.createfromVersion( artifact.getVersion()));	
		
		sourceArtifacts = ArtifactContainerPlugin.getInstance().getQuickImportScanController().runPartialSourceArtifactQuery( query);
		if (sourceArtifacts.size() > 0) {
			Map<Version, SourceArtifact> matchingArtifacts = sourceArtifacts.stream().filter( s -> {
					try {
						Version version = VersionProcessor.createFromString( s.getVersion());							
						if (VersionRangeProcessor.matches(range, version)) {
							return true;
						}
					} catch (VersionProcessingException e) {
						String msg ="cannot version from source artifact's version [" + s.getVersion() + "]";
						log.error( msg, e);					
					}
					return false;
			}).collect( Collectors.toMap( 
					new Function<SourceArtifact, Version>() {
						public Version apply( SourceArtifact s) { return VersionProcessor.createFromString( s.getVersion());}
					},
					Function.<SourceArtifact>identity()));
							
			List<Version> sorted = matchingArtifacts.keySet().stream().sorted( VersionProcessor.comparator.reversed()).collect( Collectors.toList());
			Version highest = sorted.get( 0);
			matchingSourceArtifact = matchingArtifacts.get(highest);
		}
		return matchingSourceArtifact;
	}
		
	private static SourceArtifact determineSourceArtifactFromDependency(Dependency dependency) {
		SourceArtifact matchingSourceArtifact = null;
		
		VersionRange range = dependency.getVersionRange();
		
		List<SourceArtifact> sourceArtifacts = null;
		
		String query = dependency.getGroupId() + ":" + dependency.getArtifactId();
		if (!range.getInterval()) { 
			query += "#" + VersionRangeProcessor.toString( range);
			sourceArtifacts = ArtifactContainerPlugin.getInstance().getQuickImportScanController().runSourceArtifactQuery( query);
			if (sourceArtifacts.size() > 0) {
				matchingSourceArtifact = sourceArtifacts.get(0);
			}
		}
		else {
			
			sourceArtifacts = ArtifactContainerPlugin.getInstance().getQuickImportScanController().runPartialSourceArtifactQuery( query);
			
			// match source artifact to range
			List<Version> matchingVersions = new ArrayList<Version>();
			Map<Version, SourceArtifact> matchingSourceArtifacts = new HashMap<Version, SourceArtifact>();
			for (SourceArtifact sourceArtifact : sourceArtifacts) {
				Version version = VersionProcessor.createFromString( sourceArtifact.getVersion());
				if (VersionRangeProcessor.matches( range, version)) {
					matchingVersions.add( version);
					matchingSourceArtifacts.put( version, sourceArtifact);
				}
			}
			if (matchingVersions.size() > 0) {
				matchingVersions.sort( VersionProcessor::compare);
				matchingSourceArtifact = matchingSourceArtifacts.get( matchingVersions.get( matchingVersions.size()-1));
			}
		}
		
		return matchingSourceArtifact;

	}
	
	
	public static boolean isSolutionAvailable( Solution solution) {
		WorkspaceProjectRegistry registry = ArtifactContainerPlugin.getWorkspaceProjectRegistry();
		Artifact artifact = Artifact.T.create();
		ArtifactProcessor.transferIdentification(artifact, solution);
		artifact.setVersion( solution.getVersion());
		IProject project = registry.getProjectForArtifact(artifact);
		if (project != null) {
			return false;
		}
		return true;
	}
	
	public static boolean markSolutionAsAvailable( Solution solution, TreeItem item) {
		WorkspaceProjectRegistry registry = ArtifactContainerPlugin.getWorkspaceProjectRegistry();
		Artifact artifact = Artifact.T.create();
		ArtifactProcessor.transferIdentification(artifact, solution);
		artifact.setVersion( solution.getVersion());
		IProject project = registry.getProjectForArtifact(artifact);
		if (project != null) {
			return false;
		}
		String wc = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getSvnPreferences().getWorkingCopy();
		String actualWcPath = ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver().resolve(wc);
	
		String projectFile = NameParser.buildPartialPath(artifact, artifact.getVersion(), actualWcPath) + File.separator + ".project";			
		File file = new File( projectFile);
		if (file.exists()) {
			item.setData( DATAKEY_PROJECT, file);
			return true;
		}
		return false;
	}	
}
