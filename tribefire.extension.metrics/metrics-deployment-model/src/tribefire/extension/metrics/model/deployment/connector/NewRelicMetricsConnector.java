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
package tribefire.extension.metrics.model.deployment.connector;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 *
 */
public interface NewRelicMetricsConnector extends MetricsConnector {

	final EntityType<NewRelicMetricsConnector> T = EntityTypes.T(NewRelicMetricsConnector.class);

	String accountId = "accountId";
	String uri = "uri";
	String apiKey = "apiKey";

	@Initializer("'3039590'")
	@Mandatory
	String getAccountId();
	void setAccountId(String accountId);

	@Initializer("'https://insights-collector.eu01.nr-data.net/v1/accounts/3039590/events'")
	String getUri();
	void setUri(String uri);

	@Initializer("'NRII-U7MaELdgBE4iOgF8TMjacA_vtln9-xYL'")
	@Mandatory
	String getApiKey();
	void setApiKey(String apiKey);

}
