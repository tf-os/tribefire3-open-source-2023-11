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
package com.braintribe.model.resource.api;

import java.util.Date;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.io.WriterBuilder;

/**
 * {@link WriterBuilder} for a {@link Resource}.
 * 
 * @author peter.gazdik
 */
public interface ResourceWriterBuilder extends WriterBuilder<Resource> {

	@Override
	/** Value to be used for {@link Resource#getName()} of the resulting resource. */
	ResourceWriterBuilder withName(String name);

	/** Value to be used for {@link Resource#getMimeType() mimeType} of the resulting resource. */
	ResourceWriterBuilder withMimeType(String mimeType);

	/** Value to be used for {@link Resource#getTags() tags} of the resulting resource. */
	ResourceWriterBuilder withTags(String... tags);

	/** Value to be used for {@link Resource#getMd5() md5} of the resulting resource. */
	ResourceWriterBuilder withMd5(String md5);

	/** Value to be used for {@link Resource#getFileSize() fileSize} of the resulting resource. */
	ResourceWriterBuilder withFileSize(Long fileSize);

	/** Value to be used for {@link Resource#getCreated() created} of the resulting resource. */
	ResourceWriterBuilder withCreated(Date date);

	/** Value to be used for {@link Resource#getCreator() creator} of the resulting resource. */
	ResourceWriterBuilder withCreator(String creator);

	/** Value to be used for {@link Resource#getSpecification() specification} of the resulting resource. */
	ResourceWriterBuilder withSpecification(ResourceSpecification specification);

	/** Writes the Resource data as a copy of given resource. */
	default Resource fromResource(Resource resource) {
		return fromInputStreamFactory(resource::openStream);
	}

}
