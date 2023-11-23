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
package tribefire.extension.scripting.statechange.test.integration;
// ============================================================================

import static com.braintribe.utils.SysPrint.spOut;

// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

import java.io.FileInputStream;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.cortex.processorrules.MetaDataStateChangeProcessorRule;
import com.braintribe.model.extensiondeployment.meta.OnChange;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DateTools;

import tribefire.extension.scripting.deployment.model.GroovyScript;
import tribefire.extension.scripting.test.model.data.Person;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class StateChangeScriptingIntegrationTest extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(StateChangeScriptingIntegrationTest.class);

	private static PersistenceGmSession session_access;

	private static String uuid = DateTools.getCurrentDateString("yyyyMMddHHmmssSSS");

	private static final String TEST_DATA_MODEL = "tribefire.extension.scripting:test-data-model";

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		ImpApi imp = apiFactory().build();

		String accessId = "access.test-" + uuid;

		// multiple service domains
		GmMetaModel dataConfigurationModel = imp.model()
				.create("tribefire.extension.scripting:test-data-configuration-model-" + uuid, TEST_DATA_MODEL).get();

		CollaborativeSmoodAccess csa = imp.deployable().access().createCsa(accessId, accessId, dataConfigurationModel).get();

		PersistenceGmSession session = imp.session();

		AspectConfiguration aspects = session.create(AspectConfiguration.T);

		StateProcessingAspect stateProcessingAspect = session.create(StateProcessingAspect.T);
		stateProcessingAspect.setName("test.data.state.processing.aspect");
		stateProcessingAspect.setExternalId("test.data.state.processing.aspect-" + uuid);

		MetaDataStateChangeProcessorRule metaDataStateChangeProcessorRule = session.create(MetaDataStateChangeProcessorRule.T);
		metaDataStateChangeProcessorRule.setName("test.data.meta.data.rule");
		metaDataStateChangeProcessorRule.setExternalId("test.data.meta.data.rule-" + uuid);

		stateProcessingAspect.getProcessors().add(metaDataStateChangeProcessorRule);
		aspects.getAspects().add(stateProcessingAspect);
		csa.setAspectConfiguration(aspects);

		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(dataConfigurationModel).withSession(session).done();

		// --------------------------
		// after change

		Resource scriptResourceAfter = session.resources().create().store(new FileInputStream("res/afterChange.groovy"));
		GroovyScript groovyScriptAfter = session.create(GroovyScript.T);
		groovyScriptAfter.setSource(scriptResourceAfter);

		tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor afterProcessor = session
				.create(tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor.T);
		afterProcessor.setName("AfterStateChangeProcessor");
		afterProcessor.setExternalId("state-change-processor.test-after-" + uuid);
		afterProcessor.setAfterScript(groovyScriptAfter);

		OnChange onChangeAfter = session.create(OnChange.T);
		onChangeAfter.setProcessor(afterProcessor);

		modelEditor.onEntityType(Person.T).addPropertyMetaData(Person.familyName, onChangeAfter);

		// --------------------------
		// before change

		Resource scriptResourceBefore = session.resources().create().store(new FileInputStream("res/beforeChange.groovy"));
		GroovyScript groovyScriptBefore = session.create(GroovyScript.T);
		groovyScriptBefore.setSource(scriptResourceBefore);

		tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor beforeProcessor = session
				.create(tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor.T);
		beforeProcessor.setName("BeforeStateChangeProcessor");
		beforeProcessor.setExternalId("state-change-processor.test-before-" + uuid);
		beforeProcessor.setBeforeScript(groovyScriptBefore);
		beforeProcessor.setAfterScript(groovyScriptAfter);

		OnChange onChangeBefore = session.create(OnChange.T);
		onChangeBefore.setProcessor(beforeProcessor);

		modelEditor.onEntityType(Person.T).addPropertyMetaData(Person.firstName, onChangeBefore);

		// --------------------------
		// process change

		Resource scriptResourceProcess = session.resources().create().store(new FileInputStream("res/processChange.groovy"));
		GroovyScript groovyScriptProcess = session.create(GroovyScript.T);
		groovyScriptProcess.setSource(scriptResourceProcess);

		tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor processProcessor = session
				.create(tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor.T);
		processProcessor.setName("ProcessStateChangeProcessor");
		processProcessor.setExternalId("state-change-processor.test-process-" + uuid);
		processProcessor.setProcessScript(groovyScriptProcess);

		OnChange onChangeProcess = session.create(OnChange.T);
		onChangeProcess.setProcessor(processProcessor);

		modelEditor.onEntityType(Person.T).addPropertyMetaData(Person.role, onChangeProcess);

		// ------------------------

		session.commit();

		imp.deployable(afterProcessor).deploy();
		imp.deployable(beforeProcessor).deploy();
		imp.deployable(processProcessor).deploy();
		imp.deployable(metaDataStateChangeProcessorRule).deploy();
		imp.deployable(stateProcessingAspect).deploy();
		imp.deployable(csa).deploy();

		session_access = imp.switchToAccess(accessId).session();

		log.info("Test preparation finished successfully!");
		spOut("Test preparation finished successfully!");
	}

	@Test
	public void testScriptedAfterChangeProcessor() throws Exception {

		Person person = session_access.create(Person.T);
		person.setFamilyName("Mustermann");
		session_access.commit();
		Assertions.assertThat(person.getName()).isEqualTo("Mr. Mustermann");
	}

	@Test
	public void testScriptedBeforeChangeProcessor() throws Exception {

		Person person = session_access.create(Person.T);
		person.setFirstName("mia");
		session_access.commit();
		Assertions.assertThat(person.getState()).isEqualTo("modified");
	}

	@Test
	public void testScriptedProcessChangeProcessor() throws Exception {

		Person person = session_access.create(Person.T);
		person.setRole("Student");
		session_access.commit();

		// update is asynchronous
		Person personRefreshed = session_access.query().entity(person).refresh();
		int i = 0;
		for (; i < 10; ++i) {
			if (personRefreshed.getState() != null)
				break;
			Thread.sleep(10);
			personRefreshed = session_access.query().entity(person).refresh();
		}
		if (i >= 9) { // did not work
			Assertions.fail("The onProcessScript was not properly executed.");
		}
		Assertions.assertThat(personRefreshed.getState()).isEqualTo("Property \"role\" was changed to \"STUDENT\"");
	}

}