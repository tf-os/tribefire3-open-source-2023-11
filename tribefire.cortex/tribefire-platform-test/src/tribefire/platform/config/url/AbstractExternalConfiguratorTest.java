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

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.platform.impl.configuration.EnvironmentDenotationRegistry;

public class AbstractExternalConfiguratorTest {

	private static final String json = "[{\"_type\":\"com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry\",\"bindId\":\"model\",\"denotation\":{\"_type\":\"com.braintribe.model.meta.GmMetaModel\",\"name\":\"TestModel\"}}]";

	@Test
	public void testReadFromInputStream() throws Exception {
		
		AbstractExternalConfigurator configurator = new AbstractExternalConfigurator() {
			// @formatter:off
			@Override protected List<RegistryEntry> getEntries() { return null; }
			@Override protected String getSourceInformation() { return null; }
			// @formatter:on
		};
		List<RegistryEntry> list = configurator.readConfigurationFromInputStream(new StringReader(json));
		
		assertThat(list).isNotNull().hasSize(1);
		assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
		assertThat(list.get(0).getBindId()).isEqualTo("model");
	}
	
	@Test
	public void testRegistration() throws Exception {
		AbstractExternalConfigurator configurator = new AbstractExternalConfigurator() {
			@Override
			protected List<RegistryEntry> getEntries() throws ConfiguratorException {
				try (StringReader reader = new StringReader(json)) {
					return super.readConfigurationFromInputStream(reader);
				} catch(Exception e) {
					throw new ConfiguratorException("Error while trying to read value "+json, e);
				}
			}

			@Override
			protected String getSourceInformation() {
				return "Test";
			}
			
		};
		configurator.configure();
		
		GenericEntity entity = EnvironmentDenotationRegistry.getInstance().lookup("model");
		assertThat(entity).isNotNull().isInstanceOf(GmMetaModel.class);
		assertThat(((GmMetaModel) entity).getName()).isEqualTo("TestModel");
	}
}
