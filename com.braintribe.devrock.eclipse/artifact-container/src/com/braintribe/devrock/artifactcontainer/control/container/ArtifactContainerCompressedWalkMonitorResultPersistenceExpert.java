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
package com.braintribe.devrock.artifactcontainer.control.container;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.views.dependency.ContainerMode;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerSolutionTuple;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

/**
 * a persistence expert that compresses the {@link ArtifactContainer}'s data before it's dumped, uses a standard non-compressing as delegate (if older, non compressed files are present)
 *  
 * @author pit
 *
 */
public class ArtifactContainerCompressedWalkMonitorResultPersistenceExpert {
	private static Logger log = Logger.getLogger(ArtifactContainerCompressedWalkMonitorResultPersistenceExpert.class);
	private static final boolean USE_ARCHIVES = false;
	private static final String CONTENT_XML = "monitor.xml";
	static final String COMPILE_MONITOR_FILE = ".artifact.container.compile.monitor.zip";
	static final String RUNTIME_MONITOR_FILE = ".artifact.container.runtime.monitor.zip";
	static final String TEMP_MONITOR_FILE = ".artifact.container.tmp.monitor.zip";
	private StaxMarshaller marshaller = new StaxMarshaller();	
	
	
	/**
	 * get the proper file location 	
	 */
	private File deriveFile( IProject project, ContainerMode mode) {
		String path = ArtifactContainerPlugin.getInstance().getStateLocation().toOSString();
		String name;
		String projectName = project != null ? project.getName() : "unkown";
		switch (mode) {
			case runtime:
				name = path + File.separator + projectName + RUNTIME_MONITOR_FILE;
				break;
			case compile:
			default:
				name = path + File.separator + projectName + COMPILE_MONITOR_FILE;
				break;					
		}
		return new File( name);
	}
	public WalkMonitoringResult decode(IProject project, ContainerMode mode) {
		File file = deriveFile( project, mode);
		if (!file.exists()) {
			return null;
		}
		if (USE_ARCHIVES)
			return decodeViaArchive( project.getName(), file, mode);
		else
			return decodeViaStream( project.getName(), file, mode);
	}
	/**
	 * decode aka read the persisted data of the {@link ArtifactContainer} attached to {@link IJavaProject} via Archives
	 * @prjName - the name of the project
	 * @file - the file to read
	 * @mode - the {@link ContainerMode} 
	 * @return - the {@link ArtifactContainerSolutionTuple} found 
	 */
	private WalkMonitoringResult decodeViaArchive(String prjName, File file, ContainerMode mode) {			
		InputStream stream = null;
		try {
			ZipContext context = Archives.zip().from(file);
			stream = context.getEntry( CONTENT_XML).getPayload();								
			WalkMonitoringResult tuple = (WalkMonitoringResult) marshaller.unmarshall(stream);
			return tuple;
		} catch (MarshallException e) {
			String msg="cannot unmarshall persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName + "]"; 
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		} catch (ArchivesException e) {
			String msg="cannot open stream to persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName + "]"; 
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		}		
		finally {
			IOTools.closeQuietly(stream);
		}
	}
	
	/**
	 * decode aka read the persisted data of the {@link ArtifactContainer} attached to {@link IJavaProject} directly
	 * @param prjName - the name of the project
	 * @param file - the file to read
	 * @param mode - the {@link ContainerMode} 
	 * @return - the {@link ArtifactContainerSolutionTuple} found 
	 */
	private WalkMonitoringResult decodeViaStream(String prjName, File file, ContainerMode mode) {
		try ( 
				FileInputStream in = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(in);
                ZipInputStream zin = new ZipInputStream(bin);
			) {
			ZipEntry ze;
			do {
				ze = zin.getNextEntry();
				if (ze == null) {
					String msg="cannot unmarshall persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName + "] as entry cannot be found"; 
					ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
					ArtifactContainerPlugin.getInstance().log(status);	
				}
				if (ze.getName().equals( CONTENT_XML)) {
					WalkMonitoringResult tuple = (WalkMonitoringResult) marshaller.unmarshall( zin);
					return tuple;
				}
			} while (true);
			
		}
		catch (Exception e) {
			String msg="cannot unmarshall persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName + "]"; 
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		return null;
	}
	
	
	public void encode( IProject project, WalkMonitoringResult tuple, ContainerMode mode){
		File file = deriveFile(project, mode);		
		if (USE_ARCHIVES)
			encodeViaArchive( project.getName(), file, tuple, mode);
		else 
			encodeViaStream( project.getName(), file, tuple, mode);
	}
	/**
	 * store the {@link ArtifactContainer}'s {@link ArtifactContainerSolutionTuple} via Archives
	 * @param file  
	 * @param tuple - the {@link ArtifactContainerSolutionTuple}
	 * @param mode - the {@link ContainerMode} associated
	 */
	private void encodeViaArchive( String prjName, File file, WalkMonitoringResult tuple, ContainerMode mode){
		File tempFile = null;
		OutputStream stream = null;
		try {
			tempFile = TemporaryFilestorage.createTempFile(TEMP_MONITOR_FILE, null);
			stream = new FileOutputStream(tempFile);			
			marshaller.marshall(stream, tuple, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.high));
			Archives.zip().add(CONTENT_XML, tempFile).to(file).close();			
		} catch (IOException e) {
			String msg ="cannot open stream to encoded container data to [" + file.getAbsolutePath() + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		} catch (MarshallException e) {
			String msg ="cannot marshall container data to [" + tempFile.getAbsolutePath() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		} catch (ArchivesException e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		finally {
			if (tempFile != null) {
				tempFile.delete();
			} 
		}
	}
	
	/**
	 * store the {@link ArtifactContainer}'s {@link ArtifactContainerSolutionTuple} via Archives
	 * @param file  
	 * @param tuple - the {@link ArtifactContainerSolutionTuple}
	 * @param mode - the {@link ContainerMode} associated
	 */
	private void encodeViaStream( String prjName, File file, WalkMonitoringResult tuple, ContainerMode mode){
		long before = System.currentTimeMillis();
		try (
				OutputStream out = new FileOutputStream( file);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				ZipOutputStream zout = new ZipOutputStream(bout);
		) {
			zout.putNextEntry(new ZipEntry(CONTENT_XML));
			marshaller.marshall( zout, tuple, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.high));
			zout.closeEntry();
		}
		catch (Exception e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		long after = System.currentTimeMillis();
		
		log.debug("persisting monitor data (mode [" + mode.toString() + "]) result for [" + prjName + "] took [" + (after-before) + "] ms");
		
	}
	
	public static void main(String[] args) {
		File contents = new File( "res/persist");
		File input = new File( contents, "input");
		File output = new File( contents, "output");
		
		File [] files = input.listFiles();
		
		
		ArtifactContainerCompressedWalkMonitorResultPersistenceExpert expert = new ArtifactContainerCompressedWalkMonitorResultPersistenceExpert();
		int i = 0;
		long before = System.currentTimeMillis();
		for (File file : files) {
			String name = file.getName();
			if (!name.endsWith( "monitor.zip")) {
				System.out.println("filtered [" + name + "]");
				continue;
			}
			else {
				System.out.println("processing [" + name + "]");
				i++;
			}
			ContainerMode mode = (name.endsWith("compile.monitor.zip")) ? ContainerMode.compile : ContainerMode.runtime;
					
			long beforeRead = System.currentTimeMillis();
			WalkMonitoringResult decodedViaStream = expert.decodeViaStream("first", file, mode);
			long beforeWrite = System.currentTimeMillis();
			expert.encodeViaStream("first", new File( output, name), decodedViaStream, mode);
			long afterWrite = System.currentTimeMillis();
			
			System.out.println("reading took [" + (beforeWrite-beforeRead) + "] ms");
			System.out.println("writing took [" + (afterWrite-beforeWrite) + "] ms");
						
		}
		long after = System.currentTimeMillis();
		System.out.println( "processing [" + i + "] monitor files took [" + (after-before) + "] ms");
	}
}
