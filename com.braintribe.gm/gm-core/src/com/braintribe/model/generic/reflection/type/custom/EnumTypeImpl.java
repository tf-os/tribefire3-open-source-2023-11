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
package com.braintribe.model.generic.reflection.type.custom;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;
import com.braintribe.model.generic.value.EnumReference;

public class EnumTypeImpl extends AbstractCustomType implements EnumType {

	@Override
	public Class<? extends Enum<?>> getJavaType() {
		return (Class<? extends Enum<?>>) super.getJavaType();
	}

	private Map<String, Enum<?>> constants;

	public EnumTypeImpl(Class<? extends Enum<?>> javaType) {
		super(javaType);
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.enumType;
	}

	@Override
	public final boolean isEnum() {
		return true;
	}

	@Override
	public boolean isScalar() {
		return true;
	}

	@Override
	public Enum<? extends Enum<?>>[] getEnumValues() {
		Enum<? extends Enum<?>> enumValues[] = getJavaType().getEnumConstants();
		return enumValues;
	}

	@Override
	public Enum<? extends Enum<?>> getEnumValue(String name) {
		return getInstance(name);
	}

	@Override
	public Enum<? extends Enum<?>> findEnumValue(String name) {
		if (constants == null) {
			Map<String, Enum<?>> _constants = newMap();

			for (Enum<?> enumValue : getEnumValues())
				_constants.put(enumValue.name(), enumValue);

			constants = _constants;
		}

		return constants.get(name);
	}

	@Override
	public <T> T instanceFromString(String encodedValue) throws GenericModelException {
		return (T) getInstance(encodedValue);
	}

	@Override
	public String instanceToString(Object value) throws GenericModelException {
		return ((Enum<?>) value).name();
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return toGmString(value);
	}

	public static String toGmString(Object value) throws GenericModelException {
		return GmValueCodec.enumToGmString((Enum<?>) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		String[] parsedEnum = GmValueCodec.parseEnumConstantIdentifier(encodedValue);
		return getInstance(parsedEnum[1]);
	}

	@Override
	public <T extends Enum<T>> T getInstance(String value) {
		return Enum.valueOf((Class<T>) getJavaType(), value);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		return instance;
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object instance) {
		// noop
	}

	@Override
	public EnumReference getEnumReference(Enum<?> enumConstant) {
		EnumReference ref = EnumReference.T.createPlain();
		ref.setTypeSignature(getTypeName());
		ref.setConstant(enumConstant.name());

		return ref;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		return instance != null ? instance.toString() : "";
	}

	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		return type instanceof EnumTypeImpl && type == this;
	}

	@Override
	public boolean isInstance(Object value) {
		return javaType == value.getClass();
	}

	@Override
	public boolean areCustomInstancesReachable() {
		return true;
	}

	@Override
	public boolean areEntitiesReachable() {
		return false;
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null;
	}
}
