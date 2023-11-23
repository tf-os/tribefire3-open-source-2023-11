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
package com.braintribe.model.processing.query.planner.core.cross.simple;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.planner.core.cross.FromGroup;
import com.braintribe.model.query.From;
import com.braintribe.model.query.conditions.Condition;

/**
 * 
 */
class GroupFromConditionAnalysis {

	final Map<ConditionApplicationType, Set<StepDescription>> stepByApplicationType;
	final Map<Condition, Set<From>> fromsForCondition;
	final Map<Condition, Set<FromGroup>> groupsForCondition;
	final Set<From> joinableFroms; // TODO this seems to be not used, it's never set to anything.

	public GroupFromConditionAnalysis(Map<ConditionApplicationType, Set<StepDescription>> stepByApplicationType,
			Map<Condition, Set<From>> fromsForCondition, Map<Condition, Set<FromGroup>> groupsForCondition, Set<From> joinableFroms) {

		this.stepByApplicationType = stepByApplicationType;
		this.fromsForCondition = fromsForCondition;
		this.groupsForCondition = groupsForCondition;
		this.joinableFroms = joinableFroms;
	}

	public Set<From> fromsForCondition(Condition condition) {
		return notNull(fromsForCondition.get(condition));
	}

	public Set<FromGroup> groupsForCondition(Condition condition) {
		return notNull(groupsForCondition.get(condition));
	}

	public boolean isJoinable(From from) {
		return joinableFroms.contains(from);
	}

	private <T> Set<T> notNull(Set<T> set) {
		return set != null ? set : Collections.<T> emptySet();
	}

}
