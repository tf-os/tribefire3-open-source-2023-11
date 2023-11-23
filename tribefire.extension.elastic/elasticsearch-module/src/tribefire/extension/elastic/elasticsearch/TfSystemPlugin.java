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
package tribefire.extension.elastic.elasticsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.NetworkPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import tribefire.extension.elastic.elasticsearch.wares.NodeServlet;

/**
 * This is a plugin that is need to access the inner objects of the Elastic {@link Node}. The constructor of this
 * {@link Node} creates {@link NamedXContentRegistry} and a {@link RestController} that we need access to in the
 * {@link NodeServlet}. Hence, this plugin implements those methods that are executed by the Node constructor and keeps
 * the information that we need for later access (see {@link NodeServlet#postConstruct()}). This way of accessing things
 * replaces the older way of using reflection.
 */
public class TfSystemPlugin extends Plugin implements NetworkPlugin {

	private static NamedXContentRegistry xContentRegistry = null;
	private static RestController restController = null;

	@Override
	public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
			ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry) {
		TfSystemPlugin.xContentRegistry = xContentRegistry;
		return Collections.emptyList();
	}

	public static NamedXContentRegistry getxContentRegistry() {
		return xContentRegistry;
	}

	@Override
	public Map<String, Supplier<HttpServerTransport>> getHttpTransports(Settings settings, ThreadPool threadPool, BigArrays bigArrays,
			CircuitBreakerService circuitBreakerService, NamedWriteableRegistry namedWriteableRegistry, NamedXContentRegistry xContentRegistry,
			NetworkService networkService, HttpServerTransport.Dispatcher dispatcher) {
		if (dispatcher instanceof RestController) {
			TfSystemPlugin.restController = (RestController) dispatcher;
		}
		return Collections.emptyMap();
	}

	public static RestController getRestController() {
		return restController;
	}

}
