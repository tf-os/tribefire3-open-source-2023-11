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
package com.braintribe.model.processing.dataio.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.options.attributes.UseDirectPropertyAccessOption;
import com.braintribe.model.example.Address;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.dataio.GenericModelInputStream;
import com.braintribe.model.processing.dataio.GenericModelOutputStream;
import com.braintribe.model.processing.dataio.GmInputStream;
import com.braintribe.model.processing.dataio.GmOutputStream;
import com.braintribe.model.processing.dataio.travtest.BorderlineEntity;
import com.braintribe.model.processing.dataio.travtest.SubBorderlineEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;

public class GenericModelDataIOTest {

	protected List<File> tmpFiles = new ArrayList<File>();

	@After
	public void cleanup() {
		for (File f : tmpFiles) {
			f.delete();
		}
	}

	@Test
	public void testSerializationRoundtrip() throws Exception {

		Person person = Person.T.create();
		person.setName("Dirk");
		person.setSex(Sex.male);

		Person elli = Person.T.create();
		elli.setName("Elli");
		elli.setSex(Sex.female);
		person.getChildren().add(elli);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GenericModelOutputStream gmOut = new GenericModelOutputStream(out, true);

		gmOut.writeObject(person);
		gmOut.writeNull();
		gmOut.writeObject(5);

		List<Object> list = new ArrayList<Object>();
		list.add(5);

		gmOut.writeObject(list);
		gmOut.close();

		System.out.println("Size of serialized GM data: " + out.size());

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		GenericModelInputStream gmIn = new GenericModelInputStream(in, new Consumer<Set<String>>() {
			@Override
			public void accept(Set<String> requiredTypes) throws RuntimeException {
				System.out.println("received required types: " + requiredTypes);
			}
		});

		Person readPerson = (Person) gmIn.readObject();
		assertThat(person.getName()).isEqualTo(readPerson.getName());
		assertThat(person.getSex()).isEqualTo(readPerson.getSex());
		assertThat(person.getChildren().size()).isEqualTo(1);
		assertThat(person.getChildren().size()).isEqualTo(readPerson.getChildren().size());

		Object value = gmIn.readObject();
		assertThat(value).isNull();

		Object value2 = gmIn.readObject();
		assertThat(value2).isEqualTo(5);

		Object value3 = gmIn.readObject();
		assertThat(value3).isInstanceOf(List.class);
		@SuppressWarnings("unchecked")
		List<Object> readList = (List<Object>) value3;
		assertThat(readList.get(0)).isEqualTo(5);
		gmIn.close();

	}

	@Test
	public void testXmlFile() throws Exception {

		int count = 1000;

		File tmpFile = File.createTempFile("GenericModelDataIOTest", ".tmp");
		this.tmpFiles.add(tmpFile);

		FileOutputStream fos = new FileOutputStream(tmpFile);
		GenericModelOutputStream gmOut = new GenericModelOutputStream(fos, true);

		for (int i = 0; i < count; ++i) {
			Address address = Address.T.create();
			address.setCountry("Austria");
			address.setStreet("Kandlgasse 19-21/" + i);
			address.setZip("1170");
			address.setCity("Vienna");
			gmOut.writeObject(address);
		}

		gmOut.close();
		fos.close();

		System.out.println("Serialized data size: " + tmpFile.length());

		FileInputStream fis = new FileInputStream(tmpFile);
		GenericModelInputStream gmIn = new GenericModelInputStream(fis);

		for (int i = 0; i < count; ++i) {
			Object value = gmIn.readObject();
			assertThat(value).isInstanceOf(Address.class);
			Address address = (Address) value;
			assertThat(address.getCity()).isEqualTo("Vienna");
			assertThat(address.getStreet()).isEqualTo("Kandlgasse 19-21/" + i);
		}

		gmIn.close();
		fis.close();

	}

	@Test
	public void testQuerySerialization() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(GenericEntity.T).where().conjunction().property("id").eq("Test")
				.property("deployed").eq(true).close().done();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmOutputStream gmOut = new GmOutputStream(baos,
				GmSerializationOptions.deriveDefaults().build().findAttribute(UseDirectPropertyAccessOption.class).orElse(false));
		gmOut.writeObject(query);
		gmOut.flush();
		gmOut.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		GmInputStream gmIn = new GmInputStream(bais, GmDeserializationOptions.deriveDefaults().build());
		EntityQuery deserializedQuery = (EntityQuery) gmIn.readObject();
		assertThat(deserializedQuery).isNotNull();
		gmIn.close();

