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
package com.braintribe.gm.config.api;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * ModelledConfiguration allows to access configuration data that is defined by a root entity type. It may come from some persistence defined in a runtime system or from memory.
 * The fact that the configuration is solely given by modeled data supports generic serialization/persistence.
 * @author dirk.scheffler
 */
public interface ModeledConfiguration {
	/**
	 *	Returns a configuration for the given Type or throws a ReasonException in case that the configuration could not be retrieved.
	 *	If an explicit configuration cannot be found a default initialized instance of the configType will be returned. 
	 */
	<C extends GenericEntity> C config(EntityType<C> configType) throws ReasonException;
	
	/**
	 *	Returns a configuration for the given type or a reason why the configuration could not be retrieved.
	 *	If an explicit configuration cannot be found a default initialized instance of the configType will be returned. 
	 */
	<C extends GenericEntity> Maybe<C> configReasoned(EntityType<C> configType);
}
