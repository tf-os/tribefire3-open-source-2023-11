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
package tribefire.extension.tracing.templates.api.connector;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.notification.Level;

import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;

/**
 * 
 *
 */
public interface TracingTemplateConnectorContextBuilder {

	TracingTemplateConnectorContextBuilder setEntityTypeInclusions(Set<String> entityTypeInclusions);

	TracingTemplateConnectorContextBuilder setEntityTypeHierarchyInclusions(Set<String> entityTypeHierarchyInclusions);

	TracingTemplateConnectorContextBuilder setEntityTypeExclusions(Set<String> entityTypeExclusions);

	TracingTemplateConnectorContextBuilder setEntityTypeHierarchyExclusions(Set<String> entityTypeHierarchyExclusions);

	TracingTemplateConnectorContextBuilder setUserInclusions(Set<String> userInclusions);

	TracingTemplateConnectorContextBuilder setUserExclusions(Set<String> userExclusions);

	TracingTemplateConnectorContextBuilder setDefaultAttributes(Set<DefaultAttribute> defaultAttributes);

	TracingTemplateConnectorContextBuilder setCustomAttributes(Map<String, String> customAttributes);

	TracingTemplateConnectorContextBuilder setDefaultTracingEnabled(Boolean defaultTracingEnabled);

	TracingTemplateConnectorContextBuilder setComponentName(String componentName);

	TracingTemplateConnectorContextBuilder setTenant(String tenant);

	TracingTemplateConnectorContextBuilder setServiceName(String serviceName);

	TracingTemplateConnectorContextBuilder setAddAttributesFromNotificationsMessage(Level setAddAttributesFromNotificationsMessage);

	TracingTemplateConnectorContextBuilder setAddAttributesFromNotificationsDetailsMessage(Level addAttributesFromNotificationsDetailsMessage);

	TracingTemplateConnectorContext build();

}