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
package com.braintribe.tribefire.jinni.support;

import static com.braintribe.console.ConsoleOutputs.brightYellow;
import static com.braintribe.console.ConsoleOutputs.println;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.utils.FileTools;

public class PackagedSolution {

	private static final String FILENAME_PACKAGED_SOLUTIONS = "packaged-solutions.txt";

	public String groupId;
	public String artifactId;
	public String version;

	public PackagedSolution(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public static Set<PackagedSolution> readSolutionsFrom(File installationDir) {

		if (installationDir == null) {
			println(brightYellow("Missing information about packaged libraries"));
			return null;
		}

		File file = new File(installationDir, FILENAME_PACKAGED_SOLUTIONS);

		if (!file.exists()) {
			println(brightYellow("Missing information about packaged libraries"));
			return null;
		}

		String text = FileTools.read(file).asString();

		Pattern pattern = Pattern.compile("(.*):(.*)#(.*)");

		String solutions[] = text.split("\\n");

		Comparator<PackagedSolution> comparator = Comparator //
				.comparing((PackagedSolution s) -> s.groupId) //
				.thenComparing((PackagedSolution s) -> s.artifactId);

		Set<PackagedSolution> packagedSolutions = new TreeSet<>(comparator);

		for (String solution : solutions) {
			Matcher matcher = pattern.matcher(solution);
			if (matcher.matches()) {
				PackagedSolution packagedSolution = new PackagedSolution(matcher.group(1), matcher.group(2), matcher.group(3));
				packagedSolutions.add(packagedSolution);
			}
		}

		return packagedSolutions;
	}

}