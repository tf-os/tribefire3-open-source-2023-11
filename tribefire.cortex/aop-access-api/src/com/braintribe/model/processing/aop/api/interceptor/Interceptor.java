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
package com.braintribe.model.processing.aop.api.interceptor;

import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;

/**
 * Represents an action that may be "attached" to a given method, meaning it's execution is bound on the method
 * execution. For a list of possible methods see: {@link AccessJoinPoint}.
 * 
 * Note that all implementations must be thread-safe.
 * 
 * TODO please someone try to explain this with better words...
 * 
 * @author gunther.schenk, dirk, pit
 */
public interface Interceptor {
	// empty
}
