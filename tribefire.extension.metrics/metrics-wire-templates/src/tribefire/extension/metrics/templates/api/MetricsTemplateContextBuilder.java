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
import java.util.function.Function;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.metrics.templates.api.connector.MetricsTemplateConnectorContext;

/**
 * Template Context Builder
 * 
 *
 */
public interface MetricsTemplateContextBuilder {

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	MetricsTemplateContextBuilder setMetricsModule(Module module);

	MetricsTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix);

	MetricsTemplateContextBuilder setAddDemo(boolean demo);

	MetricsTemplateContextBuilder setContext(String context);

	MetricsTemplateContextBuilder setConnectorContexts(Set<MetricsTemplateConnectorContext> connectorContexts);

	MetricsTemplateContext build();

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	MetricsTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	MetricsTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction);

	MetricsTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction);
}