		// Not really testing the result here, EntityComparator throws an exception here
		// assertThat(AssemblyComparison.equals(query, deserializedQuery)).isTrue();

	}

	@Test
	public void testQueryResultSerialization() throws Exception {

		EntityQueryResult result = EntityQueryResult.T.create();
		List<GenericEntity> entities = new ArrayList<GenericEntity>();
		result.setEntities(entities);

		Person p = Person.T.create();
		p.setName("person1");
		p.setId(100L);
		p.setGlobalId("GL1");

		entities.add(p);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmOutputStream gmOut = new GmOutputStream(baos,
				GmSerializationOptions.deriveDefaults().build().findAttribute(UseDirectPropertyAccessOption.class).orElse(false));
		gmOut.writeObject(result);
		gmOut.flush();
		gmOut.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		GmInputStream gmIn = new GmInputStream(bais, GmDeserializationOptions.deriveDefaults().build());
		EntityQueryResult deserializedResult = (EntityQueryResult) gmIn.readObject();
		assertThat(deserializedResult).isNotNull();
		gmIn.close();

		// Not really testing the result here, EntityComparator throws an exception here
		// assertThat(AssemblyComparison.equals(query, deserializedQuery)).isTrue();

	}

	@Test
	public void testSubBorderlineSerialization() throws Exception {

		SubBorderlineEntity sbe = SubBorderlineEntity.T.create();
		sbe.setGlobalId("1");
		sbe.setPartition("1");
		sbe.setProperty2("2");
		sbe.setProperty3("3");
		sbe.setProperty4("4");
		sbe.setProperty5("5");
		sbe.setProperty6("6");
		sbe.setProperty7("7");
		sbe.setProperty8("8");
		sbe.setProperty9("9");
		sbe.setPropertyA("A");
		sbe.setPropertyB("B");
		sbe.setPropertyC("C");
		sbe.setPropertyD("D");
		sbe.setPropertyE("E");
		sbe.setPropertyF("F");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmOutputStream gmOut = new GmOutputStream(baos,
				GmSerializationOptions.deriveDefaults().build().findAttribute(UseDirectPropertyAccessOption.class).orElse(false));
		gmOut.writeObject(sbe);
		gmOut.flush();
		gmOut.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		GmInputStream gmIn = new GmInputStream(bais, GmDeserializationOptions.deriveDefaults().build());
		SubBorderlineEntity deserializedResult = (SubBorderlineEntity) gmIn.readObject();
		assertThat(deserializedResult).isNotNull();
		gmIn.close();

		// Not really testing the result here, EntityComparator throws an exception here
		// assertThat(AssemblyComparison.equals(query, deserializedQuery)).isTrue();

	}

	@Test
	public void testBorderlineSerialization() throws Exception {

		BorderlineEntity sbe = BorderlineEntity.T.create();
		sbe.setGlobalId("1");
		sbe.setPartition("1");
		sbe.setProperty2("2");
		sbe.setProperty3("3");
		sbe.setProperty4("4");
		sbe.setProperty5("5");
		sbe.setProperty6("6");
		sbe.setProperty7("7");
		sbe.setProperty8("8");
		sbe.setProperty9("9");
		sbe.setPropertyA("A");
		sbe.setPropertyB("B");
		sbe.setPropertyC("C");
		sbe.setPropertyD("D");
		sbe.setPropertyE("E");
		sbe.setPropertyF("F");
		sbe.setPropertyX("X");
		sbe.setPropertyY("Y");

		Set<String> set = sbe.getSetProperty2();
		set.add("SetValue1");
		set.add("SetValue2");
		set.add("SetValue3");

		AbsenceInformation ai1 = AbsenceInformation.T.create();
		Property absentProperty1 = GMF.getTypeReflection().getEntityType(BorderlineEntity.class).getProperty("absentProperty1");
		absentProperty1.setAbsenceInformation(sbe, ai1);

		AbsenceInformation ai2 = AbsenceInformation.T.create();
		ai2.setSize(new Integer(1));
		Property absentProperty2 = GMF.getTypeReflection().getEntityType(BorderlineEntity.class).getProperty("absentProperty2");
		absentProperty2.setAbsenceInformation(sbe, ai2);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmOutputStream gmOut = new GmOutputStream(baos,
				GmSerializationOptions.deriveDefaults().build().findAttribute(UseDirectPropertyAccessOption.class).orElse(false));
		gmOut.writeObject(sbe);
		gmOut.flush();
		gmOut.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		GmInputStream gmIn = new GmInputStream(bais, GmDeserializationOptions.deriveDefaults().build());
		BorderlineEntity deserializedResult = (BorderlineEntity) gmIn.readObject();
		gmIn.close();

		AbsenceInformation resultAi2 = absentProperty2.getAbsenceInformation(deserializedResult);
		Assert.assertEquals(ai2.getSize(), resultAi2.getSize());

	}
}
