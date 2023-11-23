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
package com.braintribe.devrock.mj.ui.dialog.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.build.gwt.GwtModuleChecker;
import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.mj.plugin.MungoJerryPlugin;
import com.braintribe.devrock.mj.plugin.MungoJerryStatus;
import com.braintribe.devrock.mj.ui.dialog.tab.AnalysisController;
import com.braintribe.devrock.mj.ui.dialog.tab.GwtModuleScannerMonitor;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;


public class Analyzer {
	private static Logger log = Logger.getLogger(Analyzer.class);
	private CompiledDependencyIdentification gwtArtifactToBeInjected;
	
	public void setGwtArtifactToBeInjected(CompiledDependencyIdentification gwtArtifactToBeInjected) {
		this.gwtArtifactToBeInjected = gwtArtifactToBeInjected;
	}
	
	public Collection<ModuleCheckProtocol> extractProtocols(AnalysisController analysisController, IProgressMonitor monitor) throws AnalyzerException {
		
		IProject project = analysisController.getProject();

		
			IJavaProject javaProject = JavaCore.create(project);
		
			IPath wsOutputLocation;
			try {
				wsOutputLocation = javaProject.getOutputLocation();
			} catch (JavaModelException e) {
				String msg = "cannot retrieve output location of project [" + project.getName() + "]";
				log.error(msg, e);
				MungoJerryStatus status = new MungoJerryStatus( msg , e);
				MungoJerryPlugin.instance().log(status);
				throw new AnalyzerException(msg, e);
			}			  
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(wsOutputLocation);
			final File outputFolder = ifile.getRawLocation().toFile();
			MungoJerryStatus status = new MungoJerryStatus( "Outputfolder for project [" + project.getName() + "] is [" + outputFolder.getAbsolutePath() + "]" , Status.INFO);
			MungoJerryPlugin.instance().log(status);
		
			File baseFolder = project.getLocation().toFile();
		
			
			if (
					(outputFolder.exists() == false) ||
					(outputFolder.list().length == 0) 
				){
				String msg = "An output folder (declared in the project as [" + outputFolder.getAbsolutePath() + "] must exist and cannot be empty";
				status = new MungoJerryStatus( msg , Status.ERROR);
				MungoJerryPlugin.instance().log(status);
				throw new AnalyzerException(msg);
			}
			final File sourceFolder = new File(baseFolder, "src");
			if (
					(sourceFolder.exists() == false) ||
					(sourceFolder.list().length == 0) 
				){
				String msg = "An source folder (declared in the project as [" + outputFolder.getAbsolutePath() + "] must exist and cannot be empty";
				status = new MungoJerryStatus( msg , Status.ERROR);
				MungoJerryPlugin.instance().log(status);
				throw new AnalyzerException(msg);				
			}
						
			status = new MungoJerryStatus( "Sourcefolder for project [" + project.getName() + "] is [" + sourceFolder.getAbsolutePath() + "]" , Status.INFO);
			MungoJerryPlugin.instance().log(status);
			//
			// monitor 
			//
			GwtModuleScannerMonitor moduleMonitor = new GwtModuleScannerMonitor();
			moduleMonitor.setMonitor( monitor);
			// build the classpath 			
			List<File> classpathAsFiles = getClassPath( javaProject);
			
			//
			// if the project's a model, we must inject the gwt-user jar.. 
			//
			InjectorTuple tuple = retrieveInjectorTuple();
			if (tuple != null) {
				boolean gwtUserPresent = true;
				if (NatureHelper.isModelArtifact(project)) {
					// a model has it missing by default
					gwtUserPresent = false;				
				}				
				if (!gwtUserPresent) {
					String cpPath = null;
					String soPath = null;
					if (tuple.compiledJar != null) {
						 cpPath = tuple.compiledJar.getAbsolutePath();
					}
					if (tuple.sourcesJar != null) {
						soPath = tuple.sourcesJar.getAbsolutePath();
					}
					boolean cpPresent = false;
					boolean soPresent = false;
					
					for (File file : classpathAsFiles) {
						if (cpPath != null && file.getAbsolutePath().equalsIgnoreCase( cpPath)) {
							cpPresent = true;
						}
						if (soPath != null && file.getAbsolutePath().equalsIgnoreCase( soPath)) {
							soPresent = true;
						}
						if (cpPresent && soPresent)
							break;
					}
					if (!cpPresent && cpPath != null) {
						classpathAsFiles.add( tuple.compiledJar);
					}
					if (!soPresent && soPath != null) {
						classpathAsFiles.add( tuple.sourcesJar);
					}
				}				
			}
			for (File file : classpathAsFiles) {
				status = new MungoJerryStatus( "Classpath for project [" + project.getName() + "] contains [" + file.getAbsolutePath() + "]", IStatus.INFO);
				MungoJerryPlugin.instance().log(status);						
			}
			
	
			//
			// checker
			//
			GwtModuleChecker checker = new GwtModuleChecker();
			checker.setMonitor( moduleMonitor);
			checker.setStrict( true);
			checker.setArtifactClassFolder( outputFolder);
			checker.setClasspath( classpathAsFiles);
			checker.setSrcPath( sourceFolder);
			Set<ModuleCheckProtocol> protocols;
			try {
				protocols = checker.check();
			} catch (Exception e) {
				throw new AnalyzerException(e);
			}
			
			//
			// give the data to the result page 								
			analysisController.setProtocols( protocols);
			analysisController.setOutputFolder( outputFolder);
			analysisController.setSourceFolder( sourceFolder);
			analysisController.setClasspathAsFiles( classpathAsFiles);
			
			return protocols;
	
	}

