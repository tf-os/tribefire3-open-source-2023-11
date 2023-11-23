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

import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.type.BaseTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.AbstractCollectionType;
import com.braintribe.model.generic.reflection.type.custom.AbstractEntityType;
import com.braintribe.model.generic.reflection.type.custom.EnumTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.AbstractSimpleType;

public abstract class AbstractGenericModelType implements GenericModelType {

	protected Class<?> javaType;

	protected AbstractGenericModelType(Class<?> javaType) {
		this.javaType = javaType;
	}

	@Override
	public Class<?> getJavaType() {
		return javaType;
	}

	@Override
	public abstract TypeCode getTypeCode();

	@Override
	public abstract String getTypeName();

	@Override
	public abstract String getSelectiveInformation(Object instance);

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public final <T> T clone(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		if (strategy == null)
			strategy = cloningContext.getStrategyOnCriterionMatch();

		RootCriterion rootCriterion = RootCriterion.T.createPlain();
		rootCriterion.setTypeSignature(getActualType(instance).getTypeSignature());
		cloningContext.pushTraversingCriterion(rootCriterion, instance);
		try {
			if (!cloningContext.isTraversionContextMatching())
				return (T) cloneImpl(cloningContext, instance, strategy);
			else
				return null;

		} finally {
			cloningContext.popTraversingCriterion();
		}
	}

	/**
	 * This method returns in the most specified type statement seen from assignable perspective.
	 */
	@Override
	public GenericModelType getActualType(Object value) {
		return this;
	}

	public abstract Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException;

	@Override
	public final void traverse(TraversingContext traversingContext, Object instance) throws GenericModelException {
		RootCriterion rootCriterion = RootCriterion.T.createPlain();
		rootCriterion.setTypeSignature(getActualType(instance).getTypeSignature());
		traversingContext.pushTraversingCriterion(rootCriterion, instance);
		try {
			if (!traversingContext.isTraversionContextMatching())
				traverseImpl(traversingContext, instance);

		} finally {
			traversingContext.popTraversingCriterion();
		}
	}

	public abstract void traverseImpl(TraversingContext traversingContext, Object instance) throws GenericModelException;

	@Override
	public TraversingContext traverse(Object instance, Matcher matcher, TraversingVisitor traversingVisitor) throws GenericModelException {
		StandardTraversingContext traversingContext = new StandardTraversingContext();
		traversingContext.setMatcher(matcher);
		traversingContext.setTraversingVisitor(traversingVisitor);
		traverse(traversingContext, instance);
		return traversingContext;
	}

	@Override
	public final <T> T clone(Object instance, Matcher matcher, StrategyOnCriterionMatch strategy) throws GenericModelException {
		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setMatcher(matcher);
		return clone(cloningContext, instance, strategy);
	}

	private static final GenericModelType[] emptyParameterization = new GenericModelType[] {};

	@Override
	public GenericModelType[] getParameterization() {
		return emptyParameterization;
	}

	/**
	 * Returns a "snapshot" (i.e. current state) of given value. Typical use-case is the value to be set for a
	 * manipulation (e.g. ChangeValueManipulation). This value may in most cases be the original object, but in case of
	 * collections (including maps), we have to create a copy (because if we used the original collection and changed it
	 * later, we would also change the manipulation).
	 */
	@Override
	public Object getValueSnapshot(Object value) {
		return value;
	}

	@Override
	public String getTypeSignature() {
		return getTypeName();
	}

	/** This is overridden in the {@link BaseTypeImpl} subclass. */
	@Override
	public boolean isBase() {
		return false;
	}

	/** This is overridden in the {@link AbstractCollectionType} subclass. */
	@Override
	public boolean isCollection() {
		return false;
	}

	/** This is overridden in the {@link AbstractEntityType} subclass. */
	@Override
	public boolean isEntity() {
		return false;
	}

	/** This is overridden in the {@link AbstractEntityType} subclass. */
	@Override
	public boolean isVd() {
		return false;
	}

	/** This is overridden in the {@link EnumTypeImpl} subclass. */
	@Override
	public boolean isEnum() {
		return false;
	}

	/** This is overridden in the {@link AbstractSimpleType} subclass. */
	@Override
	public boolean isSimple() {
		return false;
	}

	/** This is overridden in the specific numeric type implementations. */
	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public <T extends GenericModelType> T cast() {
		return (T) this;
	}

	@Override
	public String toString() {
		return "type " + getTypeSignature();
	}

	@Override
	public int compareTo(GenericModelType o) {
		if (o == null) {
			return 1;
		}

		if (this == o)
			return 0;

		String myTypeSignature = this.getTypeSignature();
		String otherTypeSignature = o.getTypeSignature();

		return myTypeSignature.compareTo(otherTypeSignature);
	}

}
