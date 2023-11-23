// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.console.ConsoleOutputs.brightRed;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static java.util.Collections.emptyList;

import java.util.Iterator;

import com.braintribe.build.cmd.assets.impl.modules.api.TfsContext;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentDescriptor;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentType;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.mc.core.commons.ArtifactResolutionUtil;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireModule;

import tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs;

/**
 * @author peter.gazdik
 */
public class ClasspathPreProcessor {

	public static void doYourThing(TfsContext context, ComponentDescriptor cd) {
		new ClasspathPreProcessor(context, cd).doItBro();
	}

	private final TfsContext context;
	private final ComponentDescriptor cd;

	private AnalysisArtifact currentCpItem;
	private String currentGrId;
	private String currentArId;

	/* package */ ClasspathPreProcessor(TfsContext context, ComponentDescriptor cd) {
		this.context = context;
		this.cd = cd;
		this.currentCpItem = cd.assetSolution;
	}

	private void doItBro() {
		Iterator<AnalysisArtifact> it = cd.transitiveSolutions.iterator();
		while (it.hasNext())
			if (handleIfKnownLogLib(it.next()))
				it.remove();
	}

	private boolean handleIfKnownLogLib(AnalysisArtifact moduleCpItem) {
		currentCpItem = moduleCpItem;
		currentGrId = currentCpItem.getGroupId();
		currentArId = currentCpItem.getArtifactId();

		if (autoExclude())
			return true;

		failIfNoHope();
		return false;
	}

	private boolean autoExclude() {
		if (!isLibrary("org.slf4j", "slf4j-api") && //
				!isLibrary("org.apache.logging.log4j", "log4j-api") && //
				!isLibrary("commons-logging", "commons-logging"))
			return false;

		for (AnalysisDependency libDependency : currentCpItem.getDependers()) {
			AnalysisArtifact libDepender = libDependency.getDepender();
			libDepender.getDependencies().remove(libDependency);
		}

		ConsoleOutputs.println( //
				ConsoleOutputs.sequence( //
						text("        Auto-removing "), //
						ArtifactOutputs.solution(currentCpItem), //
						text(" FROM " + cd.componentType().toString().toUpperCase() + " "), //
						ArtifactOutputs.solution(cd.assetSolution)) //
		);

		return true;
	}

	private void failIfNoHope() {
		if (isModulesPrivateDep())
			return;

		if (isKnownSlf4jBinding())
			fail("Found SLF4J binding", "Found SLF4J binding! Only a single binding is allowed on the classpath and platform adds it.");

		else if (isKnownSlf4jBridge())
			fail("Found SLF4J bridge!", "SLF4J bridges must not be provided! The platform takes care of that.");

		else if (isLoggingFrameworkImplementation())
			fail("Found logging framework implementation",
					"It's probably not needed and thus should be excluded (via exclusions in the POM file)." + explainHowToInclude());

		else if (isUnknownLoggingLibrary())
			fail("Found logging library",
					"Unless it's really needed, it should be excluded (via exclusions in the POM file)." + explainHowToInclude());
	}
	private boolean isModulesPrivateDep() {
		PlatformAsset a = cd.asset;
		return a.getNature() instanceof TribefireModule && context.isPrivateDep(a, currentCpItem);
	}

	private String explainHowToInclude() {
		if (cd.componentType() == ComponentType.Module)
			return " If the library is really required though, configure it as a private dependency of your module.";
		else
			return "";
	}

	private boolean isKnownSlf4jBinding() {
		// @formatter:off
		return isGroup("org.slf4j") && (
					isArtifact("slf4j-log4j12") ||
					isArtifact("slf4j-jcl") ||
					isArtifact("android") ||
					isArtifact("slf4j-jdk14") ||
					isArtifact("simple") ||
					isArtifact("slf4j-nop")
				);
		// @formatter:on
	}

	private boolean isKnownSlf4jBridge() {
		// @formatter:off
		return
				isLibrary("org.apache.logging.log4j", "log4j-to-slf4j") || (
					isGroup("org.slf4j") && (
							isArtifact("jul-to-slf4j") || //
							isArtifact("log4j-over-slf4j") || //
							isArtifact("jcl-over-slf4j") //
					)
				);
		// @formatter:on

	}

	private boolean isLoggingFrameworkImplementation() {
		return isLibrary("log4j", "log4j") || //
				isLibrary("org.apache.logging.log4j", "log4j-core") || //
				isLibrary("ch.qos.logback", "logback-core") || //
				isLibrary("ch.qos.logback", "logback-classic");
	}

	private boolean isUnknownLoggingLibrary() {
		return isGroup("org.slf4j") || //
				isGroup("commons-logging") || //
				isGroup("log4j") || //
				isGroup("org.apache.logging.log4j") || //
				isGroup("ch.qos.logback");
	}	

	private boolean isLibrary(String groupId, String artifactId) {
		return isGroup(groupId) && isArtifact(artifactId);
	}

	private boolean isArtifact(String artifactId) {
		return artifactId.equals(currentArId);
	}

	private boolean isGroup(String groupId) {
		return groupId.equals(currentGrId);
	}

	private void fail(String msgPrefix, String msgSuffix) {
		ConsoleOutputs.println();
		ConsoleOutputs.println( //
				ConsoleOutputs.sequence( //
						brightRed("Error: "), //
						text(msgPrefix + " "), //
						ArtifactOutputs.solution(currentCpItem), //
						text(". " + msgSuffix), //
						text(" Module: "), //
						ArtifactOutputs.solution(cd.assetSolution) //
				) //
		);

		ConsoleOutputs.println();
		ArtifactResolutionUtil.printTrimmedResolution(cd.resolution, asSet(currentCpItem), emptyList());

		throw new IllegalArgumentException("Setup failed as some module has a logging library dependency. See details above.");
	}

}
