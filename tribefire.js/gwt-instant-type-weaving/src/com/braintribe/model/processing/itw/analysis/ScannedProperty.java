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
package com.braintribe.model.processing.itw.analysis;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;

class ScannedProperty {
	public final Class<? extends GenericEntity> entityClass;
	public final String propertyName;
	public Method setter;
	public Method getter;
	public Type propertyType;

	public ScannedProperty(Class<? extends GenericEntity> entityClass, String propertyName) {
		this.entityClass = entityClass;
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public Type getPropertyType() {
		return propertyType;
	}

	public Class<?> getPropertyRawType() {
		return getter.getReturnType();
	}

	private Type getSetterType() {
		return setter != null ? setter.getGenericParameterTypes()[0] : null;
	}

	private Type getGetterType() {
		return getter != null ? getter.getGenericReturnType() : null;
	}

	public String getInitializerString() {
		Initializer gi = getter.getAnnotation(Initializer.class);
		return gi == null ? null : gi.value();
	}

	@Override
	public String toString() {
		return "ScannedProperty[" + fullPropertyName() + "]";
	}

	public void validate() throws JavaTypeAnalysisException {
		Type setterType = getSetterType();
		Type getterType = JavaTypeAnalysis.sanitizeType(getGetterType());

		if (setterType == null) {
			try {
				entityClass.getMethod(setterName(), getPropertyRawType());
			} catch (Exception e) {
				throw new JavaTypeAnalysisException("setter not found for property: " + fullPropertyName());
			}

			return;
		}

		// This must be equals cause for parameterized types we can have different instances being equal
		if (!setterType.equals(getterType)) {
			throw new JavaTypeAnalysisException(
					"setter type (" + setterType + ") does not match getter type (" + getterType + ") for property: " + fullPropertyName());
		}

		propertyType = getterType;
	}

	private String setterName() {
		return "s" + getter.getName().substring(1);
	}

	private String fullPropertyName() {
		return entityClass.getName() + "." + propertyName;
	}

}
