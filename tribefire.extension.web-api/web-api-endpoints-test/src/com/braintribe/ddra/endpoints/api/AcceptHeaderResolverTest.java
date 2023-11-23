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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class AcceptHeaderResolverTest {

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
	 * Tests the most basic cases where there is no q= param on the accepted mime types, no defaultMimeType, no wildcards and the INTERNAL_MIME_TYPE_BIAS doesn't play a role
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
		
		// APPLICATION_JSON comes first in the supported, but TEXT_HTML first in the accepted mime types. Both mime types are part of accepted and supported. 
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
				TEXT_HTML //
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
				APPLICATION_JSON //
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
				marshallerRegistryWithTypes( APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(ANY_ANY), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
				);
	}
	
	@Test
	public void testSimpleWithQParamAlwaysSameSize() {
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_YAML+Q_1), //
				null, //
				APPLICATION_YAML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_1, APPLICATION_YAML+Q_1), //
				null, //
				TEXT_HTML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_1, APPLICATION_YAML+Q_1), //
				null, //
				TEXT_HTML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_YAML+Q_0), //
				null, //
				APPLICATION_YAML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0_1, APPLICATION_YAML+Q_0_1), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0_1, APPLICATION_YAML+Q_0_1), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0_1, APPLICATION_YAML+Q_0_1), //
				null, //
				TEXT_HTML //
				);
	}
	
	@Test
	public void testSimpleWithQParam() {
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_1, APPLICATION_YAML+Q_0), //
				null, //
				TEXT_HTML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_1), //
				null, //
				APPLICATION_YAML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_1, APPLICATION_JSON+Q_1), //
				null, //
				APPLICATION_YAML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0, APPLICATION_JSON+Q_1), //
				null, //
				APPLICATION_JSON //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0_9, APPLICATION_JSON+Q_1), //
				null, //
				APPLICATION_JSON //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML, TEXT_HTML, APPLICATION_JSON), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0_9, APPLICATION_JSON+Q_0_1), //
				null, //
				APPLICATION_YAML //
				);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0, APPLICATION_YAML+Q_0_8, APPLICATION_JSON+Q_0_1), //
				null, //
				TEXT_HTML //
		);

	}
	
	@Test
	public void testNoMatch() {
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				TEXT_HTML, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(APPLICATION_YAML), //
				acceptHeaderMimeTypes(TEXT_HTML), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML), //
				acceptHeaderMimeTypes(ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, TEXT_TEXT), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM, APPLICATION_JSON, APPLICATION_YAML, ANY_APPLICATION), //
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM, APPLICATION_JSON), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML), //
				acceptHeaderMimeTypes(APPLICATION_OCTET_STREAM+Q_0_1, APPLICATION_JSON+Q_0), //
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);
	}
	
	// Order (if accepted):
		// 1) defaultMimeType
		// 2) Highest q parameter
		// 3) Listed first in accept
		// 4) BIAS
		// 5) Listed first in marshaller registry
	@Test
	public void testRulesOfOrder() {
		// Trivial case: TEXT_HTML is first in both accepted and supported and also default
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT),
				TEXT_HTML, //
				TEXT_HTML //
		);
		
		// Trivial case: TEXT_HTML is first in both accepted and supported and no default
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT),
				null, //
				TEXT_HTML //
		);
		
		// Rule 1) default value is supported and accepted
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT),
				APPLICATION_YAML, //
				APPLICATION_YAML //
		);
		
		// (1) default value is supported but not accepted
		// Rule 2) Highest supported q-param wins 
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0_1, APPLICATION_OCTET_STREAM+Q_0_8, TEXT_TEXT+Q_0_9),
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM //
		);
		
		// (1) default value is accepted but not supported
		// Rule 2) Highest supported q-param wins 
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_YAML, APPLICATION_OCTET_STREAM), //
				acceptHeaderMimeTypes(TEXT_HTML+Q_0_1, APPLICATION_OCTET_STREAM+Q_0_8, TEXT_TEXT+Q_0_9),
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM //
		);
		
		// (1) default value is accepted but not supported
		// (2) all q params same
		// Rule 3) APPLICATION_OCTET_STREAM is the first matching in accept
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(APPLICATION_YAML, APPLICATION_OCTET_STREAM, TEXT_TEXT, TEXT_HTML),
				APPLICATION_YAML, //
				APPLICATION_OCTET_STREAM //
		);
		
		// (1) default value is accepted but not supported
		// (2) all q params same
		// (3) No match between accepted and supported
		// Rule 4) INTERNAL_MIME_TYPE_BIAS
		testResolving( //
				marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT), //
				acceptHeaderMimeTypes(APPLICATION_YAML),
				APPLICATION_YAML, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		// (1) default value is accepted but not supported
		// (2) all q params same
		// (3) No match between accepted and supported
		// (4) No INTERNAL_MIME_TYPE_BIAS
		// Rule 5) First in marshaller registry wins
		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver(marshallerRegistryWithTypes(TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_TEXT));
		acceptHeaderResolver.setInternalMimeTypeBias(null);
		String resolvedMimeType = acceptHeaderResolver.resolveMimeType(
				acceptHeaderMimeTypes(APPLICATION_YAML), 
				APPLICATION_YAML);
		
		assertThat(resolvedMimeType).isEqualTo(TEXT_HTML);
		
	}
	
	@Test
	public void testBrowserStandardHeaderFirefoxImages() {
		List<String> firefoxHtmlAccept = acceptHeaderMimeTypes(TEXT_HTML, "application/xhtml+xml", "application/xml;q=0.9", "image/webp", "*/*;q=0.8");
		
		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/webp"), //
				firefoxHtmlAccept,
				TEXT_HTML, //
				"image/webp" //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg"), //
				firefoxHtmlAccept,
				TEXT_HTML, //
				"application/xml" //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg", "application/xhtml+xml", TEXT_HTML), //
				firefoxHtmlAccept,
				null, //
				TEXT_HTML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("application/xml", "image/jpeg", "application/xhtml+xml", TEXT_HTML), //
				firefoxHtmlAccept,
				"application/xhtml+xml", //
				"application/xhtml+xml" //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept,
				null, //
				INTERNAL_MIME_TYPE_BIAS //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept,
				"application/x-whatever", //
				"application/x-whatever" //
		);
		
		testResolving( //
				false,
				marshallerRegistryWithTypes("application/x-whatever"), //
				firefoxHtmlAccept,
				null, //
				"application/x-whatever" //
		);
	}
	
	@Test
	public void testBrowserStandardHeaderChromeHtml() {
		List<String> chromeImageAccept = acceptHeaderMimeTypes("image/webp", "image/apng", "image/*", "*/*;q=0.8");
		
		testResolving( //
				marshallerRegistryWithTypes("image/jpeg", "image/apng", "image/webp"), //
				chromeImageAccept,
				TEXT_HTML, //
				"image/webp" //
		);
		
		testResolving( //
				marshallerRegistryWithTypes("image/jpeg", "image/apng", "image/webp", TEXT_HTML), //
				chromeImageAccept,
				TEXT_HTML, //
				TEXT_HTML //
		);
		
		testResolving( //
				marshallerRegistryWithTypes( TEXT_HTML, "image/jpeg"), //
				chromeImageAccept,
				null, //
				"image/jpeg" //
		);
		
	}

	@Test
	public void testOnlyOneType() {
		testResolving( //
				false,
				marshallerRegistryWithTypes(APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				null, //
				APPLICATION_JSON //
		);

		testResolving( //
				false,
				marshallerRegistryWithTypes(APPLICATION_JSON), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				APPLICATION_JSON, //
				APPLICATION_JSON //
		);
	}

	@Test
	public void testEmptyMarshallerRegistry() {
		testResolving( //
				false, //
				emptyMarshallerRegistry(), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				null, //
				null //
		);

		testResolving( //
				false, //
				emptyMarshallerRegistry(), //
				acceptHeaderMimeTypes(APPLICATION_JSON), //
				APPLICATION_JSON, //
				null //
		);
		
		testResolving( //
				false, //
				emptyMarshallerRegistry(), //
				acceptHeaderMimeTypes(ANY_ANY), //
				APPLICATION_JSON, //
				null //
		);
		
		testResolving( //
				false, //
				emptyMarshallerRegistry(), //
				acceptHeaderMimeTypes(ANY_APPLICATION+Q_0_1), //
				APPLICATION_JSON, //
				null //
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

	private void testResolving(OrderedDummyMarshallerRegistry registry, List<String> originalAccepts, String defaultMimeType, String expectedMimeType) {
		testResolving(true, registry, originalAccepts, defaultMimeType, expectedMimeType);
	}
	
	private void testResolving(boolean withBias, OrderedDummyMarshallerRegistry registry, List<String> originalAccepts, String defaultMimeType, String expectedMimeType) {

		AcceptHeaderResolver acceptHeaderResolver = new AcceptHeaderResolver(registry);
		
		if (withBias) {
			acceptHeaderResolver.setInternalMimeTypeBias(INTERNAL_MIME_TYPE_BIAS);
			registry.registerMarshaller(INTERNAL_MIME_TYPE_BIAS);
		} else {
			acceptHeaderResolver.setInternalMimeTypeBias(null);
		}
		
		String resolvedMimeType = acceptHeaderResolver.resolveMimeType(originalAccepts, defaultMimeType);
		assertThat(resolvedMimeType).isEqualTo(expectedMimeType);
	}

}
