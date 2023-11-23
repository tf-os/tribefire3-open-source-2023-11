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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.io.File;
import java.util.Set;

public class VersionSupplier {

	public static String jinniVersion(File installationDir) {
		return versionOf("tribefire.extension.setup", "jinni", installationDir);
	}

	/** Returns a version of some artifact in the group. This makes sense if the caller only cares for major.minor. */
	public static String groupVersion(String groupId, File installationDir) {
		return packagedSolutions(installationDir).stream() //
				.filter(s -> s.groupId.equals(groupId)) //
				.map(s -> s.version) //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException(
						"Cannot determine version for group " + groupId + ", as no artifact from that group is used in Jinni itself."));
	}

	private static String versionOf(String groupId, String artifactId, File installationDir) {
		return packagedSolutions(installationDir).stream() //
				.filter(s -> s.groupId.equals(groupId) && s.artifactId.equals(artifactId)) //
				.map(s -> s.version) //
				.findFirst() //
				.orElse("unknown");
	}

	private static Set<PackagedSolution> packagedSolutions(File installationDir) {
		return nullSafe(PackagedSolution.readSolutionsFrom(installationDir));
	}

}
