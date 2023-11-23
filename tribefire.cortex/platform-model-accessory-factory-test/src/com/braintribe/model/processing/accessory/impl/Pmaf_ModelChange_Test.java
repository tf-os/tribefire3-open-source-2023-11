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
package com.braintribe.model.processing.accessory.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.display.Color;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentials;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_1B_CustomModel;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_2A_AccessAndServiceDomain;
import com.braintribe.model.processing.accessory.test.custom.model.CustomEntity;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * 
 */
public class Pmaf_ModelChange_Test extends AbstractPlatformModelAccessoryFactoryTest {

	private static final String COLOR_MD_GLOBAL_ID = "md:color";

	/**
	 * @see MaInit_1B_CustomModel
	 * @see MaInit_2A_AccessAndServiceDomain
	 */
	@Test
	public void onAccessModelChangePurgesAllCaches() throws Exception {
		// We make sure there is no Color MD configured od CustomEntity in the cortex (in case previous run didn't clean up properly)
		ensureNoColor();

		// Now we acquire MAs for an access and a service domain
		maf = contract.platformModelAccessoryFactory();
		customAccessMa = maf.getForAccess(CUSTOM_ACCESS_EXTERNAL_ID);
		customSdMa = maf.getForServiceDomain(CUSTOM_SERVICE_DOMAIN_EXTERNAL_ID);
		customModelMa = maf.getForModel(CUSTOM_MODEL_NAME);

		// we check that the CMD resolvers of both MAs do not resolve the Color MD
		assertNoColorMd(); // No MD configured -> no color

		// we put the Color MD on CustomEntity's GmEntityType
		makeCustomEntityColorful();

		// The CMD's still don't resolve the Color, as their internal caches have not been purged
		assertNoColorMd();

		// We trigger purging of internal caches related to custom model
		notifyCustomModelChange();

		// MD configured, caches purged -> yes color
		assertYesColorMd();
	}

	// Ensure No Color

	private void ensureNoColor() {
		PersistenceGmSession session = contract.cortexCsaDu().newSession();
		Color color = session.findEntityByGlobalId(COLOR_MD_GLOBAL_ID);
		if (color != null) {
			session.deleteEntity(color);
			session.commit();
		}
	}

	// Assert No Color

	private void assertNoColorMd() {
		assertThat(customEntityColorInfo(customAccessMa)).isNull();
		assertThat(customEntityColorInfo(customSdMa)).isNull();
		assertThat(customEntityColorInfo(customModelMa)).isNull();
	}

	// Set Color MD

	private void makeCustomEntityColorful() {
		PersistenceGmSession session = contract.cortexCsaDu().newSession();
		GmMetaModel model = session.query().entity(GmMetaModel.T, Model.modelGlobalId(CUSTOM_MODEL_NAME))
				.withTraversingCriterion(PreparedTcs.everythingTc).require();

		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(model).withSession(session).done();

		mdEditor.onEntityType(CustomEntity.T).addMetaData(session.create(Color.T, COLOR_MD_GLOBAL_ID));

		session.commit();
	}

	// Purge model-related Caches

	private void notifyCustomModelChange() {
		contract.platformModelEssentialsSupplier().onModelChange(CUSTOM_MODEL_NAME);
	}

	// Assert Yes Color

	private void assertYesColorMd() {
		assertColorMdAvailableInPme();
		assertColorMdAvailableInMa();
	}

	private void assertColorMdAvailableInPme() {
		PlatformModelEssentials accessPme = contract.platformModelEssentialsSupplier().getForAccess(CUSTOM_ACCESS_EXTERNAL_ID, null, true);
		PlatformModelEssentials sdPme = contract.platformModelEssentialsSupplier().getForServiceDomain(CUSTOM_SERVICE_DOMAIN_EXTERNAL_ID, null, true);
		PlatformModelEssentials modelPme = contract.platformModelEssentialsSupplier().getForModelName(CUSTOM_MODEL_NAME, null);

		assertColorMdAvailableInPme(accessPme);
		assertColorMdAvailableInPme(sdPme);
		assertColorMdAvailableInPme(modelPme);
	}

	private void assertColorMdAvailableInPme(PlatformModelEssentials pme) {
		GmEntityType customEntityGmType = pme.getOracle().getEntityTypeOracle(CustomEntity.T).asGmEntityType();

		boolean hasColorMd = customEntityGmType.getMetaData().stream().filter(Color.T::isInstance).findFirst().isPresent();
		assertThat(hasColorMd).isTrue();
	}

	private void assertColorMdAvailableInMa() {
		assertThat(customEntityColorInfo(customAccessMa)).isNotNull();
		assertThat(customEntityColorInfo(customSdMa)).isNotNull();
		assertThat(customEntityColorInfo(customModelMa)).isNotNull();
	}

	// Color MD resolution

	private Color customEntityColorInfo(ModelAccessory ma) {
		return ma.getCmdResolver().getMetaData().entityType(CustomEntity.T).meta(Color.T).exclusive();
	}

}
