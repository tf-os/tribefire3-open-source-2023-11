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
package com.braintribe.model.processing.generic.synchronize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.builder.SynchronizationResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Abstract implementation of {@link GenericEntitySynchronization}   
 */
public abstract class AbstractSynchronization<S extends GenericEntitySynchronization> implements GenericEntitySynchronization {

	private static Logger logger = Logger.getLogger(AbstractSynchronization.class);
	
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private static BaseType baseType = typeReflection.getBaseType();
	
	private final Collection<GenericEntity> entities = new ArrayList<GenericEntity>();
	private PersistenceGmSession session;
	private final IdentityManagerRegistry identityManagerRegistry = new IdentityManagerRegistry();
	private boolean includeIdProperties = false;
	private boolean commitAfterSynchronization = false;
	
	/**
	 * Private constructor. Use {@link #newInstance()} to get an instance of the {@link AbstractSynchronization}.
	 */
	protected AbstractSynchronization(boolean withDefaultIdentityManagers) {
		super();
		if (withDefaultIdentityManagers) {
			addDefaultIdentityManagers();
		}
	}
	
	/* ************************************************************
	 * Interface implementations
	 * ************************************************************/
	
	/**
	 * Adds the given entity to the collection of entities that should be synchronized.
	 */
	@Override
	public S addEntity(GenericEntity entity) {
		this.entities.add(entity);
		return self();
	}
	
	/**
	 * Adds the given entities to the collection of entities that should be synchronized.
	 */
	@Override
	public S addEntities(Collection<? extends GenericEntity> entities) {
		this.entities.addAll(entities);
		return self();
	}
	
	/**
	 * Can be called to clear all entities provided before. This is typically used when the same <br />
	 * instance of {@link GenericEntitySynchronization} is used multiple times in a row.
	 */
	@Override
	public S clearEntities() {
		this.entities.clear();
		return self();
	}
	
	/**
	 * Sets the target {@link PersistenceGmSession} for the synchronization. 
	 * @return
	 */
	@Override
	public S session(PersistenceGmSession session) {
		this.session = session;
		return self();
	}
	
	/**
	 * Adds the given {@link IdentityManager} that should be used during synchronization.
	 */
	@Override
	public S addIdentityManager(IdentityManager identityManager) {
		this.identityManagerRegistry.addIdentityManager(identityManager);
		return self();
	}

	/**
	 * Adds the given {@link IdentityManager}'s that should be used during synchronization.
	 */
	@Override
	public S addIdentityManagers(Collection<IdentityManager> identityManagers) {
		this.identityManagerRegistry.addIdentityManagers(identityManagers);
		return self();
	}

	@Override
	public S addDefaultIdentityManagers() {
		this.identityManagerRegistry.addIdentityManagers(BasicIdentityManagers.defaultManagers);
		return self();
	}

	
	@Override
	public S includeIdProperties() {
		this.includeIdProperties = true;
		return self();
	}
	
	@Override
	public S commitAfterSynchronization() {
		this.commitAfterSynchronization = true;
		return self();
	}
	
	/**
	 * Synchronizes the given entities based on the given identity management strategies.
	 */
	@Override
	public SynchronizationResultConvenience synchronize() throws GenericEntitySynchronizationException {
		if (session == null) {
			throw new GenericEntitySynchronizationException("No target session provided.");
		}
		
		try {
			logger.debug("Start synchronization of cortex entities.");
			SynchronizeCloningContext synchronizeCloningContext = 
					new SynchronizeCloningContext(new BasicSynchronizationContext(session));
			
			baseType.clone(
					synchronizeCloningContext,
					entities, 
					StrategyOnCriterionMatch.skip);
			
			logger.debug("Finished synchronization of cortex entities.");
			
			if (commitAfterSynchronization) {
				session.commit();
			}
			
			final List<GenericEntity> synchronizedEntities = new ArrayList<GenericEntity>();
			for (GenericEntity initialEntity : entities) {
				GenericEntity synchronizedEntity = synchronizeCloningContext.getAssociated(initialEntity);
				synchronizedEntities.add(synchronizedEntity);
			}
			
			
			return new SynchronizationResultConvenience() {
				
				@Override
				public <E extends GenericEntity> E first() {
					List<E> list = list();
					
					if (list.isEmpty())
						return null;
					else
						return list.get(0);

				}
				
				@Override
				public <E extends GenericEntity> E unique() {
					List<E> list = list();
					switch (list.size()) {
					case 0:
						return null;
					case 1:
						return list.get(0);
					default:
						throw new GenericEntitySynchronizationException("query returned not exactly one result but " + list.size());
					}
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public <E extends GenericEntity> List<E> list() {
					return (List<E>) synchronizedEntities;
				}
			};
			
			
			
		} catch (Exception e) {
			throw new GenericEntitySynchronizationException("Error while synchronizing entities into session.",e);
		}

	}
	
	/* ************************************************************
	 * Helper methods and classes.
	 * ************************************************************/

	protected S self() {
		@SuppressWarnings("unchecked")
		S self = (S) this;
		return self;
	}
	
	/**
	 * Internally used implementation of {@link CloningContext} that takes care of the identity management during synchronization. 
	 */
	protected class SynchronizeCloningContext extends StandardCloningContext {
		private final BasicSynchronizationContext context;
		
		public SynchronizeCloningContext(BasicSynchronizationContext context) {
			this.context = context;
		}
		
		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			 
			GenericEntity clone = null;
			IdentityManager identityManager = identityManagerRegistry.findIdentityManager(instanceToBeCloned, entityType, context);
			if (identityManager != null) {
				 clone = identityManager.findEntity(instanceToBeCloned, entityType, context);
			}
			
			// If non of the identity management experts is responsible for given type 
			// or didn't find an existing instance we create a new one in the session
			if (clone == null) {
				clone = session.create(entityType);
			} else {
				// Instance found in session. Register in context.
				context.registerEntityFoundInSession(clone);
			}
			
			return clone;
		}
		
		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			
			if (!includeIdProperties && property.isIdentifier()) {
				return false;
			}
			
			IdentityManager identityManager = identityManagerRegistry.findIdentityManager(instanceToBeCloned, entityType, context);
			if (identityManager != null) {
				return identityManager.canTransferProperty(instanceToBeCloned, clonedInstance, entityType, property, context);
			}

			return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance,sourceAbsenceInformation);
		}
		
	}
	
	

	
	
}
