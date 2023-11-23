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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.DelegateJoinGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessCombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;

/**
 * 
 * @author peter.gazdik
 */
public class CharStringsResolver {

	private final Map<SourceNodeGroup, List<String>> charStringsForGroup = newMap();

	public List<String> getCharStrings(SourceNodeGroup group) {
		List<String> result = charStringsForGroup.get(group);

		if (result == null) {
			result = computeCharStringsFor(group);
			charStringsForGroup.put(group, result);
		}

		return result;
	}

	private List<String> computeCharStringsFor(SourceNodeGroup group) {
		List<String> result = newList();
		add(group, result);

		Collections.sort(result);

		return result;
	}

	private static void add(SourceNodeGroup group, List<String> result) {
		switch (group.nodeGroupType()) {
			case combination:
				add((CombinationGroup) group, result);
				return;
			case delegateQueryJoin:
				add((DelegateJoinGroup) group, result);
				return;
			case singleAccessCombination:
				add((SingleAccessCombinationGroup) group, result);
				return;
			case singleSource:
				add((SingleSourceGroup) group, result);
				return;
		}

		throw new SmartQueryPlannerException("Unknown group type '" + group.nodeGroupType() + "'.");
	}

	private static void add(SingleAccessCombinationGroup group, List<String> result) {
		for (SingleSourceGroup operand: group.operands)
			add(operand, result);
	}

	private static void add(SingleSourceGroup group, List<String> result) {
		result.add(stringFor(group.access, first(group.allNodes)));
	}

	private static void add(DelegateJoinGroup group, List<String> result) {
		add(group.materializedGroup, result);
		add(group.queryGroup, result);
	}

	private static void add(CombinationGroup group, List<String> result) {
		for (SourceNodeGroup operand: group.operands)
			add(operand, result);
	}

	private static String stringFor(IncrementalAccess access, EntitySourceNode sn) {
		return access.getExternalId() + "#" + sn.getDelegateGmType().getTypeSignature();
	}

}
