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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.jseval.genericmodel.PrettyJavaScriptPrototypes;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.codec.marshaller.dom.DomMarshaller;
import com.braintribe.codec.marshaller.jse.JseMarshaller;
import com.braintribe.codec.marshaller.jseval.JsEvalMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.sax.SaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxResearchMarshaller;
import com.braintribe.marshaller.impl.basic.test.model.JackOfAllTrades;
import com.braintribe.marshaller.impl.basic.test.model.Mode;
import com.braintribe.marshaller.impl.basic.test.model.Moron;
import com.braintribe.marshaller.impl.basic.test.model.PrimitiveJohn;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;

public class SmoodContentTest {
	private SaxMarshaller<?> saxMarshaller;
	// private XmlMarshaller xmlMarshaller;
	private StaxMarshaller staxMarshaller;
	private BinMarshaller binMarshaller;
	private DomMarshaller domMarshaller;
	private JsEvalMarshaller jsEvalMarshaller;
	private JseMarshaller jseMarshaller;
	private JsonStreamMarshaller jsonResearchMarshaller;
	private StaxResearchMarshaller staxResearchMarshaller;
	private Bin2Marshaller bin2Marshaller;

	@Test
	public void testComparator() {
		Map<String, String> map1 = new HashMap<String, String>();
		Map<String, String> map2 = new HashMap<String, String>();

		map1.put("one", "1");
		map1.put("two", "2");

		map2.put("one", "1");
		map2.put("two", "2");

		Map<String, String> map3 = new HashMap<String, String>();
		Map<String, String> map4 = new HashMap<String, String>();

		map3.put("one", "1");
		map3.put("two", "2");

		map4.put("one", "1");
		map4.put("twos", "2");

		Map<String, String> map5 = new HashMap<String, String>();
		Map<String, String> map6 = new HashMap<String, String>();

		map5.put("one", "1");
		map5.put("two", "2");

		map6.put("one", "1");
		map6.put("two", "1");

		assertThat(AssemblyComparison.equals(map1, map2)).isTrue();
		assertThat(AssemblyComparison.equals(map3, map4)).isFalse();
		assertThat(AssemblyComparison.equals(map5, map6)).isFalse();

	}

	// @Test
	public void testXml() throws Exception {
		testMarshallerRoundtrip(getXmlMarshaller());
	}

	// @Test
	public void testJson() throws Exception {
		testMarshallerRoundtrip(getJsonResearchMarshaller());
	}

	// @Test
	public void testSax() throws Exception {
		testMarshallerRoundtrip(getSaxMarshaller());
	}

	// @Test
	public void testStax() throws Exception {
		testMarshallerRoundtrip(getStaxMarshaller());
	}

	// @Test
	public void testStaxResearch() throws Exception {
		testMarshallerRoundtrip(getStaxResearchMarshaller());
	}

	// @Test
	public void testDom() throws Exception {
		testMarshallerRoundtrip(getDomMarshaller());
	}

	@Test
	public void testBin() throws Exception {
		testMarshallerRoundtrip(getBinMarshaller());
	}

	@Test
	public void testBin2() throws Exception {
		testMarshallerRoundtrip(getBin2Marshaller());
	}

	@Ignore
	public void testMarshallerRoundtrip(Marshaller marshaller) throws Exception {
		Object assembly1 = getAssembly();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshall(out, assembly1);
		out.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Object assembly2 = marshaller.unmarshall(in);

		assertThat(AssemblyComparison.equals(assembly1, assembly2)).isTrue();
	}

	@Ignore
	private Object getAssembly() throws FileNotFoundException, MarshallException {
		File file = new File("current.xml");
		return getXmlMarshaller().unmarshall(new FileInputStream(file));
	}

	@Ignore
	private Object getAssembly4() {
		JackOfAllTrades j1 = JackOfAllTrades.T.create();
		j1.setId(5L);

		PrimitiveJohn moron = PrimitiveJohn.T.create();
		moron.setId(5L);

		return j1;
	}

