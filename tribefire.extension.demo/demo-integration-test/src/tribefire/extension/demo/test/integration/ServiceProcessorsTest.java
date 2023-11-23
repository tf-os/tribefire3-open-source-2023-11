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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmRootPathElement;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistryException;
import com.braintribe.model.processing.test.tools.comparison.PropertyByProperty;
import com.braintribe.model.uicommand.GotoModelPath;
import com.braintribe.model.user.User;
import com.braintribe.product.rat.imp.ReducedImpApi;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.testing.internal.tribefire.helpers.http.HttpGetHelper;
import com.braintribe.testing.internal.tribefire.helpers.http.HttpPostHelper;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import tribefire.extension.demo.model.api.FindByText;
import tribefire.extension.demo.model.api.GetEmployeeByGenderResponse;
import tribefire.extension.demo.model.api.GetEmployeesByGender;
import tribefire.extension.demo.model.api.GetEmployeesByGenderAsNotifications;
import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Gender;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.test.integration.utils.AbstractDemoTest;

/**
 * Tests the following Services: GetEmployeesByGender,
 * GetEmployeesByGenderAsNotifications, FindByText, NewEmployee <br>
 *
 * @author Neidhart
 *
 */
public class ServiceProcessorsTest extends AbstractDemoTest {

	private QueryHelper queryHelper;
	private ReducedImpApi demoAccessImp;

	private final static String TRIBEFIRE_SERVICES_URL = AbstractTribefireQaTest.apiFactory().getURL();
	private static String evalRequestBaseURL, basicParameters;

	private static String authenticate() {
		try {
			HttpPostHelper httpHelper = new HttpPostHelper(
					TRIBEFIRE_SERVICES_URL + "/api/v1/authenticate?user=cortex&password=cortex");

			String result = httpHelper.getContent();

			System.out.println(result);

			return result.replaceAll("\"", "").trim();
		} catch (IOException e) {
			throw new RuntimeException("Could not authenticate in tribefire via rest", e);
		}
	}

