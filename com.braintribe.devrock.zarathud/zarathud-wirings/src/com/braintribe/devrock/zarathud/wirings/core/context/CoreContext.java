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
package com.braintribe.devrock.zarathud.wirings.core.context;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.zarathud.model.data.Artifact;

/**
 * the {@link CoreContext} is used throughout the analysis
 * @author pit
 *
 */
public class CoreContext implements ScopeContext {

	Collection<AnalysisArtifact> classpath;
	Collection<String> classesDirectories;
	Map<String, AnalysisArtifact> foldersForNonPackagedClasses;
	
	List<AnalysisDependency> dependencies;
	Collection<CompiledArtifact> compiledSolutionsOfClasspath;
	
	BasicPersistenceGmSession session;
	
	Resource customRatingsResource;
	Resource pullRequestRatingsResource;
	
	Artifact terminalArtifact;
	private boolean respectBraintribeSpecifica;
	private ConsoleOutputVerbosity consoleOutputVerbosity = ConsoleOutputVerbosity.terse;
	
	
	/**
	 * @return - the {@link List} of the *declared* {@link AnalysisDependency} of the terminal artifact
	 */
	public List<AnalysisDependency> getDependencies() {
		return dependencies;
	}
	public void setDependencies(List<AnalysisDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	/**
	 * @return - the {@link Collection} of {@link AnalysisArtifact} that make up the classpath
	 */
	public Collection<AnalysisArtifact> getClasspath() {
		return classpath;
	}
	public void setClasspath(Collection<AnalysisArtifact> classpath) {
		this.classpath = classpath;
	}		
	
	/**
	 * used for the Eclipse integration : folders for classes (one per {@link AnalysisArtifact}
	 * @return - a {@link Map} of the classes-folder's name and the associated {@link AnalysisArtifact}
	 */
	public Map<String, AnalysisArtifact> getFoldersForNonPackagedClasses() {
		return foldersForNonPackagedClasses;
	}
	public void setFoldersForNonPackagedClasses(Map<String, AnalysisArtifact> foldersForNonPackagedClasses) {
		this.foldersForNonPackagedClasses = foldersForNonPackagedClasses;
	}
	/**
	 * @return - the Folder where the .class files are of the terminal (if not contained as AnalysisArtifact)
	 */
	public Collection<String> getClassesDirectories() {
		return classesDirectories;
	}
	public void setClassesDirectories(Collection<String> classesDirectories) {
		this.classesDirectories = classesDirectories;
	}
	
	/**
	 * @return - the {@link Collection} of the {@link AnalysisArtifact} with-out any filtering by the classpath resolution
	 */
	public Collection<CompiledArtifact> getCompiledSolutionsOfClasspath() {
		return compiledSolutionsOfClasspath;
	}
	public void setCompiledSolutionsOfClasspath(Collection<CompiledArtifact> compiledSolutionsOfClasspath) {
		this.compiledSolutionsOfClasspath = compiledSolutionsOfClasspath;
	}
	
	/**
	 * @return - an optional {@link BasicPersistenceGmSession}
	 */
	public BasicPersistenceGmSession getSession() {
		return session;
	}
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	
	/**
	 * @return - the {@link Resource} that contains the custom ratings 
	 */
	public Resource getCustomRatingsResource() {
		return customRatingsResource;
	}
	public void setCustomRatingsResource(Resource customRatingsResource) {
		this.customRatingsResource = customRatingsResource;
	}
	
	/**
	 * @return
	 */
	public Resource getPullRequestRatingsResource() {
		return pullRequestRatingsResource;
	}
	public void setPullRequestRatingsResource(Resource pullRequestRatingsResource) {
		this.pullRequestRatingsResource = pullRequestRatingsResource;
	}
	/**
	 * @return - the terminal as {@link Artifact}
	 */
	public Artifact getTerminalArtifact() {
		return terminalArtifact;
	}
	public void setTerminalArtifact(Artifact terminalArtifact) {
		this.terminalArtifact = terminalArtifact;
	}
	
	/**
	 * @param respectBraintribeSpecifica
	 */
	public void setRespectBraintribeSpecifica(boolean respectBraintribeSpecifica) {
		this.respectBraintribeSpecifica = respectBraintribeSpecifica;		
	}
	public boolean getRespectBraintribeSpecifica() {
		return respectBraintribeSpecifica;
	}
	
	/**
	 * @return - the {@link ConsoleOutputVerbosity}
	 */
	public ConsoleOutputVerbosity getConsoleOutputVerbosity() {
		return consoleOutputVerbosity;
	}
	public void setConsoleOutputVerbosity(ConsoleOutputVerbosity consoleOutputVerbosity) {
		this.consoleOutputVerbosity = consoleOutputVerbosity;
	}
	
	

	
}
