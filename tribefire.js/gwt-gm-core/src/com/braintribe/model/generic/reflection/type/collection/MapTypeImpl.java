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
import java.util.Map;

import com.braintribe.model.generic.collection.PlainMap;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapEntryCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

public final class MapTypeImpl extends AbstractCollectionType implements MapType {
	private final GenericModelType parameterization[];
	private final boolean simpleOrEnumContent;
	private final String typeSignature;
	private final AbstractGenericModelType keyType;
	private final AbstractGenericModelType valueType;

	private MapCriterion mapCriterion;
	private MapEntryCriterion mapEntryCriterion;
	private MapKeyCriterion mapKeyCriterion;
	private MapValueCriterion mapValueCriterion;

	public MapTypeImpl(GenericModelType keyType, GenericModelType valueType) {
		super(Map.class);
		this.keyType = (AbstractGenericModelType) keyType;
		this.valueType = (AbstractGenericModelType) valueType;
		this.parameterization = new GenericModelType[] { keyType, valueType };
		this.simpleOrEnumContent = isSimpleOrEnumContent(keyType) && isSimpleOrEnumContent(valueType);
		this.typeSignature = CollectionType.TypeSignature.forMap(keyType.getTypeSignature(), valueType.getTypeSignature());
	}

	@Override
	public boolean hasSimpleOrEnumContent() {
		return simpleOrEnumContent;
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.mapType;
	}

	@Override
	public CollectionKind getCollectionKind() {
		return CollectionKind.map;
	}

	@Override
	public GenericModelType getCollectionElementType() {
		return valueType;
	}

	@Override
	public GenericModelType getKeyType() {
		return keyType;
	}

	@Override
	public GenericModelType getValueType() {
		return valueType;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueSnapshot(Object value) {
		if (value == null)
			return value;

		return new PlainMap<>(this, (Map<?, ?>) value);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy)
			throws GenericModelException {
		if (instance == null)
			return null;

		Map<Object, Object> mapClone = createPlain();
		Map<?, ?> map = (Map<?, ?>) instance;

		if (map.isEmpty()) {
			return mapClone;
		}

		try {
			cloningContext.pushTraversingCriterion(acquireMapCriterion(), instance);

			MapEntryCriterion entryCriterion = acquireMapEntryCriterion();
			MapKeyCriterion keyCriterion = acquireMapKeyCriterion();
			MapValueCriterion valueCriterion = acquireMapValueCriterion();

			for (Map.Entry<?, ?> entry: map.entrySet()) {
				try {
					cloningContext.pushTraversingCriterion(entryCriterion, entry);
					if (cloningContext.isTraversionContextMatching()) {
						continue;
					}

					// clone key
					Object key = entry.getKey();
					Object clonedKey = null;

					try {
						cloningContext.pushTraversingCriterion(keyCriterion, key);
						clonedKey = keyType.cloneImpl(cloningContext, key, strategy);
					} finally {
						cloningContext.popTraversingCriterion();
					}

					// clone value
					Object value = entry.getValue();
					Object clonedValue = null;

					try {
						cloningContext.pushTraversingCriterion(valueCriterion, value);
						clonedValue = valueType.cloneImpl(cloningContext, value, strategy);
					} finally {
						cloningContext.popTraversingCriterion();
					}

					mapClone.put(clonedKey, cloningContext.postProcessCloneValue(valueType, clonedValue));

				} finally {
					cloningContext.popTraversingCriterion();
				}
			}
		} finally {
			cloningContext.popTraversingCriterion();
		}

		return mapClone;
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object instance) throws GenericModelException {
		if (instance == null)
			return;

		Map<?, ?> map = (Map<?, ?>) instance;
		if (map.isEmpty()) {
			return;
		}

		MapEntryCriterion entryCriterion = acquireMapEntryCriterion();
		MapKeyCriterion keyCriterion = acquireMapKeyCriterion();
		MapValueCriterion valueCriterion = acquireMapValueCriterion();

		try {
			traversingContext.pushTraversingCriterion(acquireMapCriterion(), instance);
			for (Map.Entry<?, ?> entry: map.entrySet()) {
				try {
					traversingContext.pushTraversingCriterion(entryCriterion, entry);

					// traverse key
					Object key = entry.getKey();
					try {
						traversingContext.pushTraversingCriterion(keyCriterion, key);
						keyType.traverseImpl(traversingContext, key);
					} finally {
						traversingContext.popTraversingCriterion();
					}

					// traverse value
					Object value = entry.getValue();
					try {
						traversingContext.pushTraversingCriterion(valueCriterion, value);
						valueType.traverseImpl(traversingContext, value);
					} finally {
						traversingContext.popTraversingCriterion();
					}
				} finally {
					traversingContext.popTraversingCriterion();
				}
			}
		} finally {
			traversingContext.popTraversingCriterion();
		}
	}

	private MapCriterion acquireMapCriterion() {
		if (mapCriterion == null) {
			MapCriterion mc = MapCriterion.T.createPlain();
			mc.setTypeSignature(typeSignature);

			mapCriterion = mc;
		}

		return mapCriterion;
	}

	private MapEntryCriterion acquireMapEntryCriterion() {
		if (mapEntryCriterion == null) {
			MapEntryCriterion mec = MapEntryCriterion.T.createPlainRaw();
			mec.setTypeSignature(typeSignature);

			mapEntryCriterion = mec;
		}

		return mapEntryCriterion;
	}

	private MapKeyCriterion acquireMapKeyCriterion() {
		if (mapKeyCriterion == null) {
			MapKeyCriterion mkc = MapKeyCriterion.T.createPlainRaw();
			mkc.setTypeSignature(keyType.getTypeSignature());

			mapKeyCriterion = mkc;
		}

		return mapKeyCriterion;
	}

	private MapValueCriterion acquireMapValueCriterion() {
		if (mapValueCriterion == null) {
			MapValueCriterion mvc = MapValueCriterion.T.createPlainRaw();
			mvc.setTypeSignature(valueType.getTypeSignature());

			mapValueCriterion = mvc;
		}

		return mapValueCriterion;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		if (instance != null) {
			return "element count = " + ((Map<?, ?>) instance).size();
		} else
			return "";
	}

	@Override
	public GenericModelType[] getParameterization() {
		return parameterization;
	}

	@Override
	public String getTypeName() {
		return "map";
	}

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public PlainMap<Object, Object> createPlain() {
		return new PlainMap<>(this);
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null || Collections.EMPTY_MAP.equals(value);
	}
	
	@Override
	protected boolean isInstanceOfThis(Object value) {
		return value instanceof Map;
	}
}
