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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchange.GenericExchangePayload;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.FsBasedModelArtifactBuilder;
import com.braintribe.utils.FileTools;

public class ModelPersistenceExpert {

	private static FsBasedModelArtifactBuilder artifactBuilder = new FsBasedModelArtifactBuilder();
	private static StaxMarshaller marshaller = new StaxMarshaller();

	public static File dumpModelJar(GmMetaModel model, File folder) {
		try {
			artifactBuilder.setModel(model);
			artifactBuilder.setVersionFolder(folder);
			artifactBuilder.publish();

			File[] jars = folder.listFiles((dir, name) -> isJar(name));
			return jars.length > 0 ? jars[0] : null;

		} catch (Exception e) {
			throw new IllegalStateException("cannot write model [" + model.getName() + "] as " + e);
		}
	}

	private static boolean isJar(String name) {
		if (name.endsWith("sources.jar"))
			return false;

		if (!name.endsWith("jar"))
			return false;

		return true;
	}

	public static File dumpMappingModel(GmMetaModel mappingModel, File output) {
		String fileName = mappingModel.getName().replace(':', '.') + ".model.xml";
		return dumpMappingModel(mappingModel, output, fileName);
	}

	public static File dumpMappingModel(GmMetaModel mappingModel, File output, String fileName) {
		File file = new File(output, fileName);
		return FileTools.write(file) //
				.usingOutputStream(out -> marshaller.marshall(out, mappingModel, prettyOptions()));
	}

	public static File dumpExchangePackage(File output, String exchangePackageName, List<GmType> shallowSkeletonTypes, GmMetaModel skeletonModel,
			GmMetaModel... enrichmentModels) {

		ExchangePackage exchangePackage = ExchangePackage.T.create();
		exchangePackage.setExportedBy(ModelPersistenceExpert.class.getName());
		exchangePackage.setExported(new Date());
		List<GmMetaModel> models = asList(enrichmentModels);
		models.add(skeletonModel);

		for (GmMetaModel model : models) {
			GenericExchangePayload skPayload = GenericExchangePayload.T.create();
			skPayload.setAssembly(model);
			//
			// add all shallow types of the skeleton model, as the enrichment models don't have any
			if (model == skeletonModel) {
				skPayload.setExternalReferences(newSet(shallowSkeletonTypes));
			}

			exchangePackage.getPayloads().add(skPayload);
		}

		File file = new File(output, exchangePackageName);
		return FileTools.write(file) //
				.usingOutputStream(out -> marshaller.marshall(out, exchangePackage, prettyOptions()));
	}

	private static GmSerializationOptions prettyOptions() {
		return GmSerializationOptions.deriveDefaults() //
				.outputPrettiness(OutputPrettiness.high) //
				.build();
	}

}
