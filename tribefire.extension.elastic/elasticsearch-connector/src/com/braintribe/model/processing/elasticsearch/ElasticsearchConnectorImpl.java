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

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;

/**
 * This connector creates a connection to elastic, based on the provided credentials.
 *
 */
public class ElasticsearchConnectorImpl implements ElasticsearchConnector, LifecycleAware {

	private final static Logger logger = Logger.getLogger(ElasticsearchConnectorImpl.class);

	protected ElasticsearchAddress address;
	protected ElasticsearchClientImpl client;

	protected String clusterName = "elasticsearch.cartridge";
	protected String host = "127.0.0.1";
	protected int port = 9300;
	protected String nodeName = null;
	protected boolean clusterSniff = false;

	private ClientRegistry clientRegistry;

	@Override
	public void postConstruct() {
		if (this.getClient() != null) {

			if (logger.isDebugEnabled()) {
				logger.debug(ElasticsearchConnectorImpl.class.getSimpleName() + " already set up.");
			}

			return;
		}

		ElasticsearchClientImpl clientImpl = clientRegistry.acquire(this.clusterName, this.host, this.port, this.nodeName, this.clusterSniff);
		this.client = clientImpl;
		this.address = clientImpl.getAddress();

		logger.info("Initialized " + ElasticsearchConnectorImpl.class.getSimpleName() + ".");
	}

	@Override
	public void preDestroy() {
		this.clientRegistry.close(this.client);

		logger.info("ElasticsearchClient connection on " + ElasticsearchConnectorImpl.class.getSimpleName() + "closed.");
	}

	@Configurable
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Configurable
	public void setHost(String host) {
		if (host != null && host.trim().length() > 0) {
			this.host = host;
		}
	}

	@Configurable
	public void setPort(Integer port) {
		if (port != null) {
			this.port = port.intValue();
		}
	}

	@Configurable
	public void setClusterSniff(Boolean clusterSniff) {
		if (clusterSniff != null) {
			this.clusterSniff = clusterSniff.booleanValue();
		}
	}

	@Configurable
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ElasticsearchAddress getAddress() {
		return address;
	}

	public void setAddress(ElasticsearchAddress address) {
		this.address = address;
	}

	@Override
	public ElasticsearchClient getClient() {
		return client;
	}

	@Required
	@Configurable
	public void setClientRegistry(ClientRegistry clientRegistry) {
		this.clientRegistry = clientRegistry;
	}

}
