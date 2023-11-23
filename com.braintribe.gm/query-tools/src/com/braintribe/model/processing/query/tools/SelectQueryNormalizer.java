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

import static com.braintribe.model.processing.query.tools.SourceTypeResolver.buildPropertyChain;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiPredicate;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyTransferCompetence;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.NormalizedSelectQuery;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Normalizes the query according to given parameters. Mostly the normalization means we are replacing property paths with joins. However, it is not
 * always clear, how to deal with the last property on the path in case a join is possible. These behavior is customizable using "forceJoin" flags.
 * 
 * Some examples for various use-cases regarding forcing joins by query normalizer:
 * <ul>
 * <li>Standard smood planner does not want any forced joins.</li>
 * <li>The HqlBuilder wants to force joins on entities, for generating group-by clause automatically (if eligible). In that case a select like:
 * <tt>select c, c.address, count(p) from Company c join c.emplyees p</tt> can only have a valid group-by if we force the join with the entity, like
 * this: <tt>select c, a, count(p) from Company c join c.emplyees p join c.address a group by c, a</tt>.</li>
 * <li>SmartAccess wants to force both joins, because it wants to have a {@link Source} instance for every source.</li>
 * </ul>
 */
public class SelectQueryNormalizer {

	static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final NormalizedSelectQuery query;
	private final Set<GenericEntity> evaluationExcludes;
	private final boolean forceCollectionJoins;
	private final boolean forceEntityJoins;
	private final Map<Source, SourceWrapper> sourceWrappers = newMap();

	private String defaultPartition;
	private BiPredicate<String, String> mappedPropertyIndicator = (signature, property) -> true;

	private final CloningContext queryCopyingContext = new QueryCopyingCloningContext();
	private final Map<GenericEntity, GenericEntity> copyToOriginal = new IdentityHashMap<>();

	/**
	 * @param query
	 *            The query to be normalized. This entity (or any entities reachable from it) is not modified in any way, but a clone is created
	 *            first, and that one is being modified by the normalizer. However, not all entities of the assembly are cloned, only those where
	 *            modification might be needed. So one cannot assume the returned query is freely modifiable without any side-effects.
	 * @param forceCollectionJoins
	 *            if set to <tt>true</tt>, it means every collection will be joined (both in SELECT clause as well as in WHERE clause). Note that this
	 *            necessarily changes the conventions for the returned {@link SelectQuery} . A query like
	 *            {@code from Person p where 'hacker' in p.nicknames} is transformed into
	 *            {@code from Person p left join p.nickanems n where 'hacker' in n}. The difference is, that now we use a join alias as if it referred
	 *            to the entire collection, and not just a single element. This is a violation of the standard convention, and whoever uses the
	 *            normalizer in this way should be aware of that.
	 * @param forceEntityJoins
	 *            similar to <tt>forceCollectionsJoins</tt>, but is applied to an entity property (i.e. type of the property is {@link GenericEntity})
	 */
	public SelectQueryNormalizer(SelectQuery query, boolean forceCollectionJoins, boolean forceEntityJoins) {
		this.evaluationExcludes = nullSafe(query.getEvaluationExcludes());
		this.query = (NormalizedSelectQuery) copy(query, queryCopyingContext);
		this.forceCollectionJoins = forceCollectionJoins;
		this.forceEntityJoins = forceEntityJoins;
	}

	/**
	 * If default partition is set, the value is used to replace all {@link PropertyOperand}s for the {@link GenericEntity#getPartition() partition},
	 * in case the corresponding property is not mapped. The test whether it's mapped is done using {@link #mappedPropertyIndicator(BiPredicate)
	 * mappedPropertyIndicator}.
	 */
	public SelectQueryNormalizer defaultPartition(String defaultPartition) {
		this.defaultPartition = defaultPartition;
		return this;
	}

	/** @see #defaultPartition(String) */
	public SelectQueryNormalizer mappedPropertyIndicator(BiPredicate<String, String> mappedPropertyIndicator) {
		this.mappedPropertyIndicator = mappedPropertyIndicator;
		return this;
	}

