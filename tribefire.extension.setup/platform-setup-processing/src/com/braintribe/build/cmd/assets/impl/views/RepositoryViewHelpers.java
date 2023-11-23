package com.braintribe.build.cmd.assets.impl.views;

import java.io.File;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.FileTools;

public class RepositoryViewHelpers {

	public static <T extends GenericEntity> T readYamlFile(File file) {
		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
				.absentifyMissingProperties(true) //
				.build();
		return (T) FileTools.read(file).fromInputStream(it -> new YamlMarshaller().unmarshall(it, options));
	}

	public static void writeYamlFile(GenericEntity entityToWrite, File file) {
		FileTools.write(file).usingOutputStream( //
				os -> new YamlMarshaller().marshall(os, entityToWrite, GmSerializationOptions.defaultOptions.derive() //
						.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
						.writeEmptyProperties(false) //
						.writeAbsenceInformation(false) //
						.build() //
				));
	}
}
