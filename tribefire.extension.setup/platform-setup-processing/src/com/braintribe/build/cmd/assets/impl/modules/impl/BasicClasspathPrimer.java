// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.console.ConsoleOutputs.brightRed;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.setup.tools.TfSetupTools.isPackagedAsJar;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.removeFirst;
import static java.util.Collections.emptyList;
import static tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs.solution;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.modules.api.ClasspathConfiguration;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsComponentSetup;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentDescriptor;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.mc.core.commons.ArtifactResolutionUtil;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.setup.tools.TfSetupTools;

/**
 * Basic classpath priming. This does the absolute minimum when it comes to classpath priming, by promoting (moving from module's classpath to plaform
 * classpath) the artifacts that need to be promoted (and their dependencies). These artifacts are for now models, GM APIs and platform libraries.
 * <p>
 * Note that this involves checking for version clashes, so e.g. having a different version of the same model in different modules would result in an
 * exception being thrown.
 * 
 * @author peter.gazdik
 */
class BasicClasspathPrimer {

	/**
	 * Basic classpath priming, adjusting the classpaths of platform and modules by promoting all models and APIs.
	 */
	public static void primeClasspaths(ClasspathConfiguration cp) {
		new BasicClasspathPrimer(cp).primeClasspaths();
	}

	private final ClasspathConfiguration cp;

	public BasicClasspathPrimer(ClasspathConfiguration cp) {
		this.cp = cp;
	}

	private final Map<AnalysisArtifact, AnalysisArtifact> libSolutionToClash = newMap();

	private void primeClasspaths() {
		removeNoJarModuleDeps();

		addPlatformLibrariesToPlatformSolutions();

		checkNoModuleWithPlatformOnlyDeps();

		promoteNobles();

		/* After we have promoted our nobles, maybe we have also promoted some non-marked nobles, if they are dependencies of marked nobles. Therefore
		 * we run this again to get rid of such promoted solutions from the module classpath */
		cp.removePlatformSolutionsFromModules();
	}

	private void removeNoJarModuleDeps() {
		cp.moduleSetups.stream() //
				.forEach(this::removeNoJarDepsFrom);
	}

	private void removeNoJarDepsFrom(TfsComponentSetup moduleSetup) {
		AnalysisArtifact s;

		Iterator<AnalysisArtifact> it = moduleSetup.classpath.iterator();
		while (it.hasNext())
			if (!isPackagedAsJar(s = it.next())) {
				printWillIgnoreNonJar(s, moduleSetup);
				it.remove();
			}
	}

	private void printWillIgnoreNonJar(AnalysisArtifact s, TfsComponentSetup moduleSetup) {
		if (cp.context.verbose)
			println(sequence( //
					text("            Ignoring '"), //
					solution(s), //
					text("' as is it has no jar, but is packaged as: " + s.getOrigin().getPackaging()), //
					text(". Module: " + moduleSetup.shortComponentName()) //
			));
	}

	private void addPlatformLibrariesToPlatformSolutions() {
		// Note that temporarily we also have models mixed with libraryDescriptors, so we also promote models from cartridges.
		for (ComponentDescriptor libDescriptor : cp.libraryDescriptors) {
			for (AnalysisArtifact librarySolution : libDescriptor.transitiveSolutions)
				if (isPackagedAsJar(librarySolution))
					promoteLibraryToPlatform(librarySolution, libDescriptor);
			failIfClashes(libDescriptor);
		}
	}

	private void promoteLibraryToPlatform(AnalysisArtifact libSolution, ComponentDescriptor libDescriptor) {
		AnalysisArtifact clashingSolution = cp.promoteToPlatformIfNoClashes(libSolution, libDescriptor);

		if (clashingSolution != null)
			libSolutionToClash.put(libSolution, clashingSolution);
	}

	private void failIfClashes(ComponentDescriptor libDesc) {
		if (libSolutionToClash.isEmpty())
			return;

		printClashingLibDeps(libDesc);

		ArtifactResolutionUtil.printTrimmedResolution(libDesc.resolution, libSolutionToClash.keySet(), emptyList());

		throw new IllegalArgumentException("Houston, we have a problem. Cannot prepare this setup. See details above.");
	}

	private void printClashingLibDeps(ComponentDescriptor libDesc) {
		ConsoleOutputs.println();
		ConsoleOutputs.println( //
				ConsoleOutputs.sequence( //
						brightRed("Error: "), //
						text("Platform library has dependencies incompatible with the platform. Library: "),
						ArtifactResolutionUtil.outputArtifact(libDesc.assetSolution) //
				) //
		);

		for (Entry<AnalysisArtifact, AnalysisArtifact> e : libSolutionToClash.entrySet())
			ConsoleOutputs.println( //
					ConsoleOutputs.sequence( //
							text("    Libary dependency "), //
							ArtifactResolutionUtil.outputArtifact(e.getKey()), //
							text(" is incompatible with platform's "), //
							ArtifactResolutionUtil.outputArtifact(e.getValue()) //
					) //
			);

		ConsoleOutputs.println();
	}

	private void checkNoModuleWithPlatformOnlyDeps() {
		for (TfsComponentSetup moduleSetup : cp.moduleSetups)
			for (AnalysisArtifact moduleCpItem : moduleSetup.classpath)
				if (cp.isPlatformOnly(moduleCpItem))
					failForPlatformOnly(moduleSetup.componentDescriptor, moduleCpItem);
	}

	private void failForPlatformOnly(ComponentDescriptor moduleDesc, AnalysisArtifact platformOnlyArtifact) {
		ConsoleOutputs.println();
		ConsoleOutputs.println( //
				ConsoleOutputs.sequence( //
						brightRed("Error: "), //
						text("Module "), //
						ArtifactResolutionUtil.outputArtifact(moduleDesc.assetSolution), //
						text(" depends on a \"platform-only\" artifact: "), //
						ArtifactResolutionUtil.outputArtifact(platformOnlyArtifact) //
				) //
		);

		ConsoleOutputs.println();
		ArtifactResolutionUtil.printTrimmedResolution(moduleDesc.resolution, asSet(platformOnlyArtifact), emptyList());

		throw new IllegalArgumentException("Houston, we have a problem. Cannot prepare this setup. See details above.");
	}

	/** Regarding the name see {@link ClasspathConfiguration#NOBLE_GM_NATURES} */
	private void promoteNobles() {
		for (TfsComponentSetup moduleSetup : cp.moduleSetups) {
			Set<AnalysisArtifact> moduleNobles = findNobles(moduleSetup);
			Set<AnalysisArtifact> handledCpItems = TfSetupTools.analysisArtifactSet();

			while (!moduleNobles.isEmpty()) {
				AnalysisArtifact noble = removeFirst(moduleNobles);
				cp.promoteSolutionAndItsDepsIfPossible(noble, moduleSetup, true, handledCpItems);
			}
		}
	}

	private Set<AnalysisArtifact> findNobles(TfsComponentSetup moduleSetup) {
		try {
			return moduleSetup.classpath.stream() //
					.filter(cp::isNoble) //
					.collect(Collectors.toSet());

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while resolving nobles for module: " + moduleSetup.componentName());
		}
	}

}
