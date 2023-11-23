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
package com.braintribe.gwt.ioc.gme.client.expert.bootstrapping;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment;
import com.braintribe.gwt.gme.constellation.client.ModelEnvironmentTypesExpert;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpertAspect;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.bapi.AvailableAccesses;
import com.braintribe.model.bapi.AvailableAccessesRequest;
import com.braintribe.model.bapi.CurrentUserInformationRequest;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.CompositeRequestEvaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.user.User;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Expert responsible for loading all requests needed for the bootstrapping.
 * @author michel.docouto
 *
 */
public class BootstrappingRequest implements InitializableBean {
	
	private AvailableAccessesRequest accessesRequest;
	private CurrentUserInformationRequest userRequest;
	private GetModelAndWorkbenchEnvironment modelEnvironmentRequest;
	private Evaluator<ServiceRequest> rpcEvaluator;
	private EvalContext<? extends AvailableAccesses> accessEval;
	private boolean compositeRequested;
	private CompositeRequestEvaluator compositeEvaluator;
	private EvalContext<? extends User> userEval;
	private EvalContext<? extends ModelEnvironment> modelEnvironmentEval;
	private String cortexAccessId = "cortex";
	private boolean modelEnvironmentAlreadyLoaded;
	private Set<BootstrappingRequestListener> bootstrappingRequestListeners;
	private String accessId;
	private boolean accessRequested;
	private boolean userRequested;
	private boolean modelEnvironmentRequested;
	private boolean loadUserAndAccesses = true;
	
	/**
	 * Configures the required request for the {@link AvailableAccesses}.
	 */
	@Required
	public void setAccessesRequest(AvailableAccessesRequest accessesRequest) {
		this.accessesRequest = accessesRequest;
	}
	
	/**
	 * Configures the required request for the current {@link User}.
	 */
	@Required
	public void setUserRequest(CurrentUserInformationRequest userRequest) {
		this.userRequest = userRequest;
	}
	
	/**
	 * Configures the required request for the {@link ModelEnvironment}.
	 */
	@Required
	public void setModelEnvironmentRequest(GetModelAndWorkbenchEnvironment modelEnvironmentRequest) {
		this.modelEnvironmentRequest = modelEnvironmentRequest;
	}
	
	/**
	 * Configures the required {@link Evaluator}.
	 */
	@Required
	public void setRpcEvaluator(Evaluator<ServiceRequest> rpcEvaluator) {
		this.rpcEvaluator = rpcEvaluator;
	}
	
	/**
	 * Configures the accessId of the Cortex. Defaults to "cortex".
	 */
	@Configurable
	public void setCortexAccessId(String cortexAccessId) {
		this.cortexAccessId = cortexAccessId;
	}
	
	@Override
	public void intializeBean() throws Exception {
		compositeEvaluator = new CompositeRequestEvaluator();
		if (accessesRequest == null || userRequest == null)
			loadUserAndAccesses = false;
		else {
			accessEval = accessesRequest.eval(compositeEvaluator);
			userEval = userRequest.eval(compositeEvaluator);
		}
	}
	
	public EvalContext<? extends AvailableAccesses> getAccessEval() {
		accessRequested = true;
		requestCompositeEval();
		return accessEval;
	}
	
	public EvalContext<? extends User> getUserEval() {
		userRequested = true;
		requestCompositeEval();
		return userEval;
	}
	
	public EvalContext<? extends ModelEnvironment> getModelEnvironmentEval(String accessId) {
		this.accessId = accessId;
		EvalContext<? extends ModelEnvironment> modelEnvironmentEval;
		if (!modelEnvironmentAlreadyLoaded) {
			if (accessId != null)
				this.modelEnvironmentEval = modelEnvironmentRequest.eval(compositeEvaluator);
			this.modelEnvironmentRequested = true;
			requestCompositeEval();
			modelEnvironmentAlreadyLoaded = true;
			modelEnvironmentEval = this.modelEnvironmentEval;
		} else {
			modelEnvironmentEval = modelEnvironmentRequest.eval(rpcEvaluator);
			if (cortexAccessId.equals(accessId))
				modelEnvironmentEval.with(EmbeddedRequiredTypesExpertAspect.class, new ModelEnvironmentTypesExpert());
			else
				modelEnvironmentEval.with(EmbeddedRequiredTypesExpertAspect.class, null);
		}
		
		modelEnvironmentRequest.setAccessId(accessId);
			
		return modelEnvironmentEval;
	}
	
	/**
	 * Add a listener so the caller is notified if a failure occurs in the composite evaluator.
	 */
	public void addBootstrappingRequestListener(BootstrappingRequestListener listener) {
		if (bootstrappingRequestListeners == null)
			bootstrappingRequestListeners = new HashSet<>();
		
		bootstrappingRequestListeners.add(listener);
	}

	private void requestCompositeEval() {
		if (!compositeRequested && isAllEvalRequested()) {
			compositeRequested = true;
			
			EvalContext<? extends CompositeResponse> compositeEval = compositeEvaluator.eval(rpcEvaluator);
			
			if (cortexAccessId.equals(this.accessId))
				compositeEval.with(EmbeddedRequiredTypesExpertAspect.class, new ModelEnvironmentTypesExpert());
			else
				compositeEval.with(EmbeddedRequiredTypesExpertAspect.class, null);
			
			compositeEval.get(AsyncCallback.of(e -> {
				if (bootstrappingRequestListeners != null) {
					for (BootstrappingRequestListener listener : bootstrappingRequestListeners)
						listener.onFailure(e);
				}
			}));
		}
	}

	private boolean isAllEvalRequested() {
		return (accessRequested || !loadUserAndAccesses) && (userRequested || !loadUserAndAccesses) && modelEnvironmentRequested;
	}

}