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
package com.braintribe.marshaller.impl.basic.test;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.jseval.genericmodel.PrettyJavaScriptPrototypes;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.codec.marshaller.dom.GmXmlCodec;
import com.braintribe.codec.marshaller.jseval.JsEvalMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxResearchMarshaller;
import com.braintribe.codec.marshaller.xml.XmlMarshaller;
import com.braintribe.marshaller.impl.basic.test.model.JackOfAllTrades;
import com.braintribe.marshaller.impl.basic.test.model.Mode;
import com.braintribe.marshaller.impl.basic.test.model.Moron;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class StaxTest {
	private StaxMarshaller staxMarshaller;
	private XmlMarshaller xmlMarshaller;
	private GmXmlCodec<Object> xmlCodec;
	private BinMarshaller binMarshaller;
	private JsEvalMarshaller jseMarshaller;
	private JsonStreamMarshaller jsonResearchMarshaller;
	private StaxResearchMarshaller staxResearchMarshaller;

	@Test
	public void emptyTag() throws Exception {
		LocalizedString ls = LocalizedString.T.create();
		// ls.getLocalizedValues().put("default", "egal");

		GmCodec<Object, String> stringCodec = getStaxMarshaller().getStringCodec();
		String xml = stringCodec.encode(ls, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid).build());

		System.out.println(xml);

		stringCodec.decode(xml);
	}

	@Test
	public void viewStaxOutput() throws Exception {
		Object assembly = getAssembly();
		String xml = getStaxMarshaller().getStringCodec().encode(assembly,
				GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid).build());
		System.out.println(xml);
	}

	public void viewJsonResearchOutput() throws Exception {
		Object assembly = getAssembly();
		String xml = getJsonResearchMarshaller().getStringCodec().encode(assembly);

		System.out.println(xml);
	}

	public void viewStaxResearchOutput() throws Exception {
		Object assembly = getAssembly();
		String xml = getStaxResearchMarshaller().getStringCodec().encode(assembly);

		System.out.println(xml);
	}

	public void testXmlCodec() throws Exception {
		Object assembly = getAssembly();
		String xml = getXmlCodec().encode(assembly);

		System.out.println(xml);

		/* Object assembly2 = getXmlCodec().decode(xml);
		 * 
		 * assert(AssemblyComparison.equals(assembly, assembly2)); */
	}

	@Test
	public void testStax() throws Exception {
		test(getStaxMarshaller());
	}

	@Test
	public void testBin() throws Exception {
		test(getBinMarshaller());
	}

	public void testJse() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getJseMarshaller().marshall(out, getAssembly());
		out.close();
		System.out.println(new String(out.toByteArray(), "UTF-8"));
	}

	@Test
	public void testEntityVisiting() throws IOException {

		JackOfAllTrades j1 = JackOfAllTrades.T.create();
		JackOfAllTrades j2 = JackOfAllTrades.T.create();

		j1.setOther(j2);
		j2.setOther(j1);

		List<JackOfAllTrades> jacks = new ArrayList<>();
		jacks.add(j1);
		jacks.add(j2);

		StaxMarshaller marshaller = StaxMarshaller.defaultInstance;
		StreamPipe pipe = StreamPipes.simpleFactory().newPipe("test");

		List<GenericEntity> marshalledEntities = new ArrayList<>();
		List<GenericEntity> unmarshalledEntities = new ArrayList<>();

		GmSerializationOptions so = GmSerializationOptions.defaultOptions.derive().set(EntityVisitorOption.class, marshalledEntities::add).build();
		GmDeserializationOptions dso = GmDeserializationOptions.defaultOptions.derive().set(EntityVisitorOption.class, unmarshalledEntities::add)
				.build();

		try (OutputStream out = pipe.openOutputStream()) {
			marshaller.marshall(out, jacks, so);
		}

		try (InputStream in = pipe.openInputStream()) {
			marshaller.unmarshall(in, dso);
		}

		Assertions.assertThat(marshalledEntities.size()).isEqualTo(2);
		Assertions.assertThat(marshalledEntities).contains(j1);
		Assertions.assertThat(marshalledEntities).contains(j2);
		Assertions.assertThat(unmarshalledEntities.size()).isEqualTo(2);
	}

	@Ignore
	private void test(Marshaller marshaller) throws Exception {
		Object assembly1 = getAssembly();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshall(out, assembly1);
		out.close();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		Object assembly2 = marshaller.unmarshall(in);

		assertThat(AssemblyComparison.equals(assembly1, assembly2)).isTrue();
	}

	private static Object getAssembly() {
		JackOfAllTrades j1 = JackOfAllTrades.T.create();
		JackOfAllTrades j2 = JackOfAllTrades.T.create();
		JackOfAllTrades j3 = JackOfAllTrades.T.create();
		JackOfAllTrades j4 = JackOfAllTrades.T.create();
		JackOfAllTrades j5 = JackOfAllTrades.T.create();

		j1.setId(1L);
		j1.setStringValue("master");
		j1.setBooleanValue(true);
		j1.setPrimitiveBooleanValue(true);
		j1.setIntegerValue(23);
		j1.setPrimitiveIntegerValue(23);
		j1.setLongValue(42L);
		j1.setPrimitiveLongValue(42L);
		j1.setFloatValue((float) Math.E);
		j1.setPrimitiveFloatValue((float) Math.E);
		j1.setDoubleValue(Math.PI);
		j1.setPrimitiveDoubleValue(Math.PI);
		j1.getStringList().addAll(Arrays.asList("one", "two", "three"));
		j1.getEntityList().addAll(Arrays.asList(j2, j3, j4));
		j1.getStringSet().addAll(Arrays.asList("one", "two", "three"));
		j1.getEntitySet().addAll(Arrays.asList(j2, j3, j4));
		j1.getStringStringMap().put("foo", "bar");
		j1.getStringStringMap().put("fix", "foxy");
		j1.getEntityEntityMap().put(j2, j3);
		j1.getEntityEntityMap().put(j3, j4);
		j1.setDecimalValue(new BigDecimal("2045"));
		j1.setDateValue(new Date());
		j1.setMode(Mode.fast);
		j1.setOther(j5);
		j1.getObjectList().add(j1);
		j1.getObjectList().add("Hallo");
		j1.getObjectList().add(Mode.slow);
		j1.setObject(j2);
		j1.setObjectL(newList());
		j1.setObjectS(newSet());
		j1.setObjectM(newMap());
		j2.entityType().getProperty("other").setAbsenceInformation(j2, GMF.absenceInformation());
		j2.setStringValue("slave1");
		j2.setId(2L);
		j3.setStringValue("slave2");
		j3.setId(3L);
		j4.setStringValue("slave3");
		j4.setId(4L);
		j5.setStringValue("slave4");
		j5.setId(5L);

		Moron moron = Moron.T.create();
		moron.setId(1L);
		moron.setText("Holla");
		moron.getTexts().add("one");
		moron.getNumbers().add(1);
		moron.getNumbers().add(2);

		com.braintribe.marshaller.impl.basic.test.model.sub.Moron moron2 = com.braintribe.marshaller.impl.basic.test.model.sub.Moron.T.create();
		moron2.setId(1L);
		moron.setText("<   >\"\n\r\n");

		return Arrays.asList(j1, moron, moron2, true, "Hallo Welt!");
	}

	@Ignore
	private StaxMarshaller getStaxMarshaller() {
		if (staxMarshaller == null) {
			staxMarshaller = new StaxMarshaller();
			return staxMarshaller;

		}

		return staxMarshaller;
	}

	@Ignore
	private StaxResearchMarshaller getStaxResearchMarshaller() {
		if (staxResearchMarshaller == null) {
			staxResearchMarshaller = new StaxResearchMarshaller();
			return staxResearchMarshaller;

		}

		return staxResearchMarshaller;
	}

	@Ignore
	private XmlMarshaller getXmlMarshaller() {
		if (xmlMarshaller == null) {
			xmlMarshaller = new XmlMarshaller();
			return xmlMarshaller;

		}

		return xmlMarshaller;
	}

	@Ignore
	private GmXmlCodec<Object> getXmlCodec() {
		if (xmlCodec == null) {
			xmlCodec = new GmXmlCodec<Object>();
		}

		return xmlCodec;
	}

	@Ignore
	private BinMarshaller getBinMarshaller() {
		if (binMarshaller == null) {
			binMarshaller = new BinMarshaller();
			binMarshaller.setWriteRequiredTypes(true);
		}

		return binMarshaller;
	}

	@Ignore
	private JsEvalMarshaller getJseMarshaller() {
		if (jseMarshaller == null) {
			jseMarshaller = new JsEvalMarshaller();
			GenericModelJsEvalCodec<Object> codec = new GenericModelJsEvalCodec<Object>();
			codec.setHostedMode(false);
			codec.setPrototypes(new PrettyJavaScriptPrototypes());
			jseMarshaller.setCodec(codec);
		}

		return jseMarshaller;
	}

	@Ignore
	private JsonStreamMarshaller getJsonResearchMarshaller() {
		if (jsonResearchMarshaller == null) {
			jsonResearchMarshaller = new JsonStreamMarshaller();
		}

		return jsonResearchMarshaller;
	}
}
