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
package tribefire.cortex.services.tribefire_web_platform_test.tests.accessory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.display.Color;
import com.braintribe.model.modelnotification.InternalOnModelChanged;
import com.braintribe.model.modelnotification.OnModelChanged;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.user.User;

import tribefire.cortex.services.tribefire_web_platform_test.tests.PlatformHolder;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This is a basic test to verify that the {@link ModelAccessoryFactory} is wired correctly to handle model change notifications -
 * {@link OnModelChanged}, {@link InternalOnModelChanged}. It does so by testing that CMD resolver resolves a MD added on a type after the
 * {@link OnModelChanged} is triggered. Extensive use-case testing (for {@link ModelAccessoryFactory#getForServiceDomain(String) service domain} or
 * for just {@link ModelAccessoryFactory#getForModel(String) model}) is beyond the scope of this method and is expected to be tested on the concrete
 * MAF implementation.
 *
 * @author peter.gazdik
 */
public class ModelAccessoryFactoryTests {

	private static final String TEST_ACCESS_NAME = "model-accessory-test-access";
	private static final String TEST_MODEL_NAME = "test:model-accessory-test-model";
	private static final String TEST_MODEL_GID = Model.modelGlobalId(TEST_MODEL_NAME);

	private static final String COLOR_MD_GLOBAL_ID = "test:md:model-accessory-factory-color";

	private TribefireWebPlatformContract platform;

	@Before
	public void setup() {
		platform = PlatformHolder.platformContract;
	}

	// ################################################
	// ## . . . . . . . Module Loading . . . . . . . ##
	// ################################################

	public static void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind("cortex", ModelAccessoryFactoryTests::createModelAccessoryTestAccess);
	}

	private static void createModelAccessoryTestAccess(PersistenceInitializationContext ctx) {
		ManagedGmSession session = ctx.getSession();

		Model uModel = User.T.getModel();
		GmMetaModel userModel = session.getEntityByGlobalId(uModel.globalId());

		GmMetaModel model = session.create(GmMetaModel.T, TEST_MODEL_GID);
		model.setName(TEST_MODEL_NAME);
		model.getDependencies().add(userModel);
		model.setVersion("1.0");

		CollaborativeSmoodAccess sa = session.create(CollaborativeSmoodAccess.T, "access:model-accessory-test");
		sa.setExternalId(TEST_ACCESS_NAME);
		sa.setName(TEST_ACCESS_NAME);
		sa.setMetaModel(model);
	}

	// ################################################
	// ## . . . . . . . . . Tests . . . . . . . . . .##
	// ################################################

	@Test
	public void purgesCachesOnModelChange() throws Exception {
		ensureNoColor();

		ModelAccessoryFactory maf = platform.systemUserRelated().modelAccessoryFactory();
		ModelAccessory ma = maf.getForAccess(TEST_ACCESS_NAME);

		assertThat(userColorInfo(ma)).isNull(); // No MD configured -> no color

		makeUserColorful();
		assertThat(userColorInfo(ma)).isNull(); // MD configured, old MA cached -> no color

		notifyModelChange();
		assertThat(userColorInfo(ma)).isNotNull(); // MD configured, MA cache purged -> yes color
	}

	private void ensureNoColor() {
		PersistenceGmSession session = platform.systemUserRelated().cortexSessionSupplier().get();
		Color color = session.findEntityByGlobalId(COLOR_MD_GLOBAL_ID);
		if (color != null) {
			session.deleteEntity(color);
			session.commit();
		}
	}

	private Color userColorInfo(ModelAccessory ma) {
		return ma.getCmdResolver().getMetaData().entityType(User.T).meta(Color.T).exclusive();
	}

	private void makeUserColorful() {
		PersistenceGmSession session = platform.systemUserRelated().cortexSessionSupplier().get();
		GmMetaModel model = session.query().entity(GmMetaModel.T, TEST_MODEL_GID).withTraversingCriterion(PreparedTcs.everythingTc)
				.require();

		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(model).withSession(session).done();

		mdEditor.onEntityType(User.T).addMetaData(session.create(Color.T, COLOR_MD_GLOBAL_ID));

		session.commit();
	}

	private void notifyModelChange() {
		OnModelChanged omc = OnModelChanged.T.create();
		omc.setModelName(TEST_MODEL_NAME);

		platform.systemUserRelated().evaluator().eval(omc).get();
	}

}