	@Before
	public void initLocal() {

		queryHelper = new QueryHelper(demoAccessSession);
		demoAccessImp = new ReducedImpApi(globalImp.getImpApiFactory(), demoAccessSession);

		// @formatter:off
		basicParameters = 
				demoAccessSession.getAccessId();

		evalRequestBaseURL = TRIBEFIRE_SERVICES_URL
				+ "/api/v1/" + basicParameters; 
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testServices() {
		logger.info("Test GetEmpoloyeesByGender service (both variants simultaneously)...");
		logger.info("Make sure that the result of both variants of the service match the result of the Java API...");

		for (Company company : queryHelper.allPersistedEntities(Company.T)) {
			getEmployeesViaBothVariantsAndAssertCorrectResult(Gender.male, company);
			getEmployeesViaBothVariantsAndAssertCorrectResult(Gender.female, company);
		}

		logger.info("Make sure correct exception is thrown when calling service with null arguments");
		assertNullParametersHandledCorrectly(Gender.female, Company.T.create(), this::getEmployeesViaSimpleService,
				NullPointerException.class);

		logger.info(
				"Make sure correct error notification is returned when calling notification variant of service with null arguments");
		Company anyCompany = queryHelper.findAny(Company.T);
		assertCorrectMessageNotificationWhenUsingParameters(null, anyCompany, "Please provide a gender.");
		assertCorrectMessageNotificationWhenUsingParameters(null, null, "Please provide a company.");
		assertCorrectMessageNotificationWhenUsingParameters(Gender.female, null, "Please provide a company.");

		logger.info("Make sure there is no result when calling service with non-existent company...");
		GetEmployeesByGender nonExistantCompanyRequest = GetEmployeesByGender.T.create();
		nonExistantCompanyRequest.setCompany(Company.T.create());
		nonExistantCompanyRequest.setGender(Gender.female);

		// using callWithoutRefresh because a non-existant company can't be refreshed
		assertThat((List<Person>) demoAccessImp.service(nonExistantCompanyRequest).callWithoutRefresh()).isEmpty();

		logger.info("Tests executed successfully. Now the FindByText service..");
		logger.info("Do some basic queries with and without wildcards where a result is expected...");
		findByTextAndAssertResultExists("*o", Person.T.create());
		findByTextAndAssertResultExists("*", Person.T.create());
		findByTextAndAssertResultExists("Doe", Person.T.create());

		findByTextAndAssertResultExists("Braintribe", Company.T.create());
		findByTextAndAssertResultExists("Marketing", Department.T.create());

		logger.info("Assert that there is no result for a highly unlikely query...");
		List<Person> nothingShouldBeFoundResult = findByText("ThisStrangeTextShouldNeverBeTheNameOfAnything!",
				Person.T.getTypeSignature());
		assertThat(nothingShouldBeFoundResult).isEmpty();

		logger.info("Assert  correct exception is thrown when stating unsupported entity type");
		assertThatThrownBy(() -> findByText("*", User.T.getTypeSignature()))
				.isExactlyInstanceOf(GmExpertRegistryException.class);

		logger.info("Test completed successfully!");

	}

	/**
	 * @return the persons found by the simple service version
	 */
	@SuppressWarnings("unchecked")
	private List<Person> getEmployeesViaSimpleService(Gender gender, Company company) {
		logger.info("Call simple service version for a " + gender + " employee from " + company.getName());
		GetEmployeesByGender getEmployeeRequest = GetEmployeesByGender.T.create();
		getEmployeeRequest.setCompany(company);
		getEmployeeRequest.setGender(gender);

		return (List<Person>) demoAccessImp.service(getEmployeeRequest).call();
	}

	/**
	 * @return the notifications returned by the notification version of the service
	 */
	private List<Notification> notificationVariantOfGetEmployees(Gender gender, Company company) {
		logger.info("Call notification service version for a " + gender + " employee from " + company);
		GetEmployeesByGenderAsNotifications getEmployeeRequest = GetEmployeesByGenderAsNotifications.T.create();
		getEmployeeRequest.setCompany(company);
		getEmployeeRequest.setGender(gender);

		GetEmployeeByGenderResponse response = demoAccessImp.service()
				.with(getEmployeeRequest, GetEmployeeByGenderResponse.T).call();

		return response.getNotifications();
	}

	private void assertCorrectMessageNotificationWhenUsingParameters(Gender gender, Company company,
			String expectedMessage) {
		List<Notification> notifications = notificationVariantOfGetEmployees(gender, company);
		assertThat(notifications).as("unexpected number of notifications returned").hasSize(1);

		MessageNotification messageNotification = (MessageNotification) notifications.get(0);

		assertThat(messageNotification.getMessage()).isEqualTo(expectedMessage);
	}

	private List<Person> getEmployeesViaBothVariantsAndAssertCorrectResult(Gender gender, Company company) {
		// ### Without notifications
		List<Person> serviceResult = getEmployeesViaSimpleService(gender, company);

		assertThat(serviceResult).as("found non-%s but requested a %s employee", gender, gender)
				.allMatch(x -> x.getGender() == gender);

		List<Person> controlResult = company.getEmployees().stream().filter(x -> x.getGender() == gender)
				.collect(Collectors.toList());

		assertThat(serviceResult).as("Found different employees via Java API and Service API").isEqualTo(controlResult);

		// ### With Notifications
		List<Notification> notifications = notificationVariantOfGetEmployees(gender, company);

		// in the current implementation of the service the notifications are in the
		// following order
		MessageNotification messageNotification = (MessageNotification) notifications.get(0);
		CommandNotification commandNotification = (CommandNotification) notifications.get(1);

		GotoModelPath command = (GotoModelPath) commandNotification.getCommand();
		List<GmModelPathElement> elements = command.getPath().getElements();
		GmRootPathElement element = (GmRootPathElement) elements.get(0);

		// assert that the notification variant returns the same elements like the other
		// one and spits out the expected
		// message
		@SuppressWarnings("unchecked")
		List<Person> notificationResult = (List<Person>) element.getValue();
		assertThat(notificationResult).as("Found different employees via Notification variant")
				.isEqualTo(controlResult);

		assertThat(messageNotification.getMessage()).as("Unexpected Message from notification").isEqualTo("Found "
				+ controlResult.size() + " " + gender + " employee(s) working at '" + company.getName() + "'.");

		return serviceResult;
	}

	private <T extends GenericEntity> void findByTextAndAssertResultExists(String text, T object) {

		String typeSignature = object.entityType().getTypeSignature();

		List<T> findByTextViaJavaApiResult = findByText(text, typeSignature);
		List<T> findByTextViaRestEvalResult = findByTextViaRestEval(text, typeSignature);

		assertThat(findByTextViaJavaApiResult)
				.as("Did not get any results for term '%s' of type '%s'", text, typeSignature).isNotEmpty();

		PropertyByProperty.checkEquality(0, findByTextViaJavaApiResult, findByTextViaRestEvalResult)
				.withMessage("Calling service via java api returns different result than via REST").assertThatEqual();
	}

	/**
	 * @return the persons returned by the "find by text" service called with given
	 *         parameters
	 */
	private <T extends GenericEntity> List<T> findByText(String text, String typeSignature) {
		FindByText findRequest = FindByText.T.create();
		findRequest.setText(text);
		findRequest.setType(typeSignature);

		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) demoAccessImp.service(findRequest).call();
		return result;
	}

