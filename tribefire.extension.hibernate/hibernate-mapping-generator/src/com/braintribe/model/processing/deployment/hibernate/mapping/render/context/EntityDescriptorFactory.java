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
package com.braintribe.model.processing.deployment.hibernate.mapping.render.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.UnmappableModelException;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.EntityHint;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.EntityHintProvider;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.CollectionsUtils;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType;
import com.braintribe.model.processing.deployment.hibernate.mapping.wrapper.HbmEntityType.EntityTypeCategory;

/**
 * Converts {@link HbmEntityType}(s) into {@link EntityDescriptor}(s)
 * 
 */
public class EntityDescriptorFactory {

	private final HbmXmlGenerationContext context;
	private final EntityHintProvider entityHintProvider;
	private final Map<String, EntityDescriptor> entityDescriptors;
	private final Map<String, Map<String, GmProperty>> collectedProperties;

	private static final Logger log = Logger.getLogger(EntityDescriptorFactory.class);

	public EntityDescriptorFactory(HbmXmlGenerationContext context) {
		this.context = context;
		this.entityHintProvider = context.getEntityHintProvider();
		this.entityDescriptors = newTreeMap();
		this.collectedProperties = newMap();
	}

	public Collection<EntityDescriptor> createEntityDescriptors(Map<String, HbmEntityType> hbmEntityTypes) {
		for (Map.Entry<String, HbmEntityType> hbmEntityTypeEntry : hbmEntityTypes.entrySet())
			if (hbmEntityTypeEntry.getValue().getIsTopLevel()) {
				EntityDescriptor entityDescriptor = buildEntityDescriptor(null, hbmEntityTypeEntry.getValue());
				generateEntitySubTypeMappings(entityDescriptor, hbmEntityTypeEntry.getValue());
			}

		return entityDescriptors.values();
	}

	/**
	 * Creates {@link EntityDescriptor} instances for the sub types of the given {@link HbmEntityType}
	 * 
	 * @param hbmEntityType
	 *            Super type of origin for the sub types' {@link EntityDescriptor} instances to be created
	 */
	private void generateEntitySubTypeMappings(EntityDescriptor parent,  HbmEntityType hbmEntityType) {
		for (HbmEntityType hbmEntitySubType : hbmEntityType.getSubTypes()) {

			// ignore the generation if the given type is not the single elected super type of this subtype
			if (hbmEntitySubType.getSuperType() != null && !hbmEntityType.equals(hbmEntitySubType.getSuperType())) {
				log.info(() -> "Although " + hbmEntitySubType.getType().getTypeSignature() + " is among the sub types of "
						+ hbmEntityType.getType().getTypeSignature() + ", this type is not its single elected hibernate super type: "
						+ hbmEntitySubType.getSuperType().getType().getTypeSignature());
				continue;
			}

			EntityDescriptor entityDescriptor = buildEntityDescriptor(parent, hbmEntitySubType);

			generateEntitySubTypeMappings(entityDescriptor, hbmEntitySubType);
		}
	}

	private EntityDescriptor buildEntityDescriptor(EntityDescriptor parent, HbmEntityType hbmEntityType) {
		String typeSignature = hbmEntityType.getType().getTypeSignature();
		EntityHint entityHint = entityHintProvider.provide(typeSignature);
		EntityDescriptor entityDescriptor = createEntityDescriptor(parent, hbmEntityType, entityHint);

		if (entityDescriptor.getXml() == null)
			buildProperties(entityDescriptor);

		entityDescriptors.put(typeSignature, entityDescriptor);

		return entityDescriptor;
	}

	/** Creates a {@link EntityDescriptor} based on the given arguments. */
	private EntityDescriptor createEntityDescriptor(EntityDescriptor parent, HbmEntityType hbmEntityType, EntityHint entityHint) {
		return new EntityDescriptor(parent, hbmEntityType, entityHint, context);
	}

