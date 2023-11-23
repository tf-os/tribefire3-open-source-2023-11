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
package com.braintribe.devrock.greyface.process.upload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.listener.RepositoryAccessListener;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.devrock.greyface.GreyfaceException;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.process.notification.UploadContext;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessNotificator;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.devrock.greyface.view.tab.selection.SelectionCodingCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.utils.FileTools;

public class UploaderImpl implements Uploader, UploadProcessNotificator, RepositoryAccessListener {
	private static final String PROTOCOL_HTTP = "http";
	private static final String PROTOCOL_HTTPS = "https";
	private static final String PROTOCOL_FILE = "file";
	
	private static final boolean OVERWRITE = false;
	private static Logger log = Logger.getLogger(UploaderImpl.class);
	private List<UploadProcessListener> listeners = new ArrayList<UploadProcessListener>();	
	private CurrentUploadContext currentUploadContext;
	private int currentUploadCount = 0;
	private int currentUploadIndex = 0;
	private HttpAccess httpAccess = new HttpAccess();

	@Override
	public void addUploadProcessListener(UploadProcessListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeUploadProcessListener(UploadProcessListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void upload(IProgressMonitor monitor, UploadContext context) {		
		
		broadcastUploadBegin( context.getTarget());
		// single selected parts have override (as they're translated into solutions)
		if (context.getParts().size() > 0) {			
			try {
				uploadParts( monitor, context.getParts(), context);
			} catch (GreyfaceException e) {
				GreyfaceStatus status = new GreyfaceStatus( "cannot upload", e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}
		}
		else if (context.getSolutions().size() > 0) {
			try {
				uploadSolutions(monitor, context.getSolutions(), context);
			} catch (GreyfaceException e) {				
				GreyfaceStatus status = new GreyfaceStatus( "cannot upload", e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}			
		}				
				
		broadcastUploadEnd( context.getTarget());
	}
	
	
	/**
	 * uploads all parts of the passed solutions (if fake's not set) 
	 * @param monitor - the {@link IProgressMonitor} that controls canceling 
	 * @param solutions - the {@link List} of {@link Solution} (real or faked)
	 * @param context - the {@link UploadContext}
	 */
	private void uploadSolutions(IProgressMonitor monitor, List<Solution> solutions, UploadContext context) throws GreyfaceException {
					
		RepositorySetting target = context.getTarget();
		Set<RepositorySetting> sources = context.getSources();
		
		broadcastRootSolutions (target, context.getRootSolutions());
		
		// calculate how many parts there are to upload... 
		currentUploadCount = 0;
		for (Solution solution : solutions) {
			int numParts = solution.getParts().size(); 
			currentUploadCount += (numParts*3); // add hashes (might be duplicate by now)			
			currentUploadCount += 6; // solution's metadata, plus group metadata  
		}
		
		Server targetServer = Server.T.create();
		targetServer.setUsername( target.getUser());
		targetServer.setPassword( target.getPassword());
		
		currentUploadIndex = 0;
		monitor.beginTask( "uploading selected artifacts", currentUploadCount);
		
		for (Solution solution : solutions) {
			
			// 
			currentUploadContext = new CurrentUploadContext();
			currentUploadContext.setSolution(solution);
			currentUploadContext.setTarget(target);
			currentUploadContext.setMonitor(monitor);
			
			try {
				String groupRoot = target.getUrl() + "/" + solution.getGroupId().replace('.', '/') + "/" + solution.getArtifactId(); 
				String artifactRoot;
			
				artifactRoot = groupRoot + "/" + VersionProcessor.toString( solution.getVersion());
				downloadFilesToTempoaryLocation( solution, sources);							
				File solutionMetaData = downloadSolutionMetaDataFileToTempoaryLocation(solution, target);				
								
				Map<File, String> sourceToTargetMap = PartMapper.prepareBatchUpload(solution, groupRoot, artifactRoot, solutionMetaData, context.getPrunePom());
				
				
				monitor.subTask( "uploading artifact [" + NameParser.buildName(solution) + "]");
		
				// broadcast start of upload 
				broadcastSolutionUploadBegin(target, solution);
				Map<File, Integer> result;
				if (!GreyfacePlugin.getInstance().getGreyfacePreferences(false).getFakeUpload()) {
					String protocol = PROTOCOL_HTTP;
					try {
						URL url = new URL( target.getUrl());
						protocol = url.getProtocol();
					} catch (MalformedURLException e) {
						log.warn("cannot extract protocol of url [" + target.getUrl() + "], http chosen by default");
						e.printStackTrace();
					}
					if (protocol.equalsIgnoreCase(PROTOCOL_HTTP) || protocol.equalsIgnoreCase(PROTOCOL_HTTPS)) {
						result = httpAccess.upload(targetServer, sourceToTargetMap, OVERWRITE, this);
					}
					else {
						result = fileUpload(sourceToTargetMap, OVERWRITE, this);
					}
				}
				else {
					result = simulatedBatchUpload(target.getUser(), target.getUrl(), sourceToTargetMap, this);										
				}
				// reset the location of successful uploads to their target location
				for (Entry<File, Integer> entry : result.entrySet()) {
					int status = entry.getValue();
					if (status >= 200 && status < 300) {
						for (Part part : solution.getParts()) {
							String sourceLocation = entry.getKey().getAbsolutePath(); 
							String partLocation = part.getLocation();
							if (partLocation != null && partLocation.equalsIgnoreCase(sourceLocation)) {
								part.setLocation( sourceToTargetMap.get(entry.getKey()));
							}
						}
					}
				}
				// validate 
				boolean crcValidated = validate( sourceToTargetMap);
				
				// broadcast end of upload 
				if (crcValidated && currentUploadContext.isSuccess()) {
					broadcastSolutionUploadEnd(target, solution);
				} else {
					broadcastSolutionUploadFail(target, solution);
				}
				
			} catch (HttpRetrievalException e) {
				String msg = "cannot upload [" + NameParser.buildName(solution) + "] as [" + e.getMessage() + "]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
			} 
			finally {
				httpAccess.closeContext();
			}
			if (monitor.isCanceled())
				break;			
		}	
		monitor.done();
	}
	
	/**
	 * validate uploaded files via the CRC (MD5 and SHA1) - CURRENTLY NOT IMPLEMENTED 
	 * @param sourceToTargetMap - 
	 * @return - true if all files validate otherwise, it will notify for any failed 
	 */
	private boolean validate(Map<File, String> sourceToTargetMap) {
		// TODO : implement once it makes sense 
		// would need
		// a) create hashes of local files
		// b) create hashes of remote files (i.e. download the files again, create hashes, then compare) 
		// c) download hashes from remote to make sure that they are identical 
		// so there are 2 downloads involved of which we don't know either if they worked.. so 
		// relying on 1 successful upload seems to be the better choice 
		// if GF runs on the server, and doesn't upload, but actually has access the file system, well, then it makes sense.. 
		return true;
	}

	private Map<File, Integer> fileUpload(Map<File, String> sourceToTargetMap, boolean overwrite2, RepositoryAccessListener listener) {

		Map<File, Integer> sourceToStatusMap = new HashMap<File, Integer>();
		
		String prefix = PROTOCOL_FILE + "//";
		for (Entry<File, String> entry : sourceToTargetMap.entrySet()) {
			
			File source = entry.getKey();
			String target = entry.getValue();
			target = target.substring( prefix.length()+1);
		
			long before = System.nanoTime();						
			String shortSourceName = source.getName();		
					

			//					
			File targetDir = new File( target).getParentFile();
			targetDir.mkdirs();
			try {
				FileTools.copyFile( source, new File(target));
				sourceToStatusMap.put( source, 200);
			} catch (Exception e) {
				sourceToStatusMap.put( source, 500);
			}
		
			long after = System.nanoTime();
			listener.acknowledgeUploadSuccess(source.getAbsolutePath(), target, new Double((after - before) / 1E6).longValue());
			System.out.println( shortSourceName + " -> " + target);
					
		}
		return sourceToStatusMap;
	}

	/**
	 * simulate an upload to the target repository - 100% compatible with the actual upload function  	 
	 */
	private Map<File, Integer> simulatedBatchUpload( String user, String url, Map<File, String> sourceToTargetMap, RepositoryAccessListener listener) {
		Map<File, Integer> sourceToStatusMap = new HashMap<File, Integer>();
		
		GreyfacePlugin plugin = GreyfacePlugin.getInstance();
		boolean simulateErrors = plugin.getGreyfacePreferences( false).getSimulateErrors();
	
		String uploadVar = plugin.getGreyfacePreferences( false).getFakeUploadTarget();
		String uploadTarget = plugin.getVirtualPropertyResolver().resolve(uploadVar);
		
		for (Entry<File, String> entry : sourceToTargetMap.entrySet()) {
			
			File source = entry.getKey();
			String target = entry.getValue();
		
			long before = System.nanoTime();
			// while a while, 1 second for now
			try {
				Thread.sleep( 100);
			} catch (InterruptedException e) {				
			}
			
			String shortSourceName = source.getName();
			// all success for now
			double random = Math.random();
			
			if (simulateErrors && random < 0.05 ) {
				sourceToStatusMap.put( source, 403);
				listener.acknowledgeUploadFailure(source.getAbsolutePath(), target, "simulated upload error");
				System.err.println( shortSourceName + " -> " + target);				
			}
			else {				
				if (uploadTarget != null && uploadTarget.length() > 0) {
					// 					
					target = target.replace( url, uploadTarget);
					File targetDir = new File( target).getParentFile();
					targetDir.mkdirs();
					FileTools.copyFile( source, new File(target));
				}		
				sourceToStatusMap.put( source, 200);
				long after = System.nanoTime();
				listener.acknowledgeUploadSuccess(source.getAbsolutePath(), target, new Double((after - before) / 1E6).longValue());
				System.out.println( shortSourceName + " -> " + target);
			}		

		}
		return sourceToStatusMap;
	}
	
	/**
	 * upload only selected parts - fake solutions for them 
	 */
	private void uploadParts( IProgressMonitor monitor, List<Part> parts, UploadContext context) throws GreyfaceException {
		Map<Solution, Solution> solutions = CodingMap.createHashMapBased(new SelectionCodingCodec());					
		for (Part part : parts) {
			Solution solution = Solution.T.create();
			ArtifactProcessor.transferIdentification(solution, part);
			Solution stored = solutions.get(solution);
			if (stored == null) {
				stored = solution;
				solutions.put(stored, stored);
			}
			stored.getParts().add(part);					
		}		
		List<Solution> artificalSolutions = new ArrayList<Solution>();
		artificalSolutions.addAll( solutions.values());
		uploadSolutions(monitor, artificalSolutions, context);				
	}
	
	/**
	 * download all required files of a solution (unless it's a local file anyhow)
	 * @param solution - the {@link Solution} whose file we should download 
	 * @param sources - the {@link Set} of {@link RepositorySetting} the represents all sources 
	 */
	private void downloadFilesToTempoaryLocation( Solution solution, Set<RepositorySetting> sources) {
		Set<Part> parts = solution.getParts();
		for (Part part : parts) {
			RepositorySetting source = null;
			String location = part.getLocation();			
			for (RepositorySetting suspect : sources) {
				if (location.startsWith( suspect.getUrl())) {
					source = suspect;
					break;
				}
			}
			if (source == null || source.getPhonyLocal()) {
				if (log.isDebugEnabled()) {
					log.debug( "file [" + location + "] is a local file");
				}
				continue;
			}
			Server server = Server.T.create();
			server.setUsername( source.getUser());
			server.setPassword( source.getPassword());
			
			String sourceLocation = part.getLocation().replace('\\', '/');
			int p = sourceLocation.lastIndexOf('/');
			String name = sourceLocation.substring(p+1);
			File target = null;
			try {
				target = TempFileHelper.createTempFileFromFilename(name);				
				File file = httpAccess.acquire(target, sourceLocation, server, null);
				if (file != null && file.exists()) {
					part.setLocation( target.getAbsolutePath());							
				}
			} catch (IOException e) {
				String msg = "cannot create temporary file for [" + sourceLocation +"], no download";
				log.error( msg, e);
			} catch (HttpRetrievalException e) {
				String msg = "cannot download possible for [" + sourceLocation +"]";
				log.error( msg, e);
			}
			finally {
				if (target != null)
					target.deleteOnExit();	
			}
		}		
	}
	
	private File downloadSolutionMetaDataFileToTempoaryLocation( Solution solution, RepositorySetting targetSetting) {						
		String location = targetSetting.getUrl() + "/" + solution.getGroupId().replace('.', '/') + "/" + solution.getArtifactId() + "/maven-metadata.xml";							
		String sourceLocation = location.replace('\\', '/');
		int p = sourceLocation.lastIndexOf('/');
		String name = sourceLocation.substring(p+1);
		
		Server targetServer = Server.T.create();
		targetServer.setUsername( targetSetting.getUser());
		targetServer.setPassword( targetSetting.getPassword());
		
		File target = null;
		try {
			target = TempFileHelper.createTempFileFromFilename(".grp." + name);
			URL url = new URL( sourceLocation);
			File file = null;
			if (url.getProtocol().equalsIgnoreCase( PROTOCOL_HTTP) || url.getProtocol().equalsIgnoreCase( PROTOCOL_HTTPS)) { 			
				 file = httpAccess.acquire(target, sourceLocation, targetServer, null);
			}
			else {
				file = new File( sourceLocation.substring( (PROTOCOL_FILE + "//").length() + 1));
				if (file.exists()) {
					FileTools.copyFile( file, target);					
				}
				else {
					file = null;
				}
			}
			if (file != null && file.exists()) {
				return target;							
			}
		} catch (IOException e) {
			String msg = "cannot create temporary file for [" + sourceLocation +"], no download";
			log.error( msg, e);
		} catch (HttpRetrievalException e) {
			String msg = "cannot download possible for [" + sourceLocation +"]";
			log.error( msg, e);
		} catch (IllegalArgumentException e) {
			String msg = "invalid download argument for [" + sourceLocation +"]";
			log.error( msg, e);
		}
		finally {
			if (target != null)
				target.deleteOnExit();	
		}
		return null;				
	}
		
	
	private void broadcastRootSolutions( RepositorySetting target, Set<Solution> solutions) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeRootSolutions(target, solutions);
		}
	}
	
	private void broadcastUploadBegin( RepositorySetting target) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadBegin( target, currentUploadCount);
		}
	}
	
