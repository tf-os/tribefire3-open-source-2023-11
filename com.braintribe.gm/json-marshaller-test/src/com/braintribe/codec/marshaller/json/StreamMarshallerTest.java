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
package com.braintribe.codec.marshaller.json;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.DateFormatOption;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.EntityRecurrenceDepth;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.IdTypeSupplier;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.IdentityManagementModeOption;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.PropertyDeserializationTranslation;
import com.braintribe.codec.marshaller.api.PropertySerializationTranslation;
import com.braintribe.codec.marshaller.api.PropertyTypeInferenceOverride;
import com.braintribe.codec.marshaller.api.StringifyNumbersOption;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.json.model.AccountAccess;
import com.braintribe.codec.marshaller.json.model.AddressEntity;
import com.braintribe.codec.marshaller.json.model.CityEntity;
import com.braintribe.codec.marshaller.json.model.Consent;
import com.braintribe.codec.marshaller.json.model.CountryEntity;
import com.braintribe.codec.marshaller.json.model.PerformanceCertificate;
import com.braintribe.codec.marshaller.json.model.PersonEntity;
import com.braintribe.codec.marshaller.json.model.SimpleEntity;
import com.braintribe.codec.marshaller.json.model.TestEntity;
import com.braintribe.codec.marshaller.json.model.TestEnum;
import com.braintribe.codec.marshaller.json.model.abstractentities.BasicEntity;
import com.braintribe.codec.marshaller.json.model.abstractentities.FirstLevelAbstract;
import com.braintribe.codec.marshaller.json.model.abstractentities.FirstLevelImpl;
import com.braintribe.codec.marshaller.json.model.abstractentities.SecondLevelImpl;
import com.braintribe.codec.marshaller.json.model.abstractentities.ThirdLevelImpl;
import com.braintribe.codec.marshaller.json.model.pets.Category;
import com.braintribe.codec.marshaller.json.model.pets.Pet;
import com.braintribe.codec.marshaller.json.model.pets.Tag;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.collection.PlainSet;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.type.collection.SetTypeImpl;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.test.tools.comparison.PropertyByProperty;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.SqlSource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.genericmodel.GmTools;
import com.braintribe.utils.lcd.StringTools;

public class StreamMarshallerTest {

