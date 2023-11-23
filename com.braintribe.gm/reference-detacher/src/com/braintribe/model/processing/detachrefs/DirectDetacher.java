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
package com.braintribe.model.processing.detachrefs;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.manipulator.api.ReferenceDetacherException;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public class DirectDetacher extends AbstractAccessBasedReferenceDetacher<IncrementalAccess> {

	public DirectDetacher(IncrementalAccess access, CmdResolver cmdResolver) {
		super(access, cmdResolver);
	}

	@Override
	protected void executeDetach(SelectQuery query, QualifiedProperty qualifiedProperty, Property property, GenericEntity entityToDetach,
			String detachProblem) throws ReferenceDetacherException {

		SelectQueryResult queryResult;
		do {
			queryResult = executeQuery(query);
			checkDetachAllowed(detachProblem, queryResult, qualifiedProperty, entityToDetach);
			removeReferences(queryResult, qualifiedProperty, property, entityToDetach);

		} while (queryResult.getHasMore());
	}

	@Override
	protected SelectQueryResult executeQuery(SelectQuery query) throws ReferenceDetacherException {
		try {
			return access.query(query);

		} catch (Exception e) {
			throw new ReferenceDetacherException("Error while querying entities.", e);
		}
	}

}
