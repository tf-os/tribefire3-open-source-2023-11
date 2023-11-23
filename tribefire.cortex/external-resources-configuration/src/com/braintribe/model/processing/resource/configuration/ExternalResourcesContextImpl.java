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
package com.braintribe.model.processing.resource.configuration;

import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.source.ResourceSource;

public class ExternalResourcesContextImpl implements ExternalResourcesContext, ExternalResourcesContextBuilder {

	private BinaryProcessWith binaryProcessWith;
	private GmMetaModel persistenceDataModel;
	private EntityType<? extends ResourceSource> resourceSourceType;

	@Override
	public ExternalResourcesContextBuilder setBinaryProcessWith(BinaryProcessWith binaryProcessWith) {
		this.binaryProcessWith = binaryProcessWith;
		return this;
	}

	@Override
	public ExternalResourcesContextBuilder setPersistenceDataModel(GmMetaModel persistenceDataModel) {
		this.persistenceDataModel = persistenceDataModel;
		return this;
	}

	@Override
	public ExternalResourcesContextBuilder setResourceSourceType(EntityType<? extends ResourceSource> resourceSourceType) {
		this.resourceSourceType = resourceSourceType;
		return this;
	}

	@Override
	public ExternalResourcesContext build() {
		return this;
	}

	@Override
	public BinaryProcessWith getBinaryProcessWith() {
		return binaryProcessWith;
	}

	@Override
	public GmMetaModel getPersistenceDataModel() {
		return persistenceDataModel;
	}

	@Override
	public EntityType<? extends ResourceSource> getResourceSourceType() {
		return resourceSourceType;
	}
	
}