	private void broadcastUploadEnd( RepositorySetting target) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadEnd( target);
		}
	}
	
	private void broadcastSolutionUploadBegin( RepositorySetting target, Solution solution) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadSolutionBegin(target, solution);
		}
	}
	
	private void broadcastSolutionUploadEnd( RepositorySetting target, Solution solution) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadSolutionEnd(target, solution);
		}
	}
	
	private void broadcastSolutionUploadFail( RepositorySetting target, Solution solution) {
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadSolutionFail(target, solution);
		}
	}
	

	@Override
	public void acknowledgeUploadFailure(String source, String target, String reason) {
		currentUploadContext.setSuccess(false);
		String normalizedSource = source.replace( '\\', '/');
		// identify part
		Part part = null;
		for (Part suspect : currentUploadContext.getSolution().getParts()) {
			String suspectLocation = suspect.getLocation().replace('\\', '/');
			if (suspectLocation.equalsIgnoreCase( normalizedSource)) {
				part = suspect;
				break;
			}		
		}		
		currentUploadContext.getMonitor().worked( 1);
		
		if (part == null) {
			log.info( "no corresponding part found for [" + source + "]->[" + target + "] to signal fail");
			return;
		}
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeFailedPart( currentUploadContext.getTarget(), currentUploadContext.getSolution(), part, reason, currentUploadIndex);
		}
	}

	@Override	
	public void acknowledgeUploadSuccess(String source, String target, long time) {
		String normalizedSource = source.replace( '\\', '/');
		Part part = null;
		for (Part suspect : currentUploadContext.getSolution().getParts()) {
			String suspectLocation = suspect.getLocation().replace('\\', '/');
			if (suspectLocation.equalsIgnoreCase( normalizedSource)) {
				part = suspect;
				part.setLocation(target);
				break;
			}		
		}
				
		currentUploadContext.getMonitor().worked(1);
		
		if (part == null) {
			log.info( "no corresponding part found for [" + source + "]->[" + target + "] to signal success");
			return;
		}
		for (UploadProcessListener listener : listeners) {
			listener.acknowledgeUploadedPart(currentUploadContext.getTarget(), currentUploadContext.getSolution(), part, time, currentUploadIndex);
		}
	}

	@Override
	public void acknowledgeDeleteFailure(String arg0, String arg1) {}

	@Override
	public void acknowledgeDeleteSuccess(String arg0, long arg1) {}

	@Override
	public void acknowledgeDownloadFailure(String arg0, String arg1) {}

	@Override
	public void acknowledgeDownloadSuccess(String arg0, long arg1) {}
	
	
}
