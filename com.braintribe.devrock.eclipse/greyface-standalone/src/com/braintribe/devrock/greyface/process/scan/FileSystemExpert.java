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
package com.braintribe.devrock.greyface.process.scan;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class FileSystemExpert {
	private static Logger log = Logger.getLogger(FileSystemExpert.class);
	

	public static void enrichSolutionFromFileSystem(Solution solution, RepositorySetting setting, String directory){		
		Set<Part> parts = solution.getParts();
		if (parts == null) {
			parts = new HashSet<Part>();
			solution.setParts( parts);
		}
		
		
		String prefix = solution.getArtifactId() + "-" + VersionProcessor.toString(solution.getVersion());
		
		File [] files = new File(directory).listFiles();
		for (File file : files)  {
			
			String name = file.getName();
			if (name.startsWith(prefix) == false) {
				continue;
			}
			String rest = name.substring( prefix.length());
			int p = rest.lastIndexOf('.');
			if (p < 0)
				continue;
			String extension = rest.substring( p+1);
			// filter out hashes 
			if (
					extension.equalsIgnoreCase( "md5") ||
					extension.equalsIgnoreCase( "sha1")
					) {
				continue;
			}
			String classifier = null;
			if (p > 0) {
				classifier = rest.substring(0, p);
			}
			
			PartTuple tuple = PartTupleProcessor.create();
			if (classifier != null)
				tuple.setClassifier(classifier);
			tuple.setType(extension);
			
			Part part = Part.T.create();
			ArtifactProcessor.transferIdentification(part, solution);
			part.setLocation( file.getAbsolutePath());
			part.setType(tuple);
			solution.getParts().add(part);
			//System.out.println("adding part : " + name);
		}
						
	}
	
	public static Map<Solution, String> extractSolutionsFromMavenCompatibleFileSystem(ScanContext scanContext, RepositorySetting setting, Dependency dependency) {
		Map<Solution, String> result = new HashMap<Solution, String>();
		File rootDirectory = new File( setting.getUrl());
		String groupPath = dependency.getGroupId().replace('.',  '/');
		String artifactPath = dependency.getArtifactId();
		File groupDirectory = new File( rootDirectory, groupPath + "/" + artifactPath);
		if (!groupDirectory.exists()) {
			return result;
		}
		// list directories
		
		VersionRange range = dependency.getVersionRange();
		if (range.getInterval()) {
			// match their versions (names) with the range of the dependency 
			File [] directories = groupDirectory.listFiles( new FileFilter() {			
				@Override
				public boolean accept(File file) {
					if (file.getName().equalsIgnoreCase(".") || file.getName().equalsIgnoreCase(".."))
						return false;
					if (file.isDirectory())
						return true;
					return false;
				}
			});
			for (File directory : directories) {
				try {
					Version version = VersionProcessor.createFromString(directory.getName());
					if (VersionRangeProcessor.matches(range, version)) {
						Solution [] solutions = extractSolutions(scanContext, setting, directory);
						if (solutions != null) {
							for (Solution solution : solutions) {
								result.put(solution,  directory.getAbsolutePath());
							}
						}
					}
				} catch (VersionProcessingException e) {
					log.error( "cannot process directory name as a version or version range");
				} catch (PomReaderException e) {
					log.error("cannot read the pom in a directory");
				}
			}
		}
		else {
			String rangeAsString = VersionRangeProcessor.toString(range);
			File artifactDirectory = new File( groupDirectory, rangeAsString);
			try {
				Solution [] solutions = extractSolutions(scanContext, setting, artifactDirectory);
				if (solutions != null && solutions.length > 0) {
					result.put( solutions[0], artifactDirectory.getAbsolutePath());
				}
			} catch (PomReaderException e) {
				String msg = "cannot read at least one  pom file in [" + artifactDirectory.getAbsolutePath() + "]";
				log.error( msg, e);
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
				
			}
		}
		
		return result;
	}

	private static Solution [] extractSolutions(ScanContext context, RepositorySetting setting, File artifactDirectory) throws PomReaderException {
		File [] poms = artifactDirectory.listFiles( new FileFilter() {			
			@Override
			public boolean accept(File file) {
				if (file.getName().endsWith( ".pom"))
					return true;
				return false;
			}
		});
		Set<Solution> solutions = new HashSet<Solution>();
		
		ArtifactPomReader reader = GreyfaceScope.getScope().getPomReader();			
		//reader.setValidating( context.getValidatePoms());
		if (poms != null && poms.length > 0) {						
			for (int i = 0; i < poms.length; i++) {			
				File pom = poms[i];
				Solution solution = reader.read(context.getContextId(), pom.getAbsolutePath());		
				if (solution != null)
					solutions.add(solution);
				}
		}
		return solutions.toArray( new Solution[0]);
	}
		
	
	public static Map<Solution, String> extractSolutionsFromFileSystem(ScanContext scanContext, RepositorySetting setting, Dependency dependency){
		Map<Solution, String> result = new HashMap<Solution, String>();
		
		File directory = new File( setting.getUrl());
		Solution[] solutions;
		try {
			solutions = extractSolutions(scanContext, setting, directory);
		} catch (PomReaderException e) {
			log.error("cannot extract solutions from directory [" + directory.getAbsolutePath() + "]");
			return result;
		}
		if (solutions == null || solutions.length == 0)
			return result;
		
		for (Solution solution : solutions) {
			if (solution.getGroupId().equalsIgnoreCase( dependency.getGroupId()) == false)
				continue;
			if (solution.getArtifactId().equalsIgnoreCase( dependency.getArtifactId()) == false)
				continue;
			result.put( solution, directory.getAbsolutePath());
		}
		
		return result;
	}
}
