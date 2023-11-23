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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder.conjunction;
import static com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder.disjunction;
import static com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder.valueComparison;
import static com.braintribe.model.processing.smart.query.planner.context.ConstantPropertyTools.entitySignatureFor;
import static com.braintribe.model.processing.smart.query.planner.context.ConstantPropertyTools.toValueComparison;
import static com.braintribe.model.processing.smart.query.planner.context.StaticValueComparisonEvaluator.evaluateVc;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.planner.condition.ConditionNormalizer;
import com.braintribe.model.processing.query.planner.condition.ConstantCondition;
import com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder;
import com.braintribe.model.processing.smart.query.planner.context.StaticValueComparisonEvaluator.SimpleValueResolver;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConstantPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.ConstantPropertyWrapper;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;

/**
 * @author peter.gazdik
 */
class SmartConditionNormalizer extends ConditionNormalizer {

	private final ModelExpert modelExpert;
	private final QueryPlanStructure planStructure;

	public SmartConditionNormalizer(SmartQueryPlannerContext context) {
		super(context.evalExclusionCheck());

		this.modelExpert = context.modelExpert();
		this.planStructure = context.planStructure();
	}

	@Override
	protected Condition normalize(ValueComparison vc) {
		vc = (ValueComparison) super.normalize(vc);

		if (vc instanceof ConstantCondition)
			return vc;

		EntitySourceNode leftEsn = getEsnIfPossible(vc.getLeftOperand());
		EntitySourceNode rightEsn = getEsnIfPossible(vc.getRightOperand());

		EntityPropertyMapping leftEpm = getEpmIfPropertyOperand(leftEsn, vc.getLeftOperand());
		EntityPropertyMapping rightEpm = getEpmIfPropertyOperand(rightEsn, vc.getRightOperand());

		if (leftEsn != null && leftEpm == null)
			return normalizeEntityComparison(vc, leftEsn);

		ConstantPropertyWrapper leftNode = getOwnerNodeIfConstantProperty(leftEpm);
		ConstantPropertyWrapper rightNode = getOwnerNodeIfConstantProperty(rightEpm);

		if (leftNode != null || rightNode != null)
			return normalizeConstantPropertyComparison(vc, leftNode, rightNode);

		// if right side is also some property operand, we cannot do anythihng
		if (rightEpm != null)
			return vc;

		if (isPartitionEmp(leftEpm))
			return normalizePartitionComparison(vc, leftEpm);

		return vc;
	}

	private EntitySourceNode getEsnIfPossible(Object operand) {
		Source source = getSourceIfPossible(operand);
		if (source == null)
			return null;

		SourceNode sn = planStructure.getSourceNode(source);
		return sn instanceof EntitySourceNode ? (EntitySourceNode) sn : null;
	}

	private Source getSourceIfPossible(Object operand) {
		if (!isOperand(operand))
			return null;

		if (operand instanceof Source)
			return (Source) operand;

		if (operand instanceof PropertyOperand)
			return ((PropertyOperand) operand).getSource();

		return null;
	}

	private EntityPropertyMapping getEpmIfPropertyOperand(EntitySourceNode esn, Object operand) {
		if (esn == null)
			return null;

		if (!(operand instanceof PropertyOperand))
			return null;

		PropertyOperand po = (PropertyOperand) operand;
		String propertyName = po.getPropertyName();

		return propertyName != null ? esn.resolveSmartPropertyMapping(propertyName) : null;
	}

	// ########################################################
	// ## . . . . . . . . Entity Comparison . . . . . . . . ##
	// ########################################################

