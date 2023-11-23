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
package tribefire.extension.elasticsearch.processing;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.elasticsearch.model.api.ElasticsearchRequest;
import tribefire.extension.elasticsearch.model.api.ElasticsearchResponse;
import tribefire.extension.elasticsearch.model.api.request.admin.CreateIndexByName;
import tribefire.extension.elasticsearch.model.api.request.admin.DeleteIndexByName;
import tribefire.extension.elasticsearch.model.api.request.doc.DeleteById;
import tribefire.extension.elasticsearch.model.api.request.doc.IndexResources;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchAsYouType;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchRequest;
import tribefire.extension.elasticsearch.model.api.request.doc.UpdateById;
import tribefire.extension.elasticsearch.model.api.response.IndexResponse;
import tribefire.extension.elasticsearch.model.api.response.SearchResponse;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;
import tribefire.extension.elasticsearch.processing.expert.CreateIndexExpert;
import tribefire.extension.elasticsearch.processing.expert.DeleteExpert;
import tribefire.extension.elasticsearch.processing.expert.DeleteIndexExpert;
import tribefire.extension.elasticsearch.processing.expert.IndexExpert;
import tribefire.extension.elasticsearch.processing.expert.SearchExpert;
import tribefire.extension.elasticsearch.processing.expert.UpdateExpert;

public class ElasticsearchProcessor extends AbstractDispatchingServiceProcessor<ElasticsearchRequest, ElasticsearchResponse> {

	private static final Logger logger = Logger.getLogger(ElasticsearchProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	@Override
	protected void configureDispatching(DispatchConfiguration<ElasticsearchRequest, ElasticsearchResponse> dispatching) {
		dispatching.register(CreateIndexByName.T, this::createIndexByName);
		dispatching.register(DeleteIndexByName.T, this::deleteIndexByName);
		dispatching.register(IndexResources.T, this::indexResources);
		dispatching.register(SearchRequest.T, this::search);
		dispatching.register(SearchAsYouType.T, this::searchAsYouType);
		dispatching.register(UpdateById.T, this::updateById);
		dispatching.register(DeleteById.T, this::deleteById);
	}

	private SuccessResult createIndexByName(ServiceRequestContext context, CreateIndexByName request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return CreateIndexExpert.forCreateIndexByName(request).process();
	}

	private SuccessResult deleteIndexByName(ServiceRequestContext context, DeleteIndexByName request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return DeleteIndexExpert.forDeleteIndexByName(request).process();
	}

	private IndexResponse indexResources(ServiceRequestContext context, IndexResources request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return IndexExpert.forIndexResources(request, cortexSessionSupplier.get()).process();
	}

	private SearchResponse search(ServiceRequestContext context, SearchRequest request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return SearchExpert.forSearch(request).process();
	}

	private SearchResponse searchAsYouType(ServiceRequestContext context, SearchAsYouType request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return SearchExpert.forSearchAsYouType(request).process();
	}

	private SuccessResult updateById(ServiceRequestContext context, UpdateById request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return UpdateExpert.forUpdateById(request).process();
	}

	private SuccessResult deleteById(ServiceRequestContext context, DeleteById request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		return DeleteExpert.forDeleteById(request).process();
	}

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

}