	/**
	 * Creates {@link PropertyDescriptor} instances for the given {@link EntityDescriptor}.
	 * <p>
	 * After invocation, the created {@link PropertyDescriptor} instances are accessible through {@link EntityDescriptor#getProperties()}
	 */
	private void buildProperties(EntityDescriptor entityDescriptor) {
		List<PropertyDescriptor> commonProperties = newList();

		Collection<GmProperty> hibernateProperties = getHbmProperties(entityDescriptor);
		for (GmProperty gmProperty : hibernateProperties) {
			String propertyName = gmProperty.getName();

			if (!GenericEntity.id.equals(propertyName)) {
				commonProperties.add(createPropertyDescriptor(entityDescriptor, gmProperty));
				log.trace(() -> propertyName + " property was added as a regular property into " + entityDescriptor.getFullName() + " descriptor");

			} else {
				if (!entityDescriptor.getIsTopLevel()) {
					log.debug(() -> gmProperty.nameWithOrigin() + " was ignored as it is an id property in a subclass");
					continue;
				}

				// id properties shall always be the first added to the entity descriptor
				PropertyDescriptor idPropertyDescriptor = createPropertyDescriptor(entityDescriptor, gmProperty);
				entityDescriptor.setIdProperty(idPropertyDescriptor);
				entityDescriptor.getProperties().add(idPropertyDescriptor);

				log.trace(() -> propertyName + " property was added as id into top level " + entityDescriptor.getFullName() + " descriptor");
			}
		}

		if (entityDescriptor.getIsTopLevel() && entityDescriptor.getIdProperty() == null)
			throw new UnmappableModelException("Unmappable model: Id property not found for top-level entity: " + entityDescriptor.getFullName());

		// non-id properties shall always be added after a possible id property
		entityDescriptor.getProperties().addAll(commonProperties);

	}

	/** Creates a {@link PropertyDescriptor} based on given arguments. */
	private PropertyDescriptor createPropertyDescriptor(EntityDescriptor entityDescriptor, GmProperty gmProperty) {
		return PropertyDescriptor.create(context, entityDescriptor, gmProperty);
	}

	private boolean isPropertyMappable(HbmEntityType hbmEntityType, EntityHint entityHint, GmProperty gmProperty) {
		if (gmProperty == null)
			return false;

		if (!MappingHelper.mapPropertyToDb(hbmEntityType.getType(), gmProperty, entityHint, context.getMappingMetaDataResolver())) {
			log.debug(() -> hbmEntityType.getType().getTypeSignature() + "." + gmProperty.getName()
					+ " property was ignored as it was marked not to be mapped");
			return false;
		}

		return true;
	}

