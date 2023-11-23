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
package com.braintribe.devrock.dmb.builder;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.devrock.api.markers.MarkerHelper;
import com.braintribe.devrock.api.project.JavaProjectDataExtracter;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectInfo;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.dmb.builder.marker.ReasonedMarkerHandler;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderPlugin;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderStatus;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.utils.IOTools;



/**
 * updates the diverse files for the module debug project
 * @author pit
 *
 */
public class DebugModuleBuilder extends IncrementalProjectBuilder {
	public static final String ID = "com.braintribe.devrock.dmb.builder.DebugModuleBuilder";
	private static final String NAME_TRIBEFIRE = ".tribefire";
	private static final String NAME_CLASSPATH = "classpath";
	private static final String NAME_SOLUTIONS = "solutions";
	private static final String NAME_MODULES = "modules";
	private static Logger log = Logger.getLogger(DebugModuleBuilder.class);
	
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();

		MarkerHelper.removeFailedResolutionMarkersFromProject( project, ReasonedMarkerHandler.MRK_REASON);		
		updateModuleCarrierClasspath( project);
		
		return null;
	}
	
	public static void updateModuleCarrierClasspath(IProject project) {	
		log.debug( "updating carrier [" + project.getName() + "]");
		
		// find the .tomcat file & extract the carrier's modules location
		File tribefireDirectory = findModules( project);
		
		if (tribefireDirectory == null) {
			
			return;
		}
		
		File modulesDirectory = new File ( tribefireDirectory, NAME_MODULES);
		if (!modulesDirectory.exists()) {
			String msg = "expected modules directory [" + modulesDirectory + "] doesn't exist";
			log.error( msg);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR);
			DebugModuleBuilderPlugin.instance().log(status);
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject(project, msg, IMarker.SEVERITY_ERROR);
			return;
		}

		// enumerate all module directories
		File [] modules = modulesDirectory.listFiles(File::isDirectory);
		
		// iterate over the modules
		for (File module : modules) {
			updateModuleClasspath( project, module);
		}
	}

	/**
	 * update the .classpath file of a module
	 * @param pomReader - the {@link ArtifactPomReader} needed to identify the jars 
	 * @param module - the modules parent directory 
	 */
	private static void updateModuleClasspath(IProject project, File module) {
		String moduleName = module.getName();
		String [] tokens = moduleName.split( "[_#]");
		if (tokens.length != 3) {
			String msg = "cannot deduce module from directory [" + moduleName + "]";
			log.error(msg);
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return;
		}
		String grp = tokens[0];
		String art = tokens[1];
		//String vrs = tokens[2];
				
		
		File solutionFile = new File( module, NAME_SOLUTIONS);	
		
		String contents;
		try (InputStream in = new FileInputStream(solutionFile)) {
			contents = IOTools.slurp(in, "UTF-8");			
		} catch (FileNotFoundException e1) {
			String msg = "cannot find " + NAME_SOLUTIONS + " file within module [" + moduleName + "] in associated folder [" + module.getAbsolutePath() + "]";
			log.error( msg, e1);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e1);
			DebugModuleBuilderPlugin.instance().log(status);
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return;
		} catch (IOException e1) {
			String msg = "cannot read " + NAME_SOLUTIONS + " file within module [" + moduleName + "] in associated folder [" + module.getAbsolutePath() + "]";
			log.error(msg, e1);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e1);
			DebugModuleBuilderPlugin.instance().log(status);
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return;
		}
		// if nothing's in the file, ignore it 
		if (contents == null || contents.length() == 0) {
			return;
		}
				
		String [] solutionNames = contents.split( "\n");
	
		List<String> result = new ArrayList<>();
		
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		
		for (String solutionName : solutionNames) {
			
			String name = solutionName.trim();
			
			// parse the 'name' into a CPI, | is the delimiter to an optional |<classifier>:<type> expression. 
			// CPI cannot be parsed, hence this must be a specific parser.
			
			// TODO : correct type
			CompiledPartIdentification vai;			
			try {
				// TODO: correct parser
				vai = parsePartIdentificationFromString( name);
			} catch (Exception e1) {
				String msg = "cannot extract a valid artifact-identification from the expression [" + name + "] in [" + moduleName + "]'s solution file [" + solutionFile.getAbsolutePath() + "]";
				log.error(msg, e1);
				DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e1);
				DebugModuleBuilderPlugin.instance().log(status);
				ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
				continue;
			}
			

			// check group-id and artifact-id to detect the current module's jar
			boolean moduleItself = vai.getGroupId().equalsIgnoreCase( grp) && vai.getArtifactId().equalsIgnoreCase(art);
			
		
			// now .. some magic is required
			
			// first: check direct match 
			WorkspaceProjectInfo projectInfo = workspaceProjectView.getProjectInfo(vai);
			
			// if no direct match, try autorange 
			if (projectInfo == null) {
				projectInfo = workspaceProjectView.getAutoRangedProjectInfo(VersionedArtifactIdentification.create( vai.getGroupId(), vai.getArtifactId(), vai.getVersion().asString()));
				if (projectInfo != null) {
					VersionedArtifactIdentification takenVai = projectInfo.getVersionedArtifactIdentification();					
					String msg = "Debug module project '"+ project.getName() + "': requested '" + vai.asString() + "', best match '" + takenVai.asString() + "'";
					DebugModuleBuilderStatus status = new DebugModuleBuilderStatus( msg, IStatus.WARNING);
					DebugModuleBuilderPlugin.instance().log(status);
				}
			}
			// found anything? 
			if (projectInfo != null) {
				IProject workspaceProject = projectInfo.getProject();
				IJavaProject javaProject = JavaCore.create(workspaceProject);
				if (javaProject.exists() && javaProject.isOpen()) {
					try {
						// manage standard output folder 
						File binaryOutputFolder = ResourcesPlugin.getWorkspace().getRoot().getFile( javaProject.getOutputLocation()).getRawLocation().toFile();	 												
						String path = binaryOutputFolder.getAbsolutePath();
						store( result, path, moduleItself);
								
						// ARB: get all exported folders from ARB and add them
						Maybe<List<File>> maybe = JavaProjectDataExtracter.getExportedDirectories(javaProject);
						if (maybe.isSatisfied()) {
							List<File> exportedFiles = maybe.get();
							for (File file : exportedFiles) {
								path = file.getAbsolutePath();
								store( result, path, moduleItself);
							}
						}
						else {
							// handle error 
							Reason reason = maybe.whyUnsatisfied();
							String msg = "cannot extract exported folders of matching project [" + workspaceProject.getName() + "] of [" + solutionName + "]";
							if (reason instanceof com.braintribe.gm.model.reason.essential.InternalError) {
								com.braintribe.gm.model.reason.essential.InternalError ie = (com.braintribe.gm.model.reason.essential.InternalError) reason;
								Throwable exception = ie.getJavaException();
								log.warn(msg, exception);
								DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.WARNING, (Exception) exception);
								DebugModuleBuilderPlugin.instance().log(status);		
							}
							else {
								log.warn(msg);
								DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.WARNING);
								DebugModuleBuilderPlugin.instance().log(status);
							}
						}											
												
					} catch (JavaModelException e) {
						String msg = "cannot extract output folder of matching project [" + workspaceProject.getName() + "] of [" + solutionName + "]";
						log.warn(msg, e);
						DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.WARNING, e);
						DebugModuleBuilderPlugin.instance().log(status);
						ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
						workspaceProject = null;
					}
				}
				else {
					projectInfo = null;
				}
			}
			// still nothing found, so no matching project found
			if (projectInfo == null) {
				String jarPathForSolution = getJarPathForSolution(vai);
				if (jarPathForSolution == null) {
					String msg = "cannot find a matching jar for [" + solutionName +"]";
					log.error( msg);
					DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR);
					DebugModuleBuilderPlugin.instance().log(status);
					ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
				}
				else {
					store( result, jarPathForSolution, moduleItself);
				}
			}
						
		}		
		StringBuilder builder = new StringBuilder();
		for (String expression : result) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append( expression);
		}
		File outFile = new File( module, NAME_CLASSPATH);
		
		try {
			IOTools.spit(outFile, builder.toString(), "UTF-8", false);
		} catch (IOException e) {
			String msg = "cannot write " + NAME_CLASSPATH + " file for [" + moduleName + "] of [" + module.getAbsolutePath() + "]";
			log.error( msg);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e);
			DebugModuleBuilderPlugin.instance().log(status);	
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
		}		
		
	}
	
	private static CompiledPartIdentification parsePartIdentificationFromString(String name) {
		int pipe = name.indexOf( '|');
		if (pipe < 0) {
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(name);
			CompiledPartIdentification cpi = CompiledPartIdentification.from(cai);
			return cpi;
		}
		else {
			String caiPart = name.substring( 0, pipe);
			String piPart = name.substring( pipe+1);
			PartIdentification pi;
			if (!piPart.contains(":")) {
				pi = PartIdentification.create(piPart, "jar");
			}
			else {
				pi = PartIdentification.parse( piPart);
			}
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(caiPart);
			CompiledPartIdentification cpi = CompiledPartIdentification.from(cai, pi); 
			return cpi;			
		}
	}

	/**
	 * find the jar that matches the solution. If no direct match can be made, the standard range is used to
	 * find the top matching solution. 
	 * @param vai - the {@link VersionedArtifactIdentification}
	 * @return - the path to the jar
	 */
	private static String getJarPathForSolution(CompiledPartIdentification cpi) {
		
		// default type to jar
		if (cpi.getType() == null) {
			cpi.setType("jar");
		}
		
		// direct lookup
		CompiledArtifactIdentification cai = cpi;
		Maybe<CompiledArtifact> potential = DevrockPlugin.mcBridge().resolve( cai);
		if (potential.isSatisfied()) {
			//CompiledArtifact ca = potential.get();					
			Maybe<File> jarPotential = DevrockPlugin.mcBridge().resolve(cpi);
			if (jarPotential.isSatisfied()) {
				return jarPotential.get().getAbsolutePath();
			}	
			// output? 
			log.debug("no jar found for direct match of [" + cpi.asString() +"]");
		}
		
		// build dependency : rangify the version 
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parseAndRangify( cai.asString());
		Maybe<CompiledArtifactIdentification> matchingPotential = DevrockPlugin.mcBridge().resolve( cdi);
		if (matchingPotential.isUnsatisfied()) {
			log.debug("no artifact found matching derived range [" + cai.asString() +"] -> [" + cdi.asString() + "]");
			return null;
		}
		Maybe<CompiledArtifact> matchedPotential = DevrockPlugin.mcBridge().resolve( cai);
		if (matchedPotential.isSatisfied()) {
			CompiledArtifact ca = matchedPotential.get();
			CompiledPartIdentification mcpi = CompiledPartIdentification.from(ca, cpi);					
			Maybe<File> jarPotential = DevrockPlugin.mcBridge().resolve(mcpi);
			if (jarPotential.isSatisfied()) {
				return jarPotential.get().getAbsolutePath();
			}	
			// output? 
			log.debug("no jar found for ranged match of [" + cai.asString() +"] within [" + cdi.asString() + "]");
		}
		return null;
	}	

	/**
	 * either adds a value or inserts it at the first place
	 * @param list - the {@link List} of {@link String}
	 * @param value - the {@link String} to store 
	 * @param top - true if at first place.. bla.. 
	 */
	private static void store( List<String> list, String value, boolean top) {
		if (!top) {
			list.add(value);
		}
		else {
			list.add( 0, value);
		}
			 
	}

	private static File findModules(IProject project) {
		IResource resource = project.findMember( NAME_TRIBEFIRE);
		if (resource == null) {
			String msg = "cannot find " + NAME_TRIBEFIRE + " file for project [" + project.getName() + "]";
			log.error( msg);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR);
			DebugModuleBuilderPlugin.instance().log(status);
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return null;
		}
		
		File tomcatFile = new File(resource.getLocation().toOSString());		
		try (InputStream in = new FileInputStream(tomcatFile)) {		
			
			String contents = IOTools.slurp(in, "UTF-8");
			// could be a relative file path.. 			
			File modules = new File( contents);			
			if (!modules.isAbsolute()) {
				String projectDir = project.getLocation().toString();
				modules = new File( projectDir, contents);
			}
			if (modules.exists()) {
				return modules;
			}
			else {
				String msg = "cannot find the directory [" + contents + "] referenced in the .tribefire file of project [" + project.getName() + "]";
				log.error( msg);
				DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR);
				DebugModuleBuilderPlugin.instance().log(status);	
				ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			}
		} catch (FileNotFoundException e) {
			String msg = "cannot find " + NAME_TRIBEFIRE + " file for project [" + project.getName() + "]";
			log.error( msg);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e);
			DebugModuleBuilderPlugin.instance().log(status);	
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return null;
		} catch (IOException e) {
			String msg = "cannot read the " + NAME_TRIBEFIRE + " file for project [" + project.getName() + "]";
			log.error( msg);
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus(msg, IStatus.ERROR, e);
			DebugModuleBuilderPlugin.instance().log(status);	
			ReasonedMarkerHandler.addFailedResolutionMarkerToProject( project, msg, IMarker.SEVERITY_ERROR);
			return null;
		}

		return null;
	}
}
