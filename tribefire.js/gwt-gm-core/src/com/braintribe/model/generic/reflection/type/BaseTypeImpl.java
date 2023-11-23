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
package com.braintribe.model.generic.reflection.type;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

public class BaseTypeImpl extends AbstractGenericModelType implements BaseType {

	public static final BaseTypeImpl INSTANCE = new BaseTypeImpl();

	private BaseTypeImpl() {
		super(Object.class);
	}

	/* NOTE: This must be initialized lazily!!! */
	private GenericModelTypeReflection typeReflection;

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.objectType;
	}

	@Override
	public boolean isBase() {
		return true;
	}

	@Override
	public boolean isEmpty(Object value) {
		if (value == null)
			return true;

		GenericModelType type = getActualType(value);
		return type.isEmpty(value);
	}

	public AbstractGenericModelType getActualAbsType(Object value) {
		return (AbstractGenericModelType) getActualType(value);
	}

	@Override
	public GenericModelType getActualType(Object value) {
		if (value == null)
			return this;

		return getTypeReflection().getType(value);
	}

	private GenericModelTypeReflection getTypeReflection() {
		if (typeReflection == null) {
			typeReflection = GMF.getTypeReflection();
		}

		return typeReflection;
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		if (instance == null)
			return null;
		else
			return getActualAbsType(instance).cloneImpl(cloningContext, instance, strategy);
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		if (instance == null)
			return null;
		else
			return getActualAbsType(instance).getSelectiveInformation(instance);
	}

	@Override
	public String getTypeName() {
		return "object";
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueSnapshot(Object value) {
		return value != null ? getActualType(value).getValueSnapshot(value) : null;
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object instance) throws GenericModelException {
		if (instance != null) {
			getActualAbsType(instance).traverseImpl(traversingContext, instance);
		}
	}

	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		return true;
	}

	@Override
	public boolean isValueAssignable(Object value) {
		return true;
	}

	@Override
	public boolean isInstance(Object value) {
		return value != null;
	}
	
	@Override
	public boolean areCustomInstancesReachable() {
		return true;
	}

	@Override
	public boolean areEntitiesReachable() {
		return true;
	}
}
