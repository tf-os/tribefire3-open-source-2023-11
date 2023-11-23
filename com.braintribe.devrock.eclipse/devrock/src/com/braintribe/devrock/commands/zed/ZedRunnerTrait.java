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
package com.braintribe.devrock.commands.zed;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.identification.PomIdentificationHelper;
import com.braintribe.devrock.api.nature.CommonNatureIds;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.project.DerivedJavaProjectData;
import com.braintribe.devrock.api.project.JavaProjectDataExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectInfo;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.mc.reason.InvalidArtifactIdentification;
import com.braintribe.devrock.model.mc.reason.UnresolvedArtifact;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependency;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.devrock.zed.ui.ZedResultViewer;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;

/**
 * collected functionality for zed within Eclipse
 * @author pit
 */
public interface ZedRunnerTrait {
	 static Logger log = Logger.getLogger(ZedRunnerTrait.class);
	
	/**
	 * prop and run zed's analysis (maybe on a project)
	 * @param artifactName - the name of the artifact
	 * @param zedWireRunner - the correctly configured {@link ZedWireRunner}
	 * @param project - an optional project (null if it's run on an artifact rather than an eclipse project)
	 */
	public static void displayZedAnalysisResult(String artifactName, ZedWireRunner zedWireRunner, IProject project) {
		Display display = PlatformUI.getWorkbench().getDisplay();					
		
		Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> analysisDataMaybe = zedWireRunner.collectedForensics();
		
		if (analysisDataMaybe.isEmpty()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Analysis failed", (Reason) analysisDataMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return;
		}
		
		Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>  analysisData = analysisDataMaybe.value();			
		DevrockPluginStatus status = new DevrockPluginStatus("Rating for project [" + artifactName + "]: " + analysisData.first.toString(), IStatus.INFO);
		DevrockPlugin.instance().log(status);
		
		// retrieve forensic data 
		Artifact analyzedArtifact = zedWireRunner.analyzedArtifact();
		DependencyForensicsResult dependencyForensicsResult = zedWireRunner.dependencyForensicsResult();
		ClasspathForensicsResult classpathForensicsResult = zedWireRunner.classpathForensicsResult();
		ModelForensicsResult modelForensicsResult = zedWireRunner.modelForensicsResult();
		ModuleForensicsResult moduleForensicsResult = zedWireRunner.moduleForensicsResult();
		RatingRegistry activeRatingsRegistry = zedWireRunner.ratingRegistry();
		
		// build context for displaying
		ZedViewingContext viewingContext = new ZedViewingContext();
		
		// gm compatible 
		viewingContext.setArtifact(analyzedArtifact);
		viewingContext.setDependencyForensicsResult(dependencyForensicsResult);
		viewingContext.setClasspathForensicsResult(classpathForensicsResult);
		viewingContext.setModelForensicsResult(modelForensicsResult);
		viewingContext.setModuleForensicsResult(moduleForensicsResult);
		viewingContext.setWorstRating( analysisData.first);
		viewingContext.setIssues( analysisData.second);
		
		// gm non-compatible
		viewingContext.setRatingRegistry( activeRatingsRegistry);				
		viewingContext.setProject(project);
		
		Reason mainReasonAsReturnedByZed = analysisDataMaybe.whyUnsatisfied();
		viewingContext.setAnalyzerReturnReason(mainReasonAsReturnedByZed);
		
		// transpose context to storable construct...
		
		
		// open dialog  
		final Shell shell = display.getActiveShell();
		ZedResultViewer resultViewer = new ZedResultViewer( shell);
		resultViewer.setContext(viewingContext);
		resultViewer.open();
	}
	
	default void displayComparisonResult( Artifact base, Artifact other) {
		
	}
	
