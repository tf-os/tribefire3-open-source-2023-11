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

import com.braintribe.model.processing.resource.streaming.ResourceStreamException;
import com.braintribe.model.resource.source.ResourceSource;

/**
 * The interface for implementations of Resource Cache.
 * 
 * @author gunther.schenk
 *
 */
public interface ResourceCache {

	/**
	 * Returns an already existing {@link ResourceCacheEntry} or creates a new one. <br>
	 * Keeps track on locking of the entry creation. If the creation is already in progress this method blocks until the
	 * creation is done.
	 */
	ResourceCacheEntry acquireCacheEntry(ResourceSource representation) throws ResourceStreamException;

	/**
	 * Same as {@link #acquireCacheEntry(ResourceSource)} but with a custom cacheKey and cache domain.
	 */
	ResourceCacheEntry acquireCacheEntry(String cacheKey, String domain) throws ResourceStreamException;

	/**
	 * Returns the cache entry for this source without locking and respecting creation locks.
	 */
	ResourceCacheEntry getCacheEntry(ResourceSource source) throws ResourceStreamException;

	/**
	 * Same as {@link #getCacheEntry(ResourceSource)} but with a custom cacheKey and cache domain.
	 */
	ResourceCacheEntry getCacheEntry(String cacheKey, String domain) throws ResourceStreamException;

}
