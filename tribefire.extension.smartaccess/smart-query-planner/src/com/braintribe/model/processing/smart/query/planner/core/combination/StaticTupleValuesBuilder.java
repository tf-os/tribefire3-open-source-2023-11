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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import static com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper.extractConversion;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.StaticTuple;
import com.braintribe.model.smartqueryplan.set.StaticTuples;

/**
 * @author peter.gazdik
 */
public class StaticTupleValuesBuilder extends AbstractTsBuilder {

	private final SingleSourceGroup group;
	private final EntitySourceNode sourceNode;

	private boolean isEmptyTupleSet;

	private List<DeducedTuple> deducedTuples;
	private DeducedTuple currentDeducedTuple;

	static class DeducedTuple {
		Map<String, Object> propertyValues = newMap();
		private String signatureValue;
		private boolean idSelected;

		/**
		 * Specifies whether information deduced from the condition is enough to replace query with a
		 * {@link StaticTuple}. This is only iff we are selecting an id, we specify all the selected properties, and
		 * signature if needed.
		 */
		private boolean canReplaceQuery(Collection<String> selectedDelegateProperties, boolean signatureSelected) {
			return idSelected && propertyValues.keySet().containsAll(selectedDelegateProperties) && (!signatureSelected || signatureValue != null);
		}
	}

	public StaticTupleValuesBuilder(SmartQueryPlannerContext context, SingleSourceGroup group) {
		super(context);

		this.group = group;
		this.sourceNode = group.sourceNode;
	}

	public TupleSet build() {
		if (!hasNoDelegatableJoins())
			return null;

		findStaticPropertyValues();
		if (deducedTuples == null)
			return null;

		Collection<String> selectedDelegateProperties = sourceNode.selectedDelegateProperties();

		List<List<ScalarMapping>> disjunctedScalarMappings = newList();
		for (DeducedTuple deducedTuple : deducedTuples) {
			if (!deducedTuple.canReplaceQuery(selectedDelegateProperties, sourceNode.isSignatureSelected()))
				return null;

			List<ScalarMapping> scalarMappings = buildValueMapping(deducedTuple);
			if (scalarMappings == null)
				return null;

			disjunctedScalarMappings.add(scalarMappings);
		}

		return buildTupleSetFor(disjunctedScalarMappings);
	}

	private TupleSet buildTupleSetFor(List<List<ScalarMapping>> disjunctedScalarMappings) {
		if (disjunctedScalarMappings.size() == 1)
			return buildStaticTuple(disjunctedScalarMappings.get(0));
		else
			return buildStaticTuples(disjunctedScalarMappings);
	}

	private TupleSet buildStaticTuples(List<List<ScalarMapping>> disjunctedScalarMappings) {
		StaticTuples result = StaticTuples.T.create();

		for (List<ScalarMapping> scalarMappings : disjunctedScalarMappings)
			result.getTuples().add(buildStaticTuple(scalarMappings));

		return result;
	}

	private StaticTuple buildStaticTuple(List<ScalarMapping> scalarMappings) {
		StaticTuple result = StaticTuple.T.createPlain();
		result.setScalarMappings(scalarMappings);

		return result;
	}

	private boolean hasNoDelegatableJoins() {
		return sourceNode.getExplicitJoins().isEmpty() && sourceNode.getSimpleCollectionJoins().isEmpty()
				&& sourceNode.getSimpleUnmappedCollectionJoins().isEmpty();
	}

	private void findStaticPropertyValues() {
		List<List<ValueComparison>> vcsList = extractEqualityComparisons(group.conditions);
		if (vcsList == null)
			return;

		deducedTuples = newList();
		for (List<ValueComparison> vcs : vcsList) {
			currentDeducedTuple = new DeducedTuple();

			for (ValueComparison vd : vcs) {
				if (!addPropertyValueEntry(vd)) {
					deducedTuples = null;
					return;
				}
			}

			deducedTuples.add(currentDeducedTuple);
		}
	}

	private List<List<ValueComparison>> extractEqualityComparisons(Collection<Condition> conditions) {
		List<ValueComparison> conjs = newList();
		List<List<ValueComparison>> disjs = newList();

		if (!extractEqualityComparisons(conditions, conjs, disjs, true))
			return null;

		if (disjs.isEmpty()) {
			disjs.add(conjs);
			return disjs;
		}

		for (List<ValueComparison> disj : disjs)
			disj.addAll(conjs);

		return disjs;
	}

	private boolean extractEqualityComparisons(Collection<Condition> conditions, List<ValueComparison> conjs, List<List<ValueComparison>> disjs,
			boolean allowDisjunction) {

		for (Condition c : inline(conditions, ConditionType.conjunction, newList())) {
			switch (c.conditionType()) {
				case conjunction: {
					Conjunction conjunction = (Conjunction) c;
					boolean ok = extractEqualityComparisons(conjunction.getOperands(), conjs, disjs, allowDisjunction);
					if (!ok)
						return false;
					else
						allowDisjunction &= disjs.isEmpty();

					break;
				}

				case valueComparison: {
					ValueComparison vc = (ValueComparison) c;
					if (vc.getOperator() != Operator.equal)
						return false;

					conjs.add(vc);
					break;
				}

				// If have any of these conditions, we return null (indication building StaticTupelValues is not
				// possible)
				case disjunction: {
					if (!allowDisjunction)
						return false;

					boolean ok = extractEqualityComparisonsFromDisjunction((Disjunction) c, disjs);
					if (!ok)
						return false;
					else
						allowDisjunction = false;
					break;
				}
				case fulltextComparison:
				case negation:
					return false;
			}
		}

		return true;
	}

