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
package com.braintribe.devrock.zarathud.test.extraction.classes;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.commons.ClasspathResolvingUtil;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * @author pit
 *
 */
@Category(KnownIssue.class)
public abstract class AbstractClassesRunnerLab {
	private static Logger log = Logger.getLogger(AbstractClassesRunnerLab.class);	
	
	public Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> test(File classesFolder, String terminal) {
		
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminal);
		
		Maybe<CompiledArtifactIdentification> caiMaybe = ClasspathResolvingUtil.resolve(cdi, null);
		
		if ( caiMaybe.isUnsatisfied()) {
			String msg = "cannot resolve [" + terminal + "]: " + caiMaybe.whyUnsatisfied().stringify();
			log.error( msg);
			Assert.fail( msg);
		}
		
		CompiledArtifactIdentification cai = caiMaybe.get();
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
		
		Maybe<AnalysisArtifactResolution> maybe = ClasspathResolvingUtil.runAsArtifact(cai.asString(), ClasspathResolutionContext.build().done(), null);
		if (maybe.isUnsatisfied()) {
			Assert.fail("resolution failed as " + maybe.whyUnsatisfied().stringify());
			return null;
		}
		
		AnalysisArtifactResolution resolution = maybe.get();
		ClassesProcessingRunnerContext cprc = ClassesProcessingRunnerContext.T.create();
		
		cprc.setTerminalClassesDirectoryNames( Collections.singletonList(classesFolder.getAbsolutePath()));
		List<AnalysisArtifact> solutions = resolution.getSolutions();
		cprc.setClasspath( solutions);
		cprc.setTerminal( cai.asString());			
		
		AnalysisArtifact analysisTerminal = (AnalysisArtifact) resolution.getTerminals().get(0);		
		cprc.setDependencies( analysisTerminal.getDependencies());

		cprc.setConsoleOutputVerbosity( com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity.verbose);
		
		ZedWireRunner zedWireRunner = wireContext.contract().classesRunner(cprc);
		
		return zedWireRunner.run();
	}

	public Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> test(File classesFolder, Map<String, AnalysisArtifact> map, String terminal) {
		
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminal);
		
		Maybe<CompiledArtifactIdentification> caiMaybe = ClasspathResolvingUtil.resolve(cdi, null);
		
		if ( caiMaybe.isUnsatisfied()) {
			String msg = "cannot resolve [" + terminal + "]: " + caiMaybe.whyUnsatisfied().stringify();
			log.error( msg);
			Assert.fail( msg);
		}
		
		CompiledArtifactIdentification cai = caiMaybe.get();
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
		
		Maybe<AnalysisArtifactResolution> maybe = ClasspathResolvingUtil.runAsArtifact(cai.asString(), ClasspathResolutionContext.build().done(), null);
		if (maybe.isUnsatisfied()) {
			Assert.fail("resolution failed as " + maybe.whyUnsatisfied().stringify());
			return null;
		}
		
		AnalysisArtifactResolution resolution = maybe.get();
		ClassesProcessingRunnerContext cprc = ClassesProcessingRunnerContext.T.create();
		cprc.setTerminalClassesDirectoryNames(Collections.singletonList( classesFolder.getAbsolutePath()));
		
		Set<String> nonPackaged = new HashSet<>();
		for (AnalysisArtifact aa : map.values()) {
			nonPackaged.add( aa.asString());
		}
		
		// prune the list of all solutions in the CP that have been replaced with the folders 
		List<AnalysisArtifact> solutions = resolution.getSolutions();
		List<AnalysisArtifact> packagedSolutions = solutions.stream().filter( s -> !nonPackaged.contains( s.asString())).collect(Collectors.toList());
				
		cprc.setClasspath( packagedSolutions);
		cprc.setTerminal( cai.asString());			
		cprc.getNonpackedSolutionsOfClasspath().putAll(map);
		
		AnalysisArtifact analysisTerminal = (AnalysisArtifact) resolution.getTerminals().get(0);		
		cprc.setDependencies( analysisTerminal.getDependencies());

		cprc.setConsoleOutputVerbosity( com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity.verbose);
		
		ZedWireRunner zedWireRunner = wireContext.contract().classesRunner(cprc);
		
		return zedWireRunner.run();
	}
	
}
