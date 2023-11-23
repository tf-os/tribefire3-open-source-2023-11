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
package com.braintribe.model.access.hibernate.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.stream.Collectors.toCollection;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;

import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;

/**
 * @author peter.gazdik
 */
public class HibernateMappingInfoProvider {

	private final Map<String, Set<String>> mappedPropertiesByEntity = newMap();
	private final Set<Property> mappedProperties = newSet();
	private final Set<String> compositeIdEntityTypes = newSet();

	public HibernateMappingInfoProvider(EntityManagerFactory emFactory) {
		Metamodel metamodel = emFactory.getMetamodel();

		for (javax.persistence.metamodel.EntityType<?> javaxEntityType : metamodel.getEntities())
			index(javaxEntityType);
	}

	private void index(javax.persistence.metamodel.EntityType<?> javaxEntityType) {
		String entityName = javaxEntityType.getJavaType().getName();

		if (hasCompositeId(javaxEntityType))
			compositeIdEntityTypes.add(entityName);

		Set<? extends Attribute<?, ?>> attributes = javaxEntityType.getAttributes();
		Set<String> propertyNamesSet = attributes.stream().map(Attribute::getName).collect(Collectors.toSet());

		mappedPropertiesByEntity.put(entityName, propertyNamesSet);

		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityName);

		propertyNamesSet.stream().map(this::ensureGmPropertyName) //
				.map(entityType::getProperty) //
				.collect(toCollection(() -> mappedProperties));
	}

	private boolean hasCompositeId(javax.persistence.metamodel.EntityType<?> javaxEntityType) {
		return javaxEntityType.getIdType().getJavaType() == CompositeIdValues.class;
	}

	public boolean isEntityMapped(GmEntityType gmEntityType) {
		return mappedPropertiesByEntity.containsKey(gmEntityType.getTypeSignature());
	}

	public boolean isEntityMapped(EntityType<?> entityType) {
		return mappedPropertiesByEntity.containsKey(entityType.getTypeSignature());
	}

	public boolean isPropertyMapped(String ownerTypeSignature, String propertyName) {
		Set<String> props = mappedPropertiesByEntity.get(ownerTypeSignature);
		return props != null && props.contains(propertyName);
	}

	public boolean isPropertyMapped(Property property) {
		return mappedProperties.contains(property);
	}

	public boolean hasCompositeId(String typeSignature) {
		return compositeIdEntityTypes.contains(typeSignature);
	}

	/* Reverts ReflectionTools.ensureValidJavaBeansName */
	private String ensureGmPropertyName(String propertyName) {
		if (propertyName.length() > 0 && Character.isUpperCase(propertyName.charAt(0))) {
			return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
		}
		return propertyName;
	}

}
