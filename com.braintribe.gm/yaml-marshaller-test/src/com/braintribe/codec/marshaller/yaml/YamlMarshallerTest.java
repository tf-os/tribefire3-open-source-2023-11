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

import static com.braintribe.codec.marshaller.yaml.YamlMarshallerTestUtils.assertContent;
import static com.braintribe.codec.marshaller.yaml.YamlMarshallerTestUtils.marshallToString;
import static com.braintribe.codec.marshaller.yaml.YamlMarshallerTestUtils.marshallingRoundTrip;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.snakeyaml.engine.v2.exceptions.ParserException;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.model.PersonEntity;
import com.braintribe.codec.marshaller.yaml.model.TestEntity;
import com.braintribe.codec.marshaller.yaml.model.TestEnum;
import com.braintribe.codec.marshaller.yaml.model.pets.SelfReference;
import com.braintribe.codec.marshaller.yaml.model.pets.Status;
import com.braintribe.codec.marshaller.yaml.model.pets.StatusMappings;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.wire.api.util.Maps;

public class YamlMarshallerTest implements YamlMarshallerTestUtils {
	private static Date date;

	static {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.clear();
		calendar.set(Calendar.YEAR, 2002);
		// Java's months are zero-based...
		calendar.set(Calendar.MONTH, 11); // x
		calendar.set(Calendar.DAY_OF_MONTH, 14);
		date = calendar.getTime();
	}

	private static final List<Object> scalars = Arrays.asList(null, "null", 1, 23L, 2.718F, 3.14, new BigDecimal("0.000001"), true, false, "Ein Text",
			date, date, Status.available);

	@Test
	public void testStringList() throws Exception {
		assertContent("res/listOfStrings.yaml", Arrays.asList("Hallo Welt wie geht es Dir", "null"));
	}

	@Test
	public void testScalarList() throws Exception {
		assertContent("res/listOfScalars.yaml", scalars);
	}

	@Test
	public void testScalarSetByMap() throws Exception {
		assertContent("res/setOfScalarsByMap.yaml", new HashSet<>(scalars));
	}

	@Test
	public void testScalarSetBySequence() throws Exception {
		assertContent("res/setOfScalarsBySequence.yaml", new HashSet<>(scalars));
	}

	@Test
	public void testInvalidReference() throws Exception {

		try (InputStream in = new FileInputStream("res/invalidReference.yaml")) {
			Assertions.assertThatThrownBy(() -> new YamlMarshaller().unmarshall(in)).hasMessageStartingWith("The following anchors where referenced but never defined: 1").isInstanceOf(ParserException.class);
		}
	}

	@Test
	public void testScalarMap() throws Exception {
		Map<Object, Object> expectation = map( //
				entry("one", 1), //
				entry("two", 23L), //
				entry("three", 2.718F), //
				entry("four", 3.14), //
				entry("five", new BigDecimal("0.000001")), //
				entry("six", true), //
				entry("seven", false), //
				entry("eight", "Ein Text"), //
				entry("nine", Status.available));

		assertContent("res/mapOfScalars.yaml", expectation);
	}

	@Test
	public void testInferredMapToEntitiesRoundtrip() throws Exception {
		PersonEntity p1 = PersonEntity.T.create();
		p1.setGlobalId("p1");
		p1.setFirstName("John");
		p1.setLastName("Deere");

		PersonEntity p2 = PersonEntity.T.create();
		p2.setGlobalId("p2");
		p2.setFirstName("Lara");
		p2.setLastName("Soft");

		Map<Object, Object> expectation = map( //
				entry("available", p1), //
				entry("pending", p2), //
				entry("foo", 5), //
				entry("bar", "fixfox"));

		Map<Object, Object> map = marshallingRoundTrip(expectation, GmSerializationOptions.defaultOptions);

		Assertions.assertThat(AssemblyComparison.build().useGlobalId().compare(map, expectation).equal()).isTrue();
	}

	@Test
	public void testMapToEntitiesRoundtripWithoutTypeExplicitness() throws Exception {
		PersonEntity p1 = PersonEntity.T.create();
		p1.setGlobalId("p1");
		p1.setFirstName("John");
		p1.setLastName("Deere");

		PersonEntity p2 = PersonEntity.T.create();
		p2.setGlobalId("p2");
		p2.setFirstName("Lara");
		p2.setLastName("Soft");

		Map<Object, Object> input = map(entry("available", p1), entry("pending", p2));

		// Without an explicit type the marshaller does not know into which entity it should unmarshall the values -
		// so it marshalls it into a map.
		Map<Object, Object> expectation = map( //
				entry("available", //
						map( //
								entry("firstName", "John"), //
								entry("globalId", "p1"), //
								entry("lastName", "Deere") //
						) //
				), //
				entry("pending", //
						map( //
								entry("firstName", "Lara"), //
								entry("globalId", "p2"), //
								entry("lastName", "Soft") //
						) //
				) //
		);

		GmSerializationOptions typeImplicitOptions = GmSerializationOptions.defaultOptions.derive() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.never) //
				.build();

		Map<Object, Object> map = marshallingRoundTrip(input, typeImplicitOptions);

