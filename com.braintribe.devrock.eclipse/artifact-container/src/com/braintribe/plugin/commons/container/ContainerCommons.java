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
package com.braintribe.plugin.commons.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class ContainerCommons {
	private static Logger log = Logger.getLogger(ContainerCommons.class);
	
	/**
	 * create a MD5 string from a {@link IResource} 
	 */
	public static String getMd5ofResource( IResource resource) {
		
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			throw Exceptions.unchecked(e, "cannot determine md5 of resource", IllegalStateException::new);
		}
		File file = resource.getLocation().toFile();	
		try ( 
				DigestInputStream in = new DigestInputStream( new FileInputStream( file), digest);				
				
			) {			
			IOTools.pump(in, new OutputStream() {
				
				@Override
				public void write(int b) throws IOException {					
				}
			}, IOTools.SIZE_32K);
			
			byte[] bytes = digest.digest();
			String hex = StringTools.toHex(bytes);
			System.out.println( file.getAbsolutePath() + " -> " + hex);
			return hex;
			
			
		} catch (Exception e) {
			log.error( "cannot extract md5 from resource [" + resource + "]", e);			
		}		
		return null;
	}	

	/**
	 * get an {@link InputStream} from a file relative to the plugin's location 	
	 */
	public static InputStream getResourceStream(String urlAsString) {		
		try {
			URL url = new URL( ArtifactContainerPlugin.PLUGIN_RESOURCE_PREFIX + "/" + urlAsString);
			InputStream inputStream = url.openConnection().getInputStream();
			return inputStream;
		} catch (MalformedURLException e) {
			String msg="cannot retrieve resource [" + urlAsString + "]";
			log.error( msg, e);
		} catch (IOException e) {
			String msg="cannot open stream to resource [" + urlAsString + "]";
			log.error( msg, e);
		}
		return null;
	}
	
	
	/**
	 * get the output location of the passed {@link IClasspathEntry} 
	 */
	public static File getOutputLocationOfProject( IClasspathEntry entry) throws JavaModelException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject referencedProject = root.getProject( entry.getPath().segment(0));
		
		IJavaProject javaProject = JavaCore.create( referencedProject);
		return getOutputlocationOfProject( javaProject);
	}
	
	
	/**
	 * get the output location of the {@link IJavaProject} 
	 */
	public static File getOutputlocationOfProject( IJavaProject project) throws JavaModelException {
		IPath wsOutputLocation = project.getOutputLocation();			  
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(wsOutputLocation);
		return ifile.getRawLocation().toFile();
	}
	
	/**
	 * test if AC is mentioned in the .classpath file
	 * @param prjDir - the {@link File} that represents the project directory 
	 * @return - true if AC's there, false if not
	 */
	public static boolean isAcActive(File prjDir) {
		  
		  try {
		   File classpathFile = new File(prjDir, ".classpath");
		   RandomAccessFile raf = new RandomAccessFile(classpathFile, "r");
		   long length = raf.length();
		   byte[] data = new byte[(int)length];
		   raf.readFully(data);
		   raf.close();
		   
		   String cpContent = new String(data, "UTF-8");
		   return cpContent.contains( ArtifactContainer.ID.toOSString());
		   
		  } catch (IOException e) {
		   log.error("Could not determine if AC is active. Assuming it is not ...", e);
		   return false;
		  }
	}
	
	/**
	 * create a backup file form the {@link File} file, by deleting an existing backup, and renaming the file
	 */
	public static boolean backupFile( File file) {
		File backup = new File( file.getAbsolutePath() + ".bak");
		if (backup.exists()) {
			backup.delete();
		}
		return new File(file.getAbsolutePath()).renameTo(backup);
	}
	
	/**
	 * check if the {@link File} file can be written to 
	 */
	public static boolean checkWriteable( File file) {
		if (	
				(file.exists()) &&
				(file.canWrite() == false)
			){
			String msg ="Cannot write to file [" + file.getAbsolutePath()+ "]";
			log.error(msg);
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);	
			return false;
		}	
		return true;
	}
	
}
