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
package com.braintribe.codec.marshaller.api;

/**
 * Binds a mime type to a compatible {@link Marshaller} instance.
 */
public interface MarshallerRegistryEntry {
	
	/**
	 * Returns the mime type associated with the
	 * {@link Marshaller} returned by {@link #getMarshaller()}
	 * @return This entry's mime type
	 */
	String getMimeType();
	
	/**
	 * Returns the {@link Marshaller} compatible with the 
	 * mime type returned by {@link #getMimeType()}
	 * @return This entry's {@link Marshaller}
	 */
	Marshaller getMarshaller();

}
