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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.qatests.deployables.access.AbstractPersistenceTest;
import com.braintribe.testing.internal.suite.FolderStructure;

/**
 * tests services SetupWorkbench and ConfigureWorkbench (Ensure Standard Folders option only) <br>
 * asserts workbench standard folder structure and workbench model structure
 */
public class SetupWorkbenchTest extends AbstractPersistenceTest {

	CollaborativeSmoodAccess testAccess;
	ImpApi imp;

	String testAccessId;

	String workbenchModelName;
	String workbenchAccessId;

	String ensuredWorkbenchMessage;
	String modelAlreadyExistsMessage;
	String createdNewAccessMessage;
	String createdNewModelMessage;
	String noWorkbenchAssignedMesage;
	String accessAlreadyConfiguredMessage;
	String reuseAccessMessageResetTrue;
	String reuseModelMessageResetTrue;
	String reuseAccessMessage;

	/**
	 * creates a new test access and asserts that there is no respective workbench or workbench model
	 */
	@Before
	public void init() {
		logger.info("Starting DevQA-test: Testing SetupWorkbench service...");

		imp = apiFactory().build();

		eraseTestEntities();

		testAccess = createAndDeployFamilyAccessWithTimestamp(imp);
		testAccessId = testAccess.getExternalId();

		workbenchModelName = getWorkbenchModelName(testAccess.getMetaModel());
		workbenchAccessId = testAccess.getExternalId() + ".wb";

		ensuredWorkbenchMessage = "Ensured workbench for access: " + testAccessId;
		createdNewAccessMessage = "Created new WorkbenchAccess for access: " + testAccessId;
		createdNewModelMessage = "Created new WorkbenchModel: " + workbenchModelName + " for access: " + testAccessId;
		noWorkbenchAssignedMesage = "No workbench access assigned to access: " + testAccessId;
		accessAlreadyConfiguredMessage = "There is already a WorkbenchAccess configured for: " + testAccessId;
		modelAlreadyExistsMessage = "A WorkbenchModel with name: " + workbenchModelName + " already exists.";
		reuseAccessMessageResetTrue = "Reuse existing WorkbenchAccess: " + workbenchAccessId + " of access: " + testAccessId + "  (reset=true)";
		reuseModelMessageResetTrue = "Reuse existing WorkbenchModel: " + workbenchModelName + "  (reset=true)";
		reuseAccessMessage = "Reuse existing WorkbenchAccess: " + workbenchAccessId + " which was not assigned to access: " + testAccessId;

		logger.info("Starting test for access " + testAccessId);

		EntityQuery entityQuery = EntityQueryBuilder.from(GmMetaModel.T).where().property("name").eq(workbenchModelName).done();
		GmMetaModel workbenchModel = imp.session().query().entities(entityQuery).unique();

		assertThat(workbenchModel).as("Workbench model already existed prior to this test: " + workbenchModelName).isNull();
		assertThat(testAccess.getWorkbenchAccess()).as("Unexpected workbench access already defined in freshly created access").isNull();
	}

	@Test
	public void testFolderStructure() {

		final String changed = "Changed";

		//@formatter:off
		// no workbech exists until now
		// create new
		assertForRequest(testAccess, false, false,
				ensuredWorkbenchMessage,
				createdNewAccessMessage,
				createdNewModelMessage,
				noWorkbenchAssignedMesage
				);

		// assert that workbench is like a virgin, deploy it, use ConfigureWorkbench service and assert result
		assertFreshlyCreatedWorkbench(testAccess);
		createStandardWorkbenchFolderStructureViaService(testAccess);
		assertStandardWorkbenchFolderStructure(testAccess);

		// calling SetupWorkbench again now should result in an error message
		assertForRequest(testAccess, false, false,
				accessAlreadyConfiguredMessage
				);

		assertStandardWorkbenchFolderStructure(testAccess);

		// remove workbenchAccess from access
		logger.info("removing workbench access from test access...");
		testAccess.setWorkbenchAccess(null);
		imp.commit();

		// now try with both reset flags on which should succeed
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue,
				reuseAccessMessage,
				noWorkbenchAssignedMesage
				);

		assertStandardWorkbenchFolderStructure(testAccess);

		// change name of workbench again to show once more that the workbench itself will not be changed
		testAccess.getWorkbenchAccess().setName(changed);
		imp.commit();
		// try again now with resetModel=false should give an error message again
		assertForRequest(testAccess, false, true,
				modelAlreadyExistsMessage,
				reuseAccessMessageResetTrue
				);

		assertStandardWorkbenchFolderStructure(testAccess);

