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
package com.braintribe.devrock.importer.scanner;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static java.util.Collections.emptySet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.potential.Potential;
import com.braintribe.devrock.api.concurrent.CustomThreadFactory;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataDecodeFailure;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataDecodingNoData;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataEncodeFailure;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;

/**
 * the scanner scans the 'scanRepositories' for sources of artifacts
 * 
 *  <br/>
 *  
 * @author pit
 *
 */
public class SourceScanner {
	private static Logger log = Logger.getLogger(SourceScanner.class);
	private static final String PAYLOAD = "payload.yaml";	
	
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	
	private boolean isActive = false;

	public Set<EnhancedCompiledArtifactIdentification> scanSourceRepositories() {
		return scanSourceRepositories( DevrockPlugin.envBridge().getScanDirectories());
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public Set<EnhancedCompiledArtifactIdentification> scanSourceRepositories(List<File> repositoriesToScan) {
		if (isEmpty(repositoriesToScan))
			return emptySet();

		isActive = true;
		Set<EnhancedCompiledArtifactIdentification> result = new HashSet<>();
		for (File sourceRepository : repositoriesToScan) {
			long before = System.nanoTime();
			Set<EnhancedCompiledArtifactIdentification> scanned = new ScanJob().scan( sourceRepository);
			result.addAll( scanned);

			long after = System.nanoTime();		
			double diff = (after - before) / 1E6; // in ms
			
			log.debug("scanning [" + sourceRepository + "] for [" + scanned.size() + "] artifacts [" + diff + "] ms");
		}

		isActive = false;
		return result;
	}

	public static class ScanJob {

		// Creating a new thread-pool has almost no overhead in comparison to scanning lots of folders
		// Our tasks are extremely lightweight, so Executors.newCachedThreadPool() would impair performance due to excessive number of threads
		private final ExecutorService executorService = Executors.newFixedThreadPool(//
				Runtime.getRuntime().availableProcessors(), //
				CustomThreadFactory.create().namePrefix("source-scanner"));

		private final AtomicInteger submittedJobs = new AtomicInteger(0);
		private final CountDownLatch cdl = new CountDownLatch(1);
		private final Map<File, File> poms = new ConcurrentHashMap<>();

		public Set<EnhancedCompiledArtifactIdentification> scan(File directory) {
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
			File pom = new File(dir, "pom.xml");
			if (pom.exists()) {
				poms.put(pom, pom);
				return;
			}

			submittedJobs.incrementAndGet();
			executorService.submit(() -> {
				for (File subDir : dir.listFiles(File::isDirectory))
					findAllPomsInParallel(subDir);

				if (submittedJobs.decrementAndGet() == 0)
					cdl.countDown();
			});

		}

		private EnhancedCompiledArtifactIdentification pomToIdentification(File pom) {
			Maybe<CompiledArtifact> maybeAritfact = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pom);
			if (maybeAritfact.isSatisfied()) {
				EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.from(maybeAritfact.get());
				ecai.setOrigin(pom.getParent());

				return ecai;

			} else {
				DevrockPlugin.instance().log(new DevrockPluginStatus(
						"cannot identify [" + pom.getAbsolutePath() + "] " + maybeAritfact.whyUnsatisfied().stringify(), IStatus.ERROR));
				return null;
			}
		}

	}
	
	/**
	 * decodes a zipped file with the stored {@link EnhancedCompiledArtifactIdentification}s from a earlier scan 
	 * @param file - the {@link File} that contains the data 
	 * @return - a {@link List} of {@link EnhancedCompiledArtifactIdentification} or null if nothing's returnable
	 */
	@SuppressWarnings("unchecked")
	public Potential<Set<EnhancedCompiledArtifactIdentification>, Reason> decode(File file) {
		if (!file.exists()) {
			ScanDataDecodingNoData sddnd = Reasons.build( ScanDataDecodingNoData.T).text( "no file [" + file.getAbsolutePath() + "] exists").toReason();			
			return Potential.empty( sddnd);
		}
		Set<EnhancedCompiledArtifactIdentification> payload = null;
		long before = System.currentTimeMillis();
		try ( 
				FileInputStream in = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(in);
                ZipInputStream zin = new ZipInputStream(bin);
			) {
			ZipEntry ze;
			do {
				ze = zin.getNextEntry();
				if (ze == null) {
					String msg="cannot unmarshall file [" + file.getAbsolutePath() + "] because no compressed data is found it zip"; 
					DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
					DevrockPlugin.instance().log(status);	
				}
				if (ze.getName().equals( PAYLOAD)) {
					payload = (Set<EnhancedCompiledArtifactIdentification>) marshaller.unmarshall( zin);
					break;
				}
			} while (true);			
		}
		catch (Exception e) {
			String msg="cannot unmarshall persisted scan data from [" + file.getAbsolutePath() + "]"; 
			log.error( msg, e);
			ScanDataDecodeFailure sddf = Reasons.build( ScanDataDecodeFailure.T).text(msg).toReason();			
			return Potential.empty( sddf);
				
		}		
		long after = System.currentTimeMillis();		
		log.debug("unmarshalling source repository data from [" + file.getAbsolutePath() + "] took [" + (after-before) + "] ms");
		return Potential.fill( payload);
	}
	
	/**
	 * encodes a previous scan result to a zipped file 
	 * @param file - the {@link File} to store the data 
	 * @param ecais - a {@link List} of {@link EnhancedCompiledArtifactIdentification}
	 */
	public Reason encode(File file, Collection<EnhancedCompiledArtifactIdentification> ecais) {
		long before = System.currentTimeMillis();
		try (
				OutputStream out = new FileOutputStream( file);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				ZipOutputStream zout = new ZipOutputStream(bout);
		) {
			zout.putNextEntry(new ZipEntry(PAYLOAD));
			marshaller.marshall( zout, ecais, GmSerializationOptions.deriveDefaults().outputPrettiness( OutputPrettiness.low).build());
			zout.closeEntry();
		}
		catch (Exception e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";		
			log.error( msg,e);
			ScanDataEncodeFailure sdef = Reasons.build(ScanDataEncodeFailure.T).text(msg).toReason();			
			return sdef;
		}
		long after = System.currentTimeMillis();		
		log.debug("marshalling source repository data to [" + file.getAbsolutePath() + "] took [" + (after-before) + "] ms");
		return null;
	}
	
	public static void main(String[] args) {
		SourceScanner scanner = new SourceScanner();
		List<File> roots = Collections.singletonList(new File("f:/works/COREDR-10/com.braintribe.devrock"));		
		
		Set<EnhancedCompiledArtifactIdentification> data = scanner.scanSourceRepositories( roots);
		File storage = new File("res/storage.zip");	
		Reason failed = scanner.encode(storage, data);
		if (failed != null) {
			log.error("failed encoding : " + failed.stringify());
		}
		
		Potential<Set<EnhancedCompiledArtifactIdentification>, Reason> pDecode = scanner.decode(storage);
		if (pDecode.isEmpty()) {
			log.error("failed decoding : " + pDecode.whyEmpty().stringify());
		}				
	}
}
