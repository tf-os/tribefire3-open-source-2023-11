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
package com.braintribe.ddra.endpoints.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.mimetype.MimeTypeParser;
import com.braintribe.mimetype.ParsedMimeType;

/**
 * 
 * Dummy {@link MarshallerRegistry} that completely ignores the marshallers themselves but preserves the order of the
 * registered marshallers' mime types so that they are in a predictable order during the tests. Most code copied from
 * {@link BasicConfigurableMarshallerRegistry}
 * 
 * @author Neidhart.Orlich
 *
 */
class OrderedDummyMarshallerRegistry implements MarshallerRegistry {

	private final List<String> marshallers = new ArrayList<>();

	@Override
	public Marshaller getMarshaller(String mimeType) {
		throw new NotImplementedException();
	}

	@Override
	public MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType) {
		throw new NotImplementedException();
	}

	public void registerMarshaller(String mimeType) {
		marshallers.add(mimeType);
	}

	@Override
	public List<MarshallerRegistryEntry> getMarshallerRegistryEntries(String mimeType) {
		if (mimeType == null)
			return null;

		Stream<String> found;

		if (containsWildcard(mimeType)) {
			found = getCompatibleRegistryEntries(mimeType);
		} else {
			String key = normalizeMimetype(mimeType);
			found = marshallers.stream().filter(key::equals);
		}

		return found.map(TrivialTestMarshallerRegistryEntry::new).collect(Collectors.toList());
	}

	private boolean containsWildcard(String mimeType) {
		return mimeType.length() > 0 && (mimeType.charAt(mimeType.length() - 1) == '*' || mimeType.charAt(0) == '*');
	}

	private Stream<String> getCompatibleRegistryEntries(String mimeTypeWithWildcard) {
		Pattern pattern = Pattern.compile(mimeTypeWithWildcard.replace("*", ".*"));
		return marshallers.stream().filter(mimeType -> pattern.matcher(mimeType).matches());
	}

	protected static String normalizeMimetype(String mimeType) {
		if (mimeType == null) {
			return null;
		}

		ParsedMimeType parsedType = MimeTypeParser.getParsedMimeType(mimeType);
		parsedType.getParams().keySet().retainAll(Collections.singleton("spec"));

		return parsedType.toString();
	}

	class TrivialTestMarshallerRegistryEntry implements MarshallerRegistryEntry {

		private final String mimeType;

		public TrivialTestMarshallerRegistryEntry(String mimeType) {
			this.mimeType = mimeType;
		}

		@Override
		public String getMimeType() {
			return mimeType;
		}

		@Override
		public Marshaller getMarshaller() {
			return null;
		}

	}

}
