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
package com.braintribe.devrock.greyface.process.retrieval;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalExpert;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.utils.FileTools;

/**
 * @author pit
 *
 */
public class GlobalDependencyResolver extends AbstractDependencyResolver {
	private static final String PROTOCOL_HTTP = "http";
	private static final String PROTOCOL_HTTPS = "https";
	private static final String PROTOCOL_FILE = "file";
	private static final String PROTOCOL_FILE_PREFIX = PROTOCOL_FILE + "//";
	
	private RepositorySetting setting;
	private HttpAccess httpAccess = new HttpAccess();
	
	@Configurable @Required
	public void setSetting(RepositorySetting setting) {
		this.setting = setting;
	}
		

	@Override
	public Part resolvePomPart(String contextId, Part part) throws ResolvingException {
		File target = null;
		try {			
			Part pomPart = ArtifactProcessor.createPartFromIdentification(part, part.getVersion(), PartTupleProcessor.createPomPartTuple());
			String name = NameParser.buildFileName(pomPart);
			//String localFileName =  NameParser.buildPartialPath(pomPart, setting.getUrl()) + "/" + name;
			target = TempFileHelper.createTempFileFromFilename(name);
			String remoteFileName = null;
			File file = null;
			URL url = new URL( setting.getUrl());
			String protocol = url.getProtocol();
			if (protocol.equalsIgnoreCase(PROTOCOL_HTTP) || protocol.equalsIgnoreCase( PROTOCOL_HTTPS)) {
				remoteFileName = RepositoryReflectionHelper.getSolutionUrlLocation(setting.getUrl(), pomPart) + "/" + name;
				
				Server server = Server.T.create();
				server.setUsername( setting.getUser());
				server.setPassword( setting.getPassword());
				
				file = httpAccess.require(target, remoteFileName, server, null);
			}
			else {
				String urlAsString = setting.getUrl();
				String remotePrefix = urlAsString.substring( PROTOCOL_FILE_PREFIX.length() + 1);
				remoteFileName = RepositoryReflectionHelper.getSolutionUrlLocation( remotePrefix, pomPart) + "/" + name;
				try {
					File sourceFile = new File( remoteFileName);
					if (sourceFile.exists()) {
						FileTools.copyFile( sourceFile, target);
						file = target;
					}
					else {
						file = null;
					}
				} catch (Exception e) {
					file = null;
				}				
			}
						
			
			if (file != null && file.exists()) {
				pomPart.setLocation( target.getAbsolutePath());			
				return pomPart;
			}
			target.delete();
			target = null;
			return null;				
		} catch (Throwable e) {
			String msg ="cannot resolve pom using [" + setting.getUrl() + "]";
			throw new ResolvingException(msg, e);
		} 	
		finally {
			if (target != null)
				target.deleteOnExit();	
		}
	}


	@Override
	public Set<Solution> resolveTopDependency(String contextId, Dependency dependency) throws ResolvingException {
		Set<Solution> result = new HashSet<Solution>();
		String remoteDirectoryName = RepositoryReflectionHelper.getArtifactUrlLocation( setting.getUrl(), dependency);
		HttpRetrievalExpert retrievalexpert = new HttpRetrievalExpert(httpAccess);
		Server server = Server.T.create();
		server.setUsername( setting.getUser());
		server.setPassword( setting.getPassword());
		MavenMetaData extractMavenMetaData = null;
		extractMavenMetaData = GreyfaceScope.getScope().getMavenMetaDataForRemoteRepositoryDirectory(remoteDirectoryName);
		boolean retrieved = false;
		if (extractMavenMetaData == null) {
			try {
				 extractMavenMetaData = retrievalexpert.extractMavenMetaData(  new FilesystemSemaphoreLockFactory(), remoteDirectoryName, server);
				 retrieved = true;
			} catch (RepositoryAccessException e) {
			}
		}				
		else {
			System.out.println("cached [" + remoteDirectoryName + "]");
		}
		List<Version> possibles = new ArrayList<Version>();
		if (extractMavenMetaData == null) {			
			try {
				List<String> versionList = retrievalexpert.extractVersionDirectoriesFromRepository( remoteDirectoryName, server);
				for (String versionAsString : versionList) {
					try {
						Version version = VersionProcessor.createFromString(versionAsString);
						if (VersionRangeProcessor.matches( dependency.getVersionRange(), version)) {
							possibles.add(version);
						}
					} catch (VersionProcessingException e) {
						continue;
					}
				}
				// write a temporary  metadata and store it with hin the scope. 
				MavenMetaData metaData = MavenMetaData.T.create();
				metaData.setGroupId( dependency.getGroupId());
				metaData.setArtifactId( dependency.getArtifactId());				
				Versioning versioning = Versioning.T.create();
				metaData.setVersioning(versioning);
				for (Version version : possibles) {
					versioning.getVersions().add(version);
				}
								
				GreyfaceScope.getScope().addMavenMetaDataForRemoteRepositoryDirectory(remoteDirectoryName, metaData);
				System.out.println("caching temporary [" + remoteDirectoryName + "]");
				
			} catch (RepositoryAccessException e) {
				throw new ResolvingException("cannot retrieve directories per enumeration of [" + NameParser.buildName(dependency) + "]", e);
			}			
		}
		else {
			if (retrieved) {
				GreyfaceScope.getScope().addMavenMetaDataForRemoteRepositoryDirectory(remoteDirectoryName,  extractMavenMetaData);				
				System.out.println("caching [" + remoteDirectoryName + "]");
			}
			Versioning versioning = extractMavenMetaData.getVersioning();
			if (versioning == null) {
				throw new ResolvingException("no versions found via metadata of [" + NameParser.buildName(dependency) + "]");
			}
			for (Version version : versioning.getVersions()) {				
				if (VersionRangeProcessor.matches( dependency.getVersionRange(), version)) {
					possibles.add(version);
				}				
			}
			// can't trust metadata ..  
			if (possibles.size() == 0 & !dependency.getVersionRange().getInterval()) {
				possibles.add(dependency.getVersionRange().getDirectMatch());				
			}
			
		}
		// 
		PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
		for (Version version : possibles) {
			Solution solution = Solution.T.create();
			ArtifactProcessor.transferIdentification(solution, dependency);
			solution.setVersion(version);						
			Part part = ArtifactProcessor.createPartFromIdentification(solution, version, pomPartTuple);
			Part pomPart = resolvePomPart(contextId, part);
			// only add the solution if the pom part is found
			if (pomPart != null) {
				solution.getParts().add(pomPart);
				result.add(solution);
			}
		}
		// if nothing's found, return a null result
		if (result.size() == 0)
			return null;
		return result;
		
	}
	
	

}