		// try again now with both reset flags on should succeed
		// but with less messages than the last time as there was already an access configured
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue
				);

		assertStandardWorkbenchFolderStructure(testAccess);

		// @formatter:on

	}

	@Test
	public void testMessagesOnly() {

		//@formatter:off
		// no workbech exists until now
		// create new
		assertForRequest(testAccess, false, false,
				ensuredWorkbenchMessage,
				createdNewAccessMessage,
				createdNewModelMessage,
				noWorkbenchAssignedMesage
				);

		// calling SetupWorkbench again now should result in an error message
		assertForRequest(testAccess, false, false,
				accessAlreadyConfiguredMessage
				);

		// remove workbenchAccess from access
		logger.info("removing workbench access from test access...");
		testAccess.setWorkbenchAccess(null);
		imp.commit();

		// different kind of error messages expected
		assertForRequest(testAccess, false, false,
				accessAlreadyConfiguredMessage,
				reuseAccessMessage,
				noWorkbenchAssignedMesage
				);

		// now try with both reset flags on which should succeed
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue,
				reuseAccessMessage,
				noWorkbenchAssignedMesage
				);

		// try again now with resetModel=false should give an error message again
		assertForRequest(testAccess, false, true,
				modelAlreadyExistsMessage,
				reuseAccessMessageResetTrue
				);

		// try again now with both reset flags on should succeed
		// but with less messages than the last time as there was already an access configured
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue
				);

		// @formatter:on

	}

	@Test
	public void testWorkbenchReset() {

		final String defaultWorkbenchAccessName = defaultWorkbenchAccessName(testAccess);
		final String changed = "Changed";

		//@formatter:off
		// no workbench exists until now
		// create new
		assertForRequest(testAccess, false, false,
				ensuredWorkbenchMessage,
				createdNewAccessMessage,
				createdNewModelMessage,
				noWorkbenchAssignedMesage
				);

		assertThat(testAccess.getWorkbenchAccess().getName()).isEqualTo(defaultWorkbenchAccessName);

		// change name of workbench to show that the workbench itself will not be changed
		testAccess.getWorkbenchAccess().setName(changed);
		imp.commit();

		// calling SetupWorkbench again now should result in an error message
		assertForRequest(testAccess, false, false,
				accessAlreadyConfiguredMessage
				);

		// assert that name is still equal to what we changed it to before
		assertThat(testAccess.getWorkbenchAccess().getName()).isEqualTo(changed);

		// remove workbenchAccess from access and try again
		logger.info("removing workbench access from test access...");
		testAccess.setWorkbenchAccess(null);
		imp.commit();

		// now try with both reset flags on which should succeed
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue,
				reuseAccessMessage,
				noWorkbenchAssignedMesage
				);

		// assert that name is reset
		assertThat(testAccess.getWorkbenchAccess().getName()).isEqualTo(defaultWorkbenchAccessName);

		// change name of workbench again to show once more that the workbench itself will not be changed
		testAccess.getWorkbenchAccess().setName(changed);
		imp.commit();

		// try again now with resetModel=false should give an error message again
		assertForRequest(testAccess, false, true,
				modelAlreadyExistsMessage,
				reuseAccessMessageResetTrue
				);

		// assert that name is still equal to what we changed it to before
		assertThat(testAccess.getWorkbenchAccess().getName()).isEqualTo(changed);

		// try again now with both reset flags on should succeed
		// but with less messages than the last time as there was already an access configured
		assertForRequest(testAccess, true, true,
				ensuredWorkbenchMessage,
				reuseModelMessageResetTrue,
				reuseAccessMessageResetTrue
				);

		// assert that name is reset
		assertThat(testAccess.getWorkbenchAccess().getName()).isEqualTo(defaultWorkbenchAccessName);

		// @formatter:on

	}

	@After
	public void cleanUp() {
		eraseTestEntities();
	}

	/**
	 * currently the standard workbench comes already deployed - assert that it is so <br>
	 * currently the standard workbench is completely empty upon creation - assert that it is so<br>
	 */
	private void assertFreshlyCreatedWorkbench(CollaborativeSmoodAccess access) {
		logger.info("assert workbench model is like expected...");
		GmMetaModel accessWorkbenchModel = access.getWorkbenchAccess().getMetaModel();
		GmMetaModel accessModel = access.getMetaModel();

		assertThat(accessWorkbenchModel.getName()).isEqualTo(workbenchModelName);
		assertThat(accessWorkbenchModel.getDependencies()).contains(accessModel).extracting("name").contains("tribefire.cortex:workbench-model");
		assertThat(accessWorkbenchModel.getMetaData()).isEmpty();
		assertThat(accessWorkbenchModel.getEnumConstantMetaData()).isEmpty();
		assertThat(accessWorkbenchModel.getEnumTypeMetaData()).isEmpty();
		assertThat(accessWorkbenchModel.getTypeOverrides()).isEmpty();
		assertThat(accessWorkbenchModel.getTypes()).isEmpty();
		assertThat(accessWorkbenchModel.getVersion()).isEqualTo(accessModel.getVersion());

		logger.info("assert that workbench is in a fresh and new state and deploy...");
		String workbenchAccessId = access.getWorkbenchAccess().getExternalId();
		IncrementalAccess workbenchAccess = imp.deployable().access().with(workbenchAccessId).get();

		assertThat(workbenchAccess.getDeploymentStatus()).isEqualTo(DeploymentStatus.deployed);

		// TODO use workbench imp
		PersistenceGmSession workbenchAccessSession = apiFactory().buildSessionFactory().newSession(workbenchAccessId);

		EntityQuery folderQuery = EntityQueryBuilder.from(Folder.T).done();
		EntityQuery perspectiveQuery = EntityQueryBuilder.from(WorkbenchPerspective.T).done();
		List<WorkbenchPerspective> perspectives = workbenchAccessSession.query().entities(perspectiveQuery).list();
		List<Folder> folders = workbenchAccessSession.query().entities(folderQuery).list();

		assertThat(perspectives).isEmpty();
		assertThat(folders).isEmpty();
	}

	private void createStandardWorkbenchFolderStructureViaService(CollaborativeSmoodAccess access) {
		logger.info("create standard workbench folder structure via service...");

		List<String> response = imp.service().ensureStandardWorkbenchFoldersRequest(access).callAndGetMessages();

		response.forEach(m -> logger.warn("Message from SetupAccessResponse: " + m));
	}

	private void assertStandardWorkbenchFolderStructure(CollaborativeSmoodAccess access) {
		logger.info("Assert that workbench has standard folder structure...");
		String workbenchAccessId = access.getWorkbenchAccess().getExternalId();

		// TODO use workbench imp
		PersistenceGmSession workbenchAccessSession = apiFactory().buildSessionFactory().newSession(workbenchAccessId);

		FolderStructure.Factory assertFolderStructure = new FolderStructure.Factory(workbenchAccessSession);

		// @formatter:off

		assertFolderStructure.fromWorkbenchPerspective("root")
			.subFolder("root")
				.subFolder(access.getExternalId());

		assertFolderStructure.fromWorkbenchPerspective("homeFolder")
			.assertHasExactlyTheseSubfoldersAndNoOthers(); // ==> no subfolders

		assertFolderStructure.fromWorkbenchPerspective("actionbar")
			.subFolder("actionbar")
			.assertHasExactlyTheseSubfoldersAndNoOthers(
					"$exchangeContentView",
					"$workWithEntity",
					"$gimaOpener",
					"$deleteEntity",
					"$changeInstance",
					"$clearEntityToNull",
					"$addToCollection",
					"$insertBeforeToList",
					"$removeFromCollection",
					"$clearCollection",
					"$refreshEntities",
					"$ResourceDownload",
					"$executeServiceRequest",
					"$addToClipboard");

		assertFolderStructure.fromWorkbenchPerspective("headerbar")
			.subFolder("headerbar")
			.assertHasSubFolders(
					"tb_Logo",
					"$quickAccess-slot",
					"$settingsMenu",
						"$userMenu");

		assertFolderStructure.fromWorkbenchPerspective("global-actionbar")
			.subFolder("global-actionbar")
			.assertHasSubFolders(
					"$new",
					"$dualSectionButtons",
					"$upload",
					"$undo",
					"$redo",
					"$commit");

		// @formatter:on
	}

	/**
	 * @param testAccess
	 *            the access the service should be called upon
	 * @param resetModel
	 *            a parameter for the service request
	 * @param resetAccess
	 *            a parameter for the service request
	 * @param expectedMessages
	 *            exactly the messages you expect in the service response (in any order)
	 */
	private void assertForRequest(CollaborativeSmoodAccess testAccess, boolean resetModel, boolean resetAccess, String... expectedMessages) {
		logger.info("Calling SetupWorkbench service with resetModel=" + resetModel + " and resetAccess=" + resetAccess
				+ " and check response messages........");

		List<String> response = imp.service().setupWorkbenchRequest(testAccess, resetAccess, resetModel).callAndGetMessages();

		assertThat(response).containsExactlyInAnyOrder(expectedMessages);

	}

	private String getWorkbenchModelName(GmMetaModel model) {
		String modelName = model.getName();
		assertThat(modelName).endsWith("Model");

		String modelNameWithoutTrailingModel = modelName.substring(0, modelName.length() - "Model".length());
		return modelNameWithoutTrailingModel + "WorkbenchModel";
	}

	public String defaultWorkbenchAccessName(IncrementalAccess access) {
		String accessName = access.getName();
		String defaultWBAName = accessName.replaceAll("[Aa]ccess$", "") + "Workbench Access";
		return defaultWBAName;
	}
}
