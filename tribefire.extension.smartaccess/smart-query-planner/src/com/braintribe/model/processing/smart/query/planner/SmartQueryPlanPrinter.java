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
package com.braintribe.model.processing.smart.query.planner;

import java.util.List;
import java.util.Map.Entry;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.processing.query.tools.QueryPlanPrinter;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;
import com.braintribe.model.smartqueryplan.filter.SmartFullText;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.functions.DiscriminatorValue;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;
import com.braintribe.model.smartqueryplan.set.DelegateQueryAsIs;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;
import com.braintribe.model.smartqueryplan.set.SmartTupleSet;
import com.braintribe.model.smartqueryplan.set.StaticTuple;
import com.braintribe.model.smartqueryplan.set.StaticTuples;
import com.braintribe.model.smartqueryplan.value.CompositeDiscriminatorBasedSignature;
import com.braintribe.model.smartqueryplan.value.CompositeDiscriminatorSignatureRule;
import com.braintribe.model.smartqueryplan.value.ConvertedValue;
import com.braintribe.model.smartqueryplan.value.SimpleDiscriminatorBasedSignature;
import com.braintribe.model.smartqueryplan.value.SmartEntitySignature;
import com.braintribe.model.smartqueryplan.value.SmartValue;

/**
 * 
 */
public class SmartQueryPlanPrinter extends QueryPlanPrinter {

	private static final Logger log = Logger.getLogger(SmartQueryPlanPrinter.class);

	public static String printSafe(SmartQueryPlan plan) {
		try {
			return print(plan);

		} catch (Exception e) {
			log.warn("SmartQueryPlan printing failed.", e);
			return "<SmartQueryPlan printing failed>";
		}
	}

	public static String print(SmartQueryPlan plan) {
		return print(plan.getTupleSet());
	}

	public static String print(TupleSet set) {
		return print(set, false);
	}

	public static String print(TupleSet set, boolean useSimpleName) {
		SmartQueryPlanPrinter printer = new SmartQueryPlanPrinter().setUseEntitySimpleName(useSimpleName);
		printer.view(set);

		return printer.builder.toString();
	}

	public static String printSafe(SelectQuery query) {
		try {
			return print(query);

		} catch (Exception e) {
			log.warn("SelectQuery printing failed.", e);
			return "<SelectQuery printing failed>";
		}
	}

	public static String print(SelectQuery query) {
		BasicQueryStringifier stringifier = BasicQueryStringifier.create().shorteningMode().simplified();
		stringifier.addExpertDefinition(ResolveId.class, (ri, context) -> {
			return "ResolveId[" + context.stringify(ri.getSource()) + "]";
		});

		return stringifier.stringify(query);
	}

	@Override
	public SmartQueryPlanPrinter setUseEntitySimpleName(boolean useSimpleName) {
		return (SmartQueryPlanPrinter) super.setUseEntitySimpleName(useSimpleName);
	}

	// ####################################
	// ## . . . . VIEW TUPLE SET . . . . ##
	// ####################################

	@Override
	protected void view(TupleSet tupleSet) {
		if (tupleSet.tupleSetType() == TupleSetType.extension)
			view((SmartTupleSet) tupleSet);
		else
			super.view(tupleSet);
	}

	private void view(SmartTupleSet tupleSet) {
		println(getSimpleClassName(tupleSet));

		levelUp();
		switch (tupleSet.smartType()) {
			case delegateQueryAsIs:
				viewTupleSet((DelegateQueryAsIs) tupleSet);
				break;
			case delegateQueryJoin:
				viewTupleSet((DelegateQueryJoin) tupleSet);
				break;
			case delegateQuerySet:
				viewTupleSet((DelegateQuerySet) tupleSet);
				break;
			case orderedConcatenation:
				viewTupleSet((OrderedConcatenation) tupleSet);
				break;
			case staticTuple:
				viewTupleSet((StaticTuple) tupleSet);
				break;
			case staticTuples:
				viewTupleSet((StaticTuples) tupleSet);
				break;
			default:
				throw new RuntimeQueryEvaluationException("Unsupported TupleSet: " + tupleSet + " of type: " + tupleSet.tupleSetType());
		}
		levelDown();
	}

