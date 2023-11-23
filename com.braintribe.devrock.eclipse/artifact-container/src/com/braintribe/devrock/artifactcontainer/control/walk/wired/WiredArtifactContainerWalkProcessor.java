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
package com.braintribe.devrock.artifactcontainer.control.walk.wired;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifact.walk.multi.WalkerImpl;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.container.diagnostics.ContainerClasspathDiagnosticsListener;
import com.braintribe.devrock.artifactcontainer.container.diagnostics.ContainerDiagnostics;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.monitor.WalkProgressMonitor;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerDevloaderUpdateExpert;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.listener.MalaclypseAnalysisMonitor;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.SolutionProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.container.ArtifactKind;
import com.braintribe.model.malaclypse.cfg.container.ContainerGenerationMode;
import com.braintribe.model.malaclypse.cfg.container.ContainerKind;
import com.braintribe.model.malaclypse.cfg.denotations.WalkScope;
import com.braintribe.model.malaclypse.container.ContainerPersistence;
import com.braintribe.plugin.commons.SolutionSortComparator;
import com.braintribe.plugin.commons.container.ContainerCommons;
import com.braintribe.plugin.commons.container.ContainerNatureExpert;


/**
 * a class that actually performs the actions of the {@link ArtifactContainer}
 * @author pit
 *
 */
public class WiredArtifactContainerWalkProcessor {
	private static Logger log = Logger.getLogger(WiredArtifactContainerWalkProcessor.class);
	private static final String TAG_RULE_CLASSPATH = "tagRule.classpath";
	
	private MalaclypseAnalysisMonitor malaclypseAnalysisMonitor;
	private WalkProgressMonitor walkNotificationListener; 
	private ContainerClasspathDiagnosticsListener containerClasspathDiagnosticsListener;
	private ArtifactContainerRegistry artifactContainerRegistry = ArtifactContainerPlugin.getArtifactContainerRegistry();
	private ClasspathResolverContract classpathResolverContract;

		
	@Configurable
	public void setMalaclypseAnalysisMonitor(MalaclypseAnalysisMonitor malaclypseAnalysisMonitor) {
		this.malaclypseAnalysisMonitor = malaclypseAnalysisMonitor;
	}
	
	@Configurable
	public void setContainerClasspathDiagnosticsListener( ContainerClasspathDiagnosticsListener containerClasspathDiagnosticsListener) {
		this.containerClasspathDiagnosticsListener = containerClasspathDiagnosticsListener;
	}
	
	public WiredArtifactContainerWalkProcessor() {	
	}
	public WiredArtifactContainerWalkProcessor(WalkProgressMonitor listener) {
		this.walkNotificationListener = listener;
	}
	/**
	 * initialize an artifact container 
	 */
	public void initializeCompileContainer(ArtifactContainer container, ClasspathResolverContract classpathResolverContract) {
		IJavaProject iJavaProject = container.getProject();
		ContainerPersistence containerPersistence = container.getContainerPersistence();
		IClasspathEntry[] classpathEntries;
		
		long start = System.currentTimeMillis();
		if (ArtifactContainerPlugin.isDebugActive()) {
			ArtifactContainerPlugin.log("initializing container [" + container.getId() + "] (compile) attached to [" + iJavaProject.getProject().getName() + "]");
		}
				
		// no solutions?		
		IResource pomResource = iJavaProject.getProject().findMember("pom.xml");
		// cannot access pom yet, so let Eclipse try us later.
		if (pomResource == null) {
			return;
		}
		if (containerPersistence == null) {			
			// try to load 
			containerPersistence = artifactContainerRegistry.loadPersistedContainerData( iJavaProject.getProject());			
			
			if (containerPersistence != null) {
				// check if the pom hasn't been changed in the meantime..
				String storedMd5 = containerPersistence.getMd5();
				String currentMd5 = ContainerCommons.getMd5ofResource( pomResource);
				if (ArtifactContainerPlugin.isDebugActive() && !currentMd5.equalsIgnoreCase( storedMd5)) {
					//  
					String msg = "Project [" + iJavaProject.getProject().getName() + "]'s pom has changed, you might want to resynch the project";
					ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
					ArtifactContainerPlugin.getInstance().log(status);
					
				}			
			}
											
		}		
		// nothing persisted or pom has changed
		if (containerPersistence == null) {
			if (ArtifactContainerPlugin.isDebugActive()) {
				ArtifactContainerPlugin.log( "md5 of pom [" + pomResource.getLocation().toOSString() + "] has changed or no perstisted container found, full walk required");
			}
			containerPersistence = performWalk(classpathResolverContract, container, ArtifactContainerUpdateRequestType.compile, pomResource);
			// 
			// store the compile data 
			artifactContainerRegistry.persistContainerData( container);
		}
		// something's persisted or walk has run 
		if (containerPersistence != null && containerPersistence.getCompileSolutions().size() > 0) {
			classpathEntries = buildClasspathEntries( container, containerPersistence.getCompileSolutions(), ContainerGenerationMode.standard, ArtifactContainerUpdateRequestType.compile, true);
			container.setClasspathEntries(classpathEntries);
			container.setContainerPersistence(containerPersistence);				
		}
		long end = System.currentTimeMillis();
		if (ArtifactContainerPlugin.isDebugActive()) {
			ArtifactContainerPlugin.log("done initializing container [" + container.getId() + "] (compile) attached to [" + iJavaProject.getProject().getName() + "] in [" + (end - start) + "] ms");
		}
	}
	
