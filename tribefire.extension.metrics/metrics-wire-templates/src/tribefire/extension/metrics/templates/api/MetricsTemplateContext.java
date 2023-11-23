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
package tribefire.extension.metrics.templates.api;

import java.util.Set;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.scope.InstanceConfiguration;

import tribefire.extension.metrics.templates.api.connector.MetricsTemplateConnectorContext;

/**
 * METRICS Template Context - contains information for configuring METRICS functionality
 * 
 *
 */
public interface MetricsTemplateContext {

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	Module getMetricsModule();

	String getGlobalIdPrefix();

	boolean getAddDemo();

	String getContext();

	Set<MetricsTemplateConnectorContext> getConnectorContexts();

	static MetricsTemplateContextBuilder builder() {
		return new MetricsTemplateContextImpl();
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	<T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration);

	<T extends GenericEntity> T lookup(String globalId);

	<T extends HasExternalId> T lookupExternalId(String externalId);

}