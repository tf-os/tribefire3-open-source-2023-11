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
package com.braintribe.template.processing.helper;

import static com.braintribe.console.ConsoleOutputs.println;

import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputFiles;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

public final class ConsoleOutputHelper extends ConsoleOutputFiles {

	public static void outTemplateResolvingResult(CompiledArtifactIdentification resolvingResult) {
		println(templateNameOutput(resolvingResult, 1));
	}

	public static ConsoleOutput templateNameOutput(CompiledArtifactIdentification part, int indentCount) {
		return artifactNameOutput(part.getGroupId(), part.getArtifactId(), part.getVersion().asString(), indentCount);
	}

	public static ConsoleOutput templateNameOutput(String artifactName, int indentCount) {
		String[] artifactNameParts = artifactName.split(":|#");
		return artifactNameOutput(artifactNameParts[0], artifactNameParts[1], artifactNameParts[2], indentCount);
	}

}
