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
package com.braintribe.model.processing.query.planner.core.cross.simple;

import static com.braintribe.model.query.Operator.equal;
import static com.braintribe.model.query.Operator.greater;
import static com.braintribe.model.query.Operator.greaterOrEqual;
import static com.braintribe.model.query.Operator.in;
import static com.braintribe.model.query.Operator.less;
import static com.braintribe.model.query.Operator.lessOrEqual;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * 
 */
class ConditionAnalysisTools {

	/* Note that the {@link ConditionNormalizer} ensures that if only one of the operands is an {@link Operand}, it will be the left one. */
	static From getSingleFromOperand(ValueComparison condition, QueryPlannerContext context) {
		return getIfEffectivelyFrom(condition.getLeftOperand(), context);
	}

	static From getIfEffectivelyFrom(Object o, QueryPlannerContext context) {
		if (context.isEvaluationExclude(o))
			return null;

		if (o instanceof From)
			return (From) o;

		if (o instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) o;
			if (po.getPropertyName() == null)
				return getIfFrom(po.getSource(), context);
		}

		return null;
	}

	static From getIfFrom(Object o) {
		return o instanceof From ? (From) o : null;
	}

	static From getIfFrom(Object o, QueryPlannerContext context) {
		return o instanceof From && !context.isEvaluationExclude(o) ? (From) o : null;
	}
	
	static Object findStaticValue(ValueComparison condition, QueryPlannerContext context) {
		return getIfStaticValue(condition.getRightOperand(), context);
	}

	static Object getIfStaticValue(Object o, QueryPlannerContext context) {
		return o instanceof Operand && !context.isEvaluationExclude(o) ? null : o;
	}

	static Operand getIfOperand(Object o, QueryPlannerContext context) {
		return o instanceof Operand && !context.isEvaluationExclude(o) ? (Operand) o : null;
	}

	/* Note that the {@link ConditionNormalizer} ensures that if only one of the operands is an {@link Operand}, it will be the left one. */
	static PropertyOperand findSinglePropertyOperand(ValueComparison condition, QueryPlannerContext context) {
		return getIfPropertyOperand(condition.getLeftOperand(), context);
	}

	static PropertyOperand getIfPropertyOperand(Object o, QueryPlannerContext context) {
		return o instanceof PropertyOperand && !context.isEvaluationExclude(o) ? (PropertyOperand) o : null;
	}

	/**
	 * Returns (if possible) a list of {@link IndexInfo} which represent the path starting at {@link From} corresponding to given <tt>source</tt> and
	 * goes all the way to given <tt>propertyName</tt>. These {@linkplain IndexInfo}s are, however, returned in reverse order as to what the
	 * join-chain goes. Is no such chain exists, empty list is returned.
	 * <p>
	 * For example, if we have an index chain like this: <tt>Comany.indexedOwner.indexedName</tt>, this method would return a list of size two, the
	 * first element being info about <tt>Owner.indexedName</tt> index, and the second would be <tt>Company.indexedOwner</tt>.
	 */
	static List<IndexInfo> findIndexChainIfPossible(Source source, String propertyName, QueryPlannerContext context) {
		List<IndexInfo> result = newList();

		while (true) {
			GenericModelType type = context.sourceManager().resolveType(source);

			IndexInfo indexInfo = context.getIndexInfo(type.getTypeSignature(), propertyName);

			if (indexInfo == null)
				return Collections.emptyList();

			result.add(indexInfo);

			if (source instanceof From)
				return result;

			Join join = (Join) source;
			propertyName = join.getProperty();
			source = join.getSource();
		}
	}

	static IndexInfo getIndexInfoIfPossible(From from, String propertyName, QueryPlannerContext context) {
		return from != null ? context.getIndexInfo(from, propertyName) : null;
	}

	// ###########################################################################
	// ## . . . . . . . . Handling static values for given From . . . . . . . . ##
	// ###########################################################################

	public static Set<?> toRefOrEntitySet(From from, Object staticValue) {
		Collection<?> collection = staticValue instanceof Collection ? (Collection<Object>) staticValue : Arrays.asList(staticValue);
		return filterCompatibleInstances(collection, from);
	}

	private static Set<?> filterCompatibleInstances(Collection<?> collection, From from) {
		EntityType<?> et = resolveType(from.getEntityTypeSignature());
		return collection.stream() //
				.filter(o -> isOrDenotesInstance(o, et)) //
				.collect(Collectors.toSet());
	}

	private static boolean isOrDenotesInstance(Object o, EntityType<?> et) {
		if (o instanceof PersistentEntityReference) {
			String ts = ((PersistentEntityReference) o).getTypeSignature();
			EntityType<?> oType = resolveType(ts);
			return et.isAssignableFrom(oType);

		} else {
			return et.isInstance(o);
		}
	}

	private static EntityType<GenericEntity> resolveType(String ts) {
		return GMF.getTypeReflection().getEntityType(ts);
	}

	// ###########################################################################
	// ## . . . . . . Resolving if static/index source is possible . . . . . . .##
	// ###########################################################################

	private static final EnumSet<Operator> indexCompatibleOperators = EnumSet.of(equal, greater, greaterOrEqual, less, lessOrEqual, in);

	static boolean isOperatorIndexCandidate(ValueComparison condition) {
		return indexCompatibleOperators.contains(condition.getOperator());
	}

	static boolean isEqualityOperator(ValueComparison condition) {
		Operator op = condition.getOperator();
		return op == equal || op == in;
	}

}
