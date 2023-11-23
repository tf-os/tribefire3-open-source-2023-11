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

import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.HasMetaData;

/**
 * @author gunther.schenk
 * 
 */
@Abstract
@SelectiveInformation("${name} (${externalId})")
public interface Deployable extends HasExternalId, HasName, HasMetaData {

	EntityType<Deployable> T = EntityTypes.T(Deployable.class);

	String module = "module";
	String autoDeploy = "autoDeploy";
	String deploymentStatus = "deploymentStatus";

	/**
	 * The module which is responsible for the actual deployment of this instance.
	 * <p>
	 * In theory, multiple modules may bind experts for the same denotation type (for example differing in the library used for implementation). In
	 * such cases we need to specify the module explicitly, to specify out intentions to the deployment mechanism.
	 * <p>
	 * Note that the platform itself is also considered a module here, and is also represented by a {@link Module} instance (see
	 * {@link Module#PLATFORM_MODULE_GLOBAL_ID}).
	 * <p>
	 * If no value is set, the deployable can be deployed if there is exactly one module that bound experts for this type.
	 * <p>
	 * Tribefire attempts to set this property automatically.
	 * <p>
	 * In case the deployable comes via an initializer in a module, it will try to examine that module and all it's dependent modules, and if only one
	 * of them bound experts for this type, TF sets that module as the value here.
	 * <p>
	 * In case the deployable is created by a user, the module is set automatically only if there is exactly one module that bound experts for this
	 * type. In this situation, if a module cannot be set due to ambiguity, the deployable also cannot be deployed unless the user sets the value
	 * explicitly.
	 */
	Module getModule();
	void setModule(Module module);

	@Initializer("true")
	boolean getAutoDeploy();
	void setAutoDeploy(boolean autoDeploy);

	@Initializer("undeployed")
	DeploymentStatus getDeploymentStatus();
	void setDeploymentStatus(DeploymentStatus status);

	default String shortDescription() {
		return entityType().getShortName() + "[" + getExternalId() + "]";
	}

}
