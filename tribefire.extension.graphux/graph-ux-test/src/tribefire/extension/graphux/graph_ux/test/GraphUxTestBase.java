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
package tribefire.extension.graphux.graph_ux.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.graphux.graph_ux.test.wire.GraphUxTestWireModule;
import tribefire.extension.graphux.graph_ux.test.wire.contract.GraphUxTestContract;
import tribefire.extension.simple.model.data.Company;
import tribefire.extension.simple.model.data.Department;
import tribefire.extension.simple.model.data.Person;


public abstract class GraphUxTestBase {
	private static Logger logger = Logger.getLogger(GraphUxTestBase.class);
	
	protected static WireContext<GraphUxTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static GraphUxTestContract testContract;
	
	protected static PersistenceGmSession session;
	protected static SmoodAccess smoodAccess;
	
	protected static BasicModelMetaDataEditor mdEditor;

	
	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(GraphUxTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
		smoodAccess = testContract.access();
		session = GmTestTools.newSession(smoodAccess);
		mdEditor = new BasicModelMetaDataEditor(Company.T.getModel().getMetaModel());
		createExampleData();
	}
	
	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}
	
	private static void createExampleData() {
		logger.debug("Creating example data ...");

		Person person1 = session.create(Person.T); //Person.T.create();
		person1.setFirstName("Jane");
		person1.setLastName("Smith");

		Person person2 = session.create(Person.T);
		person2.setFirstName("John");
		person2.setLastName("Adams");

		Person person3 = session.create(Person.T);
		person3.setFirstName("Jack");
		person3.setLastName("Taylor");

		Person person4 = session.create(Person.T);
		person4.setFirstName("Jim");
		person4.setLastName("Taylor");
		person4.setFather(person3);

		Company company = session.create(Company.T);

		Department department1 = session.create(Department.T);
		department1.setName("Marketing");
		department1.setNumberOfEmployees(1);
		department1.setCompany(company);
		department1.setManager(person1);
		department1.setId(1L);

		Department department2 = session.create(Department.T);
		department2.setName("R&D");
		department2.setNumberOfEmployees(2);
		department2.setCompany(company);
		department2.setManager(person2);

		company.setName("Acme");
		company.setCeo(person3);
		company.setAverageRevenue(new BigDecimal("1234567890"));
		company.getEmployees().add(person1);
		company.getEmployees().add(person2);
		company.getEmployees().add(person3);
		company.getEmployees().add(person4);
		company.getDepartments().add(department1);
		company.getDepartments().add(department2);
		
		session.commit();

		// we obviously could create a lot more example data here

		logger.debug("Successfully created example data.");
	}

}
