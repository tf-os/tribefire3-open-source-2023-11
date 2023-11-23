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
package com.braintribe.model.processing.resource.streaming.util;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;

public interface UploadResourceEnrichingInterface {

	void toResource(Resource resource);

	InputStream wrapInputStream(InputStream rawInput);

	Long getLength();

	void setLength(Long length);

	String getName();

	void setName(String name);

	Date getCreated();

	void setCreated(Date created);

	String getMd5();

	void setMd5(String md5);

	String getMimeType();

	void setMimeType(String mimeType);

	String getCreator();

	void setCreator(String creator);

	Set<String> getTags();

	void setTags(Set<String> tags);

	ResourceSpecification getResourceSpecification();

	void setResourceSpecification(ResourceSpecification resourceSpecification);

}