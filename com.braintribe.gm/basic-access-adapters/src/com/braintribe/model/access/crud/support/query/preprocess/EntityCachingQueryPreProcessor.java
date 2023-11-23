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
package com.braintribe.model.access.crud.support.query.preprocess;

import com.braintribe.model.access.crud.api.read.QueryContext;
import com.braintribe.model.access.crud.support.query.EntityCachingQueryContext;

public class EntityCachingQueryPreProcessor implements QueryPreProcessor {

	private QueryPreProcessor delegatePreProcessor;
	
	public EntityCachingQueryPreProcessor() {
	
	}
	public EntityCachingQueryPreProcessor(QueryPreProcessor delegatePreProcessor) {
		this.delegatePreProcessor = delegatePreProcessor;
	}
	
	@Override
	public QueryContext preProcess(QueryContext context) {
		if (this.delegatePreProcessor != null) {
			context = this.delegatePreProcessor.apply(context);
		}
		return new EntityCachingQueryContext(context);
	}
	
	
}
