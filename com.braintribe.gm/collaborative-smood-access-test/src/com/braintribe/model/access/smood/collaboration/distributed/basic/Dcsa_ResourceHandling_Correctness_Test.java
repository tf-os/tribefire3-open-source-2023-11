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
package com.braintribe.model.access.smood.collaboration.distributed.basic;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.AbstractCsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.AbstractCsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.deployment.DcsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.deployment.InMemoryDcsaSharedStorage;
import com.braintribe.model.access.smood.collaboration.distributed.AbstractDcsaTestBase;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.junit.assertions.assertj.core.api.FileSystemAssert;

/**
 * @author peter.gazdik
 */
public class Dcsa_ResourceHandling_Correctness_Test extends AbstractDcsaTestBase {

	private DcsaDeployedUnit dcsaUnit1;
	private DcsaDeployedUnit dcsaUnit2;

	private FileSystemAssert fsAssert1;
	private FileSystemAssert fsAssert2;

	@Before
	public void setup() {
		dcsaUnit1 = deployDcsa("access.dcsa", 1);
		dcsaUnit2 = deployDcsa("access.dcsa", 2);

		fsAssert1 = new FileSystemAssert(dcsaUnit1.baseFolder);
		fsAssert2 = new FileSystemAssert(dcsaUnit2.baseFolder);
	}

	@After
	public void cleanup() {
		cleanup(dcsaUnit1);
		cleanup(dcsaUnit2);
	}

	final String resourceName = "HelloResource";

	/** If absolute path handling needs to be tested to, adjust {@link AbstractCsaBuilder.ResourceUploadRequestEvaluator#eval(ServiceRequest) } */
	@Test
	public void managingResourceDistributedCorrect() throws Exception {
		String content1 = "Hello";
		InputStreamProvider isp = inputStreamProviderFor(content1);

		// create entity in DCSA-1
		Resource resource1 = dcsaUnit1.session.resources().create().name(resourceName).store(isp);
		dcsaUnit1.session.commit();

		// check entity in DCSA-2
		Resource resource2 = dcsaUnit2.session.findEntityByGlobalId(resource1.getGlobalId());
		Assertions.assertThat(resource2).isNotNull();
		Assertions.assertThat(resource2.getName()).isEqualTo(resourceName);

		// This performs lazy-loading, see AbstractCsaBuilder.ResourceUploadRequestEvaluator -> handleGetResource
		String content2 = readResourceContent(resource2);
		assertThat(content2).isEqualTo(content1);

		// check both file systems
		assertResourceInPlace(fsAssert1);
		assertResourceInPlace(fsAssert2);

		// delete resource in DCSA-1
		dcsaUnit1.session.resources().delete(resource1).delete();

		// update DCSA-2
		dcsaUnit2.session.query().entity(resource2).refresh();

		assertNoResourceAnywhere(fsAssert1);
		assertNoResourceAnywhere(fsAssert2);
	}

	/**
	 * The beginning here are the exact same steps as {@link #managingResourceDistributedCorrect()}, but with additional checks related to resource
	 * lazy-loading.
	 * <p>
	 * No deletion of the resources is done.
	 * <p>
	 * The original implementation didn't have lazy-loading, and while implementing it, I have decided to keep the original test to have a more simple
	 * version in case something not related to lazy-loading goes wrong - to have a simpler test pointing out the problem.
	 * 
	 * Step order:
	 * 
	 * <pre>
	 * D1: store Resource -> check resource path in local map
	 * D2: force update -> check resource path in local map
	 * D2: check actual resource file not found (for now, we delete it)
	 * D2: access resource - data is provided and file is retrieved
	 * </pre>
	 */
	@Test
	public void resourceLazyLoadingWorks() throws Exception {
		String content1 = "Hello";
		InputStreamProvider isp = inputStreamProviderFor(content1);

		// create entity in DCSA-1
		Resource resource1 = dcsaUnit1.session.resources().create().name(resourceName).store(isp);
		dcsaUnit1.session.commit();

		// check entity in DCSA-2
		Resource resource2 = dcsaUnit2.session.findEntityByGlobalId(resource1.getGlobalId());
		Assertions.assertThat(resource2).isNotNull();
		Assertions.assertThat(resource2.getName()).isEqualTo(resourceName);

		// Resource entity is indexed in DCSA-2, but was not loaded yet
		if (InMemoryDcsaSharedStorage.TMP_ENABLE_LAZY_LOADING)
			assertNoResourceAnywhere(fsAssert2);

		// This performs lazy-loading, see AbstractCsaBuilder.ResourceUploadRequestEvaluator -> handleGetResource
		String content2 = readResourceContent(resource2);
		assertThat(content2).isEqualTo(content1);

		// check both file systems
		assertResourceInPlace(fsAssert1);
		assertResourceInPlace(fsAssert2);
	}

	private void assertResourceInPlace(FileSystemAssert fsAssert) {
		// @formatter:off
		fsAssert.isDirectory()
			.sub(AbstractCsaDeployedUnit.resourcesFolderName).isDirectory()
				.sub(resourceName).isExistingFile();
		// @formatter:on
	}

	private void assertNoResourceAnywhere(FileSystemAssert fsAssert) {
		// @formatter:off
		fsAssert.base()
			.sub(AbstractCsaDeployedUnit.resourcesFolderName)
				.sub(resourceName).notExists_();
		// @formatter:on
	}

	private InputStreamProvider inputStreamProviderFor(String s) {
		return () -> new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	private String readResourceContent(Resource resource) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
			return reader.readLine();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected CollaborativeSmoodConfiguration prepareNewConfiguration() {
		return configForStages("stage0", "trunk");
	}

}
