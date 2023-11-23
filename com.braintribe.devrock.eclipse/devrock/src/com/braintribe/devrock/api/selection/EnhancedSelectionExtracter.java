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
package com.braintribe.devrock.api.selection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * enhanced version of the {@link SelectionExtracter}, adds features to select higher-level entities
 * such as {@link EnhancedCompiledArtifactIdentification}. All {@link EnhancedCompiledArtifactIdentification} are actually 
 * read from the pom on the disk, so their version will be accurate 
 * 
 * @author pit
 *
 */
public class EnhancedSelectionExtracter extends SelectionExtracter {
	private static Logger log = Logger.getLogger(EnhancedSelectionExtracter.class);
	
	/**
	 * a selection extracter that splits a multiple selection into single selection, and then,
	 * for each single selection, tries to first extract a container entry from it and if that fails
	 * tries to extract an artifact from it. 
	 * @param selection - the selection 
	 * @return
	 */
	public static List<EnhancedCompiledArtifactIdentification> extractEitherJarEntriesOrOwnerArtifacts(ISelection selection) {
		List<EnhancedCompiledArtifactIdentification> result = new ArrayList<>();
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			@SuppressWarnings("rawtypes")
			Iterator iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				Object next = iterator.next();
				IStructuredSelection singleSelection = new StructuredSelection(next);
				List<EnhancedCompiledArtifactIdentification> jars = extractSelectedJars(singleSelection);
				if (jars == null || jars.isEmpty()) {
					List<EnhancedCompiledArtifactIdentification> artifacts = extractSelectedArtifacts(singleSelection);
					if (artifacts != null && !artifacts.isEmpty()) {
						result.add( artifacts.get(0));
					}
				}
				else {
					result.add( jars.get(0));
				}
			}
		}
		else {
			return extractSelectedJars(selection);
		}
		
		return result;
	}
	
	/**
	 * extracts the JARs (i.e. entries from a container)
	 * @param selection - the {@link ISelection}
	 * @return - a {@link List} of {@link VersionedArtifactIdentification} of the selected jars 
	 */
	public static List<EnhancedCompiledArtifactIdentification> extractSelectedJars(ISelection selection) {
		Map<IProject, List<String>> projectToSelectedJarMap = new HashMap<>();
		
		// iterate over the selected 'parent' projects, i.e. the owners of the selections (container entry or other)
		  if (selection instanceof IStructuredSelection) {
			  for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
					Object element = it.next();
					if (element instanceof IAdaptable) {
						
						IPackageFragmentRoot fragment = ((IAdaptable) element).getAdapter( IPackageFragmentRoot.class);
						if (fragment != null) {
							// 							
							IPath path = fragment.getPath();
							
							IProject parentProject = fragment.getJavaProject().getProject();
							String jarPath = path.toOSString();
							List<String> jars = projectToSelectedJarMap.computeIfAbsent( parentProject, pp -> new ArrayList<>());
							jars.add( jarPath);																								
						}
					}
			  }
		  }
		  else {
			  String msg = "passed selection is not a valid structured selection. Cannot identify jars";
			  log.warn(msg);
			  return Collections.emptyList();
		  }
		  
		  List<EnhancedCompiledArtifactIdentification> result = new ArrayList<>();
		  
		  // as project information is not of interest, this part here is shorter 
		  for (Map.Entry<IProject, List<String>> entry : projectToSelectedJarMap.entrySet()) {
			  result.addAll(transposeToEnhancedCompileArtifactIdentification( entry.getValue()));
		  }		  		   
		  return result;
	}
	
	/**
	 * gets a list of jar files (as String), then looks for the .pom in the same directory, reads it and builds an {@link EnhancedCompiledArtifactIdentification} 
	 * @param jarPaths - a list of the jar files, full absolute paths 
	 * @return - a {@link List} of {@link EnhancedCompiledArtifactIdentification}
	 */
	private static List<EnhancedCompiledArtifactIdentification> transposeToEnhancedCompileArtifactIdentification(List<String> jarPaths) {
		List<EnhancedCompiledArtifactIdentification> result = new ArrayList<>( jarPaths.size());
		for (String jar : jarPaths) {
			File jarFile = new File( jar);			
			File parentFile = jarFile.getParentFile();
			
			File pomFile = null;
			File [] files = parentFile.listFiles();
			for (File file : files) {
				if (file.getName().endsWith("pom")) {
					pomFile = file;
					break;
				}
			}
			if (pomFile == null) {
				return result;
			}
						
			Maybe<CompiledArtifact> pEcai = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pomFile);
			if (pEcai.isEmpty()) {
				continue;
			}			
			result.add( EnhancedCompiledArtifactIdentification.from( pEcai.get()));					
		}				
		return result;
	}
	
	/**
	 * extracts projects OR jars in a container
	 * @param selection - the {@link ISelection}
	 * @return - a {@link List} of {@link VersionedArtifactIdentification}
	 */
	public static List<EnhancedCompiledArtifactIdentification> extractSelectedArtifacts(ISelection selection) {		
		List<EnhancedCompiledArtifactIdentification> artifacts = new ArrayList<>();
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
		
				// test if the selected adapter is a IProject
				IProject project = adaptable.getAdapter( IProject.class);
				if (project != null) {
					File projectDir = project.getLocation().toFile();					
					File pomFile = new File( projectDir, "pom.xml");											
					if (pomFile.exists()) {
						Maybe<CompiledArtifact> identificationPotential = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pomFile);
						if (identificationPotential.isUnsatisfied()) {
							String msg = "cannot read artifact from pom [" + pomFile.getAbsolutePath() + "] " + identificationPotential.whyUnsatisfied().stringify();
							log.warn(msg);
							DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.WARNING);
							DevrockPlugin.instance().log(status);
							continue;
						}
						EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.from(identificationPotential.get()); 

						artifacts.add( ecai);						
					}					
				}
				else {
					// test if it's a fragment ... 
					IPackageFragmentRoot fragment = adaptable.getAdapter( IPackageFragmentRoot.class);
					if (fragment == null) {
						continue;
					}
					IPath path = fragment.getPath();
					if (log.isDebugEnabled()) {
						log.debug("Fragment found at [" + path.toOSString() + "]");
					}

					File jarFile = new File( path.toOSString());
					String jarName = jarFile.getName();
					String pomName = jarName.substring(0, jarName.lastIndexOf('.')) + ".pom";
					File pomFile = new File( jarFile.getParentFile(), pomName);
					
					if (pomFile.exists()) {
						Maybe<CompiledArtifact> identificationPotential = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pomFile);
						if (identificationPotential.isUnsatisfied()) {
							String msg = "cannot read artifact from pom [" + pomFile.getAbsolutePath() + "] " + identificationPotential.whyUnsatisfied().stringify();
							log.warn(msg);
							DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.WARNING);
							DevrockPlugin.instance().log(status);
							continue;
						}
						EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.from(identificationPotential.get());

						artifacts.add( ecai);						
					}					
				}
			}		
		}
		return artifacts;
	}
}
