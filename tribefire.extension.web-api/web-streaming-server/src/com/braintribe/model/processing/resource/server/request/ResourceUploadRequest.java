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
package com.braintribe.model.processing.resource.server.request;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

/**
 * <p>
 * Type-safe holder of request parameters handled on upload operations.
 * 
 */
public class ResourceUploadRequest extends ResourceStreamingRequest {

	private String responseMimeType;
	private String useCase;
	private String mimeType;
	private String md5;
	private Set<String> tags;

	private EntityType<? extends ResourceSource> sourceType;
	private ResourceSpecification specification;


	public ResourceUploadRequest() {
	}

	public String getResponseMimeType() {
		return responseMimeType;
	}

	public void setResponseMimeType(String responseMimeType) {
		this.responseMimeType = responseMimeType;
	}

	public String getUseCase() {
		return useCase;
	}

	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public void setSpecification(ResourceSpecification specification) {
		this.specification = specification;
	}

	public ResourceSpecification getSpecification() {
		return specification;
	}

	public EntityType<? extends ResourceSource> getSourceType() {
		return sourceType;
	}

	public void setSourceType(EntityType<? extends ResourceSource> sourceType) {
		this.sourceType = sourceType;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

}
