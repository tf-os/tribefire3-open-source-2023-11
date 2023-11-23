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
package com.braintribe.model.processing.query.fluent;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.CriterionBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

public class SelectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {
	private Source firstSource;

	public SelectQueryBuilder() {
		super(SelectQuery.T.create());
	}
	
	public SelectQueryBuilder from(Class<? extends GenericEntity> clazz, String alias) {
		return from(clazz.getName(), alias);
	}
	
	public SelectQueryBuilder from(EntityType<?> type, String alias) {
		return from(type.getTypeSignature(), alias);
	}
	
	public SelectQueryBuilder from(String typeSignature, String alias) {
		From from =  From.T.create();
		from.setEntityTypeSignature(typeSignature);
		from.setName(alias);
		return from(from, alias);
	}
	
	public SelectQueryBuilder from(From from, String alias) {
		List<From> froms = query.getFroms();
		if (froms == null)
			query.setFroms(froms = newList());

		froms.add(from);
		
		registerSource(alias, from);
		return this;
	}
	
	public SelectQueryBuilder join(String sourceAlias, String propertyName, String alias) {
		return this.join(sourceAlias, propertyName, alias, JoinType.inner);
	}
	
	public SelectQueryBuilder leftJoin(String sourceAlias, String propertyName, String alias) {
		return this.join(sourceAlias, propertyName, alias, JoinType.left);
	}
	
	public SelectQueryBuilder rightJoin(String sourceAlias, String propertyName, String alias) {
		return this.join(sourceAlias, propertyName, alias, JoinType.right);
	}
	
	public SelectQueryBuilder fullJoin(String sourceAlias, String propertyName, String alias) {
		return this.join(sourceAlias, propertyName, alias, JoinType.full);
	}
	
	public SelectQueryBuilder join(String sourceAlias, String propertyName, String alias, JoinType joinType) {
		Source source = acquireActualSourceForJoin(sourceAlias);

		Join join = Join.T.create();
		join.setSource(source);
		join.setProperty(propertyName);
		join.setJoinType(joinType);

		Set<Join> joins = source.getJoins();
		if (joins == null)
			source.setJoins(joins = newSet());

		joins.add(join);
		registerSource(alias, join);
		join.setName(alias);
		return this;
	}

	private Source acquireActualSourceForJoin(String sourceAlias) {
		Source source = acquireSource(sourceAlias);

		EarlySource earlySource = earlySources.get(source);
		if (earlySource != null)
			source = earlySource.source;

		if (source == null)
			throw new IllegalStateException("Unable to resolve source: " + sourceAlias);

		return source;
	}

	public SelectQueryBuilder select(String sourceAlias, String property) {
		PropertyOperand propertyOperand = PropertyOperand.T.create();
		propertyOperand.setSource(acquireSource(sourceAlias));
		propertyOperand.setPropertyName(property);
		return select(propertyOperand);
	}
	
	public SelectQueryBuilder select(Object expression) {
		List<Object> selections = query.getSelections();
		if (selections == null)
			query.setSelections(selections = newList());

		selections.add(expression);
		return this;
	}
	
	public OperandBuilder<SelectQueryBuilder> select() {
		return new OperandBuilder<>(this, this, this::select);
	}
	
	public SelectQueryBuilder select(String sourceAlias) {
		return select(sourceAlias, null);
	}
	
	@Override
	public ConditionBuilder<SelectQueryBuilder> where() {
		return new ConditionBuilder<>(this, this, aquireRestriction()::setCondition);
	}

	public ConditionBuilder<SelectQueryBuilder> having() {
		return new ConditionBuilder<>(this, this, query::setHaving);
	}

	@Override
	public SelectQueryBuilder limit(int limit) {
		return (SelectQueryBuilder) super.limit(limit);
	}
	
	@Override
	public SelectQueryBuilder paging(int pageSize, int pageStart) {
		return (SelectQueryBuilder) super.paging(pageSize, pageStart);
	}
	
	/**
	 * @deprecated in case of a {@link SelectQuery} one must also specify an alias, not just property name! Please use
	 *             {@link #orderBy()} followed by specifying the operand, like
	 *             {@code orderBy().property("alias", "propertyName")}.
	 */
	@Override
	@Deprecated
	public SelectQueryBuilder orderBy(String propertyName) {
		return (SelectQueryBuilder) super.orderBy(propertyName);
	}
	
	/**
	 * @deprecated in case of a {@link SelectQuery} one must also specify an alias, not just property name! Please use
	 *             {@link #orderBy(OrderingDirection)}, followed by specifying the operand, like
	 *             {@code orderBy().property("alias", "propertyName")}.
	 */
	@Override
	@Deprecated
	public SelectQueryBuilder orderBy(String propertyName, OrderingDirection orderingDirection) {
		return (SelectQueryBuilder) super.orderBy(propertyName, orderingDirection);
	}

	@Override
	public SelectQueryBuilder orderingDirection(OrderingDirection orderingDirection) {
		return (SelectQueryBuilder) super.orderingDirection(orderingDirection);
	}

	@Override
	public CascadedOrderingBuilder<SelectQueryBuilder> orderByCascade() {
		return (CascadedOrderingBuilder<SelectQueryBuilder>) super.orderByCascade();
	}

	public SelectQueryBuilder groupBy(String alias) {
		return groupBy().entity(alias);
	}

	public SelectQueryBuilder groupBy(String alias, String propertyName) {
		return groupBy().property(alias, propertyName);
	}

	public IOperandBuilder<SelectQueryBuilder> groupBy() {
		return new OperandBuilder<>(this, this, aquireGroupBy().getOperands()::add);
	}

	protected GroupBy aquireGroupBy() {
		GroupBy groupBy = query.getGroupBy();
		if (groupBy == null) {
			query.setGroupBy(groupBy = GroupBy.T.create());
			groupBy.setOperands(newList());
		}
			
		return groupBy;
	}
	
	@Override
	public SelectQueryBuilder tc(TraversingCriterion traversingCriterion) {
		query.setTraversingCriterion(traversingCriterion);
		return this;
	}

	@Override
	public CriterionBuilder<SelectQueryBuilder> tc() {
		return (CriterionBuilder<SelectQueryBuilder>) super.tc();
	}

	@Override
	protected void registerSource(String alias, Source source) {
		super.registerSource(alias, source);
		if (firstSource == null)
			firstSource = source;
	}

	@Override
	public Source getFirstSource() {
		return firstSource;
	}
}
