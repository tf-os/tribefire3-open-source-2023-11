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
package com.braintribe.model.access.hibernate.hql;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.query.Query;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.tools.SelectQueryNormalizer;
import com.braintribe.model.processing.query.tools.traverse.ConditionTraverser;
import com.braintribe.model.processing.query.tools.traverse.OperandVisitor;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * 
 * <h3>Localize</h3>
 * 
 * The {@link Localize} query function has a little convoluted implementation. In GM, we do a simple usage, liked this:
 * 
 * <pre>
 * select localize(e.localizedString, 'en') from SomeEntity e
 * </pre>
 * 
 * This is not so straight forward, because there is a forward logic, we take the value form the corresponding
 * {@link LocalizedString#getLocalizedValues() localized values} map for key 'en', but if no such entry exists, we fall back to key 'default' -
 * {@link LocalizedString#LOCALE_DEFAULT}.
 * 
 * Normally, one would try something like:
 * 
 * <pre>
 * select ls.localizedValues['en'] from SomeEntity e left join e.localizedString ls
 * </pre>
 * 
 * in Hibernate, but this has two problems - accessing an indexed value is only allowed in a condition and accessing the value for key 'en' is
 * actually an inner join, thus we can no longer do a fallback. Even this would not work as it returns no result if no entry for locale 'en' is
 * present:
 * 
 * <pre>
 * select case when 'en' in indices(ls.localizedValues) then ls.localizedValues['en'] else ls.localizedValues['default'] end from SomeEntity e left join e.localizedString ls
 * </pre>
 * 
 * Therefore, we have to go with a more complicated construction with an extra created join with additional conditions, like this:
 * 
 * <pre>
 * select LV from SomeEntity e left join e.localizedString ls left join ls.localizedValues LV where 
 * 		LV is null OR ('en' in indices(LV) AND key(LV) = 'en') OR (NOT ('en' in indices(LV)) AND key(LV) = 'default')
 * </pre>
 * 
 * See {@link #collectLocalizeInConditions()} See {@link #encodeConditionForSelectedLocalizedString(Join, String)}
 * 
 * @author peter.gazdik
 */
public class SelectHqlBuilder extends HqlBuilder<SelectQuery> {

	private int selectionPosition;

	public final List<Integer> entitySignaturePositions = newList();

	private final Map<Localize, Join> localizeToInducedJoin = newMap();

	public SelectHqlBuilder(SelectQuery query) {
		super(query);
	}

	@Override
	public Query<?> encode() {
		buildHql();

		return finishQuery();
	}

	// So we can test the builder
	/* package */ final void buildHql() {
		normalizeQuery();

		collectMappedJoins();

		collectLocalizeInConditions();

		encodeSelections();

		encodeFroms();

		encodeCondition();

		encodeConditionForSelectedLocalizedStringsIfNeeded();

		encodeGroupBy();

		encodeHaving();

		encodeOrdering();
	}

	private void normalizeQuery() {
		query = new SelectQueryNormalizer(query, false, true) //
				.defaultPartition(defaultPartition) //
				.mappedPropertyIndicator(this::isPropertyMapped) //
				.normalize();
	}

	private boolean isPropertyMapped(String typeSignature, String propertyName) {
		Property property = typeReflection.getEntityType(typeSignature).getProperty(propertyName);
		return mappedPropertyIndicator.test(property);
	}

	/** Analyze which joins are mapped so we know which selections to replace with <code>null</code>. */
	private void collectMappedJoins() {
		collectMappedSources(query.getFroms());
	}

	private void collectMappedSources(Collection<? extends Source> sources) {
		if (sources == null)
			return;

		for (Source source : sources)
			if (mapped(source)) {
				context.mappedSources.add(source);
				collectMappedSources(source.getJoins());
			}
	}

	/**
	 * Because {@link Localize} function requires an extra join, we need to find all occurrences in the condition first, to create the joins before we
	 * start encoding the conditions to HQL.
	 */
	private void collectLocalizeInConditions() {
		ConditionTraverser.traverse(o -> false, null, new LocalizeVisitor(), condition());
	}

	private class LocalizeVisitor implements OperandVisitor {
		@Override
		public void visit(Localize localize) {
			onLocalizeEncountered(localize);
		}
	}

	@Override
	protected void encodeLocalize(Localize localize) {
		String alias = onLocalizeEncountered(localize);
		builder.append(alias);
		return;
	}

	/** Notes this localize to add a join and extra conditions to make it work and returns the alias to be used as a value. */
	private String onLocalizeEncountered(Localize localize) {
		Join join = localizeToInducedJoin.computeIfAbsent(localize, this::newLocalizeEncountered);
		return context.aquireAlias(join);
	}

	private Join newLocalizeEncountered(Localize localize) {
		Object o = localize.getLocalizedStringOperand();
		if (o instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) o;
			if (po.getPropertyName() != null)
				throw unsupportedLocalizeOperandException(o);
			o = po.getSource();
		}

		if (!(o instanceof Source))
			throw unsupportedLocalizeOperandException(o);

		Source s = (Source) o;
		Join join = Join.T.create();
		join.setSource(s);
		join.setProperty(LocalizedString.localizedValues);
		join.setJoinType(JoinType.left);

		return join;
	}

	private RuntimeException unsupportedLocalizeOperandException(Object o) {
		return new IllegalArgumentException("Localize function is only supported on a from or join operands, not: " + o);
	}

	private void encodeSelections() {
		try {
			context.setSelectClause(true);
			List<?> selections = query.getSelections();

			if (selections == null || selections.isEmpty()) {
				if (query.getDistinct()) {
					List<Source> sources = new ArrayList<>();
					collectSources(query.getFroms(), sources);
					selections = sources;
				}
			}

			if (selections != null && !selections.isEmpty()) {
				selectionPosition = 0;
				for (Object select : selections) {

					if (selectionPosition == 0) {
						builder.append("select ");
						builder.append(query.getDistinct() ? "distinct " : "");
					} else
						builder.append(',');

					encodeSelectionOperand(select);
					selectionPosition++;
				}
				builder.append(' ');
			}
		} finally {
			context.setSelectClause(false);
		}
	}

	private void collectSources(Collection<? extends Source> sources, List<Source> result) {
		for (Source source : sources) {
			result.add(source);
			collectSources(source.getJoins(), result);
		}
	}

	/**
	 * Special encoding for operators in the SELECT clause - due to special handling being needed for collection properties.
	 */
	private void encodeSelectionOperand(Object object) {
		if (object instanceof PropertyOperand) {
			GenericModelType propertyType = context.getPropertyType((PropertyOperand) object, false);
			if (propertyType instanceof CollectionType) {
				encodeFunctionWrappedOperand("elements", object, false, true);
				return;
			}
		}

		encodeOperand(object, false, true);
	}

	protected void encodeFroms() {
		builder.append("from ");
		List<From> froms = query.getFroms();
		if (froms != null) {
			int i = 0;
			for (From from : froms) {
				if (!context.mappedSources.contains(from))
					continue;

				if (i++ > 0)
					builder.append(',');

				encodeFrom(from);
			}

			for (From from : froms) {
				if (!context.mappedSources.contains(from))
					continue;

				builder.append(' ');
				encodeJoins(from);
			}

			for (Join join : localizeToInducedJoin.values())
				encodeJoin(join);
		}
	}

	private void encodeJoins(Source source) {
		Set<Join> joins = source.getJoins();

		for (Join join : nullSafe(joins)) {
			if (!context.mappedSources.contains(join))
				continue;

			encodeJoin(join);
			encodeJoins(join);
		}
	}

	private void encodeJoin(Join join) {
		String alias = context.aquireAlias(join);
		encodeJoin(join, alias);
	}

	@Override
	protected void markEntitySignatureSelection() {
		entitySignaturePositions.add(selectionPosition);
	}

	// I guess we assume all the "froms" are mapped.
	private boolean mapped(Source source) {
		return source instanceof From || mapped(context.getQualifiedProperty((Join) source).val1());
	}

	private void encodeConditionForSelectedLocalizedStringsIfNeeded() {
		if (localizeToInducedJoin.isEmpty())
			return;

		Condition condition = condition();

		boolean firstCondition = condition == null;
		for (Entry<Localize, Join> e : localizeToInducedJoin.entrySet()) {
			if (firstCondition)
				builder.append(" where ");
			else
				builder.append(" and ");

			firstCondition = false;

			encodeConditionForSelectedLocalizedString(e.getValue(), e.getKey().getLocale());
		}
	}

	/** Adds a condition for a condition for a join specifically introduced for the {@link Localize} function. */
	private void encodeConditionForSelectedLocalizedString(Join localizeJoin, String locale) {
		if (locale == null)
			locale = context.getLocale();

		String alias = context.aquireAlias(localizeJoin);
		String quotedLocale = "'" + escapeStringLiteralBody(locale) + "'";
		String localeExists = quotedLocale + "in indices(" + alias + ")";
		String andKey = " and key(" + alias + ")";

		// Example for the condition:
		// LV1 is null OR ('de' in indices(LV1) AND key(LV1) = 'de') OR (NOT ('de' in indices(LV1)) AND key(LV1) = 'default')
		builder.append('(');
		builder.append(alias);
		builder.append(" is null or (");
		builder.append(localeExists);
		builder.append(andKey);
		builder.append(" = ");
		builder.append(quotedLocale);
		builder.append(") or (not(");
		builder.append(localeExists);
		builder.append(")");
		builder.append(andKey);
		builder.append(" = 'default')");
		builder.append(')');
	}

	private void encodeGroupBy() {
		List<Object> groupBys = resolveGroupBys();

		boolean first = true;
		for (Object groupBy : groupBys) {
			if (first) {
				builder.append(" GROUP BY ");
				first = false;
			} else {
				builder.append(',');
			}

			encodeGroupByOperand(groupBy);
		}
	}

	private List<Object> resolveGroupBys() {
		GroupBy groupBy = query.getGroupBy();

		if (groupBy != null && groupBy.getOperands() != null)
			return groupBy.getOperands();

		if (query.getSelections() == null)
			return Collections.emptyList();

		// if there are aggregate functions, we should consider everything else as needed for group-by
		List<Object> nonAggregatedSelections = newList();

		boolean aggregationUsed = false;
		for (Object selection : query.getSelections()) {
			if (selection instanceof AggregateFunction)
				aggregationUsed = true;
			else
				nonAggregatedSelections.add(selection);
		}

		return aggregationUsed ? nonAggregatedSelections : Collections.emptyList();
	}

	private void encodeGroupByOperand(Object groupBy) {
		if (groupBy instanceof PropertyOperand)
			encodeGroupByPropertyOperand((PropertyOperand) groupBy);
		else if (groupBy instanceof Source)
			encodeGroupBySource((Source) groupBy);
		else
			encodeGroupByStandard(groupBy);
	}

	private void encodeGroupByPropertyOperand(PropertyOperand po) {
		if (po.getPropertyName() == null)
			encodeGroupBySource(po.getSource());
		else
			encodeGroupByRealPropertyOperand(po);
	}

	private void encodeGroupByRealPropertyOperand(PropertyOperand po) {
		GenericModelType propertyType = context.getPropertyType(po);

		if (propertyType instanceof EntityType)
			throw new IllegalArgumentException("Seems you are attempting a group-by with a property which is an entity (or collection of entities)."
					+ " That is not supported, please do an explicit join for this source. Referenced entity type:"
					+ propertyType.getTypeSignature());

		encodeGroupByStandard(po);
	}

	private void encodeGroupBySource(Source source) {
		GenericModelType sourceType = context.getSourceType(source);

		if (!(sourceType instanceof EntityType)) {
			encodeGroupByStandard(source);
			return;
		}

		PropertyOperand po = PropertyOperand.T.create();
		po.setSource(source);

		boolean first = true;
		for (Property p : ((EntityType<?>) sourceType).getProperties()) {
			if (p.getType() instanceof CollectionType)
				continue;

			if (first)
				first = false;
			else
				builder.append(',');

			po.setPropertyName(p.getName());
			encodeOperand(po, false, true);
		}
	}

	private void encodeGroupByStandard(Object groupBy) {
		encodeOperand(groupBy, true, true);
	}

	private void encodeHaving() {
		Condition having = query.getHaving();
		if (having != null) {
			builder.append(" HAVING ");
			encodeCondition(having);
		}
	}

}
