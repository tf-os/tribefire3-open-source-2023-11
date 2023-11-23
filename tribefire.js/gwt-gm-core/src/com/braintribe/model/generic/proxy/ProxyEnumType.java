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
package com.braintribe.model.generic.proxy;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

@SuppressWarnings("unusable-by-js")
public class ProxyEnumType implements ScalarType {
	private static final GenericModelType[] PARAMETERIZATION = new GenericModelType[0];
	private final String typeSignature;
	private final Map<String, ProxyEnum> constants = new HashMap<>();
	private final ProxyContext proxyContext;

	public ProxyEnumType(ProxyContext proxyContext, String typeSignature) {
		super();
		this.proxyContext = proxyContext;
		this.typeSignature = typeSignature;
	}

	@Override
	public Class<?> getJavaType() {
		return ProxyEnum.class;
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.enumType;
	}

	@Override
	public String getTypeName() {
		return typeSignature;
	}

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		return "proxy of " + typeSignature;
	}

	@Override
	public GenericModelType getActualType(Object value) {
		return this;
	}

	@Override
	public Object getValueSnapshot(Object value) {
		return null;
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public boolean isInstance(Object value) {
		return false;
	}

	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		return false;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public boolean isEntity() {
		return false;
	}

	@Override
	public boolean isBase() {
		return false;
	}

	@Override
	public boolean isVd() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return true;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isScalar() {
		return true;
	}

	@Override
	public <T extends GenericModelType> T cast() {
		return (T) this;
	}

	@Override
	public boolean areEntitiesReachable() {
		return true;
	}

	@Override
	public boolean areCustomInstancesReachable() {
		return true;
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null;
	}

	@Override
	public int compareTo(GenericModelType o) {
		return 0;
	}

	@Override
	public GenericModelType[] getParameterization() {
		return PARAMETERIZATION;
	}

	@Override
	public void traverse(TraversingContext traversingContext, Object instance) throws GenericModelException {
		throw new UnsupportedOperationException("traverse is not possible for ProxyEnum");

	}

	@Override
	public TraversingContext traverse(Object instance, Matcher matcher, TraversingVisitor traversingVisitor) throws GenericModelException {
		throw new UnsupportedOperationException("traverse is not possible for ProxyEnum");
	}

	@Override
	public Object clone(Object instance, Matcher matcher, StrategyOnCriterionMatch strategy) throws GenericModelException {
		throw new UnsupportedOperationException("clone is not possible for ProxyEnum");
	}

	@Override
	public <T> T clone(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		throw new UnsupportedOperationException("clone is not possible for ProxyEnum");
	}

	@Override
	public <T> T instanceFromString(String encodedValue) throws GenericModelException {
		ProxyEnum proxyEnum = constants.get(encodedValue);

		if (proxyEnum == null) {
			proxyEnum = new ProxyEnum(this, encodedValue);
			constants.put(encodedValue, proxyEnum);
			proxyContext.onCreateEnum(proxyEnum);
		}

		return (T) proxyEnum;
	}

	@Override
	public String instanceToString(Object value) throws GenericModelException {
		return ((ProxyEnum) value).getConstantName();
	}

	@Override
	public String instanceToGmString(Object value) {
		ProxyEnum proxyEnum = (ProxyEnum) value;
		return GmValueCodec.enumToGmString(getTypeSignature(), proxyEnum.getConstantName());
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.enumFromGmString(encodedValue);
	}

}
