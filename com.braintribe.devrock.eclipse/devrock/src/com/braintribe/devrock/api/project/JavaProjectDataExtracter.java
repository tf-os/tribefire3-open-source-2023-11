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
package com.braintribe.devrock.api.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.eclipse.model.reason.devrock.ProjectInternalExtractionFailure;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * some helpers in the domain of {@link IJavaProject}s
 * @author pit
 *
 */
public class JavaProjectDataExtracter {
	private static Logger log = Logger.getLogger(JavaProjectDataExtracter.class);
	
	/**
	 * returns a Maybe with all files (actually directories) that are marked as exported
	 * @param javaProject - the {@link IJavaProject} to access 
	 * @return a {@link Maybe} containing a {@link List} of exported {@link File} (directories)
	 */
	public static Maybe<List<File>> getExportedDirectories( IJavaProject javaProject) {
	
		IClasspathEntry[] entries;
		try {
			//entries = javaProject.getReferencedClasspathEntries();
			entries = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			InternalError ieReason = InternalError.from(e);
			return Maybe.empty( ieReason);
		}
		if (entries == null || entries.length == 0) {
			return Maybe.complete(Collections.emptyList());
		}
		Reason issue = null;
		List<File> files = new ArrayList<>();
		for (IClasspathEntry entry : entries) {
			if (entry.isExported() && entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
				IPath path = entry.getPath();
				File file;
				try {
					file = ResourcesPlugin.getWorkspace().getRoot().getFile( path).getRawLocation().toFile();
				} catch (Throwable e) {
					IProject project = javaProject.getProject();					
					if (issue == null) {
						issue = Reasons.build(Reason.T).text("issues during exported directory evaluation of :" + project.getName()).toReason();
					}			
					VersionedArtifactIdentification vai = getVersionedArtifactIdentificationFromProject(project);
					Reason failReason = TemplateReasons.build(ProjectInternalExtractionFailure.T)
										.assign( ProjectInternalExtractionFailure::setExtractionTarget, "exported folder")
										.assign( ProjectInternalExtractionFailure::setProjectIdentification, vai)
										.toReason();
					issue.getReasons().add( failReason);
					continue;					
				}
				files.add(file);
			}
		}
		if (issue != null) {
			return Maybe.incomplete( files, issue);
		}
		else {
			return Maybe.complete( files);
		}
	}
	
	/**
	 * retrieves all source directories from the {@link IProject}
	 * @param project - the {@link IProject}
	 * @return - a {@link Maybe} of a {@link List} of all source directory {@link File}s
	 */
	public static Maybe<List<File>> getSourceDirectories( IProject project) {
		IClasspathEntry[] entries;
		IJavaProject javaProject;
		try {
			javaProject = JavaCore.create(project);
			entries = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			InternalError ieReason = InternalError.from(e);
			return Maybe.empty( ieReason);
		}
		if (entries == null || entries.length == 0) {
			return Maybe.complete(Collections.emptyList());
		}
		Reason issue = null;
		List<File> sourceDirectories = new ArrayList<>();
		for (IClasspathEntry raw : entries) {
			if (raw.getEntryKind() == IClasspathEntry.CPE_SOURCE) {					
				String fullpath = javaProject.getProject().getFullPath().toOSString();
				String sourcePath = raw.getPath().toOSString();
				String combinedPath = fullpath.substring( 0, fullpath.indexOf( project.getName())-1) + sourcePath;
				Path path = new Path( combinedPath);
				File file;
				try {
					file = ResourcesPlugin.getWorkspace().getRoot().getFile( path).getRawLocation().toFile();
					sourceDirectories.add(file);
				} catch (Throwable e) {					
					if (issue == null) {
						issue = Reasons.build(Reason.T).text("issues during source directory evaluation of :" + project.getName()).toReason();
					}			
					VersionedArtifactIdentification vai = getVersionedArtifactIdentificationFromProject(project);
					Reason failReason = TemplateReasons.build(ProjectInternalExtractionFailure.T)
										.assign( ProjectInternalExtractionFailure::setExtractionTarget, "source folder")
										.assign( ProjectInternalExtractionFailure::setProjectIdentification, vai)
										.toReason();
					issue.getReasons().add( failReason);
					continue;
				}
			}
		}
				
		if (issue != null) {
			return Maybe.incomplete( sourceDirectories, issue);
		}
		else {
			return Maybe.complete( sourceDirectories);
		}		
	}
	
