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
package com.braintribe.plugin.commons.selection;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.quickImport.ui.HasQuickImportTokens;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.SourceArtifact;

/**
 * helper class to extract selections from the package explorer<br/> 
 * analyzes {@link ISelection} retrieved from the package explorer to extract {@link IProject} or {@link IWorkingSet} instances  
 * @author pit
 *
 */
public class SelectionExtractor implements HasQuickImportTokens{
	private static Logger log = Logger.getLogger(SelectionExtractor.class);
	
	
	/**
	 * gets the {@link IProject} currently selected in the PackageExplorer
	 * @return - the currently selected {@link IProject}
	 */
	public static IProject extractSelectedProject() {
		return extractSelectedProject(getCurrentPackageExplorerSelection());
	}
	
	/**
	 * return the select {@link IProject} from the {@link ISelection} or null if unable 
	 * @param selection - the {@link ISelection} as received from the package explorer
	 * @return - the {@link IProject} selected (or deduced) or null if unable
	 */
	public static IProject extractSelectedProject( ISelection selection) {		
		IProject project;
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				
				project = extractProjectFromIAdaptable(element);
				if (project != null)
					return project;							
			}
		}
		return null;
	}
		
	
	
	/**
	 * @param element - a member data of the {@link ISelection}
	 * @return - an {@link IProject} if possible
	 */
	private static IProject extractProjectFromIAdaptable( Object element) {
		if (element instanceof IAdaptable == false) {
			return null;
		}
		IAdaptable iAdaptable = (IAdaptable) element;
		
		IProject project = null;

		// project itself
			project = iAdaptable.getAdapter(IProject.class);
			if (project != null)
				return project;
			
			// package fragment
			IPackageFragment fragment = iAdaptable.getAdapter( IPackageFragment.class);
			if (fragment != null) {														
				project = fragment.getJavaProject().getProject();
				if (project != null) {
					return project;
				}					
			}
			// resource
			IResource resource = iAdaptable.getAdapter( IResource.class);
			if (resource != null) {
				project = resource.getProject();
				if (project != null) {
					return project;
				}
			}
			// workbench adapter
			IWorkbenchAdapter workbenchAdapter = iAdaptable.getAdapter( IWorkbenchAdapter.class);
			if (workbenchAdapter != null) {
				Object obj = workbenchAdapter.getParent( iAdaptable);
				if (obj instanceof IJavaProject) {
					project = ((IJavaProject) obj).getProject();
					if (project != null) {									
						return project;
					}
				}
				else if (obj instanceof IAdaptable) {
					return extractProjectFromIAdaptable( (IAdaptable) obj);
				}
			}							
		
		return project;
		
	}
	
	/**
	 * @return - a {@link Set} of all {@link IProject} selected in the PackageExplorer
	 */
	public static Set<IProject> extractSelectedProjects() {
		return extractSelectedProjects( getCurrentPackageExplorerSelection());
	}
	
	/**
	 * @param selection - the {@link ISelection}
	 * @return - a {@link Set} of all {@link IProject} selected in the {@link ISelection}
	 */
	public static Set<IProject> extractSelectedProjects(ISelection selection) {		
		Set<IProject> projects = new HashSet<IProject>();
		
		if (selection instanceof IStructuredSelection) {
				for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
					Object element = it.next();
					
					IProject project = extractProjectFromIAdaptable(element);
					if (project != null) {
						projects.add(project);
					}					
				}
			}
		  return projects;
	}
	/**
	 * @return - current {@link IWorkingSet} of the PackageExplorer
	 */
	public static IWorkingSet extractSelectedWorkingSet() {
		return extractSelectedWorkingSet(getCurrentPackageExplorerSelection());
	}
	
	/**
	 * return the selected or deduced {@link IWorkingSet} if any  	 
	 * @param selection - the {@link ISelection}
	 * @return - current {@link IWorkingSet} of the {@link ISelection}
	 */
	public static IWorkingSet extractSelectedWorkingSet(ISelection selection) {
		SelectionTuple tuple = extractSelectionTuple(selection);
		if (tuple == null)
			return null;
		return tuple.workingSet;
	}
	
	
	/**
	 * @param selection - the {@link ISelection}
	 * @return - a {@link SelectionTuple} of the current selected {@link IProject}, {@link IWorkingSet} pairing
	 */
	public static SelectionTuple extractSelectionTuple(ISelection selection) {
		// find if a working set is directly specified in the selection
		SelectionTuple selectionTuple = new SelectionTuple();
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				if (element instanceof IWorkingSet) {
					selectionTuple.workingSet = (IWorkingSet) element;
					return selectionTuple;
				}								
			}
		} 
		
		// 
		IProject selectedProject = extractSelectedProject(selection);
		selectionTuple.project = selectedProject;
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();
		if (workingSets == null || workingSets.length == 0)
			return selectionTuple;
		for (IWorkingSet workingSet : workingSets) {
			for (IAdaptable adaptable : workingSet.getElements()) {
				IJavaProject project = adaptable.getAdapter(IJavaProject.class);
				if (project != null && project.getProject() == selectedProject) {
					selectionTuple.workingSet = workingSet;
					return selectionTuple;
				}
			}
		}		
		return null;		
	}
	
	/**
	 * returns the artifact of a project if any 
	 * @param project - the {@link IProject}
	 * @return - the associated {@link Artifact} or null
	 */
	public static Artifact extractArtifactFromProject( IProject project) {
		IResource pomResource = project.findMember("pom.xml");
		if (pomResource == null) {
			return null;
		}
		File file = pomResource.getLocation().toFile();
		Artifact artifact;
		try {
			artifact = extractArtifactFromPom(file);
			return artifact;
		} catch (Exception e) {
			return null;
		}
	}
	

	
	/**
	 * extracts the currently selected IProject's artifact 
	 * @param selection - the {@link ISelection}
	 * @return - the associated {@link Artifact} or null
	 */
	public static Artifact extractSelectedArtifact(ISelection selection) {
		
		// collect any directly selected project
		// or collect any project select per fragment (jar)
		
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				
				IProject project = extractProjectFromIAdaptable(element);
				if (project != null) {
					return extractArtifactFromProject((IProject) element);
				}			
			}
		}
		
		return null;
	}
	/**
	 * extracts all artifacts from the current package explorer selection, all selected, no matter project or container entry  
	 * @return - a {@link List} of selected {@link Artifact}
	 */
	public static List<Artifact> extractSelectedArtifacts(){
		return extractSelectedArtifacts( getCurrentPackageExplorerSelection());
	}
	
	/**
	 * extracts all artifacts from the passed selection, all selected, no matter project or container entry
	 * @param selection the {@link ISelection}
	 * @return - a {@link List} of selected {@link Artifact}
	 */
	public static List<Artifact> extractSelectedArtifacts(ISelection selection) {
		String uuid = null;
		List<Artifact> artifacts = new ArrayList<Artifact>();
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				
				IAdaptable adaptable;
				if (element instanceof IAdaptable) {
					adaptable = (IAdaptable) element;
				}
				else {
					continue;
				}
		
				IProject project = adaptable.getAdapter( IProject.class);
				if (project != null) {
					Artifact artifact = extractArtifactFromProject( project);
					if (artifact != null) {
						artifacts.add(artifact);
						continue;
					}
				}
				else {
					IPackageFragmentRoot fragment = adaptable.getAdapter( IPackageFragmentRoot.class);
					if (fragment == null) {
						continue;
					}
					IPath path = fragment.getPath();
					if (log.isDebugEnabled()) {
						log.debug("Fragment found at [" + path.toOSString() + "]");
					}

					ArtifactPomReader pomReader = MalaclypseWirings.fullClasspathResolverContract().contract().pomReader();
					
					if (uuid == null) {
						uuid = UUID.randomUUID().toString();
					}
					File jarFile = new File( path.toOSString());
					String jarName = jarFile.getName();
					String pomName = jarName.substring(0, jarName.lastIndexOf('.')) + ".pom";
					File pomFile = new File( jarFile.getParentFile(), pomName);
					try {
						if (pomFile.exists()) {
							Solution pom = pomReader.readPom( uuid, pomFile);
							artifacts.add(pom);
						}
					} catch (PomReaderException e) {
							String msg = "cannot read artifact from pom [" + pomFile.getAbsolutePath() + "]";
							ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
							ArtifactContainerPlugin.getInstance().log(status);			
					}
				}
			}
		
		}
		return artifacts;
	}
	
	
	/**
	 * extracts the artifact linked to each top projects in the selection 
	 * @param selection - the {@link ISelection}
	 * @return - a {@link List} of selected {@link Artifact}
	 */
	public static List<Artifact> extractSelectedProjectsArtifact(ISelection selection) {
		
		// collect any directly selected project
		// or collect any project select per fragment (jar)
		List<Artifact> artifacts = new ArrayList<Artifact>();
		Set<IProject> projects = extractSelectedProjects(selection);
		if (projects != null) {
			for (IProject project : projects) {
				Artifact artifact = extractArtifactFromProject(project);
				if (artifact != null) {
					artifacts.add(artifact);			
				}
			}
		}
		
		return artifacts;
	}
	
	
	
	/**
	 * @param pomFile - the pom file 
	 * @return - the {@link Artifact} (i.e. a {@link Solution}) read from the pom
	 * @throws Exception - the {@link ArtifactPomReader} can't read it 
	 */
	public static Artifact extractArtifactFromPom( File pomFile) throws Exception {		
		ArtifactPomReader pomReader = MalaclypseWirings.fullClasspathResolverContract().contract().pomReader();
		Solution solution = pomReader.readPom( UUID.randomUUID().toString(), pomFile);	
		return solution;		
	}
	
	
	
	/**
	 * get a map of project file to artifact from the jar names passed
	 * 
	 * @param jars - an array of jar names
	 * @return - {@link Map} of file path to artifact
	 */
	public static Map<String, Artifact> extractProjects( String  ... jars) {
		
		Map<String, Artifact> projects = new HashMap<String, Artifact>();
		StringBuilder stringBuilder = new StringBuilder();

		Map<Identification, VersionRange> solutionToRange = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		for (String jar : jars) {
			// 
			// convert string reference of jar file to solution 
			// add pom to solution.
			Solution solution = ArtifactContainerPlugin.getArtifactContainerRegistry().getArtifactRelatedToJar(jar);
			if (solution == null) {
				continue;
			}
			if (stringBuilder.length() > 0) {
				stringBuilder.append("|");				
			}
			stringBuilder.append( solution.getGroupId() + ":" + solution.getArtifactId());			
			VersionRange range = VersionRangeProcessor.createfromVersion( solution.getVersion());
			range = VersionRangeProcessor.autoRangify(range);
			solutionToRange.put(solution, range);
		}
		ArtifactContainerPlugin artifactContainerPlugin = ArtifactContainerPlugin.getInstance();
		String string = stringBuilder.toString();
		List<SourceArtifact> sourceArtifacts = artifactContainerPlugin.getQuickImportScanController().runPartialSourceArtifactQuery( string);
	
		sourceArtifacts.forEach( s -> {
			try {
				Artifact artifact = Artifact.T.create();
				artifact.setGroupId( s.getGroupId());
				artifact.setArtifactId( s.getArtifactId());
				Version version = VersionProcessor.createFromString( s.getVersion());
				artifact.setVersion( version);

				VersionRange versionRange = solutionToRange.get( artifact);
				if (VersionRangeProcessor.matches(versionRange, version)) {
					
					File projectFile = PantherSelectionHelper.determineProjectFile( s);
					if (projectFile != null) {						
						projects.put( projectFile.getAbsolutePath(), artifact);
					}				
				}
			} catch (VersionProcessingException e) {
				String msg ="cannot version from source artifact's version [" + s.getVersion() + "]";
				log.error( msg, e);
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				artifactContainerPlugin.log(status);
			}
		});
		
		return projects;
	}

	
	
	/**
	 * @param jars
	 * @return
	 */
	public static List<DetailedProjectExtractionResult> extractProjectsWithDetails( String  ... jars) {
		
		List<DetailedProjectExtractionResult> result = new ArrayList<>();
		
		StringBuilder stringBuilder = new StringBuilder();

		Map<Identification, VersionRange> solutionToRange = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		Map<Identification, DetailedProjectExtractionResult> solutionToDetail = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		for (String jar : jars) {
			DetailedProjectExtractionResult detail = new DetailedProjectExtractionResult();
			detail.jar = jar;
			
			result.add( detail);

			// 
			// convert string reference of jar file to solution 
			// add pom to solution.
			Solution solution = ArtifactContainerPlugin.getArtifactContainerRegistry().getArtifactRelatedToJar(jar);
			if (solution == null) {
				continue;
			}
			detail.jarSolution = solution;
			if (stringBuilder.length() > 0) {
				stringBuilder.append("|");				
			}
			stringBuilder.append( solution.getGroupId() + ":" + solution.getArtifactId());			
			VersionRange range = VersionRangeProcessor.createfromVersion( solution.getVersion());
			range = VersionRangeProcessor.autoRangify(range);
			solutionToRange.put(solution, range);
			solutionToDetail.put( solution, detail);
		}
		ArtifactContainerPlugin artifactContainerPlugin = ArtifactContainerPlugin.getInstance();
		String string = stringBuilder.toString();
		List<SourceArtifact> sourceArtifacts = artifactContainerPlugin.getQuickImportScanController().runPartialSourceArtifactQuery( string);
	
		sourceArtifacts.forEach( s -> {
			try {
				Artifact artifact = Artifact.T.create();
				artifact.setGroupId( s.getGroupId());
				artifact.setArtifactId( s.getArtifactId());
				Version version = VersionProcessor.createFromString( s.getVersion());
				artifact.setVersion( version);
				
				DetailedProjectExtractionResult detail = solutionToDetail.get(artifact);
				if (detail == null) {
					String msg ="unexpectedly cannot find detail for [" + NameParser.buildName(artifact) + "]";
					log.error( msg);
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
					artifactContainerPlugin.log(status);
					throw new IllegalStateException(msg);
				}
				detail.extractedArtifact = artifact;

				VersionRange versionRange = solutionToRange.get( artifact);
				if (VersionRangeProcessor.matches(versionRange, version)) {
					
					File projectFile = PantherSelectionHelper.determineProjectFile( s);
					if (projectFile != null) {
						detail.extractedProject = projectFile.getAbsolutePath();
						
					}				
				}
			} catch (VersionProcessingException e) {
				String msg ="cannot version from source artifact's version [" + s.getVersion() + "]";
				log.error( msg, e);
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				artifactContainerPlugin.log(status);
			}
		});
		
		return result;
	}
	
	
	
	/**
	 * @return - all JAR entries currently selected anywhere in the PackageExplorer
	 */
	public static PackageExplorerSelectedJarsTuple extractSelectedJars() {
		ISelection selection = getCurrentPackageExplorerSelection();
		return extractSelectedJars(selection);		
	}
	
	

	/**
	 * @return - the {@link ISelection} active in the package explorer
	 */
	public static ISelection getCurrentPackageExplorerSelection() {		
		return ArtifactContainerPlugin.getInstance().getCurrentSelection();
	}
	
	/**
	 * no change to {@link SelectionExtractor#getCurrentPackageExplorerSelection()}, i.e. same behavior
	 * @return
	 */
	public static ISelection getCurrentPackageExplorerSelectionCrapped() {
		
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = iworkbenchwindow.getSelectionService();
		ISelection selection = selectionService.getSelection(); 
		return selection;
	}
	
	/**
	 * not functional, even if based on example, use {@link SelectionExtractor#getCurrentPackageExplorerSelection()}
	 * @return
	 */
	public static ISelection getCurrentPackageExplorerSelectionCrappedToo() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();

		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = iworkbenchwindow.getSelectionService();
		ISelection selection = selectionService.getSelection("org.eclipse.jdt.ui.PackageExplorer");
		return selection;
	}
	


	/**
	 * @param selection - the {@link ISelection}
	 * @return - all JAR entries currently selected anywhere in the PackageExplorer
	 */
	public static PackageExplorerSelectedJarsTuple extractSelectedJars(ISelection selection) {
		  
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkingSetManager manager = iworkbench.getWorkingSetManager();
		IWorkingSet [] workingSets = manager.getWorkingSets();
		IWorkingSet currentWorkingSet = null;
		  				 
		  List<String> jars = new ArrayList<String>();
		  if (selection instanceof IStructuredSelection) {
			  for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
					Object element = it.next();
					if (element instanceof IAdaptable) {
						
						IPackageFragmentRoot fragment = ((IAdaptable) element).getAdapter( IPackageFragmentRoot.class);
						if (fragment != null) {
							// 
							
							IPath path = fragment.getPath();
							if (log.isDebugEnabled()) {
								log.debug("Fragment found at [" + path.toOSString() + "]");
							}
							jars.add( path.toOSString());
			
							if (currentWorkingSet != null || workingSets == null || workingSets.length == 0)
								continue;
							IProject parentProject = fragment.getJavaProject().getProject();
							for (IWorkingSet workingSet : workingSets) {
								IAdaptable [] adaptables = workingSet.getElements();
								if (adaptables != null) {
									for (IAdaptable adaptable : adaptables) {
										IProject project = adaptable.getAdapter(IProject.class);
										if (project == parentProject) {
											currentWorkingSet = workingSet;
											break;
										}
									}
								}
							}
						}
					}
			  }
		  }
		  PackageExplorerSelectedJarsTuple tuple = new PackageExplorerSelectedJarsTuple();
		  tuple.currentWorkingSet = currentWorkingSet;
		  tuple.selectedJars = jars;
		  return tuple;
	}
	
	/**
	 * determine the artifacts that are backing the jars passed 
	 * @param jars - {@link List} of JAR names (fully qualified files)
	 * @return - an {@link Array} of {@link Artifact}
	 */
	public static Collection<Artifact> extractArtifactsFromSelectedJars( List<String> jars) {
		String uuid = null;
		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (String jar : jars) {
			Map<String, Artifact> extractProjects = SelectionExtractor.extractProjects(jar);
			if (extractProjects != null && extractProjects.size() > 0) {
				artifacts.addAll( extractProjects.values());
			}
			else {
								
				ArtifactPomReader pomReader = MalaclypseWirings.fullClasspathResolverContract().contract().pomReader();
				
				if (uuid == null) {
					uuid = UUID.randomUUID().toString();
				}
				File jarFile = new File( jar);
				String jarName = jarFile.getName();
				String pomName = jarName.substring(0, jarName.lastIndexOf('.')) + ".pom";
				File pomFile = new File( jarFile.getParentFile(), pomName);
				try {
					if (pomFile.exists()) {
						Solution pom = pomReader.readPom( uuid, pomFile);
						artifacts.add(pom);
					}
				} catch (PomReaderException e) {
						String msg = "cannot read artifact from pom [" + pomFile.getAbsolutePath() + "]";
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
						ArtifactContainerPlugin.getInstance().log(status);			
				}
				
			}			
		}
		// 
		return artifacts;
	}
}
