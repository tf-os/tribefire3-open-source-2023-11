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
package com.braintribe.model.access.smood.collaboration.basic;

import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.getInitializersRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.getStageStatsRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.mergeStageRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.pushStageRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.renameStageRequest;
import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.resetCollaborativePersistence;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.smood.collaboration.deployment.CsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.CsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.manager.model.CsaTestModel;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.StrictErrorHandler;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.junit.assertions.assertj.core.api.FileSystemAssert;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.utils.FileTools;

/**
 * Base for CSA and {@link CollaborativeAccessManager} tests.
 * 
 * @see CollaborativePersistenceRequest
 * 
 * @author peter.gazdik
 */
public abstract class AbstractCollaborativePersistenceTest {

	public static final String trunkStageName = "trunk";

	protected CsaDeployedUnit csaUnit;
	protected CollaborativeSmoodAccess csa;
	protected PersistenceGmSession session;
	protected CollaborativeAccessManager accessManager;

	protected FileSystemAssert baseFolderFsAssert;

	/** Also works as re-deploy */
	@Before
	public void deploy() {
		deploy(TestTools.newTempDirBuilder().relativePath("_BT", "TEST", "Csa").buildFile());
	}

	protected void redeploy() {
		deploy(csaUnit.baseFolder);
	}

	private void deploy(File baseFolder) {
		csaUnit = CsaBuilder.create() //
				.baseFolder(baseFolder) //
				.cortex(useCortexSetup()) //
				.mergeModelAndData(mergeModelAndData()) //
				.configurationSupplier(this::prepareNewConfiguration) //
				.staticInitializers(preInitializers()).staticPostInitializers(postInitializers()) //
				.errorHandler(errorHandler()).model(model()) //
				.done();

		session = csaUnit.session;
		csa = csaUnit.csa;
		baseFolderFsAssert = FileSystemAssert.of(csaUnit.baseFolder);
	}

	protected List<PersistenceInitializer> preInitializers() {
		return emptyList();
	}

	protected List<PersistenceInitializer> postInitializers() {
		return emptyList();
	}

	protected GmmlManipulatorErrorHandler errorHandler() {
		return StrictErrorHandler.INSTANCE;
	}

	private GmMetaModel model() {
		GmMetaModel result = CsaTestModel.raw();

		if (useCortexSetup()) {
			GmMetaModel csaTestModel = result;
			GmMetaModel metaModel = GMF.getTypeReflection().getModel("com.braintribe.gm:meta-model").getMetaModel();

			result = MetaModelBuilder.metaModel("test:CsaCortexTestModle");
			result.setDependencies(asList(csaTestModel, metaModel));
		}

		return result;
	}

	private CollaborativeSmoodConfiguration prepareNewConfiguration() {
		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName(trunkStageName);

		CollaborativeSmoodConfiguration configuration = CollaborativeSmoodConfiguration.T.create();
		configuration.getInitializers().add(manInitializer);
		return configuration;
	}

	protected boolean useCortexSetup() {
		return true;
	}

	protected boolean mergeModelAndData() {
		return false;
	}

	@After
	public void cleanup() {
		csaUnit.cleanup();
	}

	// ###############################################
	// ## . . . . . . . . Requests . . . . . . . . .##
	// ###############################################

	protected List<SmoodInitializer> getInitializers() {
		return eval(getInitializersRequest());
	}

	protected CollaborativeStageStats getStageStats(String name) {
		return eval(getStageStatsRequest(name));
	}

	protected void renameStage(String oldName, String newName) {
		eval(renameStageRequest(oldName, newName));
	}

	protected void pushNewStage(String newStageName) {
		eval(pushStageRequest(newStageName));
	}

	protected void mergeStage(String source, String target) {
		eval(mergeStageRequest(source, target));
	}

	protected void resetPersistence() {
		eval(resetCollaborativePersistence());
	}

	protected <T> T eval(CollaborativePersistenceRequest request) {
		return csaUnit.eval(request);
	}

	// ###############################################
	// ## . . . . . . . . File Checks . . . . . . . ##
	// ###############################################

	protected String getTrunkFileContent(boolean model) {
		return getStageFileContent(trunkStageName, model);
	}

	protected String getStageFileContent(String stageName, boolean model) {
		File contentFile = csaUnit.stageManFile(stageName, model);

		return getFileContent(contentFile);
	}

	private static String getFileContent(File file) {
		if (!file.exists())
			throw new IllegalStateException("File not found: " + file.getAbsolutePath());

		return FileTools.read(file).asString();
	}

	// ###############################################
	// ## . . . . . . . . . Asserts . . . . . . . . ##
	// ###############################################

	protected void assertEntityStage(GenericEntity entity, String stageName) {
		PersistenceStage stage = csa.findStageForReference(entity.reference());
		Assertions.assertThat(stage.getName()).isEqualTo(stageName);
	}
}
