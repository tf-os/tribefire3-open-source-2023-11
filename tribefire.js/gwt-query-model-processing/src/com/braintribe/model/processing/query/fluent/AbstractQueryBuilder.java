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
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.CriterionBuilder;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;

public abstract class AbstractQueryBuilder<T extends Query> implements SourceRegistry {
	protected T query = null;
	private final Map<String, Source> sources = newMap();
	protected final Map<Join, EarlySource> earlySources = newMap();
	
	
	protected AbstractQueryBuilder(T query) {
		this.query = query;
	}
	
	protected void registerSource(String alias, Source source) {
		Source existingSource = sources.get(alias);
		if (existingSource == null) {
			sources.put(alias, source);
			return;
		}

		EarlySource earlySource = earlySources.get(existingSource);
		if (earlySource == null || earlySource.source != null) 
			throw new QueryBuilderException("the alias " + alias + " is already defined.");
		
		source.setName(alias);
		earlySource.source = source;
	}		
	
	public ConditionBuilder<? extends AbstractQueryBuilder<T>> where() {
		return new ConditionBuilder<>(this, this, aquireRestriction()::setCondition);
	}
	
	protected Restriction aquireRestriction() {
		Restriction restriction = query.getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			query.setRestriction(restriction);
		}
		return restriction;
	}
	
	protected Paging aquirePaging() {
		Restriction restriction = aquireRestriction();
		Paging paging = restriction.getPaging();
		
		if (paging == null) {
			paging = Paging.T.create();
			paging.setPageSize(20);
			restriction.setPaging(paging);
		}
		return paging;
	}
	
	protected SimpleOrdering aquireSimpleOrdering() {
		Ordering ordering = query.getOrdering();
		if (ordering == null) {
			SimpleOrdering simpleOrdering = SimpleOrdering.T.create();
			simpleOrdering.setDirection(OrderingDirection.ascending);
			query.setOrdering(simpleOrdering);
			ordering = simpleOrdering;
		}
		else if (!(ordering instanceof SimpleOrdering)) {
			throw new QueryBuilderException("you cannot have both a SimpleOrdering and a CascadingOrdering");
		}
			
		return (SimpleOrdering)ordering;
	}
	
	protected CascadedOrdering aquireCascadedOrdering() {
		Ordering ordering = query.getOrdering();
		if (ordering == null) {
			CascadedOrdering cascadedOrdering = CascadedOrdering.T.create();
			cascadedOrdering.setOrderings(newList());
			query.setOrdering(cascadedOrdering);
			return cascadedOrdering;
		}
		
		if (!(ordering instanceof CascadedOrdering))
			throw new QueryBuilderException("you cannot have both a SimpleOrdering and a CascadingOrdering");
		
		return (CascadedOrdering)ordering;
	}
	
	public AbstractQueryBuilder<T> limit(int limit) {
		aquirePaging().setPageSize(limit);
		return this;
	}
	
	public AbstractQueryBuilder<T> paging(int pageSize, int pageStart) {
		Paging paging = aquirePaging();
		paging.setPageSize(pageSize);
		paging.setStartIndex(pageStart);
		return this;
	}

	public OperandBuilder<? extends AbstractQueryBuilder<T>> orderBy() {
		return new OperandBuilder<>(this, this, aquireSimpleOrdering()::setOrderBy);
	}
	
	public OperandBuilder<? extends AbstractQueryBuilder<T>> orderBy(OrderingDirection orderingDirection) {
		orderingDirection(orderingDirection);
		return orderBy();
	}
	
	public CascadedOrderingBuilder<? extends AbstractQueryBuilder<T>> orderByCascade() {
		return new CascadedOrderingBuilder<>(this, this, aquireCascadedOrdering()::setOrderings);
	}
	
	public AbstractQueryBuilder<T> distinct() {
		return distinct(true);
	}

	public AbstractQueryBuilder<T> distinct(boolean distinct) {
		query.setDistinct(distinct);
		return this;
	}

	public AbstractQueryBuilder<T> orderBy(String propertyName) {
		return orderBy().property(propertyName);
	}
	
	public AbstractQueryBuilder<T> orderBy(String propertyName, OrderingDirection orderingDirection) {
		return orderBy(orderingDirection).property(null, propertyName);
	}
	
	public AbstractQueryBuilder<T> orderingDirection(OrderingDirection orderingDirection) {
		aquireSimpleOrdering().setDirection(orderingDirection);
		return this;
	}
	
	public AbstractQueryBuilder<T> tc(TraversingCriterion traversingCriterion) {
		query.setTraversingCriterion(traversingCriterion);
		return this;
	}
	
	public CriterionBuilder<? extends AbstractQueryBuilder<T>> tc() {
		return new CriterionBuilder<>(this, this::tc);
	}
	
	public T done() {
		if (earlySources.isEmpty())
			return query;
		else
			return (T) Query.T.clone(new DelegateSourceResolvingCc(), query, StrategyOnCriterionMatch.skip);
	}
	
	private final class DelegateSourceResolvingCc extends StandardCloningContext {
		@SuppressWarnings("unusable-by-js")
		@Override
		public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
			EarlySource earlySource = earlySources.get(instanceToBeCloned);
			if (earlySource == null)
				return instanceToBeCloned;

			if (earlySource.source == null)
				throw new IllegalStateException("Unable to resolve source: " + earlySource.alias);

			return earlySource.source;
		}
	}

	@Override
	public Source acquireSource(String alias) {
		if (alias == null)
			return null;
		
		Source source = sources.get(alias);
		if (source == null) {
			source = newEarlySource(alias);
			source.setName(alias);
			sources.put(alias, source);
		}
			
		return source;
	}
	
	
	@Override
	public Join acquireJoin(String alias) {
		Source source = sources.get(alias);
		if (source == null) {
			source = newEarlySource(alias);
			source.setName(alias);
			sources.put(alias, source);
		}

		return (Join) source;
	}
	
	private Join newEarlySource(String alias) {
		Join fakeFrom = Join.T.create();
		
		EarlySource earlySource = new EarlySource();
		earlySource.alias = alias;
		
		earlySources.put(fakeFrom, earlySource);

		return fakeFrom;
	}

	public static class EarlySource {
		public Source source;
		public String alias;
	}
	
	public static Object adaptValue(Object value) {
		if (value instanceof Set<?>) {
			Set<Object> adaptedSet = newSet();
			for (Object object : (Set<?>) value)
				adaptedSet.add(adaptValue(object));

			return adaptedSet;
		}

		if (value instanceof ValueDescriptor)
			return value;

		if (value instanceof GenericEntity)
			return ((GenericEntity) value).reference();

		if (value instanceof Enum)
			return EnumReference.of((Enum<?>) value);

		return value;
	}
	
	// Query getter than enables the decorator pattern on extending query builders
	public T getQuery() {
		return query;
	}

}
