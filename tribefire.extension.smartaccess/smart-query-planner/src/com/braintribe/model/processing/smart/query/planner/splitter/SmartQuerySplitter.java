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
package com.braintribe.model.processing.smart.query.planner.splitter;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Iterator;
import java.util.List;

import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.query.tools.SelectQueryNormalizer;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.structure.MappedHierarchyExpert;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.QueryableHierarchyExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;

/**
 * 
 * If query-sources are entities that are either not mapped or some of their sub-types are mapped to different hierarchy (rare, but theoretically
 * possible), then we need to split this query. For each such source we have to create versions where that given source is replaced by a query-able
 * hierarchy. As a simple example, consider model with Person and Company and a query: <tt>select ge from GenericEntity ge</tt>. This query has to be
 * split into two queries <tt>select ge from Person ge</tt> and <tt>select ge from Company ge</tt>. The analysis of which sub-types have to be queried
 * for given source type is done by {@link QueryableHierarchyExpert}. The hierarchy for one queryable type is resolved by
 * {@link MappedHierarchyExpert}.
 * 
 * 
 * 
 * So I do 2 such queries, and then create a merge of the results (with normal queries it is a concatenation, but with aggregation it might be needed
 * to really merge the results based on the group-by clause). The planner really creates as many queries as needed, and then creates a query-plan for
 * each such query individually. Later, these partial results are merged together.
 * 
 * Even in the non-aggregated case this is not so straight forward if we are sorting. Then we get a bunch of TupleSets which are already sorted, but
 * we need to merge the data correctly (now we mean merge as in merge-sort). This is then again not so straight forward, cause we might not even have
 * the value used sorting, so we might not be able to do this merge. For example: <tt>select p.name from Person p order by p.brithDate</tt>, if Person
 * has two sub-types each mapped to say a type in different access.
 * 
 * The solution for now is to change the query to <tt>select p.name, p.birthDate from Person p order by p.brithDate</tt> , and put another projection
 * on the very top (after merging is done), which only selects the first elements of the tuple, leaving the order-bys out. In general, we will always
 * have all sorts at the very end, in order from most to least significant. Then we only need to remember the number of sorts to do the comparison and
 * also to do the final projection (just removing those final values). This operation (merging of sorted {@link TupleSet}s is represented by
 * {@link OrderedConcatenation}).
 * 
 * @see MappedHierarchyExpert
 * @see QueryableHierarchyExpert
 * 
 * @author peter.gazdik
 */
public class SmartQuerySplitter implements Iterable<DisambiguatedQuery> {

	private final DisambiguatedQuery queryDescriptor;
	private final SelectQuery query;
	private final ModelExpert modelExpert;

	private final QueryableTypeFiltering queryableTypeFiltering;

	private final List<Object> orderBys = newList();
	private final List<SortCriterion> sortCriteria = newList();
	private final List<FromEntry> fromEntries = newList();

	private final int originalSelectionsCount;

	private Paging originalPaging;

	private boolean hasMultipleQueries;
	private boolean isEmpty;
	private final SelectQueryNormalizer normalizer;

	static class FromEntry {
		From from;
		List<EntityMapping> queryableTypes;
		Iterator<EntityMapping> currentIterator;
		EntityMapping lastReturnedNext;
		boolean needsAdjusting;

		void resetIteration() {
			currentIterator = queryableTypes.iterator();
			lastReturnedNext = currentIterator.next();
		}

		void moveIteration() {
			lastReturnedNext = currentIterator.next();
		}
	}

	public SmartQuerySplitter(SelectQuery originalQuery, ModelExpert modelExpert) {
		originalQuery = GmReflectionTools.makeDeepCopy(originalQuery);
		this.normalizer = new SelectQueryNormalizer(originalQuery, true, true);

		this.query = normalizer.normalize();
		this.modelExpert = modelExpert;
		this.originalSelectionsCount = query.getSelections().size();
		this.queryableTypeFiltering = new QueryableTypeFiltering(query, modelExpert);

		this.queryDescriptor = new DisambiguatedQuery(query, originalQuery);

		initFromEntries();
		if (hasMultipleQueries) {
			initOrderBys();
			initPaging(query);
		}
	}

	private void initPaging(SelectQuery query) {
		Restriction r = query.getRestriction();
		Paging p = r == null ? null : r.getPaging();

		if (p == null)
			return;

		originalPaging = Paging.T.createPlain();
		originalPaging.setPageSize(p.getPageSize());
		originalPaging.setStartIndex(p.getStartIndex());

		p.setPageSize(p.getPageSize() + p.getStartIndex());
		p.setStartIndex(0);
	}

