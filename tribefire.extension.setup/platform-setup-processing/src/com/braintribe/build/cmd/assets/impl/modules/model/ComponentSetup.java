// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.model;

import java.util.List;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.asset.PlatformAsset;

/**
 * Represents the final classpath setup for a TF component, based on the information from all relevant components. <br>
 * 
 * For example, all models and GM APIs from modules are moved to the platform, and further optimization of the setup
 * might be done (e.g. moving arbitrary libraries shared by two or more modules to the platform, as long as it doesn't
 * create clashes). <br>
 * 
 * The information about the original classpaths from the individual components, as well as the actual
 * {@link PlatformAsset} representing this component, is attached via the {@link #descriptor} property.
 *
 * @author peter.gazdik
 */
public class ComponentSetup {


	/**
	 * The actual classpath to be configured for this asset. The solution for the asset itself is included as the very
	 * first element. For the platform it contains all the transitive solutions and the promoted solutions from modules.
	 * For modules this is a subset of transitive solutions, which lacks the promoted solutions, and so can also be empty.
	 */
	public List<AnalysisArtifact> classpath;
	public ComponentDescriptor descriptor;

}
