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
package tribefire.extension.demo.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.utils.lcd.StringTools;

import tribefire.extension.demo.model.data.AuditRecord;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.model.deployment.RevenueNotificationProcessor;
import tribefire.extension.demo.test.integration.utils.AbstractDemoTest;

public class StateChangeProcessorsTest extends AbstractDemoTest {

	private QueryHelper queryHelper;

	// currently unused because not working
	private final static String OK_MESSAGE = "<$department ok>";
	private final static String RISK_MESSAGE = "<$department risk>";
	private final static String REVENUE_NOTIFICATION_PROCESSER_EXT_ID = "statechangeProcessor.revenueNotification";

	@Before
	public void initLocally() {
		queryHelper = new QueryHelper(demoAccessSession);
	}

	@Test
	public void testDepartmentRiskProcessor() {
		EntityQueryBuilder hasCompany = EntityQueryBuilder.from(Department.class).where().property("company").ne(null);
		logger.info("State change processor test...");
		logger.info("Testing Department risk processor...");

		// Department randomDepartment = queryHelper.findAny(hasCompany);
		//
		// setProfitable(randomDepartment, true);
		// setProfitable(randomDepartment, false);

		// DepartmentRiskProcessor riskProcessor = cortex.queries().findUnique(DepartmentRiskProcessor.T);
		// riskProcessor.setOkMessage(OK_MESSAGE);
		// riskProcessor.setRiskMessage(RISK_MESSAGE);
		//
		// cortex.commit();
		List<Department> companyDepartments = demoAccessSession.query().entities(hasCompany.done()).list();
		for (Department department : companyDepartments) {
			setProfitable(department, true);
			setProfitable(department, false);
		}
	}

	private List<AuditRecord> getCurrentlyPersistedAuditRecords() {
		return queryHelper.allPersistedEntities(AuditRecord.T);
	}

	@Test
	public void testAuditRecords() {
		logger.info("Checking audit records now...");
		List<AuditRecord> previousAuditRecords = getCurrentlyPersistedAuditRecords();

		logger.info("Asserting audit record on Person creation...");
		Person testPerson = demoAccessSession.create(Person.T);
		testPerson.setLastName("Rapunzel");
		testPerson.setFirstName("unknown");
		demoAccessSession.commit();
		assertThat(queryHelper.getNew(AuditRecord.T, previousAuditRecords)).hasSize(3);

		logger.info("Asserting audit record on Person update...");
		previousAuditRecords = getCurrentlyPersistedAuditRecords();
		testPerson.setFirstName("Heinrich");
		demoAccessSession.commit();
		assertThat(queryHelper.getNew(AuditRecord.T, previousAuditRecords)).hasSize(1);

		logger.info("Asserting audit record on Person delete...");
		previousAuditRecords = getCurrentlyPersistedAuditRecords();
		demoAccessSession.deleteEntity(testPerson);
		demoAccessSession.commit();
		assertThat(queryHelper.getNew(AuditRecord.T, previousAuditRecords)).hasSize(1);

		logger.info("Asserting that there is no audit record on Department creation...");
		previousAuditRecords = getCurrentlyPersistedAuditRecords();
		Department testDepartment = demoAccessSession.create(Department.T);
		testDepartment.setName("SneakyDepartment");
		demoAccessSession.commit();
		assertThat(queryHelper.getNew(AuditRecord.T, previousAuditRecords)).isEmpty();

		previousAuditRecords = getCurrentlyPersistedAuditRecords();
		demoAccessSession.deleteEntity(testDepartment);
		demoAccessSession.commit();
		assertThat(queryHelper.getNew(AuditRecord.T, previousAuditRecords)).isEmpty();
	}

	@Test
	public void testRevenueNotificationProcessor() {
		logger.info("testing RevenueNotification...");
		logger.info("Setting min and max revenue to custom values...");
		RevenueNotificationProcessor revenueProcessor = globalImp.deployable(RevenueNotificationProcessor.T, REVENUE_NOTIFICATION_PROCESSER_EXT_ID)
				.get();
		revenueProcessor.setMinRevenue(new BigDecimal(10));
		revenueProcessor.setMaxRevenue(new BigDecimal(100));
		globalImp.deployable(revenueProcessor).commitAndRedeploy();

		Company randomCompany = queryHelper.findAny(Company.T);
		Person ceo = randomCompany.getCeo();

		logger.info("retrieved random CEO " + ceo.getLastName() + " from company " + randomCompany.getName());
		logger.info("setting average revenue to a value between min and max and expecting no notification...");
		List<String> previousComments = new ArrayList<>(ceo.getComments());
		randomCompany.setAverageRevenue(new BigDecimal(10));
		demoAccessSession.commit();
		assertThat(ceo.getComments()).isEqualTo(previousComments);

		logger.info("Setting average revenue to first below min then above max and expecting notifications both times...");
		previousComments = new ArrayList<>(ceo.getComments());
		randomCompany.setAverageRevenue(new BigDecimal(9));
		demoAccessSession.commit();
		assertThat(ceo.getComments()).hasSize(previousComments.size() + 1);

		previousComments = new ArrayList<>(ceo.getComments());
		randomCompany.setAverageRevenue(new BigDecimal(109));
		demoAccessSession.commit();
		assertThat(ceo.getComments()).hasSize(previousComments.size() + 1);

		logger.info("Test completed successfully!");

	}

	private void setProfitableAndAssertCustomMessageWasSent(Department department, boolean isProfitable) throws InterruptedException {
		setProfitable(department, isProfitable);

		Person ceo = department.getCompany().getCeo();

		String messageTemplate = isProfitable ? OK_MESSAGE : RISK_MESSAGE;

		String message = ceo.getComments().get(ceo.getComments().size() - 1);
		String expectedMessage = StringTools.replaceAllOccurences(messageTemplate, "$department", department.getName());

		assertThat(message).isSameAs(expectedMessage);
	}

	/**
	 * sets the profitable property of a department, commits and asserts that a comment was added to the ceo
	 */
	private void setProfitable(Department department, boolean isProfitable) {
		logger.info("Setting department " + department.getName() + " to profitable=" + isProfitable);
		Person ceo = department.getCompany().getCeo();

		List<String> previousComments = new ArrayList<>(ceo.getComments());

		department.setProfitable(isProfitable);
		demoAccessSession.commit();

		logger.info("Check if a message/comment was sent to ceo");

		assertThat(ceo.getComments()).as("Expected CEO to recieve exactly 1 message but was dissappointed").hasSize(previousComments.size() + 1)
				.containsAll(previousComments);
	}

}