	private void initOrderBys() {
		Ordering ordering = query.getOrdering();
		if (ordering == null)
			return;

		int sortComponentIndex = originalSelectionsCount;
		if (ordering instanceof SimpleOrdering)
			addOrderBy((SimpleOrdering) ordering, sortComponentIndex++);
		else
			for (SimpleOrdering so : ((CascadedOrdering) ordering).getOrderings())
				addOrderBy(so, sortComponentIndex++);

		query.getSelections().addAll(orderBys);
	}

	private void addOrderBy(SimpleOrdering so, int sortComponentIndex) {
		orderBys.add(so.getOrderBy());
		sortCriteria.add(newSortCriterion(so, sortComponentIndex));
	}

	private SortCriterion newSortCriterion(SimpleOrdering so, int sortComponentIndex) {
		SortCriterion result = SortCriterion.T.createPlain();
		result.setDescending(so.getDirection() == OrderingDirection.descending);
		result.setValue(ValueBuilder.tupleComponent(sortComponentIndex));

		return result;
	}

	private void initFromEntries() {
		List<From> froms = query.getFroms();

		for (From from : froms) {
			GmEntityType smartType = modelExpert.resolveSmartEntityType(from.getEntityTypeSignature());
			List<EntityMapping> queryableTypes = modelExpert.resolveRelevantQueryableTypesFor(smartType);

			if (queryableTypes.isEmpty())
				throw new SmartQueryPlannerException("Unable to find any queryable type for smart type: " + from.getEntityTypeSignature());

			queryableTypes = queryableTypeFiltering.filterPlease(from, queryableTypes);

			if (queryableTypes.isEmpty()) {
				isEmpty = true;
				return;
			}

			FromEntry fe = new FromEntry();
			fe.from = from;
			fe.queryableTypes = queryableTypes;

			if (queryableTypes.size() > 1) {
				hasMultipleQueries = true;
				fe.needsAdjusting = true;

			} else if (first(queryableTypes).getSmartEntityType() != smartType) {
				fe.needsAdjusting = true;
			}

			if (fe.needsAdjusting)
				fromEntries.add(fe);
			else
				queryDescriptor.fromMapping.put(from, first(queryableTypes).getAccess());
		}
	}

	public boolean hasMultipleQueries() {
		return hasMultipleQueries;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public DisambiguatedQuery singleQuery() {
		return fromEntries.isEmpty() ? queryDescriptor : iterator().next();
	}

	@Override
	public Iterator<DisambiguatedQuery> iterator() {
		return new QueryIterator();
	}

	class QueryIterator implements Iterator<DisambiguatedQuery> {
		boolean hasNext;

		public QueryIterator() {
			for (FromEntry fe : fromEntries)
				fe.resetIteration();

			hasNext = true;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public DisambiguatedQuery next() {
			if (!hasNext)
				throw new RuntimeException("No next element exists;");

			for (FromEntry fe : fromEntries) {
				String ts = fe.lastReturnedNext.getSmartEntityType().getTypeSignature();

				From originalFrom = normalizer.getOriginalEntity(fe.from);

				fe.from.setEntityTypeSignature(ts);
				originalFrom.setEntityTypeSignature(ts);

				queryDescriptor.fromMapping.put(fe.from, fe.lastReturnedNext.getAccess());
			}

			prepareNext();

			return queryDescriptor;
		}

		private void prepareNext() {
			computeHasNext();

			if (!hasNext)
				return;

			int nextsToUpdate = 0;
			for (FromEntry fe : fromEntries) {
				if (fe.currentIterator.hasNext())
					break;

				nextsToUpdate++;
			}

			for (int i = 0; i < nextsToUpdate; i++)
				fromEntries.get(i).resetIteration();

			fromEntries.get(nextsToUpdate).moveIteration();
		}

		private void computeHasNext() {
			for (FromEntry fe : fromEntries) {
				if (fe.currentIterator.hasNext()) {
					hasNext = true;
					return;
				}
			}

			hasNext = false;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Method 'SmartQuerySplitter.QueryIterator.remove' is not supported!");
		}
	}

	public Paging getPaging() {
		return originalPaging;
	}

	public int getOriginalSelectionsCount() {
		return originalSelectionsCount;
	}

	public int getSelectionsCount() {
		return query.getSelections().size();
	}

	public List<SortCriterion> getSortCriteria() {
		return sortCriteria;
	}

}
