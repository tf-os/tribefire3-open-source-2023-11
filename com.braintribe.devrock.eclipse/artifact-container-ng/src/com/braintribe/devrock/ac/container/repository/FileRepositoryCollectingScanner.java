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
package com.braintribe.devrock.ac.container.repository;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.concurrent.CustomThreadFactory;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.resource.FileResource;

/**
 * a parallel scanner that scans poms and resolves their classpath
 * @author pit
 *
 */
public class FileRepositoryCollectingScanner {
	
	public static Map<File,AnalysisArtifact> scanRepository(MavenFileSystemRepository repository, FileRepositoryCollectingScannerListener listener) {
		if (!new File(repository.getRootPath()).exists()) {
			return Collections.emptyMap();
		}
		if (listener != null) {
			listener.acknowledgeScanStart();
		}
		Map<File, AnalysisArtifact> result = new ScanJob().scan( repository);
		if (listener != null) {
			listener.acknowledgeScanComplete();
		}
		return result;							
	}
	
	
	/**
	 * @author peter/pit
	 *
	 */
	public static class ScanJob {
		
		// Creating a new thread-pool has almost no overhead in comparison to scanning lots of folders
		// Our tasks are extremely lightweight, so Executors.newCachedThreadPool() would impair performance due to excessive number of threads
		private final ExecutorService executorService = Executors.newFixedThreadPool(//
				Runtime.getRuntime().availableProcessors(), //
				CustomThreadFactory.create().namePrefix("file-repository-scanner"));

		private final AtomicInteger submittedJobs = new AtomicInteger(0);
		private final CountDownLatch cdl = new CountDownLatch(1);
		private final Map<File, AnalysisArtifact> pomAndResolutions = new ConcurrentHashMap<>();

		public Map<File,AnalysisArtifact> scan(MavenFileSystemRepository repository) {
			Map<File, AnalysisArtifact> resolutions = findAllPomsIn(repository);
			return resolutions;						
		}		
		

		private Map<File,AnalysisArtifact> findAllPomsIn(MavenFileSystemRepository repository) {
			File dir = new File( repository.getRootPath());
			findAllPomsInParallel(repository.getName(), dir);

			if (submittedJobs.get() > 0)
				await();

			executorService.shutdown();

			return pomAndResolutions;
		}

		private void await() {
			try {
				cdl.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		

		/**
		 * @param repoId
		 * @param dir
		 */
		private void findAllPomsInParallel(String repoId, File dir) {
			// don't know how the pom's named, so find any poms here.. 
			File[] files = dir.listFiles(new FilenameFilter() {				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith( ".pom"))
						return true;
					return false;
				}
			});
			
			if (files != null && files.length > 0) {
				File pom = files[0];			
				if (pom.exists()) {
					// analyse
					AnalysisArtifact aa = resolve(repoId, pom);  	
					if (aa != null) {
						pomAndResolutions.put(pom.getParentFile(), aa);					
					}
					return;
				}
			}

			submittedJobs.incrementAndGet();
			executorService.submit(() -> {
				for (File subDir : dir.listFiles(File::isDirectory))
					findAllPomsInParallel(repoId, subDir);

				if (submittedJobs.decrementAndGet() == 0)
					cdl.countDown();
			});
		}
			
		private AnalysisArtifact resolve(String repoId, File pom) {
			Maybe<CompiledArtifact> maybeArtifact = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pom);
			if (maybeArtifact.isSatisfied()) {
				CompiledArtifact compiledArtifact = maybeArtifact.get();
				AnalysisArtifact aa = AnalysisArtifact.of(compiledArtifact);
				File parentDirectory = pom.getParentFile();
				// iterate 
				String mask = aa.getArtifactId() + "-" + aa.getVersion();
				
				File[] listOfFiles = parentDirectory.listFiles( new FilenameFilter() {				
					@Override
					public boolean accept(File dir, String name) {
						if (name.startsWith(mask))
							return true;
						return false;
					}
				});
				for (File file : listOfFiles) {
					String name = file.getName();
					int i = name.lastIndexOf('.');
					String type = name.substring( i+1);
					String namePart = name.substring(0, i);					
					String classifier = namePart.length() != mask.length() ?  namePart.substring( mask.length() +1) : null;
					
					Part part = Part.T.create();									
					part.setType( type);
					part.setClassifier(classifier);
					FileResource resource = FileResource.T.create();
					resource.setName(file.getName());
					resource.setPath(file.getAbsolutePath());
					resource.setFileSize(file.length());
					part.setResource(resource);
					part.setRepositoryOrigin( repoId);
					String partKey = classifier != null ? classifier + ":" + type : ":" + type;
					aa.getParts().put(partKey, part);
				}														
				return aa;

			} else {
				DevrockPlugin.instance().log(new DevrockPluginStatus(
						"cannot identify [" + pom.getAbsolutePath() + "] " + maybeArtifact.whyUnsatisfied().stringify(), IStatus.ERROR));
				return null;
			}
		}
	}
		
			
}
