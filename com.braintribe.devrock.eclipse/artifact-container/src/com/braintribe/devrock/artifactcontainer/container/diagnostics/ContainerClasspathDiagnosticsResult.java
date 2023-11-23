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
package com.braintribe.devrock.artifactcontainer.container.diagnostics;

import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.artifact.Solution;

/**
 * a container for solutions that are special for classpath handling, i.e. pom aggregates or jars treated as aggregates are 
 * referenced in here.
 * 
 * @author Pit
 *
 */
public class ContainerClasspathDiagnosticsResult {

	private Set<Solution> pomAggregates = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	private Set<Solution> pomAggregatesReferencedAsJars = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	private Set<Solution> jarReferencedAsPomAggregates = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	private Set<Solution> nonjarReferencedAsClassesJar = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	
	public void addToPomAggregates( Solution solution) {
		pomAggregates.add(solution);
	}
	
	public void addToPomAggregatesReferencedAsJars( Solution solution) {
		pomAggregatesReferencedAsJars.add(solution);
	}
	
	public void addtoJarReferencedAsPomAggregates( Solution solution) {
		jarReferencedAsPomAggregates.add(solution);
	}
	
	public void addToNonJarReferencedAsClassesJar( Solution solution) {
		nonjarReferencedAsClassesJar.add( solution);
	}
	
	/**
	 * returns the {@link ClasspathDiagnosticsClassification} of the given {@link Solution} 
	 * @param solution - the {@link Solution} to check for 
	 * @return - the {@link ClasspathDiagnosticsClassification} for the solution
	 */
	public ClasspathDiagnosticsClassification getDiagnosticsClassificationOfSolution( Solution solution) {
		if (pomAggregates.contains(solution)) { 
			return ClasspathDiagnosticsClassification.pomAsPom;
		}
		if (pomAggregatesReferencedAsJars.contains(solution)) {
			return ClasspathDiagnosticsClassification.pomAsJar;
		}
		if (jarReferencedAsPomAggregates.contains(solution)) { 
			return ClasspathDiagnosticsClassification.jarAsPom;
		}
		if (nonjarReferencedAsClassesJar.contains(solution)) {
			return ClasspathDiagnosticsClassification.nonJarAsClassesJar;
		}
		return ClasspathDiagnosticsClassification.standard;
	}

	/**
	 * checks if anything "improper" is stored within the container's data<br/>
	 * if neither poms referenced as jars or jars referenced as poms, it will return {@link ClasspathDiagnosticsClassification#standard}  
	 * @return - the {@link ClasspathDiagnosticsClassification} overall 
	 */
	public ClasspathDiagnosticsClassification getDiagnosticsClassification() {
		if (jarReferencedAsPomAggregates.size()> 0) {
			return ClasspathDiagnosticsClassification.jarAsPom;
		}
		if (pomAggregatesReferencedAsJars.size() > 0) {
			return ClasspathDiagnosticsClassification.pomAsJar;
		}
		return ClasspathDiagnosticsClassification.standard;
	}
}
