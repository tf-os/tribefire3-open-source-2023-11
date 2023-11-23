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
package com.braintribe.codec.marshaller.json;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.model.TestEntity;
import com.braintribe.codec.marshaller.json.model.TestEnum;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;

public class MarshallingLab {
	@Test
	public void mapOutput() {
		TestEntity entity = TestEntity.T.create();

		entity.getEnumMap().put(TestEnum.ONE, "one");
		entity.getEnumMap().put(TestEnum.TWO, "two");
		entity.getStringMap().put("ONE", "one");
		entity.getStringMap().put("TWO", "two");
		entity.setDoubleValue(3.14);
		entity.setFloatValue(2.81F);
		entity.setIntegerValue(42);
		entity.setLongValue(2321321321321321321L);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		marshaller.marshall(System.out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
	}

	@Test
	public void numberParsing() {

		String json = "[1, 2.3, 123213214214321321]";

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		List<Object> list = (List<Object>) marshaller.decode(json);

		for (Object element : list) {
			System.out.println(element.getClass() + ": " + element);
		}
	}

	// This Cannot be here!! It depends on document-model, which is in tribefire.cortex repo
	// @Test
	// public void documentParsing() throws Exception {
	//
	// JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	// try (InputStream in = new BufferedInputStream(new FileInputStream(new File("res/document.json")))) {
	// Document doc = (Document) marshaller.unmarshall(in);
	//
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// marshaller.marshall(baos, doc, GmSerializationOptions.defaults.outputPrettiness(OutputPrettiness.high));
	//
	// String json = baos.toString("UTF-8");
	//
	// try (InputStream bain = new ByteArrayInputStream(baos.toByteArray())) {
	// Document checkDoc = (Document) marshaller.unmarshall(bain);
	//
	// System.out.println("Successfully parsed document: "+checkDoc+" from\n"+json);
	// }
	// }
	//
	// }

	// @Test
	public void speedComparisonStreamVsDom() {

		GmMetaModel model = GMF.getTypeReflection().getModel("com.braintribe.model:MetaModel").getMetaModel();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		JsonStreamMarshaller streamMarshaller = new JsonStreamMarshaller();

		String json = marshaller.encode(model, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());

		System.out.println(json);

		{
			long s = System.nanoTime();
			try (StringReader reader = new StringReader(json)) {
				marshaller.unmarshall(reader, GmDeserializationOptions.deriveDefaults().build());
			}
			long d = System.nanoTime() - s;
			System.out.println(d / 1_000_000D);
		}

		{
			long s = System.nanoTime();
			try (StringReader reader = new StringReader(json)) {
				streamMarshaller.unmarshall(reader, GmDeserializationOptions.deriveDefaults().build());
			}
			long d = System.nanoTime() - s;
			System.out.println(d / 1_000_000D);
		}

		System.out.println(json);
	}
}
