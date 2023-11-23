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

import java.util.List;
import java.util.Map.Entry;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.QueryPlan;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.filter.ConditionType;
import com.braintribe.model.queryplan.filter.Junction;
import com.braintribe.model.queryplan.filter.Negation;
import com.braintribe.model.queryplan.filter.ValueComparison;
import com.braintribe.model.queryplan.index.GeneratedIndex;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Intersection;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.Union;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.set.join.IndexRangeJoin;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.PropertyJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;
import com.braintribe.model.queryplan.value.AggregateFunction;
import com.braintribe.model.queryplan.value.HashSetProjection;
import com.braintribe.model.queryplan.value.IndexValue;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueProperty;
import com.braintribe.model.queryplan.value.range.Range;
import com.braintribe.model.queryplan.value.range.RangeIntersection;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 * @author peter.gazdik
 */
public class QueryPlanPrinter extends AbstractStringifier {

	protected boolean useSimpleName = false;

	private static final Logger log = Logger.getLogger(QueryPlanPrinter.class);

	public static String printSafe(QueryPlan plan) {
		try {
			return print(plan);

		} catch (Exception e) {
			log.warn("QueryPlan printing failed.", e);
			return "<QueryPlan printing failed>";
		}
	}

	public static String print(QueryPlan queryPlan) {
		return print(queryPlan.getTupleSet());
	}

	public static String print(TupleSet set) {
		QueryPlanPrinter printer = new QueryPlanPrinter();
		printer.view(set);

		return printer.builder.toString();
	}

	public static String print(Value value) {
		QueryPlanPrinter printer = new QueryPlanPrinter();
		printer.view("", value);

		return printer.builder.toString();
	}

	public static String print(PropertyQuery query) {
		PersistentEntityReference ref = query.getEntityReference();
		return "selectProperty " + query.getPropertyName() + " of " + ref.getTypeSignature() + "[" + ref.getRefId() + "]"
				+ (ref.getRefPartition() == null ? "" : ("[" + ref.getRefPartition() + "]"));
	}

	public static String print(SelectQuery query) {
		return printQuery(query);
	}

	public static String printSafe(SelectQuery query) {
		return printQuerySafe(query);
	}

	public static String print(EntityQuery query) {
		return printQuery(query);
	}

	public static String printSafe(EntityQuery query) {
		return printQuerySafe(query);
	}

	public static String printQuerySafe(Query query) {
		try {
			return printQuery(query);

		} catch (Exception e) {
			String queryType = query.entityType().getShortName();
			log.warn(queryType + " printing failed.", e);
			return "<" + queryType + " printing failed>";
		}
	}

	public static String printQuery(Query query) {
		return BasicQueryStringifier.create().lenient().stringify(query);
	}

	public QueryPlanPrinter setUseEntitySimpleName(boolean useSimpleName) {
		this.useSimpleName = useSimpleName;
		return this;
	}

	// ####################################
	// ## . . . . VIEW TUPLE SET . . . . ##
	// ####################################

	protected void view(String s, TupleSet tupleSet) {
		print(s);
		view(tupleSet);
	}

	protected void view(TupleSet tupleSet) {
		println(getSimpleClassName(tupleSet));

		levelUp();
		switch (tupleSet.tupleSetType()) {
			case aggregatingProjection:
				viewTupleSet((Projection) tupleSet);
				break;
			case cartesianProduct:
				viewTupleSet((CartesianProduct) tupleSet);
				break;
			case concatenation:
				viewTupleSet((Concatenation) tupleSet);
				break;
			case distinctSet:
				viewTupleSet((DistinctSet) tupleSet);
				break;
			case entityJoin:
				viewTupleSet((EntityJoin) tupleSet);
				break;
			case filteredSet:
				viewTupleSet((FilteredSet) tupleSet);
				break;
			case indexLookupJoin:
				viewTupleSet((IndexLookupJoin) tupleSet);
				break;
			case indexOrderedSet:
				viewTupleSet((IndexOrderedSet) tupleSet);
				break;
			case indexRange:
				viewTupleSet((IndexRange) tupleSet);
				break;
			case indexRangeJoin:
				viewTupleSet((IndexRangeJoin) tupleSet);
				break;
			case indexSubSet:
				viewTupleSet((IndexSubSet) tupleSet);
				break;
			case intersection:
				viewTupleSet((Intersection) tupleSet);
				break;
			case listJoin:
				viewTupleSet((ListJoin) tupleSet);
				break;
			case mapJoin:
				viewTupleSet((MapJoin) tupleSet);
				break;
			case mergeLookupJoin:
				viewTupleSet((MergeLookupJoin) tupleSet);
				break;
			case mergeRangeJoin:
				viewTupleSet((MergeRangeJoin) tupleSet);
				break;
			case orderedSet:
				viewTupleSet((OrderedSet) tupleSet);
				break;
			case orderedSetRefinement:
				viewTupleSet((OrderedSetRefinement) tupleSet);
				break;
			case pagination:
				viewTupleSet((PaginatedSet) tupleSet);
				break;
			case projection:
				viewTupleSet((Projection) tupleSet);
				break;
			case querySourceSet:
				viewTupleSet((QuerySourceSet) tupleSet);
				break;
			case setJoin:
				viewTupleSet((SetJoin) tupleSet);
				break;
			case sourceSet:
				viewTupleSet((SourceSet) tupleSet);
				break;
			case staticSet:
				viewTupleSet((StaticSet) tupleSet);
				break;
			case union:
				viewTupleSet((Union) tupleSet);
				break;
			default:
				throw new RuntimeQueryEvaluationException("Unsupported TupleSet: " + tupleSet + " of type: " + tupleSet.tupleSetType());
		}
		levelDown();
	}

