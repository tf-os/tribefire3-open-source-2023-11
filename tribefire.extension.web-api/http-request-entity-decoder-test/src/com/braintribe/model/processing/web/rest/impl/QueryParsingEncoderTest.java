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
package com.braintribe.model.processing.web.rest.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoderOptions;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.PrimitiveTypesEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.testing.model.test.technical.features.SimpleTypesEntity;

public class QueryParsingEncoderTest {
	QueryParamEncoder encoder = new QueryParamEncoder();
	
	@Test
	public void testMixed() {
		QueryParamEncoder encoder = new QueryParamEncoder();
		
		GmType type1 = GmListType.T.create();
		type1.setTypeSignature("Test type signature");
		GmMetaModel model = GmMetaModel.T.create();
		type1.setDeclaringModel(model);
		model.setName("Model Name");
		model.getTypes().add(type1);
		
		encoder.registerTarget("model", model);
		
		String encoded = encoder.encode();
		
		GmMetaModel decodedModel = GmMetaModel.T.create();
		
		QueryParamDecoder decoder = new QueryParamDecoder(HttpRequestEntityDecoderOptions.defaults());
		decoder.registerTarget("model", decodedModel);
		decoder.decode(encoded);
		
		assertThat(model.getName()).isEqualTo(decodedModel.getName());
		assertThat(model.getTypes()).hasSameSizeAs(decodedModel.getTypes());
		GmType firstType = decodedModel.getTypes().iterator().next();
		assertThat(model.getTypes()).extracting("typeSignature").containsExactly(firstType.getTypeSignature());
		assertThat(decodedModel).isEqualTo(firstType.getDeclaringModel());
		
	}
	
	@Test
	public void testNoParams() {
		SimpleEntity simpleEntity = SimpleEntity.T.create();
		String encoded = encode(simpleEntity);
		
		assertThat(encoded).isEmpty();
	}
	
	@Test
	public void testMaps() {
		CollectionEntity entity = CollectionEntity.T.create();
		entity.getIntegerToIntegerMap().put(1, 1);
		entity.getIntegerToIntegerMap().put(-1, 0);
		entity.getIntegerToIntegerMap().put(876, -9);
		
		CollectionEntity decoded = roundTrip(entity);
		
		assertThat(entity.getIntegerToIntegerMap()).isEqualTo(decoded.getIntegerToIntegerMap());

		entity = CollectionEntity.T.create();

		SimpleEntity simpleEntity1 = SimpleEntity.T.create();
		ComplexEntity complexEntity1 = ComplexEntity.T.create();
		entity.getSimpleEntityToComplexEntityMap().put(simpleEntity1, complexEntity1);
		
		SimpleEntity simpleEntity2 = SimpleEntity.T.create();
		simpleEntity2.setBooleanProperty(true);
		simpleEntity2.setStringProperty("simpleEntity2");
		ComplexEntity complexEntity2 = ComplexEntity.T.create();
		complexEntity2.setDoubleProperty(12.8756);
		complexEntity2.setSimpleEnum(SimpleEnum.FOUR);
		
		entity.getSimpleEntityToComplexEntityMap().put(simpleEntity2, complexEntity2);
		
		SimpleEntity simpleEntity3 = SimpleEntity.T.create();
		entity.getSimpleEntityToComplexEntityMap().put(simpleEntity3, complexEntity2);
		
		decoded = roundTrip(entity);
		
		assertThat(entity.getIntegerToIntegerMap()).isEqualTo(decoded.getIntegerToIntegerMap());
	}
	
