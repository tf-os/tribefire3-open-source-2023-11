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

import static com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.NodeGroupType.singleAccessCombination;
import static com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper.extractConversion;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.sort;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.query.fluent.CascadedOrderingBuilder;
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.IOperandBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.OperandBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.fluent.ValueComparisonBuilder;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SimpleValueNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CorrelationJoinInfo;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.OrderAndPagination;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessCombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DqjDescriptor;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.LinkPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.tools.SmartConversionTools;
import com.braintribe.model.processing.smart.query.planner.tools.ValueBasedEntryComparator;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;

/**
 * Note the builder also acquires tuple positions for the properties.
 */
public class DelegateQueryBuilder extends AbstractTsBuilder {

	private final SingleAccessGroup group;
	private final Set<EntitySourceNode> dqjNodes;
	private final QueryPlanStructure planStructure;

	private final SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
	private final Map<SourceNode, String> aliases = newMap();

	private final List<EntitySourceNode> froms = newList();
	private final Set<SourceNode> skippedCollectionNodes = newSet();
	private final List<CorrelationJoinInfo> correlationConditions = newList(); // this must be a List!!!

	// i.e. nodes which represent Maps, where the key is an entity
	private final Set<SourceNode> entityKeyMapNodes = newSet();

	// output
	private SelectQuery query;
	private Disjunction correlationDisjunction;
	private List<OperandRestriction> correlationRestrictions;
	private final List<ScalarMapping> scalarMappings = newList();
	private int querySelectionPosition;

	public DelegateQueryBuilder(SmartQueryPlannerContext context, SingleAccessGroup group) {
		super(context);

		this.group = group;
		this.dqjNodes = getDqjNodes(group);
		this.planStructure = context.planStructure();
	}

	private Set<EntitySourceNode> getDqjNodes(SingleAccessGroup group) {
		return group.nodeGroupType() == singleAccessCombination ? ((SingleAccessCombinationGroup) group).dqjNodes : emptySet();
	}

	public DelegateQueryBuilder from(Collection<EntitySourceNode> nodes) {
		froms.addAll(nodes);
		return this;
	}

	public DelegateQueryBuilder from(EntitySourceNode node) {
		froms.add(node);
		return this;
	}

	public DelegateQueryBuilder correlationWhere(Collection<CorrelationJoinInfo> correlationInfos) {
		correlationConditions.addAll(correlationInfos);
		return this;
	}

	public void finish() {
		setDistinctIfNeeded();
		buildFroms();
		buildSelection();
		addConditions();
		buildOrderByAndPaging();

		query = queryBuilder.done();

		postProcessCorrelationConditions();
	}

	private void setDistinctIfNeeded() {
		if (context.needsDistinct())
			queryBuilder.distinct();
	}

	// ####################################
	// ## . . . . . . Sources . . . . . .##
	// ####################################

	private void buildFroms() {
		for (EntitySourceNode node : GroupSortingTools.sortEntityNodesAlphabetically(froms))
			if (!isJoin(node))
				buildFrom(node);
	}

	private void buildFrom(EntitySourceNode node) {
		queryBuilder.from(node.getDelegateGmType().getTypeSignature(), acquireAlias(node));

		buildJoins(node);
	}

	private void buildJoins(EntitySourceNode node) {
		join(node.getExplicitJoins());
		join(node.getSimpleCollectionJoins());
		join(node.getSimpleUnmappedCollectionJoins());
	}

	private void join(Collection<? extends SourceNode> joins) {
		for (SourceNode joinNode : joins)
			if (isJoinNeeded(joinNode))
				join(joinNode);
			else
				skippedCollectionNodes.add(joinNode);
	}

	/**
	 * We do not need to do the join iff our join stems from the in/contains condition only (i.e. it is not also part of
	 * the SELECT clause) and all the conditions which refer to our join are contained in this very delegate query (i.e.
	 * no conditions remain to be evaluated by the SmartAccess, therefore we do not need to retrieve the data).
	 */
	private boolean isJoinNeeded(SourceNode joinNode) {
		if (joinNode.isNodeMarkedForSelection())
			return true;

		Source source = joinNode.getSource();

		Collection<ValueComparison> allComparisons = context.getAllCollectionConditions().getAll(source);
		if (isEmpty(allComparisons))
			// this means our source is not related to collection comparison
			return true;

		Collection<ValueComparison> localComparisons = context.findCollectionConditions(group.conditions).getAll(source);
		boolean thisQueryContainsAllRelevantComparisons = localComparisons != null && localComparisons.size() == allComparisons.size();

		return !thisQueryContainsAllRelevantComparisons;
	}

