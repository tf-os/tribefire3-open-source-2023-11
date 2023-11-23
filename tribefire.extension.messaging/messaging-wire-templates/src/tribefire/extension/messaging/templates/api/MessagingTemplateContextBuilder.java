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
package tribefire.extension.messaging.templates.api;

import java.util.function.Function;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.messaging.model.deployment.event.EventConfiguration;

/**
 * Template Context Builder
 */
public interface MessagingTemplateContextBuilder {

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	MessagingTemplateContextBuilder setMessagingModule(Module module);

	MessagingTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix);

	MessagingTemplateContextBuilder setContext(String context);

	MessagingTemplateContextBuilder setEventConfiguration(EventConfiguration eventConfiguration);

	MessagingTemplateContext build();

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	MessagingTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	MessagingTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction);

	MessagingTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction);

	MessagingTemplateContextBuilder setServiceModelDependency(GmMetaModel serviceModelDependency);
}
