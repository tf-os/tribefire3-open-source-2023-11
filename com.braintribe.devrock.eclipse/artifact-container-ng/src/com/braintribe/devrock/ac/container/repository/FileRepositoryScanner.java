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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.api.concurrent.CustomThreadFactory;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.resource.FileResource;

public class FileRepositoryScanner {

	public static List<AnalysisArtifact> scanRepository(File root) {
		if (!root.exists()) {
			return Collections.emptyList();
		}		
		Set<AnalysisArtifact> scanned = new ScanJob().scan( root);					
		return new ArrayList<>(scanned);
	}
	
	
	public static class ScanJob {

		// Creating a new thread-pool has almost no overhead in comparison to scanning lots of folders
		// Our tasks are extremely lightweight, so Executors.newCachedThreadPool() would impair performance due to excessive number of threads
		private final ExecutorService executorService = Executors.newFixedThreadPool(//
				Runtime.getRuntime().availableProcessors(), //
				CustomThreadFactory.create().namePrefix("file-repository-scanner"));

		private final AtomicInteger submittedJobs = new AtomicInteger(0);
		private final CountDownLatch cdl = new CountDownLatch(1);
		private final Map<File, File> poms = new ConcurrentHashMap<>();

		public Set<AnalysisArtifact> scan(File directory) {
			Set<File> poms = findAllPomsIn(directory);

			// This doesn't get faster with parallel stream or even executor service. Arguably slower.
			return poms.stream() //
					.map(this::pomToIdentification) //
					.filter(id -> id != null) //				
					.collect(Collectors.toSet());
		}		
		

		private Set<File> findAllPomsIn(File dir) {
			findAllPomsInParallel(dir);

			if (submittedJobs.get() > 0)
				await();

			executorService.shutdown();

			return poms.keySet();
		}

		private void await() {
			try {
				cdl.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		

		private void findAllPomsInParallel(File dir) {
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
					poms.put(pom, pom);
					return;
				}
			}

			submittedJobs.incrementAndGet();
			executorService.submit(() -> {
				for (File subDir : dir.listFiles(File::isDirectory))
					findAllPomsInParallel(subDir);

				if (submittedJobs.decrementAndGet() == 0)
					cdl.countDown();
			});

		}

		private AnalysisArtifact pomToIdentification(File pom) {
			Maybe<CompiledArtifact> maybeAritfact = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pom);
			if (maybeAritfact.isSatisfied()) {
				EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.from(maybeAritfact.get());								
				AnalysisArtifact aa = AnalysisArtifact.T.create();
				aa.setGroupId( ecai.getGroupId());
				aa.setArtifactId( ecai.getArtifactId());
				aa.setVersion( ecai.getVersion().asString());
				aa.setArchetype( ecai.getArchetype());
				
				Part pomPart = Part.T.create();								
				FileResource resource = FileResource.T.create();
				resource.setName(pom.getName());
				resource.setPath(pom.getAbsolutePath());
				resource.setFileSize(pom.length());				
				pomPart.setResource(resource);
				aa.getParts().put("pom", pomPart);
				
				return aa;

			} else {
				ArtifactContainerPlugin.instance().log(new ArtifactContainerStatus(
						"cannot identify [" + pom.getAbsolutePath() + "] " + maybeAritfact.whyUnsatisfied().stringify(), IStatus.ERROR));
				return null;
			}
		}

	}
}
