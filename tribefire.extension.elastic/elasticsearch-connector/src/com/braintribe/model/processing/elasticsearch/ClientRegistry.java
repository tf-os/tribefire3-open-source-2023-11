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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

public class ClientRegistry {

	private final static Logger logger = Logger.getLogger(ClientRegistry.class);

	private ReentrantLock lock = new ReentrantLock();

	private Map<String, ClientEntry> clients = new HashMap<>();

	public ElasticsearchClientImpl acquire(String clusterName, String host, int port, String nodeName, boolean clusterSniff) {

		String key = computeKey(clusterName, host, port);

		lock.lock();
		try {

			ClientEntry entry = clients.computeIfAbsent(key, k -> {

				ClientEntry newEntry = new ClientEntry();

				newEntry.address = new ElasticsearchAddress(clusterName, host, port, nodeName, clusterSniff);
				newEntry.client = new ElasticsearchClientImpl(newEntry.address);
				newEntry.referenceCount = 0;

				try {
					newEntry.client.open();
				} catch (Exception e) {
					throw new RuntimeException("Could not connect to " + newEntry.address, e);
				}

				return newEntry;
			});

			entry.referenceCount++;

			return entry.client;

		} finally {
			lock.unlock();
		}

	}

	public void close(ElasticsearchClientImpl client) {

		if (client == null) {
			return;
		}

		ElasticsearchAddress address = client.getAddress();

		String clusterName = address.getClusterName();
		String host = address.getHost();
		int port = address.getPort();

		String key = computeKey(clusterName, host, port);

		lock.lock();
		try {
			ClientEntry entry = clients.get(key);
			if (entry != null) {

				entry.referenceCount--;

				if (entry.referenceCount <= 0) {
					try {
						IOTools.closeCloseable(entry.client, logger);
					} finally {
						clients.remove(key);
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private class ClientEntry {
		ElasticsearchClientImpl client;
		ElasticsearchAddress address;
		int referenceCount;
	}

	private static String computeKey(String clusterName, String host, int port) {
		String key = "".concat(clusterName).concat("@").concat(host).concat(":").concat("" + port);
		return key;
	}
}
