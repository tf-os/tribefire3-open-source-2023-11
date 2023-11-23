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
package tribefire.extension.audit.service.test;
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================



import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparisonResult;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.NullSafe;

import tribefire.extension.audit.model.ServiceAuditRecord;
import tribefire.extension.audit.model.deployment.AuditInterceptor;
import tribefire.extension.audit.model.deployment.meta.AuditDataPreservation;
import tribefire.extension.audit.model.deployment.meta.AuditPreservationDepth;
import tribefire.extension.audit.model.deployment.meta.Audited;
import tribefire.extension.audit.model.deployment.meta.CreateServiceAuditRecordWith;
import tribefire.extension.audit.model.deployment.meta.ServiceAuditPreservations;
import tribefire.extension.audit.model.integration.test.deployment.ServiceAuditIntegrationTestAuditRecordFactory;
import tribefire.extension.audit.model.integration.test.deployment.ServiceAuditIntegrationTestServiceProcessor;
import tribefire.extension.audit.model.test.TestServiceAuditRecord;
import tribefire.extension.audit.model.test.api.GetPersonData;
import tribefire.extension.audit.model.test.api.GetPersonData_Audited;
import tribefire.extension.audit.model.test.api.GetPersonData_CustomAudited;
import tribefire.extension.audit.model.test.api.GetPersonData_PreservedRequest;
import tribefire.extension.audit.model.test.api.GetPersonData_PreservedRequestAndResult;
import tribefire.extension.audit.model.test.api.GetPersonData_PreservedResult;
import tribefire.extension.audit.model.test.api.GetPersonData_ShallowPreservedResult;
import tribefire.extension.audit.model.test.api.TestRequest;
import tribefire.extension.audit.model.test.data.Person;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class ServiceAuditIntegrationTest extends AbstractTribefireQaTest {

	private static final String SERVICE_AUDIT_API_CONFIGURATION_MODEL = "tribefire.extension.audit:service-audit-api-configuration-model";
	private static final String SERVICE_AUDIT_DATA_CONFIGURATION_MODEL = "tribefire.extension.audit:service-audit-data-configuration-model";
	private static final String SERVICE_AUDIT_MODEL = "tribefire.extension.audit:service-audit-test-model";
	private static final String TEST_API_MODEL = "tribefire.extension.audit:service-audit-test-api-model";
	private static final String AUDIT_API_MODEL = "tribefire.extension.audit:service-audit-api-model";
	private static final String BASIC_RESOURCE_MODEL = FileSystemSource.T.getModel().getModelArtifactDeclaration().getName();

	private static Logger log = Logger.getLogger(ServiceAuditIntegrationTest.class);

	private static PersistenceGmSession dataSession = null;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");
		
		String uuid = DateTools.getCurrentDateString("yyyyMMddHHmmssSSS");

		ImpApi imp = apiFactory().build();

		GmMetaModel serviceAuditConfigurationModel = imp.model().create(SERVICE_AUDIT_API_CONFIGURATION_MODEL + "-" + uuid, TEST_API_MODEL, AUDIT_API_MODEL).get();
		
		// Module module = imp.session().query().findEntity("module://tribefire.extension.audit:service-audit-integration-test-module");

		PersistenceGmSession session = imp.session();

		///////////////////////////////////////////////////////////////////
		// wiring for separate domain and accesses for data and auditing //
		///////////////////////////////////////////////////////////////////

		GmMetaModel dataModel = imp.model().create(SERVICE_AUDIT_DATA_CONFIGURATION_MODEL + "-" + uuid, SERVICE_AUDIT_MODEL, BASIC_RESOURCE_MODEL).get();
		
		String dataAccessExternalId = "access.test.audit.data-" + uuid;
		String auditInterceptorExternalId = "processor.test.audit-" + uuid;
		String testProcessorExternalId = "processor.test-" + uuid;
		String testRecordFactoryExternalId = "processor.test-record-factory-" + uuid;
		
		SmoodAccess dataAccess = session.create(SmoodAccess.T);
		dataAccess.setExternalId(dataAccessExternalId);
		dataAccess.setGlobalId(dataAccessExternalId);
		dataAccess.setMetaModel(dataModel);
		dataAccess.setServiceModel(serviceAuditConfigurationModel);
		dataAccess.setName("Audit Test Data Smood");

		AuditInterceptor auditInterceptor = session.create(AuditInterceptor.T);
		auditInterceptor.setAuditAccess(dataAccess);
		auditInterceptor.setName("Audit Interceptor");
		auditInterceptor.setExternalId(auditInterceptorExternalId);
		auditInterceptor.setGlobalId(auditInterceptorExternalId);
		
		ServiceAuditIntegrationTestServiceProcessor testProcessor = session.create(ServiceAuditIntegrationTestServiceProcessor.T);
		testProcessor.setExternalId(testProcessorExternalId);
		testProcessor.setName("Test Service Processor");
		
		ServiceAuditIntegrationTestAuditRecordFactory testRecordFactory = session.create(ServiceAuditIntegrationTestAuditRecordFactory.T);
		testRecordFactory.setExternalId(testRecordFactoryExternalId);
		testRecordFactory.setName("Test Audit Record Factory");
		
		session.commit();
		
		BasicModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(serviceAuditConfigurationModel).withSession(session).done();

		ProcessWith processWith = session.create(ProcessWith.T);
		processWith.setProcessor(testProcessor);
		
		AroundProcessWith aroundProcessWith = session.create(AroundProcessWith.T);
		aroundProcessWith.setProcessor(auditInterceptor);
		
		Audited audited = session.create(Audited.T);
		
		AuditDataPreservation preservation = session.create(AuditDataPreservation.T);
		preservation.setDepth(AuditPreservationDepth.reachable);
		preservation.setMimeType("application/json");
		
		AuditDataPreservation shallowPreservation = session.create(AuditDataPreservation.T);
		shallowPreservation.setDepth(AuditPreservationDepth.shallow);
		shallowPreservation.setMimeType("application/json");
		
		ServiceAuditPreservations requestPreservation = session.create(ServiceAuditPreservations.T);
		requestPreservation.setRequestPreservation(preservation);
		
		ServiceAuditPreservations resultPreservation = session.create(ServiceAuditPreservations.T);
		resultPreservation.setResultPreservation(preservation);
		
		ServiceAuditPreservations shallowResultPreservation = session.create(ServiceAuditPreservations.T);
		shallowResultPreservation.setResultPreservation(shallowPreservation);
		
		ServiceAuditPreservations requestAndResultPreservation = session.create(ServiceAuditPreservations.T);
		requestAndResultPreservation.setRequestPreservation(preservation);
		requestAndResultPreservation.setResultPreservation(preservation);
		
		modelEditor.onEntityType(TestRequest.T).addMetaData(processWith);
		modelEditor.onEntityType(GetPersonData_Audited.T).addMetaData(audited);
		modelEditor.onEntityType(TestRequest.T).addMetaData(aroundProcessWith);
		modelEditor.onEntityType(GetPersonData_PreservedRequest.T).addMetaData(requestPreservation);
		modelEditor.onEntityType(GetPersonData_PreservedResult.T).addMetaData(resultPreservation);
		modelEditor.onEntityType(GetPersonData_ShallowPreservedResult.T).addMetaData(shallowResultPreservation);
		modelEditor.onEntityType(GetPersonData_PreservedRequestAndResult.T).addMetaData(requestAndResultPreservation);
		
		CreateServiceAuditRecordWith createServiceAuditRecordWith = session.create(CreateServiceAuditRecordWith.T);
		createServiceAuditRecordWith.setRecordFactory(testRecordFactory);
		
		modelEditor.onEntityType(GetPersonData_CustomAudited.T).addMetaData(createServiceAuditRecordWith);
		
		session.commit();

		imp.deployable(dataAccess).redeploy();
		imp.deployable(testProcessor).redeploy();
		imp.deployable(auditInterceptor).redeploy();
		imp.deployable(testRecordFactory).redeploy();

		dataSession = imp.switchToAccess(dataAccessExternalId).session();

		log.info("Test preparation finished successfully!");
	}
	
	private void checkPersonOne(Person person) {
		Assertions.assertThat((String)person.getId()).as("Returned Person has not the expected id").isEqualTo("one");
	}
	
	private <S extends DomainRequest, R> AuditedServiceEvaluation<S, R> auditedEvaluation(EntityType<S> type) {
		return new AuditedServiceEvaluation<S, R>(dataSession, type);
	}
	
	private <S extends GetPersonData, R extends Person> AuditedServiceEvaluation<S, R> auditedGetPersonDataEvaluation(EntityType<S> type) {
		return this.<S,R>auditedEvaluation(type) //
			.requestEnricher(r -> r.setPersonId("one")) //
			.responseValidator(this::checkPersonOne); 
	}

	@Test
	public void testUnaudited() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData.T) //
			.recordExpected(false) //
			.evaluate(); 
	}
	
	@Test
	public void testAudited() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_Audited.T) //
			.evaluate();
	}
	
	@Test
	public void testAuditedUnsatisifed() throws Exception {
		auditedEvaluation(GetPersonData_Audited.T) //
			.requestEnricher(r -> r.setPersonId("three")) //
			.expectedReason(NotFound.T) //
			.evaluate();
	}
	
	@Test
	public void testCustomAudited() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_CustomAudited.T) //
			.auditRecordValidator(this::validateCustomAuditRecord) //
			.evaluate();
	}
	
	private void validateCustomAuditRecord(ServiceAuditRecord record) {
		Assertions.assertThat(record).as("ServiceAuditRecord is not of expected custom sub type").isInstanceOf(TestServiceAuditRecord.class);
		
		TestServiceAuditRecord customRecord = (TestServiceAuditRecord)record;
		
		
		Assertions.assertThat(customRecord.getPersonId()).as("Unexpected value for TestServiceAuditRecord.personId").isEqualTo("one");
	}
	
	@Test
	public void testAuditedPreserveRequest() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_PreservedRequest.T) //
			.requestPreservation(AuditPreservationDepth.reachable) //
			.preservedRequestValidator(this::preservedRequestGetPersonDataOneValidator) //
			.evaluate();
	}
	
	private void preservedRequestGetPersonDataOneValidator(GetPersonData preservedRequest, GetPersonData actualRequest) {
		Assertions.assertThat(preservedRequest.getPersonId()).as("Unexpected GetPersonData.personId in preserved request").isEqualTo("one");
	}

	
	@Test
	public void testAuditedShallowPreserveResult() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_ShallowPreservedResult.T) //
			.resultPreservation(AuditPreservationDepth.reachable) //
			.preservedResultValidator(this::shallowPreservedResultPersonOneValidator)
			.evaluate();
	}
	
	@Test
	public void testAuditedPreserveResult() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_PreservedResult.T) //
			.resultPreservation(AuditPreservationDepth.reachable) //
			.preservedResultValidator(this::preservedResultPersonOneValidator)
			.evaluate();
	}
	
	@Test
	public void testAuditedPreserveResultUnsatisfied() throws Exception {
		auditedEvaluation(GetPersonData_PreservedResult.T) //
			.resultPreservation(AuditPreservationDepth.reachable) //
			.requestEnricher(r -> r.setPersonId("three")) //
			.preservedResultValidator(this::preservedResultNotFoundValidator) //
			.expectedReason(NotFound.T) //
			.evaluate();
	}
	
	private void preservedResultNotFoundValidator(Maybe<?> preservedMaybe, Maybe<?> actualMaybe) {
		Assertions.assertThat(preservedMaybe.isUnsatisfied()).as("Preserved result should be unsatisfied").isTrue();
		Assertions.assertThat(NotFound.T.isInstance(preservedMaybe.whyUnsatisfied())).as("").isTrue();
	}
	
	private void preservedResultPersonOneValidator(Maybe<Person> preservedMaybe, Maybe<Person> actualMaybe) {
		Assertions.assertThat(preservedMaybe.isSatisfied()).as("Preserved Person result should be satisfied").isTrue();
		
		Person preservedPerson = preservedMaybe.get();
		Person actualPerson = actualMaybe.get();
		
		AssemblyComparisonResult comparison = AssemblyComparison.build().enableTracking().compare(preservedPerson, actualPerson);
		boolean equal = comparison.equal();
		
		if (!equal)
			Assertions.fail("Preserved Person structure is not as expected: " + comparison.mismatchDescription());
	}
	
	private void shallowPreservedResultPersonOneValidator(Maybe<Person> preservedMaybe, Maybe<Person> actualMaybe) {
		Assertions.assertThat(preservedMaybe.isSatisfied()).as("Preserved Person result should be satisfied").isTrue();
		
		Person preservedPerson = preservedMaybe.get();
		Person actualPerson = actualMaybe.get();
		
		shallowPreserveCompare(Person.T, preservedPerson, actualPerson);
	}
	
	private <T extends GenericEntity> void shallowPreserveCompare(EntityType<T> type, T preservedEntity, T actualEntity) {
		for (Property p: type.getProperties()) {
			Object actualValue = p.get(actualEntity); 
			Object preservedValue = p.get(preservedEntity); 
					
			if (p.getType().isScalar() || p.isIdentifier()) {
				boolean eq = NullSafe.compare((Comparable<Comparable>)actualValue, (Comparable<Comparable>)preservedValue) == 0;
				if (!eq) 
					Assertions.fail(type.getTypeSignature() + "." + p.getName() + " has not the expected value");
			}
			else {
				if (actualValue == null) {
					if (preservedValue != null || p.isAbsent(preservedEntity)) 
						Assertions.fail(type.getTypeSignature() + "." + p.getName() + " has not the expected value");
				}
				else {
					if (preservedValue != null || !p.isAbsent(preservedEntity)) 
						Assertions.fail(type.getTypeSignature() + "." + p.getName() + " has not the expected value");
				}
			}
		}
	}
	
	@Test
	public void testAuditedPreserveRequestAndResult() throws Exception {
		auditedGetPersonDataEvaluation(GetPersonData_PreservedRequestAndResult.T) //
			.requestPreservation(AuditPreservationDepth.reachable) //
			.resultPreservation(AuditPreservationDepth.reachable) //
			.preservedRequestValidator(this::preservedRequestGetPersonDataOneValidator) //
			.preservedResultValidator(this::preservedResultPersonOneValidator)
			.evaluate();
	}
}