	private void viewTupleSet(DelegateQueryAsIs tupleSet) {
		println("delegateAccess: " + tupleSet.getDelegateAccess().getExternalId() + " [exertnalId]");
		view("delegateQuery: ", tupleSet.getDelegateQuery());
	}

	private void viewTupleSet(DelegateQueryJoin tupleSet) {
		println("isLeftJoin: " + tupleSet.getIsLeftJoin());
		print("materializedSet: ");
		view(tupleSet.getMaterializedSet());
		print("querySet: ");
		view(tupleSet.getQuerySet());
		println("JoinRestrictions: ");
		for (OperandRestriction or : tupleSet.getJoinRestrictions()) {
			view("- ", or);
		}
	}

	private void view(String prefix, OperandRestriction or) {
		println(prefix + "OperandRestriction");
		levelUp();
		println("operand: " + or.getQueryOperand());
		view("materializedCorrelationValue: ", or.getMaterializedCorrelationValue());
		levelDown();
	}

	private void viewTupleSet(DelegateQuerySet tupleSet) {
		println("delegateAccess: " + tupleSet.getDelegateAccess().getExternalId() + " [exertnalId]");
		view("delegateQuery: ", tupleSet.getDelegateQuery());
		println("batchSize: " + tupleSet.getBatchSize());
		println("scalarMappings:");
		for (ScalarMapping sm : tupleSet.getScalarMappings())
			view("- ", sm);
	}

	private void viewTupleSet(StaticTuple tupleSet) {
		println("scalarMappings:");
		for (ScalarMapping sm : tupleSet.getScalarMappings())
			view("- ", sm);
	}

	private void viewTupleSet(StaticTuples tupleSet) {
		println("StaticTuples:");
		for (StaticTuple tuple : tupleSet.getTuples())
			view("- ", tuple);
	}

	private void view(String prefix, SelectQuery query) {
		println(prefix + " " + print(query));
	}

	private void view(String prefix, ScalarMapping sm) {
		levelUp();
		print(prefix);
		view(sm.getTupleComponentIndex() + " <- ", sm.getSourceValue());
		levelDown();
	}

	private void viewTupleSet(OrderedConcatenation tupleSet) {
		super.viewTupleSet(tupleSet);

		for (SortCriterion c : tupleSet.getSortCriteria())
			viewSortCriterion(c);
	}

	// ####################################
	// ## . . . . VIEW CONDITION. . . . .##
	// ####################################

	@Override
	protected void view(String prefix, Condition condition) {
		if (condition instanceof SmartFullText)
			view(prefix, (SmartFullText) condition);
		else
			super.view(prefix, condition);
	}

	private void view(String prefix, SmartFullText condition) {
		println(prefix + "SmartFullText [");
		levelUp();
		println("positions: " + condition.getStringPropertyPositions());
		println("text: " + condition.getText());
		levelDown();
		println("]");
	}

	// ####################################
	// ## . . . . . VIEW VALUE . . . . . ##
	// ####################################

	@Override
	protected void view(String prefix, Value value) {
		if (value == null)
			return;

		if (value.valueType() == ValueType.extension)
			view(prefix, (SmartValue) value);
		else
			super.view(prefix, value);
	}

	private void view(String prefix, SmartValue value) {
		switch (value.smartValueType()) {
			case compositeDiscriminatorBasedSignature:
				view(prefix, (CompositeDiscriminatorBasedSignature) value);
				return;
			case convertedValue:
				view(prefix, (ConvertedValue) value);
				return;
			case simpleDiscriminatorBasedSignature:
				view(prefix, (SimpleDiscriminatorBasedSignature) value);
				return;
			case smartEntitySignature:
				view(prefix, (SmartEntitySignature) value);
				return;
		}

		throw new RuntimeQueryEvaluationException("Unsupported SmartValue: " + value + " of type: " + value.valueType());
	}

