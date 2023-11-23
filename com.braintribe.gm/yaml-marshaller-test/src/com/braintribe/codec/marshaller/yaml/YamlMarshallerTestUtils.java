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
package com.braintribe.codec.marshaller.yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public interface YamlMarshallerTestUtils {
	static void assertContent(String inputFile, Object expectation) throws IOException, FileNotFoundException {
		assertContent(inputFile, expectation, false);
	}

	static void assertContent(String inputFile, Object expectation, boolean v2) throws IOException, FileNotFoundException {
		assertContent(inputFile, expectation, v2, GmDeserializationOptions.defaultOptions);
	}

	static void assertContent(String inputFile, Object expectation, boolean v2, GmDeserializationOptions options)
			throws IOException, FileNotFoundException {
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(v2);

		Object parsedValue = null;

		try (InputStream in = new FileInputStream(inputFile)) {
			parsedValue = marshaller.unmarshall(in, options);
		}

		Assertions.assertThat(parsedValue).as("Marshaller did not generate expected elements").isEqualTo(expectation);
	}

	static byte[] marshallToByteArray(Object object, GmSerializationOptions marshallingOptions) {
		YamlMarshaller marshaller = new YamlMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		marshaller.marshall(baos, object, marshallingOptions);

		return baos.toByteArray();
	}

	static String marshallToString(Object object, GmSerializationOptions marshallingOptions) {
		YamlMarshaller marshaller = new YamlMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		marshaller.marshall(baos, object, marshallingOptions);

		return baos.toString();
	}

	static <T> T marshallingRoundTrip(Object object, GmSerializationOptions marshallingOptions) throws IOException {
		YamlMarshaller marshaller = new YamlMarshaller();

		try (ByteArrayInputStream in = new ByteArrayInputStream(marshallToByteArray(object, marshallingOptions))) {
			return (T) marshaller.unmarshall(in);
		}
	}
}
