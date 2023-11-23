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
package com.braintribe.model.generic.reflection.type.simple;

import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;

@SuppressWarnings("unusable-by-js")
public abstract class AbstractSimpleType extends AbstractGenericModelType implements SimpleType {

	protected AbstractSimpleType(Class<?> javaType) {
		super(javaType);

	}

	@Override
	public final boolean isSimple() {
		return true;
	}

	@Override
	public boolean isScalar() {
		return true;
	}

	@Override
	public String instanceToString(Object value) throws GenericModelException {
		return value.toString();
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) {
		return instance;
	}

	@Override
	public final void traverseImpl(TraversingContext traversingContext, Object instance) {
		// noop
	}

	@Override
	public final String getSelectiveInformation(Object instance) {
		return instance != null ? instance.toString() : "";
	}

	@Override
	public final boolean isAssignableFrom(GenericModelType type) {
		return this == type;
	}

	@Override
	public boolean isInstance(Object value) {
		return javaType == value.getClass();
	}

	@Override
	public final boolean areEntitiesReachable() {
		return false;
	}

	@Override
	public final boolean areCustomInstancesReachable() {
		return false;
	}

	@Override
	public final boolean isEmpty(Object value) {
		return value == null;
	}
	
	@Override
	@SuppressWarnings("unusable-by-js")
	public Class<?> getPrimitiveJavaType() {
		return javaType;
	}

}
