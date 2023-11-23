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
package com.braintribe.model.access.hibernate;

import java.util.function.Consumer;

import org.hibernate.Session;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.detachrefs.AbstractAccessBasedReferenceDetacher;
import com.braintribe.model.processing.manipulator.api.ReferenceDetacherException;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * 
 */
public class HibernateReferenceDetacher extends AbstractAccessBasedReferenceDetacher<HibernateAccess> {

	private final Session applyManipulationSession;

	public HibernateReferenceDetacher(HibernateAccess access, CmdResolver cmdResolver, Session applyManipulationSession) {
		super(access, cmdResolver);
		this.applyManipulationSession = applyManipulationSession;
	}

	@Override
	protected void executeDetach(SelectQuery query, QualifiedProperty qualifiedProperty, Property property, GenericEntity entityToDetach,
			String detachProblem) throws ReferenceDetacherException {

		class Detacher implements Consumer<SelectQueryResult> {
			boolean hasMore;

			@Override
			public void accept(SelectQueryResult queryResult) throws RuntimeException {
				try {
					checkDetachAllowed(detachProblem, queryResult, qualifiedProperty, entityToDetach);
				} catch (ReferenceDetacherException e) {
					throw new RuntimeException(e);
				}

				removeReferences(queryResult, qualifiedProperty, property, entityToDetach);
				hasMore = queryResult.getHasMore();
			}
		}

		try {
			Detacher detacher = new Detacher();

			do {
				// TODO improve, no need to build the whole query over and over again, when it can be just re-used.
				// Also, we should investigate the flushing here
				access.doFor(query, detacher, applyManipulationSession);
			} while (detacher.hasMore);

		} catch (ModelAccessException e) {
			throw new ReferenceDetacherException("Error while detaching: " + entityToDetach, e);
		}
	}

	@Override
	protected SelectQueryResult executeQuery(SelectQuery query) throws ReferenceDetacherException {
		try {
			return access.query(query);

		} catch (ModelAccessException e) {
			throw new ReferenceDetacherException("Error while querying references.", e);
		}
	}

}
