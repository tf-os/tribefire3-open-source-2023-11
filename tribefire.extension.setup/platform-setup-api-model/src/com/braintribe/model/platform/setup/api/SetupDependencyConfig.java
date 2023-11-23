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
package com.braintribe.model.platform.setup.api;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface SetupDependencyConfig extends GenericEntity {
	EntityType<SetupDependencyConfig> T = EntityTypes.T(SetupDependencyConfig.class);

	@Description("Deprecated, use 'project' instead.")
	@Deprecated
	String getTerminalDependency();
	@Deprecated
	void setTerminalDependency(String terminalDependency);
	
	@Description(
			"The qualified name of the asset which is taken for setting up the project. The asset itself and its transitive dependencies are resolved and available in the setup access. " + 
			"Note that only dependencies reachable from 'setupDependency' are effective in the setup. Defaults to 'setupDependency'.")
	String getProject();
	void setProject(String project);
	
	@Description("The qualified name of the asset that should be transitively resolved to be effective in the setup."
			+ "The asset will be automatically ranged on the 2nd version level if there is no 3rd version level explicitly defined. "
			+ "Defaults to 'project'.")
	String getSetupDependency();
	void setSetupDependency(String setupDependency);
	
	@Description("Sets the runtime flag which can be evaluated by dependency selectors.")
	boolean getRuntime();
	void setRuntime(boolean runtime);
	
	@Description("Sets the stage name which can be evaluated by dependency selectors.")
	@Initializer("'dev'")
	String getStage();
	void setStage(String stage);
	
	@Description("Sets the tags which can be evaluated by dependency selectors.")
	Set<String> getTags();
	void setTags(Set<String> tags);

	/**
	 * If set to <code>true</code>, assets having a documentation nature will be excluded from the setup processing.
	 * 
	 * TODO: Remove initializer after ensuring stable and fast docs i.e. by additional CI verification
	 */
	@Description("Suppresses the compilation and integration of documentation assets.")
	@Initializer("true")
	boolean getNoDocu();
	void setNoDocu(boolean noDocu);
}