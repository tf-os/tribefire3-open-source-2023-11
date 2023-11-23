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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EntityFlags;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.PropertyQuery;

public class LazyLoader extends PropertyAccessInterceptor {

	private PersistenceGmSession persistenceSession;

	public void setPersistenceSession(PersistenceGmSession persistenceSession) {
		this.persistenceSession = persistenceSession;
	}

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		if (EntityFlags.isShallow(entity) && !property.isIdentifying() && (!property.isGlobalId() || property.getDirectUnsafe(entity) == null  )) {
			loadShallowEntity(entity, property);
		}

		if (isVd) {
			return next.getProperty(property, entity, true);
		}
		
		Object value = next.getProperty(property, entity, false);
		if (VdHolder.isVdHolder(value)) {
			VdHolder.checkIsAbsenceInfo(value, entity, property);

			if (property.isIdentifying()) {
				throw new GenericModelException("This entity was not initialized properly, as one of it's identity properties (" +
						property.getName() + ") is absent.");
			}

			try {
				value = persistenceSession.query().property(preparePropertyQuery(property, entity)).value();
			} catch (Exception e) {
				throw new GenericModelException("Error while resolving absent " + property, e);
			}

			persistenceSession.suspendHistory();
			try {
				next.setProperty(property, entity, value, false);
			} finally {
				persistenceSession.resumeHistory();
			}

			value = next.getProperty(property, entity, false);
		}
		return value;
	}

	private PropertyQuery preparePropertyQuery(Property property, GenericEntity entity) {
		PropertyQuery query = PropertyQuery.T.create();
		query.setPropertyName(property.getName());
		query.setEntityReference((PersistentEntityReference) entity.reference());
		return query;
	}

	private void loadShallowEntity(GenericEntity entity, Property property) {
		try {
			TraversingCriterion tc = tcLoadingSimplePropsAnd(property);
			persistenceSession.query().entity(entity).withTraversingCriterion(tc).refresh();

		} catch (GmSessionException e) {
			// careful here - we cannot print any property other than the identifying one, as that would call this method recursively.
			throw new GenericModelException("error loading shallow entity '" + entity.entityType().getTypeSignature() + "('" +
					entity.getId() + "', '" + entity.getPartition() + "')", e);
		}
	}

	private TraversingCriterion tcLoadingSimplePropsAnd(Property property) {
		// @formatter:off
		TraversingCriterion tc = TC.create()
					.conjunction()
						.property()
						.negation()
							.disjunction()
								.typeCondition(isKind(TypeKind.scalarType))
								.property(GenericEntity.id)
							.close()
					.close()
				.done();
		// @formatter:on

		if (!property.getType().isScalar()) {
			// @formatter:off
			TraversingCriterion tc2 = TC.create()
							.negation()
								.pattern()
									.root()
									.listElement()
									.entity()
									.property(property.getName())
								.close()
						.done();
			// @formatter:on

			((ConjunctionCriterion) tc).getCriteria().add(tc2);
		}

		return tc;
	}

}
