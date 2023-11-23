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
import java.io.File;
import java.io.FileInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.marshaller.EntityCollector;
import com.braintribe.codec.marshaller.StandardEntityCollector;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.utils.IOTools;

public class SmoodCustomCollectTest {
	private Marshaller xmlMarshaller;
	private Object assembly;

	@Test
	public void testCortexCustomCollect() throws Exception {

		File file = new File("current.xml");

		if (!file.exists()) {
			return;
		}

		byte[] cortexData = IOTools.slurpBytes(new FileInputStream(file));

		long s = System.currentTimeMillis();
		assembly = getXmlMarshaller().unmarshall(new ByteArrayInputStream(cortexData));
		long e = System.currentTimeMillis();
		long d = e - s;
		System.out.println("read data with XmlMarshaller (reference codec): " + d + " ms");

		boolean directPropertyAccess = true;

		s = System.currentTimeMillis();
		EntityCollector collector = new StandardEntityCollector();
		collector.setDirectPropertyAccess(directPropertyAccess);
		collector.collect(assembly);
		e = System.currentTimeMillis();
		d = e - s;
		System.out.println("scanned data with collector: " + d + " ms");
		s = System.currentTimeMillis();
		collector = new StandardEntityCollector();
		collector.setDirectPropertyAccess(directPropertyAccess);
		collector.collect(assembly);
		e = System.currentTimeMillis();
		d = e - s;
		System.out.println("scanned data with collector: " + d + " ms");
		s = System.currentTimeMillis();
		collector = new StandardEntityCollector();
		collector.setDirectPropertyAccess(directPropertyAccess);
		collector.collect(assembly);
		e = System.currentTimeMillis();
		d = e - s;
		System.out.println("scanned data with collector: " + d + " ms");

	}
	@Ignore
	private Marshaller getXmlMarshaller() {
		if (xmlMarshaller == null) {
			xmlMarshaller = StaxMarshaller.defaultInstance;
		}

		return xmlMarshaller;
	}
}
