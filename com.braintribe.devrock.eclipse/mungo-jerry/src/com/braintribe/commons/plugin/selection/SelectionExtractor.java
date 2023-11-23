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
package com.braintribe.commons.plugin.selection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.CheapPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;

/**
 * helper class to extract selections from the package explorer<br/> 
 * analyzes {@link ISelection} retrieved from the package explorer to extract {@link IProject} or {@link IWorkingSet} instances  
 * @author pit
 *
 */
public class SelectionExtractor {
	private static Logger log = Logger.getLogger(SelectionExtractor.class);
	
	
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
		
				if (element instanceof IProject) {
					return (IProject) element;					
				} else if (element instanceof IAdaptable) {
					
						IPackageFragment fragment = ((IAdaptable) element).getAdapter( IPackageFragment.class);
						if (fragment != null) {
							project = fragment.getJavaProject().getProject();
							if (project != null) {
								return project;
							}
						}
						IResource resource = ((IAdaptable) element).getAdapter( IResource.class);
						if (resource != null) {
							project = resource.getProject();
							if (project != null) {
								return project;
							}
						}
						IWorkbenchAdapter workbenchAdapter = ((IAdaptable) element).getAdapter( IWorkbenchAdapter.class);
						if (workbenchAdapter != null) {
							Object obj = workbenchAdapter.getParent( element);
							if (obj instanceof IJavaProject) {
								project = ((IJavaProject) obj).getProject();
								if (project != null) {									
									return project;
								}
							}
						}
					
					project = ((IAdaptable) element).getAdapter(IProject.class);
					if (project != null) {
						return project;
					}
				} 
			}
		}
		return null;
	}
	
	public static Set<IProject> extractSelectedProjects() {
		return extractSelectedProjects( getCurrentPackageExplorerSelection());
	}
	
	public static Set<IProject> extractSelectedProjects(ISelection selection) {		
		Set<IProject> projects = new HashSet<IProject>();
		
		if (selection instanceof IStructuredSelection) {
				for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
					Object element = it.next();
			
					if (element instanceof IProject) {
						projects.add((IProject) element);						
					} else if (element instanceof IAdaptable) {
						
							IPackageFragment fragment = ((IAdaptable) element).getAdapter( IPackageFragment.class);
							if (fragment != null) {
								projects.add(fragment.getJavaProject().getProject());
								continue;
							}
							IResource resource = ((IAdaptable) element).getAdapter( IResource.class);
							if (resource != null) {
								projects.add( resource.getProject());
								continue;
							}
							IWorkbenchAdapter workbenchAdapter = ((IAdaptable) element).getAdapter( IWorkbenchAdapter.class);
							if (workbenchAdapter != null) {
								Object obj = workbenchAdapter.getParent( element);
								if (obj instanceof IJavaProject) {
									projects.add(((IJavaProject) obj).getProject());
									continue;
								}
							}
						
						IProject project = ((IAdaptable) element).getAdapter(IProject.class);
						if (project != null) {
							projects.add(project);
						}
					} 
				}
			}
		  return projects;
	}
	public static IWorkingSet extractSelectedWorkingSet() {
		return extractSelectedWorkingSet(getCurrentPackageExplorerSelection());
	}
	
	/**
	 * return the selected or deduced {@link IWorkingSet} if any  
	 */
	public static IWorkingSet extractSelectedWorkingSet(ISelection selection) {
		SelectionTuple tuple = extractSelectionTuple(selection);
		if (tuple == null)
			return null;
		return tuple.workingSet;
	}
	
	
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
				IProject project = adaptable.getAdapter(IProject.class);
				if (project != null && project == selectedProject) {
					selectionTuple.workingSet = workingSet;
					return selectionTuple;
				}
			}
		}		
		return null;		
	}
	
	private static Artifact extractArtifactFromProject( IProject project) {
		IResource pomResource = project.findMember("pom.xml");
		if (pomResource == null) {
			return null;
		}
		File file = pomResource.getLocation().toFile();
		Artifact artifact;
		try {
			artifact = CheapPomReader.identifyPom( file);
			return artifact;
		} catch (PomReaderException e) {
			return null;
		}
	}
	

	
	public static Artifact extractSelectedArtifact(ISelection selection) {
		
		// collect any directly selected project
		// or collect any project select per fragment (jar)
		
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
		
				if (element instanceof IProject) {
					return extractArtifactFromProject((IProject) element);
					
				} else if (element instanceof IAdaptable) {
					
						IAdaptable iAdaptable = (IAdaptable) element;
						IProject project = iAdaptable.getAdapter(IProject.class);
						if (project != null) {
							return extractArtifactFromProject(project);
						}
						
						IWorkbenchAdapter workbenchAdapter = iAdaptable.getAdapter( IWorkbenchAdapter.class);
						if (workbenchAdapter != null) {
							Object obj = workbenchAdapter.getParent( element);
							if (obj instanceof IJavaProject) {
								return extractArtifactFromProject(((IJavaProject) obj).getProject());
							}
						}
						/*
						IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot)iAdaptable.getAdapter( IPackageFragmentRoot.class);
						if (fragmentRoot != null) {							
							File file = new File( fragmentRoot.getPath().toOSString());
							if (file.exists() == false)
								continue;
							String name = file.getName();
							int index = name.lastIndexOf( ".");
							String pomName = name.substring( 0, index) + ".pom";
							File pomFile = new File( file.getParentFile(), pomName);
							try {
								Artifact artifact = CheapPomReader.identifyPom( pomFile);
								return artifact;
							} catch (PomReaderException e) {
							}
						}
						*/											
						IResource resource = iAdaptable.getAdapter( IResource.class);
						if (resource != null) {
							return extractArtifactFromProject(resource.getProject());
						}
						IPackageFragment fragment = iAdaptable.getAdapter( IPackageFragment.class);
						if (fragment != null) {
							return extractArtifactFromProject( fragment.getJavaProject().getProject());							
						}
				} 
			}
		}
		
		return null;
	}
	
	public static Map<String, Artifact> extractProjects( List<String> jars, String workingCopy) {
		Map<String, Artifact> projects = new HashMap<String, Artifact>();
		for (String jar : jars) {
			// 
			// convert string reference of jar file to solution 
			// add pom to solution.
			File file = new File( jar);
			if (file.exists() == false)
				continue;
			String name = file.getName();
			int index = name.lastIndexOf( ".");
			String pomName = name.substring( 0, index) + ".pom";
			File pomFile = new File( file.getParentFile(), pomName);
			try {
				Artifact artifact = CheapPomReader.identifyPom( pomFile);
				String projectDirName = NameParser.buildPartialPath( artifact, artifact.getVersion(), workingCopy);
				File projectDir = new File( projectDirName);
				File projectFile = new File( projectDir, ".project");
				if (projectFile.exists() == false) {
					String msg = "No project found relating to [" + file.getAbsolutePath() + "]";
					log.warn( msg);
					Mungojerry.log( Status.WARNING, msg);
					continue;
				}
				projects.put( projectFile.getAbsolutePath(), artifact);
			} catch (Exception e) {
				String msg = "Cannot extract project for [" + file.getAbsolutePath() + "]";
				log.error( msg, e);
				Mungojerry.log( Status.ERROR,  msg);
			} 			
		}
		return projects;
	}
	
	public static PackageExplorerSelectedJarsTuple extractSelectedJars() {
		ISelection selection = getCurrentPackageExplorerSelection();
		return extractSelectedJars(selection);		
	}

	public static ISelection getCurrentPackageExplorerSelection() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();
		return selection;
	}

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
}
