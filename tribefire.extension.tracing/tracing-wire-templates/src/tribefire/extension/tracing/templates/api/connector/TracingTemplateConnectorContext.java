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
public interface TracingTemplateConnectorContext {

	Set<String> getEntityTypeInclusions();

	Set<String> getEntityTypeHierarchyInclusions();

	Set<String> getEntityTypeExclusions();

	Set<String> getEntityTypeHierarchyExclusions();

	Set<String> getUserInclusions();

	Set<String> getUserExclusions();

	Set<DefaultAttribute> getDefaultAttributes();

	Map<String, String> getCustomAttributes();

	Boolean getDefaultTracingEnabled();

	String getComponentName();

	String getTenant();

	String getServiceName();

	Level getAddAttributesFromNotificationsMessage();

	Level getAddAttributesFromNotificationsDetailsMessage();

	static TracingTemplateConnectorContextBuilder builder() {
		throw new IllegalStateException("'" + TracingTemplateConnectorContextBuilder.class.getName()
				+ "' needs to implement builder method. Either unintentionally used this base interface or the actual implementation is not overriding this method");
	}
}