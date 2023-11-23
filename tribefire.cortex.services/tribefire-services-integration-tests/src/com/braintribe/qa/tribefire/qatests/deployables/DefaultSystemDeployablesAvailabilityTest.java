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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.CollectionTools;

public class DefaultSystemDeployablesAvailabilityTest extends AbstractTribefireQaTest {

	@Test
	public void test() {

		ImpApi imp = apiFactory().build();

		logger.info("Checking if expected System deployables were found...");

		// @formatter:off
		String[] expectedDeployableIds = new String[] {
				"auth",
				"auth.wb",
				"cortex",
				"cortex.wb",
				"setup.wb",
				"setup",
				"user-sessions",
				"user-statistics",
				"workbench",
				"aspect.fulltext.default",
				"aspect.idGenerator.default"   ,
				"aspect.stateProcessing.default",
				"aspect.security.default",
				"processorRule.bidiProperty.default",
				"processorRule.metaData.default",
				"binaryPersistence.default",
				"binaryProcessor.fileSystem",
				"binaryRetrieval.template",
				"checkProcessor.hardwired.DatabaseConnectionsCheck",
				"checkProcessor.hardwired.MemoryCheckProcessor",
				"checkProcessor.hardwired.SelectedDatabaseConnectionsCheck",
				"checkProcessor.hardwired.BaseFunctionalityCheckProcessor",
				"checkProcessor.hardwired.BaseConnectivityCheckProcessor",
				"checkProcessor.hardwired.BaseVitalityCheckProcessor",
				"resourceEnricher.postPersistence.default",
				"resourceEnricher.prePersistence.default",
		};
		// @formatter:on

		List<Deployable> foundDeployables = imp.deployable().with(expectedDeployableIds).get();
		List<Deployable> foundTotalDeployables = imp.deployable().findAll(Deployable.T, "*");

		CollectionTools.getMissingElements(foundDeployables, foundTotalDeployables)
				.forEach(d -> logger.warn("Found unexpected deployable with external id: " + d.getExternalId()));

		assertThat(foundDeployables) //
				.extracting("globalId").as("System deployable has unexpected globalId") //
				.allMatch(globalId -> globalId.toString().startsWith("hardwired:") || globalId.toString().startsWith("default:"));

		assertThat(foundDeployables) //
				.extracting("externalId").as("System deployable not found by externalId") //
				.contains((Object[]) expectedDeployableIds);

		List<Deployable> undeployedDeployables = foundDeployables.stream() //
				.filter(d -> d.getDeploymentStatus() != DeploymentStatus.deployed) //
				.collect(Collectors.toList());

		assertThat(undeployedDeployables) //
				.as(() -> {
					return "These deployables are not deployed: " + undeployedDeployables.stream() //
							.map(d -> d.getDeploymentStatus().toString()) //
							.collect(Collectors.joining(", ")); //
				}) //
				.isEmpty();

		logger.info("Test successful.");
	}
}
