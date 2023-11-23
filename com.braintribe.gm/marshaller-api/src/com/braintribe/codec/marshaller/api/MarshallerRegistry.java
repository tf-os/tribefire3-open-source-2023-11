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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface MarshallerRegistry {

	/**
	 * Tries to find the marshaller registered for the given mimeType, this method supports wildcards.
	 * 
	 * e.g.: "application/*"
	 * 
	 * @param mimeType
	 *            the mimeType to query for, must not be {@code null}
	 * @return the marshaller for the given mimeType, or {@code null} if not found
	 */
	Marshaller getMarshaller(String mimeType);

	/**
	 * Tries to find the MarshallerRegistryEntry registered for the given mimeType, this method supports wildcards.
	 * 
	 * e.g.: "application/*"
	 * 
	 * @param mimeType
	 *            the mimeType to query for, must not be {@code null}
	 * @return the MarshallerRegistryEntry for the given mimeType, or {@code null} if not found
	 */
	MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType);

	/**
	 * Gets the supported mimeTypes for the given mimeType, this method supports wildcards.
	 * 
	 * e.g.: "application/*"
	 * 
	 * @param mimeType
	 *            the mimeType, must not be {@code null}
	 * @return the compatible marshaller entries, never {@code null}
	 */
	// or "*/*"
	default List<MarshallerRegistryEntry> getMarshallerRegistryEntries(String mimeType) {
		MarshallerRegistryEntry entry = getMarshallerRegistryEntry(mimeType);
		if(entry == null) {
			return Collections.EMPTY_LIST;
		}
		return Collections.singletonList(entry);
	}
	
	default Stream<MarshallerRegistryEntry> streamMarshallerRegistryEntries(String mimeType) {
		return getMarshallerRegistryEntries(mimeType).stream();
	}
}