	private <T extends GenericEntity> List<T> findByTextViaRestEval(String text, String typeSignature) {
		String requestType = FindByText.T.getShortName();
		String parameters = "sessionId=" + authenticate() //
				+ "&text=" + text //
				+ "&type=" + typeSignature;
		String requestUrl = evalRequestBaseURL + '/' + requestType + '?' + parameters;

		try {
			logger.info("Running REST request: " + requestUrl);
			HttpGetHelper httpGetHelper = new HttpGetHelper(requestUrl);
			logger.info("Got following JSON result: \n" + httpGetHelper.getContent());
			List<T> result = httpGetHelper.getUnmarshalledGmObject();
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Could not run http get request", e);
		}
	}

	@Test
	public void newEmployeeTest() {
		logger.info("Testing NewEmployee Request...");

		Company company = queryHelper.findAny(Company.T);
		logger.info("Found Company " + company.getName() + ". Execute request without setting Department...");

		newEmployeeTest(company, null);

		Department department = company.getDepartments().iterator().next();
		logger.info("Executing new request now for department " + department.getName());

		newEmployeeTest(company, department);

		logger.info("Test executed successfully");
	}

	/**
	 * Creates a NewEmployee request for given company and department and a newly
	 * created person and asserts that 1) the person was added as employee to the
	 * company 2) the manager of the department received a message 3) the employee
	 * received a message
	 */
	private void newEmployeeTest(Company company, Department department) {
		Person person = demoAccessSession.create(Person.T);
		person.setLastName("Zimmer");
		person.setFirstName("Hans");

		logger.info("Created new person.");

		demoAccessSession.commit();

		assertThat(company.getEmployees())
				.as("Person  %s already employee of company %s", person.getFirstName(), company.getName())
				.doesNotContain(person);

		NewEmployee newEmployee = NewEmployee.T.create();
		newEmployee.setCompany(company);
		newEmployee.setEmployee(person);
		newEmployee.setDepartment(department);

		demoAccessImp.service().with(newEmployee).call();

		logger.info("Person has now ID " + person.getId()
				+ ". Assert that this person was added to company's employees and expected notifications were sent");

		assertThat(company.getEmployees()).as("new employee was not added to company employees").contains(person);
		assertThat(person.getComments()).as("new employee did not recieve exactly one welcome message").hasSize(1);

		if (department != null) {
			Person manager = department.getManager();
			if (manager != null) {
				assertThat(manager.getComments()).as("Manager did not recieve new employee notification comment")
						.isNotEmpty();
			}
		}
	}

	private <A, B> void assertNullParametersHandledCorrectly(A defaultArgument1, B defaultArgument2,
			BiFunction<A, B, ?> method, Class<? extends Exception> exceptionClass) {
		assertThatThrownBy(() -> method.apply(null, defaultArgument2))
				.as("Expected exception was not thrown when calling service without arguments")
				.isExactlyInstanceOf(exceptionClass);

		assertThatThrownBy(() -> method.apply(null, null))
				.as("Expected Null pointer exception was not thrown when calling service without arguments")
				.isExactlyInstanceOf(exceptionClass);

		assertThatThrownBy(() -> method.apply(defaultArgument1, null))
				.as("Expected Null pointer exception was not thrown when calling service without arguments")
				.isExactlyInstanceOf(exceptionClass);
	}

}
