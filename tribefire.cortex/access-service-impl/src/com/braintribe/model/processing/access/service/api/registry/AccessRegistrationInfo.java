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
package com.braintribe.model.processing.access.service.api.registry;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.IncrementalAccess;

/**
 * An {@link AccessRegistrationInfo} contains the information required for registering an {@link IncrementalAccess} in an
 * {@link RegistryBasedAccessService}.
 * 
 * 
 */
public class AccessRegistrationInfo  {

	private IncrementalAccess access;
	private String accessId;
	private String accessDenotationType;
	private String modelName;
	private String modelAccessId;
	private String workbenchModelName;
	private String workbenchAccessId;
	private String resourceAccessFactoryId;
	private String name;
	private String description;
	private String serviceModelName;
	
	public IncrementalAccess getAccess() {
		return access;
	}

	@Required
	public void setAccess(IncrementalAccess access) {
		this.access = access;
	}

	public String getAccessId() {
		return accessId;
	}

	@Required
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	public String getAccessDenotationType() {
		return accessDenotationType;
	}

	public void setAccessDenotationType(String accessDenotationType) {
		this.accessDenotationType = accessDenotationType;
	}

	public String getWorkbenchAccessId() {
		return workbenchAccessId;
	}

	public void setWorkbenchAccessId(String workbenchAccessId) {
		this.workbenchAccessId = workbenchAccessId;
	}

	public String getModelAccessId() {
		return modelAccessId;
	}

	@Required
	public void setModelAccessId(String metaModelAccessId) {
		this.modelAccessId = metaModelAccessId;
	}

	public String getModelName() {
		return modelName;
	}

	@Required
	public void setModelName(String dataMetaModelName) {
		this.modelName = dataMetaModelName;
	}

	public String getWorkbenchModelName() {
		return workbenchModelName;
	}

	public void setWorkbenchModelName(String workbenchMetaModelName) {
		this.workbenchModelName = workbenchMetaModelName;
	}

	public String getResourceAccessFactoryId() {
		return resourceAccessFactoryId;
	}

	public void setResourceAccessFactoryId(String resourceAccessFactoryId) {
		this.resourceAccessFactoryId = resourceAccessFactoryId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setServiceModelName(String transientModelName) {
		this.serviceModelName = transientModelName;
	}
	
	public String getServiceModelName() {
		return serviceModelName;
	}
}
