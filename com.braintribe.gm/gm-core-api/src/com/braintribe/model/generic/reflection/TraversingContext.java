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
package com.braintribe.model.generic.reflection;

import java.util.Collection;
import java.util.Stack;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface TraversingContext {

	Stack<BasicCriterion> getTraversingStack();

	Stack<Object> getObjectStack();

	void pushTraversingCriterion(BasicCriterion criterion, Object object);

	BasicCriterion popTraversingCriterion();

	boolean isTraversionContextMatching();

	/**
	 * Registers the <code>entity</code> as {@link #getVisitedObjects() visited}.
	 * 
	 * @param entity
	 *            the visited entity
	 * @param associate
	 *            an optional associated object. Note that this is not really needed on {@link TraversingContext} level,
	 *            but it can be useful in some contexts (e.g. the <code>associate</code> could be the cloned entity).
	 */
	void registerAsVisited(GenericEntity entity, Object associate);

	<T> T getAssociated(GenericEntity entity);

	/**
	 * Returns all the entities that have been {@link #registerAsVisited(GenericEntity, Object) visited}. Note that this
	 * has NOTHING to do with {@link TraversingVisitor}s. The main purpose of this method is make sure that entities are
	 * only traversed once. Therefore it returns the entities that have been traversed (on {@link CriterionType#ENTITY}
	 * level).
	 */
	Collection<GenericEntity> getVisitedObjects();

	<T> Collection<T> getAssociatedObjects();

	/**
	 * Returns whether the <code>entity</code> has been {@link #getVisitedObjects() visited}.
	 */
	boolean isVisited(GenericEntity entity);

	boolean isPropertyValueUsedForMatching(EntityType<?> type, GenericEntity entity, Property property);

	boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation);

	/**
	 * Returns the {@link CriterionType} for {@link BasicCriterion} that is at the top of the traversing stack. If the
	 * stack is empty, the behavior of this method is not specified.
	 */
	CriterionType getCurrentCriterionType();
}
