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
package tribefire.extension.antivirus.integration.test.service;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.VerySlow;

import tribefire.extension.antivirus.integration.test.AbstractAntivirusTest;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ClamAVSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.CloudmersiveSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.VirusTotalSpecification;
import tribefire.extension.antivirus.model.service.request.ScanForVirus;
import tribefire.extension.antivirus.model.service.result.AbstractAntivirusResult;
import tribefire.extension.antivirus.model.service.result.VirusInformation;

@Category(VerySlow.class)
public class ServiceAntivirusTest extends AbstractAntivirusTest {

	private static final String INFECTION_PART_1 = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$";
	private static final String INFECTION_PART_2 = "EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";

	private static final String NOT_INFECTED_CONTENT = "test";

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	// Scan with ClamAV

	@Test
	public void scanWithClamAV_negative() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		ClamAVSpecification context = ClamAVSpecification.T.create();
		context.setURL("clamav.clamav");
		context.setPort(3310);
		context.setTimeout(20000);

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(NOT_INFECTED_CONTENT.getBytes()));

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isFalse();
	}

	@Test
	public void scanWithClamAV_positive() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		ClamAVSpecification context = ClamAVSpecification.T.create();
		context.setURL("clamav.clamav");
		context.setPort(3310);
		context.setTimeout(20000);

		String positiveTest = INFECTION_PART_1 + INFECTION_PART_2;

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(positiveTest.getBytes()));

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isTrue();
	}

	// Scan with VirusTotal

	@Test
	public void scanWithVirusTotal_negative() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		VirusTotalSpecification context = VirusTotalSpecification.T.create();
		context.setApiKey("4edf1164ffb931974a77168b202581f40249fab399f8ff0ed6b96edfe802d6ea");

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(NOT_INFECTED_CONTENT.getBytes()));
		resource.setName("not-infected");

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isFalse();
	}

	@Test
	public void scanWithVirusTotal_positive() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		VirusTotalSpecification context = VirusTotalSpecification.T.create();
		context.setApiKey("4edf1164ffb931974a77168b202581f40249fab399f8ff0ed6b96edfe802d6ea");

		String positiveTest = "abcd   " + INFECTION_PART_1 + INFECTION_PART_2 + "   abcd";

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(positiveTest.getBytes()));
		resource.setName("infected");

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isTrue();
	}

	// Scan with Cloudmersive

	@Test
	public void scanWithCloudmersive_negative() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		CloudmersiveSpecification context = CloudmersiveSpecification.T.create();
		context.setApiKey("2f2cd74e-d7f7-40fe-9858-5a5588901e78");

		// Create resource
		// Cloudmersive gives positive result to the "test" string
		String positiveTest = "not-infected";

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(positiveTest.getBytes()));
		resource.setName("not-infected");

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isFalse();
	}

	@Test
	public void scanWithCloudmersive_positive() throws Exception {
		ScanForVirus request = ScanForVirus.T.create();

		CloudmersiveSpecification context = CloudmersiveSpecification.T.create();
		context.setApiKey("2f2cd74e-d7f7-40fe-9858-5a5588901e78");

		// Create resource

		// Cloudmersive gives positive result to the "test" string
		String positiveTest = "test";

		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream(positiveTest.getBytes()));
		resource.setName("infected");

		request.setResources(asList(resource));
		request.setProviderSpecifications(asList(context));

		VirusInformation response = request.eval(cortexSession).get();
		List<AbstractAntivirusResult> results = response.getDetails();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getInfected()).isTrue();
	}

	// Health Check test

	@Test
	public void healthCheckTest() throws Exception {
		RunCheckBundles request = RunCheckBundles.T.create();

		CheckBundlesResponse response = request.eval(cortexSession).get();

		assertThat(response.getElements()).isNotEmpty();
		response.getElements().forEach(c -> {
			assertThat(c.getStatus()).isEqualTo(CheckStatus.ok);
		});

	}
}
