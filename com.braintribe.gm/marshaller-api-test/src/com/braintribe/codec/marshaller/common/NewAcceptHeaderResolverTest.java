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

import static com.braintribe.utils.lcd.CollectionTools.getList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;

public class NewAcceptHeaderResolverTest {

	private static final String ANY_ANY = "*/*";
	private static final String ANY_APPLICATION = "application/*";
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	private static final String APPLICATION_YAML = "application/yaml";
	private static final String TEXT_HTML = "text/html";
	private static final String TEXT_TEXT = "text/text";
	private static final String APPLICATION_JSON = "application/json";
	private static final String INTERNAL_MIME_TYPE_BIAS = "internal/bias";

	private static final String Q_1 = ";q=1";
	private static final String Q_0_9 = ";q=0.9";
	private static final String Q_0_8 = ";q=0.8";
	private static final String Q_0_1 = ";q=0.1";
	private static final String Q_0 = ";q=0";

	/**
	 * Tests the most basic cases where there is no q= param on the accepted mime types, no defaultMimeType, no wildcards
	 * and the INTERNAL_MIME_TYPE_BIAS doesn't play a role
	 */
	@Test
	public void testSimpleWithoutDefault() {
		// APPLICATION_JSON comes first in both the accepted and supported mime types so it is resolved
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(APPLICATION_JSON, TEXT_HTML), //
				null, //
				APPLICATION_JSON //
		);