	@Ignore
	private Object getAssembly3() {
		return "<>\r\n";
	}

	@Ignore
	private Object getAssembly2() {
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
		j1.getObjectList().add(j1);
		j1.getObjectList().add("Hallo");
		j1.getObjectList().add(Mode.slow);
		j1.setDecimalValue(new BigDecimal("2045"));
		j1.setDateValue(new Date());
		j1.setMode(Mode.fast);
		j1.setObject(j2);
		j1.setOther(j5);
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
	public void testXmlMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testXmlMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testXmlMarshallerUnmarshall: " + (stop - start) + " ms");
	}
	@Ignore
	public void _testXmlMarshallerUnmarshall(InputStream is) throws Exception {
		Marshaller marshaller = getXmlMarshaller();
		marshaller.unmarshall(is, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void testSaxMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testSaxMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testSaxMarshallerUnmarshall: " + (stop - start) + " ms");
	}
	@Ignore
	public void _testSaxMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getSaxMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void testStaxMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testStaxMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testStaxMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void testStax4MarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testStax4MarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testStax4MarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void testBinMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testBinMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testBinMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void testDomMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testDomMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testDomMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void testJsEvalMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testJsEvalMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testJsEvalMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void testJseMarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testJseMarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testJseMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	@Ignore
	public void _testStaxMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getStaxMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testStax4MarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getStaxMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testBinMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getBinMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testDomMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getDomMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testJsEvalMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getJsEvalMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testJseMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getJseMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	private Marshaller getXmlMarshaller() {
		if (staxMarshaller == null) {
			staxMarshaller = StaxMarshaller.defaultInstance;
		}

		return staxMarshaller;
	}

	@Ignore
	private Marshaller getSaxMarshaller() {
		if (saxMarshaller == null) {
			saxMarshaller = new SaxMarshaller<Object>();
			saxMarshaller.setWriteRequiredTypes(true);
		}

		return saxMarshaller;
	}

	@Ignore
	private Marshaller getStaxMarshaller() {
		if (staxMarshaller == null) {
			staxMarshaller = new StaxMarshaller();
			return staxMarshaller;
		}

		return staxMarshaller;
	}

	@Ignore
	private Marshaller getStaxResearchMarshaller() {
		if (staxResearchMarshaller == null) {
			staxResearchMarshaller = new StaxResearchMarshaller();
			return staxResearchMarshaller;
		}

		return staxMarshaller;
	}

	@Ignore
	private Marshaller getBinMarshaller() {
		if (binMarshaller == null) {
			binMarshaller = new BinMarshaller();
			binMarshaller.setWriteRequiredTypes(true);
			return binMarshaller;
		}

		return binMarshaller;
	}

	@Ignore
	private Marshaller getBin2Marshaller() {
		if (bin2Marshaller == null) {
			bin2Marshaller = new Bin2Marshaller();
			return bin2Marshaller;
		}

		return bin2Marshaller;
	}

	@Ignore
	private Marshaller getDomMarshaller() {
		if (domMarshaller == null) {
			domMarshaller = new DomMarshaller();
			return domMarshaller;
		}

		return staxMarshaller;
	}

	@Ignore
	private JsEvalMarshaller getJsEvalMarshaller() {
		if (jsEvalMarshaller == null) {
			jsEvalMarshaller = new JsEvalMarshaller();
			GenericModelJsEvalCodec<Object> codec = new GenericModelJsEvalCodec<Object>();
			codec.setHostedMode(false);
			codec.setPrototypes(new PrettyJavaScriptPrototypes());
			jsEvalMarshaller.setCodec(codec);
		}

		return jsEvalMarshaller;
	}

	@Ignore
	private JseMarshaller getJseMarshaller() {
		if (jseMarshaller == null) {
			jseMarshaller = new JseMarshaller();
			jseMarshaller.setHostedMode(false);
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
