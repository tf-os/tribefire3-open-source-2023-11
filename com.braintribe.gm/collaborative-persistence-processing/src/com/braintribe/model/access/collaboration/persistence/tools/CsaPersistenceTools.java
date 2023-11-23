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
package com.braintribe.model.access.collaboration.persistence.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulationParser;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class CsaPersistenceTools {

	public static final String TRUNK_STAGE = "trunk";

	/* The threshold value should be examined more, the initial 4 MB is just a guess of what is safe to load in memory. Roman allegedly had huge
	 * problems with a file of about 50 MB, so I guess 4 could still be OK. */
	private static final long MEGA_BYTE = 1024 * 1024;
	private static final long SMALL_FILE_SIZE_THRESHOLD = 4 * MEGA_BYTE;
	private static final long HOMEOPATHY_IGNORING_THRESHOLD = 100 * MEGA_BYTE;

	public static ParseResponse parseGmmlFile(File gmmlFile, Consumer<? super AtomicManipulation> manipulationConsumer) {
		return FileTools.read(gmmlFile).fromReader( //
				r -> ManipulationParser.parse(r, manipulationConsumer, parserConfig(gmmlFile)));
	}

	public static MutableGmmlManipulatorParserConfiguration parserConfig(File gmmlFile) {
		MutableGmmlManipulatorParserConfiguration result = Gmml.manipulatorConfiguration();
		result.setBufferEntireInput(isSmallFile(gmmlFile));

		return result;
	}

	private static boolean isSmallFile(File gmmlFile) {
		return gmmlFile.length() < SMALL_FILE_SIZE_THRESHOLD;
	}

	public static Set<String> resolveHomeopathicVariables(File gmmlFile) {
		if (!isBigEnoughToResolveHomeopathicVariables(gmmlFile))
			return Collections.emptySet();

		try (InputStream in = new FileInputStream(gmmlFile)) {
			return ManipulationParser.findHomeopathicVariables(new InputStreamReader(in), parserConfig(gmmlFile));
		} catch (Exception e) {
			throw new ManipulationPersistenceException("Error while processing GMML file: " + gmmlFile.getAbsolutePath(), e);
		}
	}

	private static boolean isBigEnoughToResolveHomeopathicVariables(File gmmlFile) {
		return gmmlFile.length() >= HOMEOPATHY_IGNORING_THRESHOLD;
	}

}
