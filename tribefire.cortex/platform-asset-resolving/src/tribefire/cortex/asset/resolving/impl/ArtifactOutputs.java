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

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.lcd.StringTools;

public abstract class ArtifactOutputs {

	public static ConsoleOutputContainer solution(Solution solution) {
		String groupId = solution.getGroupId();
		String artifactId = solution.getArtifactId();
		String version = VersionProcessor.toString(solution.getVersion());

		return solution(groupId, artifactId, version);
	}

	/** {@code groupId} and {@code version} are both optional. */
	public static ConfigurableConsoleOutputContainer solution(String groupId, String artifactId, String version) {
		ConfigurableConsoleOutputContainer configurableSequence = ConsoleOutputs.configurableSequence();

		if (groupId != null)
			configurableSequence.append(brightBlack(groupId + ":"));

		configurableSequence.append(text(artifactId));

		if (!StringTools.isEmpty(version))
			configurableSequence //
					.append(brightBlack("#")) //
					.append(isSnapshot(version) ? yellow(version) : green(version));

		return configurableSequence;
	}

	private static boolean isSnapshot(String version) {
		return version.endsWith("-pc") || version.endsWith("-SNAPSHOT");
	}

}