	public <T extends GenericEntity> T getOriginalEntity(T copy) {
		return (T) copyToOriginal.get(copy);
	}

	private class QueryCopyingCloningContext extends NormalizationCloningContext {
		@Override
		public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
			GenericEntity e = instanceToBeCloned;
			if (getTraversingStack().size() == 1)
				return e;

			if (evaluationExcludes.contains(e) || !needsToBeCloned(e))
				registerAsVisited(e, e);

			return e;
		}

		@Override
		public void registerAsVisited(GenericEntity entity, Object associate) {
			super.registerAsVisited(entity, associate);
			copyToOriginal.put((GenericEntity) associate, entity);
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			if (getTraversingStack().size() == 1)
				return NormalizedSelectQuery.T.create();
			else
				return entityType.createPlain();
		}
	}

	private static <T extends GenericEntity> T copy(T entity, CloningContext cc) {
		EntityType<?> et = entity.entityType();
		return (T) et.clone(cc, entity, StrategyOnCriterionMatch.reference);
	}

	public NormalizedSelectQuery normalize() {
		indexExplicitSources();
		normalizeSelections();
		normalizeConditions();
		normalizeGroupBy();
		normalizeHaving();
		normalizeOrdering();

		return query;
	}

	// ###########################################
	// ## . . . Indexing Explicit Sources . . . ##
	// ###########################################

	private void indexExplicitSources() {
		for (From from : query.getFroms())
			index(from);
	}

	private void index(From from) {
		SourceWrapper wrapper = new SourceWrapper(null, from);
		sourceWrappers.put(from, wrapper);
		indexJoins(from, wrapper);
	}

	private void indexJoins(Source source, SourceWrapper wrapper) {
		Set<Join> joins = source.getJoins();
		if (joins == null)
			return;

		for (Join join : source.getJoins()) {
			SourceWrapper joinWrapper = new SourceWrapper(wrapper, join);
			wrapper.registerJoin(joinWrapper, join.getProperty());
			indexJoins(join, joinWrapper);
		}
	}

	// ###########################################
	// ## . . . Indexing Explicit Sources . . . ##
	// ###########################################

	private void normalizeSelections() {
		query.setSelections(normalizeSelectionOperands());
	}

	private List<Object> normalizeSelectionOperands() {
		List<Object> selections = query.getSelections();

		if (CollectionTools.isEmpty(selections))
			return fillImplicitSelections(newList(), query.getFroms());
		else
			return normalizeOperands(selections, true);
	}

	private List<Object> fillImplicitSelections(List<Object> result, Collection<? extends Source> sources) {
		if (CollectionTools.isEmpty(sources))
			return result;

		for (Source source : sources) {
			result.add(source);
			fillImplicitSelections(result, source.getJoins());
		}

		return result;
	}

	private void normalizeConditions() {
		Restriction restriction = query.getRestriction();
		if (restriction == null)
			return;

		Condition condition = restriction.getCondition();
		if (condition == null)
			return;

		restriction.setCondition(normalizeCondition(condition));
	}

	private void normalizeHaving() {
		Condition having = query.getHaving();
		if (having != null)
			query.setHaving(normalizeCondition(having));
	}

	private Condition normalizeCondition(Condition condition) {
		return copy(condition, new ConditionCloningContext());
	}

	private void normalizeOrdering() {
		if (!forceEntityJoins)
			return;

		Ordering ordering = query.getOrdering();
		if (ordering == null)
			return;

		else if (ordering instanceof SimpleOrdering)
			normalizeOrdering((SimpleOrdering) ordering);

		else if (ordering instanceof CascadedOrdering)
			normalizeOrdering((CascadedOrdering) ordering);
	}

	private void normalizeOrdering(CascadedOrdering ordering) {
		for (SimpleOrdering so : ordering.getOrderings())
			normalizeOrdering(so);
	}

	private void normalizeOrdering(SimpleOrdering ordering) {
		ordering.setOrderBy(normalizeOperand(ordering.getOrderBy(), true, false));
	}

	private void normalizeGroupBy() {
		GroupBy groupBy = query.getGroupBy();
		if (groupBy == null)
			return;

		groupBy.setOperands(normalizeOperands(groupBy.getOperands(), true));
	}

	@SuppressWarnings("deprecation")
	private Object normalizeOperand(Object operand, boolean handleTerminalCollection, boolean usePartitionPlaceholder) {
		if (!isOperand(operand))
			return operand;

		if (operand instanceof PropertyOperand)
			return normalizePropertyOperand((PropertyOperand) operand, handleTerminalCollection, usePartitionPlaceholder);

		if (operand instanceof JoinFunction)
			return operand;

		if (operand instanceof com.braintribe.model.query.functions.Count) {
			com.braintribe.model.query.functions.Count count = ((com.braintribe.model.query.functions.Count) operand);
			count.setPropertyOperand((PropertyOperand) normalizePropertyOperand(count.getPropertyOperand(), true, false));
			return count;
		}

		if (operand instanceof AggregateFunction) {
			// This must be a SELECT clause, because we cannot have AggregateFunction in WHERE clause
			AggregateFunction af = (AggregateFunction) operand;
			af.setOperand(normalizeOperand(af.getOperand(), handleTerminalCollection, usePartitionPlaceholder));
			return af;
		}

		if (operand instanceof QueryFunction)
			return normalizeQueryFunction((QueryFunction) operand, handleTerminalCollection);

		if (operand instanceof Source)
			return operand;

		throw new RuntimeException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
	}

	private Object normalizePropertyOperand(PropertyOperand operand, boolean handleTerminalCollection, boolean usePartitionPlaceholder) {
		String propertyPath = operand.getPropertyName();
		if (propertyPath == null)
			return operand;

		SourceWrapper wrapper = sourceWrappers.get(operand.getSource());

		String[] properties = propertyPath.split("\\.");

		int counter = 0;
		for (String property : properties) {
			boolean isLast = ++counter == properties.length;

			GenericModelType propertyType = wrapper.getPropertyType(property);
			boolean isCollection = propertyType.isCollection();

			if (!isLast) {
				if (isCollection)
					throw new RuntimeQueryEvaluationException("Illegal attempt to dereference collection (" + buildPropertyChain(operand.getSource())
							+ "." + buildPropertyChain(properties, counter) + ".[" + properties[counter] + "])");

				wrapper = wrapper.acquireJoin(property, true);

			} else if ((isCollection && (handleTerminalCollection || forceCollectionJoins)) || (propertyType.isEntity() && forceEntityJoins)) {

				wrapper = wrapper.getJoin(property, isCollection, true);

				// TODO is there any reason not to simply return wrapper.source?
				return wrapper.normalizedPropertyOperand(null);
			}
		}

		String lastProperty = properties[counter - 1];

		if (defaultPartition != null && GenericEntity.partition.equals(lastProperty) && !mapped(wrapper.entityType.getTypeSignature(), lastProperty))
			// here we need a default partition based on the type
			return usePartitionPlaceholder ? PlaceholderPartition.INSTANCE : defaultPartition;

		return wrapper.normalizedPropertyOperand(lastProperty);
	}

	private boolean mapped(String typeSignature, String property) {
		return mappedPropertyIndicator.test(typeSignature, property);
	}

	private QueryFunction normalizeQueryFunction(QueryFunction function, boolean handleTerminalCollection) {
		Set<Operand> operands = QueryFunctionAnalyzer.findOperands(function);
		Map<Object, Object> operandsMap = normalizeOperands(operands, handleTerminalCollection);

		return cloneFunction(function, operandsMap);
	}

	private QueryFunction cloneFunction(QueryFunction function, Map<Object, Object> operandsMap) {
		return copy(function, new FunctionCloningContext(operandsMap));
	}

	// ###########################################
	// ## . . . . . Cloning Contexts . . . . . .##
	// ###########################################

	private static class NormalizationCloningContext extends StandardCloningContext {
		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			return instanceToBeCloned;
		}

		@Override
		public boolean isTraversionContextMatching() {
			Stack<Object> os = getObjectStack();
			if (os.size() <= 1) {
				return false;
			}

			Object o = os.peek();
			if (o == null) {
				// see comment at the bottom of this method
				return false;
			}

			if (o instanceof Collection) {
				Collection<?> c = (Collection<?>) o;
				if (c.isEmpty())
					return false;

				o = CollectionTools2.first(c);
			}

			if (o instanceof GenericEntity)
				return !needsToBeCloned((GenericEntity) o);

			/* We do not need to clone simple values or enums, but if we said "true" here, they would also be removed from any collection, including
			 * constants from "selections" or "in" operand. */
			return false;
		}

		protected boolean needsToBeCloned(GenericEntity e) {
			return e instanceof Restriction || e instanceof Condition || e instanceof Operand || e instanceof Source || e instanceof Ordering
					|| e instanceof GroupBy;
		}
	}

	private class ConditionCloningContext extends NormalizationCloningContext implements PropertyTransferCompetence {
		public ConditionCloningContext() {
			this.registerAsVisited(PlaceholderPartition.INSTANCE, PlaceholderPartition.INSTANCE);
		}

		@Override
		public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
			if (isOperand(instanceToBeCloned))
				return (GenericEntity) normalizeOperand(instanceToBeCloned, false, true);

			validateIfCollectionComparison(instanceToBeCloned);

			return super.preProcessInstanceToBeCloned(instanceToBeCloned);
		}

		private void validateIfCollectionComparison(GenericEntity ge) {
			if (!isValueComparison(ge))
				return;

			ValueComparison vc = (ValueComparison) ge;

			switch (vc.getOperator()) {
				case in:
					if (!checkOperandIsCollection(vc.getOperator(), vc.getRightOperand()))
						vc.setRightOperand(emptySet());
					return;
				case contains:
					if (!checkOperandIsCollection(vc.getOperator(), vc.getLeftOperand()))
						vc.setLeftOperand(emptySet());
					return;
				default:
					return;
			}
		}

		private boolean checkOperandIsCollection(Operator operator, Object operand) {
			if (operand == null)
				return false;

			if (isOperand(operand)) {
				GenericModelType type;
				if (operand instanceof PropertyOperand)
					type = SourceTypeResolver.resolvePropertyType((PropertyOperand) operand, false);

				else if (operand instanceof Source)
					type = SourceTypeResolver.resolveType((Source) operand);

				else if (operand instanceof AggregateFunction || operand instanceof JoinFunction)
					type = null;

				else
					return true;

				if (!(type.isCollection()))
					throw notCollectionException(operator, operand);

			} else if (!isStaticCollection(operand)) {
				throw notCollectionException(operator, operand);
			}

			return true;
		}

		private RuntimeQueryEvaluationException notCollectionException(Operator operator, Object operand) {
			return new RuntimeQueryEvaluationException("Cannot evaluate " + operator + " operator." + //
					" Operand is not a collection: " + operand + "[" + operand.getClass().getName() + "]");
		}

		private boolean isStaticCollection(Object o) {
			return o instanceof Collection || o instanceof Map;
		}

		@Override
		public void transferProperty(EntityType<?> sourceEntityType, GenericEntity sourceEntity, GenericEntity targetEntity, Property property,
				Object propertyValue) throws GenericModelException {

			if (propertyValue == PlaceholderPartition.INSTANCE)
				propertyValue = defaultPartition;

			property.setDirectUnsafe(targetEntity, propertyValue);
		}

	}

	private static class FunctionCloningContext extends NormalizationCloningContext {
		private final Map<Object, Object> operandsMap;

		public FunctionCloningContext(Map<Object, Object> operandsMap) {
			this.operandsMap = operandsMap;
		}

		@Override
		public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
			// that Map would never map an entity to a non-entity, so the cast is safe
			GenericEntity normalOperand = (GenericEntity) operandsMap.get(instanceToBeCloned);

			if (normalOperand != null)
				return normalOperand;
			else
				return super.preProcessInstanceToBeCloned(instanceToBeCloned);
		}
	}

	// ###########################################
	// ## . . . . . . Helper Methods . . . . . .##
	// ###########################################

	private Map<Object, Object> normalizeOperands(Collection<?> operands, boolean handleTerminalCollection) {
		Map<Object, Object> result = newMap();

		for (Object operand : operands)
			result.put(operand, normalizeOperand(operand, handleTerminalCollection, false));

		return result;
	}

	private List<Object> normalizeOperands(List<Object> operands, boolean handleTerminalCollection) {
		List<Object> result = newList();

		for (Object o : operands)
			result.add(normalizeOperand(o, handleTerminalCollection, false));

		return result;
	}

	private boolean isOperand(Object o) {
		return o instanceof Operand && !evaluationExcludes.contains(o);
	}

	private boolean isValueComparison(Object o) {
		return o instanceof ValueComparison && !evaluationExcludes.contains(o);
	}

	// ###########################################
	// ## . . . . . . Source Wrapper . . . . . .##
	// ###########################################

	private class SourceWrapper {
		final SourceWrapper superSource;
		final Source source;
		final GenericModelType type;
		final EntityType<?> entityType;
		final Map<String, SourceWrapper> joins = newMap();
		final Map<String, PropertyOperand> propertyOperands = newMap();

		public SourceWrapper(SourceWrapper superSource, Source source) {
			this.superSource = superSource;
			this.source = source;
			this.type = resolveType();
			this.entityType = type.isEntity() ? (EntityType<?>) type : null;
		}

		private GenericModelType resolveType() {
			if (superSource == null)
				return typeReflection.getEntityType(((From) source).getEntityTypeSignature());

			GenericModelType propertyType = superSource.getPropertyType(((Join) source).getProperty());

			if (propertyType.isEntity())
				return propertyType;

			if (propertyType.isCollection())
				return ((CollectionType) propertyType).getCollectionElementType();

			throw new RuntimeException("Unexpected join-property type. Entity or collection expecetd. Type: " + propertyType);
		}

		GenericModelType getPropertyType(String property) {
			return entityType.getProperty(property).getType();
		}

		SourceWrapper getJoin(String property, boolean forceNewJoin, boolean isLeftJoin) {
			if (forceNewJoin)
				return addNewJoin(property, "" + sourceWrappers.size(), isLeftJoin);
			else
				return acquireJoin(property, isLeftJoin);
		}

		/** Guarantees that for multiple invocations with the same property the same instance is returned (to simplify further query analysis). */
		PropertyOperand normalizedPropertyOperand(String property) {
			return propertyOperands.computeIfAbsent(property, p -> newPropertyOperand(p));
		}

		private PropertyOperand newPropertyOperand(String property) {
			PropertyOperand result = PropertyOperand.T.create();
			result.setSource(source);
			result.setPropertyName(property);
			return result;
		}

		SourceWrapper acquireJoin(String property, boolean isLeftJoin) {
			SourceWrapper result = joins.get(property);

			return result != null ? result : addNewJoin(property, "", isLeftJoin);
		}

		private SourceWrapper addNewJoin(String property, String marker, boolean isLeftJoin) {
			Join newJoin = newJoin(property, isLeftJoin);
			SourceWrapper result = new SourceWrapper(this, newJoin);

			joins.put(property + marker, result);
			sourceWrappers.put(result.source, result);

			return result;
		}

		public void registerJoin(SourceWrapper joinWrapper, String property) {
			joins.put(property, joinWrapper);
			sourceWrappers.put(joinWrapper.source, joinWrapper);
		}

		private Join newJoin(String property, boolean isLeftJoin) {
			Join result = Join.T.create();
			result.setSource(source);
			result.setProperty(property);
			result.setJoinType(isLeftJoin ? JoinType.left : JoinType.inner);

			Set<Join> joins = source.getJoins();
			if (joins == null) {
				joins = newSet();
				source.setJoins(joins);
			}
			joins.add(result);

			return result;
		}
	}
}
