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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.query.conditions.Condition;

/**
 * A wrapper for {@link EntitySourceNode} related to given {@link Condition}, with an additional flag which might tell
 * this condition is not delegate-able no matter what.
 */
public class ConditionExaminationDescription {

	public Set<EntitySourceNode> affectedSourceNodes = newSet();

	/* This might be set to false, which means there is some special reason why we cannot delegate given condition.
	 * 
	 * Currently, such case happens if we have a condition on a converted property, and this condition is not
	 * equality-based (equal, notEqual, in, contains). So for example if we try to compare our converted property using
	 * greaterThan, we cannot delegate such condition, because the conversion may not be preserving the intended order. */
	public boolean delegateable = true;

}
