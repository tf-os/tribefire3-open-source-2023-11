// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.model;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;

/**
 * Represents a setup for the entire platform + modules package.
 * 
 * @author peter.gazdik
 */
public class TfSetup {

	public ComponentSetup platformSetup;
	public Set<AnalysisArtifact> allModulesCpSolutions;
	public List<ComponentSetup> moduleSetups;

}
