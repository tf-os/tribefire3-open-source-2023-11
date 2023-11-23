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

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.xml.XmlMarshaller;

public class CrLfTest {
	private StaxMarshaller staxMarshaller;
	private XmlMarshaller xmlMarshaller;

	@Test
	public void testStax() throws Exception {
		StaxMarshaller marshaller = getStaxMarshaller();
		XmlMarshaller marshaller2 = getXmlMarshaller();
		Object assembly = getAssembly();
		FileOutputStream out = new FileOutputStream("crlf-stax4.xml");
		FileOutputStream out2 = new FileOutputStream("crlf-dom.xml");
		FileOutputStream out3 = new FileOutputStream("crlf-stax4x2.xml");
		marshaller.marshall(out, assembly);
		out.close();

		marshaller2.marshall(out2, assembly);
		out2.close();
		GmCodec<Object, String> stringCodec = marshaller.getStringCodec();
		String xml = stringCodec.encode(assembly);
		// System.out.println(xml);

		Object o = stringCodec.decode(xml);

		// String xml2 = stringCodec.encode(o);
		// System.out.println(xml2);

		marshaller.marshall(out3, o);
		out3.close();

		System.out.println(StringEscapeUtils.escapeJava(marshaller.unmarshall(new FileInputStream("crlf-stax4.xml")).toString()));
		System.out.println(StringEscapeUtils.escapeJava(marshaller.unmarshall(new FileInputStream("crlf-dom.xml")).toString()));
		System.out.println(StringEscapeUtils.escapeJava(marshaller.unmarshall(new FileInputStream("crlf-stax4x2.xml")).toString()));
		System.out.println(StringEscapeUtils.escapeJava(marshaller2.unmarshall(new FileInputStream("crlf-dom.xml")).toString()));
	}

	private static Object getAssembly() {
		return "<&>a\r\ne";
	}

	@Ignore
	private StaxMarshaller getStaxMarshaller() {
		if (staxMarshaller == null) {
			staxMarshaller = new StaxMarshaller();
			staxMarshaller.setCreateEnhancedEntities(true);
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
}
