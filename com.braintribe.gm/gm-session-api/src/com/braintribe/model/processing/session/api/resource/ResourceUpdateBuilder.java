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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

/**
 * <p>
 * Builder for updating the binary data associated with {@link Resource} as well as the resource itself if needed.
 * 
 * @author Neidhart.Orlich
 */
public interface ResourceUpdateBuilder extends ResourceCreateBuilder {

	ResourceUpdateBuilder deleteOldResourceSource(boolean keep);

	@Override
	ResourceUpdateBuilder mimeType(String mimeType);
	@Override
	ResourceUpdateBuilder md5(String md5);
	@Override
	ResourceUpdateBuilder useCase(String useCase);
	@Override
	ResourceUpdateBuilder tags(Set<String> tags);
	@Override
	ResourceUpdateBuilder sourceType(EntityType<? extends ResourceSource> sourceType);
	@Override
	ResourceUpdateBuilder specification(ResourceSpecification specification);
	@Override
	ResourceUpdateBuilder name(String resourceName);
	@Override
	ResourceUpdateBuilder creator(String creator);

}