		Assertions.assertThat(AssemblyComparison.build().useGlobalId().compare(map, expectation).equal()).isTrue();
	}

	@Test
	public void testInferredMapOfEnums() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/inferredMapOfEnums.yaml")) {
			parsedValue = marshaller.unmarshall(in);
		}

		StatusMappings mappings = (StatusMappings) parsedValue;

		Map<Object, Object> expectation = map( //
				entry("available", Status.available), //
				entry("pending", Status.pending), //
				entry("sold", Status.sold) //
		);

		Assertions.assertThat(mappings.getMappings()).as("map did not contain expected entries").isEqualTo(expectation);
	}

	@Test
	public void testSelfReference() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/selfReference.yaml")) {
			parsedValue = marshaller.unmarshall(in);
		}

		SelfReference selfReference = (SelfReference) parsedValue;

		Assertions.assertThat(selfReference.getSelf()).as("self reference mismatch").isSameAs(selfReference);
	}

	@Test
	public void testAbsenceInformation() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/ai.yaml")) {
			parsedValue = marshaller.unmarshall(in);
		}

		List<SelfReference> selfReferences = (List<SelfReference>) parsedValue;

		SelfReference sr1 = selfReferences.get(0);
		SelfReference sr2 = selfReferences.get(1);

		Property selfProperty = SelfReference.T.getProperty("self");
		AbsenceInformation ai1 = selfProperty.getAbsenceInformation(sr1);
		AbsenceInformation ai2 = selfProperty.getAbsenceInformation(sr2);

		Assertions.assertThat(ai1).as("missing absence information").isSameAs(GMF.absenceInformation());
		Assertions.assertThat(ai2).as("missing absence information").isNotNull();
		Assertions.assertThat(ai1).as("absence information was not constructed but reused").isNotSameAs(ai2);
	}

	@Test
	public void testEmptyEntity() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();

		Object parsedValue = null;

		try (InputStream in = new FileInputStream("res/emptyEntity.yaml")) {
			parsedValue = marshaller.unmarshall(in);
		}

		SelfReference selfReference = (SelfReference) parsedValue;

		Assertions.assertThat(selfReference).as("missing empty entity").isNotNull();
	}

	@Test
	public void testCollectionOfEmptyEntitiesOutput() throws Exception {
		List<SelfReference> list = new ArrayList<>();

		list.add(SelfReference.T.create());
		list.add(SelfReference.T.create());
		list.add(SelfReference.T.create());

		new YamlMarshaller().marshall(System.out, list);
	}

	@Test
	public void testMapOfEmptyEntitiesOutput() throws Exception {
		Map<String, SelfReference> map = new HashMap<>();

		map.put("one", SelfReference.T.create());
		map.put("two", SelfReference.T.create());
		map.put("three", SelfReference.T.create());

		new YamlMarshaller().marshall(System.out, map);
	}

	@Category(KnownIssue.class) // TODO: breaks ExtendedXmlJUnitResultFormatter
	@Test
	public void testOutput() throws Exception {
		Map<String, Date> map = new HashMap<>();

		map.put("date", new Date());
		map.put("nullDate", null);

		new YamlMarshaller().marshall(System.out, map);
	}

	@Test
	public void testTabs() throws Exception {
		assertContent("res/tabs.yaml", Maps.map(Maps.entry("foo", "bar")), true);
	}

	@Test
	public void testObjectProperties() throws Exception {
		testObjectProperty(true, "true");
		testObjectProperty(3, "3");
		testObjectProperty(3d, "3.0");
		testObjectProperty("3", "\"3\"");
		testObjectProperty(TestEnum.ONE, "!com.braintribe.codec.marshaller.yaml.model.TestEnum ONE");

		TestEntity testEntity = TestEntity.T.create();
		testEntity.setObjectValue(false);

		testObjectProperty(testEntity, "!" + TestEntity.T.getTypeSignature() + "\n  objectValue: false");
	}
	
	@Test
	public void testSimplePooled() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();
		
		try (InputStream in = new FileInputStream("res/simple-pooled.yaml")) {
			Object value = marshaller.unmarshall(in);
			
			System.out.println(value);
		}
	}
	
	@Test
	public void testPooledUnmarshall() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();
		
		try (InputStream in = new FileInputStream("res/pooled.yaml")) {
			List<PersonEntity> persons = (List<PersonEntity>) marshaller.unmarshall(in);
			
			Assertions.assertThat(persons.size()).as("Person count differs from expecation").isEqualTo(2);
			
			PersonEntity p1 = persons.get(0);
			PersonEntity p2 = persons.get(1);
			
			Assertions.assertThat(p1.getFirstName()).as("First person's name differs from expecation").isEqualTo("Tina");
			Assertions.assertThat(p1.getFriend()).as("First person's friend differs from expecation").isEqualTo(p2);
			Assertions.assertThat(p2.getFirstName()).as("Second person's name differs from expecation").isEqualTo("Dirk");
			Assertions.assertThat(p2.getFriend()).as("Second person's friend differs from expecation").isEqualTo(p1);
		}
	}
	
	@Test
	public void testPooledMarshall() throws Exception {
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
		
		PersonEntity p1 = PersonEntity.T.create();
		PersonEntity p2 = PersonEntity.T.create();
		
		p1.setFirstName("Tina");
		p1.setFriend(p2);
		p2.setFirstName("Dirk");
		p2.setFriend(p1);
		
		List<PersonEntity> list = new ArrayList<>();
		list.add(p1);
		list.add(p2);

		marshaller.marshall(System.out, list, GmSerializationOptions.deriveDefaults().setOutputPrettiness(OutputPrettiness.high).build());
	}

	private static void testObjectProperty(Object value, String expectedResult) {
		TestEntity testEntity = TestEntity.T.create();
		testEntity.setObjectValue(value);

		String marshalledString = marshallToString(testEntity, GmSerializationOptions.defaultOptions);

		String fullExpectedResult = "!" + TestEntity.T.getTypeSignature() + "\nobjectValue: " + expectedResult + "\n";
		assertThat(marshalledString).isEqualTo(fullExpectedResult);
	}

}
