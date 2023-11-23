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
package com.braintribe.model.access.impl.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AbstractDelegatingAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.aopaccessapi.AccessAspectAroundProceedRequest;
import com.braintribe.model.aopaccessapi.AccessAspectAroundProceedResponse;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessAspectRuntimeException;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.Caller;
import com.braintribe.model.processing.aop.api.interceptor.AfterInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;
import com.braintribe.model.processing.aop.api.service.AopIncrementalAccess;
import com.braintribe.model.processing.aop.common.JoinPointConfiguration;
import com.braintribe.model.processing.aop.common.PointCutConfigurationContextImpl;
import com.braintribe.model.processing.aop.impl.aspect.EnumConversion;
import com.braintribe.model.processing.aop.impl.context.AfterContextImpl;
import com.braintribe.model.processing.aop.impl.context.AroundContextImpl;
import com.braintribe.model.processing.aop.impl.context.BeforeContextImpl;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;


public class AopAccess extends AbstractDelegatingAccess implements AopIncrementalAccess {
	/**
	 * The logger.
	 */
	protected static Logger logger = Logger.getLogger(AopAccess.class);
	
	private List<AccessAspect> aspects = new ArrayList<AccessAspect>();		
	private PersistenceGmSessionFactory userSessionFactory;
	private PersistenceGmSessionFactory systemSessionFactory;
	private final PointCutConfigurationContextImpl pointCutConfigurationContext = new PointCutConfigurationContextImpl();
	private boolean pointCutsEnsured = false; 
	
	// **************************************************************************
	// Constructor
	// **************************************************************************

	/**
	 * Default constructor
	 */
	public AopAccess() {
	}

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	/**
	 * @param aspects the aspects to set
	 */
	public void setAspects(List<AccessAspect> aspects) {
		this.aspects = aspects;
	}
	
	public List<AccessAspect> getAspects() {
		return aspects;
	}
	
	@Required
	public void setUserSessionFactory( PersistenceGmSessionFactory userSessionFactory) {
		this.userSessionFactory = userSessionFactory;
	}
	@Required
	public void setSystemSessionFactory( PersistenceGmSessionFactory systemSessionFactory) {
		this.systemSessionFactory = systemSessionFactory;
	}
	@Override
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	// **************************************************************************
	// Interface methods
	// **************************************************************************

	private void configurePointCuts() throws AccessAspectRuntimeException {		
		for (AccessAspect aspect : aspects) {
			aspect.configurePointCuts( pointCutConfigurationContext);
		}		
	}
	
	private void ensurePointCutsConfigured() throws AccessAspectRuntimeException {
		if (pointCutsEnsured) {
			return;
		}
		
		synchronized (pointCutConfigurationContext) {
			if (!pointCutsEnsured) {
				configurePointCuts();
				pointCutsEnsured = true;
			}			
		}
	}
	
	@Override
	public GmMetaModel getMetaModel() {
		return withAop(AccessJoinPoint.getMetaModel, null);
	}

