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
package com.braintribe.model.processing.query.eval.set.join;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Iterator;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.tools.FilteringPopulationIterator;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.model.queryplan.set.join.PropertyJoin;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * 
 */
public abstract class AbstractEvalPropertyJoin extends AbstractEvalJoin {

	protected final ValueProperty valueProperty;
	private final String rightEntitySignature;

	private final boolean leftJoin;
	private final boolean rightJoin;

	public AbstractEvalPropertyJoin(PropertyJoin join, QueryEvaluationContext context) {
		super(join, context);

		this.valueProperty = join.getValueProperty();

		JoinKind joinKind = join.getJoinKind();
		this.leftJoin = joinKind == JoinKind.left || joinKind == JoinKind.full;
		this.rightJoin = joinKind == JoinKind.right || joinKind == JoinKind.full;

		this.rightEntitySignature = rightJoin ? rightType().getTypeSignature() : null;
	}

	/**
	 * TODO IMPROVE
	 * 
	 * We are trying to resolve a type for given value property. But this value property probably represents the property in a wrong way. Our
	 * {@link PropertyJoin} contains an operand, and then we have the property name. So if we should simply resolve the operand type and then resolve
	 * the type of this property. We, however, have this property value represented in a different way, and we do not access the operand directly, but
	 * we use a {@link ValueProperty}, where the value is a {@link TupleComponent}. Therefore, the context needs to be able to resolve the operand on
	 * that position, which means he needs a mapping from position (Integer) to the actual operand (which is an instance of
	 * {@link TupleComponentPosition}).
	 * 
	 * If we changed the type-resolution, we might not need the mapping at all (which is good, as we then do not force sub-classes (like the one for
	 * SmartQueries) to think about what to do there).
	 */
	private EntityType<?> rightType() {
		GenericModelType type = context.resolveValueType(valueProperty);

		if (type instanceof CollectionType)
			type = ((CollectionType) type).getCollectionElementType();

		if (type instanceof EntityType<?>)
			return (EntityType<?>) type;

		throw new RuntimeQueryEvaluationException(
				"Left-outer join can only be done if the right type is an EntityType of a collection of EntityTypes. Actual resolved type was: "
						+ type + ". Value property: " + valueProperty);
	}

	/**
	 * This is the abstract base class for {@link PropertyJoin} iterators. It is however split into two classes (this and
	 * {@link AbstractPropertyJoinLeftOuterSupportingIterator}), to make the code more readable. This class adds the support for the right-join, while
	 * the other (super class of this) adds support for the left join.
	 * <p>
	 * This more or less works as an around/before interceptor for the methods implemented by the super-class, handling the special case when we
	 * already ran out of operand tuples and want to add those from right side which were not touched yet (thus achieving the left-outer-join).
	 * <p>
	 * Right now the if we want to drop the left-outer-join for some cases (for whatever reason) it would suffice to simply change the super-type of
	 * the particular iterator to the super-class of this ( {@linkplain AbstractPropertyJoinLeftOuterSupportingIterator}). This class really is just
	 * an interceptor.
	 */
	protected abstract class AbstractPropertyJoinIterator extends AbstractPropertyJoinLeftOuterSupportingIterator {
		/* Iterator for all the right values where left side is null. This is only relevant if we are doing a right join and */
		private Iterator<GenericEntity> rightJoinIterator;
		private final Set<Object> rightValues;

		protected AbstractPropertyJoinIterator() {
			this.rightValues = rightJoin ? newSet() : null;

			super.initialize();
		}

		@Override
		protected boolean prepareNextOperandTuple() {
			if (rightJoinIterator != null)
				/* This means we have already set the rightJoinIterator before and have run out of it's elements -> there is nothing left */
				return false;

			// note that this might not initialize the nextTuple, if the left side is empty
			if (super.prepareNextOperandTuple())
				return true;
			else if (!rightJoin)
				return false;

			// code for right join
			if (nextTuple == null)
				/* nextTuple can be null if the entire left part of the join was empty (thus super.prepareNextOperandTuple() didn't initialize it) */
				nextTuple = singletonTuple;

			nextTuple.clear();

			Iterator<? extends GenericEntity> populationIterator = context.getPopulation(rightEntitySignature).iterator();
			rightJoinIterator = new FilteringPopulationIterator<GenericEntity>(populationIterator, rightValues);

			return rightJoinIterator.hasNext();
		}

		@Override
		protected final boolean hasNextJoinedValue() {
			if (rightJoinIterator != null)
				return rightJoinIterator.hasNext();

			return super.hasNextJoinedValue();
		}

		@Override
		protected final void setNextJoinedValue(ArrayBasedTuple tuple) {
			if (rightJoinIterator != null)
				tuple.setValueDirectly(componentPosition, rightJoinIterator.next());
			else
				super.setNextJoinedValue(tuple);
		}

		/**
		 * This method must be used by the sub-class to set the right-value, because this tracks the used right values for the left-outer-join
		 * purposes.
		 */
		@Override
		protected final void setRightValue(ArrayBasedTuple tuple, Object value) {
			super.setRightValue(tuple, value);

			if (rightJoin)
				rightValues.add(value);
		}

	}

	/**
	 * A base class for {@link PropertyJoin} interceptors that adds support for the left outer join. It re-defines the methods to be implemented,
	 * handling the outer-join case by itself, and forwarding to inner-join methods in the standard case. These new methods are called same as the old
	 * ones with an extra "$LeftInner" suffix to indicate the standard (non-outer) case.
	 * 
	 * @see AbstractEvalPropertyJoin.AbstractPropertyJoinIterator
	 */
	protected abstract class AbstractPropertyJoinLeftOuterSupportingIterator extends AbstractJoinIterator {
		private boolean nextJoinedValueIsNotNull;
		private boolean useNullForNextJoinedValue;

		@Override
		protected void onNewOperandTuple(ArrayBasedTuple tuple) {
			nextJoinedValueIsNotNull = context.<Object> resolveValue(tuple, valueProperty) != null;

			if (nextJoinedValueIsNotNull)
				onNewOperandTuple$LeftInner(tuple);
			else
				useNullForNextJoinedValue = leftJoin;
		}

		@Override
		protected boolean hasNextJoinedValue() {
			return useNullForNextJoinedValue || (nextJoinedValueIsNotNull && hasNextJoinedValue$LeftInner());
		}

		@Override
		protected void setNextJoinedValue(ArrayBasedTuple tuple) {
			if (useNullForNextJoinedValue) {
				useNullForNextJoinedValue = false;
				setNextJoinedValueAsVoid(tuple);

			} else {
				setNextJoinedValue$LeftInner(tuple);
			}
		}

		protected void setRightValue(ArrayBasedTuple tuple, Object value) {
			tuple.setValueDirectly(componentPosition, value);
		}

		protected abstract void onNewOperandTuple$LeftInner(ArrayBasedTuple tuple);

		protected abstract boolean hasNextJoinedValue$LeftInner();

		protected abstract void setNextJoinedValue$LeftInner(ArrayBasedTuple tuple);

		protected abstract void setNextJoinedValueAsVoid(ArrayBasedTuple tuple);
	}

}
