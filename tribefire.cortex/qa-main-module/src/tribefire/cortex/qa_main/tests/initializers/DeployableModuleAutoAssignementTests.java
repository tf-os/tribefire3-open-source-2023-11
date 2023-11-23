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
package tribefire.cortex.qa_main.tests.initializers;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;
import static tribefire.cortex.qa.CortexQaCommons.mainModuleName;
import static tribefire.cortex.qa.CortexQaCommons.spGlobalId;
import static tribefire.cortex.qa.CortexQaCommons.subModuleName;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.cortex.model.MainAndOtherSerp;
import tribefire.cortex.model.MainAndSubSerp;
import tribefire.cortex.model.MainOnlySerp;
import tribefire.cortex.model.QaSerp;
import tribefire.cortex.model.SubOnlySerp;
import tribefire.cortex.qa.CortexQaCommons;
import tribefire.cortex.qa_main.tests.PlatformHolder;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * @author peter.gazdik
 */
public class DeployableModuleAutoAssignementTests {

	private TribefireWebPlatformContract tfPlatform;

	@Before
	public void setup() {
		tfPlatform = PlatformHolder.platformContract;
	}

	// Tests temporarily disabled due to Jinni backwards compatibility issues
	//@Test
	public void deduceForUnambiguiousTypes() throws Exception {
		PersistenceGmSession session = tfPlatform.systemUserRelated().cortexSessionSupplier().get();

		assertRightModule(session, MainOnlySerp.T, mainModuleName);
		assertRightModule(session, SubOnlySerp.T, subModuleName);
	}

	//@Test
	public void deduceAsOnlyOneDepSupports() throws Exception {
		PersistenceGmSession session = tfPlatform.systemUserRelated().cortexSessionSupplier().get();

		assertRightModule(session, MainAndOtherSerp.T, mainModuleName);
		assertRightModule(session, MainAndOtherSerp.T, CortexQaCommons.otherModuleName);
		assertRightModule(session, MainAndSubSerp.T, subModuleName);
	}

	@Test
	public void cannotDeduceWhenAmbiguous() throws Exception {
		PersistenceGmSession session = tfPlatform.systemUserRelated().cortexSessionSupplier().get();

		ServiceProcessor sp = session.findEntityByGlobalId(spGlobalId(MainAndSubSerp.T, mainModuleName));
		assertThat(sp.getModule()).isNull();
	}

	private void assertRightModule(PersistenceGmSession session, EntityType<? extends QaSerp> spType, String moduleName) {
		ServiceProcessor sp = session.findEntityByGlobalId(spGlobalId(spType, moduleName));
		assertModule(sp, moduleName);
	}

	private void assertModule(Deployable d, String moduleName) {
		Module module = d.getModule();
		assertThat(module).isNotNull();
		assertThat(module.getName()).isEqualTo(moduleName);
	}

}
