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
package com.braintribe.model.processing.meta.editor.leaf;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmCustomModelElement;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.meta.editor.GlobalIdFactory;
import com.braintribe.model.processing.meta.editor.OverrideType;

/**
 * @author peter.gazdik
 */
public class LeafModel {

	private final GmMetaModel model;
	private final Map<GmCustomType, GmCustomTypeInfo> customTypeInfos = newMap();
	private final Function<EntityType<?>, GenericEntity> entityFactory;
	private final Predicate<? super GmModelElement> wasEntityUninstantiated;
	private final GlobalIdFactory globalIdFactory;

	public LeafModel(GmMetaModel model, Function<EntityType<?>, GenericEntity> entityFactory,
			Predicate<? super GmModelElement> wasEntityUninstantiated, GlobalIdFactory globalIdFactory) {

		Objects.requireNonNull(entityFactory, "'entityFactory' cannot be null");
		Objects.requireNonNull(wasEntityUninstantiated, "'wasEntityUninstantiated' cannot be null");

		this.model = model;
		this.entityFactory = entityFactory;
		this.wasEntityUninstantiated = wasEntityUninstantiated;

		this.globalIdFactory = globalIdFactory == null ? this::deriveGlobalId : globalIdFactory;

		for (GmType gmType : nullSafe(model.getTypes())) {
			if (gmType.isGmCustom()) {
				GmCustomType gmCustomType = (GmCustomType) gmType;
				customTypeInfos.put(gmCustomType, gmCustomType);
			}
		}

		for (GmCustomTypeOverride gmCustomTypeOverride : nullSafe(model.getTypeOverrides()))
			customTypeInfos.put(gmCustomTypeOverride.addressedType(), gmCustomTypeOverride);
	}

	public GmPropertyInfo acquireGmPropertyInfo(GmEntityType type, GmProperty property) {
		// TODO index for propertyName
		GmEntityTypeInfo gmEntityTypeInfo = acquireGmEntityTypeInfo(type);

		/* The gmEntityTypeInfo is declared for this model, thus if this condition is true, also the property must be
		 * declared on this level. */
		if (property.getDeclaringType() == gmEntityTypeInfo)
			return property;

		List<GmPropertyOverride> propertyOverrides = gmEntityTypeInfo.getPropertyOverrides();
		if (propertyOverrides == null) {
			gmEntityTypeInfo.setPropertyOverrides(newList());
			propertyOverrides = gmEntityTypeInfo.getPropertyOverrides();
		}

		for (GmPropertyOverride gmPropertyOverride : propertyOverrides)
			if (gmPropertyOverride.getProperty() == property)
				return gmPropertyOverride;

		GmPropertyOverride result = create(GmPropertyOverride.T, property, gmEntityTypeInfo, OverrideType.property);
		result.setProperty(property);
		result.setDeclaringTypeInfo(gmEntityTypeInfo);
		propertyOverrides.add(result);
		return result;
	}

	public GmEntityTypeInfo acquireGmEntityTypeInfo(GmEntityType type) {
		GmEntityTypeInfo cachedResult = findCustomTypeInfo(type);
		if (cachedResult != null) {
			return cachedResult;
		}

		GmEntityTypeOverride result = create(GmEntityTypeOverride.T, type, model, OverrideType.entityType);
		result.setEntityType(type);
		result.setDeclaringModel(model);

		model.getTypeOverrides().add(result);
		customTypeInfos.put(type, result);
		return result;
	}

	public GmEnumConstantInfo acquireGmConstantInfo(GmEnumType type, GmEnumConstant constant) {
		// TODO index for propertyName
		GmEnumTypeInfo gmEnumTypeInfo = acquireGmEnumTypeInfo(type);

		if (gmEnumTypeInfo instanceof GmEnumType) {
			return constant;
		}

		GmEnumTypeOverride gmEnumTypeOverride = (GmEnumTypeOverride) gmEnumTypeInfo;

		List<GmEnumConstantOverride> constantOverrides = gmEnumTypeOverride.getConstantOverrides();
		if (constantOverrides == null) {
			gmEnumTypeOverride.setConstantOverrides(newList());
			constantOverrides = gmEnumTypeOverride.getConstantOverrides();
		}

		for (GmEnumConstantOverride gmEnumConstantOverride : constantOverrides) {
			if (gmEnumConstantOverride.getEnumConstant() == constant) {
				return gmEnumConstantOverride;
			}
		}

		GmEnumConstantOverride result = create(GmEnumConstantOverride.T, constant, gmEnumTypeInfo, OverrideType.constant);
		result.setEnumConstant(constant);
		result.setDeclaringTypeOverride(gmEnumTypeOverride);
		constantOverrides.add(result);
		return result;
	}

	public GmEnumTypeInfo acquireGmEnumTypeInfo(GmEnumType type) {
		GmEnumTypeInfo cachedResult = findCustomTypeInfo(type);
		if (cachedResult != null)
			return cachedResult;

		GmEnumTypeOverride result = create(GmEnumTypeOverride.T, type, model, OverrideType.enumType);
		result.setEnumType(type);
		result.setDeclaringModel(model);

		model.getTypeOverrides().add(result);
		customTypeInfos.put(type, result);

		return result;
	}

	private <T extends GmCustomTypeInfo> T findCustomTypeInfo(GmCustomType type) {
		T result = (T) customTypeInfos.get(type);
		return result != null && !wasEntityUninstantiated.test(result) ? result : null;
	}

	private <T extends GmCustomModelElement> T create(EntityType<T> newType, GmModelElement originalElement, GmModelElement newOwnerElement,
			OverrideType overrideType) {

		String derivedGlobalId = globalIdFactory.generate(originalElement, newOwnerElement, overrideType);

		T result = (T) entityFactory.apply(newType);
		result.setGlobalId(derivedGlobalId);

		if (result instanceof GmCustomTypeInfo) {
			((GmCustomTypeInfo) result).setDeclaringModel(model);
		}

		return result;
	}

	private String deriveGlobalId(GmModelElement overriddenElement, GmModelElement newOwnerElement, OverrideType overrideType) {
		return getOverridePrefix(overrideType) + ":" + ownerPart(newOwnerElement, overrideType) + "/" + elementPart(overriddenElement, overrideType);
	}

	private String elementPart(GmModelElement overriddenElement, OverrideType overrideType) {
		switch (overrideType) {
			case constant:
				return ((GmEnumConstant) overriddenElement).getName();
			case entityType:
			case enumType:
				return overriddenElement.getGlobalId();
			case property:
				return ((GmProperty) overriddenElement).getName();
			default:
				throw new UnknownEnumException(overrideType);
		}
	}

	private String ownerPart(GmModelElement newOwnerElement, OverrideType overrideType) {
		if (overrideType != OverrideType.property && overrideType != OverrideType.constant)
			return model.getName();
		else
			return newOwnerElement.getGlobalId();
	}

	private static String getOverridePrefix(OverrideType overrideType) {
		switch (overrideType) {
			case constant:
				return "constantO";
			case entityType:
			case enumType:
				return "typeO";
			case property:
				return "propertyO";
			default:
				throw new UnknownEnumException(overrideType);
		}
	}

}
