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
package com.braintribe.devrock.zed.commons;

import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.zarathud.model.data.Artifact;

public class ConversionHelper {
	/**
	 * convert a solution to an artifact
	 * @param solution - the {@link AnalysisArtifact}
	 * @return - a matching {@link Artifact}
	 */
	public static Artifact toArtifact( ZedAnalyzerContext context, AnalysisArtifact solution) {
		Artifact artifact = Commons.create(context, Artifact.T);				
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( solution.getVersion());		
		return artifact;
	}
	
	/**
	 * safely extract a class name from a desc
	 * @param desc - the signature or desc
	 * @return - a signature.
	 */
	public static String toClassName( String desc) {
		if (desc.length() > 1) {
			int startIndex = 0;
			if (desc.startsWith( "L"))
				startIndex = 1;
			else
				startIndex = 0;
			int endIndex = desc.length();
			if (desc.endsWith( ";"))
				endIndex = desc.length() -1;
			else 
				endIndex = desc.length();
			
			return desc.substring( startIndex, endIndex).replace('/', '.');
		}
		return desc;
	}
}
