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
package com.braintribe.devrock.zarathud.runner.impl.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;

public class Commons {
	/**
	 * re-reads all {@link Solution} in the classpath to get the actual content of the pom (and not pre-chewed by the walker)
	 * @param result - the result of a classpath walk
	 * @return - a {@link Collection} of *independently* read solutions (no clash, no 'already processed magic')
	 */
	public static Collection<AnalysisArtifact> buildCompiledSolutionsFromClasspath(Collection<AnalysisArtifact> classpath) {
		List<AnalysisArtifact> result = new ArrayList<>();
		/*
		PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
		classpath.stream().forEach( s -> {
			Part pom = s.getParts().stream().filter( p -> PartTupleProcessor.equals(pomPartTuple, p.getType())).findFirst().orElse( null);
			if (pom == null) {
				throw new IllegalStateException("solution [" + NameParser.buildName(s) + "] has no pom part?");
			}						
			Solution read = reader.readPom(UUID.randomUUID().toString(), new File( pom.getLocation()));
			result.add( read);
		});
		*/
		return result;
	}
	
	
}
