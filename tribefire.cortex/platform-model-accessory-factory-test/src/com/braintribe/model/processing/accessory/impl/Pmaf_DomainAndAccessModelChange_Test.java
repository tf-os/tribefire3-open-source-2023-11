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

import static com.braintribe.model.generic.reflection.Model.modelGlobalId;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_1B_CustomModel;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_2A_AccessAndServiceDomain;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.domain.ServiceDomain;

/**
 * 
 */
public class Pmaf_DomainAndAccessModelChange_Test extends AbstractPlatformModelAccessoryFactoryTest {

	private static final String OTHER_MODEL_NAME = "test:other-model";

	/**
	 * @see MaInit_1B_CustomModel
	 * @see MaInit_2A_AccessAndServiceDomain
	 */
	@Test
	public void onAccessModelChangePurgesAllCaches() throws Exception {
		maf = contract.platformModelAccessoryFactory();
		customAccessMa = maf.getForAccess(CUSTOM_ACCESS_EXTERNAL_ID);
		customSdMa = maf.getForServiceDomain(CUSTOM_SERVICE_DOMAIN_EXTERNAL_ID);

		assertModels(CUSTOM_MODEL_NAME);

		changeModelsInCortex();

		notifyMafThatModelsChanges();

		assertModels(OTHER_MODEL_NAME);
	}

	private void notifyMafThatModelsChanges() {
		maf.onAccessChange(CUSTOM_ACCESS_EXTERNAL_ID);
		maf.onServiceDomainChange(CUSTOM_SERVICE_DOMAIN_EXTERNAL_ID);
	}

	private void assertModels(String modelName) {
		assertCustomModel(customAccessMa.getModel(), modelName, "access");
		assertCustomModel(customSdMa.getModel(), modelName, "service domain");
	}

	private void assertCustomModel(GmMetaModel model, String modelName, String desc) {
		// custom access has an extension wrapper
		if ("access".equals(desc))
			model = extractInnerAccessModel(model);

		assertThat(model).as(desc + " model dependency should not be null").isNotNull();
		assertThat(model.getName()).isEqualTo(modelName);

	}

	private void changeModelsInCortex() {
		PersistenceGmSession session = contract.cortexCsaDu().newSession();

		GmMetaModel otherModel = otherModel(session);

		HardwiredAccess accessDenotation = session.getEntityByGlobalId(CUSTOM_ACCESS_GLOBAL_ID);
		ServiceDomain domainDenotation = session.getEntityByGlobalId(CUSTOM_SERVICE_DOMAIN_GLOBL_ID);

		accessDenotation.setMetaModel(otherModel);
		domainDenotation.setServiceModel(otherModel);

		session.commit();
	}

	private GmMetaModel otherModel(PersistenceGmSession session) {
		GmMetaModel customModel = session.getEntityByGlobalId(modelGlobalId(CUSTOM_MODEL_NAME));

		GmMetaModel result = session.create(GmMetaModel.T, modelGlobalId(OTHER_MODEL_NAME));
		result.setName(OTHER_MODEL_NAME);
		result.setVersion("1.0");
		result.getDependencies().add(customModel);

		return result;
	}

	private GmMetaModel extractInnerAccessModel(GmMetaModel model) {
		assertThat(model).as("access model should not be null").isNotNull();
		assertThat(model.getDependencies()).hasSize(1);
		return first(model.getDependencies());
	}

}