	protected void viewTupleSet(CartesianProduct tupleSet) {
		println("Operands:");
		for (TupleSet operand : tupleSet.getOperands())
			view("- ", operand);
	}

	protected void viewTupleSet(Concatenation tupleSet) {
		viewCombinedSet(tupleSet);
		println("tupleSize: " + tupleSet.getTupleSize());
	}

	protected void viewTupleSet(DistinctSet tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
	}

	protected void viewTupleSet(EntityJoin tupleSet) {
		viewPropertyJoin(tupleSet);
	}

	protected void viewTupleSet(FilteredSet tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		view("filter: ", tupleSet.getFilter());
	}

	protected void viewTupleSet(IndexLookupJoin tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		view("lookupValue: ", tupleSet.getLookupValue());
		view("lookupIndex: ", tupleSet.getLookupIndex());
	}

	private void viewTupleSet(IndexOrderedSet tupleSet) {
		println("typeSignature: " + tupleSet.getTypeSignature());
		println("propertyName: " + tupleSet.getPropertyName());
		println("descending: " + tupleSet.getDescending());
		view("metricIndex: ", tupleSet.getMetricIndex());
	}

	protected void viewTupleSet(IndexRange tupleSet) {
		println("typeSignature: " + tupleSet.getTypeSignature());
		println("propertyName: " + tupleSet.getPropertyName());
		view("range: ", tupleSet.getRange());
		view("metricIndex: ", tupleSet.getMetricIndex());
	}

	protected void viewTupleSet(IndexRangeJoin tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		view("range: ", tupleSet.getRange());
		view("metricIndex: ", tupleSet.getMetricIndex());
	}

	protected void viewTupleSet(IndexSubSet tupleSet) {
		println("typeSignature: " + tupleSet.getTypeSignature());
		println("propertyName: " + tupleSet.getPropertyName());
		view("keys: ", tupleSet.getKeys());
		view("lookupIndex: ", tupleSet.getLookupIndex());
	}

	protected void viewTupleSet(Intersection tupleSet) {
		viewCombinedSet(tupleSet);
	}

	protected void viewTupleSet(ListJoin tupleSet) {
		viewPropertyJoin(tupleSet);
	}

	protected void viewTupleSet(MapJoin tupleSet) {
		viewPropertyJoin(tupleSet);
		println("mapKey:" + tupleSet.getMapKey().getIndex());
	}

	protected void viewTupleSet(MergeLookupJoin tupleSet) {
		view("operand:", tupleSet.getOperand());
		view("value: ", tupleSet.getValue());
		view("otherOperand:", tupleSet.getOtherOperand());
		view("otherValue: ", tupleSet.getOtherValue());
	}

	protected void viewTupleSet(MergeRangeJoin tupleSet) {
		view("operand:", tupleSet.getOperand());
		view("range: ", tupleSet.getRange());
		view("index: ", tupleSet.getIndex());
	}

	protected void viewTupleSet(OrderedSet tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		println("sortCriteria: [");
		levelUp();

		for (SortCriterion c : tupleSet.getSortCriteria())
			viewSortCriterion(c);

		levelDown();
		println("]");
	}

