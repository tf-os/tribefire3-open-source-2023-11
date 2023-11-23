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
package com.braintribe.model.io.metamodel;

import java.io.File;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.io.metamodel.render.MetaModelSourceGenerator;
import com.braintribe.model.io.metamodel.render.SourceWriterContext;
import com.braintribe.model.io.metamodel.render.serializer.SourceFileSerializer;
import com.braintribe.model.io.metamodel.render.serializer.SourceSerializer;
import com.braintribe.model.io.metamodel.render.serializer.SourceStreamSerializer;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * API class for generating sources for given {@link GmMetaModel}.
 */
public class GmSourceWriter {

	private final SourceWriterContext context = new SourceWriterContext();

	public void setGmMetaModel(GmMetaModel gmMetaModel) {
		setModelOracle(new BasicModelOracle(gmMetaModel));
	}

	public void setModelOracle(ModelOracle modelOracle) {
		context.modelOracle = modelOracle;
	}
	
	@Configurable
	public void setOutputDirectory(File outputDirectory) {
		context.outputDirectory = outputDirectory;
	}
	@Configurable
	public void setOutputStream(ZipOutputStream outputStream) {
		context.outputStream = outputStream;
	}

	/**
	 * This flag determines whether to create source files for classes which already exist. Some classes given in the GmMetaModel may
	 * already exist (like for instance GenericEntity).
	 */
	public void enableWritingSourcesForExistingClasses() {
		context.shouldWriteSourcesForExistingClasses = true;
	}

	public Map<String, String> writeMetaModelToDirectory() {
		return writeMetaModeToSourceSerializer(new SourceFileSerializer(context.outputDirectory));
	}
	
	public Map<String, String> writeMetaModelToStream() {
		return writeMetaModeToSourceSerializer(new SourceStreamSerializer(context.outputStream));
	}

	private Map<String, String> writeMetaModeToSourceSerializer(SourceSerializer sourceSerializer) {
		MetaModelSourceGenerator generator = new MetaModelSourceGenerator(context);
		return generator.generateSources(sourceSerializer);
	}
	
}
