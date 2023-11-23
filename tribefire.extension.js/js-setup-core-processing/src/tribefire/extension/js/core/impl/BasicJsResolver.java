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
package tribefire.extension.js.core.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

import tribefire.extension.js.core.api.JsResolver;

/**
 * an standard implementation of the {@link JsResolver}
 * @author pit
 *
 */
public class BasicJsResolver implements JsResolver {
	private static final String EXTENSION = "js.zip";

	private static Logger log = Logger.getLogger(BasicJsResolver.class);
	
	private String scopeId = UUID.randomUUID().toString();
	private BuildRangeDependencyResolver buildDependencyResolver;
	private ArtifactPomReader pomReader;
	private File localRepositoryPath;
	private MultiRepositorySolutionEnricher enricher;
	
	private boolean preferMinOverPretty;
	private boolean useSymbolicLink = true;
	
	private static YamlMarshaller marshaller = new YamlMarshaller();
	
	
	private PartTuple [] tuples = new PartTuple[] {PartTupleProcessor.fromString(EXTENSION), PartTupleProcessor.fromString("min", EXTENSION), PartTupleProcessor.fromString("asset", "man")};
	
	private LockFactory lockSupplier = new  FilesystemSemaphoreLockFactory(); 
	
	/**
	 * @param buildDependencyResolver - the {@link BuildRangeDependencyResolver} to use
	 */
	@Required @Configurable
	public void setBuildDependencyResolver(BuildRangeDependencyResolver buildDependencyResolver) {
		this.buildDependencyResolver = buildDependencyResolver;
	}
	
	/**
	 * @param pomReader - the {@link ArtifactPomReader} to use
	 */
	@Required @Configurable
	public void setPomReader(ArtifactPomReader pomReader) {
		this.pomReader = pomReader;
	}
	@Required @Configurable
	public void setLocalRepositoryPath(File localRepositoryPath) {
		this.localRepositoryPath = localRepositoryPath;
	}
	@Required @Configurable
	public void setEnricher(MultiRepositorySolutionEnricher enricher) {
		this.enricher = enricher;
	}
	
	@Configurable
	public void setPreferMinOverPretty(boolean preferMinOverPretty) {
		this.preferMinOverPretty = preferMinOverPretty;
	}
	
	@Configurable
	public void setUseSymbolicLink(boolean useSymbolicLink) {
		this.useSymbolicLink = useSymbolicLink;
	}
	
	public boolean filterSolution(Solution solution) {
		increaseResolvedArtifacts();
		return true;
	}

	private int resolvedArtifacts;
	private int resolvedLibs;
	private int establishedLibs;
	private int ignoredLibs;
	
	private static final String dynamicMsgLinking = "resolved artifacts: %3d\nresolved libs: %3d\nlinked libs: %3d\nignored libs: %3d\n";
	private static final String dynamicMsgCopying = "resolved artifacts: %3d\nresolved libs: %3d\ncopied libs: %3d\nignored libs: %3d\n";
	
	private Object outputMonitor = new Object();


	private void updateOutput() {
		String msg = String.format(useSymbolicLink? dynamicMsgLinking: dynamicMsgCopying, 
				resolvedArtifacts, resolvedLibs, establishedLibs, ignoredLibs);
		ConsoleOutputs.print(ConsoleOutputs.configurableSequence().resetPosition(true).append(msg));
	}
	
	
	private void resetOutput() {
		synchronized (outputMonitor) {
			resolvedArtifacts = 0;
			resolvedLibs = 0;
			establishedLibs = 0;
			ignoredLibs = 0;
			ConsoleOutputs.print("\n\n\n\n");
		}
	}
	