	/**
	 * initialize the launch classpath 	
	 */
	public void initializeLaunchContainer( ArtifactContainer container, ClasspathResolverContract classpathResolverContract, ContainerGenerationMode generationMode) {
		IJavaProject iJavaProject = container.getProject();
		ContainerPersistence containerPersistence = container.getContainerPersistence();
		IClasspathEntry[] classpathEntries;
		
		long start = System.currentTimeMillis();
		if (ArtifactContainerPlugin.isDebugActive()) {
			ArtifactContainerPlugin.log("initializing container [" + container.getId() + "] (launch) attached to [" + iJavaProject.getProject().getName() + "]");
		}
				
				
		// no solutions?		
		IResource pomResource = iJavaProject.getProject().findMember("pom.xml");
		// cannot access pom yet, so let Eclipse try us later.
		if (pomResource == null) {
			return;
		}
		if (containerPersistence == null) {			
			// try to load 
			containerPersistence = artifactContainerRegistry.loadPersistedContainerData( iJavaProject.getProject());			
			if (containerPersistence != null) {
				// check if the pom hasn't been changed in the meantime..
				String storedMd5 = containerPersistence.getMd5();
				String currentMd5 = ContainerCommons.getMd5ofResource( pomResource);
				if (!currentMd5.equalsIgnoreCase( storedMd5)) {									
					// reset the solution tuple
					containerPersistence = null;
				}
			}								
		}		
		// nothing persisted or pom has changed
		if (containerPersistence == null || containerPersistence.getRuntimeSolutions().size() == 0) {	
			containerPersistence = performWalk(classpathResolverContract, container, ArtifactContainerUpdateRequestType.launch, pomResource);
			// 
			// store the launch data 
			artifactContainerRegistry.persistContainerData( container);
		}
		// something's persisted or walk has run 
		if (containerPersistence != null && containerPersistence.getRuntimeSolutions().size() > 0) {
			classpathEntries = buildClasspathEntries( container, containerPersistence.getRuntimeSolutions(), generationMode, ArtifactContainerUpdateRequestType.launch, true);
			container.setRuntimeEntries(classpathEntries);
			// if this is a tomcat project, update the devloader file
			IProject project = container.getProject().getProject();
			if (ContainerNatureExpert.hasTomcatNature( project)){
				ArtifactContainerDevloaderUpdateExpert.updateTomcatDevloader(container);				
				// if this is a module carrier, update				
				if (ContainerNatureExpert.hasTribefireServicesNature(project)) {
					WiredModuleCarrierDevloaderUpdateExpert.updateModuleCarrierClasspath( classpathResolverContract, project, container);
				}			
			}
		}
		
		long end = System.currentTimeMillis();
		if (ArtifactContainerPlugin.isDebugActive()) {
			ArtifactContainerPlugin.log("done initializing container [" + container.getId() + "] (launch) attached to [" + iJavaProject.getProject().getName() + "] in [" + (end - start) + "] ms");
		}
	}
	
