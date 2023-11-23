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
package com.braintribe.model.openapi.v3_0.export.legacytests;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.openapi.v3_0.OpenapiFormat;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiType;
import com.braintribe.model.processing.test.tools.comparison.PropertyByProperty;

public class SchemaAsserter {
	private final OpenapiSchema expected;
	private boolean simpleType;

	public SchemaAsserter(OpenapiSchema expected) {
		this.expected = expected;
	}

	public static SchemaAsserter schema(EntityType<?> entityType) {
		return schema(entityType.getTypeSignature(), OpenapiType.OBJECT);
	}

	public static SchemaAsserter schema(String title, OpenapiType type) {
		OpenapiSchema expected = OpenapiSchema.T.create();
		expected.setTitle(title);
		expected.setType(type);

		return new SchemaAsserter(expected);
	}

	public boolean isSimpleType() {
		return simpleType;
	}

	public SchemaAsserter clone(EntityType<?> entityType) {
		OpenapiSchema clonedExpected = expected.clone(new StandardCloningContext());
		SchemaAsserter newSchemaAsserter = new SchemaAsserter(clonedExpected);
		clonedExpected.setTitle(entityType.getTypeSignature());
		clonedExpected.setType(OpenapiType.OBJECT);

		return newSchemaAsserter;
	}

	public void test(OpenapiSchema schema) {
		PropertyByProperty.checkEqualityExcludingIds(schema, expected).assertThatEqual();
	}

	public void testUnwrapped(OpenapiSchema schema) {
		test(AbstractOpenapiProcessorTest.unwrap(schema));
	}

	public SchemaAsserter enumeration(String... constants) {
		expected.setEnum(Arrays.asList(constants));
		return this;
	}

	public SchemaAsserter enumProperty(String name, boolean required, Class<? extends Enum<?>> enumClass) {
		EnumType enumType = GMF.getTypeReflection().getEnumType(enumClass);
		List<String> enumConstants = Stream.of(enumType.getEnumValues()).map(e -> e.name()).collect(Collectors.toList());
		String[] enumConstantNames = enumConstants.toArray(new String[0]);
		return property(name, SchemaAsserter.schema(enumType.getTypeSignature(), OpenapiType.STRING).enumeration(enumConstantNames), required);
	}

	public SchemaAsserter stringProperty(String name, boolean required) {
		return stringProperty(name, required, null);
	}

	public SchemaAsserter stringProperty(String name, boolean required, String defaultValue) {
		return property(name, SchemaAsserter.stringSchema(), required, defaultValue);
	}

	public SchemaAsserter integerProperty(String name, boolean required) {
		return integerProperty(name, required, 0);
	}

	public static SchemaAsserter intSchema() {
		SchemaAsserter schema = SchemaAsserter.schema(null, OpenapiType.INTEGER);
		schema.expected.setFormat(OpenapiFormat.INT32);
		return schema;
	}

	public static SchemaAsserter stringSchema() {
		SchemaAsserter schema = SchemaAsserter.schema(null, OpenapiType.STRING);
		schema.simpleType = true;

		return schema;
	}

	public static SchemaAsserter booleanSchema() {
		SchemaAsserter schema = SchemaAsserter.schema(null, OpenapiType.BOOLEAN);
		schema.simpleType = true;

		return schema;
	}

	public static SchemaAsserter objectSchema() {
		SchemaAsserter schema = SchemaAsserter.schema("object", OpenapiType.OBJECT);
		schema.simpleType = true;

		return schema;
	}

	public static SchemaAsserter resourceSchema() {
		SchemaAsserter schema = SchemaAsserter.schema("com.braintribe.model.resource.Resource", OpenapiType.STRING);
		schema.expected.setFormat(OpenapiFormat.BINARY);

		return schema;
	}

	public SchemaAsserter integerProperty(String name, boolean required, Integer defaultValue) {
		return property(name, intSchema(), required, defaultValue);
	}

	public SchemaAsserter booleanProperty(String name, boolean required) {
		return booleanProperty(name, required, false);
	}
	public SchemaAsserter booleanProperty(String name, boolean required, Boolean defaultValue) {
		SchemaAsserter schema = SchemaAsserter.booleanSchema();
		return property(name, schema, required, defaultValue);
	}

	public SchemaAsserter property(String name, SchemaAsserter schema, boolean required) {
		return property(name, schema, required, null);
	}

	public SchemaAsserter property(String name, SchemaAsserter schema, boolean required, Object defaultValue) {
		if (required) {
			expected.getRequired().add(name);
		}

		OpenapiSchema propertySchema = schema.expected;
		expected.getProperties().put(name, propertySchema);
		propertySchema.setDefault(defaultValue);
		return this;
	}

	public SchemaAsserter setProperty(String name, SchemaAsserter itemsSchema, boolean required) {
		SchemaAsserter propertySchema = SchemaAsserter.schema("set", OpenapiType.ARRAY).items(itemsSchema);
		propertySchema.expected.setUniqueItems(true);
		return property(name, propertySchema, required);
	}

	public SchemaAsserter listProperty(String name, SchemaAsserter itemsSchema, boolean required) {
		SchemaAsserter propertySchema = SchemaAsserter.schema("list", OpenapiType.ARRAY).items(itemsSchema);
		return property(name, propertySchema, required);
	}

	public SchemaAsserter items(String title, OpenapiType type) {
		return items(schema(title, type));
	}

	public SchemaAsserter items(SchemaAsserter schema) {
		expected.setItems(schema.expected);
		return this;
	}

	public SchemaAsserter additionalProperties(SchemaAsserter schema) {
		expected.setAdditionalProperties(schema.expected);
		return this;
	}

	public SchemaAsserter additionalProperties() {
		return additionalProperties(SchemaAsserter.schema("object", OpenapiType.OBJECT));
	}

}
