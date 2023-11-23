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
package com.braintribe.qa.tribefire.qatests.cartridges;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.commons.CommonMetaData;
import com.braintribe.qa.cartridge.main.model.data.Address;
import com.braintribe.qa.cartridge.main.model.deployment.service.TestDataAccessServiceProcessor;
import com.braintribe.qa.cartridge.main.model.service.TestAccessDataRequest;
import com.braintribe.qa.cartridge.main.model.service.TestAccessDataResponse;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

/**
 * This class creates an access which contains a {@code ServiceModel}, configures a respective {@code ServiceProcessor}
 * and tests and request/response behavior between the an {@code AccessDataRequest} and a {@code ServiceProcessor}.
 *
 *
 */
public class CartridgeDataServiceProcessorTest extends AbstractTribefireQaTest {

	private static final String DATA_MODEL_NAME = Address.T.getModel().name();
	private static final String SERVICE_MODEL_NAME = TestAccessDataRequest.T.getModel().name();

	@Before
	@After
	public void cleanup() {
		eraseTestEntities();
	}

	@Test
	public void testCartridgeDataServiceProcessor() throws GmSessionException {

		ImpApi imp = apiFactory().build();

		String currentProcessorName = nameWithTimestamp("ServiceProcessor");
		// @formatter:off
		TestDataAccessServiceProcessor accessServiceProcessor = (TestDataAccessServiceProcessor) imp.deployable()
				.serviceProcessor()
				.create(TestDataAccessServiceProcessor.T, currentProcessorName, currentProcessorName)
				.get();
		// @formatter:on

		imp.commit();
		imp.service().deployRequest(accessServiceProcessor).call();
		imp.commit();

		GmMetaModel dataModel = imp.model(DATA_MODEL_NAME).get();
		GmMetaModel serviceModel = imp.model(SERVICE_MODEL_NAME).get();

		// TODO: check why we need TC (instead of refresh) to sync EntityTypes of the ServiceModel
		imp.session().query().entity(serviceModel).withTraversingCriterion(PreparedTcs.everythingTc).refresh();

		String currentAccessName = nameWithTimestamp("Access");
		CollaborativeSmoodAccess smoodAccess = imp.deployable().access().createCsa(currentAccessName, currentAccessName, dataModel, serviceModel).get();

		ProcessWith processWith = imp.session().create(CommonMetaData.processWith);
		processWith.setProcessor(accessServiceProcessor);
		ModelMetaDataEditor metaDataEditor = imp.model(serviceModel).metaDataEditor();
		metaDataEditor.onEntityType(TestAccessDataRequest.T).removeMetaData(m -> m instanceof ProcessWith);
		metaDataEditor.onEntityType(TestAccessDataRequest.T).addMetaData(processWith);
		imp.commit();
		imp.service().deployRequest(smoodAccess).call();
		
		NotifyModelChanged notifyModelChanged = NotifyModelChanged.T.create();
		notifyModelChanged.setModel(serviceModel);
		notifyModelChanged.eval(imp.session()).get();

		// --------------------------------------------------------------------------------------------------------
		PersistenceGmSession smoodSession = apiFactory().newSessionForAccess(smoodAccess.getExternalId());

		Address address = smoodSession.create(Address.T);
		address.setStreet("Kandlgasse");

		smoodSession.commit();
		// --------------------------------------------------------------------------------------------------------

		TestAccessDataRequest accessDataRequest = TestAccessDataRequest.T.create();
		logger.info("Evaluating service request " + TestAccessDataRequest.class.getSimpleName() + " ...");
		accessDataRequest.setText(address.getStreet());
		TestAccessDataResponse response = (TestAccessDataResponse) smoodSession.eval(accessDataRequest).get();
		logger.info("Checking service response " + TestAccessDataResponse.class.getSimpleName() + " ...");
		assertThat(response).isNotNull();
		assertThat(response.getEcho()).isEqualTo("17");

		smoodSession.query().entity(address).refresh();
		assertThat(address.getStreetNumber()).isEqualTo(17);

		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: requesting an AccessDataRequest bound to a cartridge processor.");
		
		imp.model(serviceModel).metaDataEditor().onEntityType(TestAccessDataRequest.T).removeMetaData(m -> m instanceof ProcessWith);
	}
}
