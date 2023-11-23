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

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.planner.condition.JoinPropertyType;
import com.braintribe.model.processing.query.planner.tools.JoinTypeResolver;
import com.braintribe.model.processing.query.tools.QueryModelTools;
import com.braintribe.model.processing.query.tools.SourceTypeResolver;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.queryplan.TupleComponentPosition;

/**
 * Important methods:
 * 
 * @see #indexForSource(Source)
 * @see #indexForJoinKey(Join)
 * @see #resolveType(Source)
 * 
 * @author peter.gazdik
 */
public class QuerySourceManager {

	private final Map<Source, Integer> sourceIndex = newMap();
	private final Map<Integer, Join> indexToJoin = newMap();
	private final Map<Join, Integer> joinKeyIndex = newMap();
	private final Map<Join, JoinPropertyType> joinPropertyType = newMap();
	private final Map<Source, GenericModelType> sourceType = newMap();
	private final Map<Source, From> sourceRoot;
	private final Set<Join> selectionJoins;
	/**
	 * Maps given source position to all the right joins that are reachable from given {@link Source} via edges defined by "getJoins()". This is used
	 * when identifying all the joins that must be done before some condition may be applied (Because right joins have this specialty, that the join
	 * must be done first, even if condition only uses the original source...)
	 */
	private final Map<Integer, Set<Join>> indexToRightJoins = newMap();

	public QuerySourceManager(SelectQuery selectQuery) {
		SourceAnalysis sourceAnalysis = new SourceAnalyzer().analyzeSources(selectQuery);

		assignIndices(sourceAnalysis);
		assignJoinChains(sourceAnalysis);

		sourceRoot = sourceAnalysis.sourceRoot;
		selectionJoins = sourceAnalysis.mandatoryJoins;
	}

	/**
	 * Returns a tuple-index for given source.
	 * 
	 * @see TupleComponentPosition
	 */
	public int indexForSource(Source source) {
		return sourceIndex.get(source);
	}

	public Join joinForIndex(int index) {
		return indexToJoin.get(index);
	}

	public Set<Join> rightJoinsForIndex(int index) {
		return indexToRightJoins.get(index);
	}

	/**
	 * Returns a tuple-index for key of given join, assuming the join is using a <tt>list</tt> or <tt>map</tt> property.
	 * 
	 * @see TupleComponentPosition
	 */
	public int indexForJoinKey(Join join) {
		return joinKeyIndex.computeIfAbsent(join, this::throwJoinHasNoIndexException);
	}

	private Integer throwJoinHasNoIndexException(Join join) {
		throw new IllegalArgumentException("Collection index can only be applied on a list or a map. Path: '" + QueryModelTools.printWithAlias(join) + "'");
	}

	public JoinPropertyType joinPropertyType(Join join) {
		return joinPropertyType.get(join);
	}

	public From getSourceRoot(Source source) {
		From result = sourceRoot.get(source);
		if (result == null)
			throw new IllegalArgumentException("Query contains a source which is not reachable from the FROM clause: " + source);
		return result;
	}

	public Set<From> findAllFroms() {
		return newSet(sourceRoot.values());
	}

	public Set<Join> findAllSelectionJoins() {
		return selectionJoins;
	}

	public Collection<Join> findAllJoins() {
		return indexToJoin.values();
	}

	private void assignIndices(SourceAnalysis sourceAnalysis) {
		List<Source> relevantSources = sourceAnalysis.relevantSources;
		int counter = 0;
		for (Source source : relevantSources) {
			if (source instanceof Join) {
				Join join = (Join) source;

				// join.getJoinType()
				JoinPropertyType joinType = findJoinPropertyType(join);
				if (joinType == JoinPropertyType.list || joinType == JoinPropertyType.map) {
					/* we also want to associate the key position with given join (this information is used when checking when given join may be done
					 * (before/after applying condition)) */
					indexToJoin.put(counter, join);
					joinKeyIndex.put(join, counter++);
				}
				joinPropertyType.put(join, joinType);
				indexToJoin.put(counter, join);
			}

			sourceIndex.put(source, counter++);
		}
	}

	private void assignJoinChains(SourceAnalysis sourceAnalysis) {
		List<Join> rightJoins = sourceAnalysis.rightJoins;
		if (rightJoins.isEmpty())
			return;

		for (Join rightJoin : rightJoins)
			handleRightJoinChain(rightJoin);
	}

	private void handleRightJoinChain(Join rightJoin) {
		Join join = rightJoin;

		while (true) {
			Source source = join.getSource();
			Set<Join> rightJoinsForSource = acquireSet(indexToRightJoins, indexForSource(source));

			rightJoinsForSource.add(rightJoin);

			if (!(source instanceof Join))
				return;

			join = (Join) source;
		}
	}

	private JoinPropertyType findJoinPropertyType(Join join) {
		EntityType<?> sourceType = resolveType(join.getSource());
		return JoinTypeResolver.resolveJoinPropertyType(sourceType, join.getProperty());
	}

	/**
	 * Cache for the resolved type of given {@link Source}. Uses {@link SourceTypeResolver#resolveType(Source)} for the actual resolution.
	 */
	public <T extends GenericModelType> T resolveType(Source source) {
		GenericModelType result = sourceType.get(source);
		if (result == null) {
			result = SourceTypeResolver.resolveType(source);
			sourceType.put(source, result);
		}

		return (T) result;
	}

}
