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
package com.braintribe.codec.marshaller.common;
// ============================================================================

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.mimetype.MimeTypeBasedRegistry;

public class BasicConfigurableMarshallerRegistry extends MimeTypeBasedRegistry<Marshaller> implements ConfigurableMarshallerRegistry {

	public void setMarshallers(Map<String, Marshaller> marshallersMap) {
		for (Map.Entry<String, Marshaller> entry : marshallersMap.entrySet()) {
			String key = entry.getKey();
			if (key != null) {
				key = normalizeMimetype(key);
				registerMarshaller(key, entry.getValue());
			}
		}
	}

	public void setMarshallerRegistryEntries(Set<MarshallerRegistryEntry> marshallerRegistryEntries) {
		for (MarshallerRegistryEntry entry : marshallerRegistryEntries)
			registerMarshallerRegistryEntry(entry);
	}

    public void registerMarshallerRegistryEntry(MarshallerRegistryEntry entry) {
		if (entry instanceof MimeTypeBaseEntry)
			registerEntry((MimeTypeBaseEntry<Marshaller>) entry);
		else
			registerValue(entry.getMimeType(), entry.getMarshaller());
    }

	@Override
	protected BasicMarshallerRegistryEntry newEntry(String mimeType, Marshaller value) {
		return new BasicMarshallerRegistryEntry(mimeType, value);
	}

    @Override
	public Marshaller getMarshaller(String mimeType) {
		return find(mimeType);
	}

	@Override
	public MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType) {
		return (MarshallerRegistryEntry) findEntry(mimeType);
	}

	@Override
	public void registerMarshaller(String mimeType, Marshaller marshaller) {
		registerValue(mimeType, marshaller);
	}

	@Override
	public List<MarshallerRegistryEntry> getMarshallerRegistryEntries(String mimeType) {
		// First cast not needed in eclipse, but needed for javac
		return (List<MarshallerRegistryEntry>) (List<?>) getEntries(mimeType);
	}

	@Override
	public Stream<MarshallerRegistryEntry> streamMarshallerRegistryEntries(String mimeType) {
		// First cast not needed in eclipse, but needed for javac
		return (Stream<MarshallerRegistryEntry>) (Stream<?>)  streamEntries(mimeType);
	}

	@Override
	public void unregisterMarshaller(String mimeType, Marshaller marshaller) {
		unregisterValue(mimeType, marshaller);
	}

}
