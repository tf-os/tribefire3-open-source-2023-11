// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules;

import static com.braintribe.build.cmd.assets.PlatformSetupProcessor.outFileTransfer;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.CLASSPATH_FILE_NAME;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.SOLUTIONS_FILE_NAME;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.copyToDir;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.delete;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.replaceFiles;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.styled;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleStyles.FG_CYAN;
import static com.braintribe.console.ConsoleStyles.FG_GREEN;
import static com.braintribe.console.ConsoleStyles.FG_RED;
import static com.braintribe.console.ConsoleStyles.FG_YELLOW;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.StringTools.removeLastNCharacters;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

/**
 * Merges source modules folder to target one. This assumes both folders were built for debug, i.e. each module has a lib sub-folder containing just
 * two files .solutions and .classpath;
 * 
 * @author peter.gazdik
 */
public class ModuleFolderMerger {

	public static void merge(File source, File target) {
		ModuleFolderMerger merger = new ModuleFolderMerger(source, target);
		merger.doMerge();
	}

	private final ModulesFolderAnalysis sourceAnalysis;
	private final ModulesFolderAnalysis targetAnalysis;

	private File maybeModule;

	private boolean moduleInconsistencyDetected;

	public ModuleFolderMerger(File source, File target) {
		this.sourceAnalysis = analyzeModuelsFolder(source);
		this.targetAnalysis = analyzeModuelsFolder(target);
	}

	private ModulesFolderAnalysis analyzeModuelsFolder(File modulesFolder) {
		ModulesFolderAnalysis result = new ModulesFolderAnalysis();
		result.folder = modulesFolder;

		for (File file : modulesFolder.listFiles())
			analyzePossibleModule(result, file);

		return result;
	}

	private void analyzePossibleModule(ModulesFolderAnalysis result, File maybeModule1) {
		maybeModule = maybeModule1;

		ModuleAnalysis ma = analyzeMaybeModule();
		if (ma != null)
			result.moduleAnalysisByName.put(ma.name, ma);
		else
			result.otherFiles.add(maybeModule);

	}

	private ModuleAnalysis analyzeMaybeModule() {
		if (!maybeModule.isDirectory())
			return null;

		File solutions = new File(maybeModule, SOLUTIONS_FILE_NAME);
		File classpath = new File(maybeModule, CLASSPATH_FILE_NAME);

		if (!solutions.exists() || !classpath.exists())
			return null;

		return analyzeModule(solutions, classpath);
	}

	private static final Set<String> MODULE_CP_FILES = asSet(SOLUTIONS_FILE_NAME, CLASSPATH_FILE_NAME);

	private ModuleAnalysis analyzeModule(File solutions, File classpath) {
		Map<String, String> solutionToClasspathEntry = mapClasspathEntries(solutions, classpath);

		ModuleAnalysis result = new ModuleAnalysis();
		result.name = maybeModule.getName();
		result.folder = maybeModule;
		result.solutions = solutions;
		result.classpath = classpath;
		result.solutionToClasspathEntry = solutionToClasspathEntry;
		result.otherFiles = asList(maybeModule.listFiles((d, name) -> !MODULE_CP_FILES.contains(name)));

		return result;
	}

	private Map<String, String> mapClasspathEntries(File solutions, File classpath) {
		List<String> solutionLines = FileTools.read(solutions).asLines();
		List<String> classpathLines = FileTools.read(classpath).asLines();

		if (solutionLines.size() != classpathLines.size())
			return null;

		Iterator<String> solutionIt = solutionLines.iterator();
		Iterator<String> classpathIt = classpathLines.iterator();

		Map<String, String> result = newLinkedMap();

		while (solutionIt.hasNext())
			result.put(removeVersion(solutionIt.next()), classpathIt.next());

		return result;
	}

	private String removeVersion(String solution) {
		String version = StringTools.findSuffix(solution, "#");
		String classifier = StringTools.findSuffixWithBoundary(solution, "|");

		return (version.isEmpty() ? solution : removeLastNCharacters(solution, version.length() + 1)) + classifier;
	}

	private void doMerge() {
		printProcessingModules();
		mergeOtherFiles();
		mergeModules();
	}

	private void mergeOtherFiles() {
		replaceFiles(sourceAnalysis.otherFiles, targetAnalysis.folder, targetAnalysis.otherFiles);
	}

	private void mergeModules() {
		deleteRemovedModules();
		copyNewModules();
		mergeKeptModules();
		writeHintIfInconsistencyDetected();
	}

	private void printProcessingModules() {
		outFileTransfer("\nProjecting modules", "", sourceAnalysis.folder.getAbsolutePath(), targetAnalysis.folder.getAbsolutePath());
	}

