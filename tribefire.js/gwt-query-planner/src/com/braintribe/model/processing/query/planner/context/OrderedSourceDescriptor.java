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

import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.query.From;
import com.braintribe.model.queryplan.set.IndexOrderedSet;

/**
 * @author peter.gazdik
 */
public class OrderedSourceDescriptor {

	public final From from;
	public final IndexInfo indexInfo;
	public final boolean descending;
	public final int index;

	public OrderedSourceDescriptor(From from, IndexInfo indexInfo, boolean descending, int index) {
		this.from = from;
		this.indexInfo = indexInfo;
		this.descending = descending;
		this.index = index;
	}

	public String orderedProperty() {
		return indexInfo.getEntitySignature() + "#" + indexInfo.getPropertyName() + "(" + index + ")";
	}

	public IndexOrderedSet toIndexOrderedSet(QueryPlannerContext context) {
		IndexOrderedSet result = toIos(context);
		context.orderingManager().onNewIndexOrderedSet(this, result);

		return result;
	}

	private IndexOrderedSet toIos(QueryPlannerContext context) {
		IndexOrderedSet result = IndexOrderedSet.T.create();

		result.setTypeSignature(indexInfo.getEntitySignature());
		result.setPropertyName(indexInfo.getPropertyName());
		result.setIndex(context.sourceManager().indexForSource(from));
		result.setMetricIndex(context.getIndex(from, indexInfo));
		result.setDescending(descending);

		return result;
	}

}
