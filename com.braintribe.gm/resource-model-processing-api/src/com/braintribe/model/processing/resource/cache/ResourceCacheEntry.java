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
package com.braintribe.model.processing.resource.cache;

import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.model.processing.resource.streaming.ResourceStreamException;

/**
 * Interface of an representation cache entry. Provides streaming access to the cached content of a RepresentationSource
 * 
 * @author gunther.schenk
 *
 */
public interface ResourceCacheEntry {

	/**
	 * Opens an {@link InputStream} to the cached content.
	 */
	InputStream openCacheInputStream() throws ResourceStreamException;

	/**
	 * Opens an {@link OutputStream} to the cached content.
	 */
	OutputStream openCacheOutputStream() throws ResourceStreamException;

	/**
	 * Deletes all resources related to this cacheEntry.
	 */
	void delete() throws ResourceStreamException;

	/**
	 * Tells whether the representation is already cached.
	 */
	boolean isCached() throws ResourceStreamException;

	/**
	 * Tells the last modification time of this entry.
	 */
	long getLastModification() throws ResourceStreamException;

	/**
	 * @return the size of the cached entry in bytes.
	 */
	long getSize() throws ResourceStreamException;

	/**
	 * @return the md5 of the cached entry.
	 */
	String getMd5() throws ResourceStreamException;

	/**
	 * @return the cache domain.
	 */
	String getDomain();

}