	private void increaseResolvedArtifacts() {
		synchronized (outputMonitor) {
			resolvedArtifacts++;
			updateOutput();
		}
	}
	private void increaseResolvedLibs() {
		synchronized (outputMonitor) {
			resolvedLibs++;
			updateOutput();
		}
	}
	private void increaseIgnoredLibs() {
		synchronized (outputMonitor) {
			ignoredLibs++;
			updateOutput();
		}
	}
	private void increaseEstablishedLibs() {
		synchronized (outputMonitor) {
			establishedLibs++;
			updateOutput();
		}
	}
	
	
	/**
	 * @param solution - the owning {@link Solution}
	 * @param tuple - the {@link PartTuple} we're looking for 
	 * @return - the {@link File} or null if not present
	 */
	private File getPartFile( Solution solution, PartTuple tuple) {
		for (Part part : solution.getParts()) {
			if (PartTupleProcessor.equals( part.getType(), tuple)) {
				if (part.getLocation() != null) {
					File file = new File( part.getLocation());
					if (file.exists())
						return file;
					break;
				}
			}
		}
		return null;
	}
	
	private boolean unpack(Solution terminal, Solution solution, File folder, PartTuple tuple) {
		File zipFile = getPartFile(solution, tuple);
		if (zipFile == null)
			return false;
		
		String prefix = tuple.getClassifier() != null && tuple.getClassifier().equalsIgnoreCase("min") ? "min" : "pretty";
		
		// if folder exists already, we must check for overwrites
		//File touchedDateFile = new File( folder, zipFile.getName() + ".touched");
		File touchedDateFile = new File( folder.getParentFile(), prefix + ".touched");
		Date zipDate = new Date(zipFile.lastModified());

		if (folder.exists()) { 			
			try (InputStream in = new FileInputStream( touchedDateFile)) {
				Date unpackedDate = (Date) marshaller.unmarshall(in);
				if (unpackedDate.compareTo(zipDate) == 0) {
					return true;
				}		
			}
			catch (Exception e) {				
				String msg = "cannot store the date of the file [" + zipFile.getAbsolutePath() + "] as referenced [" + NameParser.buildName( solution) + "]" + buildReferencedByStr(terminal);
				log.error(msg);
			}					 					
		}
		if (folder.exists()) {
			delete( folder);
		}
				

		File downloadFolder = new File( folder.getParent(), folder.getName() + ".download");
		downloadFolder.mkdir();		
		try {
			Archives.zip().from(zipFile).unpack( downloadFolder).close();
			makeFilesReadonly(downloadFolder);
		} catch (ArchivesException e) {
			downloadFolder.delete();
			String msg = "cannot unpack [" + zipFile.getAbsolutePath() + "] to [" + folder.getAbsolutePath() + "] related to [" + NameParser.buildName( solution) + "]" + buildReferencedByStr(terminal);
			log.error( msg);
			throw new IllegalStateException( msg, e);
		}

		if (!downloadFolder.renameTo(folder)) {
			String msg = "cannot rename [" + downloadFolder.getAbsolutePath() + "] to [" + folder.getAbsolutePath() + "] related to [" + NameParser.buildName( solution) + "]" + buildReferencedByStr(terminal);
			log.error( msg);
			throw new IllegalStateException( msg);
		}
		
		try (OutputStream out = new FileOutputStream( touchedDateFile)){
			marshaller.marshall(out, zipDate);				
		}	
		catch( Exception e) {
			String msg = "cannot store data for [" + zipFile.getAbsolutePath() + "] requested by [" + NameParser.buildName( solution) + "]" + buildReferencedByStr(terminal);
			log.error(msg);
		}
		
		return true;
	}
	
	private void makeFilesReadonly(File file) {
		if (file.isDirectory()) {
			for (File child: file.listFiles()) {
				makeFilesReadonly(child);
			}
		}
		else {
			file.setWritable(false, false);
		}
	}

	private String buildReferencedByStr(Solution terminal) {
		return terminal != null? " transitively referenced by [" + NameParser.buildName( terminal) + "]": "";
	}

	@Override
	public JsResolverResult resolve(File workingFolder, File jsRepositoryFolder) {
		File srcFolder = new File( workingFolder, "src");
		
		return resolve(workingFolder, jsRepositoryFolder, Collections.singletonMap(srcFolder, "_src"));
	}
	
