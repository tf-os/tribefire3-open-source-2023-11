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
package com.braintribe.model.processing.query.planner.core.property;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class PropertyJoinTools {

	public static Set<Join> leafJoins(Set<Join> joins) {
		Set<Join> nonLeafs = newSet();

		for (Join join: joins) {
			nonLeafs.addAll(allJoinsOnChain(join.getSource()));
		}

		return CollectionTools2.substract(joins, nonLeafs);
	}

	private static Collection<Join> allJoinsOnChain(Source chainStart) {
		List<Join> result = newList();

		while (chainStart instanceof Join) {
			Join join = (Join) chainStart;
			result.add(join);
			chainStart = join.getSource();
		}

		return result;
	}

	public static List<Join> relativeJoinChain(Set<Join> sourceJoins, Join targetJoin) {
		List<Join> result = newList();

		do {
			if (sourceJoins.contains(targetJoin)) {
				targetJoin = null;

			} else {
				result.add(targetJoin);

				Source source = targetJoin.getSource();
				targetJoin = source instanceof Join ? (Join) source : null;
			}

		} while (targetJoin != null);

		Collections.reverse(result);

		return result;
	}

}
