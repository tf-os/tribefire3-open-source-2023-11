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
package com.braintribe.devrock.zarathud.runner.wire.context;

import java.util.function.Predicate;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * simple context for the runner
 * @author pit
 *
 */
public class ZedRunnerContext implements ScopeContext {
	/**
	 *  required : the terminal
	 */
	public AnalysisArtifact solution;
	
	/**
	 * defaults : true if 'braintribe quirks' (such as implict gm-core-api references) are allowed, default : false
	 */
	public boolean allowBraintribeSpecifica = false;
	
	/**
	 * defaults : verbosity of output, default : {@link ConsoleOutputVerbosity.verbose}   
	 */
	public ConsoleOutputVerbosity consoleOutputVerbosity = ConsoleOutputVerbosity.verbose;
	
	
	/**
	 * defaults : true if all class references should be listed 
	 */
	public boolean dumpTopArtifactClassReferences = false; 
	
	/**
	 * optional : filter to phase out specific entities
	 */

	public Predicate<ZedEntity> filter;
	
	
}
