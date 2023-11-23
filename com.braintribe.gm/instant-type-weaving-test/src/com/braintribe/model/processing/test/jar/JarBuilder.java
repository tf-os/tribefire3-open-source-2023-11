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
package com.braintribe.model.processing.test.jar;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.synthesis.java.jar.JavaTypeSerializer;
import com.braintribe.model.processing.test.jar.model.Druid;
import com.braintribe.model.processing.test.jar.model.IDruid;
import com.braintribe.model.processing.test.jar.model.IPerson;
import com.braintribe.model.processing.test.jar.model.Person;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.model.weaving.ProtoGmMetaModel;

/**
 * 
 */
public class JarBuilder {

	private static final String SOURCES_FILE = "resource/ItwJar-sources.jar";
	private static final String BINARY_FILE = "resource/ItwJar.jar";

	private static ProtoGmMetaModel rawMetaModel = provideModel();

	public static void main(String[] args) {

		try (ZipOutputStream zos = zipOutputStream()) {
			serialize(zos);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("SUCCESSFULLY FINISHED");
	}

	private static void serialize(ZipOutputStream zos) throws Exception {
		try (ZipInputStream zis = zipInputStream()) {
			Map<String, String> sources = SourceJarReader.readSources(zis);
			JavaTypeSerializer.serialize(rawMetaModel, zos, sources);
		}
	}

	private static ZipOutputStream zipOutputStream() {
		File file = new File(BINARY_FILE);

		try {
			return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!", e);
		}
	}

	private static ZipInputStream zipInputStream() {
		File file = new File(SOURCES_FILE);

		try {
			return new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));

		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!", e);
		}
	}

	private static GmMetaModel provideModel() {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("test:JarBuilderModel", asList(Person.T, Druid.T, IPerson.T, IDruid.T));

		// @formatter:off
		result.getTypes().stream()
			.filter(GmType::isGmEntity)
			.forEach(t -> 
				t.setTypeSignature(t.getTypeSignature().replace("jar.model", "jar.model2"))
			);
		// @formatter:on

		return result;
	}

}
