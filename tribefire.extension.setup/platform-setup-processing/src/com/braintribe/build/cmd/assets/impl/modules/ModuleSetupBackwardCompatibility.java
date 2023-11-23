// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules;

import java.util.List;

import com.braintribe.build.cmd.assets.impl.modules.model.TfSetup;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.version.Version;

import tribefire.descriptor.model.ModuleDescriptor;

/**
 * @author peter.gazdik
 */
/* package */ class ModuleSetupBackwardCompatibility {

	/* package */ static void ensureBc(TfSetup tfSetup, List<ModuleDescriptor> moduleDescriptors) {
		new ModuleSetupBackwardCompatibility(tfSetup, moduleDescriptors).run();
	}

	private final Version V_2_0_8 = Version.create(2, 0, 8);

	
	private final TfSetup tfSetup;
	private final List<ModuleDescriptor> moduleDescriptors;

	public ModuleSetupBackwardCompatibility(TfSetup tfSetup, List<ModuleDescriptor> moduleDescriptors) {
		this.tfSetup = tfSetup;
		this.moduleDescriptors = moduleDescriptors;
	}

	private void run() {
		removeAccessIdAndDepsForDescriptorModelLessThan8();
	}

	private void removeAccessIdAndDepsForDescriptorModelLessThan8() {
		if (hasOldTfDesModel())
			removeAccessIdAndDeps();
	}

	private void removeAccessIdAndDeps() {
		for (ModuleDescriptor md : moduleDescriptors) {
			md.setAccessIds(null);
			md.setDependedModules(null);
		}
	}

	private boolean hasOldTfDesModel() {
		return tfSetup.platformSetup.classpath.stream() //
				.filter(this::isOldTfDescriptorModel) //
				.findFirst() //
				.isPresent();
	}

	private boolean isOldTfDescriptorModel(AnalysisArtifact s) {
		return "tribefire.cortex".equals(s.getGroupId()) //
				&& "tribefire-descriptor-model".equals(s.getArtifactId()) //
				&& V_2_0_8.isHigherThan(Version.parse(s.getVersion())); // if 2.0.8 > s.version
	}
}
