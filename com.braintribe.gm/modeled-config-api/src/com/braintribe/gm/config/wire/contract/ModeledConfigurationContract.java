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
package com.braintribe.gm.config.wire.contract;

import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.space.WireSpace;

public interface ModeledConfigurationContract extends WireSpace {

	/**
	 * @see ModeledConfiguration#config(EntityType)
	 */
	default <C extends GenericEntity> C config(EntityType<C> configType) {
		return config().config(configType);
	}
	
	/**
	 * @see ModeledConfiguration#configReasoned(EntityType)
	 */
	default <C extends GenericEntity> Maybe<C> configReasoned(EntityType<C> configType) {
		return config().configReasoned(configType);
	}
	
	/**
	 * Returns the {@link ModeledConfiguration} on which modeled configurations can be retrieved. 
	 */
	ModeledConfiguration config();
}