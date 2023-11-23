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
package com.braintribe.setup.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.build.cmd.assets.wire.artifact.ArtifactResolutionWireModule;
import com.braintribe.build.cmd.assets.wire.artifact.contract.ArtifactResolutionContract;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.CoreComponent;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.ZipTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * Experimental!!! -
 *
 * <h3>Naming</h3>
 * 
 * This is one of few TfSetup helper classes, whose idea is to provide an API for the most common tasks associated with platform-setup-processing. All
 * the public members with static methods (should) have "TfSetup" as their name prefix, and no other class should have that prefix. This way, one can
 * easily find all the available helpers by looking for types matching this pattern.
 * 
 * <h3>(Temporary) Disclaimer</h3>
 * 
 * DO NOT USE OUTSIDE OF THIS ARTIFACT YET SO ALL THESE CAN BE CHANGED WITHOUT RISKING BAD STUFF
 * 
 * This class offers an access to the most common utility tools/methods used in the scope of setup processing.
 * 
 * @author peter.gazdik
 */
public interface TfSetupTools {

	//
	// KEEP THE METHODS SHORT. LONG ONES SHOULD BE IMPLEMENTED ELSEWHERE AND DELEGATED TO.
	//

	// ####################################################
	// ## . . . . . . . . . Malaclypse . . . . . . . . . ##
	// ####################################################

	static void withNewArtifactResolutionContext(RepositoryConfigurationLocator repoConfigLocator, VirtualEnvironment env, Consumer<ArtifactResolutionContext> closure) {
		try (WireContext<ArtifactResolutionContract> wc = Wire.context(new ArtifactResolutionWireModule(repoConfigLocator, env, false))) {
			closure.accept(wc.contract());
		}
	}
	
	static void withNewArtifactResolutionContext(VirtualEnvironment env, Consumer<ArtifactResolutionContext> closure) {
		try (WireContext<ArtifactResolutionContract> wc = Wire.context(new ArtifactResolutionWireModule(env, false))) {
			closure.accept(wc.contract());
		}
	}

	static CompiledTerminal platformAssetToTerminal(PlatformAsset asset) {
		return CompiledTerminal.create(asset.getGroupId(), asset.getName(), asset.versionWithRevision());
	}

	// ####################################################
	// ## . . . . . . . . . Solutions . . . . . . . . . .##
	// ####################################################

	static String artifactName(String groupId, String artifactId, String version) {
		return groupId + ":" + artifactId + "#" + version;
	}

	static Set<AnalysisArtifact> analysisArtifactSet() {
		return CodingSet.create(HashComparators.versionedArtifactIdentification, LinkedHashSet::new);
	}

	static <V> Map<AnalysisArtifact, V> analysisArtifactMap() {
		return CodingMap.create(HashComparators.versionedArtifactIdentification, LinkedHashMap::new);
	}

	static Optional<File> findPartFile(AnalysisArtifact solution, PartIdentification type) {
		return findPartLocation(solution, type) //
				.map(File::new); //
	}

	/**
	 * @return file for the {@link #getPartLocation(AnalysisArtifact, PartIdentification) location} of given {@link AnalysisArtifact#getParts()
	 *         solution's part} of given {@link PartIdentification type}
	 * 
	 * @throws GenericModelException
	 *             if no part is found or the part has no location
	 * 
	 * @see #findPart(AnalysisArtifact, PartIdentification)
	 */
	static File getPartFile(AnalysisArtifact solution, PartIdentification type) {
		return new File(getPartLocation(solution, type));
	}

	/**
	 * @return {@link Part#getResource() file location} of given {@link AnalysisArtifact#getParts() artifact's part} of given
	 *         {@link PartIdentification part}
	 * 
	 * @throws GenericModelException
	 *             if no part is found or the part has no location
	 * 
	 * @see #findPart(AnalysisArtifact, PartIdentification)
	 */
	static String getPartLocation(AnalysisArtifact solution, PartIdentification type) {
		return findPartLocation(solution, type) //
				.orElseThrow(() -> new GenericModelException("Part not found for solution: " + solution.asString() + ". Requested type: " + type));
	}

	static Optional<String> findPartLocation(AnalysisArtifact solution, PartIdentification type) {
		return findPart(solution, type) //
				.map(TfSetupTools::partLocation);
	}

	/**
	 * @return solution {@link Part} of given {@link PartIdentification type}
	 * 
	 * @see PartIdentifications
	 * @see PartIdentification#parse(String)
	 */
	static Optional<Part> findPart(AnalysisArtifact solution, PartIdentification type) {
		return Optional.ofNullable(solution.getParts().get(type.asString()));
	}

	static boolean isPackagedAsJar(AnalysisArtifact solution) {
		String p = solution.getOrigin().getPackaging();
		return p == null || p.equalsIgnoreCase("jar") || p.equalsIgnoreCase("bundle");
	}

	// ####################################################
	// ## . . . . . . String representations . . . . . . ##
	// ####################################################

	static String natureSensitiveAssetName(PlatformAsset a) {
		return a.getNature() instanceof CoreComponent ? a.getName() : a.getGroupId() + "." + a.getName();
	}

	static String escapePropertyValue(String s) {
		return s.replace("\\", "\\\\").replace("\n", "\\n");
	}

	// ###########################################################
	// ## . . . . . . . . . Asset Processing . . . . . . . . . .##
	// ###########################################################

	static Stream<EntityType<?>> getNatureTypes(EntityType<?> entityType) {
		Iterable<EntityType<?>> allSuperTypes = entityType.getTransitiveSuperTypes(true, true);

		return StreamSupport.stream(allSuperTypes.spliterator(), false) //
				.filter(PlatformAssetNature.T::isAssignableFrom);
	}

	// ###########################################################
	// ## . . . . . . . . . Writing files . . . . . . . . . . . ##
	// ###########################################################

	static final GmSerializationOptions DEFAULT_YML_OPTIONS = GmSerializationOptions.deriveDefaults() //
			.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
			.build();

	static void writeYml(Object o, Writer writer) {
		new YamlMarshaller().marshall(writer, o, DEFAULT_YML_OPTIONS);
	}

	static void unzipResource(Resource resource, File folder) {
		try (InputStream is = resource.openStream()) {
			ZipTools.unzip(is, folder);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while unzipping " + resourceLocation(resource) + " into folder: " + folder.getAbsolutePath());
		}
	}

	static String resourceDesc(Resource resource) {
		if (resource instanceof FileResource)
			return "resource for file '" + ((FileResource) resource).getPath() + "'";
		else
			return resource.toString();
	}

	static String partLocation(Part part) {
		return resourceLocation(part.getResource());
	}

	static String resourceLocation(Resource r) {
		if (r instanceof FileResource)
			return ((FileResource) r).getPath();
		else
			throw new IllegalStateException("Cannot resolve file path, resource is not a FileResource, but: " + r);
	}

}
