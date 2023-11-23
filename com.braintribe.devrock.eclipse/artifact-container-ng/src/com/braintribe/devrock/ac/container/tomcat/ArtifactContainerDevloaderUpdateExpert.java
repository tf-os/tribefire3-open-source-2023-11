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
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.devrock.ac.container.tomcat;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.api.project.DerivedJavaProjectData;
import com.braintribe.devrock.api.project.JavaProjectDataExtracter;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * the expert that handles the devloader file and its content. 
 * 
 * @author pit
 *
 */
public class ArtifactContainerDevloaderUpdateExpert {
	private static Logger log = Logger.getLogger(ArtifactContainerDevloaderUpdateExpert.class);
	private static final String DEVLOADER_FILE = ".#webclasspath_ac";
	/**
	 * update the devloader's file from the runtime entries of the container 
	 * @param container - the {@link ArtifactContainer}
	 */
	public static void updateTomcatDevloader( IJavaProject project, IClasspathEntry[] launchEntries) {
			
		List<String> values = buildEntries( project, launchEntries);
		String path = project.getProject().getLocation().toOSString();
		File devloaderFile = new File( path + File.separator + DEVLOADER_FILE);
		writeToWebclasspathFile(values, devloaderFile);		
		
	}
	
	public static boolean hasDevloader( IProject project) {
		String path = project.getLocation().toOSString();
		File devloaderFile = new File( path + File.separator + DEVLOADER_FILE);
		return devloaderFile.exists();
	}
	
	/**
	 * build a list of the string representations of the runtime classpath entries 
	 * @param project - the {@link IJavaProject} the container's linked to 
	 * @param entries - an array of {@link IClasspathEntry}, the runtime entries of the container 
	 * @return - a {@link List} of String representation (jar refereneces and project references) 
	 */
	private static List<String> buildEntries( IJavaProject project, IClasspathEntry [] entries){
		List<String> values = new ArrayList<String>();
		
		for (IClasspathEntry entry : entries) {
			String entryPath = null;
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_LIBRARY: {
					entryPath = entry.getPath().toOSString();
					if (entryPath != null) {
						values.add( entryPath);
					}
					break;
				}
				case IClasspathEntry.CPE_PROJECT: { 					
				
					Maybe<DerivedJavaProjectData> outputsMaybe = JavaProjectDataExtracter.getRelevantOutputLocationsOfProject( entry);
					if (outputsMaybe.isUnsatisfied()) {
						String msg ="Cannot determine output directory of project [" + entry.getPath().toOSString() + "]";				
						log.warn(msg);
						Reason reason = outputsMaybe.whyUnsatisfied();
						ArtifactContainerStatus status = new ArtifactContainerStatus(msg, reason);
						ArtifactContainerPlugin.instance().log(status);	
						continue;
					}
					DerivedJavaProjectData outputs = outputsMaybe.get();
					// standard output
					File standardOutput = outputs.outputFolder;
					if (standardOutput != null && standardOutput.exists()) {
						values.add( standardOutput.getAbsolutePath());
					}
					
					// exported data 
					for (File file : outputs.exportedFolders) {
						values.add( file.getAbsolutePath());
					}																	
					break;
				}
				default: {
					break;
				}
			}
		}
		
		// add ourself to it

		try {
			IPath wsOutputLocation = project.getOutputLocation();			  
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(wsOutputLocation);
			File sourceFile = ifile.getRawLocation().toFile();
			values.add( sourceFile.getAbsolutePath());
			
		} catch (JavaModelException e) {
			String msg ="Cannot determine output directory of project [" + project.getElementName() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.instance().log(status);	
		}
		return values;
	}
	
	
	/**
	 * make a backup and then write to the devloader file 
	 * @param values - the {@link List} of string values 
	 * @param file - the devloader file 
	 * @return - true if it worked, false otherwise, guess.. 
	 */
	private static boolean writeToWebclasspathFile( List<String> values, File file) {
		if (checkWriteable(file) == false)
			return false;
		
		backupFile(file);
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (builder.length() > 0) 
				builder.append("\n");
			builder.append( value);
		}
		try {
			IOTools.spit(file, builder.toString(), "UTF-8", false);
		} catch (IOException e) {
			String msg ="Cannot write webclasspath file [" + file.getAbsolutePath() + "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.instance().log(status);	
		}
		return true;
	}
	
	
	
	/**
	 * create a backup file form the {@link File} file, by deleting an existing backup, and renaming the file
	 */
	private static boolean backupFile( File file) {
		File backup = new File( file.getAbsolutePath() + ".bak");
		if (backup.exists()) {
			backup.delete();
		}
		return new File(file.getAbsolutePath()).renameTo(backup);
	}
	
	/**
	 * check if the {@link File} file can be written to 
	 */
	private static boolean checkWriteable( File file) {
		if (	
				(file.exists()) &&
				(file.canWrite() == false)
			){
			String msg ="Cannot write to file [" + file.getAbsolutePath()+ "]";			
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(status);	
			return false;
		}	
		return true;
	}
}
