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
package com.braintribe.model.processing.idgenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Required;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.idgendeployment.IdGeneratorAssignment;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.core.commons.EntityReferenceWrapperCodec;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.idgenerator.api.BasicIdGeneratorContext;
import com.braintribe.model.processing.idgenerator.api.IdGenerator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * 
 * This {@link AccessAspect} implementation analysis incoming Manipulations for InstantiationManipulations.
 * In case an Instantiation on a type is found which has the {@link IdGeneratorAssignment} meta data configured the 
 * assigned {@link IdGenerator} implementation will be used to create a new Id value. Using that value a
 * ChangeValueManipulation is created for the according id property.
 * 
 * @author gunther.schenk
 */
public class IdGeneratorAspect implements AccessAspect {

	private DeployRegistry deployRegistry;
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	
	@Override
	public void configurePointCuts(PointCutConfigurationContext context) {
		context.addPointCutBinding(AccessJoinPoint.applyManipulation, new IdGeneratorInterceptor());
		
	}
	
	private class IdGeneratorInterceptor implements AroundInterceptor<ManipulationRequest, ManipulationResponse> {
		@Override
		public ManipulationResponse run(final AroundContext<ManipulationRequest, ManipulationResponse> context) throws InterceptionException {
			
		    final PersistenceGmSession session = context.getSession();
			final ManipulationRequest request = context.getRequest();
			final Manipulation manipulation = request.getManipulation();

			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<EntityReference, ChangeValueManipulation> idManipulations = CodingMap.create(new LinkedHashMap(), new EntityReferenceWrapperCodec());
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<EntityReference, InstantiationManipulation> instantiationManipulations = CodingMap.create(new LinkedHashMap(), new EntityReferenceWrapperCodec());
			
			// First collect all Instantiations and Id Property assignments
			EntityType<Manipulation> manipulationType = manipulation.entityType();
			manipulationType.traverse(manipulation, null, new EntityVisitor() {
				
				@Override
				protected void visitEntity(GenericEntity entity, EntityCriterion criterion,TraversingContext traversingContext) {
					
					if (entity instanceof ChangeValueManipulation) {
						ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) entity;
						EntityProperty entityProperty = getEntityProperty(changeValueManipulation);
						
						if (entityProperty != null) {
							if (isIdPropertyManipulation(entityProperty)) {
								idManipulations.put(entityProperty.getReference(),changeValueManipulation);
							}
						}

					}
					if (entity instanceof InstantiationManipulation) {
						InstantiationManipulation instantiationManipulation = (InstantiationManipulation)entity;
						EntityReference reference = getEntityReference(instantiationManipulation);
						instantiationManipulations.put(reference, instantiationManipulation);
					}
						
				}
			});
			
			final Map<String,ChangeValueManipulation> additionalIdManipulations = collectAdditionalManipulations(context, session, idManipulations,instantiationManipulations);
			
			if (!additionalIdManipulations.isEmpty()) {
				
				List<AtomicManipulation> manis = request.getManipulation().inline();
				List<Manipulation> newManipulations = new ArrayList<>();
				for (AtomicManipulation mani : manis) {
					newManipulations.add(mani);
					if (mani instanceof InstantiationManipulation) {
						EntityReference entityReference = getEntityReference((InstantiationManipulation) mani);
						ChangeValueManipulation additionalIdManipulation = findAdditionalIdManipulation(additionalIdManipulations,entityReference);
						if (additionalIdManipulation != null) {
							newManipulations.add(additionalIdManipulation);
						}
					}
				}
				CompoundManipulation compoundRequestManipulation = combineManipulations(null, newManipulations);
				request.setManipulation(compoundRequestManipulation);
			}
			
			try {
				ManipulationResponse response = context.proceed(request);
				if (!additionalIdManipulations.isEmpty()) {
					CompoundManipulation compoundInducedManipulation = CompoundManipulation.T.create();
					compoundInducedManipulation.setCompoundManipulationList(new ArrayList<Manipulation>());
					compoundInducedManipulation.getCompoundManipulationList().addAll(additionalIdManipulations.values());
					if (response.getInducedManipulation() != null) {
						compoundInducedManipulation.getCompoundManipulationList().add(response.getInducedManipulation());
					}
					response.setInducedManipulation(compoundInducedManipulation);
				}
				return response;
			} catch (Exception e) {
				throw new InterceptionException("An error occurred while generating ids.",e);
			}
			
		}

		private ChangeValueManipulation findAdditionalIdManipulation(final Map<String,ChangeValueManipulation> additionalIdManipulations, EntityReference entityReference) {
			
			String key = "".concat(entityReference.getTypeSignature()).concat("#").concat(entityReference.getId().toString());
			return additionalIdManipulations.get(key);
		}

