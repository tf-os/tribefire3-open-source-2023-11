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
package com.braintribe.tribefire.cartridge.aws.integration.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.From;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Identity;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.CollectionTools2;

import tribefire.extension.audit.model.ManipulationRecord;
import tribefire.extension.audit.model.ManipulationType;
import tribefire.extension.audit.model.deployment.AuditAspect;
import tribefire.extension.audit.model.deployment.meta.Audited;
import tribefire.extension.audit.model.deployment.meta.AuditedPreserved;
import tribefire.extension.audit.model.deployment.meta.Unaudited;
import tribefire.extension.audit.processing.ManipulationRecordValueEncoder;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class AuditIntegrationTest extends AbstractTribefireQaTest {

	private static final String AUDIT_TEST_CONFIGURATION_MODEL = "tribefire.extension.audit:audit-test-configuration-model";
	private static final String DATA_AUDIT_TEST_CONFIGURATION_MODEL = "tribefire.extension.audit:data-audit-test-configuration-model";
	private static final String DATA_AUDIT_TEST_COMBINED_CONFIGURATION_MODEL = "tribefire.extension.audit:audit-test-combined-configuration-model";
	private static final String DATA_AUDIT_MODEL = "tribefire.extension.audit:data-audit-model";

	private static Logger log = Logger.getLogger(AuditIntegrationTest.class);

	private static PersistenceGmSession dataSession = null;
	private static PersistenceGmSession auditSession = null;
	private static PersistenceGmSession combinedSession = null;
	private static PersistenceGmSession untrackedSession;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");
		
		String uuid = DateTools.getCurrentDateString("yyyyMMddHHmmssSSS");

		ImpApi imp = apiFactory().build();

		GmMetaModel dataAuditConfigurationModel = imp.model().create(AUDIT_TEST_CONFIGURATION_MODEL + "-" + uuid, DATA_AUDIT_MODEL).get();
		
		Module module = imp.session().query().findEntity("module://tribefire.extension.audit:data-audit-module");

		PersistenceGmSession session = imp.session();

		
		////////////////////////////////////////////////////////
		// wiring for separate accesses for data and auditing //
		////////////////////////////////////////////////////////

		GmMetaModel configuredUserModel = imp.model().create(DATA_AUDIT_TEST_CONFIGURATION_MODEL + "-" + uuid, "com.braintribe.gm:user-model", "com.braintribe.gm:query-model").get();
		
		String dataAccessExternalId = "access.test.audit.data-" + uuid;
		String auditAccessExternalId = "access.test.audit.audit-" + uuid;
		String auditAspectExternalId = "access-aspect.test.audit-" + uuid;
		String aspectConfigurationId = "access-config.test.audit-" + uuid;
		
		SmoodAccess dataAccess = session.create(SmoodAccess.T);
		dataAccess.setExternalId(dataAccessExternalId);
		dataAccess.setGlobalId(dataAccessExternalId);
		dataAccess.setMetaModel(configuredUserModel);
		dataAccess.setName("Audit Test Data Smood");
		
		SmoodAccess auditAccess = session.create(SmoodAccess.T);
		auditAccess.setExternalId(auditAccessExternalId);
		auditAccess.setGlobalId(auditAccessExternalId);
		auditAccess.setMetaModel(dataAuditConfigurationModel);
		auditAccess.setName("Audit Test Audit Smood");
		
		AuditAspect auditAspect = session.create(AuditAspect.T);
		auditAspect.setAuditAccess(auditAccess);
		auditAspect.setName("Audit Test Aspect");
		auditAspect.setExternalId(auditAspectExternalId);
		auditAspect.setGlobalId(auditAspectExternalId);
		auditAspect.setModule(module);
		
		AspectConfiguration aspectConfiguration = session.create(AspectConfiguration.T);
		aspectConfiguration.setGlobalId(aspectConfigurationId);
		aspectConfiguration.getAspects().add(auditAspect);
		
		dataAccess.setAspectConfiguration(aspectConfiguration);
		
		//////////////////////////////////////////////////////
		// wiring for combined access for data and auditing //
		//////////////////////////////////////////////////////
		
		GmMetaModel configuredUserAndAuditModel = imp.model().create(DATA_AUDIT_TEST_COMBINED_CONFIGURATION_MODEL + "-" + uuid, "com.braintribe.gm:user-model", "com.braintribe.gm:query-model", "tribefire.extension.audit:data-audit-model").get();

		String combinedAccessExternalId = "access.test.combined-" + uuid;
		String combinedAspectConfigurationId = "access-config.test.combined.configuration-" + uuid;
		String combinedAspectExternalId = "access-config.test.combined-" + uuid;
		
		SmoodAccess combinedAccess = session.create(SmoodAccess.T);
		combinedAccess.setExternalId(combinedAccessExternalId);
		combinedAccess.setGlobalId(combinedAccessExternalId);
		combinedAccess.setMetaModel(configuredUserAndAuditModel);
		combinedAccess.setName("Audit Test Combined Smood");
		
		AuditAspect auditCombinedAspect = session.create(AuditAspect.T);
		auditCombinedAspect.setName("Audit Test Combined Aspect");
		auditCombinedAspect.setExternalId(combinedAspectExternalId);
		auditCombinedAspect.setGlobalId(combinedAspectExternalId);
		auditCombinedAspect.setModule(module);
		
		AspectConfiguration combinedAspectConfiguration = session.create(AspectConfiguration.T);
		combinedAspectConfiguration.setGlobalId(combinedAspectConfigurationId);
		combinedAspectConfiguration.getAspects().add(auditCombinedAspect);
		
		combinedAccess.setAspectConfiguration(combinedAspectConfiguration);
		
		///////////////////////////////////////////////////////
		// wiring for untracked access for data and auditing //
		///////////////////////////////////////////////////////
		
		String untrackedAccessExternalId = "access.test.untracked-" + uuid;
		String untrackedAspectConfigurationId = "access-config.test.untracked.configuration-" + uuid;
		String untrackedAspectExternalId = "access-config.test.untracked-" + uuid;
		
		SmoodAccess untrackedAccess = session.create(SmoodAccess.T);
		untrackedAccess.setExternalId(untrackedAccessExternalId);
		untrackedAccess.setGlobalId(untrackedAccessExternalId);
		untrackedAccess.setMetaModel(configuredUserAndAuditModel);
		untrackedAccess.setName("Audit Test Untracked Smood");
		
		AuditAspect auditUntrackedAspect = session.create(AuditAspect.T);
		auditUntrackedAspect.setName("Audit Test Untracked Aspect");
		auditUntrackedAspect.setExternalId(untrackedAspectExternalId);
		auditUntrackedAspect.setGlobalId(untrackedAspectExternalId);
		auditUntrackedAspect.getUntrackedRoles().add("tf-admin");
		auditUntrackedAspect.setModule(module);
		
		AspectConfiguration untrackedAspectConfiguration = session.create(AspectConfiguration.T);
		untrackedAspectConfiguration.setGlobalId(untrackedAspectConfigurationId);
		untrackedAspectConfiguration.getAspects().add(auditUntrackedAspect);
		
		untrackedAccess.setAspectConfiguration(untrackedAspectConfiguration);

		session.commit();
		
		BasicModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(configuredUserModel).withSession(session).done();

		modelEditor.onEntityType(Identity.T).addMetaData(session.create(Audited.T));
		modelEditor.onEntityType(Identity.T).addPropertyMetaData(session.create(Audited.T));
		modelEditor.onEntityType(User.T).addPropertyMetaData(User.lastName, session.create(AuditedPreserved.T));
		modelEditor.onEntityType(User.T).addPropertyMetaData(User.roles, session.create(AuditedPreserved.T));
		modelEditor.onEntityType(User.T).addPropertyMetaData(User.email, session.create(Unaudited.T));
		modelEditor.onEntityType(SelectQueryResult.T).addPropertyMetaData("results", session.create(AuditedPreserved.T));
		
		BasicModelMetaDataEditor auditModelEditor = BasicModelMetaDataEditor.create(dataAuditConfigurationModel).withSession(session).done();
		
		MaxLength maxLength = session.create(MaxLength.T);
		maxLength.setLength(20);
		
		auditModelEditor.onEntityType(ManipulationRecord.T).addPropertyMetaData(ManipulationRecord.value, maxLength);
		auditModelEditor.onEntityType(ManipulationRecord.T).addPropertyMetaData(ManipulationRecord.previousValue, maxLength);
		
		BasicModelMetaDataEditor combinedModelEditor = BasicModelMetaDataEditor.create(configuredUserAndAuditModel).withSession(session).done();
		combinedModelEditor.onEntityType(Role.T).addMetaData(session.create(Audited.T));
		combinedModelEditor.onEntityType(Role.T).addPropertyMetaData(Role.name, session.create(AuditedPreserved.T));

		session.commit();

		imp.deployable(auditAspect).redeploy();
		imp.deployable(auditAccess).redeploy();
		imp.deployable(dataAccess).redeploy();
		imp.deployable(auditCombinedAspect).redeploy();
		imp.deployable(combinedAccess).redeploy();
		imp.deployable(auditUntrackedAspect).redeploy();
		imp.deployable(untrackedAccess).redeploy();

		dataSession = imp.switchToAccess(dataAccessExternalId).session();
		auditSession = imp.switchToAccess(auditAccessExternalId).session();
		combinedSession  = imp.switchToAccess(combinedAccessExternalId).session();
		untrackedSession  = imp.switchToAccess(untrackedAccessExternalId).session();

		log.info("Test preparation finished successfully!");
	}
	

	@Test
	public void testCollectionsAndNonCollections() throws Exception {
		User user = dataSession.create(User.T);
		Role role1 = dataSession.create(Role.T);
		user.getRoles().add(role1);
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(User.T);
		// Note, the normalizer will change the "add" to a change value manipulation
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.roles).previousValue(null).value("(com.braintribe.m...");
		
		validate(auditSession, validator);
		
		user.setName("John");
		Role role2 = dataSession.create(Role.T);
		user.getRoles().add(role2);
		
		dataSession.commit();
		
		validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.name).previousValue(null).value("'John'");
		validator.add().kind(ManipulationType.added).type(User.T).property(User.roles).previousValue("(com.braintribe.m...").value("(com.braintribe.m...");
		
		validate(auditSession, validator);
		
		Role role3 = dataSession.create(Role.T);
		user.setRoles(CollectionTools2.asSet(role3));
	
		dataSession.commit();
		
		validator = new ManipulationRecordsValidator();
		
		//@formatter:off
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.roles)
				.previousValue("(com.braintribe.m...")
				.value("(com.braintribe.m...");
		//@formatter:on
		
		validate(auditSession, validator);
		
	}
	
	@Test
	public void testCombinedAccess() throws Exception {
		Role role = combinedSession.create(Role.T);
		role.setName("test");
		combinedSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(Role.T);
		validator.add().kind(ManipulationType.propertyChanged).type(Role.T).property(Role.name).previousValue(null).value("'test'");

		validate(combinedSession, validator);
	}
	
	@Test
	public void testUntrackedAspect() throws Exception {
		untrackedSession.create(User.T);
		untrackedSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		validate(untrackedSession, validator);
	}
	
	@Test
	public void testAuditAspect() throws Exception {
		Role role = dataSession.create(Role.T);
		User user = dataSession.create(User.T);
		Group group = dataSession.create(Group.T);
		
		group.setName("test-group");
		group.setEmail("group@mails.com");
		user.setName("test-user");
		user.setEmail("test-user@mails.com");
		user.setFirstName("Peter");
		user.setLastName("Lustig");
		role.setName("test-role");
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(User.T);
		validator.add().kind(ManipulationType.instantiated).type(Group.T);
		validator.add().kind(ManipulationType.propertyChanged).type(Group.T).property(Group.name).previousValue(null).value("'test-group'");
		validator.add().kind(ManipulationType.propertyChanged).type(Group.T).property(Group.email).previousValue(null).value("'group@mails.com'");
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.name).previousValue(null).value("'test-user'");
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.firstName).previousValue(null).value("'Peter'");
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.lastName).previousValue(null).value("'Lustig'");
		
		validate(auditSession, validator);
	}
	
	@Test
	public void testDelete() throws Exception {
		User user = dataSession.create(User.T);
		dataSession.commit();
		
		Object id = user.getId();
		String partition = user.getPartition();
		
		dataSession.deleteEntity(user);
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		String encodedId = ManipulationRecordValueEncoder.encode(BaseType.INSTANCE, id);
		
		validator.add().kind(ManipulationType.deleted).type(User.T).instanceId(encodedId).instancePartition(partition);
		
		validate(auditSession, validator);
		
	}
	
	@Test
	public void testUserInfo() throws Exception {
		dataSession.create(User.T);
		dataSession.commit();
		String userId = dataSession.getSessionAuthorization().getUserId();
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(User.T).user(userId).ipNotNull();
		
		validate(auditSession, validator);
	}
	
	@Test
	public void testNormalizationEffect() throws Exception {
		User user = dataSession.create(User.T);
		user.setName("Donald");
		user.setName("Dagobert");
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(User.T);
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.name).previousValue(null) //
			.value("'Dagobert'");
		
		validate(auditSession, validator);
	}
	
	@Test
	public void testOverflowPrevious() throws Exception {
		User user = dataSession.create(User.T);
		user.setLastName("12345678901234567890overflowytsch");
		dataSession.commit();
		
		user.setLastName("Kurmanowytsch");
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.lastName).previousValue("'1234567890123456...") //
			.value("'Kurmanowytsch'").overflowPreviousValue("'12345678901234567890overflowytsch'").overflowValue(null);
		
		validate(auditSession, validator);
	}
	
	@Test
	public void testPreviousIncrementalCollection() throws Exception {
		SelectQueryResult listRecord = dataSession.create(SelectQueryResult.T);
		listRecord.getResults().addAll(Arrays.asList(1, 2, 3));
		
		dataSession.commit();
		
		listRecord.getResults().addAll(Arrays.asList(3, 4, 5));
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.added).type(SelectQueryResult.T).property("results").previousValue("[1,2,3]").value("{3:3,4:4,5:5}");
		
		validate(auditSession, validator);
		
	}
	
	@Test
	public void testPreviousCollection() throws Exception {
		SelectQueryResult listRecord = dataSession.create(SelectQueryResult.T);
		listRecord.getResults().addAll(Arrays.asList(1, 2, 3));
		
		dataSession.commit();
		
		listRecord.setResults(Arrays.asList(3, 4, 5));
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.propertyChanged).type(SelectQueryResult.T).property("results").previousValue("[1,2,3]").value("[3,4,5]");
		
		validate(auditSession, validator);
		
	}
	@Test
	public void testPrevious() throws Exception {
		User user = dataSession.create(User.T);
		user.setLastName("romans-maiden-name");
		user.setFirstName("Roman");
		
		dataSession.commit();
		
		user.setLastName("Kurmanowytsch");
		user.setFirstName("romans-artist-name");
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.lastName).previousValue("'romans-maiden-name'") //
			.value("'Kurmanowytsch'").overflowValue(null);
		validator.add().kind(ManipulationType.propertyChanged).type(User.T).property(User.firstName).previousValue(null) //
			.value("'romans-artist-name'").overflowValue(null);
		
		validate(auditSession, validator);
	}
	
	@Test
	public void testOverflow() throws Exception {
		Group group = dataSession.create(Group.T);
		group.setEmail("12345678901234567890@mails.com");
		
		dataSession.commit();
		
		ManipulationRecordsValidator validator = new ManipulationRecordsValidator();
		
		validator.add().kind(ManipulationType.instantiated).type(Group.T);
		validator.add().kind(ManipulationType.propertyChanged).type(Group.T).property(Group.email).previousValue(null) //
			.value("'1234567890123456...").overflowValue("'12345678901234567890@mails.com'");
		
		validate(auditSession, validator);
	}


	private void validate(PersistenceGmSession session, ManipulationRecordsValidator validator) {
		List<ManipulationRecord> records = queryAuditLastTransaction(session);
		
		List<String> issues = validator.validate(records);
		
		if (!issues.isEmpty())
			fail(issues.stream().collect(Collectors.joining("\n")));
	}
	
	private static class TestSelectQueries extends SelectQueries {
		public static SelectQuery lastManipulationRecordTransactionId() {
			From r = source(ManipulationRecord.T);
			return from(r).orderBy(OrderingDirection.descending, property(r, ManipulationRecord.id)).limit(1).select(property(r, ManipulationRecord.transactionId));
		}
		
		public static SelectQuery manipulationRecordsOfTransaction(String transactionId) {
			
			From r = source(ManipulationRecord.T);
			return from(r) //
					.select(r) //
					.where(eq(property(r, ManipulationRecord.transactionId), transactionId)) //
					.orderBy(property(r, ManipulationRecord.sequenceNumber));
		}
	}
	
	private List<ManipulationRecord> queryAuditLastTransaction() {
		return queryAuditLastTransaction(auditSession);
	}
	
	private List<ManipulationRecord> queryAuditLastTransaction(PersistenceGmSession session) {
		String transactionId = session.query().select(TestSelectQueries.lastManipulationRecordTransactionId()).first();
		
		if (transactionId != null) {
			return session.query().select(TestSelectQueries.manipulationRecordsOfTransaction(transactionId)).list();
		}
		else {
			return Collections.emptyList();
		}
	}

	class ManipulationRecordValidator {
		
		private List<Function<ManipulationRecord, String>> validators = new ArrayList<>();
		
		private ManipulationRecordValidator validate(Function<ManipulationRecord, String> validator) {
			validators.add(validator);
			return this;
		}
		
		public ManipulationRecordValidator type(EntityType<?> entityType) {
			return validate(r -> {
				if (r.getInstanceType().equals(entityType.getTypeSignature()))
					return null;
				
				return "Manipulation target type mismatch. Found " + r.getInstanceType() + ", Expected: " + entityType.getTypeSignature(); 
			});
		}
		
		public ManipulationRecordValidator kind(ManipulationType kind) {
			return validate(r -> {
				if (r.getManipulationType() == kind)
					return null;
				
				return "Manipulation type mismatch. Found " + r.getManipulationType() + ", Expected: " + kind;
			});
		}
		
		public ManipulationRecordValidator property(String property) {
			return validate(r -> {
				if (property.equals(r.getInstanceProperty()))
					return null;
				
				return "Manipulation property mismatch. Found " + r.getInstanceProperty() + ", Expected: " + property;
			});
		}
		
		public ManipulationRecordValidator previousValue(Object value) {
			return validate(r -> {
				if (nullSafeEquals(value, r.getPreviousValue()))
					return null;
				
				return "Manipulation previous mismatch. Found " + r.getPreviousValue() + ", Expected: " + value;
			});
		}
		
		public ManipulationRecordValidator value(Object value) {
			return validate(r -> {
				if (nullSafeEquals(value, r.getValue()))
					return null;
				
				return "Manipulation value mismatch. Found " + r.getValue() + ", Expected: " + value;
			});
		}
		
		public ManipulationRecordValidator overflowValue(Object overflowValue) {
			return validate(r -> {
				if (nullSafeEquals(overflowValue, r.getOverflowValue()))
					return null;
				
				return "Manipulation overflowValue mismatch. Found " + r.getOverflowValue() + ", Expected: " + overflowValue;
			});
		}
		
		public ManipulationRecordValidator overflowPreviousValue(Object overflowPreviousValue) {
			return validate(r -> {
				if (nullSafeEquals(overflowPreviousValue, r.getOverflowPreviousValue()))
					return null;
				
				return "Manipulation overflowPreviousValue mismatch. Found " + r.getOverflowPreviousValue() + ", Expected: " + overflowPreviousValue;
			});
		}
		
		public ManipulationRecordValidator user(String user) {
			return validate(r -> {
				if (nullSafeEquals(user, r.getUser()))
					return null;
				
				return "Manipulation user mismatch. Found " + r.getUser() + ", Expected: " + user;
			});
		}
		
		public ManipulationRecordValidator instanceId(String id) {
			return validate(r -> {
				if (nullSafeEquals(id, r.getInstanceId()))
					return null;
				
				return "Manipulation instanceId mismatch. Found " + r.getInstanceId() + ", Expected: " + id;
			});
		}
		
		public ManipulationRecordValidator instancePartition(String partition) {
			return validate(r -> {
				if (nullSafeEquals(partition, r.getInstancePartition()))
					return null;
				
				return "Manipulation instancePartition mismatch. Found " + r.getInstancePartition() + ", Expected: " + partition;
			});
		}
		
		public ManipulationRecordValidator ipNotNull() {
			return validate(r -> {
				if (r.getUserIpAddress() != null)
					return null;
				
				return "Manipulation ip is null.";
			});
		}
		
		private boolean nullSafeEquals(Object v1, String v2) {
			if (v1 == v2)
				return true;
			
			if (v1 == null || v2 == null)
				return false;
			
			return v1.equals(v2);
		}
		
		public List<String> validate(int pos, ManipulationRecord record) {
			List<String> issues = new ArrayList<>();
			for (Function<ManipulationRecord, String> validator: validators) {
				String issue = validator.apply(record);
				
				if (issue != null)
					issues.add(pos + ". " + issue);
			}
			
			return issues;
		}

	}
	
	class ManipulationRecordsValidator {
		private List<ManipulationRecordValidator> validators = new ArrayList<>();
		
		public ManipulationRecordValidator add() {
			ManipulationRecordValidator builder = new ManipulationRecordValidator();
			validators.add(builder);
			return builder;
		}
		
		List<String> validate(List<ManipulationRecord> records) {
			List<String> allIssues = new ArrayList<String>();
			int i = 0;
			
			for (ManipulationRecord record: records) {
				if (validators.size() == i) {
					allIssues.add("Too many manipulation records");
					break;
				}
				ManipulationRecordValidator validator = validators.get(i);
				allIssues.addAll(validator.validate(i, record));
				i++;
			}
			
			if (i < validators.size())
				allIssues.add("Too few manipulation records");
			
			return allIssues; 
		}
		
	}
	
	

}