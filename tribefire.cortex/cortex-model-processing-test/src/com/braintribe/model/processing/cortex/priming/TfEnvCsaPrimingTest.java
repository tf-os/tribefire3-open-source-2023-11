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
package com.braintribe.model.processing.cortex.priming;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;

/**
 * @see TfEnvCsaPriming
 * 
 * @author peter.gazdik
 */
public class TfEnvCsaPrimingTest {

	@Before
	public void setup() {
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_CONFIGURATION_DIR, "Root");
	}

	@Test
	public void emptyListWhenNoPrimingProperty() throws Exception {
		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).isNotNull().isEmpty();
	}

	@Test
	public void emptyListWhenNoRelevantAccessEntry() throws Exception {
		setManipulationPriming("Injected/data.man>otherAccess");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).isNotNull().isEmpty();
	}

	@Test
	public void emptyListWhenNoRelevantAccessEntryPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/data.man>otherAccess");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).isNotNull().isEmpty();
	}

	@Test
	public void emptyListWhenNoRelevantAccessPatternMatches() throws Exception {
		setManipulationPriming("Injected/data.man>pattern:acc.*2$\"");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).isNotNull().isEmpty();
	}

	@Test
	public void emptyListWhenNoRelevantAccessPatternMatchesPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/data.man>pattern:acc.*2$\"");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).isNotNull().isEmpty();
	}

	@Test
	public void resolvesAbsoluteFileWhenConfigured() throws Exception {
		setManipulationPriming("Injected/model.man>access1");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesAbsoluteFileWhenConfiguredPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/model.man>access1");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesAbsoluteFileWhenPatternConfigured() throws Exception {
		setManipulationPriming("Injected/model.man>pattern:acc.*1$");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());

		files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("accXYZ1", false);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesAbsoluteFileWhenPatternConfiguredPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/model.man>pattern:acc.*1$");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());

		files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("accXYZ1", true);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesTwoAbsoluteFilesWhenConfigured() throws Exception {
		setManipulationPriming("Injected/model.man>access1,Injected/data.man>access1");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).hasSize(2);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
		assertThat(files.get(1)).isEqualTo(new File("Root/Injected/data.man").getAbsoluteFile());
	}

	@Test
	public void resolvesTwoAbsoluteFilesWhenConfiguredPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/model.man>access1,Injected/data.man>access1");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).hasSize(2);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
		assertThat(files.get(1)).isEqualTo(new File("Root/Injected/data.man").getAbsoluteFile());
	}

	@Test
	public void resolvesTwoAbsoluteFilesWhenPatternConfigured() throws Exception {
		setManipulationPriming("Injected/model.man>pattern:acc.*1$,Injected/data.man>pattern:acc.*1$");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(files).hasSize(2);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
		assertThat(files.get(1)).isEqualTo(new File("Root/Injected/data.man").getAbsoluteFile());
	}

	@Test
	public void resolvesTwoAbsoluteFilesWhenPatternConfiguredPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/model.man>pattern:acc.*1$,Injected/data.man>pattern:acc.*1$");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(files).hasSize(2);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
		assertThat(files.get(1)).isEqualTo(new File("Root/Injected/data.man").getAbsoluteFile());
	}

	@Test
	public void resolvesCortexEntryPerDefault() throws Exception {
		setManipulationPriming("Injected/model.man");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("cortex", false);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesCortexEntryPerDefaultPreInit() throws Exception {
		setManipulationPrimingPreInit("Injected/model.man");

		List<?> files = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("cortex", true);
		assertThat(files).hasSize(1);
		assertThat(files.get(0)).isEqualTo(new File("Root/Injected/model.man").getAbsoluteFile());
	}

	@Test
	public void resolvesEnvVariableAsValue() throws Exception {
		setManipulationPriming("env:MY_GMML_SCRIPT");

		List<?> envNames = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("cortex", false);
		assertThat(envNames).hasSize(1);
		assertThat(envNames.get(0)).isEqualTo("MY_GMML_SCRIPT");
	}

	@Test
	public void resolvesEnvVariableAsValuePreInit() throws Exception {
		setManipulationPrimingPreInit("env:MY_GMML_SCRIPT");

		List<?> envNames = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("cortex", true);
		assertThat(envNames).hasSize(1);
		assertThat(envNames.get(0)).isEqualTo("MY_GMML_SCRIPT");
	}

	@Test
	public void resolvesEnvVariableAsValueWhenPatternConfigured() throws Exception {
		setManipulationPriming("env:MY_GMML_SCRIPT>pattern:acc.*1$");

		List<?> envNames = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", false);
		assertThat(envNames).hasSize(1);
		assertThat(envNames.get(0)).isEqualTo("MY_GMML_SCRIPT");
	}

	@Test
	public void resolvesEnvVariableAsValueWhenPatternConfiguredPreInit() throws Exception {
		setManipulationPrimingPreInit("env:MY_GMML_SCRIPT>pattern:acc.*1$");

		List<?> envNames = TfEnvCsaPriming.getEnvironmentInitializerObjectsFor("access1", true);
		assertThat(envNames).hasSize(1);
		assertThat(envNames.get(0)).isEqualTo("MY_GMML_SCRIPT");
	}

	private void setManipulationPriming(String primingExpression) {
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_MANIPULATION_PRIMING, primingExpression);
	}

	private void setManipulationPrimingPreInit(String primingExpression) {
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_MANIPULATION_PRIMING_PREINIT, primingExpression);
	}

}
