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
package com.braintribe.devrock.zarathud.runner.impl.builders;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.zarathud.model.data.Artifact;

public class ClassesCoreContextBuilder implements Supplier<CoreContext> {
	
	private ClassesProcessingRunnerContext context;

	public ClassesCoreContextBuilder(ClassesProcessingRunnerContext context) {
		this.context = context;		
	}

	@Override
	public CoreContext get() {
		CoreContext coreContext = new CoreContext();		
		coreContext.setClasspath( context.getClasspath());
		coreContext.setClassesDirectories( context.getTerminalClassesDirectoryNames());
		coreContext.setFoldersForNonPackagedClasses( context.getNonpackedSolutionsOfClasspath());
		coreContext.setDependencies( context.getDependencies());
		coreContext.setCustomRatingsResource( context.getCustomRatingsResource());
		coreContext.setPullRequestRatingsResource(context.getPullRequestRatingsResource());
		
		String terminal = context.getTerminal();			
		Artifact artifact = Artifact.parse(terminal);			
		coreContext.setTerminalArtifact( artifact);
		
		List<CompiledArtifact> cas = context.getCompiledSolutionsOfClasspath().stream().map( aa -> aa.getOrigin()).collect( Collectors.toList());
		coreContext.setCompiledSolutionsOfClasspath(  cas);
		coreContext.setDependencies( context.getDependencies());
				
		ConsoleOutputVerbosity consoleOutputVerbosity = context.getConsoleOutputVerbosity();
		if (consoleOutputVerbosity != null) {
			coreContext.setConsoleOutputVerbosity(com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity.valueOf( consoleOutputVerbosity.toString()));
		}		
		
		return coreContext;
	}

}