	/**
	 * Returns the GmProperty(s) eligible to participate in list of properties for the given hibernate entity ({@link HbmEntityType}).
	 * <p>
	 * This method does not rely overlaid GmProperty(s), as they might not be present, therefore, only GmProperty(s) where isOverlay is {@code false}
	 * are taken into consideration.
	 * 
	 * @return A collection of GmProperty(s), sorted by the name property
	 */
	private Collection<GmProperty> getHbmProperties(EntityDescriptor entityDescriptor) {
		HbmEntityType hbmEntityType = entityDescriptor.getHbmEntityType();
		EntityHint entityHint = entityDescriptor.getEntityHint();

		if (hbmEntityType.getTypeCategory() == EntityTypeCategory.UNMAPPED)
			return Collections.emptySet();

		/* collects properties already present in hibernate hierarchy */

		Map<String, GmProperty> alreadyInheritedProperties = null;
		HbmEntityType superType = hbmEntityType.getSuperType();
		Map<String, GmProperty> superTypeProperties;
		while (superType != null) {
			superTypeProperties = collectedProperties.get(superType.getType().getTypeSignature());
			if (superTypeProperties != null) {
				if (alreadyInheritedProperties == null)
					alreadyInheritedProperties = superTypeProperties;
				else
					alreadyInheritedProperties.putAll(superTypeProperties);
			}
			superType = superType.getSuperType();
		}

		/* collects properties from non-participants in hibernate hierarchy */
		Map<String, GmProperty> inheritedProperties = collectInheritableProperties(hbmEntityType, entityHint, alreadyInheritedProperties);

		/* collects the directly defined properties, if not-overlaid and non-already-inherited */
		Map<String, GmProperty> directProperties = newMap();

		for (GmProperty gmProperty : CollectionsUtils.nullSafe(hbmEntityType.getType().getProperties())) {

			// always check isPropertyMappable first, must stop at this point if the property is overlaid
			if (!isPropertyMappable(hbmEntityType, entityHint, gmProperty))
				// property should not be taken into consideration at all
				continue;

			if (alreadyInheritedProperties != null && alreadyInheritedProperties.containsKey(gmProperty.getName()))
				// property with same name is already present in hibernate hierarchy
				continue;

			if (inheritedProperties.containsKey(gmProperty.getName()))
				// property with same name was already copied from a non-hibernate super type
				continue;

			directProperties.put(gmProperty.getName(), gmProperty);
		}

		logTypeWithCollectedProperties(hbmEntityType, inheritedProperties, directProperties);

		TreeMap<String, GmProperty> typeProperties = newTreeMap();
		typeProperties.putAll(directProperties);
		typeProperties.putAll(inheritedProperties);

		collectedProperties.put(hbmEntityType.getType().getTypeSignature(), typeProperties);

		return typeProperties.values();
	}

	/** Returns the GmProperty(s) that must be copied to hbmEntityType's mapping */
	private Map<String, GmProperty> collectInheritableProperties(HbmEntityType hbmEntityType, EntityHint entityHint,
			Map<String, GmProperty> superTypeProperties) {

		Map<String, GmProperty> inheritedProperties = newMap();

		for (GmEntityType superType : CollectionsUtils.nullSafe(hbmEntityType.getFlattenedSuperTypes())) {
			if (superType == null)
				continue;

			for (GmProperty gmProperty : CollectionsUtils.nullSafe(superType.getProperties())) {

				// always check isPropertyMappable first, must stop at this point if the property is overlaid
				if (!isPropertyMappable(hbmEntityType, entityHint, gmProperty))
					continue;

				if (superTypeProperties != null && superTypeProperties.containsKey(gmProperty.getName())) {
					log.trace(() -> "Property " + gmProperty + " is already present in type [ " + hbmEntityType.getType().getTypeSignature()
							+ " ] hibernate hierarchy");
					continue;
				}

				GmProperty preExistingInHierarchy = inheritedProperties.get(gmProperty.getName());
				if (preExistingInHierarchy != null) {
					log.debug(() -> "Property " + gmProperty + " was already collected from type [ " + hbmEntityType.getType().getTypeSignature()
							+ " ] hierarchy: " + preExistingInHierarchy);
					continue;
				}

				inheritedProperties.put(gmProperty.getName(), gmProperty);
			}
		}

		return inheritedProperties;
	}

	private void logTypeWithCollectedProperties(HbmEntityType hbmEntityType, Map<String, GmProperty> inheritedProperties,
			Map<String, GmProperty> directProperties) {

		if (!log.isTraceEnabled())
			return;

		if (inheritedProperties.isEmpty() && directProperties.isEmpty()) {
			log.trace("No property collected for hibernate type [ " + hbmEntityType.getType().getTypeSignature() + " ]");

		} else {
			StringBuilder sb = new StringBuilder("Properties collected for hibernate type [ " + hbmEntityType.getType().getTypeSignature() + " ]:");
			for (GmProperty p : inheritedProperties.values())
				sb.append("\n").append("inherited: ").append(p.nameWithOrigin());

			for (GmProperty p : directProperties.values())
				sb.append("\n").append("direct:    ").append(p.nameWithOrigin());

			log.trace(sb.toString());
		}
	}

}
