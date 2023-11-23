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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.utils.IOTools;


/**
 * updates the diverse files for the module debug project
 * @author pit
 *
 */
public class WiredModuleCarrierDevloaderUpdateExpert {
	private static final String NAME_TRIBEFIRE = ".tribefire";
	private static final String NAME_CLASSPATH = "classpath";
	private static final String NAME_SOLUTIONS = "solutions";
	private static final String NAME_MODULES = "modules";
	private static Logger log = Logger.getLogger(WiredModuleCarrierDevloaderUpdateExpert.class);
	
	public static void updateModuleCarrierClasspath( ClasspathResolverContract contract, IProject project, ArtifactContainer container) {	
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
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);
			return;
		}

		// enumerate all module directories
		File [] modules = modulesDirectory.listFiles(File::isDirectory);
		
		// iterate over the modules
		for (File module : modules) {
			updateModuleClasspath( contract, module);
		}
	}

	/**
	 * update the .classpath file of a module
	 * @param pomReader - the {@link ArtifactPomReader} needed to identify the jars 
	 * @param module - the modules parent directory 
	 */
	private static void updateModuleClasspath( ClasspathResolverContract contract, File module) {
		String moduleName = module.getName();
		String [] tokens = moduleName.split( "[_#]");
		if (tokens.length != 3) {
			log.error("cannot deduce module from directory [" + moduleName + "]");
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
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR, e1);
			ArtifactContainerPlugin.getInstance().log(status);	
			return;
		} catch (IOException e1) {
			String msg = "cannot read " + NAME_SOLUTIONS + " file within module [" + moduleName + "] in associated folder [" + module.getAbsolutePath() + "]";
			log.error(msg, e1);
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR, e1);
			ArtifactContainerPlugin.getInstance().log(status);	
			return;
		}
		// if nothing's in the file, ignore it 
		if (contents == null || contents.length() == 0) {
			return;
		}
				
		String [] solutionNames = contents.split( "\n");
	
		List<String> result = new ArrayList<>();
		
		for (String solutionName : solutionNames) {
			
			String name = solutionName.trim(); 
			Solution solution = NameParser.parseCondensedSolutionName(name);
		
			Dependency dependency = NameParser.parseCondensedDependencyName(name);
		
			// check group-id and artifact-id to detect the current module's jar
			boolean moduleItself = dependency.getGroupId().equalsIgnoreCase( grp) && dependency.getArtifactId().equalsIgnoreCase(art);
			
		
			// now .. some magic is required  
			IProject project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(solution);
			if (project != null) {
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject.exists() && javaProject.isOpen()) {
					try {
						IPath outputLocation = javaProject.getOutputLocation();
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IFolder folder = root.getFolder( outputLocation);
						String path = folder.getLocation().toOSString();
						store( result, path, moduleItself);
					} catch (JavaModelException e) {
						String msg = "cannot extract output folder of matching project [" + project.getName() + "] of [" + solutionName + "]";
						log.warn(msg, e);
						ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.WARNING, e);
						ArtifactContainerPlugin.getInstance().log(status);
						project = null;
					}
				}
				else {
					project = null;
				}
			}
			if (project == null) {
				String jarPathForSolution = getJarPathForSolution(contract, dependency);
				if (jarPathForSolution == null) {
					String msg = "cannot find a matching jar for [" + solutionName +"]";
					log.error( msg);
					ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
					ArtifactContainerPlugin.getInstance().log(status);	
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
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		
		}		
		
	}
	
	/**
	 * find the jar that matches the solution. if not found directly, resolve the REAL solution
	 * @param contract - {@link ClasspathResolverContract}  
	 * @param dependency - the {@link Dependency} (in form of a solution, i.e. may not be ranged) 
	 * @return - the path to the jar
	 */
	private static String getJarPathForSolution(ClasspathResolverContract contract, Dependency dependency) {
		String result = getJarPathForMatchingSolution(contract, dependency);
		if (result != null)
			return result;
		
						
		Set<Solution> solutions = contract.dependencyResolver().resolveTopDependency( UUID.randomUUID().toString(), dependency);
		if (solutions == null || solutions.size() == 0) {
			return null;
		}
		
		Solution resolvedSolution;
		if (solutions.size() == 1) {
			resolvedSolution = solutions.toArray(new Solution[0])[0]; 
		}
		else {
			resolvedSolution = solutions.toArray(new Solution[0])[0];
			String msg = "resolving [" + NameParser.buildName(dependency) + "] has delivered two results, one for release, the other for snapshots. Using the first one";
			log.warn( msg);
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		Dependency d = Dependency.T.create();
		ArtifactProcessor.transferIdentification(d, resolvedSolution);
		d.setVersionRange( VersionRangeProcessor.createfromVersion( resolvedSolution.getVersion()));
		return getJarPathForMatchingSolution(contract, d);
		
	}
	
	/**
	 * derive a solution from the dependency to resolve it and to retrieve the jar
	 * @param contract - the {@link ClasspathResolverContract}
	 * @param dependency - the {@link Dependency} (in form of a solution, i.e. may not be ranged) 
	 * @return - the path to the jar file 
	 */
	private static String getJarPathForMatchingSolution(ClasspathResolverContract contract, Dependency dependency) {
		
		// prepare the data for the resolving 
		Solution solution = Solution.T.create();
		ArtifactProcessor.transferIdentification(solution, dependency);
		Version v = VersionProcessor.createFromString( VersionRangeProcessor.toString( dependency.getVersionRange()));
		solution.setVersion(v);
		
		PartTuple tuple = PartTupleProcessor.createJarPartTuple();
		if (dependency.getClassifier() != null)
			tuple.setClassifier( dependency.getClassifier());
		
		Part part = Part.T.create();
		part.setType( tuple);
		ArtifactProcessor.transferIdentification(part, dependency);
		part.setVersion(v);
						
		// resolve the part 
		String expectedName = NameParser.buildFileName(part);
		SolutionReflectionExpert expert = contract.repositoryReflection().acquireSolutionReflectionExpert(solution);
		File jarFile = expert.getPart(part, expectedName, RepositoryRole.release);
		if (jarFile != null && jarFile.exists()) { 
			return jarFile.getAbsolutePath();
		}
		return null;		
	}
	
	
	
	
	
	

	/**
	 * either adds a value or inserts it at the fist place
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
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);
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
				ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
				ArtifactContainerPlugin.getInstance().log(status);	
			}
		} catch (FileNotFoundException e) {
			String msg = "cannot find " + NAME_TRIBEFIRE + " file for project [" + project.getName() + "]";
			log.error( msg);
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		} catch (IOException e) {
			String msg = "cannot read the " + NAME_TRIBEFIRE + " file for project [" + project.getName() + "]";
			log.error( msg);
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		}

		return null;
	}
}
