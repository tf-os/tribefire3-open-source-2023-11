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
package com.braintribe.gwt.gmsession.client;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.gm.model.persistence.reflection.api.GetMetaModelForTypes;
import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.gwt.gmrpc.api.client.itw.TypeEnsurer;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CortexTypeEnsurer implements TypeEnsurer {
	private static final Logger logger = Logger.getLogger(CortexTypeEnsurer.class);
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private Evaluator<ServiceRequest> evaluator;
	
	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	
	@Override
	public void ensureTypes(Set<String> requiredTypes) throws GmRpcException {
		// shortcut if no elements are in the collection
		if (requiredTypes.isEmpty())
			return;
		
		// first filter types for those not yet known
		Set<String> yetUnknownTypes = filterNotYetKnownTypes(requiredTypes);
		
		if (!yetUnknownTypes.isEmpty()) {
			try {
				// get some complete description of the yet unkown types
				logger.trace("retrieving model for missing types: " + yetUnknownTypes);
				GetMetaModelForTypes request = GetMetaModelForTypes.T.create();
				request.setTypeSignatures(yetUnknownTypes);
				
				GmMetaModel model = request.eval(evaluator).get();

				// weave the missing types
				logger.trace("weaving model classes for missing types: " + yetUnknownTypes);
				typeReflection.deploy(model);
			} catch (Exception e) {
				throw new GmRpcException("error while weaving types for a model for unkown types: " + yetUnknownTypes, e);
			}
			logger.trace("weaving types from model for missing types: " + yetUnknownTypes);
		}
	}
	
	@Override
	public void ensureTypes(Set<String> requiredTypes, final AsyncCallback<Void> asyncCallback) {
		// shortcut if no elements are in the collection
		if (requiredTypes.isEmpty())
			asyncCallback.onSuccess(null);
		
		// first filter types for those not yet known
		final Set<String> yetUnknownTypes = filterNotYetKnownTypes(requiredTypes);
		
		if (yetUnknownTypes.isEmpty()) {
			asyncCallback.onSuccess(null);
			return;
		}
		
		try {
			// get some complete description of the yet unkown types
			logger.trace("retrieving model for missing types: " + yetUnknownTypes);
			com.braintribe.processing.async.api.AsyncCallback<GmMetaModel> metaModelCallback = com.braintribe.processing.async.api.AsyncCallback.of(model -> {
				// weave the missing types
				logger.trace("weaving model classes for missing types: " + yetUnknownTypes);
						typeReflection.deploy(model,
								com.braintribe.processing.async.api.AsyncCallback.of(asyncCallback::onSuccess, asyncCallback::onFailure));
			}, asyncCallback::onFailure);
			
			GetMetaModelForTypes request = GetMetaModelForTypes.T.create();
			request.setTypeSignatures(yetUnknownTypes);
			
			request.eval(evaluator).get(metaModelCallback);

		} catch (Exception e) {
			asyncCallback.onFailure(new GmRpcException("error while ensuring types: " + yetUnknownTypes));
		}
		
		logger.trace("weaving types from model for missing types: " + yetUnknownTypes);
	}
	
	private Set<String> filterNotYetKnownTypes(Set<String> requiredTypes) {
		Set<String> filteredTypes = new HashSet<>();
		
		for (String requiredType: requiredTypes) {
			GenericModelType type = typeReflection.findType(requiredType);
			if (type == null)
				filteredTypes.add(requiredType);
		}
		return filteredTypes;
	}
	
	
}