	/**
	 * uses th WorkspaceRegistry to identify the project as a {@link VersionedArtifactIdentification}
	 * @param project - the {@link IProject}
	 * @return - the {@link VersionedArtifactIdentification} as assigned
	 */
	public static VersionedArtifactIdentification getVersionedArtifactIdentificationFromProject(IProject project) {
		DevrockPluginStatus status = new DevrockPluginStatus("cannot extract location of exported entry in [" + project.getName() + "]", IStatus.ERROR);
		DevrockPlugin.instance().log(status);
		
		BasicWorkspaceProjectInfo projectInfo = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(project);
		VersionedArtifactIdentification vai;
		if (projectInfo == null) {
			vai = VersionedArtifactIdentification.create("<unknown>", "<unknown>", "0.0.0");
		}
		else {
			vai = projectInfo.getVersionedArtifactIdentification();						
		}
		return vai;
	}
	/**
	 * get the output location of the passed {@link IClasspathEntry} 
	 * @param entry - the {@link IClasspathEntry} to process 
	 * @return - a {@link Maybe} containing a {@link Pair} of the main/build dir as {@link File}, plus all exported other
	 * binary folders as a {@link List} of {@link File}
	 * @throws JavaModelException
	 */
	public static Maybe<DerivedJavaProjectData> getRelevantOutputLocationsOfProject( IClasspathEntry entry) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject referencedProject = root.getProject( entry.getPath().segment(0));				
		return getRelevantOutputLocationsOfProject(referencedProject);		
	}
	
	/**
	 * get the output location of the passed {@link IProject} which must be assignable to a IJavaProject
	 * @param referencedProject
	 * @return
	 * @throws JavaModelException
	 */
	public static Maybe<DerivedJavaProjectData> getRelevantOutputLocationsOfProject( IProject referencedProject){
		
		IJavaProject javaProject = JavaCore.create( referencedProject);
		Maybe<File> outputDirMaybe = getOutputlocationOfProject( javaProject);
		if (outputDirMaybe.isEmpty()) {
			return outputDirMaybe.cast();
		}
		File outputDir = outputDirMaybe.get();
		List<File> exportedFiles = new ArrayList<>();
		
		// for ARB: get all exported folders from IJavaProject and add them
		Maybe<List<File>> maybe = JavaProjectDataExtracter.getExportedDirectories(javaProject);
		if (maybe.isSatisfied()) {
			exportedFiles.addAll( maybe.get());			
		}
		else {
			log.error("failed : " + maybe.whyUnsatisfied().stringify());
			return maybe.emptyCast();
		}										
		
		DerivedJavaProjectData projectData = new DerivedJavaProjectData();
		projectData.project = referencedProject;
		projectData.outputFolder = outputDir;
		projectData.exportedFolders = exportedFiles;
		
		return Maybe.complete( projectData);
	}
	
	
	/**
	 * get the output location of the {@link IJavaProject} 
	 */
	public static Maybe<File> getOutputlocationOfProject( IJavaProject project) {
		IPath wsOutputLocation;
		try {
			wsOutputLocation = project.getOutputLocation();
		} catch (JavaModelException e) {
			return Maybe.empty( Reasons.build(InternalError.T).text( e.getMessage()).toReason());
		}			  
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(wsOutputLocation);
		return Maybe.complete( ifile.getRawLocation().toFile());
	}
	
	
}
