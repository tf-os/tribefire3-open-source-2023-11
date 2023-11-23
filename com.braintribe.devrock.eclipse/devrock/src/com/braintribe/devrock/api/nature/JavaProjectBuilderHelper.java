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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InternalError;

/**
 * a helper to access a {@link IProject}'s builder configuration
 * 
 * @author pit
 *
 */
public class JavaProjectBuilderHelper {

	/**	 
	 * @param project - the {@link IProject} to check
	 * @param builderId - the fully qualified name of the builder (aka name of java class)
	 * @return - a {@link Maybe} with true or false
	 */
	public static Maybe<Boolean> hasBuilder(IProject project, String builderId) {
		Maybe<List<String>> builderMaybe = getBuilders(project);
		if (builderMaybe.isUnsatisfied()) {
			return Maybe.empty( builderMaybe.whyUnsatisfied());
		}
		List<String> builders = builderMaybe.get();
		if (builders.contains(builderId)) {
			return Maybe.complete(true);
		}
		return Maybe.complete(false);				
	}
	
	/**
	 * @param project - the {@link IProject} to check 
	 * @param builderIds - the fully qualified names of the builders to check
	 * @return - a {@link Maybe} with true or false 
	 */
	public static Maybe<Boolean> hasBuilders(IProject project, String ... builderIds ) {
		Maybe<List<String>> builderMaybe = getBuilders(project);
		if (builderMaybe.isUnsatisfied()) {
			return Maybe.empty( builderMaybe.whyUnsatisfied());
		}
		List<String> builders = builderMaybe.get();
		for (String builderId : builderIds) {
			if (builders.contains(builderId)) {
				return Maybe.complete(true);
			}
			
		}
		return Maybe.complete( false);
	}
	

	/**
	 * @param project - the {@link IProject} to check
	 * @return - a {@link Maybe} with a {@link List} of the fully qualified names of the builders attached
	 */
	public static Maybe<List<String>> getBuilders(IProject project) {
		List<String> result = new ArrayList<>();
		IProjectDescription desc;
		try {
			desc = project.getDescription();
		} catch (CoreException e) {
			InternalError ie = InternalError.from(e, "cannot access description of project: " + project.getName());
			return Maybe.empty( ie);
		}
		ICommand[] commands = desc.getBuildSpec();
		   for (int i = 0; i < commands.length; ++i) {		      
		      result.add( commands[i].getBuilderName());
		   }
		return Maybe.complete( result);
	}
}