	protected void viewTupleSet(OrderedSetRefinement tupleSet) {
		viewTupleSet((OrderedSet) tupleSet);

		println("groupValues: [");
		levelUp();

		for (Value v: tupleSet.getGroupValues())
			view("- ", v);

		levelDown();
		println("]");
	}

	protected void viewSortCriterion(SortCriterion c) {
		view(c.getDescending() ? "DESC " : "ASC ", c.getValue());
	}

	protected void viewTupleSet(PaginatedSet tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		println("limit:" + tupleSet.getLimit());
		println("offset:" + tupleSet.getOffset());
		println("tupleSize:" + tupleSet.getTupleSize());
	}

	protected void viewTupleSet(Projection tupleSet) {
		print("operand: ");
		view(tupleSet.getOperand());
		println("values: [");
		levelUp();

		List<Value> values = tupleSet.getValues();
		for (Value v : values)
			view("", v);

		levelDown();
		println("]");
	}

	protected void viewTupleSet(QuerySourceSet tupleSet) {
		Restriction r = Restriction.T.create();
		r.setCondition(tupleSet.getCondition());

		EntityQuery eq = EntityQueryBuilder.from(tupleSet.getEntityTypeSignature()).done();
		eq.setRestriction(r);
		eq.setOrdering(tupleSet.getOrdering());

		println("query: " + printQuery(eq));
	}

	protected void viewTupleSet(SetJoin tupleSet) {
		viewPropertyJoin(tupleSet);
	}

	protected void viewTupleSet(SourceSet tupleSet) {
		println("typeSignature: " + tupleSet.getTypeSignature());
		println("index:" + tupleSet.getIndex());
	}

	protected void viewTupleSet(StaticSet tupleSet) {
		println("values: " + tupleSet.getValues());
		println("index:" + tupleSet.getIndex());
	}

	protected void viewTupleSet(Union tupleSet) {
		viewCombinedSet(tupleSet);
	}

	protected void viewPropertyJoin(PropertyJoin tupleSet) {
		println("joinKind:" + tupleSet.getJoinKind());
		print("on: ");
		view("", tupleSet.getValueProperty());
		print("operand: ");
		view(tupleSet.getOperand());
		println("index: " + tupleSet.getIndex());
	}

	protected void viewCombinedSet(CombinedSet tupleSet) {
		view("first: ", tupleSet.getFirstOperand());
		view("second: ", tupleSet.getSecondOperand());
	}

	// ####################################
	// ## . . . . VIEW CONDITION. . . . .##
	// ####################################

	protected void view(String prefix, Condition condition) {
		if (condition == null) {
			println("no filter");
			return;
		}

		if (condition.conditionType() == ConditionType.negation) {
			view(prefix + "NOT ", ((Negation) condition).getOperand());
			return;
		}

		println(prefix + getSimpleClassName(condition));

		levelUp();

		switch (condition.conditionType()) {
			case conjunction:
			case disjunction:
				view((Junction) condition);
				break;

			case negation:
				throw new RuntimeException("This should be unreachable!");

			default:
				view((ValueComparison) condition);
		}

		levelDown();

	}

	private void view(Junction junction) {
		for (Condition operand : junction.getOperands())
			view("", operand);
	}

	private void view(ValueComparison valueComparison) {
		view("leftOperand: ", valueComparison.getLeftOperand());
		view("rightOperand: ", valueComparison.getRightOperand());
	}

	// ####################################
	// ## . . . . . VIEW RANGE . . . . . ##
	// ####################################

	protected void view(String prefix, Range range) {
		switch (range.rangeType()) {
			case intersection:
				view(prefix, (RangeIntersection) range);
				return;
			case simple:
				view(prefix, (SimpleRange) range);
				return;

		}

		throw new RuntimeQueryEvaluationException("Unsupported Range: " + range + " of type: " + range.rangeType());
	}

	private void view(String prefix, RangeIntersection range) {
		println(prefix + " RangeIntersection");
		levelUp();
		for (SimpleRange sr : range.getRanges())
			view("", sr);
		levelDown();
	}

	private void view(String prefix, SimpleRange range) {
		println(prefix + " SimpleRange");
		levelUp();
		if (range.getLowerBound() != null) {
			view("lower bound: ", range.getLowerBound());
			println("lower inclusion: " + range.getLowerInclusive());
		}
		if (range.getUpperBound() != null) {
			view("upper bound: ", range.getUpperBound());
			println("upper inclusion:" + range.getUpperInclusive());
		}
		levelDown();
	}

