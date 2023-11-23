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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.runner.commons.ClasspathResolvingUtil;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.zarathud.model.data.Artifact;

public class ResolvingCoreContextBuilder implements Supplier<CoreContext> {
	private static Logger log = Logger.getLogger(ResolvingCoreContextBuilder.class);	
	private ResolvingRunnerContext context;
	
	
	public ResolvingCoreContextBuilder(ResolvingRunnerContext r) {
		this.context = r;
	}

	@Override
	public CoreContext get() {
		
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build().enrichJar(true).done();
		
		Map<String,String> overrides = new HashMap<>();
		
		// additional overrides 
		overrides.putAll( context.getEnvironmentOverrides());
				
		Maybe<AnalysisArtifactResolution> resolutionMaybe = ClasspathResolvingUtil.runAsDependency( context.getTerminal(), resolutionContext, overrides);
		
		if (resolutionMaybe.isUnsatisfied()) {
			log.info("cannot run classpath resolution on artifact [" + context.getTerminal() + "] as " + resolutionMaybe.whyUnsatisfied().stringify());
			return null;
		}
		
		AnalysisArtifactResolution resolution = resolutionMaybe.get();
		// TODO: error handling here 
		if (resolution.hasFailed()) {
			System.out.println("honk");
		}
		if (resolution.getSolutions().size() == 0) {
			log.info("no dependencies found for terminal:" + context.getTerminal());			
			
		}
		
		AnalysisTerminal analysisTerminal = resolution.getTerminals().get(0);	
		if (analysisTerminal instanceof AnalysisDependency) {
			AnalysisDependency ad = (AnalysisDependency) analysisTerminal;
			if (ad.getFailure() != null) {
				log.info("cannot run classpath resolution on artifact [" + context.getTerminal() + "] as " + ad.getFailure().stringify());
				return null;
			}
		}
		AnalysisArtifact analysisArtifact = resolution.getSolutions().stream().filter( s -> s.compareTo(analysisTerminal) == 0).findFirst().orElse(null);
		String packaging = analysisArtifact.getPackaging();
		
		if (packaging == null) {
			packaging = "jar";
		}
						
		if (!packaging.equalsIgnoreCase( "JAR") && !packaging.equalsIgnoreCase("BUNDLE")) {
			log.info("artifact [" + context.getTerminal() + "] doesn't contain a jar (by its declaration : [" + packaging + "]) and will not be processed");
			return null;
		}
		
						
		CoreContext coreContext = new CoreContext();
		coreContext.setClasspath( resolution.getSolutions());
		List<CompiledArtifact> cas = resolution.getSolutions().stream().map( aa -> aa.getOrigin()).collect( Collectors.toList());
		coreContext.setCompiledSolutionsOfClasspath(  cas);
		coreContext.setDependencies( analysisArtifact.getDependencies());
		coreContext.setCustomRatingsResource( context.getCustomRatingsResource());
		coreContext.setPullRequestRatingsResource(context.getPullRequestRatingsResource());
		
		Artifact artifact = toArtifact( analysisArtifact);
		coreContext.setTerminalArtifact( artifact);
		
		coreContext.setRespectBraintribeSpecifica(context.getRespectBraintribeSpecifica());
		ConsoleOutputVerbosity consoleOutputVerbosity = context.getConsoleOutputVerbosity();
		if (consoleOutputVerbosity != null) {
			coreContext.setConsoleOutputVerbosity(com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity.valueOf( consoleOutputVerbosity.toString()));
		}		
		
		return coreContext;
	}
	
		
	/**
	 * @param solution
	 * @return
	 */
	public static Artifact toArtifact( AnalysisArtifact solution) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( solution.getVersion());		
		return artifact;
	}
	
	

}