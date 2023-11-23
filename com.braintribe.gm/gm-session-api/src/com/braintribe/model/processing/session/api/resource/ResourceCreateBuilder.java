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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ResourceSpecification;

/**
 * <p>
 * Builder for creating {@link Resource}(s) and storing their binary data.
 * 
 * @author dirk.scheffler
 */
public interface ResourceCreateBuilder {

	/**
	 * <p>
	 * Sets the MIME-type.
	 * 
	 * @param mimeType
	 *            the MIME-type.
	 */
	ResourceCreateBuilder mimeType(String mimeType);

	/**
	 * <p>
	 * Sets the md5.
	 * 
	 * @param md5
	 *            the md5.
	 */
	ResourceCreateBuilder md5(String md5);

	/**
	 * <p>
	 * Sets the use case under which the {@link Resource} is to be created.
	 * 
	 * @param useCase
	 *            the use case under which the {@link Resource} is to be created.
	 */
	ResourceCreateBuilder useCase(String useCase);

	/**
	 * <p>
	 * Sets the tags of the {@link Resource} is to be created.
	 * 
	 * @param tags
	 *            the tags of the {@link Resource} to be created.
	 */
	ResourceCreateBuilder tags(Set<String> tags);

	/**
	 * <p>
	 * Sets the type of {@link ResourceSource} to be created for the {@link Resource}.
	 * 
	 * @param sourceType
	 *            The type of {@link ResourceSource} to be created for the {@link Resource}.
	 */
	ResourceCreateBuilder sourceType(EntityType<? extends ResourceSource> sourceType);

	/**
	 * <p>
	 * Sets the type of {@link ResourceSpecification} to be created for the {@link Resource}.
	 * 
	 * @param specification
	 *            The type of {@link ResourceSpecification} to be created for the {@link Resource}.
	 */
	ResourceCreateBuilder specification(ResourceSpecification specification);

	/**
	 * <p>
	 * Sets the name of the {@link Resource} to be created.
	 * 
	 * @param resourceName
	 *            The name of the {@link Resource} to be created.
	 */
	ResourceCreateBuilder name(String resourceName);

	/**
	 * <p>
	 * Sets the creator of the {@link Resource} to be created.
	 * 
	 * @param creator
	 *            The creator of the {@link Resource} to be created.
	 */
	ResourceCreateBuilder creator(String creator);

	/**
	 * Creates the {@link Resource} storing the binary provided by the given {@link InputStream}.
	 * <p>
	 * Note that this method creates a buffer with a copy of the data of the provided {@link InputStream} to make it
	 * re-streamable and therefore temporarily acquires memory and disk space. If your binary data is already buffered
	 * (i.e. stored in a local file or a byte array as a whole) please consider using
	 * {@link #store(InputStreamProvider)} instead with an {@link InputStreamProvider} to that buffered data.
	 * 
	 * @param inputStream
	 *            The source of the binary to be stored.
	 * @return The created {@link Resource}
	 * @throws java.io.UncheckedIOException
	 *             If the IO operation fails.
	 */
	Resource store(InputStream inputStream);

	/**
	 * <p>
	 * Creates the {@link Resource} storing the binary provided by the given {@link InputStreamProvider}.
	 * 
	 * @param inputStreamProvider
	 *            The source of the binary to be stored. Every time {@link InputStreamProvider#openInputStream()} is
	 *            called it must return a new instance of an {@link InputStream} which provides the exact same data.
	 * @return The created {@link Resource}
	 * @throws java.io.UncheckedIOException
	 *             If the IO operation fails.
	 */
	Resource store(InputStreamProvider inputStreamProvider);

	/**
	 * Creates the {@link Resource} storing the binary provided by the given {@link OutputStreamer}.
	 * 
	 * @param streamer
	 *            The consumer that will have to feed data on the passed {@link OutputStream} to make up the resource
	 *            content
	 * @return The created {@link Resource}
	 * @throws java.io.UncheckedIOException
	 *             If the IO operation fails.
	 */
	Resource store(OutputStreamer streamer);

}