	/**
	 * fully process a project 
	 * @param project - the project to handle
	 * @throws CoreException
	 */
	public static void process(IProject project) throws CoreException {
		
		Maybe<ClassesProcessingRunnerContext> cprContextMaybe = produceContext(project);
		
		if (cprContextMaybe.isEmpty()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Cannot process project : " + project.getName(), (Reason) cprContextMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return;
		}
		ClassesProcessingRunnerContext cprContext = cprContextMaybe.get();
				
		// c) run												
		try (WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE)) {																			 
			
			Job job = new WorkbenchJob("running zed's analysis") {						
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {					
					ZedWireRunner zedWireRunner = ZedRunnerTrait.runExtraction(cprContext);
					displayZedAnalysisResult(project.getName(), zedWireRunner, project);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	/**
	 * simply run an extraction on the context
	 * @param cprContext - the {@link ClassesProcessingRunnerContext}
	 * @return - the {@link ZedWireRunner} with all data accumulated
	 */
	public static ZedWireRunner runExtraction(ClassesProcessingRunnerContext cprContext) {
		try (WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE)) {																
			ZedWireRunner zedWireRunner = wireContext.contract().classesRunner(cprContext);						
			zedWireRunner.run();															
			return zedWireRunner;			
		}
		
	}
	
	/**
	 * produce a {@link ClassesProcessingRunnerContext} based on a {@link IProject}
	 * @param project - the {@link IProject}
	 * @return - a {@link Maybe} containing the {@link ClassesProcessingRunnerContext}
	 */
	public static Maybe<ClassesProcessingRunnerContext> produceContext(IProject project) {
		
		if (!NatureHelper.hasAnyNatureOf(project, JavaCore.NATURE_ID)) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] doesn't have a Java nature and cannot be processed", IStatus.CANCEL);
			DevrockPlugin.instance().log(status);
			return null;
		}
		
		// build context
		IJavaProject javaProject = JavaCore.create(project);								
		ClassesProcessingRunnerContext cprContext = ClassesProcessingRunnerContext.T.create();

		// identify project - get the pom, get minimal identification
		Maybe<CompiledArtifactIdentification> maybe = PomIdentificationHelper.identifyProject(project);
		if (maybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] cannot be identified", (Reason) maybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return maybe.cast();
		}
		CompiledArtifactIdentification cai = maybe.get();
		cprContext.setTerminal(cai.asString());
		
		// 
		Maybe<CompiledArtifact> terminalCaMaybe = DevrockPlugin.mcBridge().resolve(cai);
		if (terminalCaMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "]'s backing pom cannot be compiled", (Reason) terminalCaMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return maybe.cast();
		}
		
		CompiledArtifact ca = terminalCaMaybe.get();
		
