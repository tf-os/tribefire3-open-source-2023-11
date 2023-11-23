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
package com.braintribe.model.processing.elasticsearch.indexing;

import org.elasticsearch.index.IndexNotFoundException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.util.ElasticsearchUtils;

public class EnsureOpenIndex implements Runnable {

	private final static Logger logger = Logger.getLogger(EnsureOpenIndex.class);

	private IndexedElasticsearchConnector elasticsearchConnector;

	@Override
	public void run() {

		if (elasticsearchConnector == null) {
			logger.warn(() -> "There is no connector set.");
			return;
		}

		String index = elasticsearchConnector.getIndex();
		ElasticsearchClient client = elasticsearchConnector.getClient();

		logger.debug(() -> "Trying to ensure index " + index);

		try {
			ElasticsearchUtils.openIndex(client, index);

			logger.debug(() -> "Successfully ensured index " + index);

		} catch (IndexNotFoundException e) {
			logger.debug(() -> "Could not find index " + index, e);
		} catch (IllegalStateException e) {
			logger.debug(() -> "Error while trying to open index " + index, e);
		} catch (InterruptedException e) {
			logger.debug(() -> "Got interrupted while trying to open index " + index);
		}
	}

	@Configurable
	@Required
	public void setElasticsearchConnector(IndexedElasticsearchConnector elasticsearchConnector) {
		this.elasticsearchConnector = elasticsearchConnector;
	}

}
