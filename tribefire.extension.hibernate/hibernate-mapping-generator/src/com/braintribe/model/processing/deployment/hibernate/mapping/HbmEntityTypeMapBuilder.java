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
package com.braintribe.model.processing.deployment.hibernate.mapping;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.UnmappableModelException;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.EntityHint;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.MappingHelper;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.CollectionsUtils;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType.EntityTypeCategory;

/**
 * <p>
 * Creates {@link HbmEntityType} instances for the entities of a {@link HbmXmlGenerationContext}'s {@link GmMetaModel}.
 */
public class HbmEntityTypeMapBuilder {

	private static final Logger log = Logger.getLogger(HbmEntityTypeMapBuilder.class);

	private final HbmXmlGenerationContext context;

	private final Map<String, HbmEntityType> hbmEntityTypes = newMap();
	private final Set<String> topLevelCandidates = newSet();
	private Map<String, Set<String>> overlappingTypes = newMap();
	private final Set<String> skippedEntities = newSet(); // elements are "typeSignature"
	private final Set<String> skippedProperties = newSet(); // elements are "typeSignature:propertyName"
	private final Set<GmEntityType> embeddableTypes = newSet(); // entities that are embedded, not mapped

	public HbmEntityTypeMapBuilder(HbmXmlGenerationContext context) {
		this.context = context;
	}

	public Map<String, HbmEntityType> generateHbmEntityTypeMap() {
		buildSkipMappingsSet();
		buildHbmEntitiesStructure();
		return hbmEntityTypes;
	}

	private void buildSkipMappingsSet() {
		for (GmEntityType gmEntityType : context.getEntityTypes()) {
			EntityHint entityHint = context.getEntityHintProvider().provide(gmEntityType.getTypeSignature());
			if (!MappingHelper.mapEntityToDb(gmEntityType, entityHint, context.getMappingMetaDataResolver()))
				skippedEntities.add(gmEntityType.getTypeSignature());

			if (MappingHelper.isEmbeddable(gmEntityType, context))
				embeddableTypes.add(gmEntityType);

			for (GmProperty gmProperty : CollectionsUtils.nullSafe(gmEntityType.getProperties()))
				if (!MappingHelper.mapPropertyToDb(gmEntityType, gmProperty, entityHint, context.getMappingMetaDataResolver()))
					skippedProperties.add(gmEntityType.getTypeSignature() + ":" + gmProperty.getName());
		}
	}

