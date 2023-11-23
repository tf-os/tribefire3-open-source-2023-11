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
package tribefire.extension.xml.schemed.test.commons.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.ModelArtifactBuilder;
import com.braintribe.model.processing.meta.ModelArtifactBuilderException;

public class ModelPersistenceExpert {

	private static ModelArtifactBuilder artifactBuilder = new ModelArtifactBuilder();
	private static StaxMarshaller marshaller = new StaxMarshaller();

	public static File dumpModelJar(GmMetaModel model, File folder) {
		try {
			artifactBuilder.setModel(model);
			artifactBuilder.setVersionFolder(folder);
			artifactBuilder.publish();

			for (File file : folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith("sources.jar"))
						return false;
					if (!name.endsWith("jar"))
						return false;

					return true;
				}
			})) {
				return file;
			}
			;

		} catch (ModelArtifactBuilderException e) {
			throw new IllegalStateException("cannot write model [" + model.getName() + "] as " + e);
		}
		return null;
	}

	public static File dumpMappingModel(GmMetaModel mappingModel, File output) {
		String fileName = mappingModel.getName().replace(':', '.') + ".model.xml";
		File file = new File(output, fileName);
		try (OutputStream out = new FileOutputStream(file)) {

			marshaller.marshall(out, mappingModel, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
			return file;
		} catch (Exception e) {
			throw new IllegalStateException("cannot write model [" + mappingModel.getName() + "] as " + e);
		}
	}

}
