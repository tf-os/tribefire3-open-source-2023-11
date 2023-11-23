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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.ReflectionTools.ensureValidJavaBeansName;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.braintribe.common.lcd.Tuple.Tuple2;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;
import com.braintribe.model.query.functions.value.ValueFunction;

/* This offers some lenience when it comes to  */
public abstract class HqlBuilder<Q extends com.braintribe.model.query.Query> {

	public static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public Q query;
	protected HqlBuilderContext context = new HqlBuilderContext();
	protected StringBuilder builder = new StringBuilder();

	protected Session session;
	protected String defaultPartition;
	protected Predicate<EntityType<?>> mappedEntityIndicator = signature -> true;
	protected Predicate<Property> mappedPropertyIndicator = property -> true;
	protected BiFunction<String, Object, Object> idAdjuster = (signature, id) -> id;

	private boolean adaptPagingForHasMore = false;

	public HqlBuilder(Q query) {
		this.query = query;
	}

	protected Condition condition() {
		Restriction restriction = query.getRestriction();
		return restriction == null ? null : restriction.getCondition();
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setDefaultPartition(String defaultPartition) {
		this.defaultPartition = defaultPartition;
	}

	public void setAdaptPagingForHasMore(boolean adaptPagingForHasMore) {
		this.adaptPagingForHasMore = adaptPagingForHasMore;
	}

	public void setMappedEntityIndicator(Predicate<EntityType<?>> mappedEntityIndicator) {
		this.mappedEntityIndicator = mappedEntityIndicator;
	}

	public void setMappedPropertyIndicator(Predicate<Property> mappedPropertyIndicator) {
		this.mappedPropertyIndicator = mappedPropertyIndicator;
	}

	/** For entities with composite id this creates the CompositeIdValues out of a String. */
	public void setIdAdjuster(BiFunction<String, Object, Object> idAdjuster) {
		this.idAdjuster = idAdjuster;
	}

	public abstract Query<?> encode();

	protected void encodeFrom(From from) {
		String alias = context.aquireAlias(from);
		encodeFrom(from, alias);
	}

	protected void encodeFrom(From from, String alias) {
		builder.append(from.getEntityTypeSignature());
		builder.append(" as ");
		builder.append(alias);
	}

	protected void encodeJoin(Join join, String alias) {
		String sourceAlias = context.aquireAlias(join.getSource());
		JoinType jt = join.getJoinType();
		if (jt == null)
			jt = JoinType.inner;

		switch (jt) {
			case full:
				builder.append(" outer join ");
				break;
			case left:
				builder.append(" left join ");
				break;
			case right:
				builder.append(" right join ");
				break;
			case inner:
			default:
				builder.append(" join ");
				break;
		}

		builder.append(sourceAlias);
		builder.append('.');
		builder.append(join.getProperty());
		builder.append(" as ");
		builder.append(alias);
	}

	protected <R> Query<R> finishQuery() {
		HibernateQueryBuilder<R> hqb = new HibernateQueryBuilder<>(session, builder.toString());

		applyPaging(hqb);

		setQueryParameters(hqb);

		return hqb.getResult();
	}

	protected final void encodeOrdering() {
		// build order by clause if needed
		Ordering ordering = query.getOrdering();

		if (ordering instanceof SimpleOrdering)
			encodeSimpleOrdering((SimpleOrdering) ordering);

		else if (ordering instanceof CascadedOrdering)
			encodeCascadedOrdering((CascadedOrdering) ordering);
	}

	private void encodeSimpleOrdering(SimpleOrdering ordering) {
		builder.append(" order by ");
		encodeOrdering(ordering);
	}

	private void encodeCascadedOrdering(CascadedOrdering cascadedOrdering) {
		List<SimpleOrdering> orderings = cascadedOrdering.getOrderings();
		if (isEmpty(orderings))
			return;

		builder.append(" order by ");
		for (int i = 0; i < orderings.size(); i++) {
			if (i > 0)
				builder.append(',');

			encodeOrdering(orderings.get(i));
		}
	}

	private void encodeOrdering(SimpleOrdering ordering) {
		OrderingDirection dir = ordering.getDirection();
		Object orderBy = ordering.getOrderBy();

		if (orderBy != null)
			encodeOperand(orderBy, true, true);
		else
			throw new IllegalArgumentException("SimpleOrdering.orderBy must not be null");

		if (dir == null)
			dir = OrderingDirection.ascending;

		builder.append(' ');
		builder.append(dirString(dir));
	}

	private Object dirString(OrderingDirection dir) {
		switch (dir) {
			case ascending:
				return "asc";
			case descending:
				return "desc";
			default:
				throw new UnsupportedEnumException(dir);
		}
	}

	private void applyPaging(HibernateQueryBuilder<?> hqBuilder) {
		Restriction restriction = query.getRestriction();
		if (restriction == null)
			return;

		Paging paging = restriction.getPaging();
		if (paging == null)
			return;

		int pageSize = paging.getPageSize();
		if (pageSize > 0 && adaptPagingForHasMore)
			pageSize++;

		int startIndex = paging.getStartIndex();

		hqBuilder.setPagination(pageSize, startIndex);
	}

	private void setQueryParameters(HibernateQueryBuilder<?> hqBuilder) {
		int i = 0;
		for (Object value : context.getValues()) {
			String name = "p" + i++;
			hqBuilder.setParameter(name, value);
		}
	}

	/** This is stupid, because the {@link Restriction} entity which binds together condition and pagination is incredibly stupid. */
	protected void encodeCondition() {
		Condition condition = condition();
		if (condition != null) {
			builder.append(" where ");
			encodeCondition(condition);
		}
	}

	protected void encodeCondition(Condition condition) {
		if (condition instanceof Conjunction)
			encodeJunction((AbstractJunction) condition, "and", "1=1");

		else if (condition instanceof Disjunction)
			encodeDisjunction((Disjunction) condition);

		else if (condition instanceof Negation)
			encodeNegation((Negation) condition);

		else if (condition instanceof ValueComparison)
			encodeValueComparison((ValueComparison) condition);

		else if (condition instanceof FulltextComparison)
			encodeFulltextComparision((FulltextComparison) condition);

		else if (condition instanceof Intersects)
			encodeIntersects((Intersects) condition);

		else
			builder.append("1=1");
	}

	private void encodeDisjunction(Disjunction d) {
		DisjunctedInOptimizer.optimize(query, d);

		encodeJunction(d, "or", "1=0");
	}

	private void encodeFulltextComparision(FulltextComparison fulltextComparison) {
		if (isAllFulltextExpression(fulltextComparison.getText()))
			builder.append(" 1=1 ");
		else {
			// Fulltext not supported
			builder.append(" 1=0 ");
		}
	}

	private static boolean isAllFulltextExpression(Object text) {
		if (text == null || text.toString().trim().length() == 0) {
			return true;
		}
		return isWildarcdExpression(text);
	}

	private static boolean isWildarcdExpression(Object text) {
		return text.equals("*") || text.equals("%");
	}

	private void encodeNegation(Negation negation) {
		builder.append("not ");
		encodeCondition(negation.getOperand());
	}

	private void encodeJunction(AbstractJunction junction, String andOrOr, String emptyLiteral) {
		List<Condition> operands = junction.getOperands();

		if (isEmpty(operands)) {
			builder.append(emptyLiteral);
			return;
		}

		if (operands.size() == 1) {
			encodeCondition(first(operands));
			return;
		}

		builder.append('(');
		int i = 0;

		for (Condition condition : junction.getOperands()) {
			if (i++ > 0) {
				builder.append(' ');
				builder.append(andOrOr);
				builder.append(' ');
			}
			encodeCondition(condition);
		}
		builder.append(')');
	}

	private void encodeValueComparison(ValueComparison propertyComparision) {
		Object leftOperand = propertyComparision.getLeftOperand();
		Object rightOperand = propertyComparision.getRightOperand();
		Operator operator = propertyComparision.getOperator();

		switch (operator) {
			case like:
				encodeOperand(leftOperand, true, false);
				builder.append(' ');
				encodeOperator(builder, operator);
				builder.append(' ');
				encodeOperand(encodeHqlLikePattern(rightOperand), true, false);
				builder.append(" escape '!'");
				break;
			case ilike:
				encodeFunctionWrappedOperand("lower", leftOperand, true, false);
				builder.append(' ');
				encodeOperator(builder, operator);
				builder.append(' ');
				encodeFunctionWrappedOperand("lower", encodeHqlLikePattern(rightOperand), true, false);
				builder.append(" escape '!'");
				break;
			case contains:
				checkIsCollection(context, leftOperand, operator);
				encodeInCondition(rightOperand, leftOperand);
				break;
			case in:
				checkIsCollection(context, rightOperand, operator);
				encodeInCondition(leftOperand, rightOperand);
				break;
			default:
				encodeComparison(leftOperand, operator, rightOperand);
		}
	}

	private void encodeInCondition(Object elementOperand, Object collectionOperand) {
		boolean isConstantCollection = collectionOperand instanceof Collection<?>;

		encodeOperand(elementOperand, isConstantCollection, false);
		builder.append(" in ");

		if (isConstantCollection) {
			String typeSignatureIfId = resolveTypeSignatureIfId(elementOperand);
			if (typeSignatureIfId == null)
				encodeOperand(collectionOperand, false, false);
			else
				encodeOperand(adjustIds(typeSignatureIfId, (Collection<?>) collectionOperand), false, false);
		} else {
			encodeFunctionWrappedOperand("elements", collectionOperand, false, false);
		}
	}

	private void checkIsCollection(HqlBuilderContext context, Object operand, Operator operator) {
		if (!isCollection(context, operand)) {
			throw new IllegalArgumentException("Illegal argument for the '" + operator + "' operator. Collection expected!");
		}
	}

	private boolean isCollection(HqlBuilderContext context, Object operand) {
		if (operand instanceof PropertyOperand) {
			GenericModelType propertyType = context.getPropertyType((PropertyOperand) operand, false);
			return propertyType instanceof CollectionType;
		}

		return operand instanceof Collection;
	}

	private static Object encodeHqlLikePattern(Object rightOperand) {
		StringBuilder builder = new StringBuilder();
		String s = rightOperand.toString();

		int escapeLock = -1;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '_':
					builder.append("!_");
					break;
				case '%':
					builder.append("!%");
					break;
				case '*':
					if (escapeLock == i) {
						builder.append(c);
					} else {
						builder.append('%');
					}
					break;
				case '?':
					if (escapeLock == i) {
						builder.append(c);
					} else {
						builder.append('_');
					}
					break;
				case '\\':
					if (escapeLock == i) {
						builder.append("\\");
					} else {
						escapeLock = i + 1;
					}
					break;
				default:
					builder.append(c);
			}
		}
		return builder.toString();
	}

	private void encodeComparison(Object leftOperand, Operator operator, Object rightOperand) {
		encodeComparisonOperand(leftOperand, rightOperand);
		builder.append(' ');
		encodeOperator(builder, operator);
		builder.append(' ');
		encodeComparisonOperand(rightOperand, leftOperand);
	}

	/**
	 * Encodes the operand, including doing a possible conversion(s) to {@link CompositeIdValues} if the other operand is an <code>id</code> property.
	 */
	private void encodeComparisonOperand(Object operand, Object otherOperand) {
		String typeSignatureIfId = resolveTypeSignatureIfId(otherOperand);
		if (typeSignatureIfId == null)
			encodeOperand(operand, shouldCompareEntitiesById(otherOperand), false);
		else if (operand instanceof String)
			encodeOperand(idAdjuster.apply(typeSignatureIfId, operand), otherOperand != null, false);
		else if (operand instanceof Collection)
			encodeOperand(adjustIds(typeSignatureIfId, (Collection<?>) operand), otherOperand != null, false);
		else
			encodeOperand(operand, otherOperand != null, false); // not common, could we even do something like x.id = y.id?
	}

	private boolean shouldCompareEntitiesById(Object otherOperand) {
		if (otherOperand == null)
			return false;

		// if other operand is MapKey, Hibernate does not support comparisons by id it seems.
		// .. I.e. if otherOperand is MapKey (encoded as index()), our operand (keyEntity) should be encoded as entity, not just id:
		// select e from Entity e join e.myMap map where index(map) = :keyEntity
		if (otherOperand instanceof MapKey)
			return false;

		return true;
	}

	private Set<?> adjustIds(String typeSignatureIfId, Collection<?> ids) {
		return ids.stream() //
				.map(id -> idAdjuster.apply(typeSignatureIfId, id)) //
				.collect(Collectors.toSet());
	}

	private String resolveTypeSignatureIfId(Object operand) {
		if (!(operand instanceof PropertyOperand))
			return null;

		PropertyOperand po = (PropertyOperand) operand;
		String propertyName = po.getPropertyName();
		if (propertyName == null)
			return null;

		if (!propertyName.equals(GenericEntity.id))
			return null;

		return context.getSourceType(po.getSource()).getTypeSignature();
	}

	private void encodeOperator(StringBuilder builder, Operator operator) {
		switch (operator) {
			case equal:
				builder.append("=");
				break;
			case notEqual:
				builder.append("!=");
				break;
			case like:
			case ilike:
				builder.append("like");
				break;
			case greater:
				builder.append(">");
				break;
			case greaterOrEqual:
				builder.append(">=");
				break;
			case less:
				builder.append("<");
				break;
			case lessOrEqual:
				builder.append("<=");
				break;
			case in:
				builder.append("in");
				break;
			default:
				throw new IllegalArgumentException("unsupported operator type " + operator);
		}
	}

	private Object adaptValue(Object object) {
		if (object instanceof PersistentEntityReference) {
			PersistentEntityReference entityReference = (PersistentEntityReference) object;
			return entityReference.getRefId();

		} else if (object instanceof EnumReference) {
			EnumReference enumReference = (EnumReference) object;
			return enumReference.constant();

		} else if (object instanceof Set) {
			Set<?> set = (Set<?>) object;
			return adaptCollection(set);

		} else
			return object;
	}

	private Collection<?> adaptCollection(Collection<?> collection) {
		Collection<Object> adaptedCollection = newCollectionWithSameTypeAs(collection);

		for (Object value : collection)
			adaptedCollection.add(adaptValue(value));

		return adaptedCollection;
	}

	private Collection<Object> newCollectionWithSameTypeAs(Collection<?> collection) {
		if (collection instanceof Set)
			return newSet();

		if (collection instanceof List)
			return newList();

		throw new IllegalArgumentException("Unsupported collection type " + collection.getClass() + "! (please use Set or List)");
	}

	protected void encodeFunctionWrappedOperand(String function, Object object, boolean compareEntitiesById, boolean inlineSimple) {
		builder.append(function);
		builder.append('(');
		encodeOperand(object, compareEntitiesById, inlineSimple);
		builder.append(')');
	}

	protected void encodeOperand(Object object, boolean compareEntitiesById, boolean inlineSimple) {
		if (object == null)
			encodeNull(inlineSimple);

		else if (object instanceof PropertyOperand)
			encodePropertyOperand((PropertyOperand) object, compareEntitiesById);

		else if (object instanceof QueryFunction)
			encodeQueryFunction((QueryFunction) object, compareEntitiesById, inlineSimple);

		else if (object instanceof PersistentEntityReference)
			encodePersistentRef((PersistentEntityReference) object, compareEntitiesById);

		else if (object instanceof EnumReference)
			encodeSimpleOperand(((EnumReference) object).constant(), inlineSimple);

		else if (object instanceof Collection)
			encodeCollectionOperand((Collection<?>) object, true);

		else if (object instanceof Source)
			encodeSourceOperand((Source) object, compareEntitiesById);

		else if (object instanceof GenericEntity)
			encodeGenericEntity((GenericEntity) object, compareEntitiesById);

		else
			encodeSimpleOperand(object, inlineSimple);
	}

	private void encodeNull(boolean inlineSimple) {
		if (inlineSimple)
			builder.append("cast(null as char)");
		else
			builder.append("null");
	}

	/**
	 * @param inlineSimple
	 *            determines whether we use a variable for a constant value (e.g. in WHERE clause), or we use the value directly, which we do in the
	 *            SELECT clause
	 */
	private void encodeSimpleOperand(Object value, boolean inlineSimple) {
		if (inlineSimple) {
			if (value instanceof String)
				value = prepareInlineString((String) value);

			builder.append(value);

		} else {
			List<Object> values = context.getValues();
			String name = ":p" + values.size();
			builder.append(name);
			values.add(value);
		}
	}

	private String prepareInlineString(String s) {
		return "'" + s.replaceAll("'", "''") + "'";
	}

	private void encodeCollectionOperand(Collection<? extends Object> collection, boolean adapt) {
		List<Object> values = context.getValues();
		String name = ":p" + values.size();
		builder.append('(');
		builder.append(name);
		builder.append(')');
		if (adapt) {
			Collection<?> adaptedCollection = adaptCollection(collection);
			values.add(adaptedCollection);
		} else {
			values.add(collection);
		}
	}

	private void encodeGenericEntity(GenericEntity entity, boolean compareEntitiesById) {
		Object id = entity.getId();
		if (id == null)
			throw new IllegalArgumentException("Unsupported preliminary entity : " + entity);

		encodeReferenceValue(entity.entityType().getTypeSignature(), id, compareEntitiesById);
	}

	private void encodePersistentRef(PersistentEntityReference ref, boolean compareEntitiesById) {
		encodeReferenceValue(ref.getTypeSignature(), ref.getRefId(), compareEntitiesById);
	}

	private void encodeReferenceValue(String typeSignature, Object id, boolean compareEntitiesById) {
		id = idAdjuster.apply(typeSignature, id);

		if (compareEntitiesById)
			encodeSimpleOperand(id, false);
		else
			encodeSimpleOperand(getEntity(typeSignature, id), false);
	}

	private GenericEntity getEntity(String typeSignature, Object id) {
		return session.get(typeReflection.getEntityType(typeSignature).getJavaType(), (Serializable) id);
	}

	@SuppressWarnings("deprecation")
	private void encodeQueryFunction(QueryFunction queryFunction, boolean compareEntitiesById, boolean inlineSimple) {
		if (queryFunction instanceof JoinFunction) {
			JoinFunction joinFunction = (JoinFunction) queryFunction;
			builder.append("index(");
			builder.append(context.aquireAlias(joinFunction.getJoin()));
			builder.append(')');

		} else if (queryFunction instanceof Localize) {
			encodeLocalize((Localize) queryFunction);

		} else if (queryFunction instanceof AggregateFunction) {
			encodeAggregateFunction((AggregateFunction) queryFunction, compareEntitiesById, inlineSimple);

		} else if (queryFunction instanceof EntitySignature) {
			EntitySignature signatureFunction = (EntitySignature) queryFunction;
			Object operand = signatureFunction.getOperand();

			EntityType<?> operandType = context.getOperandEntityType(operand);

			if (operandType != null) {
				if (!isAnyMapped(operandType.getSubTypes())) {
					builder.append("'");
					builder.append(operandType.getTypeSignature());
					builder.append("'");

				} else {
					/* This means we are doing a query on a source that is not mapped, e.g.: select entitySignature(ge) from GenericEntity ge */
					/* In this case putting "type(ge)" in HQL might fail if there is a sub-type that is not mapped as a hierarchy, and thus has no
					 * "class" (discriminator) column in the db. We therefore select the entity instead (i.e. select ge from GenericEntity ge) and
					 * mark our position, so the query post-processing replaces it with typeSignature. */
					boolean selectingUnmappedSource = context.isSelectClause() && !isHibernateEntity(operandType);

					if (!selectingUnmappedSource)
						builder.append("cast(type(");

					/* PGA: I think operand can only be null here if we have an entity query and we are referring the default source. */
					if (operand == null)
						encodeSourceOperand(null, false);
					else
						encodeOperand(operand, false, inlineSimple);

					if (selectingUnmappedSource)
						markEntitySignatureSelection();
					else
						builder.append(") as string)");
				}
			}

		} else if (queryFunction instanceof ValueFunction) {
			encodeValueFunction((ValueFunction) queryFunction, compareEntitiesById, inlineSimple);

		} else {
			throw new IllegalArgumentException("unsupported query function " + queryFunction.getClass().getName());
		}
	}

	protected void markEntitySignatureSelection() {
		// Only reachable in SelectQuery case, obviously
	}

	protected abstract void encodeLocalize(Localize localize);

	/** 'abc' is a string literal in HQL and just abc is it's body. We have to escape it in case it contains single quotes. */
	protected final String escapeStringLiteralBody(String s) {
		return s.replace("'", "''");
	}

	/**
	 * Checks whether at least one of these types is mapped by hibernate. When determining how to resolve the {@link EntitySignature} function, we
	 * distinguish if the class is mapped by itself, or if a hierarchy is mapped. If it is a hierarchy, we resolve by asking for discriminator,
	 * otherwise we take the typeSignature directly.
	 * 
	 * NOTE REGARDING USAGE: We assume that every model is mapped entirely (i.e. all it's entities are mapped), so the only way how none of the
	 * sub-types is mapped is that they all come from different model, which extends this one. Therefore, we do not need to also check the sub-types
	 * of sub-types and so on, but simply if none of the sub-types is mapped, this entity has no sub-types in our model, so we can take the signature
	 * directly.
	 */
	private boolean isAnyMapped(Set<EntityType<?>> types) {
		if (isEmpty(types))
			return false;

		for (EntityType<?> et : types)
			if (isHibernateEntity(et))
				return true;

		return false;
	}

	private boolean isHibernateEntity(EntityType<?> et) {
		return mappedEntityIndicator.test(et);
	}

	private void encodeValueFunction(ValueFunction valueFunction, boolean compareEntitiesById, boolean inlineSimple) {
		builder.append(getFunctionHqlName(valueFunction));
		builder.append("(");

		if (valueFunction instanceof Concatenation) {
			boolean first = true;
			for (Object operand : ((Concatenation) valueFunction).getOperands()) {
				if (first)
					first = false;
				else
					builder.append(',');

				builder.append("cast(");
				encodeOperand(operand, compareEntitiesById, inlineSimple);
				builder.append(" as string)");
			}
		} else if (valueFunction instanceof Lower) {
			Object operand = ((Lower) valueFunction).getOperand();
			encodeOperand(operand, compareEntitiesById, inlineSimple);

		} else if (valueFunction instanceof Upper) {
			Object operand = ((Upper) valueFunction).getOperand();
			encodeOperand(operand, compareEntitiesById, inlineSimple);

		} else if (valueFunction instanceof AsString) {
			Object operand = ((AsString) valueFunction).getOperand();
			encodeOperand(operand, compareEntitiesById, inlineSimple);
			builder.append(" as string");
		}

		builder.append(")");
	}

	private void encodeAggregateFunction(AggregateFunction aggregateFunction, boolean compareEntitiesById, boolean inlineSimple) {
		builder.append(getFunctionHqlName(aggregateFunction));
		builder.append("(");

		if (aggregateFunction instanceof com.braintribe.model.query.functions.aggregate.Count) {
			com.braintribe.model.query.functions.aggregate.Count count = (com.braintribe.model.query.functions.aggregate.Count) aggregateFunction;
			if (count.getDistinct()) {
				builder.append("distinct ");
			}
		}

		encodeOperand(aggregateFunction.getOperand(), compareEntitiesById, inlineSimple);
		builder.append(")");
	}

	private Object getFunctionHqlName(AggregateFunction aggregateFunction) {
		if (aggregateFunction instanceof com.braintribe.model.query.functions.aggregate.Count)
			return "count";
		else if (aggregateFunction instanceof Sum)
			return "sum";
		else if (aggregateFunction instanceof Average)
			return "avg";
		else if (aggregateFunction instanceof Min)
			return "min";
		else if (aggregateFunction instanceof Max)
			return "max";
		else
			throw new IllegalArgumentException("Unsupported aggregate function: " + aggregateFunction.getClass().getName());
	}

	private Object getFunctionHqlName(ValueFunction valueFunction) {
		if (valueFunction instanceof Concatenation)
			return "concat";
		else if (valueFunction instanceof Lower)
			return "lower";
		else if (valueFunction instanceof Upper)
			return "upper";
		else if (valueFunction instanceof AsString)
			return "cast";
		else
			throw new IllegalArgumentException("Unsupported value function: " + valueFunction.getClass().getName());
	}

	private void encodePropertyOperand(PropertyOperand propertyOperand, boolean compareEntitiesByIds) {
		boolean mapped = mapped(propertyOperand);

		if (!mapped) {
			if (defaultPartition != null && GenericEntity.partition.equals(propertyOperand.getPropertyName()))
				builder.append("'" + defaultPartition + "'");
			else
				encodeNull(true);
			return;
		}

		GenericModelType operandType = context.getPropertyType(propertyOperand);

		Source source = propertyOperand.getSource();
		String alias = context.aquireAlias(source);

		builder.append(alias);

		String propertyName = propertyOperand.getPropertyName();

		if (propertyName != null && propertyName.length() > 0) {
			builder.append('.');
			builder.append(ensureValidJavaBeansName(propertyOperand.getPropertyName()));
		}

		appendDotIdIfRelevant(operandType, compareEntitiesByIds);
	}

	private void encodeSourceOperand(Source source, boolean compareEntitiesByIds) {
		GenericModelType sourceType = context.getSourceType(source);

		String alias = context.aquireAlias(source);

		builder.append(alias);

		appendDotIdIfRelevant(sourceType, compareEntitiesByIds);
	}

	private void appendDotIdIfRelevant(GenericModelType operandType, boolean compareEntitiesByIds) {
		if (!context.isSelectClause() && compareEntitiesByIds && operandType instanceof EntityType<?>)
			builder.append(".id");
	}

	private boolean mapped(PropertyOperand propertyOperand) {
		Source source = propertyOperand.getSource();
		if (source != null && !context.mappedSources.contains(source))
			return false;

		Tuple2<EntityType<?>, Property> qualifiedProperty = context.getQualifiedProperty(propertyOperand);
		if (qualifiedProperty == null)
			// Shitty code, but I guess it means the operand doesn't reference a property but rather an entity, and thus it is mapped
			return true;

		return mapped(qualifiedProperty.val1());
	}

	/** @see DisjunctedInOptimizer */
	private void encodeIntersects(Intersects intersects) {
		PropertyOperand po = intersects.getPropertyOperand();
		Set<Object> values = intersects.getValues();

		Source source = po.getSource();
		String propertyName = po.getPropertyName();
		EntityType<?> sourceType = (EntityType<?>) context.getSourceType(source);

		String alias = context.aquireAlias(source);
		String innerAlias = "__" + context.aquireAlias(source);
		String propAlias = "__" + propertyName;

		// exists (select ee from Entity ee join ee.collection c where ee.id = e.id and (c in :values))
		builder.append("exists (");
		builder.append("select ");
		builder.append(innerAlias);
		builder.append(" from ");
		builder.append(sourceType.getTypeSignature());
		builder.append(" ");
		builder.append(innerAlias);
		builder.append(" join ");
		builder.append(innerAlias + "." + propertyName + " " + propAlias);
		builder.append(" where ");
		builder.append(innerAlias + ".id=" + alias + ".id");
		builder.append(" and ");
		builder.append(propAlias);
		builder.append(" in ");
		encodeSimpleOperand(values, false);
		builder.append(")");
	}

	/**
	 * This is tricky. We assume if a property is mapped anywhere (e.g. CustomType.partition), that it is mapped for all the types in the hierarchy
	 * that have the property (i.e. GenericEntity.partition is mapped, and hence all the entities have partition mapped). The reason is that a query
	 * like "select ge.partition from GenericEntity ge" is perfectly valid, and so we either replace it with "select null ..." if partition isn't
	 * mapped, or leave it be if mapped. Therefore we simply collect the Property instances to see if something is mapped, we con't have to worry
	 * about owner type.
	 * 
	 * NOTE: This is wrong anyway, we can have a super Property instance because of a different initializer, and if this super-level is not mapped,
	 * queries on that level would not work. Example:
	 * 
	 * <pre>
	 * SuperEntity extends GenericEntity {
	 * 		String name;
	 * }
	 * 
	 * SubEntity extends SuperEntity {
	 * 		&#64;Initializer("Hell")
	 * 		String name;
	 * }
	 * </pre>
	 * 
	 * If SuperEntity is not mapped, then the query "select se.name from SuperEntity" would always return nulls as it would think SuperEntity.name is
	 * not mapped (see the underlying implementation).
	 */
	protected boolean mapped(Property property) {
		return mappedPropertyIndicator.test(property);
	}

}