	private void buildHbmEntitiesStructure() {
		buildHbmEntityTypeMap();

		categorizeEntities();

		if (log.isDebugEnabled()) {

			StringBuilder entityClassification = new StringBuilder();
			for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet()) {
				entityClassification.append("[" + hbmEntityTypeEntry.getKey() + "]: " + hbmEntityTypeEntry.getValue().getTypeCategory())
						.append("\r\n");
			}

			log.debug("Entities' Hibernate Categorization:\r\n" + entityClassification.toString());

			String multipleInheritanceTree = generateEntityTree(false);
			log.debug("Mapped Classes Inheritance:\r\n" + multipleInheritanceTree);

			String singleInheritanceTree = generateEntityTree(true);
			log.debug("Hibernate Mappings Inheritance:\r\n" + singleInheritanceTree);
		}
	}

	public void categorizeEntities() {
		// top level entities must be resolved
		electTopLevelEntities();

		// based on the top level elected, CLASS category are applied to them
		// cascading SUBCLASS to it's subtypes
		categorizeTopLevelEntities();
	}

	private void electTopLevelEntities() {

		// 1. leaf nodes
		enlistLeafNodeTopLevelCandidates();

		// 2. forced types
		enlistForcedMappingEntities();

		// 3. referenced types
		enlistMemberReferencedTopLevelCandidates();

		// 4. fix overlapping hierarchies, if any
		fixOverlapping();
	}

	private void enlistLeafNodeTopLevelCandidates() {
		for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet()) {
			bindFlattenedHierarchy(hbmEntityTypeEntry.getValue());
			if (hbmEntityTypeEntry.getValue().getSubTypes().isEmpty()
					&& !Boolean.TRUE.equals(hbmEntityTypeEntry.getValue().getType().getIsAbstract())) {
				log.trace(() -> "Top-level classes selection: Instantiable leaf node enlisted: "
						+ hbmEntityTypeEntry.getValue().getType().getTypeSignature());
				topLevelCandidates.add(hbmEntityTypeEntry.getValue().getType().getTypeSignature());
			}
		}
	}

	private void enlistForcedMappingEntities() {
		for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet()) {
			if (MappingHelper.isForcedMappingEntity(hbmEntityTypeEntry.getValue().getType(), context.getMappingMetaDataResolver())) {
				log.debug(() -> "Top-level classes selection: Forced mapping: " + hbmEntityTypeEntry.getValue().getType().getTypeSignature());
				applyTopLevelCandidate(hbmEntityTypeEntry.getValue().getType().getTypeSignature());
			}
		}
	}

	private void enlistMemberReferencedTopLevelCandidates() throws UnmappableModelException {
		for (String memberReferencedType : CollectionsUtils.nullSafe(memberReferencedTypes())) {
			log.debug(() -> "Top-level classes selection: Referenced type enlisted: " + memberReferencedType);
			applyTopLevelCandidate(memberReferencedType);
		}
	}

	/**
	 * <p>
	 * Categorizes the HbmEntityType into:
	 * 
	 * <ul>
	 * <li>{@link EntityTypeCategory#CLASS} - top level types as determined by resolveTopLevelEntities
	 * <li>{@link EntityTypeCategory#SUBCLASS} - sub types of top level types
	 * <li>{@link EntityTypeCategory#UNMAPPED} - sibling or super types of those elected top level, default value
	 * </ul>
	 */
	private void categorizeTopLevelEntities() {
		for (String topLevelType : topLevelCandidates) {
			hbmEntityTypes.get(topLevelType).setTypeCategory(EntityTypeCategory.CLASS);
			categorizeSubTypeEntities(hbmEntityTypes.get(topLevelType));
		}
	}

	private void categorizeSubTypeEntities(HbmEntityType superType) {
		if (CollectionsUtils.isEmpty(superType.getSubTypes()))
			return;

		for (HbmEntityType subType : superType.getSubTypes()) {
			categorizeSubTypeEntities(subType);
			subType.setTypeCategory(EntityTypeCategory.SUBCLASS);

			if (subType.getSuperType() == null)
				subType.setSuperType(superType);
			else if (!assertSuperTypeDeclarationOrder(subType, subType.getSuperType(), superType))
				subType.setSuperType(superType);

		}
	}

	private static boolean assertSuperTypeDeclarationOrder(HbmEntityType entity, String superTypeSignatureA, String superTypeSignatureB) {
		Integer ai = Integer.MAX_VALUE;
		Integer bi = Integer.MAX_VALUE;
		String t = null;
		for (int i = 0; i < CollectionsUtils.safeSize(entity.getType().getSuperTypes()); i++) {
			t = entity.getType().getSuperTypes().get(i).getTypeSignature();
			if (t.equals(superTypeSignatureA))
				ai = i;
			if (t.equals(superTypeSignatureB))
				bi = i;
		}
		return ai.compareTo(bi) < 0;
	}

	private static boolean assertSuperTypeDeclarationOrder(HbmEntityType entity, HbmEntityType superEntityA, HbmEntityType superEntityB) {
		return assertSuperTypeDeclarationOrder(entity, superEntityA.getType().getTypeSignature(), superEntityB.getType().getTypeSignature());
	}

	/** Merge overlapping hierarchies. */
	private void fixOverlapping() throws UnmappableModelException {

		buildOverlappingTypeMap();

		while (!overlappingTypes.isEmpty()) {

			Map.Entry<String, Set<String>> overlappingTypesEntry = overlappingTypes.entrySet().iterator().next();

			log.debug(() -> "Overlapping hierarchy found. The type " + overlappingTypesEntry.getKey() + " participates in multiple hierarchies: "
					+ overlappingTypesEntry.getValue());

			String upperTopLevelType = electUpperTopLevelType(overlappingTypesEntry.getValue());

			// if the commonSuperType elected is not valid, throws UnmappableModelException
			// leaf nodes cannot be used as top level classes when super types are referenced by other types.
			if (ineligibleTopLevelType(upperTopLevelType)) {
				throw new UnmappableModelException("Unmappable model: Common super type of overlapping hierarchies ("
						+ overlappingTypesEntry.getValue() + ") is considered an ineligible top level: " + upperTopLevelType);
			}

			log.debug(() -> "Found valid common super type of overlapping hierarchies (" + overlappingTypesEntry.getValue() + "): "
					+ upperTopLevelType);

			// apply the commonSuperType found as top level, thus modifying the topLevelCandidates Set
			applyTopLevelCandidate(upperTopLevelType);

			log.debug(() -> "Common super type " + upperTopLevelType + " of overlapping hierarchies " + overlappingTypesEntry.getValue()
					+ " was promoted to top level class");

			// fixOverlapping is called again
			// As topLevelCandidates was modified, the overlappingTypes Map must be built again.
			// the method will run until the overlappingTypes Maps is null
			fixOverlapping();
		}
	}

	/** Evaluation of possible top level during the merge of overlapping hierarchies */
	private static boolean ineligibleTopLevelType(String commonSuperType) {
		return (commonSuperType == null || commonSuperType.equals(GenericEntity.class.getName()));
	}

	private void buildOverlappingTypeMap() {
		overlappingTypes = newLinkedMap();
		for (String topLevelType : topLevelCandidates) {
			if (hbmEntityTypes.get(topLevelType).getFlattenedSubTypesSignatures().isEmpty()) {
				continue;
			}
			for (String otherTopLevelType : topLevelCandidates) {
				if (topLevelType.equals(otherTopLevelType)) {
					continue;
				}
				if (hbmEntityTypes.get(otherTopLevelType).getFlattenedSubTypesSignatures().isEmpty()) {
					continue;
				}
				Set<String> subTypeIntersection = newSet(hbmEntityTypes.get(topLevelType).getFlattenedSubTypesSignatures());
				subTypeIntersection.retainAll(hbmEntityTypes.get(otherTopLevelType).getFlattenedSubTypesSignatures());
				for (String commonSubType : subTypeIntersection) {
					if (!overlappingTypes.containsKey(commonSubType))
						overlappingTypes.put(commonSubType, newLinkedSet());
					overlappingTypes.get(commonSubType).add(topLevelType);
					overlappingTypes.get(commonSubType).add(otherTopLevelType);
				}
			}
		}
	}

	/**
	 * This method: - Removes every typeSignature's sub types from topLevelCandidates Set. - Add the typeSignature to the topLevelCandidates Set. The
	 * action is ignored if: - typeSignature is already in topLevelCandidates - Any of typeSignature's superclasses are already a candidate
	 */
	private void applyTopLevelCandidate(String typeSignature) {
		if (!hbmEntityTypes.containsKey(typeSignature))
			throw new IllegalArgumentException(typeSignature + " cannot be set as a top level candidate as it is not mapped HbmEntityType");

		if (topLevelCandidates.contains(typeSignature)
				|| containsAny(topLevelCandidates, hbmEntityTypes.get(typeSignature).getFlattenedSuperTypesSignatures()))
			return;

		topLevelCandidates.removeAll(hbmEntityTypes.get(typeSignature).getFlattenedSubTypesSignatures());
		topLevelCandidates.add(typeSignature);
	}

	private boolean containsAny(Set<String> set, Set<String> elements) {
		for (String e : elements)
			if (set.contains(e))
				return true;

		return false;
	}

	/**
	 * Elects a preferable new top level type for a set of hierarchies that need to be merged
	 */
	private String electUpperTopLevelType(Set<String> types) {
		if (CollectionsUtils.isEmpty(types))
			return null;

		Set<String> commonSuperTypes = collectCommonSuperTypes(types);

		if (CollectionsUtils.isEmpty(commonSuperTypes))
			return null;

		if (CollectionsUtils.safeSize(commonSuperTypes) == 1)
			return first(commonSuperTypes);

		return electLeastDisruptiveSuperType(types, commonSuperTypes);
	}

	private String electLeastDisruptiveSuperType(Set<String> typeHierarchies, Set<String> commonSuperTypes) {
		Set<String> superTypesElected = electLeastDisruptiveByIntroducedSubTypes(typeHierarchies, commonSuperTypes);

		if (CollectionsUtils.isEmpty(superTypesElected))
			return null;

		if (CollectionsUtils.safeSize(superTypesElected) == 1)
			return first(superTypesElected);

		superTypesElected = electLeastDisruptiveByInheritedProperties(superTypesElected);

		if (CollectionsUtils.isEmpty(superTypesElected))
			return null;
		else
			return first(superTypesElected);
	}

	/**
	 * Collects the types in commonSuperTypes introducing the least number of new types (types not included in typeHierarchies)
	 */
	private Set<String> electLeastDisruptiveByIntroducedSubTypes(Set<String> typeHierarchies, Set<String> commonSuperTypes) {

		Set<String> typesInMergedHierarchies = collectSubTypes(typeHierarchies);
		typesInMergedHierarchies.addAll(typeHierarchies);

		Map<String, Integer> superTypesRank = rankTypeHierarchiesByTypeIntroduction(typesInMergedHierarchies, commonSuperTypes);
		if (CollectionsUtils.isEmpty(superTypesRank))
			return emptySet();

		if (CollectionsUtils.safeSize(superTypesRank) == 1)
			return superTypesRank.keySet();

		Integer minNrOfIntroducedSubTypes = Collections.min(superTypesRank.values());

		Set<String> elected = newSet();
		for (Map.Entry<String, Integer> entry : superTypesRank.entrySet()) {
			if (entry.getValue().equals(minNrOfIntroducedSubTypes))
				elected.add(entry.getKey());
		}

		return elected;
	}

	/**
	 * Collects the types in commonSuperTypes introducing the largest number of properties
	 */
	private Set<String> electLeastDisruptiveByInheritedProperties(Set<String> commonSuperTypes) {

		Map<String, Integer> superTypesRank = rankTypeHierarchiesByPropertiesInherited(commonSuperTypes);
		if (CollectionsUtils.isEmpty(superTypesRank))
			return Collections.<String> emptySet();
		if (CollectionsUtils.safeSize(superTypesRank) == 1)
			return superTypesRank.keySet();

		Integer maxNrOfInheritedProperties = Collections.max(superTypesRank.values());

		Set<String> elected = newSet();
		for (Map.Entry<String, Integer> entry : superTypesRank.entrySet()) {
			if (entry.getValue().equals(maxNrOfInheritedProperties))
				elected.add(entry.getKey());
		}

		return elected;
	}

	public Map<String, Integer> rankTypeHierarchiesByTypeIntroduction(Set<String> currentTypes, Set<String> commonSuperTypes) {
		Map<String, Integer> superTypesRank = newMap(commonSuperTypes.size());
		Set<String> typesIntroduced = null;
		for (String commonSuperType : commonSuperTypes) {
			typesIntroduced = newSet(hbmEntityTypes.get(commonSuperType).getFlattenedSubTypesSignatures());
			typesIntroduced.removeAll(currentTypes);
			superTypesRank.put(commonSuperType, typesIntroduced.size());
		}

		if (superTypesRank.isEmpty())
			return emptyMap();
		else
			return CollectionsUtils.sortMapByValue(superTypesRank);
	}

	public Map<String, Integer> rankTypeHierarchiesByPropertiesInherited(Set<String> types) {
		Map<String, Integer> inheritedPropertiesRank = newMap(types.size());
		int propertiesIntroduced = -1;
		for (String commonSuperType : types) {
			propertiesIntroduced = (hbmEntityTypes.get(commonSuperType).getType().getProperties() != null)
					? hbmEntityTypes.get(commonSuperType).getType().getProperties().size() : 0;
			inheritedPropertiesRank.put(commonSuperType, propertiesIntroduced);
		}

		if (inheritedPropertiesRank.isEmpty())
			return emptyMap();
		else
			return CollectionsUtils.sortMapByValue(inheritedPropertiesRank, true);
	}

	/**
	 * Collects the intersection of types' super types
	 */
	private Set<String> collectCommonSuperTypes(Set<String> types) {
		Set<String> commonSuperTypes = null;
		for (String type : types) {
			if (commonSuperTypes == null)
				commonSuperTypes = newLinkedSet(hbmEntityTypes.get(type).getFlattenedSuperTypesSignatures());
			else
				commonSuperTypes.retainAll(hbmEntityTypes.get(type).getFlattenedSuperTypesSignatures());
		}
		return commonSuperTypes;
	}

	/**
	 * Collects an aggregation of types' sub types
	 */
	private Set<String> collectSubTypes(Set<String> types) {
		Set<String> commonSubTypes = null;
		for (String type : types) {
			if (commonSubTypes == null)
				commonSubTypes = newLinkedSet(hbmEntityTypes.get(type).getFlattenedSubTypesSignatures());
			else
				commonSubTypes.addAll(hbmEntityTypes.get(type).getFlattenedSubTypesSignatures());
		}
		return commonSubTypes;
	}

	private void bindFlattenedSuperTypes(HbmEntityType hbmEntityType) {
		bindFlattenedSuperTypes(hbmEntityType, hbmEntityType.getType().getSuperTypes());
	}

	private void bindFlattenedSuperTypes(HbmEntityType hbmEntityType, List<GmEntityType> superTypes) {
		for (GmEntityType superType : CollectionsUtils.nullSafe(superTypes)) {
			if (!hbmEntityType.getFlattenedSuperTypes().contains(superType)) {
				hbmEntityType.getFlattenedSuperTypes().add(superType);
				hbmEntityType.getFlattenedSuperTypesSignatures().add(superType.getTypeSignature());
				bindFlattenedSuperTypes(hbmEntityType, superType.getSuperTypes());
			}
		}
	}

	private Set<String> extractSubTypeSignatures(String typeSignature, Set<String> typeSignatures) {
		if (typeSignatures == null)
			typeSignatures = newLinkedSet();

		for (HbmEntityType hbmSubEntityType : CollectionsUtils.nullSafe(hbmEntityTypes.get(typeSignature).getSubTypes())) {
			typeSignatures.add(hbmSubEntityType.getType().getTypeSignature());
			extractSubTypeSignatures(hbmSubEntityType.getType().getTypeSignature(), typeSignatures);
		}

		return typeSignatures;
	}

	private Set<String> extractSubTypeSignatures(String typeSignature) {
		return extractSubTypeSignatures(typeSignature, null);
	}

	private void bindFlattenedSubTypes(HbmEntityType hbmEntityType) {
		hbmEntityType.setFlattenedSubTypesSignatures(extractSubTypeSignatures(hbmEntityType.getType().getTypeSignature()));
	}

	private void bindFlattenedHierarchy(HbmEntityType hbmEntityType) {
		bindFlattenedSuperTypes(hbmEntityType);
		bindFlattenedSubTypes(hbmEntityType);
	}

	/**
	 * <p>
	 * Creates a map of HbmEntityType objects {@link #hbmEntityTypes} based on the {@link GmEntityType} objects provided by
	 * {@link HbmXmlGenerationContext} {@link #context}
	 * 
	 * <p>
	 * The creation of {@link HbmEntityType} includes the sub type list generation based on the existing super types list from {@link GmEntityType}
	 */
	private void buildHbmEntityTypeMap() throws UnmappableModelException {
		for (GmEntityType gmEntityType : context.getEntityTypes()) {
			String typeSignature = gmEntityType.getTypeSignature();

			if (hbmEntityTypes.containsKey(typeSignature) || isSkipped(gmEntityType) || embeddableTypes.contains(gmEntityType))
				continue;

			hbmEntityTypes.put(typeSignature, new HbmEntityType(gmEntityType));
		}
		buildHbmEntitySubTypeLists();
	}

	/**
	 * <p>
	 * Traverses obtained entities {@link #hbmEntityTypes} building the sub type list.
	 * 
	 * <p>
	 * Each entity adds itself to the list of sub types {@link HbmEntityType#subTypes} of each supertype mapped contained in
	 * {@link GmEntityType#getSuperTypes()}
	 */
	private void buildHbmEntitySubTypeLists() throws UnmappableModelException {
		for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet()) {
			HbmEntityType hbmEntityType = hbmEntityTypeEntry.getValue();

			for (GmEntityType superType : CollectionsUtils.nullSafe(hbmEntityType.getType().getSuperTypes())) {
				if (superType == null)
					continue;

				if (isSkipped(superType))
					continue;

				HbmEntityType superHbmEntityType = hbmEntityTypes.get(superType.getTypeSignature());

				if (superHbmEntityType == null)
					throw new UnmappableModelException("Unmappable model: Type \"" + hbmEntityType.getType().getTypeSignature()
							+ "\" references a super type which is not known to the meta model: \"" + superType.getTypeSignature() + "\".");

				superHbmEntityType.getSubTypes().add(hbmEntityType);
			}
		}
	}

	/**
	 * <p>
	 * Collects the signature of types referenced in entities' members
	 */
	private Set<String> memberReferencedTypes() throws UnmappableModelException {
		Set<String> typesWithAllPropsSkipped = unmappedTypesWithNoMappedSubType();
		
		Set<String> memberReferencedTypes = newSet();
		for (GmEntityType gmEntityType : context.getEntityTypes()) {

			String entitySignature = gmEntityType.getTypeSignature();
			if (typesWithAllPropsSkipped.contains(entitySignature)) {
				log.debug(() -> "Type [ " + entitySignature + " ] was marked not to be mapped and none of its subtypes is mapped either, "
						+ "so its properties won't be taken into consideration for the member-referenced types selection.");
				continue;
			}

			GmType gmType = null;
			for (GmProperty gmProperty : CollectionsUtils.nullSafe(gmEntityType.getProperties())) {

				if (isSkipped(gmEntityType, gmProperty))
					continue;

				GmType propertyType = gmProperty.getType();

				if (propertyType instanceof GmMapType)
					gmType = ((GmMapType) propertyType).getValueType();

				// obtain the gmType from direct member or collection element
				if (propertyType instanceof GmLinearCollectionType)
					gmType = ((GmLinearCollectionType) propertyType).getElementType();
				else
					gmType = propertyType;

				/* if gmType is an embeddable entity, we don't check if it's mapped (in face, we could maybe check it's not mapped and also check if
				 * we use either an xml snippet or JpaEmbedded MD for property mapping) */
				if (embeddableTypes.contains(gmType))
					continue;

				String gmTypeSignature = gmType.getTypeSignature();

				if (isSkipped(gmType)) {
					throw new UnmappableModelException(entitySignature + ":" + gmProperty.getName() + " is of type "
							+ gmType.getTypeSignature() + " which was marked not to be mapped");
				}

				// non-entity types and types already mapped are not considered
				if (!gmType.isGmEntity() || memberReferencedTypes.contains(gmTypeSignature))
					continue;

				if (gmTypeSignature.equals(GenericEntity.class.getName()))
					throw new UnmappableModelException("Unmappable model: \"" + entitySignature + ":" + gmProperty.getName()
							+ "\" is of type " + gmTypeSignature);

				memberReferencedTypes.add(gmTypeSignature);
			}
		}
		return memberReferencedTypes;
	}

	private Set<String> unmappedTypesWithNoMappedSubType() {
		HashSet<String> result = newSet(skippedEntities);
		for (HbmEntityType hbmEntityType : hbmEntityTypes.values())
			if (!isSkipped(hbmEntityType.getType()))
				result.removeAll(hbmEntityType.getFlattenedSuperTypesSignatures());

		return result;
	}

	private String generateEntityTree(boolean singleInheritance) {
		StringBuilder entityTree = new StringBuilder();

		for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet()) {

			if (!hbmEntityTypeEntry.getValue().getIsTopLevel())
				continue;

			entityTree.append(hbmEntityTypeEntry.getValue().getType().getTypeSignature()).append("\r\n");

			generateEntityTree(hbmEntityTypeEntry.getValue(), 1, entityTree, singleInheritance);
		}

		return entityTree.toString();
	}

	private void generateEntityTree(HbmEntityType hbmEntityType, int depth, StringBuilder entityTree, boolean singleInheritance) {
		for (HbmEntityType hbmEntitySubType : hbmEntityType.getSubTypes()) {

			if (singleInheritance && !hbmEntitySubType.getSuperType().equals(hbmEntityType))
				continue;

			entityTree.append(StringUtils.repeat("\t", depth) + " - " + hbmEntitySubType.getType().getTypeSignature()).append("\r\n");

			if (!hbmEntitySubType.getSubTypes().isEmpty())
				generateEntityTree(hbmEntitySubType, depth + 1, entityTree, singleInheritance);
		}
	}

	private boolean isSkipped(GmType e) {
		return skippedEntities.contains(e.getTypeSignature());
	}

	private boolean isSkipped(GmEntityType e, GmProperty p) {
		return skippedProperties.contains(e.getTypeSignature() + ":" + p.getName());
	}

}
