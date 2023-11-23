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
package com.braintribe.model.access.smood.collaboration.distributed;

import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.getStageStatsRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.mergeStageRequest;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.smood.collaboration.deployment.DcsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.DcsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.deployment.InMemoryDcsaSharedStorage;
import com.braintribe.model.access.smood.collaboration.deployment.TestFileSystemBinaryProcessor;
import com.braintribe.model.access.smood.collaboration.distributed.model.DcsaTestModel;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.testing.tools.TestTools;

/**
 * @author peter.gazdik
 */
public class AbstractDcsaTestBase {

	private final DcsaSharedStorage sharedStorage = new InMemoryDcsaSharedStorage();

	protected DcsaDeployedUnit deployDcsa(String accessId, int id) {
		return deployDcsa(accessId, baseFolder("" + id));
	}

	protected DcsaDeployedUnit redeploy(DcsaDeployedUnit deployedUnit) {
		return deployDcsa(deployedUnit.csa.getAccessId(), deployedUnit.baseFolder);
	}

	private DcsaDeployedUnit deployDcsa(String accessId, File baseFolder) {
		return DcsaBuilder.create() //
				.accessId(accessId) //
				.baseFolder(baseFolder) //
				.configurationSupplier(this::prepareNewConfiguration) //
				.cortex(true) //
				.model(DcsaTestModel.raw()) //

				// It is important same instance is not shared for same accessId
				.binaryProcessor(new TestFileSystemBinaryProcessor()) //

				.sharedStorage(sharedStorage) //
				.done();
	}

	protected void cleanup(DcsaDeployedUnit dcsaUnit) {
		if (dcsaUnit != null)
			dcsaUnit.cleanup();
	}

	protected CollaborativeSmoodConfiguration prepareNewConfiguration() {
		CollaborativeSmoodConfiguration configuration = CollaborativeSmoodConfiguration.T.create();
		configuration.getInitializers().add(manInitializer("trunk"));
		return configuration;
	}

	protected CollaborativeSmoodConfiguration configForStages(String... stageNames) {
		List<ManInitializer> manInitializers = Stream.of(stageNames) //
				.map(this::manInitializer) //
				.collect(Collectors.toList());

		CollaborativeSmoodConfiguration configuration = CollaborativeSmoodConfiguration.T.create();
		configuration.getInitializers().addAll(manInitializers);

		return configuration;
	}

	protected ManInitializer manInitializer(String name) {
		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName(name);

		return manInitializer;
	}

	protected static File baseFolder(String identifier) {
		return TestTools.newTempDirBuilder().relativePath("_BT", "TEST", "Dcsa-" + identifier).buildFile();
	}

	protected void mergeStage(DcsaDeployedUnit dcsaUnit, String source, String target) {
		dcsaUnit.eval(mergeStageRequest(source, target));
	}

	protected CollaborativeStageStats getStageStats(DcsaDeployedUnit dcsaUnit, String name) {
		return dcsaUnit.eval(getStageStatsRequest(name));
	}

}
