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
package com.braintribe.devrock.zarathud.model;

import java.util.List;

import com.braintribe.devrock.zarathud.model.context.ConfigurationAspect;
import com.braintribe.devrock.zarathud.model.context.CoreAnalysisAspect;
import com.braintribe.devrock.zarathud.model.context.CoreProcessingAspect;
import com.braintribe.devrock.zarathud.model.context.RatingAspect;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a context as used by the ant integration..
 * @author pit
 *
 */
public interface JarProcessingRunnerContext extends CoreAnalysisAspect, CoreProcessingAspect, RatingAspect, ConfigurationAspect {
	
	EntityType<JarProcessingRunnerContext> T = EntityTypes.T(JarProcessingRunnerContext.class);
	String terminalJarName = "terminalJarName";
	String compiledSolutionsOfClasspath = "compiledSolutionsOfClasspath";

	String getTerminalJarName();
	void setTerminalJarName( String name);
	
	/**
	 * @return - a list of solutions, ie. as returned by the pom reader
	 */
	List<CompiledArtifact> getCompiledSolutionsOfClasspath();
	void setCompiledSolutionsOfClasspath( List<CompiledArtifact> solution);
	
	
}
