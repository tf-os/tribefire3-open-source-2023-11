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
package com.braintribe.devrock.zarathud.model.request;

import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the request to analyze an artifact (based on ZedRunner's resolution feature)
 * @author pit
 *
 */
@Alias("zed")
public interface AnalyzeArtifact extends ZedRequest {
	
	final EntityType<AnalyzeArtifact> T = EntityTypes.T(AnalyzeArtifact.class);

	@Description("the qualified name of the artifact to analyze")
	@Alias( "t")
	@Mandatory
	String getTerminal();
	void setTerminal(String terminal);
	
	@Description("the output directory where to write the result to")
	@Alias( "o")
	String getOutputDir();
	void setOutputDir(String  output);

	@Description("whether to additionally write all collected data. Default : false")
	@Alias( "w")
	boolean getWriteAnalysisData();
	void setWriteAnalysisData(boolean writeAnalysisData);
	
	@Description("the verbosity : taciturn, terse, verbose, garrulous. Default : verbose")
	@Alias( "v")
	ConsoleOutputVerbosity getVerbosity();
	void setVerbosity(ConsoleOutputVerbosity  verbosity);
}
