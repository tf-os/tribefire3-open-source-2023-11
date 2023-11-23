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
package com.braintribe.model.generic.reflection.type.collection;

import java.util.Collections;
import java.util.Set;

import com.braintribe.model.generic.collection.PlainSet;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

public final class SetTypeImpl extends AbstractCollectionType implements SetType {
	private final GenericModelType parameterization[];
	private final boolean simpleOrEnumContent;
	private final String typeSignature;
	private final AbstractGenericModelType elementType;

	private SetElementCriterion setElementCriterion;

	public SetTypeImpl(GenericModelType elementType) {
		super(Set.class);
		this.elementType = (AbstractGenericModelType) elementType;
		this.parameterization = new GenericModelType[] { elementType };
		this.simpleOrEnumContent = isSimpleOrEnumContent(elementType);
		this.typeSignature = CollectionType.TypeSignature.forSet(elementType.getTypeSignature());
	}

	@Override
	public boolean hasSimpleOrEnumContent() {
		return simpleOrEnumContent;
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.setType;
	}

	@Override
	public CollectionKind getCollectionKind() {
		return CollectionKind.set;
	}

	@Override
	public GenericModelType getCollectionElementType() {
		return elementType;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueSnapshot(Object value) {
		if (value == null)
			return value;

		return new PlainSet<>(this, (Set<?>) value);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy)
			throws GenericModelException {

		if (instance == null)
			return null;

		SetElementCriterion criterion = acquireCriterion();

		Set<?> set = (Set<?>) instance;
		Set<Object> setClone = createPlain();
		for (Object value: set) {
			try {
				cloningContext.pushTraversingCriterion(criterion, value);
				if (!cloningContext.isTraversionContextMatching()) {
					Object clonedValue = elementType.cloneImpl(cloningContext, value, strategy);
					setClone.add(cloningContext.postProcessCloneValue(elementType, clonedValue));
				}
			} finally {
				cloningContext.popTraversingCriterion();
			}
		}
		return setClone;
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object instance) throws GenericModelException {
		if (instance == null)
			return;

		SetElementCriterion criterion = acquireCriterion();

		Set<?> set = (Set<?>) instance;
		for (Object value: set) {
			try {
				traversingContext.pushTraversingCriterion(criterion, value);
				elementType.traverseImpl(traversingContext, value);
			} finally {
				traversingContext.popTraversingCriterion();
			}
		}
	}

	private SetElementCriterion acquireCriterion() {
		if (setElementCriterion == null) {
			SetElementCriterion sc = SetElementCriterion.T.createPlainRaw();
			sc.setTypeSignature(elementType.getTypeSignature());

			setElementCriterion = sc;
		}

		return setElementCriterion;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		if (instance != null) {
			return "element count = " + ((Set<?>) instance).size();
		} else
			return "";
	}

	@Override
	public GenericModelType[] getParameterization() {
		return parameterization;
	}

	@Override
	public String getTypeName() {
		return "set";
	}

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public PlainSet<Object> createPlain() {
		return new PlainSet<>(this);
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null || Collections.EMPTY_SET.equals(value);
	}
	
	@Override
	protected boolean isInstanceOfThis(Object value) {
		return value instanceof Set;
	}
}
