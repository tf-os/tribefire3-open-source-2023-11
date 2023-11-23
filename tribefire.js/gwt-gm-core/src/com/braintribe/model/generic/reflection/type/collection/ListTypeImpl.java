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
import java.util.List;

import com.braintribe.model.generic.collection.PlainList;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

@SuppressWarnings("unusable-by-js")
public final class ListTypeImpl extends AbstractCollectionType implements ListType {
	private final GenericModelType parameterization[];
	private final boolean simpleOrEnumContent;
	private final String typeSignature;
	private final AbstractGenericModelType elementType;

	private ListElementCriterion listElementCriterion;

	public ListTypeImpl(GenericModelType elementType) {
		super(List.class);
		this.elementType = (AbstractGenericModelType) elementType;
		this.parameterization = new GenericModelType[] { elementType };
		this.simpleOrEnumContent = isSimpleOrEnumContent(elementType);
		this.typeSignature = CollectionType.TypeSignature.forList(elementType.getTypeSignature());
	}

	@Override
	public boolean hasSimpleOrEnumContent() {
		return simpleOrEnumContent;
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.listType;
	}

	@Override
	public CollectionKind getCollectionKind() {
		return CollectionKind.list;
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

		return new PlainList<>(this, (List<?>) value);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		if (instance == null)
			return null;

		ListElementCriterion criterion = acquireCriterion();

		List<?> list = (List<?>) instance;
		List<Object> listClone = createPlain();
		for (Object value : list) {
			try {
				cloningContext.pushTraversingCriterion(criterion, value);
				if (!cloningContext.isTraversionContextMatching()) {
					Object clonedValue = elementType.cloneImpl(cloningContext, value, strategy);
					listClone.add(cloningContext.postProcessCloneValue(elementType, clonedValue));
				}
			} finally {
				cloningContext.popTraversingCriterion();
			}
		}

		return listClone;
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object instance) throws GenericModelException {
		if (instance == null)
			return;

		ListElementCriterion criterion = acquireCriterion();

		List<?> list = (List<?>) instance;
		for (Object value : list) {
			try {
				traversingContext.pushTraversingCriterion(criterion, value);
				elementType.traverseImpl(traversingContext, value);
			} finally {
				traversingContext.popTraversingCriterion();
			}
		}
	}

	private ListElementCriterion acquireCriterion() {
		if (listElementCriterion == null) {
			ListElementCriterion lc = ListElementCriterion.T.createPlainRaw();
			lc.setTypeSignature(elementType.getTypeSignature());

			listElementCriterion = lc;
		}

		return listElementCriterion;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		if (instance != null) {
			return "element count = " + ((List<?>) instance).size();
		} else
			return "";
	}

	@Override
	public GenericModelType[] getParameterization() {
		return parameterization;
	}

	@Override
	public String getTypeName() {
		return "list";
	}

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public PlainList<Object> createPlain() {
		return new PlainList<>(this);
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null || Collections.EMPTY_LIST.equals(value);
	}

	@Override
	protected boolean isInstanceOfThis(Object value) {
		return value instanceof List;
	}
}
