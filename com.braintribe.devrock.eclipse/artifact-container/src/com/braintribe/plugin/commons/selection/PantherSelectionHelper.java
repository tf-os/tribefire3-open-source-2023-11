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
package com.braintribe.plugin.commons.selection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.process.repository.process.SourceRepositoryAccess;
import com.braintribe.build.process.repository.process.SourceRepositoryAccessException;
import com.braintribe.build.process.repository.process.git.GitRepositoryAccess;
import com.braintribe.build.process.repository.process.svn.SvnRepositoryAccess;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryKind;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;

/**
 * @author pit
 *
 */
public class PantherSelectionHelper {	
	
	private static final String BRAINTRIBE_CODEBASE = "braintribe codebase";
	private static final String URL_PROTOCOL_FILE = "file";
	private static final String URL_PROTOCOL_HTTPS = "https";
	private static final String URL_PROTOCOL_HTTP = "http";
	private static ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	
	/**
	 * converts the {@link SourceArtifact} to a simple string 
	 * @param artifact - the {@link SourceArtifact} to format 
	 * @return - the result {@link String} 
	 */
	public static String sourceArtifactToString( SourceArtifact artifact){
		 return artifact.getGroupId() +":" + artifact.getArtifactId() + "#" + artifact.getVersion(); 
	}
	
	
	/**
	 * translate old style data into the new format
	 * @throws SelectionException
	 */
	public static void primeRepositoryInformation() throws SelectionException {
		SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		List<SourceRepositoryPairing> sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();
		if (sourceRepositoryPairings.size() == 0) {			
			String rawWorkingCopy = svnPreferences.getWorkingCopy();
			if (rawWorkingCopy == null || rawWorkingCopy.length() == 0) {
				rawWorkingCopy = System.getenv( "BT__ARTIFACTS_HOME");
			}
			String resolvedWorkingCopy = null;
			if (rawWorkingCopy != null) {						
				resolvedWorkingCopy = plugin.getVirtualPropertyResolver().resolve(rawWorkingCopy);
			}
			
			if (resolvedWorkingCopy == null || resolvedWorkingCopy.length() == 0) {
				String msg = "cannot find sensible setting for a working copy ";				
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
				plugin.log(status);		
				throw new SelectionException(msg);
			}
			
			SourceRepository localRepository = SourceRepository.T.create();
			localRepository.setName(BRAINTRIBE_CODEBASE);
			localRepository.setRepoUrl(URL_PROTOCOL_FILE + ":" + resolvedWorkingCopy);
			
			SourceRepository remoteRepository = SourceRepository.T.create();
			remoteRepository.setName(BRAINTRIBE_CODEBASE);
			String fullRepositoryUrl = svnPreferences.getUrl();
			if (fullRepositoryUrl == null) {
				SvnRepositoryAccess svnRepositoryAccess = new SvnRepositoryAccess();
				try {					
					fullRepositoryUrl = svnRepositoryAccess.getBackingUrlOfWorkingCopy(resolvedWorkingCopy);			
				} catch (SourceRepositoryAccessException e1) {
					fullRepositoryUrl = "https://svn.braintribe.com/repo/master/Development/artifacts";
				}
			}
			// test url 
			try {
				 @SuppressWarnings("unused")
				URL url = new URL( fullRepositoryUrl);
			} catch (MalformedURLException e) {
				String msg = "cannot extract valid URL from [" + fullRepositoryUrl + "]";				
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				plugin.log(status);			
				throw new SelectionException(msg);
			}			
			remoteRepository.setRepoUrl( fullRepositoryUrl);					
			
			SourceRepositoryPairing pairing = SourceRepositoryPairing.T.create();
			pairing.setName(BRAINTRIBE_CODEBASE);
			pairing.setLocalRepresentation(localRepository);
			pairing.setRemoteRepresentation(remoteRepository);
			pairing.setRemoteRepresentationKind(SourceRepositoryKind.svn);
			sourceRepositoryPairings.add(pairing);					
		}
	
	}
	
	/**
	 * returns the matching {@link SourceRepositoryPairing} for a certain {@link SourceRepository}
	 * @param sourceRepository - the {@link SourceRepository} to search the owning pairing of
	 * @return - the {@link SourceRepositoryPairing} that contains the {@link SourceRepository} (actually, the first matching) 
	 * @throws SelectionException - if no match is found 
	 */
	public static SourceRepositoryPairing getMatchingRepositoryPairing( SourceRepository sourceRepository) throws SelectionException {
		SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		List<SourceRepositoryPairing> sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();
		URL url;
		try {
			url = new URL(sourceRepository.getRepoUrl());
		} catch (MalformedURLException e) {
			String msg = "cannot extract valid URL from [" + sourceRepository.getRepoUrl() + "]";				
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			plugin.log(status);			
			throw new SelectionException(msg);
		}
		String protocol = url.getProtocol();
		String comparisonToken = sourceRepository.getRepoUrl();
		for (SourceRepositoryPairing pairing : sourceRepositoryPairings) {
			SourceRepository repositoryToMatch;
			if (protocol.equalsIgnoreCase(URL_PROTOCOL_FILE)) {
				repositoryToMatch = pairing.getLocalRepresentation();			
			}
			else {
				repositoryToMatch = pairing.getRemoteRepresentation();				
			}
			String suspectToken = repositoryToMatch.getRepoUrl();
			if (comparisonToken.equalsIgnoreCase(suspectToken)) {
				return pairing;
			}
		}
		throw new SelectionException("not matching repository pairing found for [" + comparisonToken + "]");		
	}
	
