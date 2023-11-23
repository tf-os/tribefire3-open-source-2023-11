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
package com.braintribe.model.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.resource.api.HasResourceReadAccess;
import com.braintribe.model.resource.api.ResourceReadAccess;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.StreamableSource;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

/**
 * A representation of streamed data.
 * <ul>
 * <li>mimeType: the MIME type of the actual representation</li>
 * <li>md5: the md5 value of the actual representation</li>
 * <li>file size: the file size of the actual representation (which is NOT the size of what the enriching process delivers)</li>
 * <li>tags: a classification tag to be used for easy grouping</li>
 * <li>resourceSource: access to the contained data stream</li>
 * <li>name: a name given to this Resource</li>
 * <li>created: when</li>
 * <li>creator: who</li>
 * <li>specification: {@link ResourceSpecification}</li>
 * </ul>
 * 
 * @author pit
 *
 */
@ForwardDeclaration("com.braintribe.gm:resource-model")
@SelectiveInformation("${name}")
public interface Resource extends StandardStringIdentifiable {

	final EntityType<Resource> T = EntityTypes.T(Resource.class);

	final String mimeType = "mimeType";
	final String md5 = "md5";
	final String fileSize = "fileSize";
	final String tags = "tags";
	final String resourceSource = "resourceSource";
	final String name = "name";
	final String created = "created";
	final String creator = "creator";
	final String specification = "specification";

	// @formatter:off
	String getMimeType();
	void setMimeType(String mimeType);

	String getMd5();
	void setMd5(String md5);
	
	Long getFileSize();
	void setFileSize(Long fileSize);

	Set<String> getTags();
	void setTags(Set<String> tags);

	ResourceSource getResourceSource();
	void setResourceSource(ResourceSource resourceSource);

	String getName();
	void setName(String name);

	Date getCreated();
	void setCreated(Date created);
	
	void setCreator(String creator);
	String getCreator();

	ResourceSpecification getSpecification();
	void setSpecification(ResourceSpecification specification);
	
	default boolean isTransient() { 
		return getResourceSource() instanceof TransientSource;
	}

	default boolean isStreamable() { 
		return getResourceSource() instanceof StreamableSource;
	}

	/**
	 * Creates a new {@link TransientSource} with given {@link InputStreamProvider} and assigns it to this instance.
	 * 
	 * @param inputStreamProvider {@link InputStreamProvider} to be used for the new {@link TransientSource}, which must be restreameble: 
	 * Any time the {@link InputStreamProvider#openInputStream()} method is called, a new {@link InputStream} for the exact same binary data must be returned
	 */
	default void assignTransientSource(InputStreamProvider inputStreamProvider) {
		TransientSource transientSource = TransientSource.T.create();
		transientSource.setGlobalId(UUID.randomUUID().toString());
		transientSource.setInputStreamProvider(inputStreamProvider);
		transientSource.setOwner(this);
		setResourceSource(transientSource);
	}


	/**
	 * Creates a new {@link Resource} and a new {@link TransientSource} with given {@link InputStreamProvider} and assigns it to the new Resource.
	 * 
	 * @param inputStreamProvider {@link InputStreamProvider} to be used for the new {@link TransientSource}, which must be restreameble: 
	 * Any time the {@link InputStreamProvider#openInputStream()} method is called, a new {@link InputStream} for the exact same binary data must be returned
	 */
	static Resource createTransient(InputStreamProvider inputStreamProvider) {
		Resource resource = Resource.T.create();
		resource.assignTransientSource(inputStreamProvider);
		return resource;
	}
	
	// @formatter:on
	default InputStream openStream() {
		ResourceSource resSrc = getResourceSource();

		if (resSrc instanceof StreamableSource) {
			StreamableSource transientSource = (StreamableSource) resSrc;
			return transientSource.openStream();
		}

		GmSession session = session();

		if (!(session instanceof HasResourceReadAccess)) {
			throw new GmSessionRuntimeException("Cannot open resource stream as entity is not attached to a session which supports streaming.");
		}

		ResourceReadAccess resources = ((HasResourceReadAccess) session).resources();

		try {
			return resources.openStream(this);
		} catch (IOException e) {
			throw new RuntimeException("Error while opening stream" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	default void writeToStream(OutputStream outputStream) {
		ResourceSource resSrc = getResourceSource();
		if (resSrc instanceof StreamableSource) {
			StreamableSource transientSource = (StreamableSource) getResourceSource();
			transientSource.writeToStream(outputStream);
		} else {
			GmSession session = session();

			if (!(session instanceof HasResourceReadAccess)) {
				throw new GmSessionRuntimeException(
						"Cannot write to resource stream as entity is not attached to a session which supports streaming.");
			}

			ResourceReadAccess resources = ((HasResourceReadAccess) session).resources();
			try {
				resources.writeToStream(this, outputStream);
			} catch (IOException e) {
				throw new RuntimeException("Error while writing to stream" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
			}
		}
	}
}
