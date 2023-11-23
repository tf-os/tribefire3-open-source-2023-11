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

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.FileTools;

import tribefire.platform.impl.configuration.EnvironmentDenotationRegistry;

public class AbstractUrlBasedConfiguratorTest {

	private static final String json = "[{\"_type\":\"com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry\",\"bindId\":\"model\",\"denotation\":{\"_type\":\"com.braintribe.model.meta.GmMetaModel\",\"name\":\"TestModel\"}}]";

	@Test
	public void testUrlResolutionWithUrl() throws Exception {
		File tempFile = File.createTempFile("json", ".properties");

		try {
			FileTools.writeStringToFile(tempFile, json);

			TribefireRuntime.setProperty("my-test-prop", tempFile.toURI().toString());
			
			AbstractUrlBasedConfigurator configurator = new AbstractUrlBasedConfigurator() {
				// @formatter:off
				@Override protected String buildDefaultFileName() { return "not used"; }
				@Override protected String buildUrlProperty() { return "my-test-prop"; }
				// @formatter:on
			};
			
			List<RegistryEntry> list = configurator.getEntries();
			
			assertThat(list).isNotNull().hasSize(1);
			assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
			assertThat(list.get(0).getBindId()).isEqualTo("model");
			
		} finally {
			FileTools.deleteFileSilently(tempFile);
		}
	}
	
	@Test
	public void testSharedUrlResolutionWithUrl() throws Exception {
		File tempFile = File.createTempFile("json", ".properties");

		try {
			FileTools.writeStringToFile(tempFile, json);

			TribefireRuntime.setProperty("TRIBEFIRE_CONFIGURATION_INJECTION_URL_SHARED", tempFile.toURI().toString());
			
			AbstractUrlBasedConfigurator configurator = new AbstractUrlBasedConfigurator() {
				// @formatter:off
				@Override protected String buildDefaultFileName() { return "not used"; }
				@Override protected String buildUrlProperty() { return "not used"; }
				// @formatter:on
			};
			
			List<RegistryEntry> list = configurator.getEntries();
			
			assertThat(list).isNotNull().hasSize(1);
			assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
			assertThat(list.get(0).getBindId()).isEqualTo("model");
			
		} finally {
			FileTools.deleteFileSilently(tempFile);
		}
	}

	@Test
	public void testMixedUrlResolutionWithUrl() throws Exception {
		File tempFile = File.createTempFile("json", ".properties");

		try {
			FileTools.writeStringToFile(tempFile, json);

			TribefireRuntime.setProperty("TRIBEFIRE_CONFIGURATION_INJECTION_URL_SHARED", tempFile.toURI().toString());
			TribefireRuntime.setProperty("my-test-prop", tempFile.toURI().toString());
			
			AbstractUrlBasedConfigurator configurator = new AbstractUrlBasedConfigurator() {
				// @formatter:off
				@Override protected String buildDefaultFileName() { return "not used"; }
				@Override protected String buildUrlProperty() { return "my-test-prop"; }
				// @formatter:on
			};
			
			List<RegistryEntry> list = configurator.getEntries();
			
			assertThat(list).isNotNull().hasSize(2);
			assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
			assertThat(list.get(1)).isInstanceOf(RegistryEntry.class);
			
			assertThat(list.get(0).getBindId()).isEqualTo("model");
			assertThat(list.get(1).getBindId()).isEqualTo("model");
			
		} finally {
			FileTools.deleteFileSilently(tempFile);
		}
	}

	@Test
	public void testUrlResolutionWithDefaultFilename() throws Exception {
		File tempFile = File.createTempFile("json", ".properties");

		try {
			FileTools.writeStringToFile(tempFile, json);

			AbstractUrlBasedConfigurator configurator = new AbstractUrlBasedConfigurator() {
				// @formatter:off
				@Override protected String buildDefaultFileName() { return tempFile.getAbsolutePath(); }
				@Override protected String buildUrlProperty() { return "undefined-prop"; }
				// @formatter:on
			};
			
			List<RegistryEntry> list = configurator.getEntries();
			
			assertThat(list).isNotNull().hasSize(1);
			assertThat(list.get(0)).isInstanceOf(RegistryEntry.class);
			assertThat(list.get(0).getBindId()).isEqualTo("model");
			
		} finally {
			FileTools.deleteFileSilently(tempFile);
		}
	}
	
	@Test
	public void testRegistration() throws Exception {
		File tempFile = File.createTempFile("json", ".properties");

		try {
			FileTools.writeStringToFile(tempFile, json);

			TribefireRuntime.setProperty("my-test-prop", tempFile.toURI().toString());
			
			AbstractUrlBasedConfigurator configurator = new AbstractUrlBasedConfigurator() {
				// @formatter:off
				@Override protected String buildDefaultFileName() { return "not used"; }
				@Override protected String buildUrlProperty() { return "my-test-prop"; }
				// @formatter:on
			};
			
			configurator.configure();
			
			GenericEntity entity = EnvironmentDenotationRegistry.getInstance().lookup("model");
			assertThat(entity).isNotNull();
			assertThat(entity).isNotNull().isInstanceOf(GmMetaModel.class);
			assertThat(((GmMetaModel) entity).getName()).isEqualTo("TestModel");
			
		} finally {
			FileTools.deleteFileSilently(tempFile);
		}
	}
}