	/**
	 * returns the matching other {@link SourceRepository} that is the counterpart in the {@link SourceRepositoryPairing}
	 * @param sourceRepository - the identifying {@link SourceRepository}
	 * @return - the counterpart {@link SourceRepository} - local if you entered a remote, remote if you entered a local 
	 * @throws SelectionException
	 */
	public static SourceRepository getMatchingPairedRepository( SourceRepository sourceRepository) throws SelectionException {
		SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		List<SourceRepositoryPairing> sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();
		URL url;
		try {
			url = new URL(sourceRepository.getRepoUrl());
		} catch (MalformedURLException e) {
			String msg = "cannot extract valid URL from [" + sourceRepository.getRepoUrl() + "]";				
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			plugin.log(status);			
			throw new SelectionException(msg);
		}
		String protocol = url.getProtocol();
		String comparisonToken = sourceRepository.getRepoUrl();
		for (SourceRepositoryPairing pairing : sourceRepositoryPairings) {
			SourceRepository repositoryToMatch;
			SourceRepository repositoryToReturn;
			if (protocol.equalsIgnoreCase(URL_PROTOCOL_FILE)) {
				repositoryToMatch = pairing.getLocalRepresentation();
				repositoryToReturn = pairing.getRemoteRepresentation();
			}
			else {
				repositoryToMatch = pairing.getRemoteRepresentation();
				repositoryToReturn = pairing.getLocalRepresentation();
			}
			String suspectToken = repositoryToMatch.getRepoUrl();
			if (comparisonToken.equalsIgnoreCase(suspectToken)) {
				return repositoryToReturn;
			}
		}
		throw new SelectionException("not matching repository counterpart found for [" + comparisonToken + "]");		
	}
	
	/**
	 * find the .project file that is linked to the {@link SourceArtifact}
	 * @param s
	 * @return
	 * @throws SelectionException
	 */
	public static File determineProjectFile( SourceArtifact s) throws SelectionException {
		
		return determineAssociatedFile(s, ".project");
	}
	
	public static File determineAssociatedFile( SourceArtifact s, String filename) throws SelectionException {
		
		File projectFile;
		URL url;
		SourceRepository sourceRepository = s.getRepository();		
		if (sourceRepository == null) {
			String msg="cannot process [" + sourceArtifactToString(s) + "] as no source repository information is attached";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
			plugin.log(status);			
			throw new SelectionException(msg);
		}
		try {
			url = new URL( sourceRepository.getRepoUrl() + "/" + s.getPath());
		} catch (MalformedURLException e1) {
			String msg="cannot determine origin of [" + sourceArtifactToString(s) + "] as the URL is invalid";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
			plugin.log(status);			
			throw new SelectionException(msg);
		}
		String protocol = url.getProtocol();				
		
		if (protocol.equalsIgnoreCase( URL_PROTOCOL_HTTP) || protocol.equalsIgnoreCase( URL_PROTOCOL_HTTPS)){
			// switch repository .. 
			
			// check out : project directory is the same as remote, but the url's modified so that the source repository's url is replaced by the working copy			
			// find counterpart of remote repository
			//SourceRepository localRepository = getMatchingPairedRepository(sourceRepository);
			SourceRepositoryPairing pairing = getMatchingRepositoryPairing(sourceRepository);
			SourceRepository localRepository = pairing.getLocalRepresentation();
			
			URL localUrl;
			try {
				localUrl = new URL( localRepository.getRepoUrl());
			} catch (MalformedURLException e1) {
				String msg="cannot determine origin of [" + sourceArtifactToString(s) + "] as the URL is invalid";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
				plugin.log(status);			
				throw new SelectionException(msg);
			}
					
			String localWorkingCopyDirectory = localUrl.getFile() + File.separator + s.getPath();  
			File projectDirectory = new File( localWorkingCopyDirectory);
			// if project doesn't exist on local working copy, do a checkout.
			if (!projectDirectory.exists()) {
				try {
					SourceRepositoryAccess repositoryAccess;
					switch( pairing.getRemoteRepresentationKind()) {
						case git:						
							repositoryAccess = new GitRepositoryAccess();
							break;
						case svn:					
						default:
							repositoryAccess = new SvnRepositoryAccess();
							break;				
						}
					repositoryAccess.checkout(s.getPath(), localWorkingCopyDirectory);					
				} catch (SourceRepositoryAccessException e) {					
					String msg="cannot run checkout for [" + sourceArtifactToString(s) + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
					plugin.log(status);			
					return null;						
				}				
			}
			projectFile = new File( projectDirectory, filename);									
		}
		else if (protocol.equalsIgnoreCase(URL_PROTOCOL_FILE)){ // file protocol : local working copy 
			projectFile = new File( url.getFile(), filename);
		}
		else {
			String msg="unsupported protocol [" + protocol + "] detected in [" + sourceArtifactToString(s);
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			plugin.log(status);			
			return null;						
		}
		return projectFile;
	}
	
	public static SourceRepository findMatchingLocalRepresentationSourceRepositoryFromPath( String path) {
		SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		List<SourceRepositoryPairing> sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();
		String testPath = path.replace( File.separator, "/").toUpperCase();
		for (SourceRepositoryPairing pairing : sourceRepositoryPairings) {
			SourceRepository localRepresentation = pairing.getLocalRepresentation();
			try {
				URL url = new URL(localRepresentation.getRepoUrl());			
				String checkPath = url.getFile();
				if (testPath.startsWith( checkPath.toUpperCase())) {
					return localRepresentation;
				}
			} catch (MalformedURLException e) {
				String msg = "invalid url [" + localRepresentation.getRepoUrl() +  "] found";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				plugin.log(status);			
			}
		}
		return null;
	}
	
}
