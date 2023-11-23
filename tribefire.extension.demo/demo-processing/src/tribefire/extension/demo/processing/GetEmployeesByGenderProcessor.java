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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notifications;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;

import tribefire.extension.demo.model.api.GetEmployeeByGenderResponse;
import tribefire.extension.demo.model.api.GetEmployeesByGender;
import tribefire.extension.demo.model.api.GetEmployeesByGenderAsNotifications;
import tribefire.extension.demo.model.api.GetEmployeesByGenderRequest;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Gender;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.processing.tools.ServiceBase;

public class GetEmployeesByGenderProcessor implements AccessRequestProcessor<GetEmployeesByGenderRequest, Object>  {
	
	private AccessRequestProcessor<GetEmployeesByGenderRequest, Object> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(GetEmployeesByGender.T, this::getEmployeesByGender);
		config.register(GetEmployeesByGenderAsNotifications.T, this::getEmployeesByGenderAsNotifications);
	});
	
	@Override
	public Object process(AccessRequestContext<GetEmployeesByGenderRequest> context) {
		return dispatcher.process(context);
	}
	
	private List<Person> getEmployeesByGender(AccessRequestContext<GetEmployeesByGender> context) {
		GetEmployeesByGender request = context.getRequest();
		return new EmployeeSearcher(
				request.getCompany(), 
				request.getGender()).run();
	}
	
	private Notifications getEmployeesByGenderAsNotifications(AccessRequestContext<GetEmployeesByGenderAsNotifications> context) {
		GetEmployeesByGenderAsNotifications request = context.getRequest();
		return new EmployeeSearcher(
				request.getCompany(), 
				request.getGender()).runWithNotifications();
	}

	private class EmployeeSearcher extends ServiceBase {
		private Company company;
		private Gender gender;
		
		public EmployeeSearcher(Company company, Gender gender) {
			this.company = company;
			this.gender = gender;
		}
		
		private GetEmployeeByGenderResponse runWithNotifications() {
			if (company == null) {
				return createConfirmationResponse("Please provide a company.", Level.INFO, GetEmployeeByGenderResponse.T);
			}
			if (gender == null) {
				return createConfirmationResponse("Please provide a gender.", Level.INFO, GetEmployeeByGenderResponse.T);
			}
			
			List<Person> genderedPersons = run();

			addNotifications(
				com.braintribe.model.processing.notification.api.builder.Notifications.build()
					.add()
						.command()
						.gotoModelPath("List employees")
							.addElement(GMF.getTypeReflection().getType(genderedPersons).getTypeSignature(),genderedPersons)
						.close()
					.close()
					.list()
			);

			return createResponse("Found "+genderedPersons.size()+" "+gender+" employee(s) working at '"+company.getName()+"'.", GetEmployeeByGenderResponse.T);
			
		}
		
		private List<Person> run() {
			
			Objects.requireNonNull(company, "request must have a company parameter");
			Objects.requireNonNull(gender, "request must have a gender parameter");
			
			List<Person> genderedPersons = company.getEmployees().stream().filter(p -> p.getGender() == gender).collect(Collectors.toList());
			return genderedPersons;
		}
		
		
	}

}
