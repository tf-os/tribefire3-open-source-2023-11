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
package com.braintribe.model.access.inmemoryaccesstest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.utils.LongIdGenerator;

/**
 * Simple access implementation that maintains a in-memory collection of {@link GenericEntity}.
 */
public class InMemoryAccess extends BasicAccessAdapter {
    private final Collection<GenericEntity> storage = new HashSet<>();
    
    
    
    private boolean overrideAssignedIds = false;
    
    public InMemoryAccess() {
		
	}
    
    public void setOverrideAssignedIds(boolean overrideAssignedIds) {
		this.overrideAssignedIds = overrideAssignedIds;
	}
    
    /**
     * Returns the current population.<br />
     * This method is called by the {@link #queryEntities(com.braintribe.model.query.EntityQuery)} of the BasicAccessAdapter. <br />
     * The BasicAccessAdapter then runs the actual query processing on the collection returned by this method.
     */
    @Override
    protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {
        return this.storage;
    }
    /**
     * Inspects the given ManipulationReport and updates existing entities in the storage and adds newly created entities.
     */
    @Override
    protected void save(AdapterManipulationReport context) throws ModelAccessException {
        /*
         * Handle new entities
         */
        for (GenericEntity entity : context.getCreatedEntities()) {
            /*
             * Adding new entities to the in-memory storage.
             */
            this.storage.add(entity);
            
           	/*
           	 * Creating an id based on the Id-Property type. Only String and Long is supported.
           	 */
           	setIdForEntity(entity);
        }
        /*
         * Handle updated entities by iterating through map of touched properties.
         */
        for (Map.Entry<GenericEntity, Set<Property>> updateEntry : context.getTouchedPropertiesOfEntities().entrySet()) {
            GenericEntity updatedEntity = updateEntry.getKey();
            Set<Property> updatedProperties = updateEntry.getValue();
            /*
             * Get the EntityType and the EntityReference of current entity from the TypeReflection
             */
            EntityType<GenericEntity> entityType = updatedEntity.entityType();
            EntityReference updatedEntityReference = updatedEntity.reference();
            /*
             * Lookup the entity from the in-memory storage by using the EntityRefernce
             */
            GenericEntity entityFromPopulation = getEntityFromPopulation(updatedEntityReference);
            if (entityFromPopulation == null) {
                throw new ModelAccessException("No entity with reference: " + updatedEntityReference + " found.");
            }
            /*
             * Iterate through list of updated properties and update each of the properties of current entity by using the EntityType.
             */
            for (Property updatedProperty : updatedProperties) {
                String propertyName = updatedProperty.getName();
                // Get the property value by inspecting the updated property.
                Object propertyValue = updatedProperty.get(updatedEntity);
                //updatedProperty.setProperty(entity, value);
                // Update property using Reflection
                
                // PGA: could we use updatedProperty.setProperty()? I am not sure, as the code I replaces was not doing it...
                entityType.getProperty(propertyName).set(entityFromPopulation, propertyValue);
            }
        }
    }
    /**
     * Internal method that runs through the in-memory storage and searches for an entity matching the typeSignature and id of parsed entityReference.
     *
     * @return the entity matching the entityReference. Null if no entity is found.
     */
    protected GenericEntity getEntityFromPopulation(EntityReference entityReference) {
        for (GenericEntity entity : storage) {
            EntityReference currentReference = entity.reference();
            if (currentReference.getTypeSignature().equals(entityReference.getTypeSignature()) && currentReference.getRefId().equals(entityReference.getRefId())) {
                return entity;
            }
        }
        return null;
    }
    
	/**
	 * Generates and sets an id for the passed entity, unless the id is already set. Supports id generation for id
	 * properties of type String and long. <br />
	 * For Strings a UUID will be generated. <br />
	 * For longs the currenTimeMillis will be generated.
	 *
	 * @param entity
	 *            The entity which should get the id property set.
	 */
	protected void setIdForEntity(GenericEntity entity) {
		Object id = entity.getId();
		if (overrideAssignedIds || id == null) {
			entity.setId(LongIdGenerator.provideLongId());
		}
	}
    
    @Override
	protected Iterable<GenericEntity> queryPopulation(String typeSignature, Condition condition, Ordering ordering) throws ModelAccessException {
        Iterable<GenericEntity> population = queryPopulation(typeSignature, condition);
        if (ordering == null) {
            return population;
        } else {
            throw new UnsupportedOperationException("Method 'BasicAccessAdapter.queryPopulation' does not support ordering yet!");
        }
    }
    @Override
    public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
        return super.queryEntities(query);
    }
}
