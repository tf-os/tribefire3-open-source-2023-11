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
package tribefire.platform.config.url;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.platform.impl.configuration.EnvironmentDenotationRegistry;

public class AbstractEnvironmentVariableBasedConfiguratorTest {

	private static final String json = "[{\"_type\":\"com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry\",\"bindId\":\"model\",\"denotation\":{\"_type\":\"com.braintribe.model.meta.GmMetaModel\",\"name\":\"TestModel\"}}]";

	@Test
	public void testVariableResolution() throws Exception {
		
		AbstractEnvironmentVariableBasedConfigurator configurator = new AbstractEnvironmentVariableBasedConfigurator() {
			@Override
			protected String getEnvironmentVariableName() {
				return "my-test";
			}
		};
		System.setProperty("my-test", json);

		List<RegistryEntry> list = configurator.getEntries();
		
		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
		
		RegistryEntry re = list.get(0);
		assertThat(re.getBindId()).isEqualTo("model");
	}
	
	@Test
	public void testSharedVariableResolution() throws Exception {
		
		AbstractEnvironmentVariableBasedConfigurator configurator = new AbstractEnvironmentVariableBasedConfigurator() {
			@Override
			protected String getEnvironmentVariableName() {
				return null;
			}
		};
		System.setProperty("TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED", json);

		List<RegistryEntry> list = configurator.getEntries();
		
		assertThat(list).isNotNull();
		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
		
		RegistryEntry re = list.get(0);
		assertThat(re.getBindId()).isEqualTo("model");
	}
	
	@Test
	public void testMixedVariableResolution() throws Exception {
		
		AbstractEnvironmentVariableBasedConfigurator configurator = new AbstractEnvironmentVariableBasedConfigurator() {
			@Override
			protected String getEnvironmentVariableName() {
				return "my-test";
			}
		};
		System.setProperty("TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED", json);
		System.setProperty("my-test", json);

		List<RegistryEntry> list = configurator.getEntries();
		
		assertThat(list).isNotNull();
		assertThat(list).hasSize(2);
		assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
		assertThat(list.get(1)).isInstanceOf(RegistryEntry.class);
		
		RegistryEntry re = list.get(0);
		assertThat(re.getBindId()).isEqualTo("model");
		re = list.get(1);
		assertThat(re.getBindId()).isEqualTo("model");
	}
	
	@Test
	public void testRegistration() throws Exception {
		AbstractEnvironmentVariableBasedConfigurator configurator = new AbstractEnvironmentVariableBasedConfigurator() {
			@Override
			protected String getEnvironmentVariableName() {
				return "my-test";
			}
		};
		System.setProperty("my-test", json);
		
		configurator.configure();
		
		GenericEntity entity = EnvironmentDenotationRegistry.getInstance().lookup("model");
		assertThat(entity).isNotNull().isInstanceOf(GmMetaModel.class);
		assertThat(((GmMetaModel) entity).getName()).isEqualTo("TestModel");
	}
}
