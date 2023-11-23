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
package com.braintribe.devrock.eclipse.model.reason.devrock;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * not a real issue, just to reflect the fact that AC chose to use a different version as requested
 * Should only occur in case of debug-module projects, where all dependencies have direct (non-ranged) versions,
 * and it most cases do not match what is in the workspace (see ac for more)
 * @author pit
 *
 */
@SelectiveInformation("requested version ${requestedVersion}, actual version of project ${actualVersion}")
public interface ProjectNonPerfectMatch extends PluginReason {
	
	EntityType<ProjectNonPerfectMatch> T = EntityTypes.T(ProjectNonPerfectMatch.class);

	String actualVersion = "actualVersion";
	String requestedVersion = "requestedVersion";
	
	/**
	 * @return - the current version of the project 
	 */
	String getActualVersion();
	void setActualVersion(String value);

	/**
	 * @return - the version as requested
	 */
	String getRequestedVersion();
	void setRequestedVersion(String value);

	

}
