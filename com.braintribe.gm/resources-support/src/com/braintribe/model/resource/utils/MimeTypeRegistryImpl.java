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
package com.braintribe.model.resource.utils;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.braintribe.model.resource.api.MimeTypeRegistry;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

public class MimeTypeRegistryImpl implements MimeTypeRegistry {

	private final MultiMap<String, String> mimeTypeToExtensions = new HashMultiMap<>(true);
	private final MultiMap<String, String> extensionToMimeTypes = new HashMultiMap<>(true);

	@Override
	public void registerMapping(String mimeType, String extension) {
		mimeTypeToExtensions.put(mimeType, extension);
		extensionToMimeTypes.put(extension, mimeType);
	}

	@Override
	public Collection<String> getExtensions(String mimeType) {
		return mimeTypeToExtensions.getAll(mimeType);
	}

	@Override
	public Optional<String> getExtension(String mimeType) {
		return mimeTypeToExtensions.getAll(mimeType).stream().findFirst();
	}

	@Override
	public Collection<String> getMimeTypes(String extension) {
		return extensionToMimeTypes.getAll(extension);

	}

	@Override
	public Optional<String> getMimeType(String extension) {
		return extensionToMimeTypes.getAll(extension).stream().findFirst();
	}

	@Override
	public Set<Entry<String, String>> getEntries() {
		return mimeTypeToExtensions.entrySet();
	}
}
