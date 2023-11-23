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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.demo.model.api.FindByText;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Person;

public class FindByTextProcessor implements AccessRequestProcessor<FindByText,  List<GenericEntity>>  {

	private GmExpertRegistry registry = null;
	
	public void setRegistry(GmExpertRegistry registry) {
		this.registry = registry;
	}
	
	public FindByTextProcessor() {
		if (this.registry == null) {
			ConfigurableGmExpertRegistry registry = new ConfigurableGmExpertRegistry();
			registry.add(FindByTextExpert.class, Person.class, new PersonFinder());
			registry.add(FindByTextExpert.class, Company.class, new CompanyFinder());
			registry.add(FindByTextExpert.class, Department.class, new DepartmentFinder());
			this.registry = registry;
		}
	}
	
	
	@Override
	public List<GenericEntity> process(AccessRequestContext<FindByText> context) {
		
		FindByText request = context.getOriginalRequest();
		String type = request.getType();
		String text = request.getText();
		PersistenceGmSession session = context.getSession();
		
		
		FindByTextExpert finder = registry.getExpert(FindByTextExpert.class).forType(type);
		return finder.query(text, session);
		
	}

	public interface FindByTextExpert {
		List<GenericEntity> query (String text, PersistenceGmSession session);
	}
	
	public class PersonFinder implements FindByTextExpert {
		@Override
		public List<GenericEntity> query(String text, PersistenceGmSession session) {
			
			EntityQuery query = 
					EntityQueryBuilder
						.from(Person.T)
						.where()
							.disjunction()
								.property(Person.firstName).like(text+"*")
								.property(Person.lastName).like(text+"*")
							.close()
						.done();
			
			return session.query().entities(query).list();
		}
	}
	public class CompanyFinder implements FindByTextExpert {
		@Override
		public List<GenericEntity> query(String text, PersistenceGmSession session) {
			
			EntityQuery query = 
					EntityQueryBuilder
						.from(Company.T)
						.where()
							.property(Company.name).like(text+"*")
						.done();
			
			return session.query().entities(query).list();

			
		}
	}
	public class DepartmentFinder implements FindByTextExpert {
		@Override
		public List<GenericEntity> query(String text, PersistenceGmSession session) {
			EntityQuery query = 
					EntityQueryBuilder
						.from(Department.T)
						.where()
							.property(Department.name).like(text+"*")
						.done();
			
			return session.query().entities(query).list();

		}
	}
	 
	
}
