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
package com.braintribe.swagger.writter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.SwaggerOperation;

public class SwaggerJsonWriter {

	private final Writer writer;

	private final boolean pretty;

	private int depth = 0;

	public SwaggerJsonWriter(Writer writer, boolean pretty) {
		this.writer = writer;
		this.pretty = pretty;
	}

	public void write(SwaggerApi api) throws IOException {
		entity(api);
	}

	private void map(Map<String, ?> map, MapType type) throws IOException {
		object(map.keySet(), map::get, key -> type.getValueType());
	}

	private void entity(GenericEntity entity) throws IOException {
		final EntityType<?> entityType = entity.entityType();

		if (entity instanceof SwaggerApi) {
			entityProperties(entity, "swagger", "info", "basePath", "paths", "definitions", "tags");
		} else if (entity instanceof SwaggerOperation) {
			entityProperties(entity, "summary", "description", "tags", "parameters", "responses");
		} else {
			String[] props = entityType.getProperties().stream().map(prop -> prop.getName()).toArray(String[]::new);
			entityProperties(entity, props);
		}
	}

	private void entityProperties(GenericEntity entity, String... properties) throws IOException {
		final EntityType<?> entityType = entity.entityType();

		object(Arrays.asList(properties), prop -> {
			Property property = entityType.getProperty(prop);
			return property.get(entity);
		}, prop -> {
			Property property = entityType.getProperty(prop);
			return property.getType();
		});
	}

	private void jsonNode(Object value, GenericModelType type) throws IOException {
		switch (type.getTypeCode()) {
			case booleanType:
				writer.write(((Boolean) value) ? "true" : "false");
				break;
			case entityType:
				entity((GenericEntity) value);
				break;
			case integerType:
				writer.write((Integer) value);
				break;
			case listType:
				array((List<Object>) value, ((ListType) type).getCollectionElementType());
				break;
			case longType:
				break;
			case mapType:
				map((Map<String, ?>) value, (MapType) type);
				break;
			case stringType:
				writeEscapedString((String) value);
				break;
			case decimalType:
				HttpExceptions.internalServerError("Unsupported decimalType.");
				break;
			case doubleType:
				HttpExceptions.internalServerError("Unsupported doubleType.");
				break;
			case dateType:
				HttpExceptions.internalServerError("Unsupported dateType.");
				break;
			case enumType:
				HttpExceptions.internalServerError("Unsupported enumType.");
				break;
			case floatType:
				HttpExceptions.internalServerError("Unsupported floatType.");
				break;
			case setType:
				HttpExceptions.internalServerError("Unsupported setType.");
				break;
			case objectType:
				HttpExceptions.internalServerError("Unsupported objectType.");
				break;
			default:
				HttpExceptions.internalServerError("Unsupported type %s.", type);
		}
	}

	private void writeEscapedString(String value) throws IOException {
		// Reference to the single quoted yaml string style: https://yaml.org/spec/current.html#id2534365
		String escapedString = value.replace("'", "''");
		writer.write('\'');
		writer.write(escapedString);
		writer.write('\'');
	}

	private void object(Collection<String> keys, Function<String, Object> values, Function<String, GenericModelType> types) throws IOException {
		writer.write('{');
		depth++;
		writePretty();

		boolean first = true;
		for (String key : keys) {
			Object value = values.apply(key);
			if (isEmpty(value)) {
				continue;
			}

			if (!first) {
				writer.write(',');
				writePretty();
			}
			writeEscapedString(key);
			writer.write(": ");
			jsonNode(value, types.apply(key));

			first = false;
		}

		depth--;
		writePretty();
		writer.write('}');
	}

	private void array(List<Object> values, GenericModelType type) throws IOException {
		writer.write('[');
		depth++;
		writePretty();

		boolean first = true;
		for (Object value : values) {
			if (!first) {
				writer.write(',');
				writePretty();
			}
			jsonNode(value, type);

			first = false;
		}

		depth--;
		writePretty();
		writer.write(']');
	}

	private boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}

		if (value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		}
		if (value instanceof Map) {
			return ((Map<?, ?>) value).isEmpty();
		}

		return false;
	}

	private void writePretty() throws IOException {
		if (pretty) {
			writer.write('\n');
			writeDepth();
		}
	}

	private void writeDepth() throws IOException {
		for (int i = 0; i < depth; i++) {
			writer.write(' ');
			writer.write(' ');
		}
	}
}
