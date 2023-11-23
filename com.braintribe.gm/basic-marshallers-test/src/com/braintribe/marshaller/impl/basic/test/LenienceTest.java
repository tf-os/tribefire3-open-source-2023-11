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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.jseval.genericmodel.PrettyJavaScriptPrototypes;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.codec.marshaller.dom.GmXmlCodec;
import com.braintribe.codec.marshaller.jseval.JsEvalMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.xml.XmlMarshaller;
import com.braintribe.marshaller.impl.basic.test.model.JackOfAllTrades;

public class LenienceTest {
	private StaxMarshaller staxMarshaller;
	private XmlMarshaller xmlMarshaller;
	private GmXmlCodec<Object> xmlCodec;
	private BinMarshaller binMarshaller;
	private JsEvalMarshaller jseMarshaller;

	@Test
	public void testStaxLenience() throws Exception {
		Marshaller marshaller = getStaxMarshaller();

		InputStream in = getClass().getResourceAsStream("lenience.xml");

		marshaller.unmarshall(in);

		in.close();

		in = getClass().getResourceAsStream("lenience4.xml");

		marshaller.unmarshall(in);

		in.close();

	}

	public static void generateFiles() {
		try {
			Marshaller xmlMarshaller = new XmlMarshaller();
			Marshaller staxMarshaller = new StaxMarshaller();

			FileOutputStream out1 = new FileOutputStream("lenience.xml");
			FileOutputStream out2 = new FileOutputStream("lenience4.xml");

			Object assembly = getAssembly();

			xmlMarshaller.marshall(out1, assembly);
			staxMarshaller.marshall(out2, assembly);

			out1.close();
			out2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Object getAssembly() {
		JackOfAllTrades j1 = JackOfAllTrades.T.create();
		JackOfAllTrades j2 = JackOfAllTrades.T.create();

		return Arrays.asList(j1, j2);
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
}
