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
package com.braintribe.devrock.artifactcontainer.ui.wizard;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.process.listener.MessageType;
import com.braintribe.build.process.listener.ProcessNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * simple working copy cloner/copier for artifacts
 * @author pit
 *
 */
public class ArtifactCloner {

	private static final String FILE_POM = "pom.xml";
	private static final String FILE_BUILD = "build.xml";
	private static final String FILE_PROJECT = ".project";
	
	private String [] filesToCopy = new String [] { FILE_POM, FILE_PROJECT, FILE_BUILD, ".svnignore", ".classpath", "container.xml", ".container.cfg.xml"};
	private String [] directoriesToCreate = new String [] {"src", };
	
	private List<String> directoriesNotToCopy = Arrays.asList(new String [] {"bin", "classes", "build", "dist"});
	private ProcessNotificationListener listener;		
	
	private File workingCopy;
	private File sourceDirectory;
	private File targetDirectory;
	
	@Configurable @Required
	public void setWorkingCopy(File workingCopy) {
		this.workingCopy = workingCopy;
	}
	
	@Configurable
	public void setListener(ProcessNotificationListener listener) {
		this.listener = listener;
	}
	
	/**
	 * clones an artifact, i.e. copies all directories (foremost src), besides bin, classes, build, dist
	 * @param source - the {@link Artifact} describing the source 
	 * @param target - the {@link Artifact} describing the target
	 * @throws ArtifactClonerException - arrgh
	 */
	public File clone(Artifact source, Artifact target) throws ArtifactClonerException {
		ensureDirectories(source, target);		
		copyDirectory( sourceDirectory, targetDirectory);
		return patch( target);
	}
	
	/**
	 * recursively copy a directory and its content 
	 * @param source - the {@link File} representing the source 
	 * @param target - the {@link File} representing the target 
	 */
	private void copyDirectory( File source, File target) {
		File [] files = source.listFiles();
		if (files.length == 0) {
			target.mkdirs();
			return;
		}		
		for (File file : files) {			
			String name = file.getName();
			File targetFile = new File( target, name);
			if (file.isDirectory()) {
				if (directoriesNotToCopy.contains(name))
					continue;			
				File targetDirectory = targetFile;
				targetDirectory.mkdirs();
				copyDirectory( file, targetDirectory);
			}
			else {
				if (listener != null) {
					listener.acknowledgeProcessNotification( MessageType.info, "copying [" + file.getAbsolutePath() + "] to [" + targetFile.getAbsolutePath() + "]");
				}
				FileTools.copyFile( file, targetFile);
			}
		}
	}
	
	/**
	 * copy the relevant files from an artifact into a new one 
	 * @param source - te {@link Artifact} describing the source 
	 * @param target - the {@link Artifact} describing the target 
	 * @throws ArtifactClonerException - arrgh
	 */
	public File copy(Artifact source, Artifact target) throws ArtifactClonerException{
		ensureDirectories(source, target);
		for (String name : filesToCopy) {
			File sourceFile = new File(  sourceDirectory, name);
			if (!sourceFile.exists())
				continue;
			File targetFile = new File( targetDirectory, name);
			if (listener != null) {
				listener.acknowledgeProcessNotification( MessageType.info, "copying [" + sourceFile.getAbsolutePath() + "] to [" + targetFile.getAbsolutePath() + "]");
			}
			FileTools.copyFile( sourceFile, targetFile);
		}

		for (String name : directoriesToCreate) {
			File targetFile = new File( targetDirectory, name);
			targetFile.mkdir();
		}
		
		return patch( target);		
	}


	/**
	 * make sure the directories representing the artifacts are ok, source existing, target not existing 
	 * @param source - te {@link Artifact} describing the source 
	 * @param target - the {@link Artifact} describing the target 
	 * @throws ArtifactClonerException - arrgh
	 */
	private void ensureDirectories(Artifact source, Artifact target) throws ArtifactClonerException {
		sourceDirectory = retrieveBuildFilePrefix(source);
		if (!sourceDirectory.exists()) {
			throw new ArtifactClonerException("source directory [" + sourceDirectory.getAbsolutePath() + "] doesn't exist");
		}
		
		targetDirectory = retrieveBuildFilePrefix(target);
		if (targetDirectory.exists()) {
			throw new ArtifactClonerException("target directory [" + targetDirectory.getAbsolutePath() + "] exists");
		}
		targetDirectory.mkdirs();
	}
	
	/**
	 * build proper file name from an artifact 
	 * @param artifact - the {@link Artifact} to get the working copy location 
	 * @return - a {@link File} representing the working copy location 
	 */
	private File retrieveBuildFilePrefix( Artifact artifact) {	
		return new File( workingCopy, artifact.getGroupId().replace(".", File.separator) + File.separator + artifact.getArtifactId() + File.separator + VersionProcessor.toString( artifact.getVersion()));
	}
	
