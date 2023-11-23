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
package com.braintribe.model.processing.meta.cmd.context.scope;

import static com.braintribe.model.processing.meta.cmd.context.scope.CmdScope.MOMENTARY;
import static com.braintribe.model.processing.meta.cmd.context.scope.CmdScope.SESSION;
import static com.braintribe.model.processing.meta.cmd.context.scope.CmdScope.STATIC;
import static com.braintribe.model.processing.meta.cmd.context.scope.CmdScope.VOLATILE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeComparator {

	/* @formatter:off */
	private static final List<CmdScope> SCOPES_ORDERED_FROM_MOST_STABLE = Arrays.asList(
			STATIC,
			SESSION, 
			MOMENTARY,
			VOLATILE);
	/* @formatter:on */

	private static final Map<CmdScope, Integer> SCOPE_INDEX = new HashMap<CmdScope, Integer>();

	static {
		int i = 0;
		for (CmdScope scopeType: SCOPES_ORDERED_FROM_MOST_STABLE) {
			SCOPE_INDEX.put(scopeType, i++);
		}
	}

	public static CmdScope commonScope(CmdScope cs1, CmdScope cs2) {
		// first make sure the more stable scope is cs1 (i.e. if not, switch them)
		if (compare(cs1, cs2) > 0) {
			CmdScope tmp = cs1;
			cs1 = cs2;
			cs2 = tmp;
		}

		// in this special case, return the scope volatile, otherwise just return the less stable scope
		if (cs1 == SESSION && cs2 == MOMENTARY) {
			return VOLATILE;
		}

		return cs2;
	}

	public static int compare(CmdScope cs1, CmdScope cs2) {
		return SCOPE_INDEX.get(cs1) - SCOPE_INDEX.get(cs2);
	}

}
