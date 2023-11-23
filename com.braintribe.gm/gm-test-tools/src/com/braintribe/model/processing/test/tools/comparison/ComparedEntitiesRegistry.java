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
package com.braintribe.model.processing.test.tools.comparison;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GenericEntity;

/**
 * Manages information about the comparison status of pairs of GenericEntities. The order of the entities of a pair does not matter in any method of this class. 
 * The status is stored as Enum of type {@link Status} which can hold:<br>
 * * EQUAL<br>
 * * NOT_EQUAL<br>
 * * BEING_COMPARED <br>
 * * NOT_CHECKED (= not registered here yet)<br>
 * @author Neidhart.Orlich
 *
 */
class ComparedEntitiesRegistry {
	private final Map<Pair<GenericEntity, GenericEntity>, Status> alreadyCheckedEntities = new HashMap<>();

	enum Status {
		EQUAL,
		NOT_EQUAL,
		BEING_COMPARED,
		NOT_CHECKED
	}

	/**
	 * @return the pair of entities from the registry map, regardless of the order - or null if not registered yet
	 */
	private Pair<GenericEntity, GenericEntity> getPair(GenericEntity first, GenericEntity second) {
		Pair<GenericEntity, GenericEntity> pairStraight = new Pair<>(first, second);
		Pair<GenericEntity, GenericEntity> pairReversed = new Pair<>(second, first);

		if (alreadyCheckedEntities.containsKey(pairStraight)) {
			return pairStraight;
		} else if (alreadyCheckedEntities.containsKey(pairReversed)) {
			return pairReversed;
		} else {
			return null;
		}
	}

	/**
	 * order of parameters does not matter
	 */
	public void registerAs(GenericEntity first, GenericEntity second, Status status) {
		Pair<GenericEntity, GenericEntity> pair = getPair(first, second);

		if (pair == null) {
			pair = new Pair<GenericEntity, GenericEntity>(first, second);
		}

		alreadyCheckedEntities.put(pair, status);
	}

	/**
	 * order of parameters does not matter
	 */
	public Status getStatus(GenericEntity first, GenericEntity second) {
		Pair<GenericEntity, GenericEntity> pair = getPair(first, second);

		if (pair == null) {
			return Status.NOT_CHECKED;
		}

		return alreadyCheckedEntities.get(pair);
	}
	
	public void clear() {
		alreadyCheckedEntities.clear();
	}
	
	/**
	 * Registers the two given entities in the registry as currently being compared if they are not already in the registry yet 
	 * 
	 * @return <b>false</b>: the entities are equal or already currently being compared -> comparison should be canceled and for the moment 
	 * assumed that they are equal<br>
	 * <b>true:</b> entities are known to be not equal or not known at all -> go on with comparison
	 */
	public boolean startCompare(GenericEntity first, GenericEntity second) {

		Status registeredEqualityStatus = getStatus(first, second);
		if (registeredEqualityStatus != Status.NOT_CHECKED) {
			return registeredEqualityStatus == Status.NOT_EQUAL;
		} else {
			registerAs(first, second, Status.BEING_COMPARED);
			return true;
		}

	}
}
