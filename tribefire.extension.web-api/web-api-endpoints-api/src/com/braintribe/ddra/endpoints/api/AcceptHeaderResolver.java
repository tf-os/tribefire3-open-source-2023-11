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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.mimetype.MimeTypeParser;
import com.braintribe.mimetype.ParsedMimeType;

public class AcceptHeaderResolver {
	private final MarshallerRegistry registry;
	private String internalMimeTypeBias = "application/json";

	public AcceptHeaderResolver(MarshallerRegistry registry) {
		this.registry = registry;
	}
	
	// Order (if accepted):
	// 1) defaultMimeType
	// 2) Highest q parameter
	// 3) Listed first in accept
	// 4) JSON
	// 5) Listed first in marshaller registry
	
	// if not accepted
	// 1) default mimetype
	// 2) JSON
	public String resolveMimeType(List<String> accepts, String defaultMimeType) {
		Stream<String> acceptedMimeTypes = preProcessAccepts(accepts);

		Stream<String> acceptedAndSupportedMimeTypes = acceptedMimeTypes //
				.flatMap(m -> registry.getMarshallerRegistryEntries(m) //
						.stream() // If several marshallers are resolved - i.e. in case of wildcards - prefer json
						.sorted(Comparator.comparing((MarshallerRegistryEntry e) -> !e.getMimeType().equals(internalMimeTypeBias)))) //
				.distinct() //
				.map(m -> m.getMimeType());

		return acceptedAndSupportedMimeTypes //
				.reduce((result, m) -> preferedMimeType(defaultMimeType, m, result)) // select our prefered mime-type
				.orElse(preferedMimeTypeByUs(defaultMimeType)); // If there really is no single intersection between accepted and supported
											// mime types - use default mime-type
	}
	
	/**
	 * Return (in order of preference if supported) the defaultMimeType, the internalMimeTypeBias, the first supported type in the registry or null
	 */
	private String preferedMimeTypeByUs(String defaultMimeType) {
		List<MarshallerRegistryEntry> allSupportedMimeTypes = registry.getMarshallerRegistryEntries("*/*");
		
		String prefered = null;
		for (MarshallerRegistryEntry entry : allSupportedMimeTypes) {
			final String mimeType = entry.getMimeType();
			
			if (prefered == null) {
				prefered = mimeType;
			}
			
			if (mimeType.equals(defaultMimeType)) {
				return mimeType;
			}
			
			if (mimeType.equals(internalMimeTypeBias)) {
				prefered = mimeType;
			}
		}
		
		return prefered;
	}

	private String preferedMimeType(String defaultMimeType, String current, String result) {
		if (result == null)
			return current;

		if (current.equals(defaultMimeType) || result.equals(defaultMimeType))
			return defaultMimeType;

		return result;
	}

	public void setInternalMimeTypeBias(String internalMimeTypeBias) {
		this.internalMimeTypeBias = internalMimeTypeBias;
	}
	
	private static String qParameter(ParsedMimeType parsedMimeType) {
		return parsedMimeType.getParams().getOrDefault("q", "1");
	}

	public static Stream<String> preProcessAccepts(List<String> accepts) {
		return accepts.stream()//
				.map(MimeTypeParser::getParsedMimeType) //
				.sorted(Comparator.comparing(AcceptHeaderResolver::qParameter) // Sort by the value of the q parameter
						.reversed() // high values first
				) //
				.peek(p -> p.getParams().remove("q")) //
				.map(p -> p.toString());
	}
}
