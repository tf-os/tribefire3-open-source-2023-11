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
package com.braintribe.qa.tribefire.qatests.deployables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.qatests.deployables.access.AbstractPersistenceTest;
import com.braintribe.utils.CommonTools;

/**
 * tests the SetupAspects service on a temporary SMOOD access
 */
public class SetupAspectsTest extends AbstractPersistenceTest {

	// @formatter:off
	private static List<String> defaultAspectsExternalIds = CommonTools.getList(
			"aspect.fulltext.default",
			"aspect.idGenerator.default",
			"aspect.security.default",
			"aspect.stateProcessing.default");

	private static List<String> addedAspectMessages = defaultAspectsExternalIds.stream()
			.map(aspect -> "Added missing default aspect: " + aspect)
			.collect(Collectors.toList());
	// @formatter:on

	@Before
	@After
	public void tearDown() {
		eraseTestEntities();
	}

	@Test
	public void test() {
		logger.info("Starting DevQA-test: Testing SetupAspects service...");

		ImpApi imp = apiFactory().build();

		CollaborativeSmoodAccess testAccess = createAndDeployFamilyAccess(imp);

		if (testAccess.getAspectConfiguration() != null) {
			List<AccessAspect> aspects = testAccess.getAspectConfiguration().getAspects();
			assertThat(aspects).as("Unexpected aspects already defined in freshly created access").isEmpty();
		}

		String ensuredDefaultMessage = "Ensured default aspects for access: " + testAccess.getExternalId();
		String ensureAspectsMessage = "Ensure aspects on existing AspectConfiguration of access: " + testAccess.getExternalId();
		List<String> expectedMessages1 = new ArrayList<String>();
		expectedMessages1.add(ensuredDefaultMessage);
		expectedMessages1.addAll(addedAspectMessages);
		expectedMessages1.add("Create new AspectConfiguration for access: " + testAccess.getExternalId());

		logger.info("Executing service request with 'resetToDefault'=false...");
		List<String> response = imp.service().setupAspectsRequest(testAccess, false).callAndGetMessages();

		assertExpectedResponseAndResult(response, testAccess, expectedMessages1);

		logger.info("Executing same service request again...");
		response = imp.service().setupAspectsRequest(testAccess, false).callAndGetMessages();

		// @formatter:off
		assertExpectedResponseAndResult(response, testAccess, CommonTools.toList(
				ensuredDefaultMessage,
				"All default aspects were found on AspectConfiguration of access: " + testAccess.getExternalId() + ". Nothing added.",
				ensureAspectsMessage));
		// @formatter:on

		logger.info("Executing service request with 'resetToDefault'=true...");

		response = imp.service().setupAspectsRequest(testAccess, true).callAndGetMessages();

		List<String> expectedMessages2 = new ArrayList<String>();
		expectedMessages2.add(ensuredDefaultMessage);
		expectedMessages2.addAll(addedAspectMessages);
		expectedMessages2.add(ensureAspectsMessage);
		expectedMessages2.add("Cleaning aspects on existing AspectConfiguration of access: " + testAccess.getExternalId() + " (reset=true)");

		assertExpectedResponseAndResult(response, testAccess, expectedMessages2);
		logger.info("Test succeeded.");
	}

	/**
	 * + asserts that the response carries exactly the given expectedMessages in its notifications <br>
	 * + asserts that that the expected aspects are in the access's aspect configuration
	 */
	private void assertExpectedResponseAndResult(List<String> messages, CollaborativeSmoodAccess access, List<String> expectedMessages) {
		assertThat(messages).containsAll(expectedMessages).containsOnlyElementsOf(expectedMessages);

		assertThat(access.getAspectConfiguration()).as("No aspect configuration found for access " + access.getExternalId()).isNotNull();

		List<AccessAspect> aspects = access.getAspectConfiguration().getAspects();

		assertThat(aspects).extracting("externalId").containsExactlyInAnyOrder(defaultAspectsExternalIds.toArray());
	}

}
