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
package com.braintribe.model.deployment;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface Module extends HasName {

	EntityType<Module> T = EntityTypes.T(Module.class);

	String PLATFORM_MODULE_GLOBAL_ID = "module://platform";

	static String moduleGlobalId(String groupId, String artifactId) {
		return moduleGlobalId(groupId + ":" + artifactId);
	}

	static String moduleGlobalId(String moduleName) {
		return "module://" + moduleName;
	}

	String container = "container";
	String bindsWireContracts = "bindsWireContracts";
	String bindsHardwired = "bindsHardwired";
	String bindsInitializers = "bindsInitializers";
	String bindsDeployables = "bindsDeployables";

	/**
	 * The name is the artifactId of given module.
	 * <p>
	 * For the technical name containing groupId use {@link #moduleName()}.
	 */
	@Override
	java.lang.String getName();

	String getGroupId();
	void setGroupId(String groupId);

	boolean getBindsWireContracts();
	void setBindsWireContracts(boolean bindsWireContracts);

	boolean getBindsHardwired();
	void setBindsHardwired(boolean bindsHardwired);

	boolean getBindsInitializers();
	void setBindsInitializers(boolean bindsInitializers);

	boolean getBindsDeployables();
	void setBindsDeployables(boolean bindsDeployables);

	default boolean isPlatformModule() {
		return PLATFORM_MODULE_GLOBAL_ID.equals(getGlobalId());
	}

	/** Module name in the format "${groupId}:${artifactId}" */
	default String moduleName() {
		return getGroupId() + ":" + getName();
	}
}