		private Map<String,ChangeValueManipulation> collectAdditionalManipulations(
				final AroundContext<ManipulationRequest, ManipulationResponse> context,
				final PersistenceGmSession session,
				final Map<EntityReference, ChangeValueManipulation> idManipulations,
				final Map<EntityReference, InstantiationManipulation> instantiationManipulations) {
			
			Set<Map.Entry<EntityReference, InstantiationManipulation>> entrySet = instantiationManipulations.entrySet();
			int size = entrySet.size();
			
			final Map<String,ChangeValueManipulation> additionalIdManipulations = new LinkedHashMap<>(size);
			
			// Iterate through all Instantiations and check whether an Id Property manipulations exists
			for (Map.Entry<EntityReference,InstantiationManipulation> instantationManipulation : entrySet) {
				EntityReference entityReference = instantationManipulation.getKey();
				if (!idManipulations.containsKey(entityReference)) {
					// No explicit id property assignment found for this entity reference.
					try {
						
						//Check whether IdGeneratorAssignment is configured.
						
						EntityType<GenericEntity> entityType = typeReflection.getEntityType(entityReference.getTypeSignature());
						
						IdGeneratorAssignment assignment = session
								.getModelAccessory()
								.getMetaData()
									.entityType(entityType)
									.meta(IdGeneratorAssignment.T)
								.exclusive();
						
						if (assignment != null) {
							
							// Found and assignment. Use configured Generator to create id value and create additional ChangeValueManipulation
							com.braintribe.model.extensiondeployment.IdGenerator idGeneratorDenotation = assignment.getGenerator();
							DeployedUnit deployedUnit = deployRegistry.resolve(idGeneratorDenotation);
							if (deployedUnit == null) {
								throw new InterceptionException("No implementation deployed for IdGenerator: "+idGeneratorDenotation);
							}
							
							IdGenerator<?> idGenerator = deployedUnit.getComponent(com.braintribe.model.extensiondeployment.IdGenerator.T);
							BasicIdGeneratorContext idGeneratorContext = new BasicIdGeneratorContext(session, context.getSystemSession(), entityType);
							Object idValue = idGenerator.generateId(idGeneratorContext);
							
							if (idValue != null) {
								ChangeValueManipulation idManipulation = createIdManipulation(entityReference, idValue);
								
								EntityProperty ep = (EntityProperty)idManipulation.getOwner();
								String key = "".concat(ep.getReference().getTypeSignature()).concat("#").concat(ep.getReference().getId().toString());
								
								additionalIdManipulations.put(key, idManipulation);
							}
							
						}
					} catch (Exception e) {
						throw new RuntimeException("Error while generating ids.",e);
					}
					
				}
			}
			return additionalIdManipulations;
		}

		
	}

	private CompoundManipulation combineManipulations(Manipulation manipulation, List<? extends Manipulation> manipulationsToAdd) {
		CompoundManipulation compoundManipulation = null;
		if (manipulation instanceof CompoundManipulation) {
			compoundManipulation = (CompoundManipulation) manipulation;
		} else {
			compoundManipulation = CompoundManipulation.T.create();
			compoundManipulation.setCompoundManipulationList(new ArrayList<Manipulation>());
			if (manipulation != null) {
				compoundManipulation.getCompoundManipulationList().add(manipulation);	
			}
		}
		
		compoundManipulation.getCompoundManipulationList().addAll(manipulationsToAdd);
		return compoundManipulation;
	}
	
	private ChangeValueManipulation createIdManipulation(EntityReference entityReference, Object idValue) {
		ChangeValueManipulation idManipulation = ChangeValueManipulation.T.create();
		EntityProperty idProperty = EntityProperty.T.create();
		idProperty.setPropertyName(GenericEntity.id);
		idProperty.setReference(entityReference);
		idManipulation.setOwner(idProperty);
		idManipulation.setNewValue(idValue);
		return idManipulation;
	}

	private EntityProperty getEntityProperty(ChangeValueManipulation changeValueManipulation) {
		Owner owner = changeValueManipulation.getOwner();
		if (owner instanceof EntityProperty) {
			EntityProperty entityProperty = (EntityProperty)owner;
			return entityProperty;
		}
		return null;
	}

	private EntityReference getEntityReference(InstantiationManipulation instantiationManipulation) {
		GenericEntity entity = instantiationManipulation.getEntity(); 
		if (entity instanceof EntityReference) {
			return (EntityReference) instantiationManipulation.getEntity();
		} else {
			return entity.reference();
		}
	}

	private boolean isIdPropertyManipulation( EntityProperty entityProperty) {
		return GenericEntity.id.equals(entityProperty.getPropertyName());
	}

}


/*
				final List<Manipulation> allManipulations = new ArrayList<Manipulation>();
				List<AtomicManipulation> manis = ManipulationTools.inline(request.getManipulation());
				List<Manipulation> newManipulations = new ArrayList<Manipulation>();
				for (AtomicManipulation mani : manis) {
					newManipulations.add(mani);
					if (mani instanceof InstantiationManipulation) {
						EntityReference entityReference = getEntityReference((InstantiationManipulation) mani);
						for (ChangeValueManipulation idMani : idManipulations) {
							EntityProperty ep = (EntityProperty)idMani.getOwner();
							if (ep.getReference().getTypeSignature().equals(entityReference.getTypeSignature()) && ep.getReference().getId().equals(entityReference.getId())) {
								newManipulations.add(idMani);
							}
						}
					}
				}
				CompoundManipulation compoundRequestManipulation = combineManipulations(request.getManipulation(), newManipulations);
 */