	private void view(String prefix, SimpleDiscriminatorBasedSignature value) {
		print(prefix + "SimpleDiscriminatorBasedSignature ");
		println("(tuplePosition: " + value.getTuplePosition() + ") [");

		levelUp();
		for (Entry<Object, String> entry : value.getSignatureMapping().entrySet())
			println(entry.getKey() + " -> " + entry.getValue());
		levelDown();

		println("]");
	}

	private void view(String prefix, CompositeDiscriminatorBasedSignature value) {
		print(prefix + "CompositeDiscriminatorBasedSignature ");

		levelUp();
		List<Integer> tuplePositions = value.getTuplePositions();
		println("tuplePostions: " + tuplePositions);

		for (CompositeDiscriminatorSignatureRule rule : value.getRules())
			println(rule.getDiscriminatorValues() + " -> " + rule.getSignature());
		levelDown();

		println("]");
	}

	private void view(String prefix, ConvertedValue value) {
		SmartConversion conversion = value.getConversion();

		String conversionName = conversion.entityType().getJavaType().getSimpleName();
		String inverted = conversion.getInverse() ? "inverted " : "";
		String smartToDelegate = value.getInverse() ? "[Inverted (SmartToDelegate)]" : "";
		println(prefix + " Conversion" + smartToDelegate + "(" + inverted + conversionName + ") [");
		levelUp();
		view("conversion operand: ", value.getOperand());
		levelDown();
		println("]");
	}

	private void view(String prefix, SmartEntitySignature value) {
		print(prefix + "SmartEntitySignature ");
		if (value.getSignature() != null) {
			print("[" + value.getSignature());

		} else {
			println("(tuplePosition: " + value.getTuplePosition() + ") [");
			levelUp();
			for (Entry<String, String> entry : value.getSignatureMapping().entrySet())
				println(entry.getKey() + " -> " + entry.getValue());
			levelDown();
			value.getTuplePosition();
		}

		println("]");
	}

	@Override
	protected void view(String prefix, QueryFunction function) {
		if (function instanceof AssembleEntity) {
			view(prefix, (AssembleEntity) function);
			return;
		}

		if (function instanceof DiscriminatorValue) {
			view(prefix, (DiscriminatorValue) function);
			return;
		}

		super.view(prefix, function);
	}

	private void view(String prefix, AssembleEntity ae) {
		println(prefix + " Assemble (" + ae.getEntitySignature() + ") [");

		levelUp();
		{
			println("propertyMappings:");
			levelUp();
			for (Entry<String, PropertyMappingNode> entry : ae.getSignatureToPropertyMappingNode().entrySet())
				view(entry.getKey() + ":", entry.getValue());
			levelDown();

			view("smartEntitySignature: ", ae.getSmartEntitySignature());

		}
		levelDown();

		println("]");
	}

	private void view(String prefix, DiscriminatorValue dv) {
		println(prefix + " DiscriminatorValue (" + dv.getEntityPropertySignature() + ") [");

		levelUp();
		{
			println("propertyMappings:");
			levelUp();
			for (Entry<String, Object> entry : dv.getSignatureToStaticValue().entrySet())
				println(entry.getKey() + " -> " + entry.getValue());
			levelDown();

			println("signaturePosition: " + dv.getSignaturePosition());
		}
		levelDown();

		println("]");
	}

	private void view(String prefix, PropertyMappingNode propertyMapping) {
		println(prefix);
		levelUp();
		for (Entry<String, Value> entry : propertyMapping.getPropertyMappings().entrySet()) {
			view(entry.getKey() + " <- ", entry.getValue());
		}
		levelDown();
	}

}