	@Override
	public JsResolverResult resolve(File workingFolder, File jsRepositoryFolder, Map<File, String> linkFolders) {
		
		Set<File> resolvedLibFiles = new HashSet<>();
		
		// find pom.xml
		File pom = new File( workingFolder, "pom.xml");
		if (!pom.exists()) {
			throw new IllegalStateException("no pom.xml found in [" + workingFolder + "]");			
		}
		// read pom to retrieve solution
		Solution terminalSolution = pomReader.readPom( scopeId, pom);		
		
		ExceptionCollector exceptionCollector = new ExceptionCollector("JsResolver processing [" + NameParser.buildName(terminalSolution) + "]");
		
		//
		// local project instrumentation 
		//
		File libFolder = new File( workingFolder, "lib");
		libFolder.mkdir();
		
		for (Map.Entry<File, String> entry: linkFolders.entrySet()) {
			File folder = entry.getKey();
			String linkName = entry.getValue();
			File linkFile = new File( libFolder, linkName);
			
			folder.mkdirs();
			
			try {
				SymbolicLinker.ensureSymbolicLink(linkFile, folder, true);
				resolvedLibFiles.add(linkFile);
			} catch (RuntimeException e) {
				exceptionCollector.collect(e);
			}
		}
		
		//					
		// build-walk the dependencies
		// 
		Set<Solution> resolvedSolutions = buildDependencyResolver.resolve( terminalSolution.getDependencies());		
		if (resolvedSolutions == null || resolvedSolutions.size() == 0) {
			log.info("no dependencies found in artifact [" + NameParser.buildName(terminalSolution) + "] as defined in [" + pom.getAbsolutePath() + "]");
			return new JsResolverResult(terminalSolution, resolvedLibFiles, resolvedSolutions);
		}
		
		// iterate over solutions
		for (Solution solution : resolvedSolutions) {
			boolean solutionIsLocal = false;
			
			// determine if it's a project's solution or an remote artifact
			File solutionsPom = getPartFile(solution, PartTupleProcessor.createPomPartTuple());
			if (solutionsPom.getAbsolutePath().startsWith( localRepositoryPath.getAbsolutePath())) {
				enricher.enrichAndReflectDownload(scopeId, solution, Arrays.asList(tuples));
			}
			else {
				solutionIsLocal = true;
			}
			
			increaseResolvedLibs();
			
			// do not process pom-packaged poms here
			if (solution.getPackaging() != null && solution.getPackaging().equalsIgnoreCase("pom"))
				continue;
			
			if (!solutionIsLocal) {			
				handleArtifactFromRemote(jsRepositoryFolder, terminalSolution, exceptionCollector, libFolder, solution, resolvedLibFiles);
			}
			else {									
				handleLocalProjectArtifact(terminalSolution, exceptionCollector, libFolder, solution, resolvedLibFiles);				
			}
			
		}
		
		resetOutput();
		
		exceptionCollector.throwIfNotEmpty();		
		
		return new JsResolverResult(terminalSolution, resolvedLibFiles, resolvedSolutions);
	}
	
