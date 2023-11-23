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

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.utils.lcd.StringTools;

import tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs;

/**
 * Utility class for managing console output, to make printing output easier, and more consistent from style perspective.
 * 
 * <h3>Naming conventions</h3>
 * 
 * Methods that return a {@link ConsoleOutput} should simply be called by what they are printing, e.g. {@link #property(String, String)}.
 * <p>
 * Methods that print directly should start with "out", e.g. {@link #outProperty}.
 * 
 * 
 * @see TfSetupTools
 * 
 * @author peter.gazdik
 */
public abstract class TfSetupOutputs extends ArtifactOutputs {

	public static void outProperty(String name, String value) {
		println(property(name, value));
	}

	public static ConsoleOutputContainer property(String name, String value) {
		return sequence(yellow(name + ": "), text(value));
	}

	public static ConsoleOutput version(String version) {
		return ArtifactOutputs.version(version);
	}

	public static ConsoleOutput warning(String text) {
		return yellow(text);
	}

	public static ConsoleOutput fileName(String fileName) {
		return ArtifactOutputs.fileName(fileName);
	}

	// temp
	
	@Deprecated
	public static ConfigurableConsoleOutputContainer solutionOutput(String groupId, String artifactId, String version) {
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