	/**
	 * Assuming the leftOperand is asn entity, and the right operand is a constant value (i.e. EntityReference), we
	 * normalize this condition to:
	 * 
	 * <ul>
	 * <li>FALSE - if none of the specified references' partitions match the property's mapped access</li>
	 * <li>original VC - if the property's mapped access contains all the matching references</li>
	 * <li>original VC - otherwise</li>
	 * </ul>
	 * 
	 * To understand the difference between last two points, imagine our access handles partition "p1". The condition
	 * {@code where e in (ref(Person, 1, "p1"))} can be used as is, while in case of
	 * {@code where e in (ref(Person, 1, "p1"), ref(Person, 99, "p2"))} the 99/"p2" Person doesn't have to be delegated.
	 */
	private Condition normalizeEntityComparison(ValueComparison vc, EntitySourceNode leftEsn) {
		Object rightOperand = vc.getRightOperand();

		Map<String, Object> partitionToRefs = asPartitionToRefOrListOfRefs(rightOperand);
		if (partitionToRefs == null || partitionToRefs.containsKey(EntityReference.ANY_PARTITION))
			return vc;

		// This is terrible - it's about left operand being a collection if the condition is "contains". Find a better
		// solution!!!
		Set<String> ps = modelExpert.getPartitions(leftEsn.getAccess());
		Set<Object> partitions = vc.getOperator() == Operator.contains ? asSet(ps) : (Set<Object>) (Set<?>) ps;

		List<List<EntityReference>> matchingReferences = evaluateVc(vc.getOperator(), partitions, partitionToRefs.entrySet(), false, ENTITY_REF_VR);

		if (matchingReferences.isEmpty())
			return ConstantCondition.FALSE;

		if (rightOperand instanceof EntityReference)
			return vc;

		Set<EntityReference> originalRefs = (Set<EntityReference>) rightOperand;
		Set<EntityReference> matchingRefs = matchingReferences.stream() //
				.flatMap(List::stream) //
				.collect(Collectors.toSet());

		if (matchingRefs.size() == originalRefs.size())
			return vc;
		else
			return QueryConditionBuilder.valueComparison(vc.getLeftOperand(), matchingRefs, vc.getOperator());
	}

	/* Returns a map of partition to reference, in case the original */
	private Map<String, Object> asPartitionToRefOrListOfRefs(Object operand) {
		if (operand instanceof EntityReference) {
			EntityReference e = (EntityReference) operand;
			return asMap(e.getRefPartition(), e);
		}

		if (operand instanceof Collection) {
			Map<String, List<EntityReference>> result = newMap();
			for (Object element : (Collection<?>) operand) {
				if (!(element instanceof EntityReference))
					return null;
				EntityReference e = (EntityReference) element;
				acquireList(result, e.getRefPartition()).add(e);
			}
			return (Map<String, Object>) (Map<?, ?>) result;
		}

		return null;
	}

	private static final EntityReferenceValueResolver ENTITY_REF_VR = new EntityReferenceValueResolver();

	static class EntityReferenceValueResolver
			implements SimpleValueResolver<Object/* partition(s) */, Map.Entry<String, Object>, List<EntityReference>> {

		@Override
		public Object resolveLeft(Object left) {
			return left;
		}

		@Override
		public Object resolveRight(Entry<String, Object> right) {
			Object o = right.getValue(); // refOrListOfRefs
			if (o instanceof EntityReference)
				return ((EntityReference) o).getRefPartition();
			else
				return ((List<EntityReference>) o).stream()//
						.map(EntityReference::getRefPartition)//
						.collect(Collectors.toSet());
		}

		@Override
		public List<EntityReference> newEntry(Object left, Entry<String, Object> right) {
			Object o = right.getValue(); // refOrListOfRefs
			return o instanceof EntityReference ? asList((EntityReference) o) : (List<EntityReference>) o;
		}

	}

	// ########################################################
	// ## . . . . . Constant property comparison . . . . . . ##
	// ########################################################

	private ConstantPropertyWrapper getOwnerNodeIfConstantProperty(EntityPropertyMapping epm) {
		return epm instanceof ConstantPropertyWrapper ? (ConstantPropertyWrapper) epm : null;
	}

	// we know that the vc (returned from super.normalize()) has the rightNode as constant if possible.
	private Condition normalizeConstantPropertyComparison(ValueComparison vc, ConstantPropertyWrapper leftNode, ConstantPropertyWrapper rightNode) {
		// one is ConstantProperty, the other is operand or value (if also constant, that is OK, see how operand is
		// handled)

		boolean rightNodeIsConstant;
		ConstantPropertyWrapper constantNode;
		Source source;
		Object otherOperand;

		if (leftNode != null) {
			rightNodeIsConstant = false;
			source = ((PropertyOperand) vc.getLeftOperand()).getSource();
			constantNode = leftNode;
			otherOperand = vc.getRightOperand();

		} else {
			rightNodeIsConstant = true;
			source = ((PropertyOperand) vc.getRightOperand()).getSource();
			constantNode = rightNode;
			otherOperand = vc.getLeftOperand();
		}

		Operator operator = vc.getOperator();

		EntitySourceNode sourceNode = planStructure.getSourceNode(source);
		ConstantPropertyMapping cpm = sourceNode.resolveConstantPropertyMapping(constantNode.getSmartPropertyName());

		if (otherOperand instanceof Operand)
			return normalizeConstantPropertyWhenComparingWithOperand(sourceNode, cpm, operator, otherOperand, rightNodeIsConstant);

		List<String> matchingSignatures = evaluateVc(operator, cpm.smartToValue.entrySet(), asList(otherOperand), rightNodeIsConstant, CONST_PROP_VR);

		if (matchingSignatures.isEmpty())
			return ConstantCondition.FALSE;
		else if (cpm.isStatic)
			return ConstantCondition.TRUE;

		return ConstantPropertyTools.buildEntitySignatureCondition(source, matchingSignatures);
	}

