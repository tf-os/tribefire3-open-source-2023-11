// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs.solution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.modules.api.ClasspathConfiguration;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsComponentSetup;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsContext;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentDescriptor;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentSetup;
import com.braintribe.build.cmd.assets.impl.modules.model.TfSetup;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.mc.core.commons.ArtifactResolutionUtil;
import com.braintribe.devrock.mc.core.commons.DownloadMonitor;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.collection.impl.AttributeContexts;

import tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs;

/**
 * @author peter.gazdik
 */
public class TfSetupResolver {

	public static TfSetup resolve(TfsContext context) {
		return new TfSetupResolver(context).resolve();
	}

	private final TfsContext context;

	// variables for the scope of the resolve method
	private ComponentDescriptor platformDescriptor;
	private List<ComponentDescriptor> moduleDescriptors;
	private List<ComponentDescriptor> libraryDescriptors;

	private TfsComponentSetup tfsPlatformSetup;
	private List<TfsComponentSetup> tfsModuleSetups;

	private ClasspathConfiguration cp;

	private TfSetupResolver(TfsContext context) {
		this.context = context;
	}

	private TfSetup resolve() {
		collectDeclaredClasspathsDlWithMonitor();

		preProcessClasspaths();

		initComponentSetups();

		computeFinalClasspaths();

		return buildResult();
	}

	private void collectDeclaredClasspathsDlWithMonitor() {
		EventHub eventHub = new EventHub();

		AttributeContext attributeContext = AttributeContexts.derivePeek() //
				.set(EventBroadcasterAttribute.class, eventHub) //
				.build(); //

		AttributeContexts.push(attributeContext);

		try (DownloadMonitor downloadMonitor = new DownloadMonitor(eventHub)) {
			downloadMonitor.setIndent(8);
			downloadMonitor.setInitialLinebreak(true);

			collectDeclaredClasspaths();

		} finally {
			AttributeContexts.pop();
		}
	}

	private void collectDeclaredClasspaths() {
		platformDescriptor = toComponentDescriptor(context.tfPlatformAsset);
		moduleDescriptors = toComponentDescriptors(context.moduleAssets);
		libraryDescriptors = toComponentDescriptors(context.libraryAssets);
	}

	private List<ComponentDescriptor> toComponentDescriptors(List<PlatformAsset> assets) {
		return assets.stream() //
				.map(this::toComponentDescriptor) //
				.collect(Collectors.toList());
	}

	/** This method is thread safe. */
	private ComponentDescriptor toComponentDescriptor(PlatformAsset asset) {
		println(sequence(//
				text("        Resolving: "), solution(asset.getGroupId(), asset.getName(), asset.versionWithRevision())));

		CompiledTerminal assetTerminal = TfSetupTools.platformAssetToTerminal(asset);
		AnalysisArtifactResolution resolution = context.resolutionContext.classpathResolver().resolve(cpResolutionContext(), assetTerminal);

		if (resolution.hasFailed()) {
			ArtifactResolutionUtil.printFailedResolution(resolution);
			throw new RuntimeException("Resolution failed for asset: " + asset.qualifiedRevisionedAssetName() + ". See details above.");
		}

		ComponentDescriptor result = new ComponentDescriptor();
		result.asset = asset;
		result.assetSolution = ((AnalysisDependency) first(resolution.getTerminals())).getSolution();
		result.transitiveSolutions = resolution.getSolutions();
		result.resolution = resolution;

		return result;
	}

	private ClasspathResolutionContext cpResolutionContext() {
		return ClasspathResolutionContext.build() //
				.scope(ClasspathResolutionScope.runtime) //
				.enrichJar(true) //
				.done();
	}

	private void preProcessClasspaths() {
		if (!context.preProcessClasspaths)
			return;

		println();

		for (ComponentDescriptor moduleDescriptor : moduleDescriptors)
			preProcessDependencies(moduleDescriptor);

		for (ComponentDescriptor libDescriptor : libraryDescriptors)
			preProcessDependencies(libDescriptor);

		context.solutionOracle.purgeTransitiveSolutionsCache(); // we have changed some classpaths, it's cleaner to purge the cache
	}

	private void preProcessDependencies(ComponentDescriptor cd) {
		ClasspathPreProcessor.doYourThing(context, cd);
	}

	private void initComponentSetups() {
		tfsPlatformSetup = toTfsComponentSetup(platformDescriptor);

		tfsModuleSetups = moduleDescriptors.stream() //
				.map(this::toTfsComponentSetup) //
				.collect(Collectors.toList());

		context.solutionOracle.setComponentSetups(tfsPlatformSetup, tfsModuleSetups);
		cp = new ClasspathConfiguration(context, tfsPlatformSetup, tfsModuleSetups, libraryDescriptors);
	}

	private TfsComponentSetup toTfsComponentSetup(ComponentDescriptor componentDescriptor) {
		return new TfsComponentSetup(componentDescriptor);
	}

	private void computeFinalClasspaths() {
		printClasspaths("Declared", true, cp);

		println("\n        Basic classpath preparation (promoting Models, APIs and Platform Libraries)...");
		BasicClasspathPrimer.primeClasspaths(cp);

		println("\n        Classpath optimization (promoting other artifacts where possible)...");
		context.classpathOptimizer.pimpMyClasspaths(cp);

		printClasspaths("Computed", false, cp);
	}

	private void printClasspaths(String cpType, boolean printTree, ClasspathConfiguration cp) {
		if (!cp.context.verbose)
			return;

		println("\n        " + cpType + " classpaths: ");
		cp.componentSetups().forEach(c -> printClasspath(c, printTree));
	}

	private void printClasspath(TfsComponentSetup c, boolean printTree) {
		if (c.classpath.isEmpty())
			return;

		ConsoleOutput delimiter = text("; ");

		ConsoleOutput[] platformClasspathOut = c.classpath.stream() //
				.map(ArtifactOutputs::solution) //
				.map(output -> sequence(output, delimiter)) //
				.toArray(n -> new ConsoleOutput[n]);

		println(sequence( //
				text("\n        " + c.shortComponentName() + " (" + c.classpath.size() + "): "), //
				sequence(platformClasspathOut) //
		));

		if (printTree) {
			println();
			ArtifactResolutionUtil.printDependencyTree(c.componentDescriptor.resolution);
		}
	}

	private TfSetup buildResult() {
		List<ComponentSetup> moduleSetups = tfsModuleSetups.stream() //
				.map(TfSetupResolver::toComponentSetup) //
				.collect(Collectors.toList());

		TfSetup result = new TfSetup();
		result.platformSetup = toComponentSetup(tfsPlatformSetup);
		result.moduleSetups = moduleSetups;
		result.allModulesCpSolutions = allCpSolutionsOf(moduleSetups);

		return result;
	}

	private static ComponentSetup toComponentSetup(TfsComponentSetup tfsComponentSetup) {
		ComponentSetup result = new ComponentSetup();
		result.descriptor = tfsComponentSetup.componentDescriptor;
		result.classpath = tfsComponentSetup.finalizeClasspath();

		return result;
	}

	private Set<AnalysisArtifact> allCpSolutionsOf(List<ComponentSetup> moduleSetups) {
		Set<AnalysisArtifact> result = TfSetupTools.analysisArtifactSet();
		for (ComponentSetup moduleSetup : moduleSetups)
			result.addAll(moduleSetup.classpath);

		return result;
	}

}
