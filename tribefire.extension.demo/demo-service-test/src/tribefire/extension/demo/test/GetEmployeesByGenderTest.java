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
package tribefire.extension.demo.test;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;

import tribefire.extension.demo.model.api.GetEmployeesByGender;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Gender;
import tribefire.extension.demo.model.data.Person;

public class GetEmployeesByGenderTest extends DemoTestBase {
	
	private Company company;

	@Before
	public void prepare() {
		company = testAccessSession.create(Company.T);
		
		Person person1 = testAccessSession.create(Person.T);
		person1.setFirstName("John");
		person1.setLastName("Doe");
		person1.setGender(Gender.male);
		
		Person person2 = testAccessSession.create(Person.T);
		person2.setFirstName("Jane");
		person2.setLastName("Doe");
		person2.setGender(Gender.female);
		
		Person person3 = testAccessSession.create(Person.T);
		person3.setFirstName("Sue");
		person3.setLastName("St. Doe");
		person3.setGender(Gender.female);
		
		company.setEmployees(asSet(person1, person2, person3));
		
		testAccessSession.commit();
	}
	
	@After
	public void cleanup() {
		List<GenericEntity> genericEntities = testAccessSession.query().entities(EntityQueryBuilder.from(GenericEntity.T).done()).list();

		for (GenericEntity genericEntity : genericEntities) {
			testAccessSession.deleteEntity(genericEntity, DeleteMode.dropReferences);
		}
		testAccessSession.commit();
	}
	
	@Test
	public void testGettingEmployeesByGender() {
		GetEmployeesByGender request = GetEmployeesByGender.T.create();
		request.setDomainId(testAccessSession.getAccessId());
		request.setCompany(company);
		request.setGender(Gender.male);
		
		List<GenericEntity> response = request.eval(testAccessSession).get();
		
		assertNotNull(response);
		assertEquals(1, response.size());
		assertEquals(Person.T.getTypeSignature(), response.get(0).entityType().getTypeSignature());
		assertEquals(Gender.male, ((Person)response.get(0)).getGender());
		
		request.setGender(Gender.female);
		
		response = request.eval(testAccessSession).get();
		
		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(Person.T.getTypeSignature(), response.get(0).entityType().getTypeSignature());
		assertEquals(Person.T.getTypeSignature(), response.get(1).entityType().getTypeSignature());
		assertEquals(Gender.female, ((Person)response.get(0)).getGender());
		assertEquals(Gender.female, ((Person)response.get(1)).getGender());
	}
	
}
