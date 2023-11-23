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
package com.braintribe.model.processing.wopi.misc;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Helper class for JSON operations
 */
public class JsonUtils {

	private static final ObjectMapper JSON_MAPPER;
	private static final JsonFactory JSON_FACTORY;

	static {
		JSON_MAPPER = new ObjectMapper();
		JSON_MAPPER.setSerializationInclusion(Include.NON_NULL);
		JSON_MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);
		JSON_FACTORY = new JsonFactory(JSON_MAPPER);
	}

	public static JsonGenerator createGenerator(OutputStream out) throws IOException {
		return JSON_FACTORY.createGenerator(out);
	}

	public static <T> T readValue(String content, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
		return JSON_MAPPER.readValue(content, valueType);
	}

	public static void writeValue(OutputStream out, Object value) throws JsonGenerationException, JsonMappingException, IOException {
		JSON_MAPPER.writeValue(out, value);
	}

	public static String writeValueAsString(Object value) throws JsonProcessingException {
		return JSON_MAPPER.writeValueAsString(value);
	}

}
