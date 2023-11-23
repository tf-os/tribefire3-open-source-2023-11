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
package com.braintribe.model.processing.session.impl.persistence;

import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.api.notifying.interceptors.VdEvaluation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.aspects.SessionAspect;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.managed.AbstractManagedGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Note regarding AOP - at this level we configure one more {@link PropertyAccessInterceptor}, adding to the two defined
 * on {@link AbstractManagedGmSession} level. They are sorted from outer to the inner most, i.e. the first to handle the
 * access is the one on top, then the one below and after the one on the bottom the actual property is simply set.
 * 
 * This list of interceptors is already merged with the one from {@link AbstractManagedGmSession}.
 * 
 * <h4>{@link LazyLoader}</h4><br>
 * get - do a property query if the property is absent set - <no effect> <br>
 * set - <no effect>
 *
 * <h4>{@link ManipulationTrackingPropertyAccessInterceptor}</h4><br>
 * get - <no effect> <br>
 * set - create the right {@link ChangeValueManipulation}
 * 
 * <h4>{@link CollectionEnhancingPropertyAccessInterceptor}</h4><br>
 * get - in case of a collection property, ensure the returned value is not <tt>null</tt> (when absent, create a
 * collection that is aware it was not loaded yet) (and mark property as not-absent). Also, invoke the setter with this
 * created value!<br>
 * set - in case of a collection property, make sure that a new {@link EnhancedCollection} is set as the actual value.
 * See the actual interceptor for more details.
 *
 * Note that the collection enhancer must also be "deeper" than lazy loader (i.e. not just "deeper" than manipulation
 * tracking), so that for absent collection property no query is made, but a new "partial" (i.e. not fully loaded)
 * collection is created which then can query it's content upon demand.
 *
 * @see PersistenceGmSession
 */
public class BasicPersistenceGmSession extends AbstractPersistenceGmSession {
	private IncrementalAccess incrementalAccess;
	private ResourceAccessFactory<? super BasicPersistenceGmSession> resourcesAccessFactory;
	private ResourceAccess resourcesAccess;
	private SessionAuthorization sessionAuthorization;
	private Evaluator<ServiceRequest> requestEvaluator;
	
	public BasicPersistenceGmSession() {
		LazyLoader lazyLoader = new LazyLoader();
		lazyLoader.setPersistenceSession(this);
		interceptors().with(com.braintribe.model.processing.session.api.notifying.interceptors.LazyLoader.class).after(VdEvaluation.class)
				.before(ManipulationTracking.class).before(CollectionEnhancer.class).add(lazyLoader);
	}
	
	public BasicPersistenceGmSession(IncrementalAccess incrementalAccess) {
		this();
		setIncrementalAccess(incrementalAccess);
	}
	
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	
	@Required
	public void setIncrementalAccess(IncrementalAccess incrementalAccess) {
		this.incrementalAccess = incrementalAccess;
	}
	
	@Configurable
	public void setResourcesAccessFactory(ResourceAccessFactory<? super BasicPersistenceGmSession> resourcesAccessFactory) {
		this.resourcesAccessFactory = resourcesAccessFactory;
	}

	@Override
	protected IncrementalAccess getIncrementalAccess() {
		return incrementalAccess;
	}
	
	protected ResourceAccess getResourcesAccess() {
		if (resourcesAccess == null && resourcesAccessFactory != null) {
			resourcesAccess = resourcesAccessFactory.newInstance(this);
		}

		return resourcesAccess;
	}
	
	@Override
	public ResourceAccess resources() {
		ResourceAccess builder = getResourcesAccess();
		if (builder != null)
			return builder;
		else
			throw new UnsupportedOperationException("no resource builder configured for the session");
	}


	@Override
	public String getAccessId() {
		String accessId = super.getAccessId();
		if (accessId == null) {
			accessId = incrementalAccess.getAccessId();
		}
		return accessId;
	}
	
	public void setSessionAspects (Map<Class<? extends SessionAspect<?>>, Object> sessionAspects){
		super.sessionAspects = sessionAspects;
	}
	
	public <T, A extends SessionAspect<T>> BasicPersistenceGmSession addSessionAspect(Class<A> aspectClass, T value) {
		super.sessionAspects.put(aspectClass, value);
		return this;
	}

	public void setSessionAuthorization(SessionAuthorization sessionAuthorization) {
		this.sessionAuthorization = sessionAuthorization;
	}
	
	@Override
	public SessionAuthorization getSessionAuthorization() {
		return this.sessionAuthorization;
	}

	@Override
	protected Evaluator<ServiceRequest> getRequestEvaluator() {
		return requestEvaluator;
	}

	@Override
	protected PersistenceGmSession createEquivalentSession() {
		BasicPersistenceGmSession result = new BasicPersistenceGmSession(incrementalAccess);
		result.setAccessId(accessId);
		result.setResourcesAccessFactory(resourcesAccessFactory);
		result.setRequestEvaluator(requestEvaluator);
		result.setSessionAuthorization(sessionAuthorization);
		result.sessionAspects.putAll(sessionAspects);

		if (modelAccessory != null)
			result.setModelAccessory(modelAccessory);
		else
			result.setMetaModel(getBackup().getMetaModel());

		return result;
	}


}
