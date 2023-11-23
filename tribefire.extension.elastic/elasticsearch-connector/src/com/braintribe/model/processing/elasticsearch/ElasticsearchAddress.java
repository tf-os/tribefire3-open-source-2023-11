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

import com.braintribe.logging.Logger;

public class ElasticsearchAddress {

	private final static Logger logger = Logger.getLogger(ElasticsearchAddress.class);

	protected String clusterName = null;
	protected String host = null;
	protected String nodeName = null;
	protected int port = 9300;
	protected boolean clusterSniff = true;

	public ElasticsearchAddress(String clusterName, String host, int port, String nodeName, boolean clusterSniff) {
		this.clusterName = clusterName;
		this.host = host;
		this.port = port;
		this.nodeName = nodeName;
		this.clusterSniff = clusterSniff;

		if (logger.isDebugEnabled()) {
			logger.debug("Initialized ElasticsearchAddress: " + this.toString());
		}
	}

	public String getClusterName() {
		return clusterName;
	}
	public String getHost() {
		return host;
	}
	public String getNodeName() {
		return nodeName;
	}
	public int getPort() {
		return port;
	}
	public boolean getClusterSniff() {
		return clusterSniff;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster: ");
		sb.append(this.clusterName);
		sb.append(", Host: ");
		sb.append(this.host);
		sb.append(", Port: ");
		sb.append(this.port);
		return sb.toString();
	}
}
