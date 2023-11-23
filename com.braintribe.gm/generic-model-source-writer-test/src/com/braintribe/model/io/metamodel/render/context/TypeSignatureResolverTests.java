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
package com.braintribe.model.io.metamodel.render.context;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.io.metamodel.testbase.EntityTypeBuilder;
import com.braintribe.model.io.metamodel.testbase.MetaModelBuilder;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSetType;

/**
 * 
 */
public class TypeSignatureResolverTests {

	private TypeSignatureResolver typeSignatureResolver;

	private JavaType javaType;
	private GmEntityType entityType;

	private static final String SIMPLE_CLASS_NAME = "SimpleName";
	private static final String FULL_CLASS_NAME = MetaModelBuilder.COMMON_PACKAGE + "." + SIMPLE_CLASS_NAME;

	@Before
	public void setUp() throws Exception {
		buildMetaModel();
		typeSignatureResolver = new TypeSignatureResolver();
	}

	@Test
	public void testWorksForSimpleType() {
		javaType = typeSignatureResolver.resolveJavaType(MetaModelBuilder.intType);
		assertJavaType(Integer.class.getName());
	}

	@Test
	public void testWorksForBaseType() {
		javaType = typeSignatureResolver.resolveJavaType(MetaModelBuilder.baseType);
		assertJavaType(Object.class.getName());
		javaType = typeSignatureResolver.resolveJavaType(GmBaseType.T.create());
		assertJavaType(Object.class.getName());
	}

	@Test
	public void testWorksForEntityType() {
		javaType = typeSignatureResolver.resolveJavaType(entityType);
		assertJavaType(FULL_CLASS_NAME);
	}

	@Test
	public void testWorksForList() {
		javaType = typeSignatureResolver.resolveJavaType(getListType());
		assertJavaType("java.util.List", FULL_CLASS_NAME);
	}

	@Test
	public void testWorksForSet() {
		javaType = typeSignatureResolver.resolveJavaType(getSetType());
		assertJavaType("java.util.Set", FULL_CLASS_NAME);
	}

	@Test
	public void testWorksForMap() {
		javaType = typeSignatureResolver.resolveJavaType(getMapType());
		assertJavaType("java.util.Map", FULL_CLASS_NAME, FULL_CLASS_NAME);
	}

	private void assertJavaType(String rawType) {
		assertJavaType(rawType, null, null);
	}

	private void assertJavaType(String rawType, String elementType) {
		assertJavaType(rawType, null, elementType);
	}

	private void assertJavaType(String rawType, String keyType, String valueType) {
		assertThat(javaType.rawType).isEqualTo(rawType);
		assertThat(javaType.keyType).isEqualTo(keyType);
		assertThat(javaType.valueType).isEqualTo(valueType);
	}

	private GmCollectionType getListType() {
		GmListType result = GmListType.T.create();
		result.setElementType(entityType);
		return result;
	}

	private GmCollectionType getSetType() {
		GmSetType result = GmSetType.T.create();
		result.setElementType(entityType);
		return result;
	}

	private GmCollectionType getMapType() {
		GmMapType result = GmMapType.T.create();
		result.setKeyType(entityType);
		result.setValueType(entityType);
		return result;
	}

	private GmMetaModel buildMetaModel() {
		GmMetaModel gmMetaModel = GmMetaModel.T.create();

		entityType = new EntityTypeBuilder(FULL_CLASS_NAME).create();

		gmMetaModel.setTypes(asSet(entityType));

		return gmMetaModel;
	}

}
