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
package com.braintribe.devrock.zed.api.context;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.braintribe.devrock.zed.api.core.Verbosity;
import com.braintribe.devrock.zed.scan.ScannerResult;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * the basic context for the zed-core 
 * @author pit
 *
 */
public interface ZedAnalyzerContext extends CommonZedCoreContext {
	/**
	 * @return - a {@link Collection} of all {@link AnalysisArtifact} that make up the classpath
	 */
	Collection<AnalysisArtifact> classpath();
	/**
	 * @return - a {@link List} of the class names to analyze
	 */
	List<String> classesToProcess();
	
	/**
	 * @return - 
	 */
	ScannerResult terminalScanData();
	/**
	 * @return - the declared dependencies of the terminal
	 */
	List<AnalysisDependency> declaredTerminalDependencies();
	
	
	/**
	 * @return - the {@link AnalysisArtifact}s provided via folders of classes (case of Eclipse projects)
	 */
	List<AnalysisArtifact> additionsToClasspath();
	
	
	/**
	 * @return - the currently scanned resource, may be null
	 */
	URL currentlyScannedResource();
	
	/**
	 * @param resource - the currently scanned resource
	 */
	void setCurrentlyScannedResource(URL resource);
	
	/**
	 * @return - the Java runtime jar used
	 */
	URL runtimeJar();
	
	/**
	 * @return - the current {@link Verbosity} level
	 */
	Verbosity verbosity();
	
}