	@Override
	public void resolve(Collection<String> terminals, File jsRepository, File targetDirectory, File projectsDirectory) {
		
		Set<File> resolvedLibFiles = new HashSet<>();
		
		List<Dependency> dependencies = terminals.stream().map(NameParser::parseCondensedDependencyName).collect(Collectors.toList());
		
		// build-walk the dependencies
		Set<Solution> resolvedSolutions = buildDependencyResolver.resolve( dependencies);		
		
		// iterate over solutions
		for (Solution solution : resolvedSolutions) {
			// do not process pom-packaged poms here
			if (solution.getPackaging() != null && solution.getPackaging().equalsIgnoreCase("pom"))
				continue;
			
			ExceptionCollector exceptionCollector = new ExceptionCollector("JsResolver processing");
			
			boolean solutionIsLocal = false;
			
			// determine if it's a project's solution or an remote artifact
			File solutionsPom = getPartFile(solution, PartTupleProcessor.createPomPartTuple());
			if (solutionsPom.getAbsolutePath().startsWith( localRepositoryPath.getAbsolutePath())) {
				enricher.enrichAndReflectDownload(scopeId, solution, Arrays.asList(tuples));
			}
			else {
				solutionIsLocal = true;
			}			
			
			increaseResolvedLibs();
			
			if (!solutionIsLocal) {			
				handleArtifactFromRemote(jsRepository, null, exceptionCollector, targetDirectory, solution, resolvedLibFiles);
			}
			else {									
				handleLocalProjectArtifact(null, exceptionCollector, targetDirectory, solution, resolvedLibFiles);				
			}						
			exceptionCollector.throwIfNotEmpty();	
		}
	}

	/**
	 * handle a locally present js-project 
	 * @param terminalSolution - the {@link Solution} that is the current terminal
	 * @param exceptionCollector - the {@link ExceptionCollector}
	 * @param libFolder - the {@link File} pointing to the 'lib' folder 
	 * @param solution - the current {@link Solution} (direct or transitive dependency of terminal)
	 * @param resolvedLibs 
	 */
	private void handleLocalProjectArtifact(Solution terminalSolution, ExceptionCollector exceptionCollector, File libFolder, Solution solution, Set<File> resolvedLibFiles) {
		File pomFile = getPartFile(solution, PartTupleProcessor.createPomPartTuple());
		File projectDistFolder = new File( pomFile.getParentFile(), "src");
		projectDistFolder.mkdir();
		List<String> libEntryNames = SymbolicLinker.determineLinkName( solution);
		for (String libEntryName : libEntryNames) {
			File libEntryFile = new File( libFolder, libEntryName);
			
			try {
				copyOrLinkLib(terminalSolution, exceptionCollector, libEntryFile, projectDistFolder, true);
				increaseEstablishedLibs();
				resolvedLibFiles.add(libEntryFile);
				
			} catch (RuntimeException e) {
				exceptionCollector.collect(e);
			}
			
		}						
	}

	/**
	 * handle a remote artifact that is the product of a published js-project 
	 * @param jsRepositoryFolder - the js-repository folder (where the js-artifacts are unpacked to)
	 * @param terminalSolution - the {@link Solution} that is the current terminal
	 * @param exceptionCollector - the {@link ExceptionCollector}
	 * @param libFolder - the {@link File} pointing to the 'lib' folder 
	 * @param solution - the current {@link Solution} (direct or transitive dependency of terminal)
	 * @return 
	 */
	private void handleArtifactFromRemote(File jsRepositoryFolder, Solution terminalSolution, ExceptionCollector exceptionCollector, File libFolder, Solution solution, Set<File> resolvedLibFiles) {
		//
		// js-repository handling
		//
		// check if artifact exists in jsRepository 
		String jsArtifactName = solution.getGroupId() + "." + solution.getArtifactId() + "-" + VersionProcessor.toString( solution.getVersion());
		
		File jsArtifactFolder = new File( jsRepositoryFolder, jsArtifactName);
		
		File prettyJsFolder = new File( jsArtifactFolder, "pretty");
		boolean prettyHasBeenUnpacked = false;
		
		File minJsFolder = new File( jsArtifactFolder, "min");
		boolean minHasBeenUnpacked = false;
			
		ReadWriteLock readWriteLock = lockSupplier.getLockInstance(jsArtifactFolder);		
		Lock writeLock = readWriteLock.writeLock();

		try {
			writeLock.lock();
			// folder is of no importance, just make sure it exists
			if (!jsArtifactFolder.exists()) {							
				jsArtifactFolder.mkdirs();
			}	
			prettyHasBeenUnpacked = unpack(terminalSolution, solution, prettyJsFolder, PartTupleProcessor.fromString(EXTENSION));
			minHasBeenUnpacked = unpack( terminalSolution, solution, minJsFolder, PartTupleProcessor.fromString("min", EXTENSION));		
		}
		finally {
			writeLock.unlock();
		}
		
		// one of the folders must exist
		if (!minHasBeenUnpacked && !prettyHasBeenUnpacked) {
			boolean lenient = true;
			
			if (!lenient)
				exceptionCollector.collect( new IllegalStateException("at least one js.zip must be present for [" + NameParser.buildName(solution) + "]"));
				
			// temp solution - no js parts found
			increaseIgnoredLibs();
			
			return;
		}
		//
		// local project instrumentation
		// 
		 
		// lib entry, symbol links
		List<String> libEntryNames = SymbolicLinker.determineLinkName( solution);
	
		for (String libEntryName : libEntryNames) {
			File libEntryFile = new File( libFolder, libEntryName);
			
			createSymbolicLinkForRemoteArtifact(terminalSolution, exceptionCollector, jsArtifactFolder, prettyJsFolder, prettyHasBeenUnpacked, minJsFolder, minHasBeenUnpacked, libEntryFile);
			increaseEstablishedLibs();
			
			resolvedLibFiles.add(libEntryFile);
		}
	}

