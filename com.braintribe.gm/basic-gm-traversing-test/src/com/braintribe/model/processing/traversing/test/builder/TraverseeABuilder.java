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
package com.braintribe.model.processing.traversing.test.builder;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.traversing.test.model.TraverseeA;

/**
 * 
 */
public class TraverseeABuilder extends AbstractBuilder<TraverseeA, TraverseeABuilder> {

	public TraverseeABuilder() {
		super(TraverseeA.class);
	}

	public TraverseeABuilder name(String value) {
		instance.setName(value);
		return self;
	}

	public TraverseeABuilder someA(TraverseeA value) {
		instance.setSomeA(value);
		return self;
	}

	public TraverseeABuilder someOtherA(TraverseeA value) {
		instance.setSomeOtherA(value);
		return self;
	}

	public TraverseeABuilder addToSetA(TraverseeA... values) {
		Set<TraverseeA> as = instance.getSetA();
		if (as == null) {
			as = new HashSet<TraverseeA>();
			instance.setSetA(as);
		}

		as.addAll(asList(values));
		return self;
	}

	public TraverseeABuilder addToListA(TraverseeA... values) {
		List<TraverseeA> as = instance.getListA();
		if (as == null) {
			as = new ArrayList<TraverseeA>();
			instance.setListA(as);
		}

		as.addAll(asList(values));
		return self;
	}

	public TraverseeABuilder addToIntA(Integer key, TraverseeA value) {
		Map<Integer, TraverseeA> as = instance.getMapIntA();
		if (as == null) {
			as = new HashMap<Integer, TraverseeA>();
			instance.setMapIntA(as);
		}

		as.put(key, value);
		return self;
	}

	public TraverseeABuilder addToAA(TraverseeA key, TraverseeA value) {
		Map<TraverseeA, TraverseeA> as = instance.getMapAA();
		if (as == null) {
			as = new HashMap<TraverseeA, TraverseeA>();
			instance.setMapAA(as);
		}

		as.put(key, value);
		return self;
	}
}
