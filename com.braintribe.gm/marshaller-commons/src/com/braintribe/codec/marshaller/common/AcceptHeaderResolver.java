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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.mimetype.MimeTypeParser;
import com.braintribe.mimetype.ParsedMimeType;

/**
 * HTTP requests usually have an 'Accept' header to communicate which content type the client accepts for the response
 * (see <a href=
 * "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept">https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept</a>).
 * This class helps to analyze the value of such an header and find a matching marshaller to marshall the response body.
 * <p>
 * Further it distinguishes between marshallers from a common registry which is known by this class and request specific
 * custom marshallers that are usually mapped via a MarshallWith metadata on the respective entity type.
 * <p>
 * The algorithm tries to comply with the caller's mime-type preferences as closely as possible.
 * 
 * @author Neidhart.Orlich
 * @see #resolveMimeType(List, List, String)
 */
public class AcceptHeaderResolver {
	private static final String ALL_MIME_TYPES = "*/*";
	private boolean strict = true;

	private MarshallerRegistry registry;
	private String internalMimeTypeBias = "application/json";

	/**
	 * @param registry
	 *            Core registry for response marshallers for all kinds of requests
	 */
	public void setRegistry(MarshallerRegistry registry) {
		this.registry = registry;
	}

	/**
	 * In strict mode an exception is thrown, when no accepted mime-type is supported. Otherwise a marshaller of our
	 * choice is returned.
	 * <p>
	 * Strict mode is default and must be disabled explicitly.
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * In case of ambiguity due to multiple supported mime types resolved from a wildcarded accept header entry with no
	 * matching defaultMimeType, this mimetype can be used instead.
	 * <p>
	 * <b>Note</b>, that this mimetype must be supported by the core registry which is set via
	 * {@link #setRegistry(MarshallerRegistry)} or mapped as a custom marshaller for all possible requests
	 */
	public void setInternalMimeTypeBias(String internalMimeTypeBias) {
		this.internalMimeTypeBias = internalMimeTypeBias;
	}

	/**
	 * Note, that when no accept header is set, we assume that all possible mime types are accepted
	 * <p>
	 * In detail, the resulting marshaller is chosen like follows:<br>
	 * First the accepted mimetypes are sorted in the following order:
	 * <ol>
	 * <li>Highest q parameter (q-factor weighting)
	 * <li>Listed first in accept header
	 * </ol>
	 * We respect the caller's choice and try to resolve a marshaller for the highest ranked (=first) mime type in his
	 * list that we support
	 * <ol>
	 * <li>If we have a custom, request specific marshaller with the mime type this one has preference
	 * <li>If we have multiple matches (wildcards) and defaultMimeType is one of them it has preference. If
	 * defaultMimetype is not supported, {@link #setInternalMimeTypeBias(String) internalMimeTypeBias} would have
	 * preference instead
	 * <li>If we have a marshaller in our main registry we use that one
	 * <li>like 2)
	 * <li>If we don't have a marshaller at all for that type we move on in the list of accepted types and repeat 1) to
	 * 4)
	 * <li>If we found multiple matches and there is an ambiguity, the first entry returned by the registry is used (so
	 * the registry must have stable order)
	 * <li>If there is no match at all, an exception is thrown ({@link #setStrict(boolean) strict mode}) or the
	 * marshaller for defaultMimeType or internalMimeTypeBias is returned.
	 * </ol>
	 * 
	 * @param accepts
	 *            List of mime types from the Accept-header
	 * @param requestSpecificMarshallers
	 *            List of custom marshallers for the specific request
	 * @param defaultMimeType
	 *            In case of ambiguity this mime type gets preference.
	 * @return {@link MarshallerRegistryEntry} of marshaller and mime type to be used for response marshalling
	 */
	public MarshallerRegistryEntry resolveMimeType(List<String> accepts, List<MarshallerRegistryEntry> requestSpecificMarshallers,
			String defaultMimeType) {
		BasicConfigurableMarshallerRegistry customRegistry = new BasicConfigurableMarshallerRegistry();
		requestSpecificMarshallers.forEach(customRegistry::registerMarshallerRegistryEntry);

		Stream<String> acceptedMimeTypes = preProcessAccepts(accepts); // extracting q=;parameter and sorting

		Stream<MarshallerRegistryEntry> matchingEntries = acceptedMimeTypes //
				.flatMap(m -> entryForMimeType(customRegistry, m, defaultMimeType));

		final Stream<MarshallerRegistryEntry> finalEntries = strict //
				? matchingEntries //
				: appendDefaultEntries(matchingEntries, defaultMimeType, customRegistry);

		MarshallerRegistryEntry firstMatchingEntry = finalEntries //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException(
						"Didn't find a marshaller to return - neither one matching the accepted mime types nor a default one."));

		return firstMatchingEntry;
	}

	private Stream<MarshallerRegistryEntry> appendDefaultEntries(Stream<MarshallerRegistryEntry> entries, String defaultMimeType,
			BasicConfigurableMarshallerRegistry customRegistry) {
		return Stream.of( //
				entries, //
				customRegistry.streamMarshallerRegistryEntries(defaultMimeType), //
				registry.streamMarshallerRegistryEntries(defaultMimeType), //
				customRegistry.streamMarshallerRegistryEntries(internalMimeTypeBias), //
				registry.streamMarshallerRegistryEntries(internalMimeTypeBias) //
		).flatMap(s -> s);
	}

	private Stream<MarshallerRegistryEntry> entryForMimeType(BasicConfigurableMarshallerRegistry customRegistry, String mimeType,
			String defaultMimeType) {
		if (mimeType == null)
			return Stream.empty();

		Comparator<MarshallerRegistryEntry> comparing = biasedComparator(defaultMimeType, internalMimeTypeBias);

		return Stream.concat( //
				customRegistry.streamMarshallerRegistryEntries(mimeType).sorted(comparing), //
				registry.streamMarshallerRegistryEntries(mimeType).sorted(comparing) //
		);
	}

	private Comparator<MarshallerRegistryEntry> biasedComparator(String firstMimeTypeBias, String secondMimeTypeBias) {
		return Comparator //
				.comparing((MarshallerRegistryEntry e) -> !e.getMimeType().equals(firstMimeTypeBias)) //
				.thenComparing((MarshallerRegistryEntry e) -> !e.getMimeType().equals(secondMimeTypeBias));
	}

	private static String qParameter(ParsedMimeType parsedMimeType) {
		return parsedMimeType.getParams().getOrDefault("q", "1");
	}

	public static Stream<String> preProcessAccepts(List<String> accepts) {
		if (accepts.isEmpty())
			return Stream.of(ALL_MIME_TYPES);

		return accepts.stream()//
				.map(MimeTypeParser::getParsedMimeType) //
				.sorted(Comparator.comparing(AcceptHeaderResolver::qParameter) // Sort by the value of the q parameter
						.reversed() // high values first
				) //
				.peek(p -> p.getParams().remove("q")) //
				.map(p -> p.toString());
	}
}
