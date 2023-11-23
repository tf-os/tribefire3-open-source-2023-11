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
 */
public abstract class TracingTemplateConnectorContextImpl implements TracingTemplateConnectorContext, TracingTemplateConnectorContextBuilder {

	private Set<String> entityTypeInclusions;
	private Set<String> entityTypeHierarchyInclusions;
	private Set<String> entityTypeExclusions;
	private Set<String> entityTypeHierarchyExclusions;
	private Set<String> userInclusions;
	private Set<String> userExclusions;
	private Set<DefaultAttribute> defaultAttributes;
	private Map<String, String> customAttributes;
	private Boolean defaultTracingEnabled;
	private String componentName;
	private String tenant;
	private String serviceName;
	private Level addAttributesFromNotificationsMessage;
	private Level addAttributesFromNotificationsDetailsMessage;

	@Override
	public TracingTemplateConnectorContextBuilder setEntityTypeInclusions(Set<String> entityTypeInclusions) {
		this.entityTypeInclusions = entityTypeInclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setEntityTypeHierarchyInclusions(Set<String> entityTypeHierarchyInclusions) {
		this.entityTypeHierarchyInclusions = entityTypeHierarchyInclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setEntityTypeExclusions(Set<String> entityTypeExclusions) {
		this.entityTypeExclusions = entityTypeExclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setEntityTypeHierarchyExclusions(Set<String> entityTypeHierarchyExclusions) {
		this.entityTypeHierarchyExclusions = entityTypeHierarchyExclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setUserInclusions(Set<String> userInclusions) {
		this.userInclusions = userInclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setUserExclusions(Set<String> userExclusions) {
		this.userExclusions = userExclusions;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setDefaultAttributes(Set<DefaultAttribute> defaultAttributes) {
		this.defaultAttributes = defaultAttributes;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setCustomAttributes(Map<String, String> customAttributes) {
		this.customAttributes = customAttributes;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setDefaultTracingEnabled(Boolean defaultTracingEnabled) {
		this.defaultTracingEnabled = defaultTracingEnabled;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setComponentName(String componentName) {
		this.componentName = componentName;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setTenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setAddAttributesFromNotificationsMessage(Level addAttributesFromNotificationsMessage) {
		this.addAttributesFromNotificationsMessage = addAttributesFromNotificationsMessage;
		return this;
	}

	@Override
	public TracingTemplateConnectorContextBuilder setAddAttributesFromNotificationsDetailsMessage(Level addAttributesFromNotificationsDetailsMessage) {
		this.addAttributesFromNotificationsDetailsMessage = addAttributesFromNotificationsDetailsMessage;
		return this;
	}

	@Override
	public Set<String> getEntityTypeInclusions() {
		return entityTypeInclusions;
	}

	@Override
	public Set<String> getEntityTypeHierarchyInclusions() {
		return entityTypeHierarchyInclusions;
	}

	@Override
	public Set<String> getEntityTypeExclusions() {
		return entityTypeExclusions;
	}

	@Override
	public Set<String> getEntityTypeHierarchyExclusions() {
		return entityTypeHierarchyExclusions;
	}

	@Override
	public Set<String> getUserInclusions() {
		return userInclusions;
	}

	@Override
	public Set<String> getUserExclusions() {
		return userExclusions;
	}

	@Override
	public Set<DefaultAttribute> getDefaultAttributes() {
		return defaultAttributes;
	}

	@Override
	public Map<String, String> getCustomAttributes() {
		return customAttributes;
	}

	@Override
	public Boolean getDefaultTracingEnabled() {
		return defaultTracingEnabled;
	}

	@Override
	public String getComponentName() {
		return componentName;
	}

	@Override
	public String getTenant() {
		return tenant;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public Level getAddAttributesFromNotificationsMessage() {
		return addAttributesFromNotificationsMessage;
	}

	@Override
	public Level getAddAttributesFromNotificationsDetailsMessage() {
		return addAttributesFromNotificationsDetailsMessage;
	}

	@Override
	public String toString() {
		return "TracingTemplateConnectorContextImpl [entityTypeInclusions=" + entityTypeInclusions + ", entityTypeHierarchyInclusions="
				+ entityTypeHierarchyInclusions + ", entityTypeExclusions=" + entityTypeExclusions + ", entityTypeHierarchyExclusions="
				+ entityTypeHierarchyExclusions + ", userInclusions=" + userInclusions + ", userExclusions=" + userExclusions + ", defaultAttributes="
				+ defaultAttributes + ", customAttributes=" + customAttributes + ", defaultTracingEnabled=" + defaultTracingEnabled + ", componentName=" + componentName
				+ ", tenant=" + tenant + ", serviceName=" + serviceName + ", addAttributesFromNotificationsMessage=" + addAttributesFromNotificationsMessage
				+ ", addAttributesFromNotificationsDetailsMessage=" + addAttributesFromNotificationsDetailsMessage + "]";
	}
}
