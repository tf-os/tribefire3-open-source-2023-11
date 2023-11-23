// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.api;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.braintribe.build.cmd.assets.impl.modules.model.ComponentDescriptor;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentSetup;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.setup.tools.TfSetupTools;

/**
 * Precursor to {@link ComponentSetup} which is used while classpath is being computed. The classpath property is intended to be modified along the
 * way (i.e. when we promote solutions, we remove them from module's setup and add them to the platform's setup). Once the classpath computation is
 * done, we create a {@link ComponentSetup} using the {@link #componentDescriptor} and the result of {@link #finalizeClasspath()}.
 * 
 * @author peter.gazdik
 */
public class TfsComponentSetup {

	public final ComponentDescriptor componentDescriptor;
	public final Set<AnalysisArtifact> classpath = TfSetupTools.analysisArtifactSet();
	public final Set<AnalysisArtifact> originalClasspath = TfSetupTools.analysisArtifactSet();

	public TfsComponentSetup(ComponentDescriptor cd) {
		this.componentDescriptor = requireNonNull(cd);
		this.classpath.addAll(cd.transitiveSolutions);
		this.originalClasspath.addAll(this.classpath);
	}

	/**
	 * Prepares the final classpath, which contains the component's own jar (e.g. my-module.jar) as the very first entry, all other jars sorted by
	 * their {@link AnalysisArtifact#asString()} representation.
	 */
	public List<AnalysisArtifact> finalizeClasspath() {
		if (classpath.isEmpty())
			return emptyList();

		AnalysisArtifact componentSolution = componentDescriptor.assetSolution;

		Set<AnalysisArtifact> cpCopy = newSet(classpath);
		if (!cpCopy.remove(componentSolution))
			throw new IllegalStateException("Something went really wrong. The component '" + componentSolution.asString()
					+ " has non-empty classpath which does not contain the component's own jar (" + componentSolution.getArtifactId() + ".jar). Cp: "
					+ cpCopy);

		List<AnalysisArtifact> result = newList();
		result.addAll(cpCopy);
		result.sort(Comparator.comparing(AnalysisArtifact::asString));

		result.add(0, componentSolution);

		return result;
	}

	// To have a method reference
	public ComponentDescriptor getComponentDescriptor() {
		return componentDescriptor;
	}

	@Override
	public String toString() {
		return "ComponentSetup[" + componentName() + "]";
	}

	public String componentName() {
		return componentDescriptor.asset.qualifiedAssetName();
	}

	public String shortComponentName() {
		return componentDescriptor.asset.getName();
	}

}