	// ####################################
	// ## . . . . . VIEW VALUE . . . . . ##
	// ####################################

	protected void view(String prefix, Value value) {
		if (value == null)
			return;

		switch (value.valueType()) {
			case aggregateFunction:
				view(prefix, (AggregateFunction) value);
				return;
			case queryFunction:
				view(prefix, (QueryFunctionValue) value);
				return;
			case hashSetProjection:
				view(prefix, (HashSetProjection) value);
				return;
			case indexValue:
				view(prefix, (IndexValue) value);
				return;
			case staticValue:
				view(prefix, (StaticValue) value);
				return;
			case tupleComponent:
				view(prefix, (TupleComponent) value);
				return;
			case valueProperty:
				view(prefix, (ValueProperty) value);
				return;
			default:
				throw new RuntimeQueryEvaluationException("Unsupported Value: " + value + " of type: " + value.valueType());
		}
	}

	private void view(String prefix, AggregateFunction value) {
		println(prefix + "AggregateFunction");
		levelUp();
		view("value: ", value.getOperand());
		println("aggregationFunctionType: " + value.getAggregationFunctionType());
		levelDown();
	}

	private void view(String prefix, QueryFunctionValue value) {
		println(prefix + "QF[" + getSimpleClassName(value) + "]");
		levelUp();
		view("function: ", value.getQueryFunction());

		println("queryFunctionType: " + getFullClassName(value.getQueryFunction()));
		println("instance: " + value);

		println("operandMappings:");
		levelUp();
		for (Entry<Object, Value> entry : value.getOperandMappings().entrySet()) {
			println("key: " + entry.getKey().getClass().getName());
			view("value: ", entry.getValue());
		}
		levelDown();
		levelDown();
	}

	protected void view(String prefix, QueryFunction function) {
		if (function instanceof EntitySignature) {
			view(prefix, (EntitySignature) function);
			return;
		}

		return;
	}

	private void view(String prefix, HashSetProjection value) {
		println(prefix + "HashSetProjection");
		levelUp();
		view("tupleSet: ", value.getTupleSet());
		view("value: ", value.getValue());
		levelDown();
	}

	private void view(String prefix, TupleComponent value) {
		println(prefix + "TupleComponent[index: " + value.getTupleComponentIndex() + "]");
	}

	private void view(String prefix, IndexValue value) {
		println(prefix + "IndexValue");
		levelUp();
		println("indexId: " + value.getIndexId());
		view("keys: ", value.getKeys());
		levelDown();
	}

	private void view(String prefix, StaticValue value) {
		println(prefix + getSimpleClassName(value) + "[" + value.getValue() + "]");
	}

	private void view(String prefix, ValueProperty value) {
		println(prefix + "ValueProperty");
		levelUp();
		view("value: ", value.getValue());
		println("propertyPath: " + value.getPropertyPath());
		levelDown();
	}

	// ####################################
	// ## . . . VIEW QUERY FUNCTION . . .##
	// ####################################

	protected void view(String prefix, EntitySignature function) {
		println(prefix + "EntitySignature");
		levelUp();
		println("operand: " + function.getOperand());
		levelDown();
	}

	// ####################################
	// ## . . . . . VIEW INDEX . . . . . ##
	// ####################################

	protected void view(String prefix, Index index) {
		switch (index.indexType()) {
			case generated:
				viewIndex(prefix, (GeneratedIndex) index);
				return;
			case generatedMetric:
				viewIndex(prefix, (GeneratedMetricIndex) index);
				return;
			case repository:
				viewIndex(prefix, (RepositoryIndex) index);
				return;
			case repositoryMetric:
				viewIndex(prefix, (RepositoryMetricIndex) index);
				return;
		}

		throw new RuntimeQueryEvaluationException("Unsupported Index: " + index + " of type: " + index.indexType());
	}

	private void viewIndex(String prefix, GeneratedIndex index) {
		println(prefix + getSimpleClassName(index));
		levelUp();
		view("operand: ", index.getOperand());
		view("indexKey: ", index.getIndexKey());
		levelDown();
	}

	private void viewIndex(String prefix, RepositoryIndex index) {
		println(prefix + getSimpleClassName(index));
		levelUp();
		println("indexId: " + index.getIndexId());
		println("tupleSetComponentIndex: " + index.getTupleComponentIndex());
		levelDown();
	}

}
