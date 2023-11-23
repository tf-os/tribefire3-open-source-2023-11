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
package tribefire.module.wire.contract;

import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.enrichment.EnrichResourceResponse;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.wire.api.space.WireSpace;

/**
 * @author peter.gazdik
 */
public interface ResourceProcessingBindersContract extends WireSpace {

	ComponentBinder<BinaryPersistence, ServiceProcessor<? super BinaryPersistenceRequest, ? super BinaryPersistenceResponse>> binaryPersistenceProcessor();
	
	ComponentBinder<BinaryRetrieval, ServiceProcessor<? super BinaryRetrievalRequest, ? super BinaryRetrievalResponse>> binaryRetrievalProcessor();

	ComponentBinder<ResourceEnricher, ServiceProcessor<? super EnrichResource, ? super EnrichResourceResponse>> resourceEnricherProcessor();
}
