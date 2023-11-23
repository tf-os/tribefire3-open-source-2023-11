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
package com.braintribe.gm.config.yaml.wire;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.config.wire.ModeledConfigurationWireModule;
import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.gm.config.yaml.ModeledYamlConfiguration;
import com.braintribe.gm.config.yaml.wire.contract.YamlConfigurationsLocationContract;
import com.braintribe.gm.config.yaml.wire.space.ModeledYamlConfigurationSpace;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

/**
 * This {@link WireModule} can be used to override {@link ModeledConfigurationContract} with an {@link ModeledYamlConfiguration implementation}
 * of {@link ModeledConfiguration} that uses the filesystem and yaml marshalled modeled data to retrieve configurations.
 * 
 * <p>
 * The lookup strategy is to build a filename using kebab cased variant of the {@link EntityType#getShortName() type short name} suffixed with .yaml
 * located in the config directory given by the parameter of the {@link ModeledYamlConfigurationWireModule#ModeledYamlConfigurationWireModule(File) constructor parameter}. 
 * 
 * @see ModeledYamlConfiguration
 * 
 * @author dirk.scheffler
 */
public class ModeledYamlConfigurationWireModule implements WireModule {
	private File configDir;

	/**
	 * @param configDir the directory in which configuration files are read 
	 */
	public ModeledYamlConfigurationWireModule(File configDir) {
		super();
		this.configDir = configDir;
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Collections.singletonList(ModeledConfigurationWireModule.INSTANCE);
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(ModeledConfigurationContract.class, ModeledYamlConfigurationSpace.class);
		contextBuilder.bindContract(YamlConfigurationsLocationContract.class, () -> configDir);
	}
}