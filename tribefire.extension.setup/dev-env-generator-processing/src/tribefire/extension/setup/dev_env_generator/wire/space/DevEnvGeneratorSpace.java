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
package tribefire.extension.setup.dev_env_generator.wire.space;

import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.setup.dev_env_generator.processing.DevEnvGenerator;
import tribefire.extension.setup.dev_env_generator.wire.contract.DevEnvGeneratorContract;
import tribefire.extension.setup.dev_env_generator_config.model.DevEnvGeneratorConfig;

@Managed
public class DevEnvGeneratorSpace implements DevEnvGeneratorContract {

	@Import
	private ModeledConfigurationContract modeledConfiguration;

	@Managed
	@Override
	public DevEnvGenerator devEnvGenerator() {

		DevEnvGenerator bean = new DevEnvGenerator();
		bean.setConfiguration(modeledConfiguration.config(DevEnvGeneratorConfig.T));
		return bean;
	}

}