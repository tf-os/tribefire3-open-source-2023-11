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
package tribefire.extension.tracing.templates.api;

import java.util.function.Function;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.tracing.templates.api.connector.TracingTemplateConnectorContext;

/**
 * Template Context Builder
 * 
 *
 */
public interface TracingTemplateContextBuilder {

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	TracingTemplateContextBuilder setTracingModule(Module module);

	TracingTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix);

	TracingTemplateContextBuilder setAddDemo(boolean demo);

	TracingTemplateContextBuilder setContext(String context);

	TracingTemplateContextBuilder setConnectorContext(TracingTemplateConnectorContext connectorContext);

	TracingTemplateContext build();

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	TracingTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	TracingTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction);

	TracingTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction);
}