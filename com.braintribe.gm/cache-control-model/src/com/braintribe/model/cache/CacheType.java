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
package com.braintribe.model.cache;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum CacheType implements EnumBase {

	/**
	 * {@link #noCache} indicates that returned responses must not be used for subsequent requests to the same resource
	 * before checking if server responses have changed. Therefore a cache control of this type might still include
	 * other validation information, like fingerprint or last modification date.
	 */
	noCache,

	/**
	 * {@link #noStore} indicates that client and all intermediate proxies must not store any version of the resource
	 * whatsoever. Therefore a cache control of this type might still include other validation information, like
	 * fingerprint, which would assist caching mechanisms in purging previously cached versions of the resource.
	 */
	noStore,

	/**
	 * {@link #publicCache} indicates that the resource may be cached by a shared cache (e.g.: intermediate proxies).
	 */
	publicCache,

	/**
	 * {@link #privateCache} indicates that the resource is intended for a single user and MUST NOT be cached by a
	 * shared cache.
	 */
	privateCache;

	public static final EnumType T = EnumTypes.T(CacheType.class);

	@Override
	public EnumType type() {
		return T;
	}

}
