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
package com.braintribe.model.processing.session.api.resource;

import java.util.Set;

import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

/**
 * @see ResourceCreateBuilder
 * @see ResourceUpdateBuilder
 */
@SuppressWarnings("unusable-by-js")
public interface AbstractResourceBuilder<B extends AbstractResourceBuilder<B>> {

	B mimeType(String mimeType);
	B md5(String md5);
	B useCase(String useCase);
	B tags(Set<String> tags);
	/** Sets the type of {@link ResourceSource} to be created for the {@link Resource}. */
	B sourceType(EntityType<? extends ResourceSource> sourceType);
	/** Sets the type of {@link ResourceSpecification} to be created for the {@link Resource}. */
	B specification(ResourceSpecification specification);
	B name(String resourceName);

	B withProgressHandler(ProgressHandler progressHandler);
}
