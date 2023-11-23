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
package com.braintribe.model.processing.generic.synchronize.experts;

import java.util.Collection;
import java.util.HashSet;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.generic.synchronize.GenericEntitySynchronizationException;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.SynchronizationContext;

/**
 * A generic {@link IdentityManager} implementation that can be configured to certain needs.
 */
public abstract class ConfigurableIdentityManager extends QueryingIdentityManager {

	protected static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	protected EntityType<? extends GenericEntity> responsibleFor;
	private Collection<String> excludedProperties = new HashSet<String>();
	private Collection<String> includedProperties = new HashSet<String>();
	protected boolean transferNullValues = false;
	
	public ConfigurableIdentityManager() {}
	
	public EntityType<? extends GenericEntity> getResponsibleFor() {
		return responsibleFor;
	}
	
	@Configurable
	public void setResponsibleFor(EntityType<? extends GenericEntity> responsibleFor) {
		this.responsibleFor = responsibleFor;
	}
	
	@Configurable
	public void setResponsibleFor(Class<? extends GenericEntity> responsibleForClass) {
		setResponsibleFor(typeReflection.getEntityType(responsibleForClass));
	}
	
	/**
	 * Properties specified in excludedProperties are not respected during synchronization
	 * and thus remain untouched in the target.<br /> Excluded properties are stronger then
	 * included properties which means specifying a property in both collections will result
	 * into an excluded property. 
	 */
	@Configurable
	public void setExcludedProperties(Collection<String> excludedProperties) {
		this.excludedProperties = excludedProperties;
	}

	/**
	 * Same as {@link #setExcludedProperties(Collection)} but adds the given elements
	 * to the existing collection.
	 */
	public void addExcludedProperties(Collection<String> excludedProperties) {
		this.excludedProperties.addAll(excludedProperties);
	}
	
	/**
	 * If includedProperties are specified only those properties are respected 
	 * during synchronization (except they are also specified in excludedProperties)
	 */
	@Configurable
	public void setIncludedProperties(Collection<String> includedProperties) {
		this.includedProperties = includedProperties;
	}
	
	/**
	 * Same as {@link #setIncludedProperties(Collection)} but adds the given elements
	 * to the existing collection.
	 */
	public void addIncludedProperties(Collection<String> includedProperties) {
		this.includedProperties.addAll(includedProperties);
	}

	@Configurable
	public void setTransferNullValues(boolean transferNullValues) {
		this.transferNullValues = transferNullValues;
	}

	/**
	 * Returns the collection of properties that should be included during synchronization. <br />
	 * Per default this method returns the collection internally stored with {@link #setIncludedProperties(Collection)} or {@link #addIncludedProperties(Collection)}.
	 * Sub classes of this class can override this method to build a dynamic collection based on the current entity.<br/>  
	 * Note, that this method should never return null but at least an empty collection.
	 */
	public Collection<String> getIncludedProperties(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
		return includedProperties;
	}
	
	/**
	 * Returns the collection of properties that should be excluded during synchronization. <br />
	 * Per default this method returns the collection internally stored with {@link #setExcludedProperties(Collection)} or {@link #addExcludedProperties(Collection)}.
	 * Sub classes of this class can override this method to build a dynamic collection based on the current entity.<br/>  
	 * Note, that this method should never return null but at least an empty collection.  
	 */
	public Collection<String> getExcludedProperties(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
		return excludedProperties;
	}
	
	/**
	 * Returns the collection of properties that should be used for identity management during synchronization. <br />
	 * Sub classes of this class needs to override this method to build a dynamic collection based on the current entity.<br/>  
	 * Note, that this method should never return null but at least an empty collection.  
	 */
	public abstract Collection<String> getIdentityProperties(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context);
	
	
	/**
	 * @see com.braintribe.model.processing.generic.synchronize.api.IdentityManager#isResponsible(com.braintribe.model.generic.reflection.EntityType)
	 */
	@Override
	public boolean isResponsible(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
		return responsibleFor.isAssignableFrom(entityType);
	}
	
	/**
	 * @see com.braintribe.model.processing.generic.synchronize.api.IdentityManager#findEntity(com.braintribe.model.processing.session.api.persistence.PersistenceGmSession, com.braintribe.model.generic.GenericEntity, com.braintribe.model.generic.reflection.EntityType, java.util.Set)
	 */
	@Override
	public GenericEntity findEntity(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) throws GenericEntitySynchronizationException {
		return query(context.getSession(), instance, entityType, getIdentityProperties(instance, entityType, context));
	}

	/**
	 * @see com.braintribe.model.processing.generic.synchronize.api.IdentityManager#canTransferProperty(com.braintribe.model.processing.session.api.persistence.PersistenceGmSession, com.braintribe.model.generic.GenericEntity, com.braintribe.model.generic.GenericEntity, com.braintribe.model.generic.reflection.EntityType, com.braintribe.model.generic.reflection.Property, java.util.Set)
	 */
	@Override
	public boolean canTransferProperty(GenericEntity instance, GenericEntity clonedInstance, EntityType<? extends GenericEntity> entityType, Property property, SynchronizationContext context) {
		
		if (context.foundInSession(clonedInstance) && getIdentityProperties(instance, entityType, context).contains(property.getName())) {
			// No need to transfer identity properties on already existing entities in target.
			return false;
		}
		
		Collection<String> includedProperties = getIncludedProperties(instance, entityType, context);
		Collection<String> excludedProperties = getExcludedProperties(instance, entityType, context);
		
		if (isIncluded(property, includedProperties, excludedProperties)) {
			
			Object sourceValue = property.get(instance);
			if (sourceValue == null && !transferNullValues) {
				// We should not transfer null values.
				return false;
			}
			
			// Property is included -> transfer
			return true;
		}
		// Property is not included -> Don't transfer. 
		return false;
	}

	/**
	 * Determines whether given property is included in the sync according configuration.
	 * A property is included if one of following conditions applies:
	 * <ul>
	 * <li>Neither includeProperties nor excludedProperties are specified at all.</li>
	 * <li>No includeProperties are specified and the property is NOT specified in excludeProperties.</li>
	 * <li>The property is specified in includeProperties and NOT specified in excludeProperties.</li>
	 * </ul>
	 * 
	 */
	protected boolean isIncluded(Property property, Collection<String> includedProperties, Collection<String> excludedProperties) {
		
		// Neither includeProperties nor excludedProperties are specified at all -> Included
		if (includedProperties.isEmpty() && excludedProperties.isEmpty()) {
			return true;
		}
		
		String propertyName = property.getName();
		
		if (excludedProperties.contains(propertyName)) {
			// Property contained in exclusion. Exclusion is stronger then include. Thus property is not included.
			return false;
		}
		
		// Property is not excluded. 
		// Now check whether no explicit includes are given or the property is explicitly included. 
		return (includedProperties.isEmpty() || includedProperties.contains(propertyName));
	}



}
