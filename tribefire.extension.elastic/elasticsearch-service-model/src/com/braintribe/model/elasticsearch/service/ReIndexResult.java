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
package com.braintribe.model.elasticsearch.service;

import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexing;
import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexingStatus;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ReIndexResult extends ElasticResult {

	final EntityType<ReIndexResult> T = EntityTypes.T(ReIndexResult.class);

	void setReIndexing(ReIndexing reIndexing);
	ReIndexing getReIndexing();

	void setStatus(ReIndexingStatus status);
	ReIndexingStatus getStatus();
}
