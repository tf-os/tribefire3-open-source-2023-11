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
package tribefire.cortex.services.tribefire_web_platform_test.tests.hardwired;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQueryResult;

import tribefire.cortex.services.tribefire_web_platform_test.tests.PlatformHolder;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * @author peter.gazdik
 */
public class HardwiredBindOnConfigurationModelTests {

	private static final String CONFIGURATION_MODEL_NAME = "tribefire.platform:configuration-model-for-test";

	private TribefireWebPlatformContract platform;

	@Before
	public void setup() {
		platform = PlatformHolder.platformContract;
	}

	// ################################################
	// ## . . . . . . . Module Loading . . . . . . . ##
	// ################################################

	public static void bindHardwired(TribefireWebPlatformContract tfPlatform) {
		tfPlatform.hardwiredDeployables().bindOnNewConfigurationModel(CONFIGURATION_MODEL_NAME) //
				.serviceProcessor("test:serviceProcessor:bound-on-conf-model:select", "Select Query Processor Bound on Config Model", //
						QueryAndSelect.T, (ctx, r) -> SelectQueryResult.T.create());

		tfPlatform.hardwiredDeployables().bindOnConfigurationModel(CONFIGURATION_MODEL_NAME) //
				.serviceProcessor("test:serviceProcessor:bound-on-conf-model:entity", "Entity Query Processor Bound on Config Model", //
						QueryEntities.T, (ctx, r) -> EntityQueryResult.T.create());

		tfPlatform.hardwiredDeployables().bindOnExistingConfigurationModel(CONFIGURATION_MODEL_NAME) //
				.serviceProcessor("test:serviceProcessor:bound-on-conf-model:property", "Property Query Processor Bound on Config Model", //
						QueryProperty.T, (ctx, r) -> PropertyQueryResult.T.create());
	}

	// ################################################
	// ## . . . . . . . . . Tests . . . . . . . . . .##
	// ################################################

	@Test
	public void configuredOnNewModel() throws Exception {
		PersistenceGmSession cortexSession = platform.systemUserRelated().cortexSessionSupplier().get();

		GmMetaModel model = cortexSession.findEntityByGlobalId(Model.modelGlobalId(CONFIGURATION_MODEL_NAME));
		assertThat(model).isNotNull();

		assertThat(model.getName()).isEqualTo(CONFIGURATION_MODEL_NAME);

		assertThat(model.getDependencies()).isNotEmpty();
		assertThat(first(model.getDependencies()).getName()).isEqualTo(QueryAndSelect.T.getModel().name());

		CmdResolver cmdResolver = CmdResolverImpl.create(new BasicModelOracle(model)).done();

		checkProcessWithOn(cmdResolver, model, QueryAndSelect.T);
		checkProcessWithOn(cmdResolver, model, QueryEntities.T);
		checkProcessWithOn(cmdResolver, model, QueryProperty.T);
	}

	private void checkProcessWithOn(CmdResolver cmdResolver, GmMetaModel model, EntityType<? extends QueryRequest> requestType) {
		EntityMdDescriptor md = cmdResolver.getMetaData().entityType(requestType).meta(ProcessWith.T).exclusiveExtended();

		assertThat(md).isNotNull();
		assertThat(md.getOwnerModel()).isEqualTo(model);
	}

}