		List<AnalysisDependency> terminalDependencies = ca.getDependencies().stream().map( cd -> AnalysisDependency.from(cd)).collect(Collectors.toList());
		cprContext.setDependencies(terminalDependencies);
		
						
		// TODO: there are now two disjunct folders - the main folder + the ARB folder
		// classes directory of terminal project 
		Maybe<DerivedJavaProjectData> pdataMaybe = JavaProjectDataExtracter.getRelevantOutputLocationsOfProject( javaProject.getProject());
		
		
		if (pdataMaybe.isEmpty()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] cannot be processed", (Reason) pdataMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return pdataMaybe.cast();
		}
		DerivedJavaProjectData pdata = pdataMaybe.get();
		cprContext.setNonpackedSolutionsOfClasspath(null);
		
		// collect all class directories 
		List<String> files = new ArrayList<>();
		files.add( pdata.outputFolder.getAbsolutePath());
		files.addAll( pdata.exportedFolders.stream().map( f -> f.getAbsolutePath()).collect(Collectors.toList()));
		cprContext.setTerminalClassesDirectoryNames( files);
		
		CompiledTerminal ct = CompiledTerminal.from(cai);
		Maybe<AnalysisArtifactResolution> resolutionMaybe = DevrockPlugin.mcBridge().resolveClasspath( ct, ClasspathResolutionScope.compile);
		if (resolutionMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] cannot be processed", (Reason) resolutionMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return resolutionMaybe.cast();
		}
		
		Map<String, AnalysisArtifact> map = new HashMap<>();				
		List<AnalysisArtifact> list = new ArrayList<>();

		Maybe<RepositoryReflection> reflectRepositoryConfigurationMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
		if (reflectRepositoryConfigurationMaybe.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] cannot be processed", (Reason) reflectRepositoryConfigurationMaybe.whyUnsatisfied());
			DevrockPlugin.instance().log(status);
			return reflectRepositoryConfigurationMaybe.cast();
		}
		RepositoryReflection reflection = reflectRepositoryConfigurationMaybe.get();
		

		AnalysisArtifactResolution resolution = resolutionMaybe.get();
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		boolean isDebugModuleArtifact = NatureHelper.hasAnyNatureOf(javaProject.getProject(), CommonNatureIds.NATURE_DEBUG_MODULE);
		
		for (AnalysisArtifact analysisArtifact : resolution.getSolutions()) {
			// as the terminal is turned into a dependency, it must be removed from the classpath
			if (cai.compareTo( analysisArtifact) == 0) {
				continue;
			}
			// 
			Part pomPart = analysisArtifact.getParts().get(":pom");
			WorkspaceProjectInfo projectInfo = workspaceProjectView.getProjectInfo( analysisArtifact);
			
			if (projectInfo == null) { // if no direct match's found .. 
				// only modify version to find 'non-matching' in workspace view if artifact's dependency is singleton and not-ranged.			

				Set<AnalysisDependency> dependers = analysisArtifact.getDependers();
				if (dependers.size() == 1) {
					AnalysisDependency depender = dependers.stream().findFirst().get();
					String versionAsString = depender.getVersion();
					VersionExpression ve = VersionExpression.parse(versionAsString);
					if (ve instanceof Version) {
						projectInfo = workspaceProjectView.getAutoRangedProjectInfo(analysisArtifact);
						// write a message to the log here 
						if (projectInfo != null) {
							VersionedArtifactIdentification vai = projectInfo.getVersionedArtifactIdentification();
							AnalysisArtifact ownerArtifact = depender.getDepender();
							String msg = "Debug module project '"+ ownerArtifact.asString() + "': requested '" + depender.asString() + "', best match '" + vai.asString() + "'";
							DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.WARNING);
							DevrockPlugin.instance().log(status);
						}
					}
				}
								
			} // projectInfo == null
			
			
			if (projectInfo != null && pomPart != null) {				
				String repositoryOrigin = pomPart.getRepositoryOrigin();
				Repository origin = reflection.getRepository(repositoryOrigin);				
				boolean resolvedFromWorkspace = (origin instanceof WorkspaceRepository);
				IProject associatedProject = projectInfo.getProject();
				boolean accessible = associatedProject.isAccessible();
				
				try {
					if (!associatedProject.hasNature(JavaCore.NATURE_ID)) {
						DevrockPluginStatus status = new DevrockPluginStatus("Project [" + associatedProject.getName() + "] doesn't have a Java nature and cannot be processed. Skipped", IStatus.CANCEL);
						DevrockPlugin.instance().log(status);
						continue;
					}
				} catch (CoreException e) {
					DevrockPluginStatus status = new DevrockPluginStatus("Cannot access project [" + associatedProject.getName() + "]'s nature and hence it cannot be processed. Skipped", IStatus.CANCEL);
					DevrockPlugin.instance().log(status);
					continue;
				}	
				
				IJavaProject associatedJavaProject = JavaCore.create(associatedProject);
				
				// build context
				
				if ( resolvedFromWorkspace || (accessible && isDebugModuleArtifact)) {							
					// add to map
					//log.debug( "artifact  [" + associatedProject.getName() + "]'s dependency [" + analysisArtifact.asString() + "] has been resolved via the workspace repo");
					
					/*
					Maybe<List<File>> outputLocationsMaybe = JavaProjectDataExtracter.getExportedDirectories(associatedJavaProject);
					if (outputLocationsMaybe.isUnsatisfied()) {
						ZedPluginStatus status = new ZedPluginStatus("Cannot retrieve output locations of project [" + associatedProject.getName() + "], skipped", IStatus.CANCEL);
						ZedPlugin.instance().log(status);
						continue;
					}
					List<File> outputLocations = outputLocationsMaybe.get();
					*/
					// TODO: there are now two disjunct folders - the main folder + the ARB folder
					// output folder of project reference							
					Maybe<File> outputFolderMaybe = JavaProjectDataExtracter.getOutputlocationOfProject(associatedJavaProject);
					if (outputFolderMaybe.isEmpty()) {
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot determine output folder of project [" + associatedProject.getName() + "]'s nature and hence it cannot be processed. Skipped", IStatus.CANCEL);
						DevrockPlugin.instance().log(status);
						continue;
					}
					File outputFolder = outputFolderMaybe.get();
					map.put( outputFolder.getAbsolutePath(), analysisArtifact);																					
				}					
				// 
				if (!resolvedFromWorkspace && accessible) {
					// add to map
					//log.debug( "artifact  [" + javaProject.getProject().getProject().getName() + "]'s dependency [" + analysisArtifact.asString() + "] hasn't been resolved via the workspace repo, yet is taken as it fits the requirement");
					Maybe<File> outputFolderMaybe = JavaProjectDataExtracter.getOutputlocationOfProject(associatedJavaProject);
					if (outputFolderMaybe.isEmpty()) {
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot determine output folder of project [" + associatedProject.getName() + "]'s nature and hence it cannot be processed. Skipped", IStatus.CANCEL);
						DevrockPlugin.instance().log(status);
						continue;
					}
					File outputFolder = outputFolderMaybe.get();
					map.put( outputFolder.getAbsolutePath(), analysisArtifact);				
				}						
			}
			else {
				// add to classpath list
				list.add(analysisArtifact);		
			}	
		
		} // 
	
		
		cprContext.setClasspath(list);
		cprContext.setNonpackedSolutionsOfClasspath( map);
		cprContext.setConsoleOutputVerbosity(ConsoleOutputVerbosity.silent);				
		
		// custom finger print ratings		
		if (DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_FP_OVERRIDE_RATINGS, false)) {
			String customFingerPrintFile = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE, null);
			if (customFingerPrintFile != null) {
				File file = new File(customFingerPrintFile);
				if (file.exists()) {
					FileResource fileResource  = FileResource.T.create();
					fileResource.setPath( file.getAbsolutePath());
					cprContext.setCustomRatingsResource(fileResource);
				}
			}
		}
				
		// finger print ratings attached to artifact  
		
		String partName = "fingerprints.yaml";
		IResource resource = project.findMember(partName);
		if (resource != null) {
			File file = new File(resource.getLocation().toOSString());		
			if (file.exists()) {
				FileResource fileResource  = FileResource.T.create();
				fileResource.setPath( file.getAbsolutePath());
				cprContext.setPullRequestRatingsResource(fileResource);
			}
		}
			
		return Maybe.complete( cprContext);
	}
	/**
	 * @param customRepositoryConfigurationMaybe
	 * @return - a configured {@link McBridge} without (!) the workspace repository
	 */
	public static McBridge produceMcBridgeForExtraction(Maybe<RepositoryConfiguration> customRepositoryConfigurationMaybe) {
		// inject custom repository configuration
		McBridge mcBridge = null;
		
		if ( customRepositoryConfigurationMaybe.isSatisfied()) {
			mcBridge = DevrockPlugin.mcBridge().customBridge( customRepositoryConfigurationMaybe.get());
		}	
		else {
			Maybe<RepositoryReflection> internalRepositoryConfigurationMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
			if (internalRepositoryConfigurationMaybe.isSatisfied()) {
				RepositoryConfiguration repositoryConfiguration = internalRepositoryConfigurationMaybe.get().getRepositoryConfiguration();
				RepositoryConfiguration clonedRepositoryConfiguration = repositoryConfiguration.clone(new StandardCloningContext());
				Iterator<Repository> iterator = clonedRepositoryConfiguration.getRepositories().iterator();
				while (iterator.hasNext()) {
					Repository repository = iterator.next();
					if (repository instanceof WorkspaceRepository) {
						iterator.remove();
					}
				}
				mcBridge = DevrockPlugin.mcBridge().customBridge( clonedRepositoryConfiguration);
			}
			else {
				String msg = "cannot retrieve (and patch) locally active repository configuration as " + internalRepositoryConfigurationMaybe.whyUnsatisfied().stringify();
				log.error(msg);
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
				DevrockPlugin.instance().log(status);
				return null;
			}
		}
		return mcBridge;
	}
	
	/**
	 * produce a {@link ClassesProcessingRunnerContext} for a remote artifact 
	 * @param compiledTerminal - the {@link CompiledTerminal} 
	 * @param mcBridge - the {@link McBridge} as configured
	 * @return - a {@link Maybe} containing the {@link ClassesProcessingRunnerContext}
	 */
	public static Maybe<ClassesProcessingRunnerContext> produceContext(CompiledTerminal compiledTerminal, McBridge mcBridge) {
		// must get the declared dependencies 
				if (compiledTerminal instanceof CompiledDependency == false) {
					String msg = "CompiledTerminal [" + compiledTerminal.asString() + "] cannot be processed as it's not based on a compiled dependency as required";
					log.error(msg);
					DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
					DevrockPlugin.instance().log(status);
					if (mcBridge != null) {
						mcBridge.close();
					}
					return Maybe.empty( Reasons.build( InvalidArtifactIdentification.T).text( msg).toReason());
				}
				
				// find the matching CAI 
				CompiledDependency cd = (CompiledDependency) compiledTerminal;
				Maybe<CompiledArtifactIdentification> caiMaybe = mcBridge.resolve(cd);
				
				if (caiMaybe.isUnsatisfied()) {
					String msg = "Dependency [" + compiledTerminal.asString() + "] cannot be resolved to a concrete artifact identification";
					log.error(msg + "as " + caiMaybe.whyUnsatisfied().stringify());
					DevrockPluginStatus status = new DevrockPluginStatus(msg, (Reason) caiMaybe.whyUnsatisfied());
					DevrockPlugin.instance().log(status);
					if (mcBridge != null) {
						mcBridge.close();
					}
					return Maybe.empty( TemplateReasons.build( UnresolvedDependency.T).enrich( r -> r.setDependency(cd)).toReason());
				}
				
				CompiledArtifactIdentification cai = caiMaybe.get();		
				// resolve the CA
				Maybe<CompiledArtifact> caMaybe = mcBridge.resolve( cai);
				if (caMaybe.isUnsatisfied()) {
					String msg = "Artifact [" + cai.asString() + "] cannot be resolved to a concrete artifact";
					log.error(msg + "as " + caMaybe.whyUnsatisfied().stringify());
					DevrockPluginStatus status = new DevrockPluginStatus(msg, (Reason) caMaybe.whyUnsatisfied());
					DevrockPlugin.instance().log(status);
					if (mcBridge != null) {
						mcBridge.close();
					}
					return Maybe.empty( TemplateReasons.build( UnresolvedArtifact.T).enrich( r -> r.setArtifact(cai)).toReason());
				}
				
				// get the dependencies 
				CompiledArtifact ca = caMaybe.get();
				List<AnalysisDependency> terminalDependencies = ca.getDependencies().stream().map( c -> AnalysisDependency.from(c)).collect( Collectors.toList());
				
				
				// run resolution 
				Maybe<AnalysisArtifactResolution> resolutionMaybe = mcBridge.resolveClasspath( compiledTerminal, ClasspathResolutionScope.compile);
				if (resolutionMaybe.isUnsatisfied()) {
					String msg = "Project [" + compiledTerminal.asString() + "] cannot be processed";
					log.error(msg + "as " + resolutionMaybe.whyUnsatisfied().stringify());
					DevrockPluginStatus status = new DevrockPluginStatus(msg, (Reason) resolutionMaybe.whyUnsatisfied());
					DevrockPlugin.instance().log(status);
					if (mcBridge != null) {
						mcBridge.close();
					}
					return resolutionMaybe.cast();
				}
				
				
				mcBridge.close();
				
				
				AnalysisArtifactResolution aar = resolutionMaybe.get();
				AnalysisTerminal analysisTerminal = aar.getTerminals().get(0);
				
				ClassesProcessingRunnerContext cprContext = ClassesProcessingRunnerContext.T.create();
				
				cprContext.setTerminal( analysisTerminal.asString());
				cprContext.setClasspath( aar.getSolutions());
				cprContext.setConsoleOutputVerbosity(ConsoleOutputVerbosity.silent);
				cprContext.setDependencies( terminalDependencies);
				
				// custom finger print ratings		
				if (DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_FP_OVERRIDE_RATINGS, false)) {
					String customFingerPrintFile = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE, null);
					if (customFingerPrintFile != null) {
						File file = new File(customFingerPrintFile);
						if (file.exists()) {
							FileResource fileResource  = FileResource.T.create();
							fileResource.setPath( file.getAbsolutePath());
							cprContext.setCustomRatingsResource(fileResource);
						}
					}
				}
				
				// finger print ratings attached to artifact  
				CompiledPartIdentification cpi = CompiledPartIdentification.from(cai, PartIdentification.create("fps"));
				Maybe<File> fingerPrintOverridePartMaybe = mcBridge.resolve(cpi);	
				if (fingerPrintOverridePartMaybe.isSatisfied()) {
					File file = fingerPrintOverridePartMaybe.get();
					if (file.exists()) {
						FileResource fileResource  = FileResource.T.create();
						fileResource.setPath( file.getAbsolutePath());
						cprContext.setPullRequestRatingsResource(fileResource);
					}
				}
				return Maybe.complete(cprContext);
	}

}
