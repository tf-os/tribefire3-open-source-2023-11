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
package com.braintribe.model.query;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.query.conditions.Condition;

/**
 * This class encapsulates a request for zero or more entity objects out of a valid resource. Each query can be enriched
 * with a {@link Restriction}, an {@link Ordering}, a {@link TraversingCriterion}, a set of privileged roles to be
 * ignored {@link #setIgnorePriviledgedRoles(Set)} and a set of entities that should be excluded from the query
 * evaluation {@link #setEvaluationExcludes(Set)}.
 */
public interface Query extends GenericEntity {

	EntityType<Query> T = EntityTypes.T(Query.class);

	Restriction getRestriction();
	void setRestriction(Restriction restriction);

	TraversingCriterion getTraversingCriterion();
	void setTraversingCriterion(TraversingCriterion traversingCriterion);

	Ordering getOrdering();
	void setOrdering(Ordering ordering);

	boolean getDistinct();
	void setDistinct(boolean distinct);

	void setIgnorePriviledgedRoles(Set<String> ignorePriviledgedRoles);
	Set<String> getIgnorePriviledgedRoles();

	/**
	 * Defines a set of entities which are not to be evaluated/interpreted when evaluating the query. There are some
	 * entities that have special meaning, like {@link EntityReference}s and {@link Operand}s. However, if such entity is
	 * contained in this set, it is to be taken as a static value, so e.g. the {@linkplain EntityReference} is not resolved.
	 */
	Set<GenericEntity> getEvaluationExcludes();
	void setEvaluationExcludes(Set<GenericEntity> evaluationExcludes);

	void setNoAbsenceInformation(boolean noAbsenceInformation);
	boolean getNoAbsenceInformation();

	default Query where(Condition condition) {
		Restriction restriction = getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			setRestriction(restriction);
		}
		restriction.setCondition(condition);
		return this;
	}

	default Query paging(Paging paging) {
		Restriction restriction = getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			setRestriction(restriction);
		}
		restriction.setPaging(paging);
		return this;
	}

	default Query paging(int startIndex, int pageSize) {
		return paging(Paging.create(startIndex, pageSize));
	}

	default Query limit(int limit) {
		return paging(Paging.create(0, limit));
	}

	default Query distinct() {
		setDistinct(true);
		return this;
	}

	default Query orderBy(Ordering ordering) {
		setOrdering(ordering);
		return this;
	}

	default Query orderBy(OrderingDirection direction, Object orderValue) {
		Ordering ordering = getOrdering();

		SimpleOrdering newOrdering = SimpleOrdering.create(direction, orderValue);

		if (ordering == null) {
			return orderBy(newOrdering);
		}

		final CascadedOrdering cascadedOrdering;

		if (ordering instanceof SimpleOrdering) {
			cascadedOrdering = CascadedOrdering.T.create();
			cascadedOrdering.getOrderings().add((SimpleOrdering) ordering);
			orderBy(cascadedOrdering);
		} else if (ordering instanceof CascadedOrdering) {
			cascadedOrdering = (CascadedOrdering) ordering;
		} else {
			throw new IllegalStateException("unexpected ordering found: " + ordering.getClass());
		}

		cascadedOrdering.getOrderings().add(newOrdering);

		return this;
	}

	default Query orderBy(Object orderValue) {
		return orderBy(OrderingDirection.ascending, orderValue);
	}

	default Query tc(TraversingCriterion tc) {
		setTraversingCriterion(tc);
		return this;
	}

	default Query restriction(Restriction restriction) {
		setRestriction(restriction);
		return this;
	}

}