	/**
	 * patch all relevant files 
	 * @param target - the {@link Artifact} that represents the target 
	 * @throws ArtifactClonerException - arrgh
	 */
	private File patch( Artifact target) throws ArtifactClonerException {		
		File prefix = retrieveBuildFilePrefix(target);
		
		File project = new File( prefix, FILE_PROJECT);
		if (listener != null) {
			listener.acknowledgeProcessNotification(MessageType.info, "Patching [" + project.getAbsolutePath() + "]");
		}
		patchProject(project, target);
		//
		File build = new File( prefix, FILE_BUILD);
		if (listener != null) {
			listener.acknowledgeProcessNotification(MessageType.info, "Patching [" + build.getAbsolutePath() + "]");
		}
		patchBuild(build, target);
		//
		File pom = new File( prefix, FILE_POM);
		if (listener != null) {
			listener.acknowledgeProcessNotification(MessageType.info, "Patching [" + pom.getAbsolutePath() + "]");
		}
		patchPom(pom, target);			
		
		return project;
	}
	
	/**
	 * patch the .project file 
	 * @param file - the {@link File} to patch
	 * @param target - the {@link Artifact} to patch it with
	 * @throws ArtifactClonerException - arrgh
	 */
	private void patchProject( File file, Artifact target) throws ArtifactClonerException{		
		Document doc;
		try {
			doc = DomParser.load().from(file);
		} catch (DomParserException e) {
			String msg = "cannot load file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
		
		try {
			Element name = DomUtils.getElementByPath( doc.getDocumentElement(), "name", true);
			name.setTextContent( target.getArtifactId() + "-" + VersionProcessor.toString(target.getVersion()));
		} catch (DOMException e) {
			String msg = "cannot patch file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		} 
		try {
			DomParser.write().from(doc).to(file);
		} catch (DomParserException e) {
			String msg = "cannot write file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
	}
	/**
	 * patch the build.xml file 
	 * @param file - the {@link File} to patch
	 * @param target - the {@link Artifact} to patch it with
	 * @throws ArtifactClonerException - arrgh
	 */
	private void patchBuild( File file, Artifact target) throws ArtifactClonerException{
		Document doc;
		try {
			doc = DomParser.load().from(file);
		} catch (DomParserException e) {
			String msg = "cannot load file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
		
		try {
			doc.getDocumentElement().setAttribute("name", target.getArtifactId());
		} catch (DOMException e) {
			String msg = "cannot patch file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}				
		try {
			DomParser.write().from(doc).to(file);
		} catch (DomParserException e) {
			String msg = "cannot write file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
			
	}
	/**
	 * patch the pom.xml file
	 * @param file - the {@link File} to patch
	 * @param target - the {@link Artifact} to patch it with
	 * @throws ArtifactClonerException - arrgh
	 */
	private void patchPom( File file, Artifact target) throws ArtifactClonerException{
		Document doc;
		try {
			doc = DomParser.load().from(file);
		} catch (DomParserException e) {
			String msg = "cannot load file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
		
		try {
			Element grpE = DomUtils.getElementByPath( doc.getDocumentElement(), "groupId", true);
			grpE.setTextContent( target.getGroupId());
			Element artE = DomUtils.getElementByPath( doc.getDocumentElement(), "artifactId", true);
			artE.setTextContent( target.getArtifactId());
			Element vrsE = DomUtils.getElementByPath( doc.getDocumentElement(), "version", true);
			vrsE.setTextContent( VersionProcessor.toString( target.getVersion()));
		} catch (DOMException e) {
			String msg = "cannot patch file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}			
		
		try {
			DomParser.write().from(doc).to(file);
		} catch (DomParserException e) {
			String msg = "cannot write file [" + file.getAbsolutePath() + "]";
			throw new ArtifactClonerException(msg, e);
		}
	}
	
	public static void main( String [] args) {
		boolean copy = true;
		ArtifactCloner cloner = new ArtifactCloner();
		cloner.setWorkingCopy( new File(System.getenv( "BT__ARTIFACTS_HOME")));
		
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-clone")) {
				copy = false;
				continue;
			}
			if (arg.equalsIgnoreCase( "-copy")) {
				copy = true;
				continue;
			}
			String [] tuple = arg.split("->");
			if (tuple.length != 2) {
				System.err.println("wrong parameter [" + arg + "]");
				continue;
			}
			try {
				Artifact source = NameParser.parseCondensedArtifactName( tuple[0]);
				Artifact target = NameParser.parseCondensedArtifactName( tuple[1]);
				
				if (copy) {
					try {
						cloner.copy(source, target);
					} catch (ArtifactClonerException e) {
						System.err.println("cannot copy [" + tuple[0] + "] to [" + tuple[1] + "] as " + e);
					}
				}
				else {
					try {
						cloner.clone(source, target);
					} catch (ArtifactClonerException e) {
						System.err.println("cannot copy [" + tuple[0] + "] to [" + tuple[1] + "] as " + e);
					}
				}
			} catch (NameParserException e) {
				System.err.println("cannot parse[" + tuple[0] + "] to [" + tuple[1] + "] into artifacts");
			}
		}
	}
}
