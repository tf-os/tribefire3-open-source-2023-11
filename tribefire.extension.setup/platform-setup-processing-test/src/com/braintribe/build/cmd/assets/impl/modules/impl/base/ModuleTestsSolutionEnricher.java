// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl.base;

import static com.braintribe.devrock.mc.api.commons.PartIdentifications.jar;
import static com.braintribe.utils.lcd.CollectionTools2.asIdentityMap;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.File;
import java.util.Map;

import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.setup.tools.TfSetupSolutionEnricher;
import com.braintribe.setup.tools.TfSetupTools;

/**
 * Enriches given solution with a jar file (request for any {@link PartIdentification} other than {@link PartIdentifications#jar} would result in an
 * exception).
 * 
 * The actual file is chosen based on the solution name.
 * 
 * @author peter.gazdik
 */
public class ModuleTestsSolutionEnricher implements TfSetupSolutionEnricher {

	private static final File jarsFolder = new File("res//ModuleSetup");

	public static final File LIB_JAR = new File(jarsFolder, "lib.jar");
	public static final File API_JAR = new File(jarsFolder, "api-manifest.jar");
	public static final File MODEL_MANIFEST_JAR = new File(jarsFolder, "model-manifest.jar");
	public static final File MODEL_XML_JAR = new File(jarsFolder, "model-xml.jar");

	public static final Resource R_LIB_JAR = asResource(LIB_JAR);
	public static final Resource R_API_JAR = asResource(API_JAR);
	public static final Resource R_MODEL_MANIFEST_JAR = asResource(MODEL_MANIFEST_JAR);
	public static final Resource R_MODEL_XML_JAR = asResource(MODEL_XML_JAR);

	private static final Map<File, Resource> fileToResource = asIdentityMap(//
			LIB_JAR, R_LIB_JAR, //
			API_JAR, R_API_JAR, //
			MODEL_MANIFEST_JAR, R_MODEL_MANIFEST_JAR, //
			MODEL_XML_JAR, R_MODEL_XML_JAR //
	);

	private static Resource asResource(File file) {
		FileResource result = FileResource.T.create();
		result.setPath(file.getAbsolutePath());
		return result;
	}

	private static final Map<File, Part> partsByJarFile = asMap( //
			LIB_JAR, createJarPart(LIB_JAR), //
			API_JAR, createJarPart(API_JAR), //
			MODEL_MANIFEST_JAR, createJarPart(MODEL_MANIFEST_JAR), //
			MODEL_XML_JAR, createJarPart(MODEL_XML_JAR) //
	);

	public static Resource jarResource(Artifact artifact) {
		return fileToResource.get(jarFile(artifact));
	}

	public static File jarFile(Artifact artifact) {
		return determineJarFile(artifact.getArtifactId());
	}

	@Override
	public void enrich(AnalysisArtifact solution, PartIdentification type) {
		checkOnlyAsksForJars(type);

		if (!isEnriched(solution, jar) && hasJarType(solution)) {
			Part p = resolveJarPart(solution);
			solution.getParts().put(p.asString(), p);
		}
	}

	private boolean hasJarType(AnalysisArtifact solution) {
		String packaging = solution.getOrigin().getPackaging();
		return packaging == null || "jar".equalsIgnoreCase(packaging);
	}

	private void checkOnlyAsksForJars(PartIdentification type) {
		if (type != jar)
			throw new IllegalArgumentException("Only jar type enriching is supported in module test. Requested: " + type);
	}

	private Part resolveJarPart(AnalysisArtifact solution) {
		File jarFile = determineJarFile(solution);
		return partsByJarFile.get(jarFile);
	}

	private static int i = 0;

	private File determineJarFile(AnalysisArtifact solution) {
		return determineJarFile(solution.getArtifactId());
	}

	private static File determineJarFile(String artifactId) {
		if (artifactId.endsWith("model"))
			if (++i % 2 == 0)
				return MODEL_MANIFEST_JAR;
			else
				return MODEL_XML_JAR;

		else if (artifactId.endsWith("api"))
			return API_JAR;

		else
			return LIB_JAR;
	}

	private static Part createJarPart(File jarFile) {
		FileResource fileResource = FileResource.T.create();
		fileResource.setName(jarFile.getName());
		fileResource.setFileSize(jarFile.length());
		fileResource.setPath(jarFile.getAbsolutePath());

		Part result = createPart(jar);
		result.setResource(fileResource);

		return result;
	}

	private static Part createPart(PartIdentification type) {
		Part part = Part.T.create();
		part.setClassifier(type.getClassifier());
		part.setType(part.getType());

		return part;
	}

	private boolean isEnriched(AnalysisArtifact solution, PartIdentification type) {
		return TfSetupTools.findPart(solution, type).isPresent();
	}

}
