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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalExpert;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.generics.commons.CommonConversions;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
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
import com.braintribe.model.maven.settings.Server;

/**
 * quick implementation of an expert for remote repositories as pendant to the {@link FileSystemExpert}
 *  
 * @author pit
 *
 */
public class RemoteRepositoryExpert {
	
	private static Logger log = Logger.getLogger(RemoteRepositoryExpert.class);
	private HttpAccess httpAccess = new HttpAccess();
	private HttpRetrievalExpert expert = new HttpRetrievalExpert( httpAccess);
	private static RemoteRepositoryExpert instance;
	
	public RemoteRepositoryExpert() {
		instance = this;
	}
	
	private static HttpRetrievalExpert getHttpRetrievalExpert() {
		if (instance == null) 
			instance = new RemoteRepositoryExpert();		
		return instance.expert;
	}
	public void closeHttpAccessContext() {
		if (instance != null && instance.httpAccess != null) {
			instance.httpAccess.closeContext();
		}
	}
	
	/**
	 * returns a {@link Map} of {@link Solution} to qualified location of the solution 
	 * @param contextId - the context id (required by MC, not used) 
	 * @param setting - the {@link RepositorySetting} containing the url and user/pwd 
	 * @param dependency - the {@link Dependency} to resolve	 
	 * @return - all found solutions for this dependency 
	 */
	public static Map<Solution, String> extractSolutionsFromRemoteRepository(ScanContext scanContext, RepositorySetting setting, Dependency dependency) {
		Map<Solution, String> result = new HashMap<Solution, String>();
		try {	
			Server server = CommonConversions.serverFrom(setting);
			String location = setting.getUrl() + "/" + dependency.getGroupId().replace('.', '/') + "/" + dependency.getArtifactId();
			
			VersionRange matchRange = dependency.getVersionRange();
			// interval: iterate over all versions existing, and return matches 
			if (matchRange.getInterval()) {
				List<String> versions = getHttpRetrievalExpert().extractVersionDirectoriesFromRepository(location, server);
				for (String versionAsString : versions) {
					try {
						Version version = VersionProcessor.createFromString( versionAsString);
						if (VersionRangeProcessor.matches(matchRange, version)) {
							String solutionLocation = location + "/" + versionAsString;
							Solution [] solutions = extractSolutions( scanContext, solutionLocation, server);
							for (Solution solution : solutions) {
								result.put( solution, solutionLocation);
							}
						}
					} catch (VersionProcessingException e) {
						log.error("cannot compare range and versions", e);
					}
				}
			}
			else {
				// non interval - just get the single matching 			
				String solutionLocation = location + "/" + VersionRangeProcessor.toString(matchRange);
				Solution [] solutions = extractSolutions( scanContext, solutionLocation, server);
				for (Solution solution : solutions) {
					result.put( solution, solutionLocation);
				}				
			}
			
		}  catch (Exception e) {
			//documented empty block
			log.warn("caught exception");
		}		
			
		return result;
	}
	
	/**
	 * extract all solutions residing in the passed location (should only be one, but could be several, maven crap) 
	 * @param contextId - 
	 * @param location - the solution's location 
	 * @param server - the {@link Server} to query 
	 * @return - an array of {@link Solution}
	 */
	private static Solution [] extractSolutions(ScanContext scanContext, String location, Server server) {
		List<Solution> result = new ArrayList<Solution>();
		try {
			List<String> fileNames = getHttpRetrievalExpert().extractFilenamesFromRepository(location, server);
			for (String name : fileNames) {
				if (name.endsWith( ".pom")) {
					try {
						File tempFile = TempFileHelper.createTempFileFromFilename( name.substring(name.lastIndexOf('/')));
						File file = getHttpRetrievalExpert().extractFileFromRepository(name, tempFile.getAbsolutePath(), server);
						if (file != null && file.exists()) {
							ArtifactPomReader reader = GreyfaceScope.getScope().getPomReader();				
							//reader.setValidating( scanContext.getValidatePoms());
							Solution solution = reader.read(scanContext.getContextId(), tempFile.getAbsolutePath());
							result.add(solution);
						}
					} catch (Exception e) {
						String msg = "cannot retrieve pom for expected solution from [" + location + "]";
						log.error( msg, e);
						GreyfaceStatus status = new GreyfaceStatus( msg, e);
						GreyfacePlugin.getInstance().getLog().log(status);		
					}
				}
			}
		} catch (RepositoryAccessException e) {
			String msg = "cannot retrieve files for expected solution from [" + location + "]";
			log.error( msg, e);
			GreyfaceStatus status = new GreyfaceStatus( msg, e);
			GreyfacePlugin.getInstance().getLog().log(status);	
		}		
		return result.toArray( new Solution[0]);
	}
	

	public static void enrichSolutionFromRemoteRepository(Solution solution, RepositorySetting source, String location) {
		 
		try {
			String prefix = solution.getArtifactId() + "-" + VersionProcessor.toString(solution.getVersion());
			Server server = CommonConversions.serverFrom( source);			
			List<String> fileNames = getHttpRetrievalExpert().extractFilenamesFromRepository(location, server);
			for (String fileName : fileNames) {
				fileName = fileName.substring( fileName.lastIndexOf( '/')+1);
				if (fileName.startsWith(prefix) == false) {
					continue;
				}
				String rest = fileName.substring( prefix.length());
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
					classifier = rest.substring(1, p);
				}
				
				PartTuple tuple = PartTupleProcessor.create();
				if (classifier != null)
					tuple.setClassifier(classifier);
				tuple.setType(extension);
				
				Part part = Part.T.create();
				ArtifactProcessor.transferIdentification(part, solution);
				part.setType(tuple);				
				part.setLocation( source.getUrl() + "/" + solution.getGroupId().replace('.', '/') + "/" + solution.getArtifactId() +"/" + VersionProcessor.toString( solution.getVersion()) + "/" + fileName);
				solution.getParts().add(part);
			}
		} catch (RepositoryAccessException e) {
			log.error("cannot access remote repository", e);			
		}	
	}

}