		// APPLICATION_JSON comes first in the supported, but TEXT_HTML first in the accepted mime types. Both mime types are
		// part of accepted and supported.
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_JSON), //
				null, //
				TEXT_HTML //
		);

		// APPLICATION_YAML is the only one supported in both the accepted and supported mime types so it is resolved
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM, APPLICATION_YAML, APPLICATION_JSON), //
				null, //
				APPLICATION_YAML //
		);
	}

	@Test
	public void testSimpleWithDefault() {
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(APPLICATION_JSON, TEXT_HTML), //
				APPLICATION_JSON, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(APPLICATION_JSON, TEXT_HTML), //
				TEXT_HTML, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(APPLICATION_JSON, TEXT_HTML), //
				APPLICATION_YAML, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_JSON), //
				APPLICATION_JSON, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_JSON), //
				TEXT_HTML, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				TEXT_HTML, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				APPLICATION_OCTET_STREAM, //
				APPLICATION_YAML //
		);

	}

	@Test
	public void testSimpleWithWildcard() {
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML), //
				acceptHeaderMimeTypes(ANY_APPLICATION), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, ANY_APPLICATION), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_YAML, ANY_APPLICATION), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_JSON, TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML, ANY_APPLICATION), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				acceptHeaderMimeTypes(ANY_ANY, ANY_APPLICATION), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				acceptHeaderMimeTypes(ANY_APPLICATION, ANY_ANY), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				false, //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				acceptHeaderMimeTypes(ANY_ANY), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				false, //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(ANY_ANY), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				acceptHeaderMimeTypes(ANY_ANY), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(ANY_ANY), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);
	}

	@Test
	// Providing no accepted mime types at all should be handled as if all mime types were accepted ( */* )
	public void testNoAccepts() {
		testResolving( //
				false, //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				Collections.EMPTY_LIST, //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				false, //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				Collections.EMPTY_LIST, //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_YAML), //
				Collections.EMPTY_LIST, //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				Collections.EMPTY_LIST, //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);
	}

	@Test
	public void testSimpleWithQParamAlwaysSameSize() {
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_YAML + Q_1), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_1, APPLICATION_YAML + Q_1), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_1, APPLICATION_YAML + Q_1), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_YAML + Q_0), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0_1, APPLICATION_YAML + Q_0_1), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0_1, APPLICATION_YAML + Q_0_1), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0_1, APPLICATION_YAML + Q_0_1), //
				null, //
				TEXT_HTML //
		);
	}

	@Test
	public void testSimpleWithQParam() {

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_1, APPLICATION_YAML + Q_0), //
				null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_1), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_1, APPLICATION_JSON + Q_1), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0, APPLICATION_JSON + Q_1), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0_9, APPLICATION_JSON + Q_1), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0_9, APPLICATION_JSON + Q_0_1), //
				null, //
				APPLICATION_YAML //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0_8, APPLICATION_JSON + Q_0_1), //
				null, //
				TEXT_HTML //
		);

	}

	@Test
	public void testPreferCustomMarshaller() {
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML), //
				getList(TEXT_HTML), acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0_8, APPLICATION_JSON + Q_0_1), //
				null, //
				TEXT_HTML, //
				true //
		);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				getList(TEXT_HTML, APPLICATION_YAML), acceptHeaderMimeTypes(TEXT_HTML + Q_0, APPLICATION_YAML + Q_0_9, APPLICATION_JSON + Q_0_1), //
				null, //
				APPLICATION_YAML, //
				true);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				getList(TEXT_HTML, APPLICATION_YAML), Collections.EMPTY_LIST, //
				null, //
				TEXT_HTML, //
				true);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				getList(TEXT_HTML, APPLICATION_YAML), Collections.EMPTY_LIST, //
				APPLICATION_JSON, //
				TEXT_HTML, //
				true);

		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				getList(TEXT_HTML, APPLICATION_YAML), Collections.EMPTY_LIST, //
				APPLICATION_YAML, //
				APPLICATION_YAML, //
				true);

		// Don't prefer custom marshaller if client prefers unsupported mimetype
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				getList(TEXT_HTML, APPLICATION_YAML), getList(APPLICATION_JSON, ANY_ANY), //
				TEXT_HTML, //
				APPLICATION_JSON, //
				false);
	}

	@Test
	public void testNoMatch() {
		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver();
		acceptHeaderResolver.setRegistry(marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT));

		// (1) All q params are the same (unspecified = 1.0)
		// (2) No match between accepted and supported
		// Rule 5) Throw exception, even when default mime type is supported but not accepted
		assertThatThrownBy(
				() -> acceptHeaderResolver.resolveMimeType(acceptHeaderMimeTypes(APPLICATION_YAML), Collections.EMPTY_LIST, APPLICATION_OCTET_STREAM))
						.isInstanceOf(IllegalArgumentException.class);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) No match between accepted and supported
		// Rule 5) Throw exception, even when INTERNAL_MIME_TYPE is supported but not accepted
		assertThatThrownBy(
				() -> acceptHeaderResolver.resolveMimeType(acceptHeaderMimeTypes(APPLICATION_YAML), Collections.EMPTY_LIST, APPLICATION_YAML))
						.isInstanceOf(IllegalArgumentException.class);

		// (1) all q params same
		// (2) No match between accepted and supported
		// Rule 5) Throw Exception
		acceptHeaderResolver.setInternalMimeTypeBias(null);
		assertThatThrownBy(
				() -> acceptHeaderResolver.resolveMimeType(acceptHeaderMimeTypes(APPLICATION_YAML), Collections.EMPTY_LIST, APPLICATION_YAML))
						.isInstanceOf(IllegalArgumentException.class);
	}

	// Order:
	// ( Find first supported mime type. Move down list until all ambiguities are resolved )
	// 1) Highest q parameter
	// 2) Listed first in accept
	// 3) marshaller from a MarshallWith metdatata
	// 3a) If we have multiple matches (wildcards) and defaultMimetype is one of them it has prevalence
	// 4) marshaller from main registry
	// 4a) like 3a)
	// 5a) If there is still an ambiguity, the first entry returned by the registry is used (so the registry must have
	// stable order)
	// 5b) If there is no match at all, an exception is thrown
	@Test
	public void testRulesOfOrder() {
		// Trivial case: TEXT_HTML is first in both accepted and supported and also default
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				TEXT_HTML, //
				TEXT_HTML //
		);

		// Trivial case: TEXT_HTML is first in both accepted and supported and no default
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				null, //
				TEXT_HTML //
		);

		// Rule 1) Highest supported q-param wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				acceptHeaderMimeTypes(TEXT_HTML + Q_0_1, APPLICATION_OCTET_STREAM + Q_0_8, TEXT_TEXT + Q_0_9), //
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// Rule 2) First matching wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				APPLICATION_YAML, //
				TEXT_HTML //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// Rule 2) First matching wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT, TEXT_HTML), //
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) First matching is supported by custom and core registry
		// Rule 3) custom registry wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				getList(TEXT_TEXT, APPLICATION_OCTET_STREAM), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT, TEXT_HTML), //
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM, //
				true);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) Custom and core registries resolve different first matches
		// Rule 3) custom registry wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				getList(TEXT_TEXT, APPLICATION_OCTET_STREAM), //
				acceptHeaderMimeTypes(ANY_ANY), //
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM, //
				true //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) Custom and core registries resolve different first matches
		// (3) Custom registry resolves multiple matches
		// Rule 3a) default mime type is one of them and wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				getList(APPLICATION_JSON, APPLICATION_OCTET_STREAM, APPLICATION_YAML), acceptHeaderMimeTypes(ANY_APPLICATION), APPLICATION_YAML, //
				APPLICATION_YAML, //
				true //
		);

		// Rule 1) Highest q-param wins
		// (2) (only one match)
		// (3) Custom registry does not support this mimetype
		// Rule 4) Core registry wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				getList(APPLICATION_JSON, APPLICATION_OCTET_STREAM, APPLICATION_YAML), //
				acceptHeaderMimeTypes(ANY_APPLICATION + Q_0_8, APPLICATION_JSON + Q_0_9, TEXT_HTML), null, //
				TEXT_HTML, //
				false //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) First matching entry in accept header wins
		// (3) Custom registry does not support this mimetype
		// Rule 4) Core registry wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				getList(TEXT_TEXT, APPLICATION_JSON, APPLICATION_OCTET_STREAM, APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML, ANY_APPLICATION, APPLICATION_JSON), //
				null, //
				TEXT_HTML, //
				false //
		);

		// (1) All q params are the same (unspecified = 1.0)
		// (2) First matching entry in accept header wins
		// (3) Custom registry does not support this mimetype
		// (4) Core registry has multiple matches
		// Rule 4a) Default mimetype is one of them and wins
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_JSON, APPLICATION_OCTET_STREAM, APPLICATION_YAML), //
				getList(TEXT_TEXT), //
				acceptHeaderMimeTypes(ANY_APPLICATION, TEXT_TEXT, APPLICATION_JSON), //
				APPLICATION_OCTET_STREAM, //
				APPLICATION_OCTET_STREAM, //
				false //
		);

	}

	@Test
	public void testBrowserStandardHeaderFirefoxImages() {
		List<String> firefoxHtmlAccept = acceptHeaderMimeTypes(TEXT_HTML, "application/xhtml+xml", "application/xml;q=0.9", "image/webp",
				"*/*;q=0.8");

		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/webp"), //
				firefoxHtmlAccept, TEXT_HTML, //
				"image/webp" //
		);

		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg"), //
				firefoxHtmlAccept, TEXT_HTML, //
				"application/xml" //
		);

		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg", "application/xhtml+xml", TEXT_HTML), //
				firefoxHtmlAccept, null, //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg", "application/xhtml+xml", TEXT_HTML), //
				firefoxHtmlAccept, "application/xhtml+xml", //
				TEXT_HTML //
		);

		testResolving( //
				marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept, null, //
				INTERNAL_MIME_TYPE_BIAS);

		testResolving( //
				marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept, "application/x-whatever", //
				"application/x-whatever" //
		);

		testResolving( //
				false, marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept, null, //
				"application/x-whatever" //
		);
	}

	@Test
	public void testBrowserStandardHeaderChromeHtml() {
		List<String> chromeImageAccept = acceptHeaderMimeTypes("image/webp", "image/apng", "image/*", "*/*;q=0.8");

		testResolving( //
				marshallerRegistryWithTypes("image/jpeg", "image/apng", "image/webp"), //
				chromeImageAccept, TEXT_HTML, //
				"image/webp" //
		);

		testResolving( //
				marshallerRegistryWithTypes("image/jpeg", "image/apng", "image/webp", TEXT_HTML), //
				chromeImageAccept, TEXT_HTML, //
				"image/webp" //
		);

		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, "image/jpeg"), //
				chromeImageAccept, null, //
				"image/jpeg" //
		);

	}

	@Test
	public void testOnlyOneType() {
		testResolving( //
				false, marshallerRegistryWithTypes(APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				false, marshallerRegistryWithTypes(APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				APPLICATION_JSON, //
				APPLICATION_JSON //
		);
	}

	@Test
	public void testEmptyMarshallerRegistry() {
		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver();
		acceptHeaderResolver.setRegistry(emptyMarshallerRegistry());
		acceptHeaderResolver.setInternalMimeTypeBias(null);

		assertThatThrownBy(
				() -> acceptHeaderResolver.resolveMimeType(acceptHeaderMimeTypes(ANY_APPLICATION + Q_0_1), Collections.EMPTY_LIST, ANY_APPLICATION))
						.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	// This method tests the non-strict mode that can return a default result even if no accepted mime type is supported
	public void testNoMatchButFallback() {
		testResolving( //
				false, true, marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				TEXT_HTML, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(TEXT_HTML, TEXT_TEXT), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM, APPLICATION_JSON, APPLICATION_YAML, ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM, APPLICATION_JSON), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);

		testResolving( //
				false, true, marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM + Q_0_1, APPLICATION_JSON + Q_0), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);
	}

	// To create a list of accept-header-mime-types with this method should make the test code more understandable
	private static List<String> acceptHeaderMimeTypes(String... t) {
		return Arrays.asList(t);
	}

	private static OrderedDummyMarshallerRegistry emptyMarshallerRegistry() {
		return new OrderedDummyMarshallerRegistry();
	}

	private static OrderedDummyMarshallerRegistry marshallerRegistryWithTypes(String... mimeTypes) {
		OrderedDummyMarshallerRegistry registry = emptyMarshallerRegistry();

		for (String m : mimeTypes) {
			registry.registerMarshaller(m);
		}

		return registry;
	}

	private void testResolving(OrderedDummyMarshallerRegistry registry, List<String> originalAccepts, String defaultMimeType,
			String expectedMimeType) {
		testResolving(true, registry, originalAccepts, defaultMimeType, expectedMimeType);
	}

	private void testResolving(OrderedDummyMarshallerRegistry registry, List<String> customMarshallerTypes, List<String> originalAccepts,
			String defaultMimeType, String expectedMimeType, boolean expectCustomMarshaller) {
		testResolving(true, true, registry, customMarshallerTypes, originalAccepts, defaultMimeType, expectedMimeType, expectCustomMarshaller);
	}

	private void testResolving(boolean withBias, OrderedDummyMarshallerRegistry registry, List<String> originalAccepts, String defaultMimeType,
			String expectedMimeType) {
		testResolving(true, withBias, registry, Collections.EMPTY_LIST, originalAccepts, defaultMimeType, expectedMimeType, false);
	}

	private void testResolving(boolean strict, boolean withBias, OrderedDummyMarshallerRegistry registry, List<String> originalAccepts,
			String defaultMimeType, String expectedMimeType) {

		testResolving(strict, withBias, registry, Collections.EMPTY_LIST, originalAccepts, defaultMimeType, expectedMimeType, false);
	}

	private void testResolving(boolean strict, boolean withBias, OrderedDummyMarshallerRegistry registry, List<String> customMarshallerTypes,
			List<String> originalAccepts, String defaultMimeType, String expectedMimeType, boolean expectCustomMarshaller) {

		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver();
		acceptHeaderResolver.setRegistry(registry);
		acceptHeaderResolver.setStrict(strict);

		if (withBias) {
			acceptHeaderResolver.setInternalMimeTypeBias(INTERNAL_MIME_TYPE_BIAS);
			registry.registerMarshaller(INTERNAL_MIME_TYPE_BIAS);
		} else {
			acceptHeaderResolver.setInternalMimeTypeBias(null);
		}

		List<MarshallerRegistryEntry> customMarshallers = customMarshallerTypes.stream()
				.map(t -> new BasicMarshallerRegistryEntry(t, new CustomTestMarshaller())).collect(Collectors.toList());
		MarshallerRegistryEntry resolvedEntry = acceptHeaderResolver.resolveMimeType(originalAccepts, customMarshallers, defaultMimeType);

		assertThat(resolvedEntry.getMimeType()).isEqualTo(expectedMimeType);

		Class<?> expectedMarshallerClass = expectCustomMarshaller ? CustomTestMarshaller.class : CoreTestMarshaller.class;
		assertThat(resolvedEntry.getMarshaller()).isExactlyInstanceOf(expectedMarshallerClass);
	}

}
