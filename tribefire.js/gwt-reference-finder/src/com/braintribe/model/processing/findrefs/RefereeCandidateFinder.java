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
package com.braintribe.model.processing.findrefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.findrefs.MapCandidateProperty.MapRefereeType;

/**
 * The {@link RefereeCandidateFinder} is used to determine all properties of entity types that might refer a certain
 * entity type.
 * 
 * 
 */
public class RefereeCandidateFinder {

	/**
	 * Determines all properties that might refer the specified entity.
	 * 
	 * @param referencedEntityType
	 *            The entity type to find potential referees for.
	 * @param entityTypesToCheck
	 *            The entity types to check for candidate properties.
	 * @return The list of {@link CandidateProperty}s describing the properties that might refer the specified entity.
	 */
	public List<CandidateProperty> findRefereeCandidateProperties(GmEntityType referencedEntityType,
			List<GmEntityType> entityTypesToCheck) {
		List<CandidateProperty> candidateProperties = new ArrayList<CandidateProperty>();

		for (GmEntityType gmEntityType : entityTypesToCheck) {
			Set<CandidateProperty> candidatePropertiesOfType = getRefereeCandidateProperties(gmEntityType,
					referencedEntityType);
			candidateProperties.addAll(candidatePropertiesOfType);
		}
		return candidateProperties;
	}

	private Set<CandidateProperty> getRefereeCandidateProperties(GmEntityType entityType,
			GmEntityType referencedEntityType) {

		HashSet<CandidateProperty> refereeCandidateProperties = new HashSet<CandidateProperty>();
		for (GmProperty property : entityType.getProperties()) {
			GmType propertyType = property.getType();
			if (propertyType instanceof GmLinearCollectionType) {
				GmLinearCollectionType linearCollectionType = (GmLinearCollectionType) propertyType;
				if (isLinearCollectionRefereeCandidate(referencedEntityType, linearCollectionType)) {
					refereeCandidateProperties.add(new ListCandidateProperty(entityType, property));
				}
			} else if (propertyType instanceof GmMapType) {
				GmMapType mapType = (GmMapType) propertyType;
				MapCandidateProperty candidateProp = createMapCandidate(entityType, property, mapType,
						referencedEntityType);

				if (MapRefereeType.NONE.equals(candidateProp.getRefereeType()) == false) {
					refereeCandidateProperties.add(candidateProp);
				}
			} else {
				if (isTypeOrSuper(propertyType, referencedEntityType)) {
					refereeCandidateProperties.add(new CandidateProperty(entityType, property));
				}
			}
		}

		return refereeCandidateProperties;
	}

	private boolean isLinearCollectionRefereeCandidate(GmEntityType referencedType,
			GmLinearCollectionType linearCollectionType) {
		GmType elementType = linearCollectionType.getElementType();
		return isTypeOrSuper(elementType, referencedType);
	}

	private MapCandidateProperty createMapCandidate(GmEntityType entityType, GmProperty property, GmMapType mapType,
			GmEntityType referencedEntityType) {
		GmType keyType = mapType.getKeyType();
		boolean keyRefPossible = isTypeOrSuper(keyType, referencedEntityType);

		GmType valueType = mapType.getValueType();
		boolean valueRefPossible = isTypeOrSuper(valueType, referencedEntityType);

		MapRefereeType candidateType = MapRefereeType.getType(keyRefPossible, valueRefPossible);
		return new MapCandidateProperty(entityType, property, candidateType);
	}

	private boolean isTypeOrSuper(GmType propertyType, GmEntityType referencedEntityType) {
		return areTypeSignaturesEqual(propertyType, referencedEntityType)
				|| doSuperTypesContain(propertyType, referencedEntityType);
	}

	private boolean doSuperTypesContain(GmType propertyType, GmEntityType referencedEntityType) {
		Set<GmEntityType> allSuperTypes = getSuperTypesRecursive(referencedEntityType);
		for (GmEntityType superType : allSuperTypes) {
			if (areTypeSignaturesEqual(propertyType, superType)) {
				return true;
			}
		}
		return false;
	}

	private boolean areTypeSignaturesEqual(GmType type, GmEntityType otherType) {
		String typeSignature = type.getTypeSignature();
		String otherTypeSignature = otherType.getTypeSignature();
		return typeSignature.equals(otherTypeSignature);
	}

	/*
	 * TODO: those methods should be moved to the type-classes - or to some kind of utility class
	 */
	private Set<GmEntityType> getSuperTypesRecursive(GmEntityType type) {
		Set<GmEntityType> allSuperTypes = new HashSet<GmEntityType>();
		addSuperTypes(allSuperTypes, type);
		return allSuperTypes;
	}

	private void addSuperTypes(Set<GmEntityType> collectedSuperTypes, GmEntityType type) {
		List<GmEntityType> superTypes = type.getSuperTypes();

		for (GmEntityType gmEntityType : superTypes) {
			addSuperTypes(collectedSuperTypes, gmEntityType);
		}

		collectedSuperTypes.addAll(superTypes);
	}
}
