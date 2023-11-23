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
package com.braintribe.model.processing.manipulation.marshaller;

import static com.braintribe.model.generic.builder.vd.VdBuilder.persistentReference;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.add;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.delete;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.localEntityProperty;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.manipulation.parser.impl.model.Joat;

/**
 * @author peter.gazdik
 */
public class ManipulationStringifierTest {

	@Test
	public void escapeWhiteSpace() throws Exception {
		checkEscape("X\n\tY", "X\\n\\tY");
	}

	@Test
	public void escapeSingleQuote() throws Exception {
		checkEscape("Hello'World", "Hello\\'World");
	}

	@Test
	public void escapeDoubleQuotes() throws Exception {
		checkEscape("Hello\"World", "Hello\"World");
	}

	@Test
	public void manipulationWithComment() throws Exception {
		ManipulationComment cm = ManipulationBuilder.comment("Test Manipulation", new Date(1000000000000L));

		EntityReference ref = persistentReference(Joat.T.getTypeSignature(), "ID-1", null);
		EntityProperty owner = entityProperty(ref, "stringValue");
		ChangeValueManipulation cvm = changeValue("val1", owner);

		CompoundManipulation compound = ManipulationBuilder.compound(cm, cvm);
		String stringifiedManipulation = ManipulationStringifier.stringify(compound, true);

		String[] lines = stringifiedManipulation.split("\\n");

		int i = 0;
		assertThat(lines[i++]).isEqualTo("{");
		assertThat(lines[i++]).isEqualTo("com.braintribe.model.generic.manipulation.ManipulationComment{");
		assertThat(lines[i++]).startsWith(" date:date(2001Y,9M,9D"); // to avoid time zone issues, only days
		assertThat(lines[i++]).isEqualTo(" text:'Test Manipulation'");
		assertThat(lines[i++]).isEqualTo("}");
		assertThat(lines[i++]).isEqualTo("$0=(Joat=com.braintribe.model.manipulation.parser.impl.model.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("}");
	}

	@Test
	public void withAmbiguousSimpleName_Remote() throws Exception {
		EntityReference ref1 = persistentReference(Joat.T.getTypeSignature(), "ID-1", null);
		EntityProperty owner1 = entityProperty(ref1, "stringValue");
		ChangeValueManipulation cvm1 = changeValue("val1", owner1);

		EntityReference ref2 = persistentReference(com.braintribe.model.manipulation.parser.impl.model.ambig.Joat.T.getTypeSignature(), "ID-1", null);
		EntityProperty owner2 = entityProperty(ref2, "stringValue");
		ChangeValueManipulation cvm2 = changeValue("val1", owner2);

		CompoundManipulation compound = ManipulationBuilder.compound(cvm1, cvm2);
		String stringifiedManipulation = ManipulationStringifier.stringify(compound, true);

		String[] lines = stringifiedManipulation.split("\\n");

		int i = 0;
		assertThat(lines[i++]).isEqualTo("{");
		assertThat(lines[i++]).isEqualTo("$0=(Joat=com.braintribe.model.manipulation.parser.impl.model.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("$1=(Joat1=com.braintribe.model.manipulation.parser.impl.model.ambig.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("}");
	}

	@Test
	public void withAmbiguousSimpleName_Local() throws Exception {
		Joat joat1 = Joat.T.create("ID-1");
		LocalEntityProperty owner1 = localEntityProperty(joat1, "stringValue");
		ChangeValueManipulation cvm1 = changeValue("val1", owner1);

		GenericEntity joat2 = com.braintribe.model.manipulation.parser.impl.model.ambig.Joat.T.create("ID-1");
		LocalEntityProperty owner2 = localEntityProperty(joat2, "stringValue");
		ChangeValueManipulation cvm2 = changeValue("val1", owner2);

		CompoundManipulation compound = ManipulationBuilder.compound(cvm1, cvm2);
		String stringifiedManipulation = ManipulationStringifier.stringify(compound, false);

		String[] lines = stringifiedManipulation.split("\\n");

		int i = 0;
		assertThat(lines[i++]).isEqualTo("{");
		assertThat(lines[i++]).isEqualTo("$0=(Joat=com.braintribe.model.manipulation.parser.impl.model.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("$1=(Joat1=com.braintribe.model.manipulation.parser.impl.model.ambig.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("}");
	}

	@Test
	public void withAmbiguousSimpleName_Local_WithInitialVars() throws Exception {
		GenericEntity joat = com.braintribe.model.manipulation.parser.impl.model.ambig.Joat.T.create("ID-1");
		LocalEntityProperty owner = localEntityProperty(joat, "stringValue");
		ChangeValueManipulation cvm = changeValue("val1", owner);

		String stringifiedManipulation = stringifyManipulation(cvm, new LocalManipulationStringifier(asMap(Joat.T, "Joat")));

		String[] lines = stringifiedManipulation.split("\\n");

		int i = 0;
		assertThat(lines[i++]).isEqualTo("{");
		assertThat(lines[i++]).isEqualTo("$0=(Joat1=com.braintribe.model.manipulation.parser.impl.model.ambig.Joat)('ID-1')");
		assertThat(lines[i++]).isEqualTo(".stringValue='val1'");
		assertThat(lines[i++]).isEqualTo("}");
	}

	private String stringifyManipulation(Manipulation m, LocalManipulationStringifier stringifier) throws IOException {
		StringBuilder sb = new StringBuilder();
		stringifier.stringify(sb, m);

		return sb.toString();
	}

	/**
	 * The original implementation would fail this test, as it was using TypeReflection even for remote manipulations. Thus in this case, it would
	 * throw an exception that "test.Entity" was not found.
	 */
	@Test
	public void encodeRemoteManipulation_ChangeValue() throws Exception {
		EntityReference ref = persistentReference("test.Entity", "ID-1", null);
		EntityProperty owner = entityProperty(ref, "prop1");
		ChangeValueManipulation cvm = changeValue("val1", owner);

		String stringifiedManipulation = cvm.stringify();

		assertThat(stringifiedManipulation).doesNotContain("stringifier failed");
		assertThat(stringifiedManipulation).containsAll("test.Entity", "ID-1", ".prop1='val1'");
	}

	@Test
	public void encodeRemoteManipulation_Add() throws Exception {
		EntityReference ref = persistentReference("test.Entity", "ID-1", null);

		EntityProperty addOwner = entityProperty(ref, "listProp");
		AddManipulation am = add(asMap(0, "zero", 1, "one"), addOwner);

		String stringifiedManipulation = am.stringify();

		assertThat(stringifiedManipulation).doesNotContain("stringifier failed");
		assertThat(stringifiedManipulation).containsAll("test.Entity", "ID-1", ".listProp+{0:'zero',1:'one'}");
	}

	@Test
	public void encodeRemoteManipulation_DeleteEntity() throws Exception {
		EntityReference ref = persistentReference("test.Entity", "ID-1", null);
		DeleteManipulation deleteManipulation = delete(ref, DeleteMode.dropReferences);
		String stringifiedManipulation = deleteManipulation.stringify();
		assertThat(stringifiedManipulation).doesNotContain("stringifier failed");
		assertThat(stringifiedManipulation).containsAll("-($0=(Entity=test.Entity)('ID-1'))");
	}

	private void checkEscape(String origin, String expected) throws Exception {
		assertThat(escape(origin)).isEqualTo(expected);
	}

	private String escape(String s) throws Exception {
		StringBuilder sb = new StringBuilder();
		ManipulationStringifier.writeEscaped(sb, s);
		return sb.toString();
	}

}
