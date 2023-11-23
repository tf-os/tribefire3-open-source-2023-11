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
import java.util.Map;

import com.braintribe.devrock.zarathud.model.context.ConfigurationAspect;
import com.braintribe.devrock.zarathud.model.context.CoreAnalysisAspect;
import com.braintribe.devrock.zarathud.model.context.CoreProcessingAspect;
import com.braintribe.devrock.zarathud.model.context.RatingAspect;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ClassesProcessingRunnerContext extends RatingAspect, CoreAnalysisAspect, CoreProcessingAspect, ConfigurationAspect {
	
	EntityType<ClassesProcessingRunnerContext> T = EntityTypes.T(ClassesProcessingRunnerContext.class);
	
	String terminalClassesDirectoryNames = "terminalClassesDirectoryNames";
	String compiledSolutionsOfClasspath = "compiledSolutionsOfClasspath";
	String nonpackedSolutionsOfClasspath = "nonpackedSolutionsOfClasspath";
	
	/**
	 * @return - the folder to look for the classes of the terminal
	 */
	List<String> getTerminalClassesDirectoryNames();
	void setTerminalClassesDirectoryNames(List<String> value);

	/**
	 * @return - the {@link AnalysisArtifact}s that have a jar as {@link Part}
	 */
	List<AnalysisArtifact> getCompiledSolutionsOfClasspath();
	void setCompiledSolutionsOfClasspath( List<AnalysisArtifact> solution);
	
	
	/**
	 * @return - the {@link AnalysisArtifact}s that have classes-folders
	 */
	Map<String,AnalysisArtifact> getNonpackedSolutionsOfClasspath();
	void setNonpackedSolutionsOfClasspath(Map<String,AnalysisArtifact> value);
 
}
