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

import java.util.Arrays;
import java.util.List;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.DottedDelimiterVersionPart;
import com.braintribe.model.artifact.version.NumericVersionPart;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionPart;

public class DependencyManagementTools {
	
	public static final List<String> excludedScopes = Arrays.asList("test", "provided");
	
	public static Pair<String, String> unversionedArtifactIdentification(Identification identification) {
		return new Pair<>(identification.getGroupId(), identification.getArtifactId());
	}

	public static String majorMinorIdentification(Identification identification, Version version) {
		StringBuilder builder = new StringBuilder();
		builder.append(identification.getGroupId());
		builder.append(':');
		builder.append(identification.getArtifactId());
		builder.append('#');
		builder.append(normalizeToMajorMinorIfPossible(version));
		
		return builder.toString();

	}
	
	public static String normalizeToMajorMinorIfPossible(Version version) {
		StringBuilder builder = new StringBuilder();
		int numTokens = 0;
		for (VersionPart versionPart : version.getVersionData()) {
			if (versionPart instanceof NumericVersionPart) {
				builder.append(((NumericVersionPart) versionPart).getValue());
				if (++numTokens == 2)
					return builder.toString();
			}
			else if (versionPart instanceof DottedDelimiterVersionPart) {
				builder.append('.');
			}
			else {
				return VersionProcessor.toString(version);
			}
		}
		return null;
	}

	public static String majorMinorIdentification(Dependency dependency) {
		Version version = dependency.getVersionRange().lowerBound();
		return majorMinorIdentification(dependency, version);
	}
	
	public static String majorMinorIdentification(Solution solution) {
		Version version = solution.getVersion();
		return majorMinorIdentification(solution, version);
	}
}
