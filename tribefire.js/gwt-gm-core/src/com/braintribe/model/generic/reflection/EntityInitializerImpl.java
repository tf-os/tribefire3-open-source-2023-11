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
package com.braintribe.model.generic.reflection;

import java.util.Date;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EnumReference;

/**
 * @author peter.gazdik
 */
public abstract class EntityInitializerImpl implements EntityInitializer {

	protected Property property;

	@Override
	public abstract void initialize(GenericEntity entity);

	public static EntityInitializer newInstance(Property property) {
		return newInstance(property, property.getInitializer());
	}

	public static EntityInitializer newInstance(Property property, Object initializer) {
		EntityInitializerImpl result = newInitializerFor(initializer);
		result.property = property;
		
		return result;
	}

	private static EntityInitializerImpl newInitializerFor(Object initializer) {
		if (initializer instanceof GenericEntity) {
			if (initializer instanceof Now) {
				return new CurrentDateInitializer();
			}

			if (initializer instanceof EnumReference) {
				Object enumValue = resolveEnumConstant((EnumReference) initializer);
				return new StaticInitializer(enumValue);
			}
			
			throw new RuntimeException("No initializer implementation exists for initializer: " + initializer);
		}

		return new StaticInitializer(initializer);
	}

	private static Object resolveEnumConstant(EnumReference enumReference) {
		EnumType et = GMF.getTypeReflection().getType(enumReference.getTypeSignature());
		return et.getEnumValue(enumReference.getConstant());
	}
	
	static class StaticInitializer extends EntityInitializerImpl {
		private final Object value;

		public StaticInitializer(Object value) {
			this.value = value;
		}

		@Override
		public void initialize(GenericEntity entity) {
			property.set(entity, value);
		}
	}

	static class CurrentDateInitializer extends EntityInitializerImpl {
		@Override
		public void initialize(GenericEntity entity) {
			property.set(entity, new Date());
		}
	}
}
