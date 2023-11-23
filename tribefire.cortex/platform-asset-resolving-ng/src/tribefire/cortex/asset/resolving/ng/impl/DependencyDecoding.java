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
package tribefire.cortex.asset.resolving.ng.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingConstants;

public class DependencyDecoding implements PlatformAssetResolvingConstants {
	private static PartIdentification dependencyPartTuple = PartIdentification.parse(PART_IDENTIFIER_DEPENDENCY_MAN);
	private static MutableGmmlManipulatorParserConfiguration manipulationParserConfig;

	public static Map<String, Object> decodeDependency(AnalysisDependency dependency) {
		Map<String, Object> info = dependency.getOrigin().getParts().entrySet().stream()
				.filter(e -> HashComparators.partIdentification.compare(dependencyPartTuple, e.getKey())) //
				.map(Map.Entry::getValue) //
				.map(DependencyDecoding::decodeMap) //
				.findFirst() //
				.orElse(Collections.emptyMap()); //

		return info;
	}

	/**
	 * load the {@link PlatformAssetNature} from a {@link VirtualPart}
	 * 
	 * @param virtualPart - the {@link VirtualPart}
	 * @return - the {@link PlatformAssetNature} contained
	 */
	private static Map<String, Object> decodeMap(String payload) {
		try (Reader reader = new StringReader(payload)) {
			return loadNatureFromReader(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	private static Map<String, Object> loadNatureFromReader(Reader reader) {
		BasicManagedGmSession entityManager = new BasicManagedGmSession();
		ParseResponse response = null;
		response = ManipulatorParser.parse(reader, entityManager, getManipulationParserConfig());

		return response.variables;
	}

	private static MutableGmmlManipulatorParserConfiguration getManipulationParserConfig() {
		if (manipulationParserConfig == null) {
			manipulationParserConfig = Gmml.manipulatorConfiguration();
		}
		return manipulationParserConfig;
	}

}
