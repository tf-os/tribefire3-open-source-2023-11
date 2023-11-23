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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.jseval.genericmodel.CondensedJavaScriptPrototypes;
import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.codec.marshaller.bin.BinResearchMarshaller;
import com.braintribe.codec.marshaller.dom.DomMarshaller;
import com.braintribe.codec.marshaller.jse.JseMarshaller;
import com.braintribe.codec.marshaller.jseval.JsEvalMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.sax.SaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxResearchMarshaller;
import com.braintribe.testing.category.Slow;
import com.braintribe.utils.IOTools;

public class SmoodUnmarshalTest {
	private SaxMarshaller<?> saxMarshaller;
	private Marshaller xmlMarshaller;
	private StaxMarshaller staxMarshaller;
	private BinMarshaller binMarshaller;
	private BinResearchMarshaller binResearchMarshaller;
	private Object assembly;
	private JseMarshaller jseMarshaller;
	private JsEvalMarshaller jsEvalMarshaller;
	private DomMarshaller domMarshaller;
	private JsonStreamMarshaller jsonMarshaller;
	private StaxResearchMarshaller staxResearchMarshaller;
	private Bin2Marshaller bin2ResearchMarshaller;

	public static void main(String[] args) throws Exception {
		SmoodUnmarshalTest app = new SmoodUnmarshalTest();
		app.testCortexUnmarshal();
	}

	@Test
	public void testCortexUnmarshal() throws Exception {

		File file = new File("current.xml");

		if (!file.exists()) {
			return;
		}

		int count = 20;
		byte[] cortexData = IOTools.slurpBytes(new FileInputStream(file));

		long s = System.currentTimeMillis();
		assembly = getXmlMarshaller().unmarshall(new ByteArrayInputStream(cortexData));
		long e = System.currentTimeMillis();
		long d = e - s;
		System.out.println("read data with XmlMarshaller (reference codec): " + d + " ms");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getStaxMarshaller().marshall(out, assembly);

		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		getBinMarshaller().marshall(out2, assembly);

		ByteArrayOutputStream out3 = new ByteArrayOutputStream();
		getBin2Marshaller().marshall(out3, assembly);

		byte[] cortexData4 = out.toByteArray();
		byte[] cortexDataBin = out2.toByteArray();
		byte[] cortexDataBin2 = out3.toByteArray();

		System.out.println("--- first run - Caching + 1. JIT pass");
		testXmlMarshallerUnmarshall(cortexData, 1);
		// testSaxMarshallerUnmarshall(cortexData, 1);
		testStaxMarshallerUnmarshall(cortexData4, 1);
		testBinMarshallerUnmarshall(cortexDataBin, 1);
		testBin2MarshallerUnmarshall(cortexDataBin2, 1);
		testDomMarshallerUnmarshall(cortexData4, 1);

		System.out.println("--- second run");
		testXmlMarshallerUnmarshall(cortexData, 1);
		// testSaxMarshallerUnmarshall(cortexData, 1);
		testStaxMarshallerUnmarshall(cortexData4, 1);
		testBinMarshallerUnmarshall(cortexDataBin, 1);
		testBin2MarshallerUnmarshall(cortexDataBin2, 1);
		testDomMarshallerUnmarshall(cortexData4, 1);

		System.out.println("--- " + count + " runs");
		testXmlMarshallerUnmarshall(cortexData, count);
		// testSaxMarshallerUnmarshall(cortexData, count);
		testStaxMarshallerUnmarshall(cortexData4, count);
		testBinMarshallerUnmarshall(cortexDataBin, count);
		testBin2MarshallerUnmarshall(cortexDataBin2, count);
		testDomMarshallerUnmarshall(cortexData4, count);
	}

	@Category(Slow.class)
	@Test
	public void testCortexMarshal() throws Exception {

		File file = new File("current.xml");

		if (!file.exists()) {
			return;
		}

		int count = 20;

		assembly = getXmlMarshaller().unmarshall(new FileInputStream(file));

		Marshaller marshallers[] = new Marshaller[] { getXmlMarshaller(),
				// getSaxMarshaller(),
				getStaxMarshaller(), getStaxResearchMarshaller(), getBinMarshaller(), getBinResearchMarshaller(), getBin2Marshaller(),
				getDomMarshaller(), getJsEvalMarshaller(), getJseMarshaller(), getJsonMarshaller() };

		System.out.println("--- first run - Caching + 1. JIT pass");
		for (Marshaller marshaller : marshallers) {
			testMarshallerMarshall(marshaller, assembly, 1);
		}

		System.out.println("--- second run");
		for (Marshaller marshaller : marshallers) {
			testMarshallerMarshall(marshaller, assembly, 1);
		}

		System.out.println("--- 5 runs");
		for (Marshaller marshaller : marshallers) {
			testMarshallerMarshall(marshaller, assembly, 5);
		}

		System.out.println("--- " + count + " runs");
		for (Marshaller marshaller : marshallers) {
			testMarshallerMarshall(marshaller, assembly, count);
		}
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
	public void testBin2MarshallerUnmarshall(byte[] data, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			_testBin2MarshallerUnmarshall(bais);
			bais.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testBin2MarshallerUnmarshall: " + (stop - start) + " ms");
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
	public void _testStaxMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getStaxMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testBinMarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getBinMarshaller();
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Ignore
	public void _testBin2MarshallerUnmarshall(InputStream in) throws Exception {
		Marshaller marshaller = getBin2Marshaller();
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
	public void testMarshallerMarshall(Marshaller marshaller, Object assembly, int count) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshall(out, assembly, GmSerializationOptions.deriveDefaults().useDirectPropertyAccess(true).build());
			out.close();
		}
		long stop = System.currentTimeMillis();
		System.out.println("marshalling with " + marshaller.getClass().getSimpleName() + ": " + (stop - start) + " ms");
	}

	@Ignore
	private Marshaller getXmlMarshaller() {
		if (xmlMarshaller == null) {
			xmlMarshaller = StaxMarshaller.defaultInstance;
		}

		return xmlMarshaller;
	}

	@Ignore
	private Marshaller getSaxMarshaller() {
		if (saxMarshaller == null) {
			saxMarshaller = new SaxMarshaller<Object>();
			saxMarshaller.setWriteRequiredTypes(true);
			saxMarshaller.setCreateEnhancedEntities(true);
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
			codec.setPrototypes(new CondensedJavaScriptPrototypes());
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
	private BinResearchMarshaller getBinResearchMarshaller() {
		if (binResearchMarshaller == null) {
			binResearchMarshaller = new BinResearchMarshaller();
			binResearchMarshaller.setWriteRequiredTypes(true);
		}

		return binResearchMarshaller;
	}

	@Ignore
	private Bin2Marshaller getBin2Marshaller() {
		if (bin2ResearchMarshaller == null) {
			bin2ResearchMarshaller = new Bin2Marshaller();
		}

		return bin2ResearchMarshaller;
	}

	@Ignore
	private JsonStreamMarshaller getJsonMarshaller() {
		if (jsonMarshaller == null) {
			jsonMarshaller = new JsonStreamMarshaller();
		}

		return jsonMarshaller;
	}

}
