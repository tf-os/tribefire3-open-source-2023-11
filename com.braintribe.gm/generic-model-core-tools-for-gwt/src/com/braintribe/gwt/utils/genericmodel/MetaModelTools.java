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
package com.braintribe.gwt.utils.genericmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides {@link GmMetaModel} related utility and convenience methods.
 * 
 * @deprecated All methods provide functionality available via {@link ModelOracle}, so please ajust the code to use that one.
 */
@Deprecated
public class MetaModelTools {

	/**
	 * Finds all (i.e. also inherited) properties for given {@link GmEntityType}. The method guarantees that every property is contained
	 * within the result just once (i.e. there are no two properties with same name).
	 * <p>
	 * If a property is overridden in the sub-entity, the method tries to take the {@link GmProperty} object from the sub-entity, but (in
	 * case of multiple inheritance) this behavior is not guaranteed.
	 * 
	 * @deprecated user {@link ModelOracle}
	 */
	@Deprecated
	public static Collection<GmProperty> findAllProperties(final GmEntityType gmEntityType) {
		final Map<String, GmProperty> result = new HashMap<String, GmProperty>();

		addAll(result, gmEntityType);

		return result.values();
	}

	private static void addAll(final Map<String, GmProperty> result, final GmEntityType gmEntityType) {
		final List<GmEntityType> superTypes = gmEntityType.getSuperTypes();

		if (superTypes != null) {
			for (final GmEntityType superType: superTypes) {
				addAll(result, superType);
			}
		}

		final List<GmProperty> properties = gmEntityType.getProperties();

		if (properties != null) {
			for (final GmProperty gmProperty: properties) {
				result.put(gmProperty.getName(), gmProperty);
			}
		}
	}

	/**
	 * Gets the {@link GmEntityType} with the specified <code>entityTypeSignature</code> or <code>null</code>, if it doesn't exist in the
	 * passed model.
	 * 
	 * @deprecated use {@link ModelOracle} framework for stuff like this. Or is there any reason not to?
	 */
	@Deprecated
	public static GmEntityType getEntityType(final GmMetaModel metaModel, final String entityTypeSignature) {
		for (final GmType gmType: metaModel.getTypes()) {
			if (entityTypeSignature.equals(gmType.getTypeSignature())) {
				return (GmEntityType) gmType;
			}
		}
		return null;
	}

	/**
	 * Gets the {@link GmEntityType} with the specified <code>entityTypeSignature</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if the searched type doesn't exist in the passed model.
	 * 
	 * @deprecated use {@link ModelOracle} framework for stuff like this. Or is there any reason not to?
	 */
	@Deprecated
	public static GmEntityType getExistingEntityType(final GmMetaModel metaModel, final String entityTypeSignature) {
		final GmEntityType result = getEntityType(metaModel, entityTypeSignature);
		if (result == null) {
			throw new IllegalArgumentException("The searched entity type is not part of the passed metamodel! " +
					CommonTools.getParametersString("entityTypeSignature", entityTypeSignature));
		}
		return result;
	}
}
