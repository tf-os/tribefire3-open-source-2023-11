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
package com.braintribe.model.processing.core.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public class EntityHashingComparator<E extends GenericEntity> implements HashingComparator<E> {
	
	private List<Function<? super E, Object>> hashFieldAccessors;
	private boolean isHashImmutable;

	public EntityHashingComparator(List<Function<? super E, Object>> hashFieldAccessors, boolean isHashImmutable) {
		this.hashFieldAccessors = hashFieldAccessors;
		this.isHashImmutable = isHashImmutable;
	}

	public static <T extends GenericEntity> Builder<T> build(EntityType<T> entityType) {
		return new Builder<T>(entityType);
	}
	
	@Override
	public int computeHash(E e) {
		final int prime = 31;
		int result = 1;
		
		for (Function<? super E, Object> hashFieldAccessor: hashFieldAccessors) {
			Object value = hashFieldAccessor.apply(e);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}

		return result;
	}
	
	@Override
	public boolean compare(E e1, E e2) {
		// identity
		if (e1 == e2)
			return true;
		
		if (e1 == null)
			return false;

		if (e2 == null)
			return false;
		
		for (Function<? super E, Object> hashFieldAccessor: hashFieldAccessors) {
			Object v1 = hashFieldAccessor.apply(e1);
			Object v2 = hashFieldAccessor.apply(e2);
			
			// field identity
			if (v1 == v2) {
				continue;
			}
			
			if (v1 == null)
				return false;
			
			if (v2 == null)
				return false;
			
			if (!v1.equals(v2))
				return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isHashImmutable() {
		return isHashImmutable;
	}

	public static class NullSafePropertyPathAccessor implements Function<GenericEntity, Object> {
		private Property[] properties;
		
		
		public NullSafePropertyPathAccessor(Property[] properties) {
			super();
			this.properties = properties;
		}

		@Override
		public Object apply(GenericEntity entity) {
			Object value = null;
			int len = properties.length;
			
			for (int i = 0; i < len - 1; i++) {
				Property property = properties[i];
				value = property.get(entity);
				
				if (value == null)
					return value;
				
				entity = (GenericEntity)value;
			}
			
			return properties[len - 1].get(entity);
		}
	}
	
	public static class Builder<E extends GenericEntity> {
		private EntityType<E> entityType;
		private List<Function<? super E, Object>> hashFieldAccessors = new ArrayList<>();
		private boolean mutable = false;

		public Builder(EntityType<E> entityType) {
			super();
			this.entityType = entityType;
		}

		public Builder<E> addField(Function<? super E, Object> accessor) {
			this.hashFieldAccessors.add(accessor);
			return this;
		}
		
		public Builder<E> addField(Property property) {
			this.hashFieldAccessors.add(property::get);
			return this;
		}
		
		public Builder<E> addField(String propertyName) {
			this.hashFieldAccessors.add(entityType.getProperty(propertyName)::get);
			return this;
		}
		
		public Builder<E> addPropertyPathField(String... propertyNames) {
			
			EntityType<?> curType = entityType;
			Property finalProperty = null;
			
			int len = propertyNames.length;
			
			Property[] properties = new Property[len];
			
			for (int i = 0; i < len; i++) {
				String propertyName = propertyNames[i];
				
				if (finalProperty != null)
					throw new IllegalStateException("Invalid property path " + Arrays.asList(propertyNames) + " because the folowing has not an entity type: " + finalProperty);
				
				Property property = curType.getProperty(propertyName);
				properties[i] = property;
				
				GenericModelType propertyType = property.getType();
				
				if (propertyType.isEntity()) {
					curType = (EntityType<?>)propertyType;
				}
				else {
					finalProperty = property;
				}
			}
			
			this.hashFieldAccessors.add(new NullSafePropertyPathAccessor(properties));
			
			return this;
		}
		
		public Builder<E> addDeclaredPropertyFields() {
			entityType.getDeclaredProperties().forEach(this::addField);
			return this;
		}
		
		public Builder<E> addPropertyFields(Predicate<EntityType<?>> typeFilter) {
			entityType.getProperties()
				.stream()
				.filter(p -> typeFilter.test(p.getDeclaringType()))
				.forEach(this::addField);
			
			return this;
		}
		
		public Builder<E> hashesAreMutable(boolean mutable) {
			this.mutable = mutable;
			return this;
		}
		
		public EntityHashingComparator<E> done() {
			return new EntityHashingComparator<>(hashFieldAccessors, !mutable);
		}
	}
	
}