	/**
	 * process an update request - i.e. decoupled processing 	
	 */
	public void processContainer( WiredArtifactContainerUpdateRequest containerUpdateRequest) {
 		ArtifactContainer container = containerUpdateRequest.getContainer();
		switch (containerUpdateRequest.getWalkMode()) {
			case refresh: {
				if (container.getContainerPersistence() == null) {
					synchronizeContainerForCompilation(container);
				} else {
					updateContainer(container);
				}				
				break;
			}
			case compile : {
				synchronizeContainerForCompilation(container);
				break;
			}
			case launch: {
				synchronizeContainerForLaunch(container);
				break;
			}
			case combined: {
				synchronizeContainerForCompilation(container);				
				synchronizeContainerForLaunch(container);
				break;
			}
			case initLaunch: {
				initializeLaunchContainer(container, containerUpdateRequest.getClasspathResolverContract(), ContainerGenerationMode.standard);
				break;
			}
				
				

		}

	}
	
	/**
	 * synchronize container, i.e. perform a walk and build the {@link IClasspathEntry}s 
	 * @param container - the {@link ArtifactContainer} to act upon 
	 */
	public void synchronizeContainerForCompilation( ArtifactContainer container) {
		IResource pomResource = container.getProject().getProject().findMember("pom.xml");
		if (pomResource == null) {
			String msg = "cannot access the pom resource for [" + container.getProject().getProject().getName()+ "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);	
			return;
		}
		ContainerPersistence solutionTuple = performWalk(classpathResolverContract, container, ArtifactContainerUpdateRequestType.compile, pomResource);		
		if (solutionTuple != null && solutionTuple.getCompileSolutions().size() > 0) {
			IClasspathEntry[] classpathEntries = buildClasspathEntries( container, solutionTuple.getCompileSolutions(), ContainerGenerationMode.standard, ArtifactContainerUpdateRequestType.compile, true);
			container.setClasspathEntries(classpathEntries);
			container.setContainerPersistence(solutionTuple);		
		}
		
		// TODO : persist here 
		artifactContainerRegistry.persistContainerData( container);
	}
	/**
	 * synchronize container, i.e. perform a walk and build the {@link IClasspathEntry}s 
	 * @param container - the {@link ArtifactContainer} to act upon 
	 */
	public void synchronizeContainerForLaunch( ArtifactContainer container) {
		IProject project = container.getProject().getProject();
		IResource pomResource = project.findMember("pom.xml");
		if (pomResource == null) {
			String msg = "cannot access the pom resource for [" + project.getName()+ "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);	
			return;
		}
		ContainerPersistence solutionTuple = performWalk(classpathResolverContract, container, ArtifactContainerUpdateRequestType.launch, pomResource);
		if (solutionTuple != null && solutionTuple.getCompileSolutions().size() > 0) {
			IClasspathEntry[] classpathEntries = buildClasspathEntries( container, solutionTuple.getRuntimeSolutions(), ContainerGenerationMode.standard, ArtifactContainerUpdateRequestType.launch, true);
			container.setRuntimeEntries(classpathEntries);
			container.setContainerPersistence(solutionTuple);
			// if this is a a tomcat project, update the devloader file 
			if (ContainerNatureExpert.hasTomcatNature( project)){
				ArtifactContainerDevloaderUpdateExpert.updateTomcatDevloader(container);
				// if this is a module carrier, update				
				if (ContainerNatureExpert.hasTribefireServicesNature(project)) {
					WiredModuleCarrierDevloaderUpdateExpert.updateModuleCarrierClasspath( classpathResolverContract, project, container);
				}				
			}
		}
		// TODO : persist here
		artifactContainerRegistry.persistContainerData( container);
	}
	

