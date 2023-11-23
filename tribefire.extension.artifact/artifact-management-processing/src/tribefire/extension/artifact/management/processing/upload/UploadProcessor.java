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
package tribefire.extension.artifact.management.processing.upload;

import static com.braintribe.console.ConsoleOutputs.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.devrock.mc.api.repository.HttpUploader;
import com.braintribe.devrock.mc.api.repository.UploadContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.commons.McOutputs;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.mc.core.http.BasicFileSystemUploader;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.utils.DigestGenerator;


/**
 * actual processor for upload requests
 * @author pit
 *
 */
public class UploadProcessor {
	private static Logger log = Logger.getLogger(UploadProcessor.class);
	private List<File> poms;
	private String repositoryId;
	private boolean update;
	
	private RepositoryReflection repositoryReflection;
	private HttpUploader uploader;
	
	@Configurable @Required
	public void setUpdate(boolean update) {
		this.update = update;
	}

	@Configurable @Required
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	@Configurable @Required
	public void setUploader(HttpUploader uploader) {
		this.uploader = uploader;
	}
	
	@Configurable @Required
	public void setPoms(List<File> poms) {
		this.poms = poms;
	}
	
	@Configurable @Required
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}
	
	private class ProcessingContext {
		public ConfigurableConsoleOutputContainer oc = ConsoleOutputs.configurableSequence();
		private String entryEdge;
		private String edge;
		Map<File, CompiledArtifactIdentification> uploadContent = new HashMap<>();
		Map<CompiledArtifactIdentification, File> solutionToFile = new HashMap<>();
		Map<CompiledArtifactIdentification, List<File>> uploadFiles = new HashMap<>();
		Map<CompiledArtifactIdentification, List<File>> invalidFiles = new HashMap<>();
		List<File> invalidPoms = new ArrayList<>();
		List<Artifact> uploadArtifacts = new ArrayList<>();
		Map<CompiledArtifactIdentification, Map<File, Integer>> uploadResult = new HashMap<>();
		Repository repository;
		public File uploadProgressFile;
		
		public ProcessingContext() {			
			boolean ascii = false;
			if (ascii) {
				entryEdge = "───";
				edge = "├──";
			}
			else {
				entryEdge = "---";
				edge = "+--";
			}
		}				
		
		public void out(String msg) {
			oc.append( ConsoleOutputs.brightBlack( msg));
		}
		
		public void out(CompiledArtifactIdentification solution) {			
			String directory = solutionToFile.get(solution).getParentFile().getAbsolutePath();
			
			oc.append( "\t" + entryEdge);
			oc.append(solution.asString());
			oc.append( ConsoleOutputs.brightBlack( "\t" + directory));
			oc.append("\n");
			
			
			Map<File,Integer> result = uploadResult.get(solution);
			List<File> parts = uploadFiles.get(solution);
			List<File> invalids = invalidFiles.get(solution);
			if (invalids != null) {
				for (File invalid : invalids) {
					parts.add( invalid);
					result.put(invalid, -1);
				}
			}
			
			for (File file : parts) {
				String name = file.getName();
				oc.append( "\t " + edge);
				Integer code = result.get(file);
				if (code == null) {
					oc.append( ConsoleOutputs.brightBlack( name + " (present file)"));
				}
				else if (code == 200) {					
					oc.append( ConsoleOutputs.green( name));
				}
				else if (code > 200 && code < 300) {
					oc.append( ConsoleOutputs.yellow( name + " (upload warning)"));
				}
				else if (code < 0) {
					oc.append( ConsoleOutputs.brightMagenta( name + " (invalid file)"));
				}
				else {
					oc.append( ConsoleOutputs.red( name + " (upload error)"));
				}				
				oc.append("\n");
			}
		}
	}
	
	/**
	 * scan the working directory for artifacts
	 */
	private void scanPayload(ProcessingContext oc) {		
		int size = poms.size();
		int i = 0;
		ConfigurableConsoleOutputContainer progressOutput = null;
		
		for (File pom : poms) {
			CompiledArtifactIdentification artifact;
			try {
				Maybe<CompiledArtifactIdentification> potential = DeclaredArtifactIdentificationExtractor.extractIdentification(pom);
				
				if (potential.isUnsatisfied()) {
					String msg = "cannot scan contents of [" + pom.getAbsolutePath() + "], skipped:\n" + potential.whyUnsatisfied().stringify();				
					log.error(msg);								
					oc.invalidPoms.add( pom);
					oc.oc.append( ConsoleOutputs.red( pom.getAbsolutePath() + " cannot be identified"));
					continue;
				}
				
				artifact = potential.get();
				
				
				oc.uploadContent.put( pom,  artifact);
				oc.solutionToFile.put( artifact, pom);
				File directory = pom.getParentFile();
				
				List<File> partFiles = new ArrayList<>(Arrays.asList(directory.listFiles()));
				String expectedPrefix = artifact.getArtifactId() + "-" + artifact.getVersion().asString();
				Iterator<File> iterator = partFiles.iterator();
				while(iterator.hasNext()) {
					File file = iterator.next();
					String name = file.getName();
					if (!name.startsWith(expectedPrefix)) {
						List<File> invalids = oc.invalidFiles.computeIfAbsent(artifact, k -> new ArrayList<>());
						invalids.add(file);
						iterator.remove();
					}
				}

				oc.uploadFiles.put( artifact, partFiles);
				
				Artifact uploadArtifact = buildArtifact(artifact, partFiles);
				oc.uploadArtifacts.add(uploadArtifact);
				
				i++;
				progressOutput = ConsoleOutputs.configurableSequence();
				progressOutput.resetPosition(true);
				progressOutput.append(String.format("Identified %d%% (%d/%d) artifacts with parts from descriptors\n", i * 100 / size, i, size));
				ConsoleOutputs.print(progressOutput);

			} catch (Exception e) {
				String msg = "cannot scan contents of [" + pom.getAbsolutePath() + "], skipped";				
				log.error( msg,e);								
				oc.invalidPoms.add( pom);
				oc.oc.append( ConsoleOutputs.red( pom.getAbsolutePath() + " cannot be identified"));
			} 
		}				
		
		if (progressOutput != null) {
			progressOutput.resetPosition(false);
			ConsoleOutputs.print(progressOutput);
		}
		
	}
	
	/**
	 * upload the collected data 
	 */
	private void uploadPayload(ProcessingContext pc) {
		
		Repository repository = pc.repository;
		
		final ArtifactResolution artifactResolution;
		
		UploadContext uploadContext = UploadContext.build().progressListener(a -> {
			ConsoleOutputContainer output = ConsoleOutputs.sequence(text("Uploaded: "), McOutputs.versionedArtifactIdentification(a));
			ConsoleOutputs.println(output);
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(pc.uploadProgressFile, true), "UTF-8")) {
				writer.write(a.asString());
				writer.write("\n");
			} catch (Exception e) {
				// ignore problem because this is just an optimization
			}
		}).done();
		
		if (repository instanceof MavenHttpRepository) {
			MavenHttpRepository httpRepository = (MavenHttpRepository)repository;
			artifactResolution = uploader.upload(uploadContext, httpRepository, pc.uploadArtifacts);	
		}
		else {
			MavenFileSystemRepository fileSystemRepository = (MavenFileSystemRepository)repository;
			BasicFileSystemUploader fileSystemUploader = new BasicFileSystemUploader();
			artifactResolution = fileSystemUploader.upload(uploadContext, fileSystemRepository, pc.uploadArtifacts);	
		}

		if (artifactResolution.hasFailed()) {
			throw new RuntimeException(artifactResolution.getFailure().stringify());
		}
	}
	
	private Artifact buildArtifact(CompiledArtifactIdentification ai, List<File> files) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId(ai.getGroupId());
		artifact.setArtifactId(ai.getArtifactId());
		artifact.setVersion(ai.getVersion().asString());
		
		Map<String, Part> parts = artifact.getParts();
		
		String fileNameBase = artifact.getArtifactId() + "-" + artifact.getVersion();
		
		for (File file : files) {
			FileResource fileResource = FileResource.T.create();
			fileResource.setName(file.getName());
			fileResource.setPath(file.getAbsolutePath());
			
			String name = file.getName();
			
			String classifierAndType = name.substring(fileNameBase.length());
			int extIndex = classifierAndType.indexOf('.');
			
			final String type;
			final String classifierPart;
			
			
			if (extIndex != -1) {
				type = classifierAndType.substring(extIndex + 1);
				classifierPart = classifierAndType.substring(0, extIndex);
			}
			else {
				type = null;
				classifierPart = classifierAndType;
			}
			
			final String classifier;
			
			if (classifierPart.startsWith("-")) {
				classifier = classifierPart.substring(1);
			}
			else {
				classifier = null;
			}

			
			Part part = Part.T.create();
			part.setClassifier(classifier);
			part.setType(type);
			part.setResource(fileResource);

			parts.put(PartIdentification.asString(part), part);
			
		}
		return artifact;
	}
	
	/**
	 * run the process
	 */
	public void execute() {
		ProcessingContext pc = new ProcessingContext();
		
		initRepository(pc);
		
		scanPayload(pc);

		updateProgressInfo(pc);
		
		uploadPayload(pc);		
		
		pc.out("found " + pc.uploadContent.size() + " artifacts to upload\n");
		for (CompiledArtifactIdentification solution : pc.uploadResult.keySet()) {
			pc.out(solution);
			pc.out("\n");
		}
		
		ConsoleOutputs.println(pc.oc);
		
		pc.uploadProgressFile.delete();
	}

	private void updateProgressInfo(ProcessingContext pc) {
		SortedSet<String> names = new TreeSet<>();
		pc.uploadContent.values().stream().map(CompiledArtifactIdentification::asString).forEach(names::add);
		
		StringBuilder builder = new StringBuilder();
		
		if (pc.repository instanceof MavenHttpRepository) {
			MavenHttpRepository httpRepository = (MavenHttpRepository)pc.repository;
			builder.append(httpRepository.getUrl());	
			builder.append(";");	
		}
		
		for (String name: names) {
			builder.append(name);
			builder.append(";");	
		}
		
		String hash = null;
		
		try {
			hash = DigestGenerator.stringDigestAsString(builder.toString(), "MD5");
		} catch (Exception e) {
			// ignore here because it can only be the missing alg
			hash = UUID.randomUUID().toString();
		}
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		pc.uploadProgressFile = new File(tempDir, "upload-artifacts-progress-" + hash + ".txt");
		
		Set<String> uploadedArtifactIndex = new HashSet<>();
		
		if (pc.uploadProgressFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pc.uploadProgressFile), "UTF-8"))) {
				String line = null;
				
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty())
						continue;
					
					uploadedArtifactIndex.add(line);
				}
			}
			catch (IOException e) {
				// ignore problems here
			}
			
			Iterator<Artifact> it = pc.uploadArtifacts.iterator();
			
			while (it.hasNext()) {
				Artifact artifact = it.next();
				
				if (uploadedArtifactIndex.contains(artifact.asString())) {
					ConsoleOutputContainer output = ConsoleOutputs.sequence(text("Skipped: "), McOutputs.versionedArtifactIdentification(artifact));
					ConsoleOutputs.println(output);

					it.remove();
				}
			}
		}
		
	}

	private void initRepository(ProcessingContext pc) {
		final Repository repository;
		if (repositoryId != null) {
			repository = repositoryReflection.getRepository(repositoryId);
			
			if (repository == null)
				throw new IllegalArgumentException("Repository with the id [" + repositoryId + "] is not defined in the repository configuration");
		}
		else {
			repository = repositoryReflection.getUploadRepository();
			if (repository == null)
				throw new IllegalArgumentException("No upload repository defined in repository configuration");
		}
	
		if (!(repository instanceof MavenHttpRepository || repository instanceof MavenFileSystemRepository)) {
			throw new IllegalArgumentException("Repository with the id [" + repositoryId + "] must be a MavenFileSystemRepository or a MavenHttpRepository.");
		}

		pc.repository = repository;
	}
	
}
 