	private void deleteRemovedModules() {
		Collection<ModuleAnalysis> removedModules = subtractModules(targetAnalysis, sourceAnalysis);

		for (ModuleAnalysis removedModule : removedModules) {
			printModuleTransfer("Removing", FG_RED, removedModule);
			delete(removedModule.folder);
		}
	}

	private void copyNewModules() {
		Collection<ModuleAnalysis> newModules = subtractModules(sourceAnalysis, targetAnalysis);

		for (ModuleAnalysis newModule : newModules) {
			printModuleTransfer("Adding", FG_GREEN, newModule);
			copyToDir(newModule.folder, targetAnalysis.folder);
		}
	}

	private Collection<ModuleAnalysis> subtractModules(ModulesFolderAnalysis base, ModulesFolderAnalysis toSubtract) {
		HashMap<String, ModuleAnalysis> result = newLinkedMap(base.moduleAnalysisByName);
		result.keySet().removeAll(toSubtract.moduleAnalysisByName.keySet());
		return result.values();
	}

	private void mergeKeptModules() {
		for (ModuleAnalysis sourceModule : sourceAnalysis.moduleAnalysisByName.values()) {
			ModuleAnalysis targetModule = targetAnalysis.moduleAnalysisByName.get(sourceModule.name);
			if (targetModule != null)
				mergeModule(sourceModule, targetModule);
		}
	}

	private void mergeModule(ModuleAnalysis sourceModule, ModuleAnalysis targetModule) {
		if (targetModule.classpathAndSolutionAreInconsistent())
			replaceInconsistentModule(sourceModule, targetModule);
		else
			mergeWithConsistentModule(sourceModule, targetModule);
	}

	private void replaceInconsistentModule(ModuleAnalysis sourceModule, ModuleAnalysis targetModule) {
		printModuleTransfer("Purging", FG_CYAN, sourceModule);

		delete(targetModule.folder);
		copyToDir(sourceModule.folder, targetAnalysis.folder);

		moduleInconsistencyDetected = true;
	}

	private void mergeWithConsistentModule(ModuleAnalysis sourceModule, ModuleAnalysis targetModule) {
		printModuleTransfer("Merging", FG_YELLOW, sourceModule);

		replaceFiles(sourceModule.otherFiles, targetModule.folder, targetModule.otherFiles);

		mergeLibFolder(sourceModule, targetModule);
	}

	private static void printModuleTransfer(String action, int style, ModuleAnalysis module) {
		println(sequence( //
				text("    " + action + " "), // Adding / Removing / Merging / Purging
				styled(style, text(module.name))));
	}

	private void mergeLibFolder(ModuleAnalysis sourceModule, ModuleAnalysis targetModule) {
		List<String> mergedClasspathEntries = mergeClasspathEntries(sourceModule, targetModule);

		FileTools.copyFile(sourceModule.solutions, targetModule.solutions);

		FileTools.write(targetModule.classpath).lines(mergedClasspathEntries);
	}

	private List<String> mergeClasspathEntries(ModuleAnalysis sourceModule, ModuleAnalysis targetModule) {
		Map<String, String> sMap = sourceModule.solutionToClasspathEntry;
		Map<String, String> tMap = targetModule.solutionToClasspathEntry;

		List<String> result = newList();

		for (Entry<String, String> entry : sMap.entrySet()) {
			String sSolution = entry.getKey();
			String sCpEntry = entry.getValue();
			String tCpEntry = tMap.get(sSolution);

			if (isProjectEntry(tCpEntry))
				result.add(tCpEntry);
			else
				result.add(sCpEntry);
		}

		return result;
	}

	private boolean isProjectEntry(String cpEntry) {
		return cpEntry != null && !cpEntry.endsWith(".jar");
	}

	private void writeHintIfInconsistencyDetected() {
		if (moduleInconsistencyDetected)
			println(styled(FG_CYAN, text("\n    WARNING: You might need to RE-SYNC the DEBUG PROJECT in your IDE - "
					+ "currently it doesn't reference other projects, only jars in your local repo.")));
	}

	class ModulesFolderAnalysis {
		File folder;
		Map<String, ModuleAnalysis> moduleAnalysisByName = newLinkedMap(); // this has to be merged
		List<File> otherFiles = newList(); // these are deleted in target and then copied from source
	}

	class ModuleAnalysis {
		String name;
		File folder;
		File solutions;
		File classpath;
		Map<String, String> solutionToClasspathEntry;
		List<File> otherFiles; // these are deleted in target and then copied from source

		boolean classpathAndSolutionAreInconsistent() {
			return solutionToClasspathEntry == null;
		}
	}

}