	/**
	 * update the container, i.e. only build the {@link IClasspathEntry}	
	 */
	public void updateContainer( ArtifactContainer container) {
		IProject project = container.getProject().getProject();

		ContainerPersistence solutionTuple = container.getContainerPersistence();
		if (solutionTuple != null) { 
	
			if (solutionTuple.getCompileSolutions().size() > 0) {
				IClasspathEntry[] classpathEntries = buildClasspathEntries( container, solutionTuple.getCompileSolutions(), ContainerGenerationMode.standard, ArtifactContainerUpdateRequestType.compile, true);
				container.setClasspathEntries(classpathEntries);
				// if this is a a tomcat project, update the devloader file 
			} else {
				ArtifactContainerStatus status = new ArtifactContainerStatus("no classpath entries found for container [" + container.getId() + "] of [" + project.getName() + "]", IStatus.WARNING);
				ArtifactContainerPlugin.getInstance().log(status);
			}
			IClasspathEntry[] runtimeEntries = null;
			if (ContainerNatureExpert.hasTomcatNature( project)) {
				if (solutionTuple != null && solutionTuple.getRuntimeSolutions().size()>0) {
					runtimeEntries = buildClasspathEntries( container, solutionTuple.getRuntimeSolutions(), ContainerGenerationMode.standard, ArtifactContainerUpdateRequestType.launch, true);
					container.setRuntimeEntries( runtimeEntries);
					ArtifactContainerDevloaderUpdateExpert.updateTomcatDevloader(container);
					
					if (ContainerNatureExpert.hasTribefireServicesNature(project)) {
						container.setRuntimeEntries( runtimeEntries);
						WiredModuleCarrierDevloaderUpdateExpert.updateModuleCarrierClasspath(classpathResolverContract, project, container);
					}
				}
				else {
					ArtifactContainerStatus status = new ArtifactContainerStatus("no runtime entries found for container [" + container.getId() + "] of project [" + project.getName() + "]", IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);
				}
			}
			
			
			container.setContainerPersistence(solutionTuple);			
		}				
	}
	
	
	private MalaclypseAnalysisMonitor getMalaclypseAnalysisMonitor() {
		if (malaclypseAnalysisMonitor == null) {
			malaclypseAnalysisMonitor = new MalaclypseAnalysisMonitor("unidentified - created from [" + WiredArtifactContainerWalkProcessor.class.getName() + "]");
		}
		return malaclypseAnalysisMonitor;
	}

	/**
	 * actually perform a walk 	
	 */
	private ContainerPersistence performWalk(ClasspathResolverContract classpathResolverContract, ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode, IResource pomResource) {
		
		IClasspathEntry[] classpathEntries;
		// perform the walk		
		String walkId = UUID.randomUUID().toString();
				

		String pom = pomResource.getLocation().toOSString();
		Solution solution;
		IProject project = container.getProject().getProject();
		try {
			ArtifactPomReader pomReader = classpathResolverContract.pomReader();		
			solution = pomReader.read( walkId, pom);
		} catch (PomReaderException e) {
			String msg="cannot read pom [" + pom + "] to determine artifact representation of project [" + project.getName() + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR,e);
			ArtifactContainerPlugin.getInstance().log(status);	
			classpathEntries = new IClasspathEntry[0];
			container.setClasspathEntries(classpathEntries);				
			return null;
		}
		ContainerPersistence containerPersistence = container.getContainerPersistence();
		if (containerPersistence == null) {
			containerPersistence = ContainerPersistence.T.create();
			container.setContainerPersistence(containerPersistence);
		}
		containerPersistence.setTimestamp( new Date());
		containerPersistence.setMd5( ContainerCommons.getMd5ofResource( pomResource));
		containerPersistence.setTerminal(solution);			
				
