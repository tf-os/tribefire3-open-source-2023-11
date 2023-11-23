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
package com.braintribe.model.processing.deployment.hibernate.mapping.hints;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.CollectionPropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.PropertyDescriptor;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.StringTools;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EntityHintPersistence {

	private final HbmXmlGenerationContext context;
	private ObjectMapper mapper;
	
	private static final Logger log = Logger.getLogger(EntityHintPersistence.class);

	public EntityHintPersistence(HbmXmlGenerationContext context) { 
		if (log.isTraceEnabled())
			log.trace(getClass().getName()+" instantiated");
		this.context = context;
	}
	
	public void persist(Collection<EntityDescriptor> entityDescriptors) {
		
		//avoid unnecessary initialization
		if (context.typeHintsOutputFile == null || CollectionTools.isEmpty(entityDescriptors))
			return;

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		}

		try {
			mapper.writeValue(context.typeHintsOutputFile, generateEntityHints(entityDescriptors));
		} catch (Exception e) {
			log.error("failed to write entity hints to typeHintsOutputFile: [ "+context.typeHintsOutputFile+" ]", e);
		}
	}
	
	protected Map<String, EntityHint> generateEntityHints(Collection<EntityDescriptor> entityDescriptors) {
		
		Map<String, EntityHint> outputHints = new HashMap<String, EntityHint>(entityDescriptors.size());
		
		for (EntityDescriptor entityDescriptor : entityDescriptors) {
			
			EntityHint entityHint = new EntityHint();
			entityHint.table = entityDescriptor.getTableName();
			entityHint.discColumn = entityDescriptor.getDiscriminatorColumnName();
			entityHint.discFormula = entityDescriptor.getDiscriminatorFormula();
			
			int propertiesSize = 
					(entityDescriptor.getIdProperty() != null ? 1 : 0) + 
					(entityDescriptor.getProperties() != null ? entityDescriptor.getProperties().size() : 0);
			
			if (propertiesSize > 0) {
				entityHint.properties = new HashMap<>(propertiesSize);
				
				if (entityDescriptor.getIdProperty() != null) {
					entityHint.properties.put(entityDescriptor.getIdProperty().getName(), createPropertyHint(entityDescriptor.getIdProperty()));
				}
				
				if (entityDescriptor.getProperties() != null) {
					for (PropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
						entityHint.properties.put(propertyDescriptor.getName(), createPropertyHint(propertyDescriptor));
					}
				}
			}
			
			outputHints.put(entityDescriptor.getFullName(), entityHint);
		}
	
		return outputHints;
	}
	
	private static PropertyHint createPropertyHint(PropertyDescriptor propertyDescriptor) {
		
		PropertyHint propertyHint = new PropertyHint();
		
		fillBasicPropertyHints(propertyHint, propertyDescriptor);
		
		if (propertyDescriptor instanceof CollectionPropertyDescriptor) 
			fillCollectionPropertyHints(propertyHint, (CollectionPropertyDescriptor)propertyDescriptor);
		
		return propertyHint;
	}
	
	
	private static void fillBasicPropertyHints(PropertyHint propertyHint, PropertyDescriptor propertyDescriptor) {

		if (!StringTools.isEmpty(propertyDescriptor.getExplicitType()))
			propertyHint.type = propertyDescriptor.getExplicitType();
		
		if (!StringTools.isEmpty(propertyDescriptor.getColumnName()))
			propertyHint.column = propertyDescriptor.getColumnName();
		
		if (propertyDescriptor.getLength() != null) 
			propertyHint.length = propertyDescriptor.getLength();
		
		if (propertyDescriptor.getPrecision() != null) 
			propertyHint.precision = propertyDescriptor.getPrecision();
		
		if (propertyDescriptor.getScale() != null) 
			propertyHint.scale = propertyDescriptor.getScale();
		
		if (propertyDescriptor.getIsUnique() != null) 
			propertyHint.unique = propertyDescriptor.getIsUnique();

		if (propertyDescriptor.getIsNotNull() != null) 
			propertyHint.notNull = propertyDescriptor.getIsNotNull();

		if (propertyDescriptor.getUniqueKey() != null) 
			propertyHint.uniqueKey = propertyDescriptor.getUniqueKey();

		if (propertyDescriptor.getIndex() != null) 
			propertyHint.index = propertyDescriptor.getIndex();
		
		if (!StringTools.isEmpty(propertyDescriptor.getGeneratorClass())) 
			propertyHint.idGeneration  = propertyDescriptor.getGeneratorClass();
		
		if (!StringTools.isEmpty(propertyDescriptor.getLazy())) 
			propertyHint.lazy  = propertyDescriptor.getLazy();
		
		if (!StringTools.isEmpty(propertyDescriptor.getFetch())) 
			propertyHint.fetch  = propertyDescriptor.getFetch();
		
		if (!StringTools.isEmpty(propertyDescriptor.getForeignKey())) 
			propertyHint.foreignKey  = propertyDescriptor.getForeignKey();
		
	}
	
	
	private static void fillCollectionPropertyHints(PropertyHint propertyHint, CollectionPropertyDescriptor collectionPropertyDescriptor) {

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getMany2ManyTable()))
			propertyHint.table = collectionPropertyDescriptor.getMany2ManyTable();
		
		if (!StringTools.isEmpty(collectionPropertyDescriptor.getElementSimpleType()))
			propertyHint.type = collectionPropertyDescriptor.getElementSimpleType();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getMapKeyExplicitType()))
			propertyHint.keyType = collectionPropertyDescriptor.getMapKeyExplicitType();

		if (collectionPropertyDescriptor.getMapKeyLength() != null)
			propertyHint.keyLength = collectionPropertyDescriptor.getMapKeyLength();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getKeyColumn()))
			propertyHint.keyColumn = collectionPropertyDescriptor.getKeyColumn();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getKeyPropertyRef()))
			propertyHint.keyPropertyRef = collectionPropertyDescriptor.getKeyPropertyRef();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getIndexColumn()))
			propertyHint.indexColumn = collectionPropertyDescriptor.getIndexColumn();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getMapKeyColumn()))
			propertyHint.mapKeyColumn = collectionPropertyDescriptor.getMapKeyColumn();
		
		if (!StringTools.isEmpty(collectionPropertyDescriptor.getMapKeyForeignKey())) 
			propertyHint.mapKeyForeignKey  = collectionPropertyDescriptor.getMapKeyForeignKey();
		
		if (!StringTools.isEmpty(collectionPropertyDescriptor.getElementColumn()))
			propertyHint.elementColumn = collectionPropertyDescriptor.getElementColumn();
		
		if (!StringTools.isEmpty(collectionPropertyDescriptor.getElementForeignKey())) 
			propertyHint.elementForeignKey  = collectionPropertyDescriptor.getElementForeignKey();
		
		if (collectionPropertyDescriptor.getIsOneToMany())
			propertyHint.oneToMany = collectionPropertyDescriptor.getIsOneToMany();

		if (!StringTools.isEmpty(collectionPropertyDescriptor.getManyToManyFetch()))
			propertyHint.manyToManyFetch = collectionPropertyDescriptor.getManyToManyFetch();
		
	}
	
	

}
