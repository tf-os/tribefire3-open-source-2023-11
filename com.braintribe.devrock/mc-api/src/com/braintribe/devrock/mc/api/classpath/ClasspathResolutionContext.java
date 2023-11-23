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
package com.braintribe.devrock.mc.api.classpath;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.impl.classpath.BasicClasspathResolutionContext;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * the configuration context for the {@link ClasspathDependencyResolver}
 * @author pit / dirk
 *
 */
public interface ClasspathResolutionContext {
	
	/**
	 * @return - whether it's lenient (default : false)
	 */
	boolean lenient();
	/**
	 * @return - the currently active {@link ClasspathResolutionScope}, aka the 'magic scope', 
	 * (default : compile)
	 */
	ClasspathResolutionScope scope();
	/**
	 * @return - the currently active strategy, either 'first visit' or 'highest version'
	 * (default : highest version)
	 */
	ClashResolvingStrategy clashResolvingStrategy();
	
	/**
	 * Enrich cp-relevant solutions with jar in a mandatory and logical way
	 * @return true if jars should be enriched (default : true)
	 */
	boolean enrichJar();
	
	/**
	 * Enrich cp-relevant solutions with -javadoc.jar in an optional way
	 * (default : false)
	 * @return - true if javadoc should be leniently enriched
	 */
	boolean enrichJavadoc();

	/**
	 * Enrich cp-relevant solutions with -sources.jar in an optional way
	 * (default : false)
	 * @return - true if sources should be leniently enriched
	 */
	boolean enrichSources();
	
	/**
	 * @return - the {@link PartEnrichingContext}
	 */
	PartEnrichingContext enrich();
	
	/**
	 * @return - the dependency filter (default: pass through, i.e. filter needs to 
	 * return true if the dependency is to be processed)
	 */
	Predicate<AnalysisDependency> dependencyFilter();
	
	/**
	 * @return - a {@link Set} of {@link ArtifactIdentification} that should be excluded throughout the tree
	 */
	Set<ArtifactIdentification> globalExclusions();
	
	/**
	 * @return - the artifact filter that sees the full path to the artifact (default : pass through). CURRENTLY NOT SUPPORTED IN ACTUAL RESOLUTION. 
	 */
	Predicate<? super ArtifactPathElement> artifactPathFilter();
	
	/**
	 * @return - the dependency filter that sees the full path to the dependency (default : pass through)
	 */
	Predicate<? super DependencyPathElement> dependencyPathFilter();

	
	/**
	 * the entry point for building a {@link ClasspathResolutionContext}
	 * @return
	 */
	static ClasspathResolutionContextBuilder build() {
		return new BasicClasspathResolutionContext();
	}
}