	private class InjectorTuple {
		public File compiledJar;
		public File sourcesJar;
	}

	/**
	 * if specified, it will return a tuple of jar, sources:jar, or null 
	 * @return - a {@link InjectorTuple}
	 */
	private InjectorTuple retrieveInjectorTuple() {	
		if (gwtArtifactToBeInjected != null) {			
			InjectorTuple tuple = new InjectorTuple();
			Maybe<CompiledArtifactIdentification> potential = DevrockPlugin.mcBridge().resolve( gwtArtifactToBeInjected);
			if (potential.isUnsatisfied()) {
				String msg = "Specified auto-inject GWT artifact [" + gwtArtifactToBeInjected.asString() + "] cannot be resolved as : " + potential.whyUnsatisfied().stringify();
				MungoJerryStatus status = new MungoJerryStatus( msg , Status.ERROR);
				MungoJerryPlugin.instance().log(status);
				return null;				
			}
			CompiledArtifactIdentification gwtArtifact = potential.get(); 

			
			CompiledPartIdentification jarCpi = CompiledPartIdentification.from(gwtArtifact, PartIdentification.create(":jar"));
			Maybe<File> jarPotential = DevrockPlugin.mcBridge().resolve( jarCpi);
			if (jarPotential.isUnsatisfied()) {				//
				String msg = "The jar of  [" + gwtArtifact.asString() + "] cannot be resolved as : " + jarPotential.whyUnsatisfied().stringify();
				MungoJerryStatus status = new MungoJerryStatus( msg , Status.ERROR);
				MungoJerryPlugin.instance().log(status);
				return null;
			}			
			tuple.compiledJar = jarPotential.get();
			
			CompiledPartIdentification sourcesCpi = CompiledPartIdentification.from(gwtArtifact, PartIdentification.create("sources:jar"));
			Maybe<File> sourcesPotential = DevrockPlugin.mcBridge().resolve( sourcesCpi);
			if (sourcesPotential.isUnsatisfied()) {
				String msg = "The sources of  [" + gwtArtifact.asString() + "] cannot be resolved as : " + sourcesPotential.whyUnsatisfied().stringify();
				MungoJerryStatus status = new MungoJerryStatus( msg , Status.ERROR);
				MungoJerryPlugin.instance().log(status);
				return null;
			}			 
			tuple.sourcesJar = sourcesPotential.get();
																									
			return tuple;
		}
		else {
			return null;
		}
	}
	

