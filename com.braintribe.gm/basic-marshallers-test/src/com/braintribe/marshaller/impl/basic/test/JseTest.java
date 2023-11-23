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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.jseval.genericmodel.PrettyJavaScriptPrototypes;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.jse.JseMarshaller;
import com.braintribe.codec.marshaller.jseval.JsEvalMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.marshaller.impl.basic.test.model.JackOfAllTrades;
import com.braintribe.marshaller.impl.basic.test.model.Mode;
import com.braintribe.marshaller.impl.basic.test.model.Moron;
import com.braintribe.model.generic.GMF;

public class JseTest {
	private JsEvalMarshaller jsEvalMarshaller;
	private JseMarshaller jseMarshaller;
	private JsonStreamMarshaller jsonResearchMarshaller;

	@Test
	public void testJse() throws Exception {
		Object assembly = getAssembly();
		String jse = getJseMarshaller().encode(assembly);
		System.out.println(jse);
	}
	public void testJson() throws Exception {
		Object assembly = getAssembly();
		String json = getJsonResearchMarshaller().encode(assembly,
				GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.mid).build());
		System.out.println(json);
	}

	public void testJseEval() throws Exception {
		Object assembly = getAssembly();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getJsEvalMarshaller().marshall(out, assembly);
		String jse = new String(out.toByteArray(), "UTF-8");
		System.out.println(jse);
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
