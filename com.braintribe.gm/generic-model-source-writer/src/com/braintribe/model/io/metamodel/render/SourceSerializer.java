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
package com.braintribe.model.io.metamodel.render;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import com.braintribe.logging.Logger;
import com.braintribe.model.io.metamodel.MetaModelSourceDescriptor;
import com.braintribe.utils.IOTools;

/**
 * Stores given source-files represented by {@link MetaModelSourceDescriptor} inside the {@link #outputDirectory} passed
 * through constructor.
 */
public class SourceSerializer {

	private final File outputDirectory;

	

	private static final Logger log = Logger.getLogger(SourceSerializer.class);
	

	public SourceSerializer(File outputDirectory) {
		this.outputDirectory = outputDirectory;
		
		if (outputDirectory.exists()) {
			if (!outputDirectory.isDirectory()) {
				throw new IllegalArgumentException("Source writing initialization failed as '" + outputDirectory.getAbsolutePath()
						+ "' is not a valid directory.");
			}
		}

		
	}



	public void writeSourceFile(MetaModelSourceDescriptor sourceDescriptor) {
		try {
			writeSourceHelper(sourceDescriptor);

		} catch (Exception e) {
			log.warn("Failed to save source file '" + sourceDescriptor.sourceRelativePath + "' as " + e);
		}
	}

	private void writeSourceHelper(MetaModelSourceDescriptor sourceDescriptor) {
		File outputFile = getFileInExistingFolder(sourceDescriptor.sourceRelativePath);

		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

			writer.write(sourceDescriptor.sourceCode);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			IOTools.closeCloseable(writer, log);
		}
	}

	private File getFileInExistingFolder(String relativeSourcePath) {
		File result = new File(outputDirectory, relativeSourcePath);

		File parentFile = result.getParentFile();
		if (parentFile.isDirectory() || parentFile.mkdirs()) {
			return result;
		}

		throw new IllegalArgumentException("Failed to create folders to store file: " + result.getAbsolutePath());
	}

}
