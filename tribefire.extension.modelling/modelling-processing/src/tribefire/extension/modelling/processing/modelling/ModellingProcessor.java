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
package tribefire.extension.modelling.processing.modelling;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.modelling.model.api.data.ModelsToPublish;
import tribefire.extension.modelling.model.api.request.GetModelsToPublish;
import tribefire.extension.modelling.model.api.request.GetModifiedModels;
import tribefire.extension.modelling.model.api.request.ModellingRequest;
import tribefire.extension.modelling.model.api.request.ModellingResponse;
import tribefire.extension.modelling.model.api.request.TransferModifiedModels;

public class ModellingProcessor<M extends ModellingRequest, R extends Object>
		implements AccessRequestProcessor<M, R>, ModellingProcessorConfig {

	private Supplier<PersistenceGmSession> cortexSessionProvider;
	
	@Configurable @Required
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}
	
	//
	// Preparation: Access Request Processor
	//
	
	@Override
	public R process(AccessRequestContext<M> context) {
		return dispatcher.process(context);
	}
	
	private AccessRequestProcessor<M, R> dispatcher = AccessRequestProcessors.dispatcher(config->{

		config.register(GetModelsToPublish.T, this::getModelsToPublish);
		
		// Stateful
		config.register(TransferModifiedModels.T, () -> new TransferModifiedModelsProcessor(this));
		config.register(GetModifiedModels.T, () -> new GetModifiedModelsProcessor(this));
		
	});
	
	//
	// Config Overrides
	//
	
	//
	// Expert Implementations
	//
	
	private ModellingResponse transferModifiedModels(AccessRequestContext<TransferModifiedModels> context) {
		
		return null;
	}
	
	private ModelsToPublish getModelsToPublish(AccessRequestContext<GetModelsToPublish> context) {

		return null;
	}
	
}
