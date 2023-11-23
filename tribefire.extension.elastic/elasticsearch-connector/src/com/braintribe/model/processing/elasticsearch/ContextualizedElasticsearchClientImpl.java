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
package com.braintribe.model.processing.elasticsearch;

import java.io.Closeable;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;

import com.braintribe.model.processing.session.api.managed.ModelAccessory;

public class ContextualizedElasticsearchClientImpl implements ContextualizedElasticsearchClient, Closeable {

	private final ElasticsearchClient delegate;
	private final ModelAccessory modelAccessory;
	private String index;
	private Integer maxResultWindow;

	public ContextualizedElasticsearchClientImpl(ElasticsearchClient delegate, ModelAccessory modelAccessory) {
		this.delegate = delegate;
		this.modelAccessory = modelAccessory;
	}

	@Override
	public void open() throws Exception {
		delegate.open();
	}

	@Override
	public IndicesAdminClient getIndicesAdminClient() {
		return delegate.getIndicesAdminClient();
	}

	@Override
	public ClusterAdminClient getClusterAdminClient() {
		return delegate.getClusterAdminClient();
	}

	@Override
	public Client elastic() {
		return delegate.elastic();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public ModelAccessory getModelAccessory() {
		return modelAccessory;
	}

	@Override
	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public Integer getMaxResultWindow() {
		return maxResultWindow;
	}

	public void setMaxResultWindow(Integer maxResultWindow) {
		this.maxResultWindow = maxResultWindow;
	}

}
