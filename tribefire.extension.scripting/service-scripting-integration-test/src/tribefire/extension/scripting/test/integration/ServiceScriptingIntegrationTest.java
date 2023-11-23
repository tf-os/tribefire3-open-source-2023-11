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
package tribefire.extension.scripting.test.integration;

import java.io.FileInputStream;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.PostProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.StringSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DateTools;

import tribefire.extension.scripting.deployment.model.GroovyScript;
import tribefire.extension.scripting.model.test.api.TestStringMap;
import tribefire.extension.scripting.model.test.api.TestText;
import tribefire.extension.scripting.model.test.api.TestText1;
import tribefire.extension.scripting.model.test.api.TestText2;
import tribefire.extension.scripting.model.test.api.TestText3;
import tribefire.extension.scripting.service.model.deployment.ScriptedServiceAroundProcessor;
import tribefire.extension.scripting.service.model.deployment.ScriptedServicePostProcessor;
import tribefire.extension.scripting.service.model.deployment.ScriptedServicePreProcessor;
import tribefire.extension.scripting.service.model.deployment.ScriptedServiceProcessor;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class ServiceScriptingIntegrationTest extends AbstractTribefireQaTest {

	private static final String SCRIPTING_TEST_API_MODEL = "tribefire.extension.scripting:test-api-model";

	private static Logger log = Logger.getLogger(ServiceScriptingIntegrationTest.class);

	private static Evaluator<ServiceRequest> evaluator;

	private static String serviceDomainExternalId;

	private static String uuid = DateTools.getCurrentDateString("yyyyMMddHHmmssSSS");

	private static ModelMetaDataEditor modelEditor;
//	private static ProcessWith processWithString;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		ImpApi imp = apiFactory().build();

		// multiple service domains
		GmMetaModel serviceConfigurationModel = imp.model()
				.create("tribefire.extension.scripting:integration-test-service-configuration-model" + "-" + uuid, SCRIPTING_TEST_API_MODEL).get();

		serviceDomainExternalId = "test-domain-" + uuid;

		PersistenceGmSession session = imp.session();
		ServiceDomain serviceDomain = session.create(ServiceDomain.T);
		serviceDomain.setExternalId(serviceDomainExternalId);
		serviceDomain.setName("test-domain");
		serviceDomain.setServiceModel(serviceConfigurationModel);

		modelEditor = BasicModelMetaDataEditor.create(serviceConfigurationModel).withSession(session).done();

		// --------------------------
		// Scripted Service Processor, NOTE this one is directly read from String using StringSource

		Resource scriptResource = session.create(Resource.T);
		StringSource sourceCode = session.create(StringSource.T);
		sourceCode.setContent("return $request.text.reverse();");
		scriptResource.setResourceSource(sourceCode);
		GroovyScript groovyScript = session.create(GroovyScript.T);
		groovyScript.setSource(scriptResource);

		ScriptedServiceProcessor processor = session.create(ScriptedServiceProcessor.T);
		processor.setName("ScriptedServiceProcessor");
		processor.setExternalId("processor." + uuid);
		processor.setScript(groovyScript);

		ProcessWith processWith = session.create(ProcessWith.T);
		processWith.setProcessor(processor);

		modelEditor.onEntityType(TestText.T).addMetaData(processWith);

		// --------------------------
		// Scripted Service PreProcessor

		Resource scriptResourcePre = session.resources().create().store(new FileInputStream("res/test-text-pre.groovy"));
		GroovyScript groovyScriptPre = session.create(GroovyScript.T);
		groovyScriptPre.setSource(scriptResourcePre);

		ScriptedServicePreProcessor preProcessor = session.create(ScriptedServicePreProcessor.T);
		preProcessor.setName("ScriptedServicePreProcessor");
		preProcessor.setExternalId("preProcessor." + uuid);
		preProcessor.setScript(groovyScriptPre);

		PreProcessWith processWithPre = session.create(PreProcessWith.T);
		processWithPre.setProcessor(preProcessor);

		modelEditor.onEntityType(TestText1.T).addMetaData(processWithPre);

		// --------------------------
		// Scripted Service PostProcessor

		Resource scriptResourcePost = session.resources().create().store(new FileInputStream("res/test-text-post.groovy"));
		GroovyScript groovyScriptPost = session.create(GroovyScript.T);
		groovyScriptPost.setSource(scriptResourcePost);

		ScriptedServicePostProcessor postProcessor = session.create(ScriptedServicePostProcessor.T);
		postProcessor.setName("ScriptedServicePostProcessor");
		postProcessor.setExternalId("postProcessor." + uuid);
		postProcessor.setScript(groovyScriptPost);

		PostProcessWith processWithPost = session.create(PostProcessWith.T);
		processWithPost.setProcessor(postProcessor);

		modelEditor.onEntityType(TestText2.T).addMetaData(processWithPost);

		// --------------------------
		// Scripted Service AroundProcessor

		Resource scriptResourceAround = session.resources().create().store(new FileInputStream("res/test-text-around.groovy"));
		GroovyScript groovyScriptAround = session.create(GroovyScript.T);
		groovyScriptAround.setSource(scriptResourceAround);

		ScriptedServiceAroundProcessor aroundProcessor = session.create(ScriptedServiceAroundProcessor.T);
		aroundProcessor.setName("ScriptedServiceAroundProcessor");
		aroundProcessor.setExternalId("aroundProcessor." + uuid);
		aroundProcessor.setScript(groovyScriptAround);

		AroundProcessWith processWithAround = session.create(AroundProcessWith.T);
		processWithAround.setProcessor(aroundProcessor);

		modelEditor.onEntityType(TestText3.T).addMetaData(processWithAround);

		// --------------------------
		// Generate return string from all available script interface objects

		Resource scriptResource4 = session.resources().create().name("bar-foo").store(new FileInputStream("res/check-interface.groovy"));
		GroovyScript groovyScript4 = session.create(GroovyScript.T);
		groovyScript4.setSource(scriptResource4);

		ScriptedServiceProcessor processor4 = session.create(ScriptedServiceProcessor.T);
		processor4.setName("ScriptedServiceProcessor4");
		processor4.setExternalId("processor4." + uuid);
		processor4.setScript(groovyScript4);

		ProcessWith processWith4 = session.create(ProcessWith.T);
		processWith4.setProcessor(processor4);

		modelEditor.onEntityType(TestStringMap.T).addMetaData(processWith4);

		// ------------------------

		session.commit();

		imp.deployable(processor).deploy();
		imp.deployable(preProcessor).deploy();
		imp.deployable(postProcessor).deploy();
		imp.deployable(aroundProcessor).deploy();
		imp.deployable(processor4).deploy(); // check ScriptTools + CommonScriptedProcessor interface

		evaluator = imp.session();

		log.info("Test preparation finished successfully!");
	}

	@Test
	public void testScriptedServiceProcessor() throws Exception {

		TestText request = TestText.T.create(); // DomainRequest
		request.setText("Lagerregal");
		request.setDomainId(serviceDomainExternalId);
		Maybe<String> reversedTextMaybe = request.eval(evaluator).getReasoned();

		if (reversedTextMaybe.isUnsatisfied())
			Assertions.fail(reversedTextMaybe.whyUnsatisfied().stringify());

		Assertions.assertThat(reversedTextMaybe.get()).isEqualTo("lagerregaL");
	}

	@Test
	public void testScriptedServicePreProcessor() throws Exception {

		TestText1 request = TestText1.T.create(); // DomainRequest
		request.setText("Lagerregal");
		request.setDomainId(serviceDomainExternalId);
		Maybe<String> reversedTextMaybe = request.eval(evaluator).getReasoned();

		if (reversedTextMaybe.isUnsatisfied())
			Assertions.fail(reversedTextMaybe.whyUnsatisfied().stringify());

		Assertions.assertThat(reversedTextMaybe.get()).isEqualTo("pre-lagerregaL");
	}

	@Test
	public void testScriptedServicePostProcessor() throws Exception {

		TestText2 request = TestText2.T.create(); // DomainRequest
		request.setText("Lagerregal");
		request.setDomainId(serviceDomainExternalId);
		Maybe<String> reversedTextMaybe = request.eval(evaluator).getReasoned();

		if (reversedTextMaybe.isUnsatisfied())
			Assertions.fail(reversedTextMaybe.whyUnsatisfied().stringify());

		Assertions.assertThat(reversedTextMaybe.get()).isEqualTo("lagerregaL-post");
	}

	@Test
	public void testScriptedServiceAroundProcessor() throws Exception {

		TestText3 request = TestText3.T.create(); // DomainRequest
		request.setText("Lagerregal");
		request.setDomainId(serviceDomainExternalId);
		Maybe<String> reversedTextMaybe = request.eval(evaluator).getReasoned();

		if (reversedTextMaybe.isUnsatisfied())
			Assertions.fail(reversedTextMaybe.whyUnsatisfied().stringify());

		Assertions.assertThat(reversedTextMaybe.get()).isEqualTo("lager-regaL");
	}

	@Test
	public void testScriptedCommonInterface() throws Exception {

		TestStringMap request = TestStringMap.T.create();
		request.setDomainId(serviceDomainExternalId);
		request.setMode(0); // normal script return. No error.
		Maybe<Map<String, Object>> testMaybe = request.eval(evaluator).getReasoned();

		if (testMaybe.isUnsatisfied())
			Assertions.fail(testMaybe.whyUnsatisfied().stringify());

		Map<String, Object> info = testMaybe.get();

		Assertions.assertThat((String)info.get("domain_id")).isEqualTo("test-domain-" + uuid);
		Assertions.assertThat(info.get("deployable_info")).isEqualTo("ScriptedServiceProcessor4");
		Assertions.assertThat((String)info.get("env_test")).isNotEmpty();
		Assertions.assertThat(info.get("script_name")).isEqualTo("bar-foo");
		Assertions.assertThat(info.get("session_factory")).isEqualTo("cortex");
		Assertions.assertThat(info.get("system_session")).isEqualTo("internal");
		Assertions.assertThat((String)info.get("created_entity")).contains("InvalidArgument");
		Assertions.assertThat(info.get("type_signature")).isEqualTo("string");
		Assertions.assertThat(info.get("is_authenticated_via_AC")).isEqualTo(true);
	}

	@Test
	public void testScriptReturnReason() throws Exception {

		TestStringMap request = TestStringMap.T.create();
		request.setDomainId(serviceDomainExternalId);
		request.setMode(1); // script return Reason (empty Maybe)
		Maybe<Map<String, Object>> testMaybe = request.eval(evaluator).getReasoned();

		if (testMaybe.isUnsatisfiedBy(InvalidArgument.T))
			return;

		Assertions.fail("no expected Reason returned");
	}

	@Test
	public void testScriptReturnMaybe() throws Exception {

		TestStringMap request = TestStringMap.T.create();
		request.setDomainId(serviceDomainExternalId);
		request.setMode(2); // script return Maybe
		Maybe<Map<String, Object>> testMaybe = request.eval(evaluator).getReasoned();

		if (testMaybe.isUnsatisfiedBy(InvalidArgument.T))
			return;

		Assertions.fail("no expected Maybe returned");
	}

}