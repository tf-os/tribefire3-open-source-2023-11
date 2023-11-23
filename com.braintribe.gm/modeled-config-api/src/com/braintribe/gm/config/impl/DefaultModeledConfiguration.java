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
package com.braintribe.gm.config.impl;

import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * This default implementation of {@link ModeledConfiguration} always resolves configuration entity by returning a default initialized instance of the given config type.
 * @author dirk.scheffler
 */
public class DefaultModeledConfiguration implements ModeledConfiguration {

	@Override
	public <C extends GenericEntity> C config(EntityType<C> configType) {
		return configType.create();
	}

	@Override
	public <C extends GenericEntity> Maybe<C> configReasoned(EntityType<C> configType) {
		return Maybe.complete(config(configType));
	}

}
