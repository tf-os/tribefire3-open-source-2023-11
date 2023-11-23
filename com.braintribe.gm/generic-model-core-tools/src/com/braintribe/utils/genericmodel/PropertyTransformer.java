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
package com.braintribe.utils.genericmodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.utils.CollectionTools;

/**
 * This utility supports transformation of certain properties of a passed entity. By specifying a
 * {@link PropertyMatcher} that identifies affected properties and a {@link ValueTransformer} that are used to transform
 * the values of affected properties, one can customize which properties should be transformed in which way.
 */
public class PropertyTransformer {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private static final EntityType<GenericEntity> entityType = typeReflection.getEntityType(GenericEntity.class);

	/**
	 * Transforms the properties of the given entity identified by the matcher with the passed {@link ValueTransformer}.
	 * This methods traverses through all referenced entities.
	 */
	public static <I, O extends I> void transformProperties(GenericEntity entity, final PropertyMatcher<I> matcher,
			final ValueTransformer<I, O> transformer) {
		entityType.traverse(entity, null, new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				EntityType<GenericEntity> type = entity.entityType();
				List<Property> properties = type.getProperties();
				if (properties != null) {
					for (Property p : properties) {
						I value = p.get(entity);
						if (matcher.matches(entity, p, value, type)) {
							O result = transformer.transform(value);
							p.set(entity, result);
						}
					}
				}
			}
		});
	}

	/**
	 * Base interface for matchers that identifies the properties that should be transformed.
	 */
	public static interface PropertyMatcher<I> {
		boolean matches(GenericEntity entity, Property property, I propertyValue, EntityType<GenericEntity> type);
	}

	/**
	 * Base interface for transformation implementations.
	 */
	public static interface ValueTransformer<I, O> {
		O transform(I input);
	}

	// ************ Standard Matchers **************

	/**
	 * A matcher that identifies the properties by comparing the name to a configured name or collection of names.
	 */
	public static class NameMatcher<T> implements PropertyMatcher<T> {

		private Set<String> propertyNames = new HashSet<>();

		public NameMatcher(String name) {
			propertyNames = CollectionTools.getSet(name);
		}

		public NameMatcher(String... names) {
			propertyNames = CollectionTools.getSet(names);
		}

		public NameMatcher(Collection<String> names) {
			propertyNames = CollectionTools.getSet(names);
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {
			return propertyNames.contains(property.getName());
		}
	}

	/**
	 * A matcher that identifies the properties by comparing the name to a configured regular expression.
	 */
	public static class NameRegexMatcher<T> implements PropertyMatcher<T> {

		private Pattern regex;

		public NameRegexMatcher(String regex) {
			this.regex = Pattern.compile(regex);
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {
			return regex.matcher(property.getName()).matches();
		}
	}

	/**
	 * A matcher that identifies the properties by comparing the type to a configured type.
	 */
	public static class TypeMatcher<T> implements PropertyMatcher<T> {

		private GenericModelType propertyType;

		public TypeMatcher(GenericModelType type) {
			this.propertyType = type;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {
			return propertyType == property.getType();
		}
	}

	/**
	 * A matcher that identifies the properties by comparing the value with a configured value. <br />
	 * Note, that this matcher also works for null values.
	 */
	public static class ValueMatcher<T> implements PropertyMatcher<T> {

		private Object value;

		public ValueMatcher(Object value) {
			this.value = value;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {
			return (value == null) ? propertyValue == null : value.equals(propertyValue);
		}
	}

	// ************ Logical Matchers **************

	/**
	 * A logical matcher that returns true if all configured operands return true.
	 */
	public static class ConjunctionMatcher<T> implements PropertyMatcher<T> {

		private List<PropertyMatcher<T>> operands;

		public ConjunctionMatcher(List<PropertyMatcher<T>> operands) {
			this.operands = operands;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {

			for (PropertyMatcher<T> operand : operands) {
				if (!operand.matches(entity, property, propertyValue, type)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * A logical matcher that returns true if at least one of the configured operands returns true.
	 */
	public static class DisjunctionMatcher<T> implements PropertyMatcher<T> {

		private List<PropertyMatcher<T>> operands;

		public DisjunctionMatcher(List<PropertyMatcher<T>> operands) {
			this.operands = operands;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {

			for (PropertyMatcher<T> operand : operands) {
				if (operand.matches(entity, property, propertyValue, type)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * A logical matcher that returns the inverse of the operand matcher.
	 */
	public static class NegationMatcher<T> implements PropertyMatcher<T> {

		private PropertyMatcher<T> operand;

		public NegationMatcher(PropertyMatcher<T> operand) {
			this.operand = operand;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, T propertyValue, EntityType<GenericEntity> type) {

			return !operand.matches(entity, property, propertyValue, type);
		}
	}

}
