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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.List;

import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

/**
 * Basic common methods for managing QueryModel entities.
 */
public class QueryTools {

	/**
	 * Returns the only {@link Source} in this query (which must be a {@link From}) if it exists (i.e. there is only one
	 * From with no {@link Join}s), or <tt>null</tt> if the query has other Sources (or none at all, which would however
	 * not be the case for a valid query.)
	 */
	public static From getSingleSource(SelectQuery query) {
		List<From> froms = query.getFroms();

		if (froms == null || froms.size() > 1)
			return null;

		From from = first(froms);

		return isEmpty(from.getJoins()) ? from : null;
	}

}
