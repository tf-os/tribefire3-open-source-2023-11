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
package com.braintribe.model.processing.am;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;

/**
 * Info about all referees for one entity (this entity is not referenced from within this class though).
 */
public class RefereeData {

	public Map<GenericEntity, Counter> referees = new HashMap<GenericEntity, Counter>();
	public int totalReferences;

	public void addReferee(GenericEntity referee, int count) {
		acquireCounterFor(referee).count += count;

		this.totalReferences += count;
	}

	public void removeReferee(GenericEntity referee, int count) {
		Counter counter = referees.get(referee);

		counter.count -= count;

		if (counter.count == 0) {
			referees.remove(referee);
		}

		this.totalReferences -= count;
	}

	public void add(RefereeData other) {
		for (Entry<GenericEntity, Counter> entry: other.referees.entrySet()) {
			GenericEntity referee = entry.getKey();
			Counter counter = entry.getValue();

			acquireCounterFor(referee).count += counter.count;
		}

		this.totalReferences += other.totalReferences;
	}

	public void subtract(RefereeData other) {
		for (Entry<GenericEntity, Counter> entry: other.referees.entrySet()) {
			GenericEntity referee = entry.getKey();
			Counter otherCounter = entry.getValue();

			Counter counter = referees.get(referee);
			counter.count -= otherCounter.count;

			if (counter.count == 0) {
				referees.remove(referee);
			}
		}

		this.totalReferences -= other.totalReferences;
	}

	private Counter acquireCounterFor(GenericEntity referee) {
		Counter counter = referees.get(referee);

		if (counter == null) {
			counter = new Counter();
			referees.put(referee, counter);
		}

		return counter;
	}

	@Override
	public String toString() {
		return referees.toString();
	}

}
