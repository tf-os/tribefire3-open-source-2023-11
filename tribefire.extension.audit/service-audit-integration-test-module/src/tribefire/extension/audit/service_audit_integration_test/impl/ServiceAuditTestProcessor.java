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
package tribefire.extension.audit.service_audit_integration_test.impl;

import java.util.Map;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;

import tribefire.extension.audit.model.test.api.GetPersonData;
import tribefire.extension.audit.model.test.api.TestRequest;
import tribefire.extension.audit.model.test.data.Person;

public class ServiceAuditTestProcessor extends AbstractDispatchingServiceProcessor<TestRequest, Object> {

	private Map<String, Person> data;

	@Override
	protected void configureDispatching(DispatchConfiguration<TestRequest, Object> dispatching) {
		dispatching.registerReasoned(GetPersonData.T, this::getPersonData);
	}
	
	@Required
	public void setData(Map<String, Person> data) {
		this.data = data;
	}
	
	private Maybe<Person> getPersonData(ServiceRequestContext context, GetPersonData request) {
		
		String personId = request.getPersonId();
		
		if (personId == null)
			return Reasons.build(InvalidArgument.T).text("GetPersonData.personId must not be null").toMaybe();
			
		
		Person person = data.get(personId);
		
		if (person != null)
			return Maybe.complete(person);
		
		return Reasons.build(NotFound.T).text("Person with id " + personId + " not found").toMaybe();
	}
}
