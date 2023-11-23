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
package com.braintribe.model.processing.query.planner.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;

/**
 * 
 */
class SourceAnalysis {

	/**
	 * All sources for given query.
	 */
	List<Source> relevantSources = newList();

	/**
	 * All the joins we have to do - those needed for the 'select' and 'order by' clauses.
	 */
	Set<Join> mandatoryJoins = newSet();

	/**
	 * Sub-sequence of {@link #relevantSources} consisting strictly of all the right joins.
	 */
	List<Join> rightJoins = newList();

	/**
	 * Maps each source to the root {@link From}. For from this maps to the From itself, for joins it maps to the From
	 * that is at the beginning of the Join-chain.
	 */
	Map<Source, From> sourceRoot = newMap();

}
