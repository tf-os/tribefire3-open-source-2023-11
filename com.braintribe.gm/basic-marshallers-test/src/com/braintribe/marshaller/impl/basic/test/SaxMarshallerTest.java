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

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.sax.SaxMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.xml.XmlMarshaller;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;

/**
 * Provides tests for {@link SaxMarshallerTest}.
 * 
 * @author michael.lafite
 */
public class SaxMarshallerTest {
	private GmMetaModel model;
	private byte[] data;
	private final int loops = 100;

	private SaxMarshaller<?> saxMarshaller;
	private XmlMarshaller xmlMarshaller;
	private StaxMarshaller staxMarshaller;

	public static void main(String[] args) throws Exception {
		SaxMarshallerTest test = new SaxMarshallerTest();
		test.run();
	}

	public SaxMarshallerTest() throws Exception {
		loadData();
	}

	public void run() {
		try {
			/* testXmlMarshallerMarshall(); testSax2MarshallerMarshall(); testSaxMarshallerMarshall(); */

			for (int i = 0; i < 10; i++) {
				testXmlMarshallerUnmarshall();
				testSaxMarshallerUnmarshall();
				testStaxMarshallerUnmarshall();
				System.out.println("------------");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadData() throws Exception {
		System.out.println("loading data ....");
		model = GMF.getTypeReflection().getModel("com.braintribe.gm:meta-model").getMetaModel();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Marshaller marshaller = getXmlMarshaller();
		marshaller.marshall(out, model, GmSerializationOptions.deriveDefaults().build());
		out.flush();
		data = out.toByteArray();

	}

	public void testSaxMarshallerMarshall() throws Exception {
		_testSaxMarshallerMarshall();
		long start = System.currentTimeMillis();
		for (int i = 0; i < loops; i++)
			_testSaxMarshallerMarshall();
		long stop = System.currentTimeMillis();
		System.out.println("testSaxMarshallerMarshall: " + (stop - start) + " ms");
	}

	public void testSaxMarshallerUnmarshall() throws Exception {
		_testSaxMarshallerUnmarshall();
		long start = System.currentTimeMillis();
		for (int i = 0; i < loops; i++) {
			_testSaxMarshallerUnmarshall();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testSaxMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	public void testStaxMarshallerUnmarshall() throws Exception {
		_testStaxMarshallerUnmarshall();
		long start = System.currentTimeMillis();
		for (int i = 0; i < loops; i++) {
			_testStaxMarshallerUnmarshall();
		}
		long stop = System.currentTimeMillis();
		System.out.println("testStaxMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	public void testXmlMarshallerMarshall() throws Exception {
		_testXmlMarshallerMarshall();
		long start = System.currentTimeMillis();
		for (int i = 0; i < loops; i++)
			_testXmlMarshallerMarshall();
		long stop = System.currentTimeMillis();
		System.out.println("testXmlMarshallerMarshall: " + (stop - start) + " ms");
	}

	public void testXmlMarshallerUnmarshall() throws Exception {
		_testXmlMarshallerUnmarshall();
		long start = System.currentTimeMillis();
		for (int i = 0; i < loops; i++)
			_testXmlMarshallerUnmarshall();
		long stop = System.currentTimeMillis();
		System.out.println("testXmlMarshallerUnmarshall: " + (stop - start) + " ms");
	}

	public void _testSaxMarshallerMarshall() throws Exception {
		Marshaller marshaller = getSaxMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshall(out, model, GmSerializationOptions.deriveDefaults().build());
	}

	public void _testSaxMarshallerUnmarshall() throws Exception {
		Marshaller marshaller = getSaxMarshaller();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	public void _testStaxMarshallerUnmarshall() throws Exception {
		Marshaller marshaller = getStaxMarshaller();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	public void _testXmlMarshallerMarshall() throws Exception {
		Marshaller marshaller = getXmlMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshall(out, model, GmSerializationOptions.deriveDefaults().build());
	}

	public void _testXmlMarshallerUnmarshall() throws Exception {
		Marshaller marshaller = getXmlMarshaller();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	private Marshaller getXmlMarshaller() {
		if (xmlMarshaller == null) {
			xmlMarshaller = new XmlMarshaller();
		}

		return xmlMarshaller;
	}

	private Marshaller getSaxMarshaller() {
		if (saxMarshaller == null) {
			saxMarshaller = new SaxMarshaller<Object>();
			saxMarshaller.setWriteRequiredTypes(true);
		}

		return saxMarshaller;
	}

	private Marshaller getStaxMarshaller() {
		if (staxMarshaller == null) {
			staxMarshaller = new StaxMarshaller();
			// staxMarshaller.setVersion4(true);
			return staxMarshaller;

		}

		return staxMarshaller;
	}

}