	@Override
	public SelectQueryResult query(SelectQuery request) throws ModelAccessException {
		Objects.requireNonNull(request, "The SelectQuery is null. This request will not be processed.");

		return withAop(AccessJoinPoint.query, request);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {
		Objects.requireNonNull(request, "The EntityQuery is null. This request will not be processed.");

		return withAop(AccessJoinPoint.queryEntities, request);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {
		Objects.requireNonNull(request, "The PropertyQuery is null. This request will not be processed.");
		
		return withAop(AccessJoinPoint.queryProperties, request);
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest request) throws ModelAccessException {
		Objects.requireNonNull(request, "The manipulation is null. This request will not be processed.");

		return withAop(AccessJoinPoint.applyManipulation, request);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest request) throws ModelAccessException {
		Objects.requireNonNull(request, "The ReferencesRequest is null. This request will not be processed.");

		return withAop(AccessJoinPoint.getReferences, request);
	}
	
	@Override
	public String getAccessId() {
		if (accessId != null)
			return accessId; 
		else
			return super.getAccessId();
	}
	
	/**
	 * actually call the interceptors for the join point of the access  
	 * @param joinPoint - the {@link AccessJoinPoint}
	 * @param request - the request 
	 * @return - the response 
	 * @throws ModelAccessException - if anything goes wrong 
	 */
	protected <I,O> O withAop (AccessJoinPoint joinPoint, I request) throws ModelAccessException {
		
		ensurePointCutsConfigured();

		try {
			
			JoinPointConfiguration joinPointConfiguration = pointCutConfigurationContext.acquireJoinPointConfiguration(joinPoint);
			
			PersistenceGmSession systemSession = systemSessionFactory.newSession(getAccessId());
			PersistenceGmSession userSession = userSessionFactory.newSession( getAccessId());
			
			BeforeContextImpl<I, O> beforeContext =  new BeforeContextImpl<I, O>();
			beforeContext.setRequest( request);
			beforeContext.setJoinPoint(joinPoint);
			beforeContext.setSystemSession( systemSession);
			beforeContext.setSession( userSession);
			
			O response = null;
			boolean skipped = false;
			for (BeforeInterceptor<?, ?> _interceptor : joinPointConfiguration.beforeInterceptors) {
				BeforeInterceptor<I, O> interceptor = (BeforeInterceptor<I, O>) _interceptor;
				interceptor.run(beforeContext);				
				
				beforeContext.commitIfNecessary();
				// skipped : bunker response, don't call any subsequent before interceptors 				
				if (beforeContext.isSkipped())  {
					response = beforeContext.getResponse();
					skipped = true;
					break;
				}								
			}
			// make sure an overridden request is used 
			request = beforeContext.getRequest();
			
			// only run through around interceptors if none of the before interceptors skipped
			if (!skipped) {
				AroundContextImpl<I, O> aroundContext = new AroundContextImpl<I, O>();
				aroundContext.setJoinPoint(joinPoint);
				aroundContext.setRequest(request);
				aroundContext.setSession(userSession);
				aroundContext.setSystemSession(systemSession);
				aroundContext.setActualCaller(this.<I, O> resolveCaller(joinPoint));
				aroundContext.setInterceptors( cast(joinPointConfiguration.aroundInterceptors));
				// start recursive chain of around interceptors 
				response = aroundContext.proceed();
				request = aroundContext.getRequest();			
			}
			
			AfterContextImpl<I, O> afterContext = new AfterContextImpl<I, O>();
			afterContext.setResponse( response);
			afterContext.setRequest(request);
			afterContext.setJoinPoint(joinPoint);
			afterContext.setSession(userSession);
			afterContext.setSystemSession(systemSession);
			
			for (AfterInterceptor<?, ?> _interceptor : joinPointConfiguration.afterInterceptors) {
				AfterInterceptor<I, O> interceptor = (AfterInterceptor<I, O>) _interceptor;
				interceptor.run(afterContext);
				
				afterContext.commitIfNecessary();
								
				if (afterContext.isSkipped())
					return afterContext.getResponse();
			}
			
			return afterContext.getResponse();
			
		} catch(RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new ModelAccessException(e);
		}
	}	

	@Override
	public AccessAspectAroundProceedResponse proceedAround(AccessAspectAroundProceedRequest request) throws ModelAccessException {
		try {
			PersistenceGmSession systemSession = systemSessionFactory.newSession(getAccessId());
			PersistenceGmSession userSession = userSessionFactory.newSession( getAccessId());

			AccessJoinPoint joinPoint = EnumConversion.convert(request.getAccessJoinPoint());
			JoinPointConfiguration joinPointConfiguration = pointCutConfigurationContext.acquireJoinPointConfiguration(joinPoint);
			
			List<AroundInterceptor<Object, Object>> aroundInterceptors = cast(joinPointConfiguration.aroundInterceptors);
			
			String proceedIdentification = request.getProceedIdentification();
			
			int index = Integer.valueOf(proceedIdentification);
			if (index > aroundInterceptors.size()) {
				throw new ModelAccessException("invalid proceed Identification [" + proceedIdentification + "]");
			}
			
			AroundContextImpl<Object, Object> aroundContext = new AroundContextImpl<Object, Object>();
			aroundContext.setJoinPoint(joinPoint);
			aroundContext.setRequest(request.getRequest());
			aroundContext.setSession(userSession);
			aroundContext.setSystemSession(systemSession);
			aroundContext.setActualCaller(this.<Object, Object> resolveCaller(joinPoint));
			aroundContext.setInterceptors( aroundInterceptors);
			aroundContext.setInterceptorIndex(index);
			
			// start recursive chain of around interceptors 
			Object returnValue = aroundContext.proceed();
			
			AccessAspectAroundProceedResponse response = AccessAspectAroundProceedResponse.T.create();
			response.setResponse(returnValue);
			
			return response;

		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new ModelAccessException("error while processing around aspect proceed", e);
		}
	}
	
	private static <E> E cast(Object o) {
		return (E) o;
	}
	
	private <I, O> Caller<I, O> resolveCaller(AccessJoinPoint joinPoint) {
		return (Caller<I, O>) resolveCallerHelper(joinPoint);
	}
	
	private Caller<?, ?> resolveCallerHelper(AccessJoinPoint joinPoint) {
		switch (joinPoint) {
			case applyManipulation:
				return (request, aroundContext) -> {
					ManipulationResponse response = delegate.applyManipulation((ManipulationRequest) request);
					
					aroundContext.getSession().shallowifyInstances();
					aroundContext.getSystemSession().shallowifyInstances();
					
					return response;
				};
			case getMetaModel:
				return (request, aroundContext) -> delegate.getMetaModel();
			case getReferences:
				return (request, aroundContext) -> delegate.getReferences((ReferencesRequest) request);
			case query:
				return (request, aroundContext) -> delegate.query((SelectQuery) request);
			case queryEntities:
				return (request, aroundContext) -> delegate.queryEntities((EntityQuery) request);
			case queryProperties:
				return (request, aroundContext) -> delegate.queryProperty((PropertyQuery) request);
			default:
				throw new UnsupportedOperationException("unkown join point " + joinPoint);			
		}
	}	
	
	@Override
	public IncrementalAccess getDelegate() {
		return super.getDelegate();
	}
}
 
