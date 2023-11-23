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
package com.braintribe.model.processing.meta.oracle.flat;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;

/**
 * Factory for flat local properties - i.e. properties / property info of given entity type that partially includes
 * information from the super types, just whatever was declared on this level (with the {@link GmEntityType} and
 * {@link GmEntityTypeOverride}s).
 * 
 * If the property is inherited from a super-type, it's {@link FlatProperty#infos} will be empty.
 * 
 * @author peter.gazdik
 */
public class FlatPropertiesFactory {

	private final FlatEntityType flatEntityType;
	private final Map<String, FlatProperty> flatProperties;

	public static Map<String, FlatProperty> buildFor(FlatEntityType flatEntityType) {
		return new FlatPropertiesFactory(flatEntityType).build();
	}

	FlatPropertiesFactory(FlatEntityType flatEntityType) {
		this.flatEntityType = flatEntityType;
		this.flatProperties = newMap();
	}

	private Map<String, FlatProperty> build() {
		for (GmEntityTypeInfo entityTypeInfo : flatEntityType.infos)
			visit(entityTypeInfo);

		collectInheritedNotOverriddenProperties();

		return flatProperties;
	}

	private void visit(GmEntityTypeInfo gmEntityTypeInfo) {
		for (GmPropertyOverride gmPropertyOverride : nullSafe(gmEntityTypeInfo.getPropertyOverrides()))
			visitPropertyInfo(gmPropertyOverride);

		if (!(gmEntityTypeInfo instanceof GmEntityType))
			return;

		GmEntityType gmEntityType = (GmEntityType) gmEntityTypeInfo;
		for (GmProperty gmProperty : nullSafe(gmEntityType.getProperties()))
			visitPropertyInfo(gmProperty);
	}

	private void visitPropertyInfo(GmPropertyInfo gmPropertyInfo) {
		FlatProperty flatProperty = acquireFlatProperty(gmPropertyInfo.relatedProperty());
		flatProperty.infos.add(gmPropertyInfo);
	}

	private void collectInheritedNotOverriddenProperties() {
		GmEntityType gmEntityType = flatEntityType.type;
		for (GmEntityType superType : nullSafe(gmEntityType.getSuperTypes())) {
			try {
				FlatEntityType flatSuperType = flatEntityType.flatModel.getFlatCustomType(superType.getTypeSignature());
				Map<String, FlatProperty> flatSuperProperties = flatSuperType.acquireFlatProperties();

				for (Entry<String, FlatProperty> entry : flatSuperProperties.entrySet())
					ensureSuperProperty(entry.getKey(), entry.getValue());

			} catch (RuntimeException npe) {
				throw Exceptions.contextualize(npe, propertyContextMsg(gmEntityType, superType));
			}
		}
	}

	private String propertyContextMsg(GmEntityType gmEntityType, GmEntityType superType) {
		return "gmEntityType: " + gmEntityType + ", superTypes: " + gmEntityType.getSuperTypes() + ", superType: " + superType + //
				", flatEntityType: " + flatEntityType + ", flatModel: " + (flatEntityType != null ? null : flatEntityType.flatModel);
	}

	private void ensureSuperProperty(String propertyName, FlatProperty flatSuperProperty) {
		FlatProperty existingFlatProperty = flatProperties.get(propertyName);
		if (existingFlatProperty != null && !existingFlatProperty.infos.isEmpty())
			return;

		FlatProperty newFlatProperty = getNewFlatProperty(existingFlatProperty, flatSuperProperty);
		flatProperties.put(propertyName, newFlatProperty);
	}

	/**
	 * This method is only reachable if existingFlatProperty is null or has no infos. Because flat property only reflects property info declared on an
	 * entity level excluding any super-type infos, this means we will create a FlatProperty with empty "infos" list.
	 * <p>
	 * Here we prefer to use the {@link FlatProperty} from a super-type, assuming we can (i.e. it also has empty infos) over an already existing flat
	 * property. The reason is that this already existing one might have been created on this level, when handling the property from different
	 * sub-type, but in that case the super type also had some infos. So in this case, we make sure to use an instance that is already used, so that
	 * the possible new instance can be collected by GC.
	 */
	private FlatProperty getNewFlatProperty(FlatProperty existingFlatProperty, FlatProperty flatSuperProperty) {
		if (flatSuperProperty.infos.isEmpty())
			return flatSuperProperty;

		if (existingFlatProperty != null)
			return existingFlatProperty;

		return new FlatProperty(flatSuperProperty);
	}

	private FlatProperty acquireFlatProperty(GmProperty gmProperty) {
		String propertyName = gmProperty.getName();

		return flatProperties.computeIfAbsent(propertyName, n -> new FlatProperty(gmProperty));
	}

}
