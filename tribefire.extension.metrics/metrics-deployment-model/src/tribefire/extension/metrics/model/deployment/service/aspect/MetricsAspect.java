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
package tribefire.extension.metrics.model.deployment.service.aspect;

import java.util.Map;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.metrics.model.deployment.service.HasMetricsConnectors;

/**
 * Base for metric aspect - around DDSA services
 * 
 *
 */
@Abstract
public interface MetricsAspect extends ServiceAroundProcessor, HasMetricsConnectors, HasName {

	final EntityType<MetricsAspect> T = EntityTypes.T(MetricsAspect.class);

	String description = "description";
	String tagsSuccess = "tagsSuccess";
	String tagsError = "tagsError";
	String addDomainIdTag = "addDomainIdTag";
	String addPartitionTag = "addPartitionTag";
	String addTypeSignatureTag = "addTypeSignatureTag";
	String addRequiresAuthenticationTag = "addRequiresAuthenticationTag";

	String getDescription();
	void setDescription(String description);

	Map<String, String> getTagsSuccess();
	void setTagsSuccess(Map<String, String> tagsSuccess);

	Map<String, String> getTagsError();
	void setTagsError(Map<String, String> tagsError);

	@Initializer("false")
	boolean getAddDomainIdTag();
	void setAddDomainIdTag(boolean addDomainIdTag);

	@Initializer("false")
	boolean getAddPartitionTag();
	void setAddPartitionTag(boolean addPartitionTag);

	@Initializer("true")
	boolean getAddTypeSignatureTag();
	void setAddTypeSignatureTag(boolean addTypeSignatureTag);

	@Initializer("false")
	boolean getAddRequiresAuthenticationTag();
	void setAddRequiresAuthenticationTag(boolean addRequiresAuthenticationTag);

}
