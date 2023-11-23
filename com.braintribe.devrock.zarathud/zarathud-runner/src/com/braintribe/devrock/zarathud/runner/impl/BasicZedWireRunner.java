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
package com.braintribe.devrock.zarathud.runner.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.devrock.zarathud.model.reasons.NullContextPassed;
import com.braintribe.devrock.zarathud.model.reasons.UrlNotFound;
import com.braintribe.devrock.zarathud.model.reasons.ZedAnalysisIssue;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.api.cfg.ValidImplicitArtifactReferenceFilter;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.devrock.zarathud.wirings.main.ZedWireTerminalModule;
import com.braintribe.devrock.zarathud.wirings.main.contract.ZedMainContract;
import com.braintribe.devrock.zed.api.ZedCore;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ModelForensics;
import com.braintribe.devrock.zed.api.output.ConsoleContainerOut;
import com.braintribe.devrock.zed.context.BasicConsoleOutputContext;
import com.braintribe.devrock.zed.context.BasicZedAnalyzerContext;
import com.braintribe.devrock.zed.forensics.BasicClasspathForensics;
import com.braintribe.devrock.zed.forensics.BasicDependencyForensics;
import com.braintribe.devrock.zed.forensics.BasicModelForensics;
import com.braintribe.devrock.zed.forensics.BasicModuleForensics;
import com.braintribe.devrock.zed.forensics.fingerprint.register.FingerPrintRegistry;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.devrock.zed.forensics.structure.DependencyStructureRegistry;
import com.braintribe.devrock.zed.output.BasicConsoleContainerOut;
import com.braintribe.devrock.zed.output.BasicConsoleContainerOutputCommons;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasGenericNature;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;

/**
 * basic implementation of the {@link ZedWireRunner}
 * @author pit
 *
 */
public class BasicZedWireRunner implements ZedWireRunner{
	private CoreContext context;
	private Artifact analysedArtifact;
	private DependencyForensicsResult dependencyForensicsResult;
	private ClasspathForensicsResult classpathForensicsResult;
	private ModelForensicsResult modelForensicsResult;
	private ModuleForensicsResult moduleForensicsResult;
	private RatingRegistry ratings;
	private ZedAnalyzerContext analyzerContext;
	private Reason umbrella;
	Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> collectedForensics;
	
		
	@Override
	public Artifact analyzedArtifact() {
		return analysedArtifact;
	}
	@Override
	public DependencyForensicsResult dependencyForensicsResult() {
		return dependencyForensicsResult;
	}
	@Override
	public ClasspathForensicsResult classpathForensicsResult() {
		return classpathForensicsResult;
	}
	@Override
	public ModelForensicsResult modelForensicsResult() {
		return modelForensicsResult;
	}
	
	@Override
	public ModuleForensicsResult moduleForensicsResult() {	
		return moduleForensicsResult;
	}
	
