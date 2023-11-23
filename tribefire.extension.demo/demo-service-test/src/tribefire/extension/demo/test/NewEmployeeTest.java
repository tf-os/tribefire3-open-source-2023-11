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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;

import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.api.NewEmployeeResponse;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Person;

public class NewEmployeeTest extends DemoTestBase {
	
	private Person person1;
	private Person person2;
	private Person person3;
	private Person person4;
	private Company company1;
	private Department department1;
	private Department department2;
	private Company company2;
	private Department department3;
	private Department department4;

	@Before
	public void prepare() {
		person1 = testAccessSession.create(Person.T);
		person1.setFirstName("John");
		person1.setLastName("Doe");
		
		person2 = testAccessSession.create(Person.T);
		person2.setFirstName("Jane");
		person2.setLastName("Doe");
		
		person3 = testAccessSession.create(Person.T);
		person3.setFirstName("Sue");
		person3.setLastName("St. Doe");
		
		person4 = testAccessSession.create(Person.T);
		person4.setFirstName("James");
		person4.setLastName("Doeman");
		
		company1 = testAccessSession.create(Company.T);
		company1.setName("Braintribe IT-Technologies GmbH");
		company1.setCeo(person1);
		
		department1 = testAccessSession.create(Department.T);
		department1.setName("R&D");
		department1.setCompany(company1);
		
		department2 = testAccessSession.create(Department.T);
		department2.setName("Marketing");
		department2.setCompany(company1);
		
		company1.setDepartments(asSet(department1, department2));
		
		company2 = testAccessSession.create(Company.T);
		company2.setName("Agile Documents GmbH");
		company2.setCeo(person2);
		
		department3 = testAccessSession.create(Department.T);
		department3.setName("R&D");
		department3.setCompany(company1);
		
		department4 = testAccessSession.create(Department.T);
		department4.setName("Backoffice");
		department4.setCompany(company1);
		
		company2.setDepartments(asSet(department3, department4));
		
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
	public void testAddingNewEmployee() {
		NewEmployee request = NewEmployee.T.create();
		request.setDomainId(testAccessSession.getAccessId());
		request.setCompany(company1);
		request.setDepartment(department1);
		request.setEmployee(person3);
		
		NewEmployeeResponse response = request.eval(testAccessSession).get();
		
		assertNotNull(response);
		assertEquals(1, department1.getNumberOfEmployees());
		assertEquals(0, department2.getNumberOfEmployees());
		assertEquals(1, company1.getEmployees().size());
		assertTrue(company1.getEmployees().contains(person3));
		
		request.setDepartment(department2);
		request.setEmployee(person4);
		response = request.eval(testAccessSession).get();
		
		assertNotNull(response);
		assertEquals(1, department1.getNumberOfEmployees());
		assertEquals(1, department2.getNumberOfEmployees());
		assertEquals(2, company1.getEmployees().size());
		assertTrue(company1.getEmployees().contains(person3));
		assertTrue(company1.getEmployees().contains(person4));
		
		// trying to add same employee one more time (it should fail)
		response = request.eval(testAccessSession).get();
		
		assertNotNull(response);
		assertEquals(1, department1.getNumberOfEmployees());
		assertEquals(1, department2.getNumberOfEmployees());
		assertEquals(2, company1.getEmployees().size());
	}

}
