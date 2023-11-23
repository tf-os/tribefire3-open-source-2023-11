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
import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.common.lcd.Tuple;
import com.braintribe.common.lcd.Tuple.Tuple2;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.tools.SourceHashingComparator;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;

public class HqlBuilderContext {

	private final List<Object> values = newList();

	private final Map<Source, String> aliasMap = newMap();
	private final Map<Source, GenericModelType> sourceTypes = newMap();

	private final Map<String, Integer> counters = newMap();

	public final Set<Source> mappedSources = CodingSet.create(SourceHashingComparator.INSTANCE);

	private boolean selectClause = false;

	private EntityType<?> returnedType;

	/** for property and entity queries */
	public void setReturnedType(EntityType<?> returnedType) {
		this.returnedType = returnedType;
	}

	public EntityType<?> getReturnedType() {
		return returnedType;
	}

	public void setSelectClause(boolean selectClause) {
		this.selectClause = selectClause;
	}

	public boolean isSelectClause() {
		return selectClause;
	}

	public List<Object> getValues() {
		return values;
	}

	public Tuple2<EntityType<?>, Property> getQualifiedProperty(PropertyOperand propertyOperand) {
		String propertyName = propertyOperand.getPropertyName();
		Source source = propertyOperand.getSource();

		if (!isEmpty(propertyName)) {
			GenericModelType sourceType = getSourceType(source);
			return getQualifiedProperty((EntityType<?>) sourceType, propertyName);
		}

		if (source instanceof From || source == null)
			return null;

		return getQualifiedProperty((Join) source);
	}

	public Tuple2<EntityType<?>, Property> getQualifiedProperty(Join join) {
		GenericModelType sourceType = getSourceType(join.getSource());
		return getQualifiedProperty((EntityType<?>) sourceType, join.getProperty());
	}
	
	public GenericModelType getPropertyType(PropertyOperand propertyOperand) {
		return getPropertyType(propertyOperand, true);
	}

	public GenericModelType getPropertyType(PropertyOperand propertyOperand, boolean resolveCollectionType) {
		GenericModelType sourceType = getSourceType(propertyOperand.getSource());

		String propertyName = propertyOperand.getPropertyName();
		if (propertyName != null && propertyName.length() != 0)
			return getTypeFromPropertyPath((EntityType<?>) sourceType, propertyName, resolveCollectionType);
		else
			return sourceType;
	}

	protected GenericModelType getSourceType(Source source) {
		GenericModelType type = sourceTypes.get(source);

		if (type == null) {
			if (source == null)
				type = requireNonNull(getReturnedType(), "you cannot have null source if your are using SelectQuery");
			else if (source instanceof From)
				type = getFromType((From) source);
			else if (source instanceof Join)
				type = getJoinType((Join) source);
			else
				throw new IllegalArgumentException("unsupported source type " + source.getClass());

			sourceTypes.put(source, type);
		}

		return type;
	}

	public EntityType<?> getFromType(From from) {
		return GMF.getTypeReflection().getType(from.getEntityTypeSignature());
	}

	public GenericModelType getJoinType(Join join) {
		Source source = join.getSource();
		EntityType<?> sourceEntityType = (EntityType<?>) getSourceType(source);
		return getTypeFromPropertyPath(sourceEntityType, join.getProperty());
	}

	public GenericModelType getTypeFromPropertyPath(EntityType<?> owner, String propertyName) {
		return getTypeFromProperty(owner, propertyName, true);
	}

	public GenericModelType getTypeFromPropertyPath(EntityType<?> owner, String propertyName, boolean resolveCollectionType) {
		if (propertyName == null)
			propertyName = "";

		StringTokenizer tokenizer = new StringTokenizer(propertyName, ".");

		GenericModelType type = owner;
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			type = getTypeFromProperty((EntityType<?>) type, token, resolveCollectionType || tokenizer.hasMoreElements());
		}

		return type;
	}

	public GenericModelType getTypeFromProperty(EntityType<?> owner, String propertyName) {
		return getTypeFromPropertyPath(owner, propertyName, true);
	}

	public GenericModelType getTypeFromProperty(EntityType<?> owner, String propertyName, boolean resolveCollectionType) {
		Property property = owner.getProperty(propertyName);
		GenericModelType propertyType = property.getType();

		if (propertyType.isCollection() && resolveCollectionType) {
			GenericModelType type = ((CollectionType) propertyType).getCollectionElementType();
			return type;
		} else
			return propertyType;
	}

	private Tuple2<EntityType<?>, Property> getQualifiedProperty(EntityType<?> ownerType, String propertyPath) {
		StringTokenizer tokenizer = new StringTokenizer(propertyPath, ".");

		Property property;

		while (true) {
			property = ownerType.getProperty(tokenizer.nextToken());

			if (!tokenizer.hasMoreElements())
				return Tuple.of(ownerType, property);

			GenericModelType propertyType = property.getType();
			if (propertyType.isCollection())
				propertyType = ((CollectionType) propertyType).getCollectionElementType();
			ownerType = (EntityType<?>) propertyType;
		}
	}

	protected EntityType<?> getOperandEntityType(Object operand) {
		if (operand == null)
			return getReturnedType();

		if (operand instanceof Source) {
			return (EntityType<?>) getSourceType((Source) operand);

		} else if (operand instanceof PropertyOperand) {
			GenericModelType propertyType = getPropertyType((PropertyOperand) operand);
			if (propertyType instanceof EntityType<?>)
				return (EntityType<?>) propertyType;
		}

		return null;
	}

	public String aquireAlias(Source source) {
		String alias = aliasMap.get(source);
		if (alias == null) {
			GenericModelType type = getSourceType(source);
			String typeName = type.isEntity() ? ((EntityType<?>) type).getShortName() : type.getTypeName();

			Integer counter = counters.get(typeName);
			if (counter == null)
				counter = 0;

			alias = typeName.replace('.', '_') + counter;

			counters.put(typeName, ++counter);
			aliasMap.put(source, alias);
		}
		return alias;
	}

	public String getLocale() {
		return "en";
	}

}