	private boolean extractEqualityComparisonsFromDisjunction(Disjunction disjunction, List<List<ValueComparison>> disjs) {
		for (Condition c : inline(disjunction.getOperands(), ConditionType.disjunction, newList())) {
			List<ValueComparison> disjunctionVcs = newList();
			boolean ok = extractEqualityComparisons(Arrays.asList(c), disjunctionVcs, null, false);
			if (!ok)
				return false;
			else
				disjs.add(disjunctionVcs);
		}
		return true;
	}

	private List<Condition> inline(Collection<Condition> conditions, ConditionType junctionType, List<Condition> result) {
		for (Condition c : conditions) {
			if (c.conditionType() == junctionType)
				inline(((AbstractJunction) c).getOperands(), junctionType, result);
			else
				result.add(c);
		}
		return result;
	}

	// ###################################################
	// ## . . . . Adding correct delegate value . . . . ##
	// ###################################################

	private boolean addPropertyValueEntry(ValueComparison vd) {
		Object left = vd.getLeftOperand();
		Object right = vd.getRightOperand();

		PersistentEntityReference ref = findReference(left, right);
		if (ref != null)
			return addReferencEntry(ref, other(ref, left, right));

		Object primitive = findPrimitive(left, right);
		if (primitive != null)
			return addPrimitiveEntry(primitive, other(primitive, left, right));

		return false;
	}

	private Object other(Object that, Object o1, Object o2) {
		return that == o1 ? o2 : o1;
	}

	private PersistentEntityReference findReference(Object... os) {
		for (Object o : os)
			if (o instanceof PersistentEntityReference)
				return (PersistentEntityReference) o;

		return null;
	}

	private boolean addReferencEntry(PersistentEntityReference ref, Object other) {
		From from = null;

		if (other instanceof From) {
			from = (From) other;

		} else if (other instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) other;
			if (po.getPropertyName() != null)
				return false;

			Source source = po.getSource();
			if (source instanceof From)
				from = (From) source;
		}

		if (from == null)
			return false;

		currentDeducedTuple.idSelected = true;

		GmEntityType smartFromType = modelExpert.resolveSmartEntityType(from.getEntityTypeSignature());
		GmEntityType smartRefType = modelExpert.resolveSmartEntityType(ref.getTypeSignature());

		if (!modelExpert.isFirstAssignableFromSecond(smartFromType, smartRefType)) {
			/**/
			isEmptyTupleSet = true;
			return true;
		}

		EntityPropertyMapping epm = resolveEpm(GenericEntity.id);

		PersistentEntityReference delegateRef = (PersistentEntityReference) convertToDelegateIfNeeded(ref, other);

		currentDeducedTuple.signatureValue = delegateRef.getTypeSignature();
		currentDeducedTuple.propertyValues.put(epm.getDelegatePropertyName(), delegateRef.getRefId());

		return true;
	}

	private Object findPrimitive(Object o1, Object o2) {
		if (!(o1 instanceof GenericEntity))
			return o1;

		if (!(o2 instanceof GenericEntity))
			return o2;

		return null;
	}

	private boolean addPrimitiveEntry(Object primitive, Object other) {
		if (!(other instanceof PropertyOperand))
			return false;

		PropertyOperand po = (PropertyOperand) other;
		String smartPropertyName = po.getPropertyName();
		EntityPropertyMapping epm = resolveEpm(smartPropertyName);

		if (GenericEntity.id.equals(smartPropertyName))
			currentDeducedTuple.idSelected = true;

		currentDeducedTuple.propertyValues.put(epm.getDelegatePropertyName(), convertToDelegateIfNeeded(primitive, other));

		return true;
	}

	private EntityPropertyMapping resolveEpm(String smartProperty) {
		return modelExpert.resolveEntityPropertyMapping(sourceNode.getSmartGmType(), sourceNode.getAccess(), smartProperty);
	}

	// ###################################################
	// ## . . . . Building actual Value Mapping . . . . ##
	// ###################################################

	private List<ScalarMapping> buildValueMapping(DeducedTuple deducedTuple) {
		if (isEmptyTupleSet)
			return Collections.emptyList();

		List<ScalarMapping> result = newList();

		for (Entry<String, Object> entry : deducedTuple.propertyValues.entrySet()) {
			String delegateP = entry.getKey();
			Object smartValue = entry.getValue();

			Collection<ConversionWrapper> delegateConversions = sourceNode.getConversionsForDelegateProperty(delegateP);
			if (delegateConversions == null)
				// this means that the property was not selected, thus we cannot use the value even though we have
				// deduced it
				continue;

			for (ConversionWrapper cw : delegateConversions) {
				int propertyPosition = sourceNode.acquirePropertyPosition(delegateP, cw);
				result.add(newScalarMapping(smartValue, propertyPosition, cw));
			}
		}

		if (needsTypeSignature(sourceNode)) {
			int propertyPosition = sourceNode.acquireDelegateSignaturePosition();
			result.add(newScalarMapping(deducedTuple.signatureValue, propertyPosition, null));
		}

		return result;
	}

	private ScalarMapping newScalarMapping(Object delegateValue, int tupleComponentIndex, ConversionWrapper cw) {
		Value value = ValueBuilder.staticValue(delegateValue);
		value = SmartValueBuilder.ensureConvertedValue(value, extractConversion(cw), false);

		return SmartValueBuilder.scalarMapping(tupleComponentIndex, value);
	}

}
