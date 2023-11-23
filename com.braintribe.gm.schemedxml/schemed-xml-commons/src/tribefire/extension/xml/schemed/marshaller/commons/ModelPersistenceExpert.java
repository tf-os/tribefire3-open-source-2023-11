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
package tribefire.extension.xml.schemed.marshaller.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchange.GenericExchangePayload;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
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
		return dumpMappingModel(mappingModel, output, fileName);
	}
	public static File dumpMappingModel(GmMetaModel mappingModel, File output, String fileName) {
		File file = new File(output, fileName);
		try (OutputStream out = new FileOutputStream(file)) {

			marshaller.marshall(out, mappingModel, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
			return file;
		} catch (Exception e) {
			throw new IllegalStateException("cannot write model [" + mappingModel.getName() + "] as " + e);
		}
	}

	public static File dumpExchangePackage(File output, String exchangePackageName, List<GmType> shallowSkeletonTypes, GmMetaModel skeletonModel,
			GmMetaModel... enrichmentModels) {

		ExchangePackage exchangePackage = ExchangePackage.T.create();
		exchangePackage.setExportedBy(ModelPersistenceExpert.class.getName());
		exchangePackage.setExported(new Date());
		List<GmMetaModel> models = new ArrayList<>(Arrays.asList(enrichmentModels));
		models.add(skeletonModel);

		for (GmMetaModel model : models) {
			GenericExchangePayload skPayload = GenericExchangePayload.T.create();
			skPayload.setAssembly(model);
			//
			// add all shallow types of the skeleton model, as the enrichment models don't have any
			if (model == skeletonModel) {
				skPayload.setExternalReferences(new HashSet<>(shallowSkeletonTypes));
			}

			exchangePackage.getPayloads().add(skPayload);
		}

		File file = new File(output, exchangePackageName);
		try (OutputStream out = new FileOutputStream(file)) {

			marshaller.marshall(out, exchangePackage, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
			return file;
		} catch (Exception e) {
			throw new IllegalStateException("cannot write exchange package to [" + file.getAbsolutePath() + "] as " + e);
		}

	}

}
