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
package com.braintribe.model.access;

/**
 * 
 * An (optional) interface that can be implemented by implementations
 * of {@link PageProvider} that tells whether more data is available
 * during an iteration run without having the need to load the full 
 * data amount in memory at that time. 
 *
 */
public interface NextPageAware {

	/**
	 * Implementations return true in case there is more data available
	 * that can be loaded.
	 */
	boolean hasNextPage();
	
}
