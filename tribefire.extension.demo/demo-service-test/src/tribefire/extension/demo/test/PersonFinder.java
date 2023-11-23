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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.processing.FindByTextProcessor.FindByTextExpert;

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