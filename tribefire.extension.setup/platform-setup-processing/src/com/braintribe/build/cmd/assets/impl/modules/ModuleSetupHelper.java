// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionInterval;
import com.braintribe.model.version.VersionIntervals;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class ModuleSetupHelper {

	static final String LIB_FOLDER_NAME = "lib";
	static final String SOLUTIONS_FILE_NAME = "solutions";
	static final String CLASSPATH_FILE_NAME = "classpath";

	static final int GROUP_ID = 0;
	static final int ARTIFACT_ID = 1;
	static final int VERSION = 2;

	/** Parse a condensed name in format groupId:artifactId#version, with groupId and version being optional. */
	public static String[] parseCondensedArtifact(String condensedName) {
		return parseCondensedArtifact(condensedName, "tribefire.synthetic", "1.0");
	}

	private static String[] parseCondensedArtifact(String condensedName, String defaultGroup, String defaultVersion) {
		final char GROUP_DELIMITER = ':';
		final char VERSION_DELIMITER = '#';

		if (StringTools.isEmpty(condensedName))
			return null;

		String[] result = new String[3];

		int pos;

		pos = condensedName.indexOf(GROUP_DELIMITER);
		if (pos < 0) {
			result[GROUP_ID] = defaultGroup;
		} else {
			result[GROUP_ID] = condensedName.substring(0, pos);
			condensedName = condensedName.substring(pos + 1);
		}

		pos = condensedName.indexOf(VERSION_DELIMITER);
		if (pos < 0) {
			result[VERSION] = defaultVersion;
		} else {
			result[VERSION] = extractVersion(condensedName.substring(pos + 1), defaultVersion);
			condensedName = condensedName.substring(0, pos);
		}

		result[ARTIFACT_ID] = condensedName;

		return result;
	}

	private static String extractVersion(String depVersion, String defaultVersion) {
		VersionExpression ve = VersionExpression.parse(depVersion);

		if (ve instanceof VersionIntervals)
			ve = first(((VersionIntervals) ve).getElements());

		if (ve instanceof VersionInterval) {
			VersionInterval vi = (VersionInterval) ve;
			Version v = vi.lowerBound();
			if (v != null)
				return NullSafe.get(v.getMajor(), "1") + "." + NullSafe.get(v.getMinor(), "0");
		}

		return defaultVersion;
	}

	static void replaceFiles(List<File> adds, File targetFolder, List<File> removes) {
		for (File fileToRemove : removes)
			delete(fileToRemove);

		for (File fileToAdd : adds)
			copyToDir(fileToAdd, targetFolder);
	}

	static void delete(File file) {
		FileTools.deleteDirectoryRecursivelyUnchecked(file);
	}

	static void copyToDir(File file, File dir) {
		FileTools.copyToDir(file, dir);
	}

	public static List<File> getAllJarFiles(AnalysisArtifact s) {
		List<File> result = s.getParts().values().stream() //
				.filter(p -> "jar".equals(p.getType())) //
				.map(p -> TfSetupTools.resourceLocation(p.getResource())) //
				.map(File::new) //
				.collect(Collectors.toList());

		if (result.isEmpty())
			throw newJarWasExpectedException(s);

		return result;
	}

	private static GenericModelException newJarWasExpectedException(AnalysisArtifact s) {
		StringBuilder sb = new StringBuilder("No jar part found for solution: " + s.asString() + ". ");

		String packaging = s.getOrigin().getPackaging();
		if (TfSetupTools.isPackagedAsJar(s)) {
			sb.append("Jar is expected based on the packaging information in solutions's pom.xml. ");
			if (packaging == null)
				sb.append("No packaging is specified, which means 'jar' by default.");
			else
				sb.append("Packaging: " + packaging);
		} else
			sb.append("There seems to be some bug, as the solution clearly states it's not packaged as a jar, but: " + packaging);

		return new GenericModelException(sb.toString());
	}

	/** Finds a location of jar with no classifier, i.e. part ":jar" */
	public static String findJarLocation_NoClassifier(AnalysisArtifact s) {
		Part jarPart = s.getParts().get(":jar");
		return jarPart == null ? null : TfSetupTools.resourceLocation(jarPart.getResource());
	}

	/**
	 * Returns the classifier of the jar type of given solution, which is resolved by examining all the solution's
	 * {@link AnalysisArtifact#getDependers() dependers}.
	 */
	public static Stream<String> streamSortedJarClassifiersOf(AnalysisArtifact artifact) {
		return streamJarPartsOf(artifact) //
				.map(Part::getClassifier) //
				.map(s -> s == null ? "" : s) //
				.sorted();
	}

	private static Stream<Part> streamJarPartsOf(AnalysisArtifact artifact) {
		return artifact.getParts().values().stream() //
				.filter(p -> "jar".equals(p.getType()));
	}

}
