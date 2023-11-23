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

import static com.braintribe.model.processing.query.building.EntityQueries.from;
import static com.braintribe.model.processing.query.building.EntityQueries.property;
import static com.braintribe.model.processing.query.building.Queries.eq;

import java.util.List;

import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;

import tribefire.extension.demo.model.api.GetPersonsByName;
import tribefire.extension.demo.model.api.PaginatedPersons;
import tribefire.extension.demo.model.data.Person;

public class GetPersonsByNameProcessor implements AccessRequestProcessor<GetPersonsByName, PaginatedPersons> {

	@Override
	public PaginatedPersons process(AccessRequestContext<GetPersonsByName> context) {
		PersistenceGmSession session = context.getSession();
		GetPersonsByName request = context.getRequest();
		String name = request.getName();

		EntityQuery query = from(Person.T) //
				.where( //
						eq(property(Person.firstName), name) //
				);

		if (request.hasPagination())
			query = query.paging(request.getPageOffset(), request.getPageLimit());

		EntityQueryResult qr = session.query().entities(query).result();
		return toPaginatedPersons(qr);
	}

	private PaginatedPersons toPaginatedPersons(EntityQueryResult qr) {
		List<?> entities = qr.getEntities();

		PaginatedPersons result = PaginatedPersons.T.create();
		result.setPersons((List<Person>) entities);
		result.setHasMore(qr.getHasMore());

		return result;
	}

}