	private static final ConstantPropertyValueResolver CONST_PROP_VR = new ConstantPropertyValueResolver();

	static class ConstantPropertyValueResolver implements SimpleValueResolver<Map.Entry<String, Object>, Object, String> {

		@Override
		public Object resolveLeft(Entry<String, Object> left) {
			return left.getValue();
		}

		@Override
		public Object resolveRight(Object right) {
			return right;
		}

		@Override
		public String newEntry(Entry<String, Object> left, Object right) {
			return left.getKey();
		}

	}

	private Condition normalizeConstantPropertyWhenComparingWithOperand(EntitySourceNode sourceNode, ConstantPropertyMapping cpm, Operator operator,
			Object otherOperand, boolean rightNodeIsConstant) {

		List<Condition> conditions = newList();

		EntitySignature sourceSignature = entitySignatureFor(sourceNode.getSource());

		for (Entry<String, Object> entry : cpm.smartToValue.entrySet()) {
			String smartSignature = entry.getKey();
			Object constantValue = entry.getValue();

			Condition signatureCheck = toValueComparison(sourceSignature, smartSignature);
			Condition constantCheck = rightNodeIsConstant ? valueComparison(otherOperand, constantValue, operator)
					: valueComparison(constantValue, otherOperand, operator);

			conditions.add(conjunction(signatureCheck, constantCheck));
		}

		/* we call the normalize again, because theoretically the "otherOperand" might also be a constant property, */
		return normalize(conditions.size() == 1 ? first(conditions) : disjunction(conditions));
	}

	// ########################################################
	// ## . . . . . Partition property comparison . . . . . .##
	// ########################################################

	private boolean isPartitionEmp(EntityPropertyMapping epm) {
		return epm != null && GenericEntity.partition.equals(epm.getDelegatePropertyName());
	}

	/**
	 * Assuming the leftOperand is the property partition, and the right operand is a constant value (i.e. not an
	 * Operand), we normalize this condition right away to:
	 * 
	 * <ul>
	 * <li>FALSE - if none of the specified partitions match the property's mapped access</li>
	 * <li>TRUE - if the property's mapped access covers all the matching partitions</li>
	 * <li>original VC - otherwise</li>
	 * </ul>
	 * 
	 * To understand the difference between last two points, imagine our access handles partitions "p1" and "p2". When
	 * considering these two conditions: {@code where partition = "p1"}, {@code where partition in ("p1", "p2)}, only
	 * the second condition can be replaced with TRUE, as it must be always true in the delegate.
	 * 
	 * Note that a condition {@code where partition in ("p1", "p3")} could be simplified to reference just "p1", but we
	 * do not do that optimization here.
	 */
	private Condition normalizePartitionComparison(ValueComparison vc, EntityPropertyMapping leftPartitionEpm) {
		Object rightOperand = vc.getRightOperand();
		if (isOperand(rightOperand))
			return vc;

		Set<String> partitions = modelExpert.getPartitions(leftPartitionEpm.getAccess());
		List<String> matchingPartitions = evaluateVc(vc.getOperator(), partitions, asList(rightOperand), false, PARTITION_PROP_VR);

		if (matchingPartitions.isEmpty())
			return ConstantCondition.FALSE;
		else if (partitions.size() == matchingPartitions.size())
			return ConstantCondition.TRUE; // if all the possible partitions are specified in the query, w
		else
			return vc;
	}

	private static final PartitionPropertyValueResolver PARTITION_PROP_VR = new PartitionPropertyValueResolver();

	static class PartitionPropertyValueResolver implements SimpleValueResolver<String, Object, String> {

		@Override
		public Object resolveLeft(String left) {
			return left;
		}

		@Override
		public Object resolveRight(Object right) {
			return right;
		}

		@Override
		public String newEntry(String left, Object right) {
			return left;
		}

	}

}
