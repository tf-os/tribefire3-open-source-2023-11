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
package com.braintribe.gm.config.wire;

import com.braintribe.gm.config.impl.DefaultModeledConfiguration;
import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.gm.config.wire.space.ModeledConfigurationSpace;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

/**
 * {@link WireModule}s that want to use external configuration given by modeled data can depend on this module and then 
 * {@link Import import} {@link ModeledConfigurationContract} to retrieved configuration instances via:
 * 
 * <ul>
 * 	<li>{@link ModeledConfigurationContract#config()}
 * 	<li>{@link ModeledConfigurationContract#config(com.braintribe.model.generic.reflection.EntityType)}
 * 	<li>{@link ModeledConfigurationContract#configReasoned(com.braintribe.model.generic.reflection.EntityType)}
 * </ul>
 * 
 * <p>
 * The default {@link ModeledConfigurationSpace binding} of {@link ModeledConfigurationContract} uses the {@link DefaultModeledConfiguration} implementation.
 * 
 * <p>
 * Other modules can override the default {@link ModeledConfigurationSpace binding} of {@link ModeledConfigurationContract} using {@link WireContextBuilder#bindContract(Class, com.braintribe.wire.api.space.WireSpace)}.
 * to supply
 * @author dirk.scheffler
 *
 */
public enum ModeledConfigurationWireModule implements WireModule {
	INSTANCE
}