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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.resource.Resource;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingConstants;

public class PlatformAssetNatureLoading implements PlatformAssetResolvingConstants  {
	private static PartIdentification assetPartTuple = PartIdentification.parse(PART_IDENTIFIER_ASSET_MAN);
	private static MutableGmmlManipulatorParserConfiguration manipulationParserConfig;
	
	public PlatformAssetNature findNature(AnalysisArtifact solution) {
		Part part = solution.getParts().get(assetPartTuple.asString());
		
		if (part == null)
			return null;
		
		return loadNatureFromResource(part.getResource());
	}

	public PlatformAssetNature loadNatureFromResource(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.openStream(), "UTF-8")) {
			return loadNatureFromReader(reader); 
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}
	
	private PlatformAssetNature loadNatureFromReader(Reader reader) {
		BasicManagedGmSession entityManager = new BasicManagedGmSession();
		ParseResponse response = null;
		response = ManipulatorParser.parse( reader, entityManager, getManipulationParserConfig());
		
		Object natureCandidate = response.variables.get("$nature");
		// if $nature isn't set, the we use $natureType
		if (natureCandidate == null) {
			Object natureTypeCandidate = response.variables.get("$natureType");
			// $natureType must be set, so complain otherwise 
			if (natureTypeCandidate == null) {
				throw new IllegalStateException("neither $nature nor $natureType was given but one is at least required");	
			}
			// must be an entity type (actually a MetaData)
			if (natureTypeCandidate instanceof EntityType) {
				@SuppressWarnings("rawtypes")
				EntityType entityType = (EntityType) natureTypeCandidate;
				natureCandidate = entityType.create();
				
			}
			else {
				throw new IllegalStateException("processing the virtual part yielded an unexpected value for the $natureType variable: " + natureTypeCandidate);		
			}
		}
		// nothing was retrieved 
		if (!(natureCandidate instanceof PlatformAssetNature)) {
			throw new IllegalStateException("processing the virtual part yielded an unexpected value for the nature: " + natureCandidate);	
		}

		return (PlatformAssetNature) natureCandidate;
	}

	private static MutableGmmlManipulatorParserConfiguration getManipulationParserConfig() {
		if (manipulationParserConfig == null) {
			manipulationParserConfig = Gmml.manipulatorConfiguration();
		}
		return manipulationParserConfig;
	}

}
