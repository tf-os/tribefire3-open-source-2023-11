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
package tribefire.extension.elasticsearch.model.api.request.doc;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.elasticsearch.model.api.ElasticsearchRequest;
import tribefire.extension.elasticsearch.model.api.ElasticsearchResponse;
import tribefire.extension.elasticsearch.model.api.common.HasIndexName;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Condition;

public interface SearchRequest extends ElasticsearchRequest, HasIndexName, HasPagination {

	EntityType<SearchRequest> T = EntityTypes.T(SearchRequest.class);

	String condition = "condition";
	String parentIds = "parentIds";
	String recursively = "recursively";

	@Priority(0.9d)
	@Name("Condition")
	@Description("The condition describing the search criteria.")
	Condition getCondition();
	void setCondition(Condition condition);

	@Priority(0.8d)
	@Name("Parent Folders")
	@Description("Search in parent folders.")
	List<String> getParentIds();
	void setParentIds(List<String> parentIds);

	@Priority(0.7d)
	@Name("Recursively")
	@Description("If enabled all inner folders will be searched.")
	@Mandatory
	@Initializer("false")
	boolean getRecursively();
	void setRecursively(boolean isRecursively);

	@Override
	EvalContext<? extends ElasticsearchResponse> eval(Evaluator<ServiceRequest> evaluator);
}
