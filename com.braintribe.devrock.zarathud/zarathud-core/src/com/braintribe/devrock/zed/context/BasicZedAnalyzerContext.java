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
package com.braintribe.devrock.zed.context;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.zed.analyze.AsmAnalyzer;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.core.ArtifactRegistry;
import com.braintribe.devrock.zed.api.core.CachingZedRegistry;
import com.braintribe.devrock.zed.api.core.Verbosity;
import com.braintribe.devrock.zed.api.core.ZedEntityResolver;
import com.braintribe.devrock.zed.forensics.structure.DependencyStructureRegistry;
import com.braintribe.devrock.zed.registry.BasicArtifactRegistry;
import com.braintribe.devrock.zed.registry.BasicCachingZedRegistry;
import com.braintribe.devrock.zed.scan.ScannerResult;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.zarathud.model.data.Artifact;

public class BasicZedAnalyzerContext implements ZedAnalyzerContext, ZedForensicsContext {
	private Collection<AnalysisArtifact> classpath;
	private GmSession session;
	private Map<URL,Artifact> urlToArtifactMap;
	private List<String> classesToProcess;
	private List<AnalysisArtifact> additionsToClasspath;
	private Artifact terminalArtifact;
	private ScannerResult terminalScanData;
	private List<AnalysisDependency> declaredTerminalDependencies;
	private CachingZedRegistry registry;
	private ArtifactRegistry artifactRegistry;
	private ZedEntityResolver resolver;
	private URL runtimeJar;
	private URL scannedResource;
	private Predicate<Artifact> validImplicitArtifactReferencesFilter;
	
	
	private DependencyStructureRegistry structureRegistry;
	
	private URLClassLoader classLoader;
	private Verbosity verbosity = Verbosity.standard;

	@Override
	public Collection<AnalysisArtifact> classpath() {
		return classpath;
	}
	@Configurable @Required
	public void setClasspath(Collection<AnalysisArtifact> classpath) {
		this.classpath = classpath;
	}

	@Override
	public GmSession session() {
		return session;
	}
	@Configurable
	public void setSession(GmSession session) {
		this.session = session;
	}

	@Override
	public Map<URL, Artifact> urlToArtifactMap() {
		return urlToArtifactMap;
	}
	@Configurable @Required
	public void setUrlToArtifactMap(Map<URL, Artifact> urlToArtifactMap) {
		this.urlToArtifactMap = urlToArtifactMap;
	}
	
	@Override
	public Artifact terminalArtifact() {
		return terminalArtifact;
	}
	@Configurable @Required
	public void setTerminalArtifact(Artifact terminalArtifact) {
		this.terminalArtifact = terminalArtifact;
		
	}

	@Override
	public ScannerResult terminalScanData() {
		return terminalScanData;
	}
	@Configurable @Required
	public void setTerminalScanData(ScannerResult result) {
		this.terminalScanData = result;
		
	}
	@Override
	public List<String> classesToProcess() {
		return classesToProcess;
	}
	@Configurable @Required
	public void setClassesToProcess(List<String> classesToProcess) {
		this.classesToProcess = classesToProcess;
	}
	@Override
	public List<AnalysisDependency> declaredTerminalDependencies() {	
		return declaredTerminalDependencies;
	}
	@Configurable @Required
	public void setDeclaredTerminalDependencies(List<AnalysisDependency> declaredTerminalDependencies) {
		this.declaredTerminalDependencies = declaredTerminalDependencies;
	}
	
	private Object lazyRegistryLoadMonitor = new Object();
	
	@Override
	public CachingZedRegistry registry() {
		if (registry != null) {
			return registry;
		}
		synchronized ( lazyRegistryLoadMonitor) {
			if (registry != null) {
				return registry;
			}
			registry = new BasicCachingZedRegistry(); 
		}
		return registry;
	}
	@Configurable
	public void setRegistry(CachingZedRegistry artifactRegistry) {
		this.registry = artifactRegistry;
	}
	
	private Object lazyArtifactsLoadMonitor = new Object();
	
	@Override
	public ArtifactRegistry artifacts() {	
		if (artifactRegistry != null)
			return artifactRegistry;
		synchronized ( lazyArtifactsLoadMonitor) { 
			if (artifactRegistry != null)
				return artifactRegistry;
			artifactRegistry = new BasicArtifactRegistry();
		}
		return artifactRegistry;
	}
	@Configurable
	public void setArtifactRegistry(ArtifactRegistry artifactRegistry) {
		this.artifactRegistry = artifactRegistry;
	}
	
	@Override
	public URLClassLoader classloader() {	
		return classLoader;
	}
	@Configurable @Required
	public void setClassLoader(URLClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	private Object lazyResolverLoadMonitor = new Object();
	
	@Override
	public ZedEntityResolver resolver() {
		if (resolver != null)
			return resolver;
		synchronized ( lazyResolverLoadMonitor) { 
			if (resolver != null)
				return resolver;
			resolver = new AsmAnalyzer(runtimeJar);			
		}
		return resolver;
	}
	
	@Configurable
	public void setResolver(ZedEntityResolver resolver) {
		this.resolver = resolver;
	}
	@Override
	public Verbosity verbosity() {
		return verbosity;
	}
	
	@Configurable
	public void setVerbosity(Verbosity verbosity) {
		this.verbosity = verbosity;
	}
	@Override
	public URL runtimeJar() {
		return runtimeJar;
	}
	
	@Configurable
	public void setRuntimeJar(URL runtimeJar) {
		this.runtimeJar = runtimeJar;
	}
	@Override
	public void setCurrentlyScannedResource(URL scannedResource) {
		this.scannedResource = scannedResource;
	}
	@Override
	public URL currentlyScannedResource() {
		return scannedResource;
	}
	@Configurable
	public void setValidImplicitArtifactReferencesFilter(Predicate<Artifact> validImplicitArtifactReferencesFilter) {
		this.validImplicitArtifactReferencesFilter = validImplicitArtifactReferencesFilter;
	}
	@Override
	public Predicate<Artifact> validImplicitArtifactReferenceFilter() {	
		return validImplicitArtifactReferencesFilter;
	}
	@Override
	public DependencyStructureRegistry structuralRegistry() {
		return structureRegistry;
	}
	
	public void setStructuralRegistry(DependencyStructureRegistry structureRegistry) {
		this.structureRegistry = structureRegistry;
	}
	@Override
	public List<AnalysisArtifact> additionsToClasspath() {		
		return additionsToClasspath;
	}
	
	@Configurable
	public void setAdditionsToClasspath(List<AnalysisArtifact> additionsToClasspath) {
		this.additionsToClasspath = additionsToClasspath;
	}
	

}