	private void createSymbolicLinkForRemoteArtifact(Solution terminalSolution, ExceptionCollector exceptionCollector, File jsArtifactFolder,
			File prettyJsFolder, boolean prettyHasBeenUnpacked, File minJsFolder, boolean minHasBeenUnpacked, File libEntryFile) {
		// determine what is the preferred link and what is the fallback..
		File mainPath, fallbackPath;
		boolean mainHasBeenUnpacked, fallbackHasBeenUnpacked;
		if (preferMinOverPretty) {
			mainPath = minJsFolder;
			mainHasBeenUnpacked = minHasBeenUnpacked;
			fallbackPath = prettyJsFolder;
			fallbackHasBeenUnpacked = prettyHasBeenUnpacked;
		}
		else {
			mainPath = prettyJsFolder;
			mainHasBeenUnpacked = prettyHasBeenUnpacked;
			fallbackPath = minJsFolder;		
			fallbackHasBeenUnpacked = minHasBeenUnpacked;
		}
		
		// 
		if (mainHasBeenUnpacked) { // main (either 'min' or 'pretty' depending of flag)
			copyOrLinkLib(terminalSolution, exceptionCollector, libEntryFile, mainPath, false);							
		} 
		else if (fallbackHasBeenUnpacked) { // fall back (either 'pretty' or 'min', depending on flag)
			copyOrLinkLib(terminalSolution, exceptionCollector, libEntryFile, fallbackPath, false);
		}					
		else {
			String msg = "cannot create symbolic link [" + libEntryFile.getAbsolutePath() + "]" + buildForStr(terminalSolution) + " as neither min or pretty directories exist in [" + jsArtifactFolder.getAbsolutePath() + "]";						
			exceptionCollector.collect( new IllegalStateException( msg));
		}
	}

	private void copyOrLinkLib(Solution terminalSolution, ExceptionCollector exceptionCollector, File libEntryFile,
			File target, boolean relativize) {
		try {
			if (useSymbolicLink) {
				SymbolicLinker.ensureSymbolicLink(libEntryFile, target, relativize);
			}
			else {
				if (libEntryFile.exists())
					FileTools.deleteRecursivelySymbolLinkAware(libEntryFile);
				
				SymbolicLinker.createCopy( target, libEntryFile);
			}
				
		} catch (RuntimeException e) {
			exceptionCollector.collect(e);
		}
	}
	
	private static String buildForStr(Solution terminal) {
		return terminal != null? " for [" + NameParser.buildName( terminal) + "]": "";
	}
	
	/**
	 * @param file - a single file or a directory
	 */
	private static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					delete( child);
				} 
				child.delete();			
			}
		}		
		file.delete();		
	}
	
}