	@Override
	public RatingRegistry ratingRegistry() {	
		return ratings;
	}
	
	
	@Override
	public ZedAnalyzerContext analyzerContext() {
		return analyzerContext;		
	}
	/**
	 * @param s - a {@link Supplier} that supplies the {@link CoreContext}
	 */
	public BasicZedWireRunner(Supplier<CoreContext> s) {	
		context = s.get();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.runner.api.ZedWireRunner#run()
	 */
	public Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> run() {		
		WireContext<ZedMainContract> wireContext = Wire.context( ZedWireTerminalModule.INSTANCE);
		
		if (context == null) {
			return Reasons.build(NullContextPassed.T).toMaybe();
		}
		// 
		// extraction 
		//
		ZedCore core = wireContext.contract().coreContract().core(context);		
	
		Artifact terminalArtifact = context.getTerminalArtifact();
		analysedArtifact = core.analyseJar( terminalArtifact);		
		analyzerContext = core.getAnalyzingContext();
		
		 
		
		List<Reason> rawAnalysisErrorReasons = analyzerContext.registry().collectedAsmAnalysisErrorReasons();
		List<Reason> analysisErrorReasons = coalesceAnalysisErrorReasons( rawAnalysisErrorReasons);
		if (analysisErrorReasons != null && analysisErrorReasons.size() > 0) {
			

			Reason umbrella = acquireUmbrellaReason();
			umbrella.getReasons().addAll(analysisErrorReasons);
		}
		List<Reason> collectedAsmAnalysisErrorReasons = analyzerContext.registry().collectedAsmAnalysisErrorReasons();
		
		// can't continue
		if (analysedArtifact == null) {			
			return Maybe.empty( acquireUmbrellaReason());
		}
		
		// check for collected error reasons:
		
		// filter 
		List<UrlNotFound> uniqueNotFoundReasons = new ArrayList<>(collectedAsmAnalysisErrorReasons.size());
		List<Reason> unclassifiedReasons = new ArrayList<>(collectedAsmAnalysisErrorReasons.size());
		Set<String> visited = new HashSet<>();
		
		for (Reason reason : collectedAsmAnalysisErrorReasons) {
			if (reason instanceof UrlNotFound) {
				UrlNotFound urlNotFoundReason = (UrlNotFound) reason;
				String path = urlNotFoundReason.getCombinedUrl();
				if (!visited.add( path))
					continue;
				
				uniqueNotFoundReasons.add(urlNotFoundReason);					
			}
			else {
				unclassifiedReasons.add(reason);
			}
		}
		
		
		Set<ZedEntity> entries = terminalArtifact.getEntries();		
		ZedAnalyzerContext analyzingContext = analyzerContext;
				
		// determine if there are any generic entities (at least one : short circuit) in the artifact
		boolean containsGenericEntities = entries.stream().filter( z -> {
			if (z instanceof HasGenericNature) {
				if ( Boolean.TRUE.equals(((HasGenericNature) z).getGenericNature()))
						return true;
			}
			return false;
		}).findAny().isPresent();
	
		// activate dependency filter if model / braintribe specifica
		if (containsGenericEntities || context.getRespectBraintribeSpecifica()) {
			((BasicZedAnalyzerContext) analyzingContext).setValidImplicitArtifactReferencesFilter( new ValidImplicitArtifactReferenceFilter());
		}
		
		
		
		//
		// forensics
		//
		//
		
		// for now, the return instance BasicZedAnalyzerContext also implements the ZedForensicsContext interface, so .. 
		ZedForensicsContext forensicsContext = (ZedForensicsContext) analyzingContext;
		
		// initialization
		// build up the structural registry to get all tags used throughout the dependency declarations within the poms of all artifacts in the CP
		DependencyStructureRegistry structuralRegistry = DependencyStructureRegistry.buildRegistry( context.getCompiledSolutionsOfClasspath());
		((BasicZedAnalyzerContext) analyzingContext).setStructuralRegistry(structuralRegistry);
						
		// build up the rating registry 		
		Map<FingerPrint, ForensicsRating> ratingsFromSuppressions = core.collectSuppressAnnotations(analysedArtifact);
		ratings = wireContext.contract().forensicsContract().ratingRegistry(ratingsFromSuppressions, context.getCustomRatingsResource(), context.getPullRequestRatingsResource());			
		FingerPrintRegistry fingerPrintRegistry = wireContext.contract().forensicsContract().fingerPrintRegistry();
		
		
		// running 
		
		// dependency 
		BasicDependencyForensics dependencyForensics = new BasicDependencyForensics(forensicsContext);
		dependencyForensicsResult = dependencyForensics.runForensics();
		fingerPrintRegistry.getPrints().addAll( dependencyForensicsResult.getFingerPrintsOfIssues());

		// module forensics
		BasicModuleForensics moduleForensics = new BasicModuleForensics(forensicsContext);
		moduleForensicsResult = moduleForensics.runModuleForensics();
		// TODO: could there be any finger prints here?
				
		// classpath
		BasicClasspathForensics classpathForensics = new BasicClasspathForensics(forensicsContext);
		classpathForensicsResult = classpathForensics.runForensics();
		fingerPrintRegistry.getPrints().addAll( classpathForensicsResult.getFingerPrintsOfIssues());
		
		// model forensics 
		if (containsGenericEntities) {
			ModelForensics modelForensics = new BasicModelForensics(forensicsContext);
			modelForensicsResult = modelForensics.runForensics();
			fingerPrintRegistry.getPrints().addAll( modelForensicsResult.getFingerPrintsOfIssues());		
		}
		 
		Map<FingerPrint, ForensicsRating> ratedCurrentFingerPrints = ratings.rateCurrentFingerPrints(fingerPrintRegistry.getPrints());
		// find the worst rating of them all 
		ForensicsRating worstRating = ForensicsRating.OK;
		for (ForensicsRating fr : ratedCurrentFingerPrints.values()) {
			if (fr.ordinal() > worstRating.ordinal()) {
				worstRating = fr;
			}
		}

		
		
		/*
		//
		// dump a few data - for now 
		// 
		
		File targetDir = new File("res/fingerprints/" + terminalArtifact.getGroupId());
		targetDir.mkdirs();
		FingerPrintDumper.dump( targetDir, terminalArtifact, ratedCurrentFingerPrints);
		*/
		
		
		// if output mode is silent, we just return here 
		if (context.getConsoleOutputVerbosity() != ConsoleOutputVerbosity.silent) {
		
					
			//
			// output
			// 
			ConsoleConfiguration.install( new PrintStreamConsole( System.out, true));
			
			// console context 
			ConsoleContainerOut cco = wireContext.contract().consoleOutContract().consoleOut();
			
			Artifact runtimeArtifact = analyzingContext.artifacts().runtimeArtifact(analyzingContext);
			
			BasicConsoleOutputContext consoleOutputContext = new BasicConsoleOutputContext();				
			consoleOutputContext.setVerbosity( context.getConsoleOutputVerbosity());		
			consoleOutputContext.setRuntimeArtifact(runtimeArtifact);					
			consoleOutputContext.setFingerprintRegistry( fingerPrintRegistry);		
			consoleOutputContext.setRatingRegistry(ratings);
			
			
			if (uniqueNotFoundReasons.size() > 0 || unclassifiedReasons.size() > 0) {
				BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Issues during analysis");
				consoleOutputContext.pushIndent();
				
				if (uniqueNotFoundReasons.size() > 0) {
					cco.processScanErrorReasons(consoleOutputContext, uniqueNotFoundReasons);
				}		
				if (unclassifiedReasons.size() > 0) {			
					cco.processReasons(consoleOutputContext, unclassifiedReasons);		
				}
				consoleOutputContext.popIndent();		
			}		
			
			
			String terminalName = terminalArtifact.toVersionedStringRepresentation();
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Retrieved data of [" + terminalName + "]");
			
			// extraction data output 
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Extraction data");
			consoleOutputContext.pushIndent();		
		
			ConsoleOutputContainer cc = cco.processTerminal(consoleOutputContext, analysedArtifact);
			consoleOutputContext.popIndent();
			
			//
			// forensics output 
			//
			
			// dependency forensics data output		
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Dependency Forensics");
			consoleOutputContext.pushIndent();
			cc = cco.processDependencyForensics(consoleOutputContext, dependencyForensicsResult);				
			consoleOutputContext.popIndent();
						
			// drop all actual dependencies, if verbosity is more than verbose
			if (context.getConsoleOutputVerbosity().ordinal() > ConsoleOutputVerbosity.verbose.ordinal()) {
				List<Artifact> actualDependencies = analysedArtifact.getActualDependencies();
				if (actualDependencies != null && actualDependencies.size() > 0) {					
					actualDependencies.stream().forEach( a -> {										
						ArtifactForensicsResult artifactForensics = dependencyForensics.extractArtifactForensics(a);
						if (artifactForensics.getNumberOfReferences() == 0) {						
							ClasspathForensicsResult forensicsOnPopulation = classpathForensics.extractForensicsOnPopulation(a);
							artifactForensics.getDuplicates().addAll( forensicsOnPopulation.getDuplicates());
						}
						
						ConsoleContainerOut cco2 = new BasicConsoleContainerOut();
						cco2.processArtifactForensicsResult(consoleOutputContext, artifactForensics);
						
					});
				}
			}
			
			// classpath forensics output  
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Classpath Forensics");
			consoleOutputContext.pushIndent();
			cco.processClasspathForensicsResult(consoleOutputContext, classpathForensicsResult);
			consoleOutputContext.popIndent();
			
			// module forensics output
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Module Forensics");
			consoleOutputContext.pushIndent();
			cco.processModuleForensicsResult(consoleOutputContext, this.moduleForensicsResult);
			consoleOutputContext.popIndent();
			
			// model forensics output 
			if (containsGenericEntities && modelForensicsResult != null) {
				// inject default 'valid  supporter.. 
				BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Model Forensics");
				consoleOutputContext.pushIndent();
				cco.processModelForensicsResult(consoleOutputContext, modelForensicsResult);
				consoleOutputContext.popIndent();
				// model 
				BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Model Declaration Forensics");
				consoleOutputContext.pushIndent();
				cco.processModelDeclarationResult(consoleOutputContext, modelForensicsResult.getDeclarationResult());
				consoleOutputContext.popIndent();
							
			}
			BasicConsoleContainerOutputCommons.title( consoleOutputContext, "Overall rating");
			consoleOutputContext.pushIndent();
			String txt = "Rating for [" + terminalName + "] : " + worstRating.toString();
					
			consoleOutputContext.consoleOutputContainer().append( BasicConsoleContainerOutputCommons.padL( consoleOutputContext, txt));
			consoleOutputContext.popIndent();
			
			
			ConsoleOutputs.println(cc);			
		}
		
		// return value 
		if (umbrella == null) {
			collectedForensics = Maybe.complete( Pair.of(worstRating, ratedCurrentFingerPrints)); 
			return collectedForensics;
		}
		else {
			collectedForensics = Maybe.incomplete(Pair.of(worstRating, ratedCurrentFingerPrints), umbrella);
			return collectedForensics; 
		}
		 		
	}
		
	@Override
	public Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> collectedForensics() {	
		return collectedForensics;
	}
	/**
	 * analyze the collect reasons and tone them down so that only unique reasons remain, mostly for {@link UrlNotFound}
	 * @param rawAnalysisErrorReasons
	 * @return
	 */
	private List<Reason> coalesceAnalysisErrorReasons(List<Reason> rawAnalysisErrorReasons) {
		Map<String, Reason> uniqueReasons = new HashMap<>();
		List<Reason> result = new ArrayList<>( rawAnalysisErrorReasons.size()); // max size
		for (Reason reason : rawAnalysisErrorReasons) {
			if (reason instanceof UrlNotFound) {
				UrlNotFound unfReason = (UrlNotFound) reason;
				String key = unfReason.getScanExpression() + ":"  + unfReason.getScannedType() + ":" + unfReason.getScannedResource();
				uniqueReasons.computeIfAbsent(key, k -> reason);				
			}
			else {
				result.add( reason);
			}
		}
		result.addAll( uniqueReasons.values());
		
		return result;
	}
	private Reason acquireUmbrellaReason() {
		if (umbrella != null) {
			return umbrella;
		}
		umbrella = TemplateReasons.build( ZedAnalysisIssue.T)
											.assign( ZedAnalysisIssue::setArtifact, context.getTerminalArtifact())
											.assign( ZedAnalysisIssue::setTimestamp, new Date())
											.toReason();
		return umbrella;										
	}
	
}
