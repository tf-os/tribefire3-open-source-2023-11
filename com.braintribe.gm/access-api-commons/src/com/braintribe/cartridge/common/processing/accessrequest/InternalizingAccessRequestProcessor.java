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
package com.braintribe.cartridge.common.processing.accessrequest;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;

import java.util.List;
import java.util.function.Predicate;

import com.braintribe.model.accessapi.AccessManipulationRequest;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.accessapi.HasInducedManipulation;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

public class InternalizingAccessRequestProcessor<P extends AccessRequest,R> implements AccessRequestProcessor<P, R>, ServiceProcessor<P, R> {

	private static TraversingCriterion sessionDataDefaultTc = null;
	private final AccessRequestProcessor<P, R> delegate;
	private final PersistenceGmSessionFactory requestSessionFactory;
	private final PersistenceGmSessionFactory systemSessionFactory;
	
	@SuppressWarnings("unchecked")
	public InternalizingAccessRequestProcessor(AccessRequestProcessor<?, ?> delegate, PersistenceGmSessionFactory requestSessionFactory, PersistenceGmSessionFactory systemSessionFactory) {
		this.delegate = (AccessRequestProcessor<P, R>) delegate;
		this.requestSessionFactory = requestSessionFactory;
		this.systemSessionFactory = systemSessionFactory;
	}

	@Override
	public R process(ServiceRequestContext context, P request) {
		
		BasicAccessAwareRequestContext<P> accessAwareContext = new BasicAccessAwareRequestContext<>(context, requestSessionFactory, systemSessionFactory, request);
		
		R response = delegate.process(accessAwareContext);
		
		accessAwareContext.commitIfNecessary();
		
		response = postProcessAndDetach(response, accessAwareContext);
		
		return response;
	}

	private TraversingCriterion getSessionDataDefaultTc() {
		if (sessionDataDefaultTc == null) {
			sessionDataDefaultTc = TC.create()
					.conjunction()
						.property()
						.typeCondition(or(isKind(TypeKind.collectionType), isKind(TypeKind.entityType)))
					.close()
					.done();
					
		}
		return sessionDataDefaultTc;
	}
	
	private R postProcessAndDetach(R response, BasicAccessAwareRequestContext<P> context) {
		List<PersistenceGmSession> usedSessions = context.getUsedSessions();
		
		if (usedSessions.isEmpty())
			return response;
		
		TraversingCriterion sessionDataTc = getSessionDataDefaultTc();
		
		EnvelopeCloningContext cloningContext = new EnvelopeCloningContext(sessionDataTc, e -> usedSessions.contains(e.session()));
		
		if (context.getOriginalRequest() instanceof AccessManipulationRequest && response instanceof HasInducedManipulation) {
			HasInducedManipulation hasInducedManipulation = (HasInducedManipulation) response;
			cloningContext.setHasInducedManipulation(hasInducedManipulation);
			
			if (hasInducedManipulation.getInducedManipulation() == null) {
				List<Manipulation> inducedManipulations = context.getInducedManipulations();
				
				switch (inducedManipulations.size()) {
				case 0:
					break;
				case 1:
					hasInducedManipulation.setInducedManipulation(inducedManipulations.get(0));
					break;
				default:
					hasInducedManipulation.setInducedManipulation(ManipulationBuilder.compound(inducedManipulations));
				}
			}
		}
		
		response = BaseType.INSTANCE.clone(cloningContext, response, StrategyOnCriterionMatch.partialize);
		
		return response;
	}
	
	private static class EnvelopeCloningContext extends StandardCloningContext {
		private final SessionDataCloningContext sessionDataCloningContext;
		private final Predicate<GenericEntity> isSessionEntity;
		private HasInducedManipulation hasInducedManipulation;

		public EnvelopeCloningContext(TraversingCriterion sessionDataTc, Predicate<GenericEntity> isSessionEntity) {
			this.isSessionEntity = isSessionEntity;
			sessionDataCloningContext = new SessionDataCloningContext(sessionDataTc);
		}
		
		public void setHasInducedManipulation(HasInducedManipulation hasInducedManipulation) {
			this.hasInducedManipulation = hasInducedManipulation;
		}

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T associated = super.getAssociated(entity);
			
			if (associated != null)
				return associated;
			
			if (isSessionEntity.test(entity)) {
				associated = (T)entity.clone(sessionDataCloningContext);
				registerAsVisited(entity, associated);
			}
			
			return associated;
		}
		
		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType,
				GenericEntity instanceToBeCloned) {
			return instanceToBeCloned;
		}
		
		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
				GenericEntity instanceToBeCloned, GenericEntity clonedInstance,
				AbsenceInformation sourceAbsenceInformation) {
			
			return !(instanceToBeCloned == hasInducedManipulation && property.getName().equals("inducedManipulation"));
		}
	}
	
	private static class SessionDataCloningContext extends StandardCloningContext {

		public SessionDataCloningContext(TraversingCriterion sessionDataTc) {
			StandardMatcher matcher = new StandardMatcher();
			matcher.setCriterion(sessionDataTc);
			setMatcher(matcher);
			setAbsenceResolvable(true);
		}
		
	}

	@Override
	public R process(AccessRequestContext<P> context) {
		return delegate.process(context);
	}
}
