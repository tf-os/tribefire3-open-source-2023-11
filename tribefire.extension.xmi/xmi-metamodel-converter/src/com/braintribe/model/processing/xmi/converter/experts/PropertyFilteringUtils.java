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
package com.braintribe.model.processing.xmi.converter.experts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

/**
 * Used for detecting whether a given property is inherited from a supertype
 */
public class PropertyFilteringUtils {

	private Set<GmEntityType> visitedTypes = new HashSet<GmEntityType>();

	private static final Logger log = Logger.getLogger(PropertyFilteringUtils.class);

	/**
	 * @return true iff the property of this entityType is inherited from some of
	 *         it's supertypes
	 */
	public GmEntityType isInheritedProperty(GmEntityType entitType, GmProperty property) {
		visitedTypes.clear();
		return hasProperty(entitType.getSuperTypes(), property);
	}

	private GmEntityType hasProperty(List<GmEntityType> superTypes, GmProperty property) {
		if (superTypes == null) {
			return null;
		}

		for (GmEntityType superType : superTypes) {
			GmEntityType owner = hasProperty(superType, property);
			if (owner != null) {
				return owner;
			}
		}

		return null;
	}

	private GmEntityType hasProperty(GmEntityType superType, GmProperty property) {
		if (visitedTypes.contains(superType)) {
			return null;
		}

		visitedTypes.add(superType);

		try {
			if (containsProperty(superType.getProperties(), property)) {
				return superType;
			}
		} catch (CodecException e) {
			log.warn("Error while checking properties for entity: " + superType.getTypeSignature());
		}

		return hasProperty(superType.getSuperTypes(), property);
	}

	private boolean containsProperty(List<GmProperty> properties, GmProperty property) throws CodecException {
		if (properties == null) {
			return false;
		}

		for (GmProperty someProperty : properties) {
			if (isSameProprty(someProperty, property)) {
				return true;
			}
		}

		return false;
	}

	private boolean isSameProprty(GmProperty someProperty, GmProperty property) throws CodecException {
		if (!property.getName().equals(someProperty.getName())) {
			return false;
		}

		GmType propType = property.getType();

		if (propType == null) {
			throw new CodecException(
					"Failed to determine property type for property with name: " + someProperty.getName());
		}

		return areEqual(propType, someProperty.getType());
	}

	private boolean areEqual(GmType type1, GmType type2) {
		return type1.equals(type2) || (type1.getTypeSignature().equals(type2.getTypeSignature()));
	}

	public boolean propertyNeedsToBeImplemented(GmEntityType gmEntityType, GmProperty gmProperty) {
		return true;
		/*
		GmEntityType declaringEntityType = isInheritedProperty(gmEntityType, gmProperty);
		if (declaringEntityType != null) {
			// this is an interface -> don't implement
			if (!Boolean.TRUE.equals(gmEntityType.getIsPlain()))
				return false;
			// owning type is an interface, we're not -> implement
			if (Boolean.TRUE.equals(declaringEntityType.getIsPlain()))
				return false;
		}
		return true;
		*/
	}

}
