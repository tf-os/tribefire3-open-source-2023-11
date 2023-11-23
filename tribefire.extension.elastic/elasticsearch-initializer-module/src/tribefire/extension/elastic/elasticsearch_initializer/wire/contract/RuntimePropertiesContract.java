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
package tribefire.extension.elastic.elasticsearch_initializer.wire.contract;

import java.util.Set;

import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

public interface RuntimePropertiesContract extends PropertyLookupContract {

	@Default("true")
	boolean ELASTIC_RUN_SERVICE();

	String ELASTIC_ACCESS_INDEX(String defaultValue);

	String ELASTIC_HOST(String defaultValue);
	@Default("9200")
	int ELASTIC_HTTP_PORT(); // REST interface

	@Default("false")
	boolean ELASTIC_CREATE_DEMO_ACCESS();

	@Default("resources/res")
	String ELASTIC_SERVICE_BASE_PATH();
	@Default("9300")
	int ELASTIC_PORT(); // Intercommunication between nodes
	String ELASTIC_SERVICE_DATA_PATH();

	@Default("elasticsearch")
	String ELASTIC_CLUSTER_NAME();

	Set<String> ELASTIC_BIND_HOSTS();
	String ELASTIC_PUBLISH_HOST();
	Set<String> ELASTIC_REPOSITORY_PATHS();
	Integer ELASTIC_RECOVER_AFTER_NODES();
	Integer ELASTIC_RECOVER_AFTER_TIME_IN_MS();
	Integer ELASTIC_EXPECTED_NODES();
	Set<String> ELASTIC_CLUSTER_NODES();
}
