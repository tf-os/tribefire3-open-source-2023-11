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

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerSolutionTuple;
import com.braintribe.model.malaclypse.container.ContainerPersistence;
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
public class ArtifactContainerCompressedSolutionTuplePersistenceExpert {
	private static Logger log = Logger.getLogger(ArtifactContainerCompressedSolutionTuplePersistenceExpert.class);
	private static final boolean USE_ARCHIVES = false;
	private static final String CONTENT_XML = "content.xml";	
	private ArtifactContainerSolutionTuplePersistenceExpert delegate;
	static final String DEPENDENCY_FILE = ".artifact.container.dependencies.zip";
	private StaxMarshaller marshaller = new StaxMarshaller();	
	
	@Configurable @Required
	public void setDelegate(ArtifactContainerSolutionTuplePersistenceExpert delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * get the proper file location 
	 */
	private File deriveFile( IProject project) {
		String path = ArtifactContainerPlugin.getInstance().getStateLocation().toOSString();
		String name = path + File.separator + project.getName() + DEPENDENCY_FILE;
		return new File( name);
	}
	public ContainerPersistence decode(IProject project) {
		File file = deriveFile( project);
		if (!file.exists()) {		
			ContainerPersistence tuple  = delegate.decode( project);
			if (tuple != null) {
				delegate.dropFile( project);				
			}
			return tuple;
		}
		if (USE_ARCHIVES)
			return decodeViaArchives(project.getName(), file);
		else 
			return decodeViaStream(project.getName(), file);
	}
	
	/**
	 * decode aka read the persisted data of the {@link ArtifactContainer} attached to {@link IJavaProject}
	 * @param project - the {@link IJavaProject} the container's attached to 
	 * @return - the {@link ArtifactContainerSolutionTuple} found 
	 */
	public ContainerPersistence decodeViaArchives(String prjName, File file){
			
		InputStream stream = null;
		try {
			ZipContext context = Archives.zip().from(file);
			stream = context.getEntry( CONTENT_XML).getPayload();								
			ContainerPersistence tuple = (ContainerPersistence) marshaller.unmarshall(stream);
			return tuple;
		} catch (MarshallException e) {
			String msg="cannot unmarshall persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName + "]"; 			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		} catch (ArchivesException e) {
			String msg="cannot open stream to persisted container data from [" + file.getAbsolutePath() + "] for project [" + prjName+ "]"; 
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			return null;
		}		
		finally {
			IOTools.closeQuietly(stream);
		}
	}
	
	private ContainerPersistence decodeViaStream(String prjName, File file) {
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
					ContainerPersistence cp = (ContainerPersistence) marshaller.unmarshall( zin);
					return cp;
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
	
	public void encode( IProject project, ContainerPersistence tuple){
		File file = deriveFile(project);
		String prjName = project.getName();
		if (USE_ARCHIVES) 
			encodeViaArchive(prjName, file, tuple);
		else
			encodeViaStream(prjName, file, tuple);
	}
	
	/**
	 * store the {@link ArtifactContainer}'s {@link ArtifactContainerSolutionTuple}
	 * @param project - the {@link IJavaProject} the container is attached to 
	 * @param tuple - the {@link ArtifactContainerSolutionTuple}
	 */
	public void encodeViaArchive( String prjName, File file, ContainerPersistence tuple){		
		File tempFile = null;
		OutputStream stream = null;
		try {
			tempFile = TemporaryFilestorage.createTempFile(DEPENDENCY_FILE, null);
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
	
	private void encodeViaStream(String prjName, File file, ContainerPersistence tuple){
		long before = System.currentTimeMillis();
		try (
				OutputStream out = new FileOutputStream( file);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				ZipOutputStream zout = new ZipOutputStream(bout);
		) {
			zout.putNextEntry(new ZipEntry(CONTENT_XML));
			marshaller.marshall( zout, tuple, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.low));
			zout.closeEntry();
		}
		catch (Exception e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";		
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		long after = System.currentTimeMillis();
		
		log.debug("persisting container data for [" + prjName + "] took [" + (after-before) + "] ms");
		
	}
	
	public static void main(String[] args) {
		File contents = new File( "res/persist");
		File input = new File( contents, "input");
		File output = new File( contents, "output");
		
		File [] files = input.listFiles();
		
		ArtifactContainerCompressedSolutionTuplePersistenceExpert expert = new ArtifactContainerCompressedSolutionTuplePersistenceExpert();
		
		int i = 0;
		long before = System.currentTimeMillis();
		for (File file : files) {
			if (!file.getName().endsWith( "dependencies.zip")) {
				System.out.println("filtered [" + file.getName() + "]");
				continue;
			}
			else {
				System.out.println("processing [" + file.getName() + "]");
				i++;
			}
			long beforeRead = System.currentTimeMillis();
			ContainerPersistence decodedViaStream = expert.decodeViaStream("first", file);
			long beforeWrite = System.currentTimeMillis();
			expert.encodeViaStream("first", new File( output, file.getName()), decodedViaStream);
			long afterWrite = System.currentTimeMillis();
			
			System.out.println("reading took [" + (beforeWrite-beforeRead) + "] ms");
			System.out.println("writing took [" + (afterWrite-beforeWrite) + "] ms");
		}
		long after = System.currentTimeMillis();
		System.out.println( "processing [" + i + "] container files took [" + (after-before) + "] ms");
	}
}