	private void join(SourceNode sn) {
		queryBuilder.join(acquireAlias(sn.getJoinMaster()), sn.getExplicitJoinDelegateProperty(), acquireAlias(sn), sn.getJoinType());

		joinMapKeyIfEligible(sn);

		if (sn instanceof EntitySourceNode)
			buildJoins((EntitySourceNode) sn);
	}

	/**
	 * We add an additional "from" for every map-key which is an entity. Later, we will also add a condition which says
	 * that this value is the same as the corresponding map-key. See
	 * {@link #addEntityKeyMapJoinConditions(JunctionBuilder)}.
	 */
	private void joinMapKeyIfEligible(SourceNode sn) {
		EntitySourceNode mapKeyNode = sn.getMapKeyNode();
		if (mapKeyNode == null)
			return;

		queryBuilder.from(mapKeyNode.getDelegateGmType().getTypeSignature(), acquireAlias(mapKeyNode));
		entityKeyMapNodes.add(sn);
	}

	private boolean isJoin(EntitySourceNode node) {
		return node.getSourceNodeType().isExplicitJoin();
	}

	// ####################################
	// ## . . . . . Selections . . . . . ##
	// ####################################

	private void buildSelection() {
		// we sort all the entries based on the alias (alphabetical order)
		for (Entry<SourceNode, String> entry : ValueBasedEntryComparator.sortMapEntries(aliases))
			buildSelectionFor(entry.getKey(), entry.getValue());
	}

	private void buildSelectionFor(SourceNode sourceNode, String alias) {
		if (sourceNode instanceof EntitySourceNode)
			buildSelectionFor((EntitySourceNode) sourceNode, alias);
		else
			buildSelectionFor((SimpleValueNode) sourceNode, alias);

		buildJoinFunctionSelectionIfNeeded(sourceNode, alias);
	}

	private void buildSelectionFor(EntitySourceNode sourceNode, String alias) {
		for (String delegateP : sort(sourceNode.selectedDelegateProperties())) {
			select(sourceNode.getDelegateGmProperty(delegateP).getType()).property(alias, delegateP);
			buildPropertySelection(sourceNode, delegateP);
		}

		if (needsTypeSignature(sourceNode)) {
			queryBuilder.select().entitySignature().entity(alias);
			buildSignatureSelection(sourceNode);
		}
	}

	private void buildPropertySelection(EntitySourceNode sourceNode, String delegateP) {
		int queryIndex = newQueryIndex();

		for (ConversionWrapper cw : sourceNode.getConversionsForDelegateProperty(delegateP)) {
			int propertyPosition = sourceNode.acquirePropertyPosition(delegateP, cw);

			scalarMappings.add(newScalarMapping(queryIndex, propertyPosition, cw));
		}
	}

	private void buildSignatureSelection(EntitySourceNode sourceNode) {
		int queryIndex = newQueryIndex();
		int propertyPosition = sourceNode.acquireDelegateSignaturePosition();

		scalarMappings.add(newScalarMapping(queryIndex, propertyPosition, null));
	}

	private void buildSelectionFor(SimpleValueNode sourceNode, String alias) {
		select(sourceNode.getDelegateCollectionElementType()).entity(alias);

		int queryIndex = newQueryIndex();

		for (ConversionWrapper cw : sourceNode.getValueConversions()) {
			int simpleJoinPosition = sourceNode.acquireValuePosition(cw);

			ScalarMapping mapping = newScalarMapping(queryIndex, simpleJoinPosition, cw);
			scalarMappings.add(mapping);
		}
	}

