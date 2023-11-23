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
package com.braintribe.devrock.zarathud.runner;

import java.util.function.Predicate;

import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * simple context for the runner
 * @author pit
 *
 */
public class RunnerContext {
	/**
	 *  required : the terminal
	 */
	public AnalysisArtifact solution;

	/**
	 * defaults : true if model forensics should be done after extraction, default : false; 
	 */
	//public boolean runModelForensics = false;

	
	public boolean allowBraintribeSpecifica = false;
	/**
	 * defaults : output kind, default : OutputKind.console
	 */
	public OutputKind outputKind = OutputKind.console;	
	
	
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
