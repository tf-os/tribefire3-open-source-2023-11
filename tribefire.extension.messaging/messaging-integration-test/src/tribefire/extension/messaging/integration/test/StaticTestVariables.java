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
package tribefire.extension.messaging.integration.test;

public interface StaticTestVariables {
	// String KAFKA_URL = "kafka-dev.kafka.svc.cluster.local:9092";// "localhost:29092";
	// String PULSAR_URL = "pulsar://pulsar-dev-proxy.pulsar.svc.cluster.local:6650";// "pulsar://localhost:6650";
	// String PULSAR_SERVICE_URL = "http://pulsar-dev-proxy.pulsar.svc.cluster.local:80";// "http://localhost:8081";
	String KAFKA_URL = "localhost:29092";
	String PULSAR_URL = "pulsar://localhost:6650";
	String PULSAR_SERVICE_URL = "http://localhost:8081";

	String TOPIC = "test";
}
