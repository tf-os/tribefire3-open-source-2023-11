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

import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;
import tribefire.extension.elastic.templates.api.ElasticConstants;

/**
 * <p>
 * This {@link WireSpace Wire contract} provides lookups on already existing instances. <br>
 * It exposes instances like:
 * <ul>
 * <li>Models which are coming from ModelPriming assets</li>
 * <li>Resources coming from ResourcePriming assets</li>
 * </ul>
 * </p>
 */
@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace {

	@GlobalId("model:tribefire.extension.elastic:elasticsearch-deployment-model")
	GmMetaModel deploymentModel();

	@GlobalId("model:tribefire.extension.elastic:elasticsearch-service-model")
	GmMetaModel serviceModel();

	@GlobalId("model:tribefire.extension.elastic:elasticsearch-reflection-model")
	GmMetaModel reflectionModel();

	@GlobalId("model:tribefire.extension.elastic:elasticsearch-data-model")
	GmMetaModel dataModel();

	@GlobalId("model:com.braintribe.gm:basic-resource-model")
	GmMetaModel basicResourceModel();

	@GlobalId("model:com.braintribe.gm:user-model")
	GmMetaModel userModel();

	@GlobalId(ElasticConstants.MODULE_GLOBAL_ID)
	com.braintribe.model.deployment.Module module();

	@GlobalId("ddra:config")
	DdraConfiguration ddraConfiguration();
}