	private void buildJoinFunctionSelectionIfNeeded(SourceNode sourceNode, String alias) {
		if (!sourceNode.shouldRetrieveJoinFunction())
			return;

		if (sourceNode.getDelegateCollectionGmType().typeKind() == GmTypeKind.LIST) {
			queryBuilder.select().listIndex(alias);

		} else {
			if (sourceNode.getMapKeyNode() != null)
				/* if my map-key is a node (i.e. it's an entity), we are already selecting the values in
				 * buildSelection() (because we take everything that has an alias, and entity map-key is handled in a
				 * special way by creating an additional join with alias) -> so we don't need to retrieve the key
				 * here */
				return;

			select(sourceNode.getDelegateMapKeyGmTypeIfEligible()).mapKey(alias);
		}

		int joinFunctionPosition = sourceNode.acquireJoinFunctionPosition();
		int queryIndex = newQueryIndex();

		ScalarMapping mapping = newScalarMapping(queryIndex, joinFunctionPosition, null);
		scalarMappings.add(mapping);
	}

	private int newQueryIndex() {
		return querySelectionPosition++;
	}

	private OperandBuilder<SelectQueryBuilder> select(GmType gmType) {
		return gmType.isGmEnum() ? queryBuilder.select().asString() : queryBuilder.select();
	}

	private ScalarMapping newScalarMapping(int queryIndex, int tupleComponentIndex, ConversionWrapper cw) {
		Value value = ValueBuilder.tupleComponent(queryIndex);
		value = SmartValueBuilder.ensureConvertedValue(value, extractConversion(cw), false);

		return SmartValueBuilder.scalarMapping(tupleComponentIndex, value);
	}

	// ####################################
	// ## . . . . . Conditions . . . . . ##
	// ####################################

	private void addConditions() {
		if (!hasConditions())
			return;

		JunctionBuilder<SelectQueryBuilder> conjunction = queryBuilder.where().conjunction();

		addConditions(conjunction, group.conditions);
		addDelegatedDqjConditions(conjunction);
		addDiscriminatorConditions(conjunction);
		addEntityKeyMapJoinConditions(conjunction);

		if (!correlationConditions.isEmpty()) {
			// adding empty disjunction to be filled by the DQJ data
			JunctionBuilder<JunctionBuilder<SelectQueryBuilder>> disjunction = conjunction.disjunction();

			for (CorrelationJoinInfo cji : correlationConditions) {
				/* The following condition is true iff our correlation property on the qNode is a collection (in the
				 * delegate), and is mapped as property of some InverseKeyPropertyAssignment (we only set
				 * cji.inverseKeyCollectionNode in case of IKPA, and if the inverseNode is the mNode, it means this
				 * collection is a SimpleUnmappedCollection of our qNode).
				 * 
				 * In such case, we have already done a join using this SimpleUnmappedCollection, and that is this
				 * joined value we are comparing - by using it's alias only. See that when building joins, we also go
				 * through node.getSimpleUnmappedCollectionJoins(). */
				if (cji.inverseKeyCollectionNode != null && cji.inverseKeyCollectionNode.getInverseNode() == cji.mNode) {
					/* So in this case, our delegate query is like:
					 * "select <...> from qNode q join q.simpleUnmappedCollection c where c = ?" */
					disjunction = disjunction.entity(acquireAlias(cji.inverseKeyCollectionNode)).eq(null);

				} else if (cji.collectionNode == cji.mNode) {
					/* Example: if on Smart level we have: "from SmartPerson p join p.companies c" and our mNode is the
					 * node for c. This means, our qNode (Person) must have something like "p.companyNames" which are
					 * used for correlation with c.name. In that case, we don't want to have correlation condition like
					 * "where p.companyName = ?", but we have created a join: "join p.companyNames cs" and now do a
					 * condition: "where cs = ?". This "keyPropertySimpleCounterpart" is exactly that source.
					 * 
					 * See e.g. CollectionSelection_KeyProperty_PlannerTests.setQueryWithDelegatableSetCondition */
					disjunction = disjunction.entity(acquireAlias(cji.mNode.getKeyPropertySimpleCounterpart())).eq(null);

				} else {
					disjunction = disjunction.property(acquireAlias(cji.qNode), cji.qDelegateProperty).eq(null);
				}
			}

			conjunction = disjunction.close();
		}

		conjunction.close();
	}

	private boolean hasConditions() {
		return !group.conditions.isEmpty() || !group.polymorphicAssignmentNodes.isEmpty() || !correlationConditions.isEmpty()
				|| !entityKeyMapNodes.isEmpty() || !dqjNodes.isEmpty();
	}