		// compile walk
		switch (walkMode) {
			case compile: {
				Collection<Solution> compileWalkResult = performWalkOn( walkId, container, solution, classpathResolverContract, WalkScope.compile);
				containerPersistence.getCompileSolutions().clear();
				if (compileWalkResult != null) {
					containerPersistence.getCompileSolutions().addAll(compileWalkResult);
				} 
				else {
					String msg = "no result returned for compile on [" + NameParser.buildName(solution) + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);			
				}
				break;
			}
			case launch : {
				Collection<Solution> launchWalkResult = performWalkOn(walkId, container, solution, classpathResolverContract, WalkScope.launch);
				containerPersistence.getRuntimeSolutions().clear();
				if (launchWalkResult != null) {
					containerPersistence.getRuntimeSolutions().addAll(launchWalkResult);
				}
				else {
					String msg = "no result returned for launch on [" + NameParser.buildName(solution) + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);		
				}			
				break;
			}
			default:
				break;
		}
 			
		return containerPersistence;
	}
	
	/**
	 * perform a walk on the solution 
	 * @param solution - the {@link Solution} that represents the terminal
	 * @param classpathResolverContract - the contract from {@link MalaclypseWirings} 
	 * @param walkScope - the current scope of the walk, compile or launch
	 * @return - a {@link Collection} of the collected {@link Solution}
	 */
	private List<Solution> performWalkOn(String walkId, ArtifactContainer container, Solution solution, ClasspathResolverContract classpathResolverContract, WalkScope walkScope) {	

		WalkerImpl walker = null;
		IProject project = container.getProject().getProject();
		WalkerContext context = MalaclypseWirings.compileWalkContext();
		String tagRule = getTagruleFromPom(solution, TAG_RULE_CLASSPATH);
		context.setTagRule(tagRule);
		context.setAbortOnUnresolvedDependency( false);
		
		try {
			walker = (WalkerImpl) classpathResolverContract.walker(context);			
		} catch (RuntimeException e) {
			String msg="cannot retrieve a walker instance for project [" + project.getName() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		}		
		try {
			MalaclypseAnalysisMonitor monitor = getMalaclypseAnalysisMonitor();
			walker.addListener(monitor);
			if (walkNotificationListener != null) {
				walker.addListener(walkNotificationListener);
				walker.setAbortSignaller(walkNotificationListener);
			}
			walker.wireListeners();
		
			Collection<Solution> walkResult = walker.walk(walkId, solution);
			
			walker.unwireListeners(Collections.singleton(monitor));
			
			attachMonitor(walkId, monitor, walkScope, project);
			//			
			List<Solution> result = new ArrayList<Solution>( walkResult);
			Collections.sort(result, new SolutionSortComparator());
			return result;
		} catch (WalkException e) {
			String msg="cannot perform walk on project [" + project.getName() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		}
	}
	
	private static String getTagruleFromPom( Solution solution, String key) {
		Set<Property> properties = solution.getProperties();
		if (properties.isEmpty()) {
			return null;
		}
		for (Property property : properties) {
			if (property.getName().equalsIgnoreCase(key)) {
				if (property.getValue() != null) {
					return property.getValue();
				}
				else {
					return property.getRawValue();
				}
			}
		}
		return null;
	}
	private void attachMonitor(String walkId, MalaclypseAnalysisMonitor monitor, WalkScope walkMode, IProject project) {
		WalkMonitoringResult walkMonitoringResult = monitor.getWalkMonitoringResult( walkId);
		String msg = "project " + project.getName() + " is linked to " + NameParser.buildName(walkMonitoringResult.getTerminal()) + " for [" + walkMode.toString() + "]";		
		System.out.println( msg);
		
		switch (walkMode) {
			case compile:
				artifactContainerRegistry.updateCompileWalkMonitorResult(project, walkMonitoringResult);
				// auto store
				//plugin.getArtifactContainerRegistry().persistWalkMonitorResult(project, walkMonitoringResult, ContainerMode.compile);
				break;	
			case launch:
				artifactContainerRegistry.updateRuntimeWalkMonitorResult(project, walkMonitoringResult);
				// auto store 
				//plugin.getArtifactContainerRegistry().persistWalkMonitorResult(project, walkMonitoringResult, ContainerMode.runtime);
				break;
			default:
				break;		
		}
		// dump now
	}
	
	/**
	 * generate the classpath entries for eclipse 
	 * @param container - the {@link ArtifactContainer} we do it for 
	 * @param solutions - the {@link List} of {@link Solution} to use (might be the result of either compile or launch)
	 * @param mode - the {@link ContainerGenerationMode} : standard, combined or gwtonly 
	 * @param updateWorkspaceController - whether to supply the {@link WorkspaceProjectRegistry} with dependency information	 * 
	 */
	private IClasspathEntry [] buildClasspathEntries(ArtifactContainer container,  List<Solution> solutionsToProcess, ContainerGenerationMode mode, ArtifactContainerUpdateRequestType requestType, boolean updateWorkspaceController) {
		ContainerKind containerKind = container.getConfiguration().getContainerKind();
		if (walkNotificationListener != null) {
			walkNotificationListener.acknowledgeClasspathBuilding();
		}
		// prime container classpath diagnostics listener .. 
		if (containerClasspathDiagnosticsListener != null) {
			containerClasspathDiagnosticsListener.acknowledgeContainerProcessingStart( container, requestType);
		}
		
		// prime workspace registry
		if (updateWorkspaceController) {
			ArtifactContainerPlugin.getWorkspaceProjectRegistry().clearDependencies( container.getProject().getProject());
		}
		// prime container dependencies
		container.getDependencies().clear();
		
		List<Solution> solutions = new ArrayList<>( solutionsToProcess);
		
		List<IClasspathEntry> generatedEntries = new ArrayList<IClasspathEntry>( solutions.size());		
		for (Solution solution : solutions) {
			// 
			if (solution == null || solution.getAggregator()) {
				continue;
			}
			if (containerKind != ContainerKind.staticContainer) {
				IJavaProject project = getProjectOfSolution(solution);
				if (project != null) {
					if (walkNotificationListener != null) {
						walkNotificationListener.acknowledgeProjectClasspathEntry(solution);
					}
					generatedEntries.addAll( generateEntriesForJavaProject( container, project, solution, mode, requestType, updateWorkspaceController));					
				}
				else {
					if (walkNotificationListener != null) {
						walkNotificationListener.acknowledgeJarClasspathEntry(solution);
					}
					generatedEntries.addAll( generateEntriesForJar( container, solution, mode, requestType));
				}
			}
			
			// always add the jar (if there's one, fine, if not, so what)  
			Part jarPart = SolutionProcessor.getPart(solution, PartTupleProcessor.createJarPartTuple());
			if (jarPart != null) {
				artifactContainerRegistry.relateJarToArtifact(jarPart.getLocation(), solution);
			}
		}
		// prime container classpath diagnostics listener .. 
		if (containerClasspathDiagnosticsListener != null) {
			containerClasspathDiagnosticsListener.acknowledgeContainerProcessingEnd( container, requestType);
		}
		return generatedEntries.toArray( new IClasspathEntry[0]);
	}
	
	/**
	 * asks the {@link WorkspaceProjectRegistry} whether the solution passed is an accessible project 
	 * @param solution - the {@link Solution} we look for 
	 * @return - {@link IJavaProject} if it is there and is accessible, null otherwise
	 */
	private IJavaProject getProjectOfSolution( Solution solution) {
		IProject project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(solution);
		if (
				project != null && 
				project.isAccessible()
			) {
			return JavaCore.create(project);
		}
		log.debug("no project found that could stand in for [" + NameParser.buildName(solution));
		return null;
	}
	
	/**
	 * generate all relevant {@link IClasspathEntry} for a java project (i.e. a dependency that is represented by a {@link IProject} in the workspace)
	 * @param container - the {@link ArtifactContainer} we do it for 
	 * @param project - the {@link IJavaProject} that represents the dependency 
	 * @param solution - the {@link Solution} that represents the {@link Dependency} 
	 * @param mode - the {@link ContainerGenerationMode} - standard, combined or gwtonly
	 * @param updateWorkspaceController - whether to update the workspace controller 
	 * @return - a {@link List} of {@link IClasspathEntry}
	 */
	private List<IClasspathEntry> generateEntriesForJavaProject(ArtifactContainer container, IJavaProject project, Solution solution, ContainerGenerationMode mode, ArtifactContainerUpdateRequestType requestType, boolean updateWorkspaceController) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
		// if the solution is not expected to have a jar, it doesn't require a project entry either.. 
		if (!ContainerDiagnostics.relevantForClasspathCheck( container, requestType, solution, containerClasspathDiagnosticsListener)) {
				return result;
		}
		ArtifactKind artifactKind = container.getConfiguration().getArtifactKind();
		if (
				mode == ContainerGenerationMode.standard ||
				mode == ContainerGenerationMode.combined							
			) {
			IClasspathAttribute solutionTag = JavaCore.newClasspathAttribute("solution", NameParser.buildName(solution));
			IProject dependencyProject = project.getProject();
			IClasspathEntry entry = JavaCore.newProjectEntry( dependencyProject.getFullPath(),  new IAccessRule[0],  false,  new IClasspathAttribute[] { solutionTag},  false);
			result.add( entry);
			if (updateWorkspaceController) {
				IJavaProject containerJavaProject = container.getProject();
				ArtifactContainerPlugin.getWorkspaceProjectRegistry().addProjectDependency( containerJavaProject.getProject(), solution, dependencyProject);
			}
			container.getDependencies().add(dependencyProject);
		}
		if (
				mode == ContainerGenerationMode.combined ||
				mode == ContainerGenerationMode.gwtonly
			) {
			switch (artifactKind) {
				case gwtTerminal:
				case gwtLibrary:
				case model: {
					result.addAll( generateSourceEntriesForJavaProject( project));
					break;
				}								
				default:
					break;
			}			
		}
		return result;
	}
	
	/**
	 * generate a source typed {@link IClasspathEntry}
	 * @param project - the {@link IJavaProject} that we generate the source entry for 
	 * @return - a {@link IClasspathEntry} properly configured 
	 */
	private List<IClasspathEntry> generateSourceEntriesForJavaProject( IJavaProject project) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
		try {						
			IClasspathEntry[] rawEntries = project.getRawClasspath();
			for (IClasspathEntry raw : rawEntries) {
				if (raw.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					//
					IProject iProject = project.getProject();
					String fullpath = iProject.getFullPath().toOSString();
					String sourcePath = raw.getPath().toOSString();
					String path = fullpath.substring( 0, fullpath.indexOf( iProject.getName())-1) + sourcePath;
										
					IClasspathEntry source_entry = JavaCore.newLibraryEntry( new Path( path), new Path( path), null, new IAccessRule[0], new IClasspathAttribute[0], false);
					result.add( source_entry);		
				}
			}					
		} catch (JavaModelException e) {
			String msg = "cannot retrieve source folder information for [" + project.getElementName() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}	
		return result;
	}
	
	/**
	 * generate all relevant {@link IClasspathEntry}s for a jar typed entry 
	 * @param container - the {@link ArtifactContainer} we create the entries for
	 * @param solution - the {@link Solution} that reflects the jar 
	 * @param mode - {@link ContainerGenerationMode} that determines what {@link IClasspathEntry} are required 
	 */
	private List<IClasspathEntry> generateEntriesForJar( ArtifactContainer container, Solution solution, ContainerGenerationMode mode, ArtifactContainerUpdateRequestType requestType) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();		
		// if the solution is not expected to have a jar, don't add one. 
		if (!ContainerDiagnostics.relevantForClasspathCheck( container, requestType, solution, containerClasspathDiagnosticsListener)) {			
			return result;
		}
		// standard & combined - for standard modes and gwt (combined)
		if (
				mode == ContainerGenerationMode.standard ||
				mode == ContainerGenerationMode.combined							
			) {
			
			Part jarPart = null;
			PartTuple jarPartTuple;
			if (ContainerDiagnostics.hasClassesClassifier(solution)) {
				jarPartTuple = PartTupleProcessor.fromString("classes", "jar");
			}
			else {
				String classifier = ContainerDiagnostics.hasAnyClassifier( solution);
				if (classifier == null || classifier.length() == 0) {
					jarPartTuple = PartTupleProcessor.createJarPartTuple();
				}
				else {
					jarPartTuple = PartTupleProcessor.fromString( classifier, "jar");
				}
			}
			jarPart = SolutionProcessor.getPart(solution, jarPartTuple);
			String solutionName = NameParser.buildName(solution);
			if (jarPart == null || jarPart.getLocation() == null) {
				String msg="no jar found for [" + solutionName + "], describing tuple [" + PartTupleProcessor.toString(jarPartTuple) + "]"; 				
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);				
				// check on pom? why is that? -> derives that the container requires a walk 
			}
			else {
				// 
				// jar found 
				IClasspathEntry entry;
				Part sourcesPart = SolutionProcessor.getPart(solution,  PartTupleProcessor.create( PartType.SOURCES));
				Part javadocPart = SolutionProcessor.getPartLike( solution, PartType.JAVADOC);
				
				List<IClasspathAttribute> attributes = new ArrayList<IClasspathAttribute>();
				// solution tag 
				IClasspathAttribute solutionTag = JavaCore.newClasspathAttribute("solution", solutionName);
				attributes.add( solutionTag);
				
			
				
				// javadoc - is an URL and not a simple file path
			
				if (
						(javadocPart != null) &&
						(javadocPart.getLocation() != null)
					) {
						String location = javadocPart.getLocation();
						File javadoc = new File( location);
						if (javadoc.exists()) {
							try {
								URL url = javadoc.toURI().toURL();
								attributes.add( JavaCore.newClasspathAttribute( IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, url.toString()));
							} catch (MalformedURLException e) {
								String msg="cannot build proper URL for javadoc  [" + location + "]"; 				
								ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
								ArtifactContainerPlugin.getInstance().log(status);				
							}	
						}
				}
				
				// sources 
				if (
						(sourcesPart != null) &&
						(sourcesPart.getLocation() != null) 					
					) {
					entry = JavaCore.newLibraryEntry( new Path( jarPart.getLocation()) , new Path( sourcesPart.getLocation()), null, new IAccessRule[0], attributes.toArray( new IClasspathAttribute[0]), false);
					
				} else {
					entry = JavaCore.newLibraryEntry( new Path( jarPart.getLocation()) , null, null, new IAccessRule[0],  new IClasspathAttribute[0], false);
				}
				result.add(entry);
			}
		} // standard || combined

		// for combined (or gwtonly) 
		if (
				mode == ContainerGenerationMode.combined ||
				mode == ContainerGenerationMode.gwtonly
			){
				ArtifactKind artifactKind = container.getConfiguration().getArtifactKind();
				switch (artifactKind) {
					case gwtTerminal:
					case gwtLibrary:
					case model: {
						// sources only
						Part sourcesPart = SolutionProcessor.getPart(solution,  PartTupleProcessor.create( PartType.SOURCES));
						if (
								(sourcesPart != null) &&
								(sourcesPart.getLocation() != null) 					
							) {
							IClasspathEntry entry = JavaCore.newLibraryEntry( new Path( sourcesPart.getLocation()) , null, null, new IAccessRule[0], new IClasspathAttribute[0], false);
							result.add( entry);
						}						
						break;
					}								
					default:
						break;
				}			
		}
		
		return result;
	}
	
	public static WalkMonitoringResult getMonitorResultOn(String walkId, ClasspathResolverContract contract, Solution solution, WalkScope walkScope) throws WalkException {

		WalkerContext context;
			switch( walkScope) {
			case launch:
				context = MalaclypseWirings.runtimeWalkContext();
				break;
			case compile :
			default:
				context = MalaclypseWirings.compileWalkContext();
				break;
			}
			
			WalkerImpl walker = null;

			String solutionName = NameParser.buildName(solution);
			try {
				walker = (WalkerImpl) contract.walker(context);	
				String tagRule = getTagruleFromPom(solution, TAG_RULE_CLASSPATH);
				walker.setTagRule( tagRule);
				// AC is lenient 
				walker.setAbortIfUnresolvedDependencyIsFound(false);
 
			} catch (RuntimeException e) {
				String msg="cannot retrieve a walker instance for project [" + solutionName + "]";			
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
				ArtifactContainerPlugin.getInstance().log(status);	
				return null;
			}		
			try {
				MalaclypseAnalysisMonitor monitor = new MalaclypseAnalysisMonitor("unattached - created for [" + solutionName + "]");
				
				walker.addListener(monitor);
				
				walker.wireListeners();
			
				walker.walk(walkId, solution);
				
				walker.unwireListeners(Collections.singleton(monitor));
				
				WalkMonitoringResult walkMonitoringResult = monitor.getWalkMonitoringResult( walkId);
				//						
				return walkMonitoringResult;
			} catch (WalkException e) {
				String msg="cannot perform walk on project [" + solutionName + "]";			
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
				ArtifactContainerPlugin.getInstance().log(status);	
				throw e;
			}						
	}

	public ClasspathResolverContract getClasspathResolverContract() {
		return classpathResolverContract;
	}

	public void setClasspathResolverContract(ClasspathResolverContract classpathResolverContract) {
		this.classpathResolverContract = classpathResolverContract;
	}
}