	private List<File> getClassPath(IJavaProject project) throws AnalyzerException {
		
		List<File> classpath = new ArrayList<File>();
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		
		IClasspathEntry[] standardEntries;
		try {
			standardEntries = project.getResolvedClasspath( true);
		} catch (JavaModelException e) {
			String msg="cannot extract the resolved classpath from [" + project.getProject().getName() + "]";
			log.error( msg, e);
			throw new AnalyzerException(msg, e);
		}
		entries.addAll( Arrays.asList(standardEntries));
		//
		// we must add AC's gwt special stuff... 
		//				
		
		for (IClasspathEntry entry : entries) {
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT: {
					File sourceFile = entry.getPath().toFile();
					if (sourceFile.isAbsolute())
						classpath.add( sourceFile);
					else {
						// get root of workspace.. 
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IProject referencedProject = root.getProject( entry.getPath().segment(0));
						
						IJavaProject javaProject = JavaCore.create( referencedProject);
						
						IPath wsOutputLocation;
						try {
							wsOutputLocation = javaProject.getOutputLocation();
						} catch (JavaModelException e) {
							String msg = "cannot retrieve output location of project [" + project.getProject().getName() + "]";
							log.error(msg, e);
							throw new AnalyzerException(msg, e);
						}			  
						IFile ifile;
						try {
							ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(wsOutputLocation);
						} catch (Exception e1) {
							String msg = "cannot retrieve file representation of output location of project [" + project.getProject().getName() + "]";
							log.error(msg, e1);
							throw new AnalyzerException(msg, e1);
						}
						sourceFile = ifile.getRawLocation().toFile();
						
						// must find package root with source code in it -> first CPE_SOURCE in project's classpath
						try {
							IPackageFragmentRoot [] fragments = javaProject.getPackageFragmentRoots();
							for (IPackageFragmentRoot pfroot : fragments) {
								IClasspathEntry pfentry = pfroot.getResolvedClasspathEntry();
								if (pfentry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
									File sourceDir = ResourcesPlugin.getWorkspace().getRoot().getFile( pfentry.getPath()).getRawLocation().toFile();
									classpath.add( sourceDir);
									break;
								}						
							}
						} catch (JavaModelException e) {						
							e.printStackTrace();
						}
						
						classpath.add( sourceFile);											
					}									
					break;
				}
				case IClasspathEntry.CPE_LIBRARY: {
					File file = entry.getPath().toFile();
					if (file.exists() == false) {
						log.info( "detected possible project source directory reference in [" + file.getAbsolutePath() + "]");
						String projectName = entry.getPath().segment(0);
						String sourceDir = entry.getPath().segment(1);
						
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IProject referencedProject = root.getProject( projectName);
						File sourceFile = entry.getPath().toFile();				
						sourceFile = new File( referencedProject.getLocation().toOSString() + "/" + sourceDir);
						if (sourceFile.exists() == false) {
							log.info( "intercepting invalid path [" + file.getAbsolutePath() + "]");
						} else 
							classpath.add( sourceFile);						
					} 
					else if (entry.getSourceAttachmentPath() != null) {
						// 
						File sourceFile = entry.getSourceAttachmentPath().toFile();
						// only add our files here  or what? .. 
						// and how to determine ours? 
						if (!sourceFile.getName().endsWith("-sources.jar")) {
							classpath.add(file);
							continue;
						}
						classpath.add( sourceFile);				
						classpath.add( file);
					} 
					else {
						classpath.add( file);						
					}
					break;
				}
				case IClasspathEntry.CPE_SOURCE: {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					String projectName = entry.getPath().segment(0);
					String sourceDir = entry.getPath().segment(1);
					IProject referencedProject = root.getProject( projectName);
					File sourceFile = entry.getPath().toFile();				
					sourceFile = new File( referencedProject.getLocation().toOSString() + "/" + sourceDir);
					classpath.add( sourceFile);
					
					break;
				}							
			}
		}
	
		return classpath;
	}

}
