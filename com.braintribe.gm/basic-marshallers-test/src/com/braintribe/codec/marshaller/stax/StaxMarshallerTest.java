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
package com.braintribe.codec.marshaller.stax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmListType;

public class StaxMarshallerTest {

	@Test
	public void testEncoding() throws Exception {
		StaxMarshaller staxMarshaller = new StaxMarshaller();

		GmListType element = GmListType.T.create();
		element.setTypeSignature("list<GenericEntity>");
		element.setId("type:list<GenericEntity>");

		Set<GenericEntity> assembly = new HashSet<>();
		assembly.add(element);

		//@formatter:off
		ByteArrayOutputStream marsahllerOut = new ByteArrayOutputStream();
		staxMarshaller.marshall(
				marsahllerOut, 
				assembly, 
				GmSerializationOptions
				.deriveDefaults()
					.stabilizeOrder(true)
					.outputPrettiness(OutputPrettiness.high)
					.writeEmptyProperties(false)
					.build()
				);
		//@formatter:on

		String xml = marsahllerOut.toString();
		System.out.println(xml);

		staxMarshaller.unmarshall(new ByteArrayInputStream(xml.getBytes()), GmDeserializationOptions.deriveDefaults().build());
	}

	@Test
	public void testXXEAttack() throws Exception {

		StaxMarshaller staxMarshaller = new StaxMarshaller();

		/* User user = User.T.create(); user.setFirstName("John"); user.setLastName("Doe");
		 * 
		 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); staxMarshaller.marshall(baos, user);
		 * 
		 * String text = new String(baos.toString("UTF-8")); System.out.println(text); */

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
		sb.append("<!DOCTYPE lolz [\n");
		sb.append("<!ENTITY lol SYSTEM \"file:///\">\n");
		sb.append("]>\n");
		sb.append("<?gm-xml version=\"4\"?>\n");
		sb.append("<gm-data>\n");
		sb.append("<required-types><t alias='User^CsSFfF' num='1'>com.braintribe.model.user.User</t></required-types>\n");
		sb.append("<root-value><r>User^CsSFfF-0</r></root-value>\n");
		sb.append("<pool>\n");
		sb.append("<E id='User^CsSFfF-0'>\n");
		sb.append("<s p='firstName'>John</s>\n");
		sb.append("<s p='lastName'>Doe</s>\n");
		sb.append("<s p='password'>&lol;</s>\n");
		sb.append("</E>\n");
		sb.append("</pool>\n");
		sb.append("</gm-data>\n");

		ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

		try {
			staxMarshaller.unmarshall(bais);
			throw new AssertionError("XXE attack was successful.");
		} catch (Exception e) {
			// This is expected
			return;
		}

	}

}
