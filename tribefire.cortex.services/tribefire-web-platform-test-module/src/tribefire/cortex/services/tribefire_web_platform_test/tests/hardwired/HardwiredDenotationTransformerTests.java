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
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.cortex.services.tribefire_web_platform_test.tests.PlatformHolder;
import tribefire.module.api.DenotationTransformer;
import tribefire.module.api.DenotationTransformerRegistry;
import tribefire.module.api.EnvironmentDenotations;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * We try to bind a dummy denotation type for the leadership in Environment Denotation Registry.
 * <p>
 * If there is an entry already, we abort the mission and go home, without testing anything.
 * <p>
 * Otherwise we also register a {@link DenotationTransformer}s which in multiple steps turn the dummy instance into a {@link Locking}.
 * <p>
 * The final {@link Locking} is of the exact same kind the platform itself would create if none was set on CortexConfiguration - this means
 * the actual deployment will deploy the default platform implementation.
 * <p>
 * In other words, we are trying to test this mechanism with zero actual affect on what is being deployed.
 * 
 * @author peter.gazdik
 */
public class HardwiredDenotationTransformerTests {

	private static final String TRIBEFIRE_LOCKING_BIND_ID = "tribefire-locking";
	private static final String DEFAULT_LOCKING_EXTERNAL_ID = "default.Locking";

	private static boolean TEST_ENABLED = true;

	private static final Logger log = Logger.getLogger(HardwiredDenotationTransformerTests.class);

	private TribefireWebPlatformContract platform;

	@Before
	public void setup() {
		platform = PlatformHolder.platformContract;
	}

	// ################################################
	// ## . . . . . . . Module Loading . . . . . . . ##
	// ################################################

	public static void bindHardwired(TribefireWebPlatformContract tfPlatform) {
		if (registerEnvironmentDenotation(tfPlatform))
			bindTransformer(tfPlatform);
	}

	private static boolean registerEnvironmentDenotation(TribefireWebPlatformContract tfPlatform) {
		EnvironmentDenotations environmentDenotations = tfPlatform.platformReflection().environmentDenotations();

		GenericEntity existingLocking = environmentDenotations.lookup(TRIBEFIRE_LOCKING_BIND_ID);
		if (existingLocking != null) {
			logExistingInstance(existingLocking, TRIBEFIRE_LOCKING_BIND_ID);
			return TEST_ENABLED = false;
		}

		environmentDenotations.register(TRIBEFIRE_LOCKING_BIND_ID, lockingComment());
		return true;
	}

	private static void logExistingInstance(GenericEntity existingInstance, String bindId) {
		StringBuilder sb = new StringBuilder();
		sb.append("Will not test Environment Denotation Transformation - there is already an entry for leadership with entity: ");
		sb.append(existingInstance);
		sb.append(" under bindID: ");
		sb.append(bindId);
		sb.append(". ");

		if (existingInstance instanceof Deployable)
			sb.append("Seeing it is a Deployable, let's hope this mechanism is anyway being implicitly tested.");
		else
			sb.append("Seeing this is not a Deployable, who knows if this mechanism is being used and thus at all tested.");

		log.info(sb.toString());
	}

	private static ManipulationComment lockingComment() {
		ManipulationComment mc = ManipulationComment.T.create();
		mc.setAuthor(HardwiredDenotationTransformerTests.class.getSimpleName());
		mc.setText("This is just a dummy entity for EnvironmentDenotationRegistry to test that it can be transofrmed into Locking. "
				+ "Because any dummy can be a maanger.");
		return mc;
	}

	private static void bindTransformer(TribefireWebPlatformContract tfPlatform) {
		DenotationTransformerRegistry registry = tfPlatform.hardwiredExperts().denotationTransformationRegistry();
		registry.registerMorpher("Manipulation_To_LeadershipManager", Manipulation.T, Locking.T, //
				(ctx, manipulation) -> {
					if (!(manipulation instanceof ManipulationComment))
						throw new IllegalStateException("Was expecting ManipulationComment, not: " + manipulation);

					Locking bean = ctx.create(Locking.T);
					bean.setGlobalId("default:locking/" + DEFAULT_LOCKING_EXTERNAL_ID);
					bean.setExternalId(DEFAULT_LOCKING_EXTERNAL_ID);
					bean.setName("DMB Locking (Default) From HardwiredDenotationTransformationTest");

					return Maybe.complete(bean);

				});
	}

	// ################################################
	// ## . . . . . . . . . Tests . . . . . . . . . .##
	// ################################################

	@Test
	public void testCcLockingWasSetViaEdr2Cc() throws Exception {
		if (!TEST_ENABLED)
			return;

		PersistenceGmSession session = platform.systemUserRelated().cortexSessionSupplier().get();
		CortexConfiguration cc = session.getEntityByGlobalId(CortexConfiguration.globalId);

		assertThat(cc).isNotNull();

		Locking lm = cc.getLocking();
		assertThat(lm).isNotNull();
		assertThat(lm.getName()).endsWith("Test");
	}

}