	@Test
	public void testCustomDateFormat() {
		String pattern = "yyyy-MM-dd";

		String json = "{ \"_type\": \"date\", \"value\": \"1992-10-10\" }";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Date dateRead = (Date) marshaller.decode(json, GmDeserializationOptions.deriveDefaults().set(DateFormatOption.class, pattern).build());

		LocalDate localDate = LocalDate.of(1992, 10, 10);
		Date date = Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));

		Assert.assertEquals(date, dateRead);

		String jsonOut = marshaller.encode(dateRead, GmSerializationOptions.deriveDefaults().set(DateFormatOption.class, pattern).build());

		Assert.assertEquals("{\"value\":\"1992-10-10\", \"_type\":\"date\"}", jsonOut);
	}

	@Test
	public void testPropertyTranslation() {

		String jsonList = "[{\"LnNummer\":\"1234588\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"999999_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"fahrer1@prangl.at\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"DE\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"},{\"LnNummer\":\"1234588\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"999999_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"fahrer1@prangl.at\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"DE\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"},{\"LnNummer\":\"1234588\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"999999_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"fahrer1@prangl.at\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"DE\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"},{\"LnNummer\":\"123456\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"789555_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"fahrer1@prangl.at\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"DE\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"},{\"LnNummer\":\"123456\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"789555_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"}]";
		String json = "{\"LnNummer\":\"1234588\",\"KundeMail\":\"testkunde@test.com\",\"KundeMailOptional\":\"mail1@mail.com\",\"Kundenfeedback\":6,\"Auftragsnummer\":\"999999_212\",\"Tatigkeitsnummer\":\"8888888\",\"Leistungsdatum\":\"2022-06-08T00:00:00\",\"DurchgefuhrteArbeit\":\"Bedienen\",\"Standort\":\"AT01\",\"Abteilung\":\"010_Kran\",\"FahrerName\":\"Fahrer 1\",\"FahrerEmail\":\"fahrer1@prangl.at\",\"Naechtigung\":\"N\",\"UhrzeitVerlassen\":\"14:00:00\",\"Zielland\":\"DE\",\"ZeitRueckkehr\":\"15:00:00\",\"Dokument\":\"https://.....\"}";
		//@formatter:off
		
		
		BiFunction<EntityType<?>, String, Property> propertySupplier = (type,propertyName) -> {
			String translatedPropertyName = StringTools.uncapitalize(propertyName);
			return type.findProperty(translatedPropertyName);
		}; 
		
		Function<Property, String> propertyNameSupplier = (property) -> {
			return StringTools.capitalize(property.getName());
		}; 
		
		
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		
		PerformanceCertificate parsedEntity = (PerformanceCertificate) marshaller.decode(json, 
				GmDeserializationOptions.deriveDefaults()
					.setInferredRootType(PerformanceCertificate.T)
					.set(PropertyDeserializationTranslation.class, propertySupplier)
				.build());
		System.out.println(parsedEntity);
		
		
		@SuppressWarnings("unchecked")
		List<PerformanceCertificate> parsedEntities = (List<PerformanceCertificate>) marshaller.decode(jsonList, 
				GmDeserializationOptions.deriveDefaults()
					.setInferredRootType(GMF.getTypeReflection().getType("list<"+PerformanceCertificate.T.getTypeSignature()+">"))
					.set(PropertyDeserializationTranslation.class, propertySupplier)
				.build());
		
		parsedEntities.stream().forEach(System.out::println);
		
		String jsonOut = marshaller.encode(parsedEntity, 
				GmSerializationOptions.deriveDefaults()
					.set(PropertySerializationTranslation.class, propertyNameSupplier)
					//.set(DateFormatOption.class, pattern)
				.build());

		System.out.println(jsonOut);
		
		//@formatter:on

	}

	@Test
	public void testPropertyTypeInferenceOverride() throws IOException {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		Map<Property, GenericModelType> inferences = new HashMap<>();
		inferences.put(BasicEntity.T.getProperty("firstLevelAbstract"), FirstLevelImpl.T);
		inferences.put(FirstLevelImpl.T.getProperty("secondLevelAbstract"), SecondLevelImpl.T);
		inferences.put(SecondLevelImpl.T.getProperty("thirdLevelAbstract"), ThirdLevelImpl.T);
		inferences.put(BasicEntity.T.getProperty("firstLevelAbstracts"), GMF.getTypeReflection().getListType(FirstLevelImpl.T));

		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(BasicEntity.T)
				.set(PropertyTypeInferenceOverride.class, (t, p) -> {
					return inferences.getOrDefault(p, p.getType());
				}).build();

		final BasicEntity basicEntity;

		try (InputStream in = new FileInputStream("res/property-type-inference-override.json")) {
			basicEntity = (BasicEntity) marshaller.unmarshall(in, options);
		}

		FirstLevelImpl firstLevel = (FirstLevelImpl) basicEntity.getFirstLevelAbstract();
		SecondLevelImpl secondLevel = (SecondLevelImpl) firstLevel.getSecondLevelAbstract();
		ThirdLevelImpl thirdLevel = (ThirdLevelImpl) secondLevel.getThirdLevelAbstract();

		Assert.assertEquals("Meister Eckhart", thirdLevel.getName());

		List<FirstLevelAbstract> firstLevelAbstracts = basicEntity.getFirstLevelAbstracts();

		FirstLevelImpl i1 = (FirstLevelImpl) firstLevelAbstracts.get(0);
		FirstLevelImpl i2 = (FirstLevelImpl) firstLevelAbstracts.get(1);

		Assert.assertEquals(4711, i1.getNumber());
		Assert.assertEquals(815, i2.getNumber());
	}

	@Test
	public void testPrimitives() throws Exception {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/listOfScalars.json")) {
			parsedValue = marshaller.unmarshall(in);
		}

		System.out.println(GmTools.getDescriptionForObject(parsedValue));
	}

	@Test
	public void testDeepTreeInference() throws Exception {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/inferred-entity-tree.json")) {
			parsedValue = marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().setInferredRootType(TestEntity.T).build());
		}

		System.out.println(GmTools.getDescriptionForObject(parsedValue));
	}

	@Test
	public void testOutput() throws Exception {
		TestEntity e1 = TestEntity.T.create();
		TestEntity e2 = TestEntity.T.create();

		e1.setEntityValue(e2);
		e1.getStringSet().addAll(Arrays.asList("one", "two", "three"));

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(System.out, e1, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.outputPrettiness(OutputPrettiness.high).build());
	}

	@Test
	public void testLs() throws Exception {
		String json = "{\"_type\": \"com.braintribe.model.generic.i18n.LocalizedString\", \"_id\": \"86\","
				+ "\"globalId\": \"ls:SelectiveInformation:type:com.braintribe.model.meta.GmSimpleType\","
				+ "\"localizedValues\": {\"_type\": \"map\", \"value\":[" + "{\"key\":\"default\", \"value\":\"${typeSignature}\"}" + "]}}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		LocalizedString ls = (LocalizedString) marshaller.decode(json);

		Map<String, String> expectedValues = new HashMap<>();
		expectedValues.put("default", "${typeSignature}");

		Map<String, String> actualValues = ls.getLocalizedValues();

		Assert.assertEquals(expectedValues, actualValues);
	}

	@Test
	public void testMap() throws Exception {
		String json = "{\"_type\": \"map\", \"value\":[" + "{\"key\":\"default\", \"value\":\"${typeSignature}\"}" + "]}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Object value = marshaller.decode(json);

		Map<String, String> expectedValues = new HashMap<>();
		expectedValues.put("default", "${typeSignature}");

		Map<String, String> actualValues = (Map<String, String>) value;

		Assert.assertEquals(expectedValues, actualValues);
	}

	@Test
	public void testSetRoundtrip() throws Exception {
		User user = User.T.create();
		user.setGlobalId("g1");
		Role role = Role.T.create();
		role.setGlobalId("g2");
		role.setName("tf-admin");
		Group group = Group.T.create();
		group.setName("group");
		group.setGlobalId("g3");

		user.getRoles().add(role);
		user.getGroups().add(group);

		StringWriter writer = new StringWriter();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(writer, user, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid)
				.set(TypeExplicitnessOption.class, TypeExplicitness.always).build());

		User trippedUser = null;

		try (StringReader reader = new StringReader(writer.toString())) {
			trippedUser = (User) marshaller.unmarshall(reader, GmDeserializationOptions.deriveDefaults().build());
		}

		Assert.assertTrue(AssemblyComparison.build().useGlobalId().compare(user, trippedUser).equal());
	}

	@Test
	public void testAbsentPropertiesRoundtrip() throws Exception {
		User user = User.T.create();
		user.setGlobalId("g1");
		Role role = Role.T.create();
		role.setGlobalId("g2");
		role.setName("tf-admin");

		user.getRoles().add(role);

		StringWriter writer = new StringWriter();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults() //
				.outputPrettiness(OutputPrettiness.mid) //
				.writeAbsenceInformation(true) //
				.set(TypeExplicitnessOption.class, TypeExplicitness.always) //
				.build();

		marshaller.marshall(writer, user, options);

		System.out.println(writer.toString());
		User trippedUser = null;

		try (StringReader reader = new StringReader(writer.toString())) {
			trippedUser = (User) marshaller.unmarshall(reader, GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(true).build());
		}

		Property userNameProperty = User.T.findProperty("name");
		assertThat(userNameProperty.isAbsent(trippedUser)).as("User name should be absent but it's " + userNameProperty.get(trippedUser)).isTrue();
		assertThat(user.getName()).isNull();

		assertThat(user.getRoles()).isNotEmpty();
		assertThat(user.getRoles()).first().extracting(Role::getName).isEqualTo("tf-admin");

		Property userGroupsProperty = User.T.findProperty("groups");
		assertThat(userGroupsProperty.isAbsent(trippedUser)).as("User groups should be absent but it's " + userGroupsProperty.get(trippedUser))
				.isTrue();
		assertThat(user.getGroups()).isNotNull().isEmpty();
	}

	@Test
	public void testFlatMap() throws Exception {
		TestEntity inputEntity = TestEntity.T.create();
		inputEntity.getEnumMap().put(TestEnum.TWO, "enumString");
		inputEntity.getEnumMap().put(TestEnum.THREE, "enumString2");

		inputEntity.getObjectStringMap().put(TestEnum.TWO, "enumString");
		inputEntity.getObjectStringMap().put(TestEnum.THREE, "enumString2");

		inputEntity.getStringMap().put(TestEnum.TWO.name(), "enumString");
		inputEntity.getStringMap().put(TestEnum.THREE.name(), "enumString2");

		inputEntity.getObjectObjectMap().put(TestEnum.ONE, TestEnum.ONE);
		inputEntity.getObjectObjectMap().put(TestEnum.TWO, TestEnum.TWO);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		marshaller.marshall(out, inputEntity, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.always)
				.outputPrettiness(OutputPrettiness.high).build());
		String actualJsonWithTypeProperty = out.toString();

		TestEntity valueWithTypeProperty = (TestEntity) marshaller.decode(actualJsonWithTypeProperty);

		PropertyByProperty.checkEquality(valueWithTypeProperty, inputEntity).assertThatEqual();

		out.reset();
		marshaller.marshall(out, inputEntity, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.outputPrettiness(OutputPrettiness.high).build());
		String actualJson = out.toString();
		TestEntity entity = (TestEntity) marshaller.decode(actualJson);
		PropertyByProperty.checkEquality(entity, inputEntity).assertThatEqual();

	}

	@Test
	public void testSeqMap() throws Exception {
		String json = "{\"_type\": \"flatmap\", \"value\":[" + "\"default\", \"${typeSignature}\"" + "]}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Object value = marshaller.decode(json);

		Map<String, String> expectedValues = new HashMap<>();
		expectedValues.put("default", "${typeSignature}");

		Map<String, String> actualValues = (Map<String, String>) value;

		Assert.assertEquals(expectedValues, actualValues);
	}

	@Test
	public void testDirectMap() throws Exception {
		String json = "{\"_type\": \"map\", \"value\":{" + "\"default\": \"${typeSignature}\"" + "}}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Object value = marshaller.decode(json);

		Map<String, String> expectedValues = new HashMap<>();
		expectedValues.put("default", "${typeSignature}");

		Map<String, String> actualValues = (Map<String, String>) value;

		Assert.assertEquals(expectedValues, actualValues);
	}

	@Test
	public void testPolymorphicTypeExplicitnessOutput() throws Exception {
		TestEntity e1 = TestEntity.T.create();
		TestEntity e2 = TestEntity.T.create();

		setSimpleTypes(e1);
		setObjectTypes(e1);
		setSets(e1);
		setLists(e1);
		setMaps(e1);
		e1.setEntityValue(e2);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		marshaller.marshall(out, e1, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.outputPrettiness(OutputPrettiness.high).build());
		String expected = out.toString();
		TestEntity entity = (TestEntity) marshaller.decode(expected);
		PropertyByProperty.checkEquality(entity, e1).assertThatEqual();
	}

	@Test
	public void testAlwaysTypeExplicitnessOutput() throws Exception {
		TestEntity e1 = TestEntity.T.create();
		TestEntity e2 = TestEntity.T.create();

		setSimpleTypes(e1);
		setObjectTypes(e1);
		setSets(e1);
		setLists(e1);
		setMaps(e1);
		e1.setEntityValue(e2);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		marshaller.marshall(out, e1, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.always)
				.outputPrettiness(OutputPrettiness.high).build());
		String expected = out.toString();
		TestEntity entity = (TestEntity) marshaller.decode(expected);
		PropertyByProperty.checkEquality(entity, e1).assertThatEqual();
	}

	@Test
	public void testNeverTypeExplicitnessOutput() throws Exception {
		TestEntity e1 = TestEntity.T.create();
		TestEntity e2 = TestEntity.T.create();

		setSimpleTypes(e1);
		setObjectTypes(e1);
		setSets(e1);
		setLists(e1);
		setMaps(e1);
		e1.setEntityValue(e2);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		marshaller.marshall(out, e1, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.never)
				.outputPrettiness(OutputPrettiness.high).build());
		String marshalled = out.toString();

		assertThat(marshalled).doesNotContain("_type");

		Map<String, Object> map = (Map<String, Object>) marshaller.decode(marshalled);

		assertMapEqualsEntity(e1, map);
	}

	private void assertMapEqualsEntity(GenericEntity e1, Map<String, Object> map) {
		map.forEach((k, v) -> {
			if (k.startsWith("_")) {
				assertThat(k).isNotEqualTo("_type");
				return;
			}
			Property property = TestEntity.T.getProperty(k);
			Object value = property.get(e1);

			if (property.getType().isEntity()) {
				assertMapEqualsEntity((GenericEntity) value, (Map<String, Object>) v);
			} else {
				assertThat(v.toString()).isEqualTo(value.toString());
			}
		});
	}

	@Test
	public void testEntityRecurrenceOutput() throws Exception {
		CountryEntity serbia = CountryEntity.T.create();
		serbia.setName("Serbia");
		serbia.setCode("SER");

		CountryEntity germany = CountryEntity.T.create();
		germany.setName("Germany");
		germany.setCode("GER");

		CityEntity belgrade = CityEntity.T.create();
		belgrade.setName("Belgrade");
		belgrade.setCountry(serbia);

		CityEntity berlin = CityEntity.T.create();
		berlin.setName("Berlin");
		berlin.setCountry(germany);

		AddressEntity vladimirsAddress = AddressEntity.T.create();
		vladimirsAddress.setCity(belgrade);
		vladimirsAddress.setHouseNumber(5);
		vladimirsAddress.setStreet("Upper Street");

		AddressEntity dirksAddress = AddressEntity.T.create();
		dirksAddress.setCity(berlin);
		dirksAddress.setHouseNumber(15);
		dirksAddress.setStreet("Upper Berlin Street");

		PersonEntity vladimir = PersonEntity.T.create();
		vladimir.setFirstName("Vladimir");
		vladimir.setLastName("Bankovic");
		vladimir.setAddress(vladimirsAddress);

		PersonEntity dirk = PersonEntity.T.create();
		dirk.setFirstName("Dirk");
		dirk.setLastName("Scheffler");
		dirk.setAddress(dirksAddress);

		vladimir.setFriend(dirk);
		dirk.setFriend(vladimir);

		List<PersonEntity> persons = new LinkedList<>();
		persons.add(vladimir);
		persons.add(dirk);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/recurrenceDepth-1.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			marshaller.marshall(out, persons,
					GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
							.set(EntityRecurrenceDepth.class, -1).writeEmptyProperties(true).stabilizeOrder(true)
							.outputPrettiness(OutputPrettiness.high).build());
			String actual = out.toString();
			assertEquals(expected, actual);
		}

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/recurrenceDepth0.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			marshaller.marshall(out, persons, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
					.set(EntityRecurrenceDepth.class, 0).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());
			String actual = out.toString();
			assertEquals(expected, actual);
		}

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/recurrenceDepth1.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			marshaller.marshall(out, persons, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
					.set(EntityRecurrenceDepth.class, 1).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());
			String actual = out.toString();
			assertEquals(expected, actual);
		}

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/recurrenceDepth2.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			marshaller.marshall(out, persons, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
					.set(EntityRecurrenceDepth.class, 2).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());
			String actual = out.toString();
			assertEquals(expected, actual);
		}

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/recurrenceDepth3.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			marshaller.marshall(out, persons, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
					.set(EntityRecurrenceDepth.class, 3).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());
			String actual = out.toString();
			assertEquals(expected, actual);
		}

	}

	@Test
	public void testEntityDuplication() throws Exception {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/pets.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
					.set(IdentityManagementModeOption.class, IdentityManagementMode.id) //
					.setInferredRootType(GMF.getTypeReflection().getListType(Pet.T)).build();
			List<Pet> result = (List<Pet>) marshaller.decode(expected, options);
			Pet p0 = result.get(0);
			Pet p1 = result.get(1);
			assertTrue(p0 != p1);
			assertTrue(p0.getCategory() == p1.getCategory());
		}
	}

	@Test
	public void testEntityDuplicationOff() throws Exception {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/pets.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
					.set(IdentityManagementModeOption.class, IdentityManagementMode.off) //
					.setInferredRootType(GMF.getTypeReflection().getListType(Pet.T)).build();
			List<Pet> result = (List<Pet>) marshaller.decode(expected, options);
			Pet p0 = result.get(0);
			Pet p1 = result.get(1);
			assertTrue(p0 != p1);
			assertTrue(p0.getCategory() != p1.getCategory());
			PropertyByProperty.checkEquality(p0.getCategory(), p1.getCategory()).assertThatEqual();
		}

	}

	@Test
	public void testEntityDuplicationAuto() throws Exception {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/pets.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
					.set(IdentityManagementModeOption.class, IdentityManagementMode.auto) //
					.setInferredRootType(GMF.getTypeReflection().getListType(Pet.T)).build();
			List<Pet> result = (List<Pet>) marshaller.decode(expected, options);
			Pet p0 = result.get(0);
			Pet p1 = result.get(1);
			assertTrue(p0 != p1);
			assertTrue(p0.getCategory() == p1.getCategory());
		}

		try (BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(new FileInputStream("res/pets_with_ref.json")))) {
			String expected = inputBuffer.lines().collect(Collectors.joining("\n"));
			GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
					.set(IdentityManagementModeOption.class, IdentityManagementMode.auto) //
					.setInferredRootType(GMF.getTypeReflection().getListType(Pet.T)).build();
			List<Pet> result = (List<Pet>) marshaller.decode(expected, options);
			Pet p0 = result.get(0);
			Pet p1 = result.get(1);
			assertTrue(p0 != p1);
			assertTrue(p0.getCategory() == p1.getCategory());
		}

	}

	@Test
	public void testAbstractClassMarshalling() {
		Resource resource = Resource.T.create();
		ResourceSource resourceSource = SqlSource.T.create();
		resourceSource.setId("1234");

		resource.setCreated(new Date());
		resource.setCreator("CORTEX");
		resource.setFileSize(100L);
		resource.setGlobalId("123456789");
		resource.setId("123");
		resource.setMd5("MD5");
		resource.setMimeType("application/json");
		resource.setName("TEST");
		resource.setPartition("cortex");
		resource.setResourceSource(resourceSource);

		// test TypeExplicitness.entities
		testAbstractEntityMarshaller(resource, TypeExplicitness.entities, 0);

		// test TypeExplicitness.auto
		testAbstractEntityMarshaller(resource, TypeExplicitness.auto, 0);

		// test TypeExplicitness.polymorphic
		testAbstractEntityMarshaller(resource, TypeExplicitness.polymorphic, 0);

		// test TypeExplicitness.always
		testAbstractEntityMarshaller(resource, TypeExplicitness.always, 0);

		// test TypeExplicitness.always & recurenceDepth = -1
		testAbstractEntityMarshaller(resource, TypeExplicitness.entities, -1);

	}

	private void testAbstractEntityMarshaller(Resource resource, TypeExplicitness te, int recurenceDepth) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, resource,
				GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, te).set(EntityRecurrenceDepth.class, recurenceDepth)
						.writeEmptyProperties(true).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());

		String marshalledString = out.toString();

		GmMetaModel resourceMetaModel = new NewMetaModelGeneration().buildMetaModel("com.braintribe.gm:resource-model",
				CollectionTools.getList(Resource.T, SqlSource.T, ResourceSpecification.T));
		GmDeserializationOptions options = getDefaultDeserializationOptions(Resource.T, resourceMetaModel);

		Resource unmarshalledResource = (Resource) marshaller.decode(marshalledString, options);
		PropertyByProperty.checkEquality(resource, unmarshalledResource).assertThatEqual();
	}

	private GmDeserializationOptions getDefaultDeserializationOptions(GenericModelType inferedEntityType, GmMetaModel metaModel) {
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);

		GmType gmStringType = GmStringType.T.create();
		GmType gmLongType = GmLongType.T.create();

		editor.onEntityType(StandardIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(gmLongType));
		editor.onEntityType(StandardStringIdentifiable.T) //
				.addPropertyMetaData(GenericEntity.id, typeSpecification(gmStringType));

		SmoodAccess resourceAccess = GmTestTools.newSmoodAccessMemoryOnly("resource.access", metaModel);
		resourceAccess.getDatabase().setDefaultPartition("cortex");
		resourceAccess.getDatabase().setIgnorePartitions(false);

		StaticAccessModelAccessory accessory = new StaticAccessModelAccessory(resourceAccess.getMetaModel(), "resource.access");

		BasicPersistenceGmSession session = (BasicPersistenceGmSession) GmTestTools.newSession(resourceAccess);
		session.setModelAccessory(accessory);
		session.commit();

		//@formatter:off
		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(inferedEntityType).setSession(session)
				.set(IdTypeSupplier.class, session.getModelAccessory()::getIdType)
				.set(IdentityManagementModeOption.class, IdentityManagementMode.auto)
				.build();
		//@formatter:on
		return options;
	}

	@Test
	public void testAbstractClassInCollections() {
		Resource resource1 = Resource.T.create();
		ResourceSource resourceSource = SqlSource.T.create();

		resource1.setCreated(new Date());
		resource1.setCreator("CORTEX");
		resource1.setFileSize(100L);
		resource1.setGlobalId("123456789");
		resource1.setId("123");
		resource1.setMd5("MD5");
		resource1.setMimeType("application/json");
		resource1.setName("TEST");
		resource1.setPartition("cortex");
		resource1.setResourceSource(resourceSource);

		Resource resource2 = Resource.T.create();
		ResourceSource resourceSource2 = FileUploadSource.T.create();

		resource2.setCreated(new Date());
		resource2.setCreator("CORTEX2");
		resource2.setFileSize(1002L);
		resource2.setGlobalId("1234567892");
		resource2.setId("1232");
		resource2.setMd5("MD52");
		resource2.setMimeType("application/json");
		resource2.setName("TEST2");
		resource2.setPartition("cortex");
		resource2.setResourceSource(resourceSource2);

		SetType setType = new SetTypeImpl(Resource.T);

		PlainSet<Resource> resources = new PlainSet<>(setType);
		resources.add(resource1);
		resources.add(resource2);

		// test TypeExplicitness.entities
		testAbstractEntityInCollectionMarshaller(resources, TypeExplicitness.entities, 0, setType);

		// test TypeExplicitness.auto
		testAbstractEntityInCollectionMarshaller(resources, TypeExplicitness.auto, 0, setType);

		// test TypeExplicitness.auto & EntityRecurrenceDepth == -1
		testAbstractEntityInCollectionMarshaller(resources, TypeExplicitness.auto, -1, setType);

	}

	private void testAbstractEntityInCollectionMarshaller(PlainSet<Resource> resource, TypeExplicitness te, int recurenceDepth, SetType setType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, resource,
				GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, te).set(EntityRecurrenceDepth.class, recurenceDepth)
						.writeEmptyProperties(true).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());

		String marshalledString = out.toString();
		System.out.println(marshalledString);
		GmMetaModel resourceMetaModel = new NewMetaModelGeneration().buildMetaModel("com.braintribe.gm:resource-model",
				CollectionTools.getList(Resource.T, SqlSource.T, ResourceSpecification.T, FileUploadSource.T));
		GmDeserializationOptions options = getDefaultDeserializationOptions(setType, resourceMetaModel);

		PlainSet<Resource> unmarshalledResource = (PlainSet<Resource>) marshaller.decode(marshalledString, options);
		PropertyByProperty.checkEquality(resource, unmarshalledResource).assertThatEqual();
	}

	@Test
	public void testBetterErrorHandlerWhen_TypeIsMissing() {
		String jsonInput = "{\n" + "	\"_type\": \"com.braintribe.model.resource.Resource\",\n" + "	\"_id\": \"0\",\n"
				+ "	\"globalId\": \"123456789\",\n" + "	\"name\": \"TEST\",\n" + "	\"resourceSource\": { \n" + "		\"_id\": \"1\",\n"
				+ "		\"globalId\": null,\n" + "		\"id\": null,\n" + "		\"partition\": null,\n" + "		\"useCase\": null\n" + "	}\n"
				+ "}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try {
			marshaller.decode(jsonInput);
		} catch (CodecException ce) {
			Throwable cause = getCause(ce);
			assertEquals(
					"_type is missing for property: resourceSource with path [com.braintribe.model.resource.source.ResourceSource:resourceSource]",
					cause.getMessage());
		}

	}

	@Test
	public void testBetterErrorHandlerWhen_TypeIsMissingInCollection() {
		String jsonInput = "[{\n" + "		\"_type\": \"com.braintribe.model.resource.Resource\",\n" + "		\"_id\": \"0\",\n"
				+ "		\"id\": {\n" + "			\"value\": \"123\",\n" + "			\"_type\": \"decimal\"\n" + "		},\n"
				+ "		\"resourceSource\": {\n" + "			\"_type\": \"com.braintribe.model.resource.source.SqlSource\",\n"
				+ "			\"_id\": \"1\",\n" + "			\"globalId\": null,\n" + "			\"id\": null,\n"
				+ "			\"partition\": null,\n" + "			\"useCase\": null\n" + "		},\n" + "		\"specification\": null,\n"
				+ "		\"tags\": []\n" + "	},\n" + "	{\n" + "		\"_type\": \"com.braintribe.model.resource.Resource\",\n"
				+ "		\"_id\": \"0\",\n" + "		\"id\": {\n" + "			\"value\": \"123\",\n" + "			\"_type\": \"decimal\"\n"
				+ "		},\n" + "		\"resourceSource\": {\n" + "			\"_id\": \"1\",\n" + "			\"globalId\": null,\n"
				+ "			\"id\": null,\n" + "			\"partition\": null,\n" + "			\"useCase\": null\n" + "		},\n"
				+ "		\"specification\": null,\n" + "		\"tags\": []\n" + "	}\n" + "]";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try {
			marshaller.decode(jsonInput);
		} catch (CodecException ce) {
			Throwable cause = getCause(ce);
			assertEquals(
					"_type is missing for property: resourceSource with path [com.braintribe.model.resource.source.ResourceSource:resourceSource]",
					cause.getMessage());
		}

	}

	@Test
	public void testMissingTypePath() {
		String json = "{\"_type\": \"com.braintribe.codec.marshaller.json.model.abstractentities.BasicEntity\", \"_id\": \"0\",\n"
				+ " \"firstLevelAbstract\": {\"_type\": \"com.braintribe.codec.marshaller.json.model.abstractentities.FirstLevelImpl\", \"_id\": \"1\",\n"
				+ "  \"globalId\": null,\n" + "  \"id\": null,\n" + "  \"partition\": null,\n"
				+ "  \"secondLevelAbstract\": {\"_type\": \"com.braintribe.codec.marshaller.json.model.abstractentities.SecondLevelImpl\", \"_id\": \"2\",\n"
				+ "   \"globalId\": null,\n" + "   \"id\": null,\n" + "   \"partition\": null,\n" + "   \"thirdLevelAbstract\": { \"_id\": \"3\",\n"
				+ "    \"globalId\": null,\n" + "    \"id\": null,\n" + "    \"name\": \"Third\",\n" + "    \"partition\": null\n" + "   }\n"
				+ "  }\n" + " },\n" + " \"globalId\": null,\n" + " \"id\": null,\n" + " \"partition\": null\n" + "}";
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		try {
			marshaller.decode(json);
		} catch (CodecException ce) {
			Throwable cause = getCause(ce);
			assertEquals(
					"_type is missing for property: thirdLevelAbstract with path [com.braintribe.codec.marshaller.json.model.abstractentities.BasicEntity:firstLevelAbstract -> com.braintribe.codec.marshaller.json.model.abstractentities.FirstLevelImpl:secondLevelAbstract -> com.braintribe.codec.marshaller.json.model.abstractentities.ThirdLevelAbstract:thirdLevelAbstract]",
					cause.getMessage());
		}
	}

	@Test
	public void testMarshallingOfId() {
		Category category = Category.T.create();
		category.setId(Long.valueOf(123L));
		category.setName("Dog Category");

		Pet dog = Pet.T.create();
		dog.setId("asdf");
		dog.setName("Dog");
		dog.setCategory(category);

		// test TypeExplicitness.auto
		_testMarshallingOfId(dog, TypeExplicitness.auto, 0);
		// test TypeExplicitness.polymorphic
		_testMarshallingOfId(dog, TypeExplicitness.polymorphic, 0);

		// test TypeExplicitness.always
		_testMarshallingOfId(dog, TypeExplicitness.always, 0);

		// test TypeExplicitness.entities & recurenceDepth = -1
		_testMarshallingOfId(dog, TypeExplicitness.entities, -1);

		// test TypeExplicitness.always & recurenceDepth = -1
		_testMarshallingOfId(dog, TypeExplicitness.always, -1);

		_testMarshallingOfId(dog, TypeExplicitness.never, 0);
	}
	private void _testMarshallingOfId(Pet pet, TypeExplicitness te, int entityRecurrenceDepth) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, pet,
				GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, te).set(EntityRecurrenceDepth.class, entityRecurrenceDepth)
						.writeEmptyProperties(true).stabilizeOrder(true).outputPrettiness(OutputPrettiness.high).build());

		String marshalledString = out.toString();
		GmMetaModel petMetaModel = new NewMetaModelGeneration().buildMetaModel("com.braintribe.gm:pet-model",
				CollectionTools.getList(Pet.T, Tag.T, Category.T));
		GmDeserializationOptions options = getDefaultDeserializationOptions(Pet.T, petMetaModel);

		Pet unmarshalledResource = (Pet) marshaller.decode(marshalledString, options);
		PropertyByProperty.checkEquality(pet, unmarshalledResource).assertThatEqual();
	}

	private TypeSpecification typeSpecification(GmType gmType) {
		TypeSpecification sts = TypeSpecification.T.create();
		sts.setType(gmType);
		return sts;
	}

	private void setSimpleTypes(TestEntity e) {
		e.setPrimitiveBooleanValue(true);
		e.setIntValue(1234);
		e.setPrimitiveLongValue(Long.MAX_VALUE);
		e.setPrimitiveFloatValue(Float.MAX_VALUE);
		e.setPrimitiveDoubleValue(Double.MAX_VALUE);
	}

	private void setObjectTypes(TestEntity e) {
		e.setBooleanValue(Boolean.TRUE);
		e.setIntegerValue(Integer.MIN_VALUE);
		e.setLongValue(Long.MIN_VALUE);
		e.setFloatValue(Float.MIN_NORMAL);
		e.setDoubleValue(Double.MIN_NORMAL);
		e.setStringValue("this is test");
		e.setDecimalValue(BigDecimal.valueOf(10.0001));
	}

	private void setSets(TestEntity e) {
		e.getStringSet().addAll(Arrays.asList("one", "two"));
		e.getIntegerSet().addAll(Arrays.asList(1, 2));
		e.getLongSet().addAll(Arrays.asList(1l, 2l));
		e.getDoubleSet().addAll(Arrays.asList(2.71828_18284_59045_23536_02874, 1.70521_11401_05367_76428_85514));
		e.getFloatSet().addAll(Arrays.asList(3.14f, 1.4142135623f));
		e.getBooleanSet().addAll(Arrays.asList(true, false));
		e.getObjectSet().addAll(Arrays.asList("one", "two"));
	}

	private void setLists(TestEntity e) {
		e.getStringList().addAll(Arrays.asList("one", "two"));
		e.getIntegerList().addAll(Arrays.asList(1, 2));
		e.getLongList().addAll(Arrays.asList(1l, 2l));
		e.getDoubleList().addAll(Arrays.asList(2.71828_18284_59045_23536_02874, 1.70521_11401_05367_76428_85514));
		e.getFloatList().addAll(Arrays.asList(3.14f, 1.4142135623f));
		e.getBooleanList().addAll(Arrays.asList(true, false));
		e.getObjectList().addAll(Arrays.asList(BigDecimal.valueOf(1L)));
	}

	private void setMaps(TestEntity e) {
		e.getStringMap().put("stringMap", "value");
		e.getStringIntegerMap().put("stringInteger", Integer.MAX_VALUE);
		e.getStringLongMap().put("stringLong", Long.MAX_VALUE);
		e.getStringDoubleMap().put("stringDouble", Double.MAX_VALUE);
		e.getStringFloatMap().put("stringFloat", Float.MAX_VALUE);
		e.getStringObjectMap().put("stringObject", TestEnum.THREE);
		e.getObjectStringMap().put(TestEnum.THREE, "objectString");
		e.getObjectObjectMap().put(TestEnum.ONE, TestEnum.ONE);
		e.getEnumMap().put(TestEnum.TWO, "enumString");
	}

	private Throwable getCause(Exception e) {
		Throwable cause = null;
		Throwable result = e;

		while (null != (cause = result.getCause()) && (result != cause)) {
			result = cause;
		}
		return result;
	}

	@Test
	public void testRestError() throws IOException {
		File file = new File("res/rest-case.json");
		String json = IOTools.slurp(file, "UTF-8");

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Object value = marshaller.decode(json);
		System.out.println(value);
		assertThat(value).isInstanceOf(SelectQuery.class);
		SelectQuery query = (SelectQuery) value;
		assertThat(query.getSelections().size()).isEqualTo(30);
	}

	@Test
	public void testStringifyNumbersOption() throws Exception {
		TestEntity testEntity = TestEntity.T.create();
		testEntity.setDecimalValue(new BigDecimal("10.0001"));
		testEntity.setLongValue(Long.valueOf("1001"));

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		marshaller.marshall(out, testEntity, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.set(StringifyNumbersOption.class, true).outputPrettiness(OutputPrettiness.high).build());
		String actualJson = out.toString();

		TestEntity unmarshalledEntity = (TestEntity) marshaller.decode(actualJson);

		PropertyByProperty.checkEquality(unmarshalledEntity, testEntity).assertThatEqual();
		out.reset();
		marshaller.marshall(out, testEntity, GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.outputPrettiness(OutputPrettiness.high).build());
		actualJson = out.toString();
		TestEntity entity = (TestEntity) marshaller.decode(actualJson);
		PropertyByProperty.checkEquality(entity, testEntity).assertThatEqual();

	}

	@Test
	public void testIntegerId() throws Exception {
		String json = "{\n" + "	\"entityValue\": {\n" + "		\"id\": 5\n" + "	}\n" + "}";
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(TestEntity.T).build();

		TestEntity entity = (TestEntity) marshaller.decode(json, options);
		assertTrue(entity.getEntityValue().getId() instanceof Long);
	}

	@Test
	public void testConsent() throws Exception {

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		//@formatter:off
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults() //
				.set(EntityRecurrenceDepth.class, -1) //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.set(IdentityManagementModeOption.class, IdentityManagementMode.off) //
				.inferredRootType(Consent.T)
				.build(); //
		//@formatter:on

		Consent consent = Consent.T.create();
		AccountAccess accountAccess = AccountAccess.T.create();
		accountAccess.setAvailableAccounts("allAccounts");
		consent.setAccess(accountAccess);
		consent.setFrequencyPerDay(10);
		consent.setRecurringIndicator(true);
		consent.setValidUntil("2020-10-10");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		marshaller.marshall(os, consent, options);

		assertEquals("{\"access\": {\"availableAccounts\": \"allAccounts\"}," //
				+ "\"combinedServiceIndicator\": false," //
				+ "\"frequencyPerDay\": 10," //
				+ "\"recurringIndicator\": true," //
				+ "\"validUntil\": \"2020-10-10\"}", os.toString()); //

	}

	@Test
	public void testUnderscorePropertyUnmarshalling() {
		String json = "{\n" + "	\"_link\": \"this is test\",\n" + "	\"_entity\": {\n" + "		\"_link\": \"this is link\",\n"
				+ "		\"string\": \"my string\",\n" + "		\"int\": 10\n" + "	}\n" + "}";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(SimpleEntity.T).build();

		Object object = marshaller.unmarshall(new ByteArrayInputStream(json.getBytes()), options);

		assertTrue(object instanceof SimpleEntity);
		SimpleEntity entity = (SimpleEntity) object;
		assertEquals("this is test", entity.get_link());
		assertEquals("this is link", entity.get_entity().get_link());
		assertEquals("my string", entity.get_entity().getString());
		assertEquals(10, entity.get_entity().getInt());
	}

	@Test
	public void testPropertyLenience() {
		String resourceName = "example-name";
		String resourceMd5 = "1a2b3c";
		String notExistingProperty = "notExistingBooleanProperty";
		String jsonInput = "{\n" + "  \"_type\": \"com.braintribe.model.resource.Resource\",\n" + "  \"name\": \"" + resourceName + "\",\n" + "  \""
				+ notExistingProperty + "\": null,\n" + "  \"md5\": \"" + resourceMd5 + "\"\n" + "}\n";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		// by default we are not lenient, i.e. we expect an exception saying that our property doesn't exist
		assertThatExecuting(() -> {
			marshaller.decode(jsonInput);
		}).fails().withExceptionWhich().hasStackTraceContaining(notExistingProperty);

		DecodingLenience decodingLenience = new DecodingLenience();
		decodingLenience.setPropertyLenient(true);
		GmDeserializationOptions deserializationOptions = GmDeserializationOptions.deriveDefaults().setDecodingLenience(decodingLenience).build();

		// with property lenience enabled decoding must work
		Resource decodedEntity = (Resource) marshaller.decode(jsonInput, deserializationOptions);
		assertThat(decodedEntity.getName()).isEqualTo(resourceName);
		assertThat(decodedEntity.getMd5()).isEqualTo(resourceMd5);
	}

}
