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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;

import tribefire.extension.demo.model.api.GetOrphanedEmployees;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Person;

public class GetOrphanedEmployeesProcessor implements AccessRequestProcessor<GetOrphanedEmployees, Set<Person>>  {
	
	@Override
	public Set<Person> process(AccessRequestContext<GetOrphanedEmployees> context) {
		PersistenceGmSession session = context.getSession();
		Set<Object> orphanIds = getOrphanIds(session);
		
		Set<Person> orphanedEmployees = new HashSet<>(session.query().entities(EntityQueryBuilder.from(Person.T).where().property("id").in(orphanIds).done()).list());
		
		return orphanedEmployees;
	}
	
	/**
	 * Queries for all {@link Person} ids with a {@link JoinType#right}. By this, null values for company ids are
	 * returned, in case a person is neither an employee nor a CEO of the company. By intersection an identification of
	 * orphaned employees can be made and is returned.
	 */
	private Set<Object> getOrphanIds(PersistenceGmSession session) {
		Set<Object> ceoOrphansIds = new HashSet<>();
		Set<Object> empOrphansIds = new HashSet<>();

		//@formatter:off
		SelectQuery ceoOrphansQuery = new SelectQueryBuilder()
				.from(Company.T,"c")
					.rightJoin("c", Company.ceo, "ceo")
				.select("c", Company.id)
				.select("ceo",Person.id)
				.distinct()
				.done();
		
		SelectQuery empOrphansQuery = new SelectQueryBuilder()
				.from(Company.T,"c")
					.rightJoin("c", Company.employees, "emps")
				.select("c", Company.id)
				.select("emps",Person.id)
				.distinct()
				.done();
		//@formatter:on

		List<ListRecord> ceoOrphans = session.query().select(ceoOrphansQuery).list();
		List<ListRecord> empOrphans = session.query().select(empOrphansQuery).list();

		for (ListRecord record : ceoOrphans) {
			Object ceo = record.getValues().get(0);
			if (ceo == null)
				ceoOrphansIds.add(record.getValues().get(1));
		}

		for (ListRecord record : empOrphans) {
			Object emp = record.getValues().get(0);
			if (emp == null)
				empOrphansIds.add(record.getValues().get(1));
		}

		ceoOrphansIds.retainAll(empOrphansIds);
		return ceoOrphansIds;
	}
	
}
