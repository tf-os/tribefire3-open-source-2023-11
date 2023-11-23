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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.model.MapPropertyEntity;
import com.braintribe.codec.marshaller.yaml.model.PersonEntity;
import com.braintribe.codec.marshaller.yaml.model.SimpleEntity;
import com.braintribe.testing.test.AbstractTest;

public class YamlMarshallerMarshallingTest extends AbstractTest implements YamlMarshallerTestUtils {

	@Test
	public void testMapProblem() {
		SimpleEntity e1 = SimpleEntity.T.create();
		SimpleEntity e2 = SimpleEntity.T.create();
		e1.setInt(1);
		e2.setInt(2);

		MapPropertyEntity mapE = MapPropertyEntity.T.create();
		mapE.getMap().put(e1, e2);

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);

		ByteArrayOutputStream capture = new ByteArrayOutputStream();

		marshaller.marshall(capture, mapE);

		MapPropertyEntity mapE1 = (MapPropertyEntity) marshaller.unmarshall(new ByteArrayInputStream(capture.toByteArray()));
	}

	@Test
	public void testAnchoring() throws Exception {
		PersonEntity person = createPerson();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		//@formatter:off
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults()
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.inferredRootType(PersonEntity.T)
				.build();
		//@formatter:on

		marshaller.marshall(baos, person, options);

		String mashalledYaml = baos.toString();

		System.out.println(mashalledYaml);

		File expectedOutcome = testFile("expected-simple-anchoring.yaml");
		assertThat(expectedOutcome).hasContent(mashalledYaml);
	}

	@Test
	public void testAnchoringTypeSafe() throws Exception {
		PersonEntity person = createPerson();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().build();

		marshaller.marshall(baos, person, options);

		String mashalledYaml = baos.toString();

		System.out.println(mashalledYaml);

		File expectedOutcome = testFile("expected-simple-anchoring-type-safe.yaml");
		assertThat(expectedOutcome).hasContent(mashalledYaml);
	}

	@Test
	public void testEmptyEntityTypeSafe() throws Exception {
		PersonEntity person = createPerson2();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().build();

		marshaller.marshall(baos, person, options);

		String mashalledYaml = baos.toString();

		System.out.println(mashalledYaml);

		File expectedOutcome = testFile("expected-empty-entity-type-safe.yaml");
		assertThat(expectedOutcome).hasContent(mashalledYaml);
	}

	@Test
	public void testEmptyEntity() throws Exception {
		PersonEntity person = createPerson2();

		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
				.inferredRootType(PersonEntity.T).build();

		marshaller.marshall(baos, person, options);

		String mashalledYaml = baos.toString();

		System.out.println(mashalledYaml);

		File expectedOutcome = testFile("expected-empty-entity.yaml");
		assertThat(expectedOutcome).hasContent(mashalledYaml);
	}

	private PersonEntity createPerson() {
		PersonEntity tina = PersonEntity.T.create();
		tina.setFirstName("Tina");

		PersonEntity dirk = PersonEntity.T.create();
		dirk.setFirstName("Dirk");

		tina.setFriend(dirk);
		dirk.setFriend(tina);

		return tina;
	}

	private PersonEntity createPerson2() {
		PersonEntity tina = PersonEntity.T.create();
		tina.setFirstName("Tina");

		PersonEntity dirk = PersonEntity.T.create();

		tina.setFriend(dirk);

		return tina;
	}

}
