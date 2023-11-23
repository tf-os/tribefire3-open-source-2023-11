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
package com.braintribe.gm.config.yaml;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class ModeledYamlConfigurationLoader {
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE ;
	private Function<String, String> variableResolver = n -> null;
	
	
	public ModeledYamlConfigurationLoader virtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
		return this;
	}
	
	public ModeledYamlConfigurationLoader variableResolver(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
		return this;
	}
	
	public <C extends GenericEntity> Maybe<C> loadConfig(EntityType<C> configType, File configFile, boolean fileMustExist) {
		return loadConfig(configType, configFile, configType::create, fileMustExist);
	}
	
	public <C> Maybe<C> loadConfig(GenericModelType configType, File configFile, Supplier<C> defaultSupplier, boolean fileMustExist) {
		// if file does not exist a default instance of the configuration will be created
		if (!configFile.exists()) {
			if (fileMustExist) 
				return Reasons.build(NotFound.T).text("Configuration file " + configFile.getAbsolutePath() + " does not exist").toMaybe();
			else 
				return Maybe.complete(defaultSupplier.get());
		}
		
		ConfigVariableResolver variableResolver = new ConfigVariableResolver(virtualEnvironment, configFile);
		variableResolver.setVariableResolver(this.variableResolver);
		Maybe<C> maybe = YamlConfigurations.<C>read(configType) //
				.placeholders(variableResolver::resolve) //
				.from(configFile);
		
		if (variableResolver.getFailure() != null) {
			return variableResolver.getFailure().asMaybe();
		}
		
		return maybe;
	}
}