	@Test
	public void testSimpleTypes() {
		SimpleTypesEntity simpleTypesEntity = SimpleTypesEntity.T.create();
		simpleTypesEntity.setBooleanProperty(true);
		simpleTypesEntity.setDoubleProperty(1.234);
		simpleTypesEntity.setFloatProperty(1.543f);
		simpleTypesEntity.setIntegerProperty(12);
		simpleTypesEntity.setLongProperty(Long.MIN_VALUE);
		simpleTypesEntity.setStringProperty("test string");
		
		roundTrip(simpleTypesEntity);
		
		assertThat(encode(simpleTypesEntity)).isEqualTo("test.booleanProperty=true&test.doubleProperty=1.234D&test.floatProperty=1.543F&test.integerProperty=12&test.longProperty=-9223372036854775808&test.stringProperty=test string");
		
		PrimitiveTypesEntity primitiveTypesEntity = PrimitiveTypesEntity.T.create();
		primitiveTypesEntity.setPrimitiveBooleanProperty(true);
		primitiveTypesEntity.setPrimitiveDoubleProperty(1.234);
		primitiveTypesEntity.setPrimitiveFloatProperty(1.543f);
		primitiveTypesEntity.setPrimitiveIntegerProperty(12);
		primitiveTypesEntity.setPrimitiveLongProperty(Long.MIN_VALUE);
		
		roundTrip(primitiveTypesEntity);
		
		assertThat(encode(primitiveTypesEntity)).isEqualTo("test.primitiveBooleanProperty=true&test.primitiveDoubleProperty=1.234D&test.primitiveFloatProperty=1.543F&test.primitiveIntegerProperty=12&test.primitiveLongProperty=-9223372036854775808");
	}
	
	@Test
	@Category(KnownIssue.class) // Unignore when date parsing works and finish test
	public void testDate() {
		SimpleTypesEntity simpleTypesEntity = SimpleTypesEntity.T.create();
		simpleTypesEntity.setDateProperty(new Date());
		
		SimpleTypesEntity roundTrippedEntity = roundTrip(simpleTypesEntity);
		System.out.println(roundTrippedEntity.getDateProperty());
	}
	
	@Test
	public void testObject() {
		SimpleEntity entity = SimpleEntity.T.create();
		entity.setStringProperty("simple property string");
		
		Map<Object, Object> map = new HashMap<>();
		HashSet<Object> set = new HashSet<>();
		List<? extends Object> list = Arrays.asList("1",2);
		set.add(1);
		set.add("ü");
		map.put(list, 999);
		map.put("12345", set);
		
		testScalarObject(-42);
		testScalarObject(-42L);
		testScalarObject(1.234D);
		testScalarObject(1.234F);
		testScalarObject("String de la Test");
		testScalarObject(true);
		testScalarObject(SimpleEnum.FIVE);
		testScalarObject(list);
		testScalarObject(set);
		testScalarObject(map);
		testObject(entity);
		
		ComplexEntity complexEntity = ComplexEntity.T.create();
		complexEntity.setObjectProperty(complexEntity);
		
		ComplexEntity roundTripped = roundTrip(complexEntity);
		assertThat(roundTripped.getObjectProperty()).isEqualTo(roundTripped);
	}
	
	private void testScalarObject(Object object) {
		Object roundTrippedObjectPropertyValue = testObject(object);
		
		assertThat(roundTrippedObjectPropertyValue).isEqualTo(object);
	}
	
	private Object testObject(Object object) {
		ComplexEntity complexEntity = ComplexEntity.T.create();
		complexEntity.setObjectProperty(object);
		
		ComplexEntity roundTripped = roundTrip(complexEntity);
		
		return roundTripped.getObjectProperty();
		
	}
	
	private <T extends GenericEntity> T roundTrip(T source) {
		return decode(encode(source), source.entityType());
	}
	
	private <T extends GenericEntity> T decode(String encoded, EntityType<T> targetType) {
		QueryParamDecoder decoder = new QueryParamDecoder(HttpRequestEntityDecoderOptions.defaults());
		T target = targetType.create();
		decoder.registerTarget("test", target);
		decoder.decode(encoded);
		// uncomment for debugging
		// System.out.println(target);
		
		return target;
	}
	
	private String encode(GenericEntity entity) {
		encoder.registerTarget("test", entity);
		String encoded = encoder.encode();
		// uncomment for debugging
		// System.out.println(encoded.replaceAll("&", "\n&"));
		return encoded;
	}
}
