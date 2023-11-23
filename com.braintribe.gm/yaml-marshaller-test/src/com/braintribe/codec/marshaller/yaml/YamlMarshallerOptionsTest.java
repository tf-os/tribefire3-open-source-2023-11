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

import static com.braintribe.codec.marshaller.yaml.YamlMarshallerTestUtils.marshallToByteArray;
import static com.braintribe.codec.marshaller.yaml.YamlMarshallerTestUtils.marshallingRoundTrip;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.ScalarEntityParsers;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.api.options.attributes.WriteEmptyPropertiesOption;
import com.braintribe.codec.marshaller.common.ConfigurableScalarEntityParsers;
import com.braintribe.codec.marshaller.yaml.model.CompanyEntity;
import com.braintribe.codec.marshaller.yaml.model.EntityWithInitializer;
import com.braintribe.codec.marshaller.yaml.model.PersonEntity;
import com.braintribe.codec.marshaller.yaml.model.TestEntity;
import com.braintribe.codec.marshaller.yaml.model.pets.Status;
import com.braintribe.codec.marshaller.yaml.model.pets.StatusMappings;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparisonBuilder;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparisonResult;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class YamlMarshallerOptionsTest implements YamlMarshallerTestUtils {
	@Test
	public void testInferrenceRoundTrip() throws Exception {

		StatusMappings statusMappings = StatusMappings.T.create();

		statusMappings.getMappings().put("available", Status.available);
		statusMappings.getMappings().put("pending", Status.pending);
		statusMappings.getMappings().put("sold", Status.sold);

		GmSerializationOptions typeExplicitOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.always) //
				.build();

		StatusMappings mappings = marshallingRoundTrip(statusMappings, typeExplicitOptions);

		Map<Object, Object> expectation = map( //
				entry("available", Status.available), // 
				entry("pending", Status.pending), //
				entry("sold", Status.sold) //
		);

		Assertions.assertThat(mappings.getMappings()).as("map did not contain expected entries").isEqualTo(expectation);

	}

	@Category(KnownIssue.class) // TODO: breaks ExtendedXmlJUnitResultFormatter
	@Test
	public void testInferrenceRoundTrip2() throws Exception {
		TestEntity testEntity = TestEntity.T.create();

		testEntity.setLongValue(4711L);
		testEntity.setDecimalValue(new BigDecimal("12345689876543212345678987654321.23"));
		testEntity.getObjectList().add(4711L);
		testEntity.getObjectList().add(new BigDecimal("12345689876543212345678987654321.23"));

		TestEntity otherTestEntity = TestEntity.T.create();
		otherTestEntity.setBooleanValue(false);

		testEntity.setEntityValue(otherTestEntity);

		GmSerializationOptions polymorphicMarshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.build();
		TestEntity testEntity2 = marshallingRoundTrip(testEntity, polymorphicMarshallingOptions);

		List<Function<TestEntity, Object>> accessors = new ArrayList<>();
		accessors.add(TestEntity::getLongValue);
		accessors.add(TestEntity::getDecimalValue);
		accessors.add(TestEntity::getObjectList);

		for (Function<TestEntity, Object> accessor : accessors) {
			Object v1 = accessor.apply(testEntity);
			Object v2 = accessor.apply(testEntity2);
			Assertions.assertThat(v1).as("values did not match").isEqualTo(v2);
		}

	}

	@Category(KnownIssue.class) // TODO: breaks ExtendedXmlJUnitResultFormatter
	@Test
	public void testNestedEntityRoundTrip() throws Exception {
		TestEntity testEntity = TestEntity.T.create();
		TestEntity otherTestEntity = TestEntity.T.create();
		testEntity.setEntityValue(otherTestEntity);
		testEntity.setLongValue(4711L);

		GmSerializationOptions polymorphicMarshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.build();
		TestEntity testEntity2 = marshallingRoundTrip(testEntity, polymorphicMarshallingOptions);
	}

	@Test
	public void testModelRoundtrip() throws Exception {
		GmMetaModel model = GMF.getTypeReflection().getModel("com.braintribe.gm:root-model").getMetaModel();
		model = model.clone(new StandardCloningContext());

		GmSerializationOptions polymorphicMarshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.build();
		GmMetaModel trippedModel = marshallingRoundTrip(model, polymorphicMarshallingOptions);

		AssemblyComparisonBuilder comparison = AssemblyComparison.build().useGlobalId().enableTracking();
		AssemblyComparisonResult result = comparison.compare(model, trippedModel);

		Assertions.assertThat(result.equal()).as(result.mismatchDescription()).isTrue();

	}

	@Test
	public void testModelRoundtripNonTypeExplicit() throws Exception {
		GmMetaModel model = GMF.getTypeReflection().getModel("com.braintribe.gm:root-model").getMetaModel();
		model = model.clone(new StandardCloningContext());

		GmSerializationOptions typeImplicitOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.never) //
				.build();

		byte[] marshalledBytes = marshallToByteArray(model, typeImplicitOptions);

		try (InputStream in = new ByteArrayInputStream(marshalledBytes)) {
			YamlMarshaller marshaller = new YamlMarshaller();
			assertThatThrownBy(() -> marshaller.unmarshall(in)).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void testModelRoundtripAbsentProperties() throws Exception {
		GmMetaModel model = GMF.getTypeReflection().getModel("com.braintribe.gm:root-model").getMetaModel();
		model = model.clone(new StandardCloningContext());

		YamlMarshaller marshaller = new YamlMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions marshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.writeAbsenceInformation(false) //
				.writeEmptyProperties(false) //
				.build();

		GmDeserializationOptions unmarshallingOptions = GmDeserializationOptions.defaultOptions.derive() //
				.absentifyMissingProperties(true) //
				.build();

		// We marshall the entity tree without writing empty properties
		marshaller.marshall(baos, model, marshallingOptions);
		String firstMarshalledYaml = baos.toString();

		GmMetaModel trippedModel;

		try (InputStream in = new ByteArrayInputStream(baos.toByteArray())) {
			// We unmarshall the entity tree gaining absent properties where they were initially empty
			trippedModel = (GmMetaModel) marshaller.unmarshall(in, unmarshallingOptions);
		}

		// We umarshall the round-tripped entity tree again. Even though we turn on marshaling of empty properties - the
		// now absent properties won't be marshalled
		baos = new ByteArrayOutputStream();
		GmSerializationOptions verboseMarshalling = marshallingOptions.derive().writeEmptyProperties(true).build();
		marshaller.marshall(baos, trippedModel, verboseMarshalling);

		String secondMarshalledYaml = baos.toString();

		// Both marshall outputs should effectively be the same
		assertThat(secondMarshalledYaml).isEqualTo(firstMarshalledYaml);

	}

	@Test
	public void testScalarEntities() throws Exception {
		ConfigurableScalarEntityParsers parsers = new ConfigurableScalarEntityParsers();

		parsers.addParser(PersonEntity.T, YamlMarshallerOptionsTest::createPersonFromScalar);

		List<PersonEntity> expectedPersons = Arrays.asList(createPerson("Peter", "Frost"), createPerson("Frater", "Post"),
				createPerson("Sandra", "Lost"), createPerson("Landa", "Rost"));

		GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive() //
				.set(ScalarEntityParsers.class, parsers) //
				.setInferredRootType(GMF.getTypeReflection().getListType(PersonEntity.T)).build(); //

		YamlMarshaller marshaller = new YamlMarshaller();
		// marshaller.setV2(true);

		Object parsedValue = null;

		try (InputStream in = new FileInputStream(new File("res/scalarEntities.yaml"))) {
			parsedValue = marshaller.unmarshall(in, options);
		}

		List<PersonEntity> persons = (List<PersonEntity>) parsedValue;

		Assertions.assertThat(persons.size()).as("Size of lists is not equal").isEqualTo(expectedPersons.size());

		Comparator<PersonEntity> comparator = Comparator.comparing(PersonEntity::getFirstName).thenComparing(PersonEntity::getLastName);

		for (int i = 0; i < persons.size(); i++) {
			PersonEntity p1 = persons.get(i);
			PersonEntity p2 = expectedPersons.get(i);
			if (comparator.compare(p1, p2) != 0)
				Assertions.fail("Two persons did not match: " + p1 + ", " + p2);
		}
	}

	@Test
	// All Entities should be visited exactly once no matter how often they appear
	public void testEntityVisiting() {
		Map<GenericEntity, Integer> visitedEntities = new HashMap<>();

		PersonEntity peterFrost = createPerson("Peter", "Frost");
		PersonEntity fraterPost = createPerson("Frater", "Post");
		List<PersonEntity> persons = Arrays.asList(peterFrost, fraterPost, peterFrost, peterFrost);

		GmSerializationOptions marshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(EntityVisitorOption.class, p -> visitedEntities.merge(p, 1, (count, _v) -> count + 1)) //
				.build();

		marshallToByteArray(persons, marshallingOptions);

		assertThat(visitedEntities).hasSize(2);
		assertThat(visitedEntities.get(peterFrost)).isEqualTo(1);
		assertThat(visitedEntities.get(fraterPost)).isEqualTo(1);
	}

	@Test
	// The marshaller should ignore property @Initializers
	public void testEmptyProperties() throws Exception {
		EntityWithInitializer entity = EntityWithInitializer.T.create();
		entity.setTrue(false);

		YamlMarshaller marshaller = new YamlMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions marshallingOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.never) //
				.build();
		marshaller.marshall(baos, entity, marshallingOptions);
		assertThat(baos.toString()).isEqualTo("{}\n");

		GmDeserializationOptions deserializationOptions = GmDeserializationOptions.defaultOptions.derive() //
				.setInferredRootType(EntityWithInitializer.T) //
				.build();

		EntityWithInitializer roundTrippedEntity = (EntityWithInitializer) marshaller.unmarshall(new ByteArrayInputStream(baos.toByteArray()),
				deserializationOptions);
		assertThat(roundTrippedEntity.getTrue()).isFalse();

		entity = EntityWithInitializer.T.create();
		entity.setTrue(true);

		baos = new ByteArrayOutputStream();

		marshaller.marshall(baos, entity, marshallingOptions);
		assertThat(baos.toString()).isEqualTo("true: true\n");

		roundTrippedEntity = (EntityWithInitializer) marshaller.unmarshall(new ByteArrayInputStream(baos.toByteArray()), deserializationOptions);
		assertThat(roundTrippedEntity.getTrue()).isTrue();
	}

	private static PersonEntity createPersonFromScalar(String expression) {
		String parts[] = expression.split("\\.");
		return createPerson(parts[0], parts[1]);
	}

	private static PersonEntity createPerson(String firstName, String lastName) {
		PersonEntity person = PersonEntity.T.create();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		return person;
	}
}
