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

import static com.braintribe.model.processing.query.planner.builder.ValueBuilder.tupleComponent;
import static com.braintribe.model.processing.query.planner.builder.ValueBuilder.valueProperty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.planner.core.cross.simple.IndexOrderedSetPostProcessor;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueProperty;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @see IndexOrderedSet
 * @see IndexOrderedSetPostProcessor
 * 
 * @author peter.gazdik
 */
public class QueryOrderingManager {

	// ###############################################
	// ## . . . . . . . . . API . . . . . . . . . . ##
	// ###############################################

	private final QueryPlannerContext context;
	private final List<Value> groupValues = newList();
	private final Map<IndexOrderedSet, OrderedSourceDescriptor> ios2Osd = newMap();
	private final Map<From, OrderedSourceDescriptor> osds = newMap();

	private List<SimpleOrdering> remainingOrderings;

	public QueryOrderingManager(QueryPlannerContext context, SelectQuery query) {
		this.context = context;
		this.remainingOrderings = findOrderings(query);

		findOsds();
	}

	public OrderedSourceDescriptor findOsd(From from) {
		return osds.get(from);
	}

	public void onNewIndexOrderedSet(OrderedSourceDescriptor osd, IndexOrderedSet ios) {
		ios2Osd.put(ios, osd);
	}

	public OrderedSourceDescriptor findOsd(IndexOrderedSet ios) {
		return ios2Osd.get(ios);
	}

	// TODO examine usages
	public void onIosApplied(OrderedSourceDescriptor osd) {
		remainingOrderings = CollectionTools2.skipFirstN(remainingOrderings, 1);
		groupValues.add(toValue(osd));
	}

	private ValueProperty toValue(OrderedSourceDescriptor osd) {
		TupleComponent tupleComponent = tupleComponent(context.sourceManager().indexForSource(osd.from));
		return valueProperty(tupleComponent, osd.indexInfo.getPropertyName());
	}

	public List<SimpleOrdering> getRemainingOrderings() {
		return remainingOrderings;
	}

	public List<Value> getGroupValues() {
		return groupValues;
	}

	// ###############################################
	// ## . . . . . . . . . Analysis . . . . . . . .##
	// ###############################################

	private List<SimpleOrdering> findOrderings(SelectQuery query) {
		Ordering o = query.getOrdering();
		if (o == null)
			return emptyList();

		if (o instanceof SimpleOrdering)
			return singletonList((SimpleOrdering) o);

		if (o instanceof CascadedOrdering)
			return ((CascadedOrdering) o).getOrderings();

		throw new IllegalArgumentException("Unknown ordering type. Ordering: " + o);
	}

	private void findOsds() {
		if (remainingOrderings.isEmpty())
			return;

		for (SimpleOrdering ordering : remainingOrderings) {
			OrderedSourceDescriptor osd = toOsdIfPossible(ordering, osds.size());
			if (osd == null)
				break;
			osds.put(osd.from, osd);
		}
	}

	private OrderedSourceDescriptor toOsdIfPossible(SimpleOrdering ordering, int index) {
		Object orderBy = ordering.getOrderBy();
		if (!(orderBy instanceof PropertyOperand))
			return null;

		PropertyOperand po = (PropertyOperand) orderBy;

		Source source = po.getSource();
		if (!(source instanceof From))
			return null;

		From from = (From) source;

		String propertyName = po.getPropertyName();
		if (propertyName == null)
			return null; // this would be weird though

		IndexInfo indexInfo = context.getIndexInfo(from, propertyName);
		if (indexInfo == null || !indexInfo.hasMetric())
			return null;

		return new OrderedSourceDescriptor(from, indexInfo, ordering.getDirection() == OrderingDirection.descending, index);
	}

}
