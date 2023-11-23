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
package tribefire.cortex.asset.resolving.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.VirtualPart;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

import tribefire.cortex.asset.resolving.api.PlatformAssetResolvingConstants;

public class PlatformAssetNatureLoading implements PlatformAssetResolvingConstants  {
	private static PartTuple assetPartTuple = PartTupleProcessor.fromString(PART_IDENTIFIER_ASSET_MAN);
	private static MutableGmmlManipulatorParserConfiguration manipulationParserConfig;
	
	private final Map<String, PlatformAssetNature> inheritedNatures = new HashMap<>();
	
	public PlatformAssetNature findNature(Solution solution) {
		String majMinId = DependencyManagementTools.majorMinorIdentification(solution);
		PlatformAssetNature nature = inheritedNatures.get(majMinId);
		
		if (nature != null)
			return nature;
		
		nature = findEnrichedNature(solution);
		
		if (nature != null)
			return nature;
		
		
		return findPartNature(solution);
	}
	
	public void assignNature(Dependency dependency, PlatformAssetNature nature) {
		String majMinId = DependencyManagementTools.majorMinorIdentification(dependency);
		inheritedNatures.put(majMinId, nature);
	}
	
	private PlatformAssetNature findEnrichedNature(Solution solution) {
		PlatformAssetNature nature = solution.getRequestors().stream().map(this::findNature).filter(n -> n != null).findFirst().orElse(null);
		return nature;
	}
	
	/**
	 * scans for either directly enriched {@link PlatformAssetNature} or indirectly via {@link VirtualPart} enriched
	 * @param hasMetaData - the MetaData container as declared by {@link HasMetaData}
	 * @return - the first {@link PlatformAssetNature} found 
	 */
	private PlatformAssetNature findNature(HasMetaData hasMetaData) {
		// look for directly enriched PlaformAssetNatures 
		PlatformAssetNature directlyEnrichedNature =  (PlatformAssetNature) hasMetaData.getMetaData().stream().filter(m -> m instanceof PlatformAssetNature).findFirst().orElse(null);
		if (directlyEnrichedNature != null) {
			return directlyEnrichedNature;
		}
		
		// if none are found, look for VirtualPart attached 
		PlatformAssetNature virtualPartNature = hasMetaData.getMetaData().stream()
				.map(m -> {
					if (m instanceof VirtualPart) {
						VirtualPart part = (VirtualPart)m;
						if (PartTupleProcessor.equals(part.getType(), assetPartTuple))
							return part;
					}
					return null;
				})
				.filter(p -> p != null)
				.map(this::loadNatureFromVirtualPart)
				.findFirst().orElse( null);
		
		return virtualPartNature;
	}

	
	private PlatformAssetNature findPartNature(Solution solution) {
		for (Part part: solution.getParts()) {
			PartTuple type = part.getType();
			if (PartTupleProcessor.equals(type, assetPartTuple)) {
				return loadNatureFromPart(new File(part.getLocation()));
			}
		}
		return null;
	}

	private PlatformAssetNature loadNatureFromPart(File file) {
		try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			return loadNatureFromReader(reader); 
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}
	
	/**
	 * load the {@link PlatformAssetNature} from a {@link VirtualPart}
	 * @param virtualPart - the {@link VirtualPart}
	 * @return - the {@link PlatformAssetNature} contained
	 */
	private PlatformAssetNature loadNatureFromVirtualPart( VirtualPart virtualPart) {
		String payload = virtualPart.getPayload();
		try (Reader reader = new StringReader(payload)) {
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
