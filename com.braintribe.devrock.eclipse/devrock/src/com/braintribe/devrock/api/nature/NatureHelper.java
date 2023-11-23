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
package com.braintribe.devrock.api.nature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.common.potential.Potential;
import com.braintribe.devrock.eclipse.model.reason.devrock.NatureExtractionFailure;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;

/**
 * a collection of static methods that deal with natures 
 * @author pit
 *
 */
public class NatureHelper {
			
	/**
	 * add a nature to a project
	 * @param project - the {@link IProject}
	 * @param natureId - the String id of the nature 
	 * @return - true if the nature was successfully added, false otherwise
	 */
	public static boolean addNature(IProject project, String natureId) {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			if (natures.add( natureId)) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
			return true;
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * remove a nature from a project
	 * @param project - the {@link IProject}
	 * @param natureId - the String id of the nature 
	 * @return - true if the nature was successfully removed, false otherwise 
	 */
	public static boolean removeNature(IProject project, String natureId) {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			if (natures.remove( natureId)) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
			return true;
		} catch (CoreException e) {
			return false;
		}
	}
	
	/**
	 * @param project - the {@link IProject}
	 * @param natureIds - the ids of the natures to remove 
	 * @return - true if it worked.. 
	 */
	public static boolean removeNature(IProject project, String ... natureIds) {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			boolean changed = false;
			for (String natureId : natureIds) {			
				if (natures.remove( natureId)) {
					changed = true;
				}			
			}
			if (changed) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
			return true;
		} catch (CoreException e) {
			return false;
		}			
	}
	
	public static boolean hasAnyNatureOf( IProject project, String ... natures) {
		Map<String, Boolean> map = hasNatures(project, natures);
		for (Map.Entry<String, Boolean> entry : map.entrySet()) {
			if (entry.getValue()) 
				return true;
		}
		return false;
	}
		
	/**
	 * tests whether the project has the natures attached or not
	 * @param project -  the {@link IProject}
	 * @param natures - the String ids of the natures 
	 * @return - a Map with per nature id to presence, true if attached, false otherwise 
	 */
	public static Map<String, Boolean> hasNatures( IProject project, String ... natures) {
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		if (natures == null)
			return result;
		for (String nature : natures) {
			try {
				result.put(nature, project.getNature(nature) != null);
			} catch (CoreException e) {
				result.put(nature, false);
			}				
		}
		return result;
	}
	
	/**
	 * extracts the natures of a project 
	 * @param project - the {@link IProject} to get the natures from 
	 * @return - a {@link Potential} with a {@link List} of nature ids.
	 */
	public static Maybe<List<String>> getNatures(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			return Maybe.complete( Arrays.asList(description.getNatureIds()));
		} catch (CoreException e) {
			return Maybe.empty( Reasons.build( NatureExtractionFailure.T).text( "cannot extract natures of project [" + project.getName() + "]").toReason());
		}
	}
	
	/**
	 * determines if a project is marked as GWT project
	 * @param project - the {@link IProject}
	 * @return - true if it's marked as GWT (either library or terminal), false otherwise
	 */
	public static boolean isGwtArtifact(IProject project) {
		Maybe<List<String>> naturesOfProject = NatureHelper.getNatures(project);
		if (naturesOfProject.isSatisfied()) {
			List<String> natures = naturesOfProject.get();
			return natures.contains( CommonNatureIds.NATURE_GWT_LIBRARY) || natures.contains( CommonNatureIds.NATURE_GWT_TERMINAL);
		}		
		return false;
	}
	
	/**
	 * determines if a project is marked as a MODEL project
	 * @param project - the {@link IProject}
	 * @return - true if it's marked as a model, false otherwise
	 */
	public static boolean isModelArtifact(IProject project) {
		Maybe<List<String>> naturesOfProject = NatureHelper.getNatures(project);
		if (naturesOfProject.isSatisfied()) {
			List<String> natures = naturesOfProject.get();
			return natures.contains( CommonNatureIds.NATURE_MODEL);
		}		
		return false;
	}
	
	/**
	 * determines whether a project is marked as a DEBUG-MODULE project
	 * @param project - the {@link IProject}
	 * @return - true if it's marked as a debug module project, false otherwise
	 */
	public static boolean isDebugModuleArtifact(IProject project) {
		Maybe<List<String>> naturesOfProject = NatureHelper.getNatures(project);
		if (naturesOfProject.isSatisfied()) {
			List<String> natures = naturesOfProject.get();
			return natures.contains( CommonNatureIds.NATURE_DEBUG_MODULE);
		}		
		return false;
	}
	
	
	
	
	/**
	 * determines if a project is marked as a TOMCAT project
	 * @param project - the {@link IProject}
	 * @return - true if it's marked as a model, false otherwise
	 */
	public static boolean isTomcatArtifact(IProject project) {
		return hasAnyNatureOf(project, CommonNatureIds.NATURE_TOMCAT_NETSF, CommonNatureIds.NATURE_TOMCAT_SYSDEO);
	}

	
}
