// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.api;

import com.braintribe.console.ConsoleOutputs;

/**
 * @author peter.gazdik
 */
public interface TfsClasspathOptimizer {

	void pimpMyClasspaths(ClasspathConfiguration cp);

	static TfsClasspathOptimizer emptyOptimizer() {
		return cp -> { 
			ConsoleOutputs.println("            <No optimization configured>");
		};
	}

}
