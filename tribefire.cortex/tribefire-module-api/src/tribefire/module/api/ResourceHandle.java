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
package tribefire.module.api;

import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Provides methods for obtaining different representations of a resource reference, including ways of obtaining the
 * resource contents.
 */
public interface ResourceHandle {

	/**
	 * <p>
	 * Returns the resolved path as {@link URL}.
	 * 
	 * @return The resolved path as {@link URL}.
	 * @throws UncheckedIOException
	 *             If the resolved path cannot be represented as {@link URL}.
	 */
	URL asUrl() throws UncheckedIOException;

	/**
	 * <p>
	 * Returns the resolved path as {@link Path}.
	 * 
	 * @return The resolved path as {@link Path}.
	 * @throws UncheckedIOException
	 *             If the resolved path cannot be represented as {@link Path}.
	 */
	Path asPath() throws UncheckedIOException;

	/**
	 * <p>
	 * Returns the resolved path as {@link File}.
	 * 
	 * @return The resolved path as {@link File}.
	 * @throws UncheckedIOException
	 *             If the resolved path cannot be represented as {@link File}.
	 */
	File asFile() throws UncheckedIOException;

	/**
	 * <p>
	 * Returns the resource contents as String.
	 * 
	 * @param encoding
	 *            The encoding used for reading the resource contents.
	 * @return The resource contents as String.
	 * @throws UncheckedIOException
	 *             In case of IOException(s) while reading the resource contents.
	 */
	String asString(String encoding) throws UncheckedIOException;

	/**
	 * <p>
	 * Returns an {@link InputStream} for reading the resource contents.
	 * 
	 * @return An {@link InputStream} for reading the resource contents.
	 * @throws UncheckedIOException
	 *             In case of IOException(s) while obtaining the {@link InputStream}.
	 */
	InputStream asStream() throws UncheckedIOException;

	/**
	 * <p>
	 * Returns a {@link Properties} instance as loaded from the resource.
	 * 
	 * <p>
	 * The resource compatibility for this operation is described in the {@link Properties#load(InputStream)} method
	 * documentation.
	 * 
	 * @return A {@link Properties} instance as loaded from the resource.
	 * @throws UncheckedIOException
	 *             In case of IOException(s) while reading the resource or loading it as a {@link Properties} object.
	 */
	Properties asProperties() throws UncheckedIOException;

}
