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
package tribefire.extension.tracing.model.service.configuration.local;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.notification.Level;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;
import tribefire.extension.tracing.model.service.TracingRequest;

public interface ConfigureTracingLocal extends TracingRequest {

	EntityType<ConfigureTracingLocal> T = EntityTypes.T(ConfigureTracingLocal.class);

	@Override
	EvalContext<? extends ConfigureTracingLocalResult> eval(Evaluator<ServiceRequest> evaluator);

	// -----------------------------------------------------------------------
	// RESETTING VALUES
	// -----------------------------------------------------------------------

	@Name("Reset Entity Type Inclusions")
	@Initializer("false")
	boolean getResetEntityTypeInclusions();
	void setResetEntityTypeInclusions(boolean resetEntityTypeInclusions);

	@Name("Reset Entity Type Hierarchy Inclusions")
	@Initializer("false")
	boolean getResetEntityTypeHierarchyInclusions();
	void setResetEntityTypeHierarchyInclusions(boolean resetEntityTypeHierarchyInclusions);

	@Name("Reset Entity Type Exclusions")
	@Initializer("false")
	boolean getResetEntityTypeExclusions();
	void setResetEntityTypeExclusions(boolean resetEntityTypeExclusions);

	@Name("Reset Entity Type Hierarchy Exclusions")
	@Initializer("false")
	boolean getResetEntityTypeHierarchyExclusions();
	void setResetEntityTypeHierarchyExclusions(boolean resetEntityTypeHierarchyExclusions);

	@Name("Reset User Inclusions")
	@Initializer("false")
	boolean getResetUserInclusions();
	void setResetUserInclusions(boolean resetUserInclusions);

	@Name("Reset User Exclusions")
	@Initializer("false")
	boolean getResetUserExclusions();
	void setResetUserExclusions(boolean resetUserExclusions);

	@Name("Reset Default Attributes")
	@Initializer("false")
	boolean getResetDefaultAttributes();
	void setResetDefaultAttributes(boolean resetDefaultAttributes);

	@Name("Reset Custom Attributes")
	@Initializer("false")
	boolean getResetCustomAttributes();
	void setResetCustomAttributes(boolean resetCustomAttributes);

	@Name("Reset Component Name")
	@Initializer("false")
	boolean getResetComponentName();
	void setResetComponentName(boolean resetComponentName);

	@Name("Reset Add Attributes from Notifications Message")
	@Initializer("false")
	boolean getResetAddAttributesFromNotificationsMessage();
	void setResetAddAttributesFromNotificationsMessage(boolean resetAddAttributesFromNotificationsMessage);

	@Name("Reset Add Attributes from Notifications Details Message")
	@Initializer("false")
	boolean getResetAddAttributesFromNotificationsDetailsMessage();
	void setResetAddAttributesFromNotificationsDetailsMessage(boolean resetAddAttributesFromNotificationsDetailsMessage);

	// -----------------------------------------------------------------------
	// SETTING VALUES
	// -----------------------------------------------------------------------

	@Name("Entity Type Inclusions")
	@Description("Entity Types to be included in the tracing. Empty means accepting all entity types")
	Set<String> getEntityTypeInclusions();
	void setEntityTypeInclusions(Set<String> entityTypeInclusions);

	@Name("Entity Type Hierarchy Inclusions")
	@Description("Entity Types Hierarchy to be included in the tracing. Empty means accepting all entity types")
	Set<String> getEntityTypeHierarchyInclusions();
	void setEntityTypeHierarchyInclusions(Set<String> entityTypeHierarchyInclusions);

	@Name("Entity Type Exclusions")
	@Description("Entity Types to be excluded in the tracing. Empty means accepting all entity types")
	Set<String> getEntityTypeExclusions();
	void setEntityTypeExclusions(Set<String> entityTypeExclusions);

	@Name("Entity Type Hierarchy Exclusions")
	@Description("Entity Types Hierarchy to be excluded in the tracing. Empty means accepting all entity types")
	Set<String> getEntityTypeHierarchyExclusions();
	void setEntityTypeHierarchyExclusions(Set<String> entityTypeHierarchyExclusions);

	@Name("User Inclusions")
	@Description("Users to be included in the tracing. Empty means accepting all users")
	Set<String> getUserInclusions();
	void setUserInclusions(Set<String> userInclusions);

	@Name("User Exclusions")
	@Description("Users to be excluded in the tracing. Empty means excepting all users")
	Set<String> getUserExclusions();
	void setUserExclusions(Set<String> userExclusions);

	@Name("Default Attributes")
	@Description("Default sttributes that will be added on tracing; empty/null means all default Attributes")
	Set<DefaultAttribute> getDefaultAttributes();
	void setDefaultAttributes(Set<DefaultAttribute> defaultAttributes);

	@Name("Custom Attributes")
	@Description("Custom attributes to be added to each span")
	Map<String, String> getCustomAttributes();
	void setCustomAttributes(Map<String, String> customAttributes);

	@Name("Component Name")
	@Description("Component Name used as 'component' attribute")
	String getComponentName();
	void setComponentName(String componentName);

	@Name("Add Attributes from Notifications Message")
	@Description("In case of a notification add the messages as attribute")
	Level getAddAttributesFromNotificationsMessage();
	void setAddAttributesFromNotificationsMessage(Level addAttributesFromNotificationsMessage);

	@Name("Add Attributea from Notifications Details Message")
	@Description("In case of a notification add the details messages as attribute")
	Level getAddAttributesFromNotificationsDetailsMessage();
	void setAddAttributesFromNotificationsDetailsMessage(Level addAttributesFromNotificationsDetailsMessage);

}
