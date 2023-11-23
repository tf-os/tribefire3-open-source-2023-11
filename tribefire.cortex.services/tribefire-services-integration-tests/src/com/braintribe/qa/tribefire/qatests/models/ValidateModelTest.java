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
package com.braintribe.qa.tribefire.qatests.models;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.cortexapi.model.ValidateModel;
import com.braintribe.model.cortexapi.model.ValidateModelResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.qa.tribefire.qatests.QaTestHelper;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

public class ValidateModelTest extends AbstractTribefireQaTest {
	private ImpApi imp;
	private GmMetaModel familyModel;

	@Before
	public void init() {
		imp = ImpApiFactory.buildWithDefaultProperties();
		familyModel = QaTestHelper.createFamilyModel(imp, modelName("Family"));
	}

	@Test
	public void test() {

		ValidateModel validateModel = ValidateModel.T.create();
		validateModel.setModel(familyModel);

		// @formatter:off
		List<String> responseNotifications = imp.service()
			.withNotificationResponse(validateModel, ValidateModelResponse.T)
			.callAndGetMessages();
		// @formatter:on

		logger.info("Recieved response messages: " + responseNotifications);
		Assertions.assertThat(responseNotifications).hasSize(1);
	}

	@After
	public void cleanUp() {
		eraseTestEntities();
	}
}
