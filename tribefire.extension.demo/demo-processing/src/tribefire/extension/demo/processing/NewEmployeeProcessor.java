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
package tribefire.extension.demo.processing;

import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.utils.lcd.StringTools;

import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.api.NewEmployeeResponse;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.HasComments;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.processing.tools.ServiceBase;

/**
 * {@link ServiceProcessor} implementation that executes steps that are necessary when a new employee joins a company.
 */
public class NewEmployeeProcessor implements AccessRequestProcessor<NewEmployee, NewEmployeeResponse> {

	private String welcomeMessage = "Hello $employeeFirstName $employeeLastName! Welcome to $company.";
	private String managerMessage = "Hello $managerFirstName $managerLastName! $employeeFirstName $employeeLastName just joined your department: $department!";

	/**
	 * The message added to the comments of the new employee. If null is passed the default message remains.
	 */
	@Configurable
	public void setWelcomeMessage(String welcomeMessage) {
		if (welcomeMessage != null) {
			this.welcomeMessage = welcomeMessage;
		}
	}

	/**
	 * The message added to the comments of the department manager. If null is passed the default message remains.
	 */
	@Configurable
	public void setManagerMessage(String managerMessage) {
		if (managerMessage != null) {
			this.managerMessage = managerMessage;
		}
	}

	@Override
	public NewEmployeeResponse process(AccessRequestContext<NewEmployee> context) {	
		NewEmployee request = context.getRequest();
		return new NewEmployeeCreator(
					request.getCompany(),
					request.getEmployee(),
					request.getDepartment()
				).run();
	}

	private boolean createNewEmployee(Person employee, Company company, Department department) {
		
		
		Set<Person> employees = company.getEmployees();
		
		// First check whether the person already is an employee of this company
		if (employees.contains(employee)) {
			return false;
		}
		
		// The new employee should be added to the employees of the company.
		employees.add(employee);

		// If a department is given we increase it's employee count and
		// inform the department manager
		if (department != null) {
			department.setNumberOfEmployees((department.getNumberOfEmployees() + 1));

			Person manager = department.getManager();
			if (manager != null) {
				addComment(manager, buildManagerMessage(employee, manager, department));
			}

		}

		// Finally we send a welcome message to the employee.
		addComment(employee, buildWelcomeMessage(employee, company));
		
		return true;
	}

	/**
	 * Takes an instance of HasComments and adds the given comment to it's comment list.
	 */
	private void addComment(HasComments hasComments, String comment) {
		hasComments.getComments().add(comment);
	}

	/**
	 * Prepares a welcome message for the new employee based on the configured template: {@link #welcomeMessage}
	 */
	private String buildWelcomeMessage(Person employee, Company company) {
		String message = welcomeMessage;
		message = StringTools.replaceAllOccurences(message, "$employeeFirstName", employee.getFirstName());
		message = StringTools.replaceAllOccurences(message, "$employeeLastName", employee.getLastName());
		message = StringTools.replaceAllOccurences(message, "$company", company.getName());
		return message;
	}

	/**
	 * Prepares a message for the Manager based on the configured template: {@link #managerMessage}.
	 */
	private String buildManagerMessage(Person employee, Person manager, Department department) {
		String message = managerMessage;
		message = StringTools.replaceAllOccurences(message, "$managerFirstName", manager.getFirstName());
		message = StringTools.replaceAllOccurences(message, "$managerLastName", manager.getLastName());
		message = StringTools.replaceAllOccurences(message, "$employeeFirstName", employee.getFirstName());
		message = StringTools.replaceAllOccurences(message, "$employeeLastName", employee.getLastName());
		message = StringTools.replaceAllOccurences(message, "$department", department.getName());
		return message;
	}
	
	private class NewEmployeeCreator extends ServiceBase {
		
		private Person employee;
		private Company company;
		private Department department;
		
		
		public NewEmployeeCreator(Company company, Person employee, Department department) {
			this.employee = employee;
			this.company = company;
			this.department = department;
		}
		
		
		public NewEmployeeResponse run() {
			
			// Ensure a company is given
			if (company == null) {
				return createConfirmationResponse("Please assign a company.", Level.WARNING, NewEmployeeResponse.T);
			}

			// Ensure an employee is given
			if (employee == null) {
				return createConfirmationResponse("Please assign an employee.", Level.WARNING, NewEmployeeResponse.T);
			}
			
			boolean added = createNewEmployee(employee, company, department);
			
			if (!added) {
				return createConfirmationResponse("The person "+employee.getFirstName()+" "+employee.getLastName()+" is already an employee of company "+company.getName(), Level.WARNING, NewEmployeeResponse.T);
			}

			/*
			addNotifications(
					com.braintribe.model.processing.notification.api.builder.Notifications.build()
					.add()
						.command()
						.gotoModelPath("List employees")
							//.addElement(company)
							.addElement(company, Company.employees)
						.close()
					.close()
					.add()
						.command().refresh("Refresh Company")
					.close()
					.list());
			*/
			return createConfirmationResponse("Added person "+employee.getFirstName()+" "+employee.getLastName()+" as employee to company "+company.getName(), Level.INFO,NewEmployeeResponse.T);
		}
		
	}
}