	private <T> void addConditions(JunctionBuilder<T> cb, Collection<Condition> cs) {
		for (Condition c : cs)
			addCondition(cb, c);
	}

	private <T extends ConditionBuilder<T>> void addCondition(ConditionBuilder<T> cb, Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				JunctionBuilder<T> con = cb.conjunction();
				addConditions(con, ((Conjunction) condition).getOperands());
				con.close();
				return;

			case disjunction:
				JunctionBuilder<T> dis = cb.disjunction();
				addConditions(dis, ((Disjunction) condition).getOperands());
				dis.close();
				return;

			case fulltextComparison:
				addFulltextComparison(cb, (FulltextComparison) condition);
				return;

			case negation:
				addCondition(cb.negation(), ((Negation) condition).getOperand());
				return;

			case valueComparison:
				addComparison(cb, (ValueComparison) condition);
				return;
		}

		throw new RuntimeQueryPlannerException("Unsupported condition '" + condition + "; of type: " + condition.conditionType());
	}

	private <T extends ConditionBuilder<T>> void addFulltextComparison(ConditionBuilder<T> cb, FulltextComparison condition) {
		EntitySourceNode sourceNode = planStructure.getSourceNode(condition.getSource());
		String alias = getAlias(sourceNode);

		cb.fullText(alias, condition.getText());
	}

	private <T extends ConditionBuilder<T>> T addComparison(ConditionBuilder<T> cb, ValueComparison condition) {
		Object leftOperand = condition.getLeftOperand();
		Object rightOperand = condition.getRightOperand();
		Operator operator = condition.getOperator();

		ValueComparisonBuilder<T> vcb = addOperand(cb, leftOperand, operator, rightOperand);

		switch (condition.getOperator()) {
			case ilike:
				return vcb.ilike((String) condition.getRightOperand());
			case like:
				return vcb.like((String) condition.getRightOperand());
			default:
				break;
		}

		OperandBuilder<T> operandBuilder = operandBuilder(vcb, condition);

		return addOperand(operandBuilder, rightOperand, operator, leftOperand);
	}

	// Conditions for delegated Dqjs

	private void addDelegatedDqjConditions(JunctionBuilder<SelectQueryBuilder> conjunction) {
		for (EntitySourceNode dqjNode : dqjNodes)
			addDqjConditions(conjunction, dqjNode);
	}

	private void addDqjConditions(JunctionBuilder<SelectQueryBuilder> conjunction, EntitySourceNode dqjNode) {
		DqjDescriptor dqjDescriptor = dqjNode.getDqjDescriptor();

		EntitySourceNode ownerNode = dqjNode.getKeyPropertyJoinMaster();

		String ownerAlias = getAlias(ownerNode);
		String joinAlias = getAlias(dqjNode);

		for (String joinedEntityDelegatePropertyName : sort(dqjDescriptor.getJoinedEntityDelegatePropertyNames())) {
			String relationOwnerDelegatePropertyName = dqjDescriptor.getRelationOwnerDelegatePropertyName(joinedEntityDelegatePropertyName);

			conjunction.property(ownerAlias, relationOwnerDelegatePropertyName).eq().property(joinAlias, joinedEntityDelegatePropertyName);
		}
	}

	// Condition for Discriminator

	private void addDiscriminatorConditions(JunctionBuilder<SelectQueryBuilder> cb) {
		if (group.polymorphicAssignmentNodes.isEmpty())
			return;

		for (EntitySourceNode node : group.polymorphicAssignmentNodes)
			addDiscriminatorCondition(cb, node);
	}

	private void addDiscriminatorCondition(JunctionBuilder<SelectQueryBuilder> cb, EntitySourceNode sourceNode) {
		DiscriminatedHierarchy dh = sourceNode.getDiscriminatorHierarchy();

		if (dh.isSingleDiscriminatorProperty()) {
			/* We are building a simple condition like this: discProp in ('prod', 'test') */

			Set<Object> discValues = dh.getAllSimpleDiscriminatorValues();
			addDelegatePropertyAsOperand(cb, sourceNode, dh.getSingleDiscriminatorProperty().getName()).in(discValues);

		} else {
			/* We are building a disjunction of conjunction like this:
			 * 
			 * (discProp1 = 'prod' AND discProp2 = 'EN') OR (discProp1 = 'prod' AND discProp2 = 'DE') */

			JunctionBuilder<?> disjunctionBuilder = cb.disjunction();

			List<GmProperty> discProps = dh.getCompositeDiscriminatorProperties();
			int count = discProps.size();
			for (ListRecord record : dh.getAllCompositeDiscriminatorValues()) {
				List<Object> discValues = record.getValues();

				JunctionBuilder<?> nestedConjunctionBuilder = disjunctionBuilder.conjunction();
				for (int i = 0; i < count; i++) {
					String discPropertyName = discProps.get(i).getName();
					Object discValue = discValues.get(i);

					addDelegatePropertyAsOperand(nestedConjunctionBuilder, sourceNode, discPropertyName).eq(discValue);
				}
				nestedConjunctionBuilder.close();
			}

			disjunctionBuilder.close();
		}
	}

	// Condition for Map key

	/**
	 * Adds conditions that all "from"s created for every map-key which is an entity actually match the corresponding
	 * map-key;
	 */
	private void addEntityKeyMapJoinConditions(JunctionBuilder<SelectQueryBuilder> cb) {
		for (SourceNode sn : entityKeyMapNodes)
			cb.mapKey(getAlias(sn)).eq().entity(getAlias(sn.getMapKeyNode()));
	}

	// ####################################
	// ## . . . OrderBy and Paging . . . ##
	// ####################################

	private void buildOrderByAndPaging() {
		buildOrderBy();
		buildPagination();
	}

	private void buildOrderBy() {
		List<SimpleOrdering> orderings = findOrderings();
		List<AliasOrderingEntry> totalOrderings = computeTotalOrderingsIfNeeded(orderings);

		switch (orderings.size() + totalOrderings.size()) {
			case 0:
				return;

			case 1: {
				if (!orderings.isEmpty()) {
					SimpleOrdering ordering = first(orderings);
					addOperand(queryBuilder.orderBy(ordering.getDirection()), ordering.getOrderBy(), null, null);
				} else {
					AliasOrderingEntry ordering = first(totalOrderings);
					queryBuilder.orderBy().property(ordering.alias, ordering.property);
				}
				return;
			}

			default: {
				CascadedOrderingBuilder<SelectQueryBuilder> cb = queryBuilder.orderByCascade();

				for (SimpleOrdering ordering : orderings)
					addOperand(cb.dir(ordering.getDirection()), ordering.getOrderBy(), null, null);

				for (AliasOrderingEntry ordering : totalOrderings)
					cb.property(ordering.alias, ordering.property);

				cb.close();
			}
		}
	}

	private List<SimpleOrdering> findOrderings() {
		return group.orderAndPagination != null ? group.orderAndPagination.delegatableOrderings : emptyList();
	}

	/**
	 * In some cases (if we are using batches), we want to make sure that our query always returns the results in the
	 * exact same order (i.e. total ordering). So we add an oder-by id for every single source we are querying. However,
	 * some of those "id"s may already be used for sorting, so we first create the existingOrderingIndex which is able
	 * to tell us exactly this (it is a map where key is the alias of a node, and value is set of all properties used
	 * for sorting).
	 */
	private List<AliasOrderingEntry> computeTotalOrderingsIfNeeded(List<SimpleOrdering> alreadyUsedOrderings) {
		if (group.batchSize == null)
			return emptyList();

		Map<String, Set<String>> existingOrderingIndex = buildExistingOrderingIndex(alreadyUsedOrderings);

		List<AliasOrderingEntry> result = newList();

		for (Entry<SourceNode, String> entry : aliases.entrySet()) {
			SourceNode node = entry.getKey();
			String alias = entry.getValue();

			AliasOrderingEntry orderingEntry = newAliasOrderingEntry(alias, node, existingOrderingIndex);

			if (orderingEntry != null)
				result.add(orderingEntry);
		}

		return result;
	}

	private Map<String, Set<String>> buildExistingOrderingIndex(List<SimpleOrdering> alreadyUsedOrderings) {
		if (isEmpty(alreadyUsedOrderings))
			return emptyMap();

		Map<String, Set<String>> result = newMap();

		for (SimpleOrdering ordering : alreadyUsedOrderings) {
			Object orderBy = ordering.getOrderBy();

			if (!(orderBy instanceof PropertyOperand))
				continue;

			PropertyOperand po = (PropertyOperand) orderBy;
			SourceNode sn = planStructure.getSourceNode(po.getSource());

			acquireSet(result, acquireAlias(sn)).add(po.getPropertyName());
		}

		return result;
	}

	private AliasOrderingEntry newAliasOrderingEntry(String alias, SourceNode node, Map<String, Set<String>> existingOrderingIndex) {
		String propertyName = null;

		if (node instanceof EntitySourceNode) {
			propertyName = GenericEntity.id;

			Set<String> alreadyUsedProperties = existingOrderingIndex.get(alias);
			if (alreadyUsedProperties != null && alreadyUsedProperties.contains(propertyName))
				return null;

		} else if (!(node instanceof SimpleValueNode)) {
			throw new SmartQueryPlannerException("SourceNode of Unknow type: " + node);
		}

		return new AliasOrderingEntry(alias, propertyName);
	}

	private static class AliasOrderingEntry {
		public String alias;
		public String property;

		public AliasOrderingEntry(String alias, String property) {
			this.alias = alias;
			this.property = property;
		}
	}

	private void buildPagination() {
		OrderAndPagination orderAndPagination = group.orderAndPagination;

		if (orderAndPagination != null && orderAndPagination.isPaginationSet())
			queryBuilder.paging(orderAndPagination.limit, orderAndPagination.offset);
	}

	// ####################################
	// ## . . . . . Operands . . . . . . ##
	// ####################################

	private <T> OperandBuilder<T> operandBuilder(ValueComparisonBuilder<T> vcb, ValueComparison condition) {
		Operator operator = condition.getOperator();

		switch (operator) {
			case contains:
				if (canDelegateCollectionComparison(condition.getLeftOperand())) {
					return vcb.contains();
				} else {
					return vcb.eq();
				}
			case equal:
				return vcb.eq();
			case greater:
				return vcb.gt();
			case greaterOrEqual:
				return vcb.ge();
			case in:
				if (canDelegateCollectionComparison(condition.getRightOperand()))
					return vcb.in();
				else
					return vcb.eq();
			case less:
				return vcb.lt();
			case lessOrEqual:
				return vcb.le();
			case notEqual:
				return vcb.ne();
			default:
				throw new RuntimeQueryPlannerException("Unsupported operator: " + operator);
		}
	}

	private boolean canDelegateCollectionComparison(Object operand) {
		if (operand instanceof Collection)
			return true;

		Object sourceNode = resolveSourceNode(operand);

		/* TODO there is a bug, introduced when optimization for internal DQJ was added. This might be part of the
		 * solution. */
		/* See CollectionSelection_KeyProperty_PlannerTests.EXPECTED_TO_FAIL_setQueryWithDelegatableSetCondition() */

		// if (sourceNode instanceof EntitySourceNode) {
		// sourceNode = ((EntitySourceNode) sourceNode).getKeyPropertySimpleCounterpart();
		// }

		return skippedCollectionNodes.contains(sourceNode);
	}

	private Object resolveSourceNode(Object operand) {
		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;

			if (po.getPropertyName() != null)
				throw new SmartQueryPlannerException(
						"Property operand was not expected. Source: " + po.getSource() + ", property: " + po.getPropertyName());

			return planStructure.getSourceNode(po.getSource());
		}

		throw new SmartQueryPlannerException("This branch was not expected. Operand: " + operand);
	}

	/**
	 * @param operand
	 *            operand which will be added using our builder
	 * @param operator
	 *            the operator, in case we are encoding a condition. If we are doing say
	 *            <tt>p.convertedProperty like '*'</tt> and our converted property is String on smart access, then we
	 *            need to wrap the delegate operand into {@link AsString} function. So if our property has
	 *            string-conversion and the operator is not equality-based (see
	 *            {@link SmartConversionTools#isEqualityBasedOperator(Operator)}), we wrap the operand into
	 *            {@link AsString}. We do not do anything like this for other conversions.
	 * @param otherOperand
	 *            the other operand, in case we are encoding a condition. In case we are adding a static value which
	 *            might need conversion - this conversion is determined based on the other operand.
	 */
	private <T> T addOperand(IOperandBuilder<T> cb, Object operand, Operator operator, Object otherOperand) {
		if (!(operand instanceof Operand) || context.isEvaluationExclude(operand))
			return cb.operand(convertToDelegateIfNeeded(operand, otherOperand));

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;
			String propertyName = po.getPropertyName();

			if (propertyName == null)
				return addSourceAsOperand(cb, po.getSource());

			EntitySourceNode sourceNode = planStructure.getSourceNode(po.getSource());

			cb = applyInverseConversionIfNeeded(cb, sourceNode, propertyName, operator);

			return addSmartPropertyAsOperand(cb, sourceNode, propertyName);

		}

		if (operand instanceof JoinFunction) {
			JoinFunction jf = (JoinFunction) operand;

			SourceNode sourceNode = planStructure.getSourceNode(jf.getJoin());

			if (sourceNode.getSourceNodeType() == SourceNodeType.linkedCollectionNode)
				return addLinkEntityIndex(cb, ((EntitySourceNode) sourceNode).getJoinMaster(), jf);

			String alias = getAlias(sourceNode);

			if (jf instanceof ListIndex)
				return cb.listIndex(alias);
			else
				return cb.mapKey(alias);
		} 

		if (operand instanceof AggregateFunction)
			throw new UnsupportedOperationException("Method 'DelegateQueryBuilder.addLeftOperand' is not fully implemented yet!");

		if (operand instanceof QueryFunction)
			return addFunctionOperand(cb, (QueryFunction) operand);

		if (operand instanceof Source)
			return addSourceAsOperand(cb, (Source) operand);

		throw new SmartQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
	}

	/**
	 * See operand parameter description of {@link #addOperand(IOperandBuilder, Object, Operator, Object)}
	 */
	private <T> IOperandBuilder<T> applyInverseConversionIfNeeded(IOperandBuilder<T> cb, EntitySourceNode sourceNode, String smartPropertyName,
			Operator operator) {

		if (operator == null || SmartConversionTools.isEqualityBasedOperator(operator))
			return cb;

		SmartConversion c = sourceNode.findSmartPropertyConversion(smartPropertyName);
		if (c != null && SmartConversionTools.isDelegateableToStringConversion(c))
			return cb.asString();

		return cb;
	}

	private <T> T addLinkEntityIndex(IOperandBuilder<T> cb, EntitySourceNode linkEntityNode, JoinFunction joinFunction) {
		if (!(joinFunction instanceof ListIndex))
			throw new SmartQueryPlannerException(
					"Cannot retrieve map-key for OrderedLinkPropertyAssignment. Did you want to select list-index?");

		String alias = getAlias(linkEntityNode);
		LinkPropertyAssignmentWrapper lpaWrapper = (LinkPropertyAssignmentWrapper) linkEntityNode.getDqjDescriptor();

		return cb.property(alias, lpaWrapper.getLinkIndexPropertyName());
	}

	private <T> T addSourceAsOperand(IOperandBuilder<T> cb, Source source) {
		return addSourceNodeAsOperand(cb, planStructure.getSourceNode(source));
	}

	private <T> T addSourceNodeAsOperand(IOperandBuilder<T> cb, SourceNode sourceNode) {
		if (skippedCollectionNodes.contains(sourceNode)) {
			// node we would not skip collection nodes if the collection was not a normal property (thus ExplicitMaster)
			EntitySourceNode masterNode = sourceNode.getJoinMaster();
			String propertyName = sourceNode.getExplicitJoinDelegateProperty();

			return addDelegatePropertyAsOperand(cb, masterNode, propertyName);

		} else {
			String alias = getAlias(sourceNode);

			return cb.property(alias, null);
		}
	}

	private <T> T addSmartPropertyAsOperand(IOperandBuilder<T> cb, EntitySourceNode sourceNode, String smartProperty) {
		String delegateProperty = sourceNode.delegateForSmartProperty(smartProperty);
		return addDelegatePropertyAsOperand(cb, sourceNode, delegateProperty);
	}

	private <T> T addDelegatePropertyAsOperand(IOperandBuilder<T> cb, EntitySourceNode sourceNode, String delegateProperty) {
		String alias = getAlias(sourceNode);
		return cb.property(alias, delegateProperty);
	}

	private <T> T addFunctionOperand(IOperandBuilder<T> cb, QueryFunction operand) {
		if (operand instanceof EntitySignature) {
			EntitySignature es = (EntitySignature) operand;
			return addOperand(cb.entitySignature(), es.getOperand(), null, null);
		}

		throw new UnsupportedOperationException("Method 'DelegateQueryBuilder.addFunctionOperand' is not implemented yet!");
	}

	// ####################################
	// ## . . . . Post Processing . . . .##
	// ####################################

	private void postProcessCorrelationConditions() {
		if (correlationConditions.isEmpty())
			return;

		Restriction r = query.getRestriction();
		Conjunction mainConjunction = (Conjunction) r.getCondition();
		List<Condition> mainOperands = mainConjunction.getOperands();

		correlationDisjunction = (Disjunction) mainOperands.get(mainOperands.size() - 1);
		correlationRestrictions = extractCorrelationRestrictions();
	}

	private List<OperandRestriction> extractCorrelationRestrictions() {
		List<OperandRestriction> result = newList();

		List<Condition> disjunctionOperands = correlationDisjunction.getOperands();
		for (int i = 0; i < disjunctionOperands.size(); i++) {
			ValueComparison vc = (ValueComparison) disjunctionOperands.get(i);
			CorrelationJoinInfo cji = correlationConditions.get(i);

			int tupleIndex = getTuplePositionForCji(cji);

			Value tupleIndexValue = ValueBuilder.tupleComponent(tupleIndex);
			tupleIndexValue = SmartValueBuilder.ensureConvertedValue(tupleIndexValue, extractConversion(cji.qConversion), true);

			OperandRestriction or = OperandRestriction.T.createPlain();
			or.setQueryOperand((Operand) vc.getLeftOperand()); // is in fact a PropertyOperand
			or.setMaterializedCorrelationValue(tupleIndexValue);

			result.add(or);
		}

		return result;
	}

	// ####################################
	// ## . . . . . . Helpers . . . . . .##
	// ####################################

	private String acquireAlias(SourceNode sourceNode) {
		String alias = aliases.get(sourceNode);

		if (alias == null) {
			alias = getNiceName(sourceNode) + aliases.size();
			aliases.put(sourceNode, alias);
		}

		return alias;
	}

	private String getAlias(SourceNode sourceNode) {
		String alias = aliases.get(sourceNode);
		if (alias == null)
			throw new RuntimeQueryPlannerException("Alias not found for source: " + sourceNode.getSource());

		return alias;
	}

	private String getNiceName(SourceNode sourceNode) {
		if (sourceNode instanceof EntitySourceNode)
			return toSimpleName(((EntitySourceNode) sourceNode).getDelegateGmType().getTypeSignature());
		else
			return sourceNode.getExplicitJoinDelegateProperty() + "s";
	}

	private static String toSimpleName(String typeSignature) {
		int i = typeSignature.lastIndexOf(".");
		return i < 0 ? typeSignature : typeSignature.substring(i + 1);
	}

	// ####################################
	// ## . Final Query Data Retrievers .##
	// ####################################

	public SelectQuery getQuery() {
		return query;
	}

	public List<ScalarMapping> getScalarMappings() {
		return scalarMappings;
	}

	public Disjunction getCorrelationDisjunction() {
		return correlationDisjunction;
	}

	public List<OperandRestriction> getCorrelationRestrictions() {
		return correlationRestrictions;
	}

	/**
	 * Returns tuple position corresponding to given {@link CorrelationJoinInfo} - i.e. propertyPosition of either of
	 * the two properties which are being correlated. We pick the query side of the CJI, but it would work with the
	 * materialized one as well.
	 */
	private Integer getTuplePositionForCji(CorrelationJoinInfo cji) {
		ConversionWrapper todoConversion = null;

		if (cji.collectionNode != null)
			return cji.collectionNode.getKeyPropertySimpleCounterpart().getValuePosition(todoConversion);

		if (cji.inverseKeyCollectionNode != null)
			return cji.inverseKeyCollectionNode.getValuePosition(todoConversion);

		return cji.qNode.getSimpleDelegatePropertyPosition(cji.qDelegateProperty, cji.qConversion);
	}

}
