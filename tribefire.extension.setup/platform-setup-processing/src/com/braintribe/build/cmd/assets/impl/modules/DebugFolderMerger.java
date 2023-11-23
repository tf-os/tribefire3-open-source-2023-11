// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.setup.tools.TfSetupOutputs.fileName;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class DebugFolderMerger {

	private static final String STORED_FILES_FILE_NAME = ".written-by-jinni";

	/**
	 * Merges the content of the source debug folder into the target one. Both given dirs must exist.
	 */
	public static void merge(File sourceDebugDir, File targetDebugDir) {
		DebugFolderMerger instance = new DebugFolderMerger(sourceDebugDir, targetDebugDir);
		instance.merge();
	}

	private final List<String> previouslyWrittenFiles = newList();
	private final List<String> currentlyStoredFiles = newList();

	private final File sourceDebugDir;
	private final File targetDebugDir;
	private final Path targetPath;
	private final File autoGeneratedFile;

	private DebugFolderMerger(File sourceDebugDir, File targetDebugDir) {
		this.sourceDebugDir = sourceDebugDir;
		this.targetDebugDir = targetDebugDir;
		this.targetPath = targetDebugDir.toPath();

		this.autoGeneratedFile = new File(targetDebugDir, STORED_FILES_FILE_NAME);
	}

	private void merge() {
		printMerging();

		readPreviouslyWrittenFiles();
		copyFiles();
		deleteOldFiles();
		writeWrittenFiles();
	}

	private void printMerging() {
		println(sequence( //
				text("\nMerging debug folders: "), //
				fileName(sourceDebugDir.getAbsolutePath()), //
				text(" TO "), //
				fileName(targetDebugDir.getParentFile().getAbsolutePath()) //
		));
	}

	private void readPreviouslyWrittenFiles() {
		if (autoGeneratedFile.exists())
			previouslyWrittenFiles.addAll(FileTools.read(autoGeneratedFile).asLines());
	}

	private void copyFiles() {
		BiConsumer<File, File> copiedOrSkippedRecorder = (s, t) -> onFileCopiedOrSkipped(t);

		FileTools.copy(sourceDebugDir) //
				.as(targetDebugDir) //
				.filterFiles(newCheckerForCoreTomcatProjectFiles()) //
				.onFileCopied(copiedOrSkippedRecorder) //
				.onFileSkipped(copiedOrSkippedRecorder) //
				.please();
	}

	// We only skip files that were written before, so we consider them as written.
	private void onFileCopiedOrSkipped(File targetFile) {
		String relativePath = targetPath.relativize(targetFile.toPath()).toString();
		currentlyStoredFiles.add(relativePath);
	}

	private static final Set<String> coreTomcatProjectFiles = asSet( //
			// we do not want to overwrite the eclipse .project file, user might have changed it or
			".project", ".classpath");

	private BiPredicate<File, File> newCheckerForCoreTomcatProjectFiles() {
		return (s, t) -> coreTomcatProjectFiles.contains(s.getName()) && t.exists();
	}

	private void deleteOldFiles() {
		previouslyWrittenFiles.removeAll(currentlyStoredFiles);
		for (String relativePath : previouslyWrittenFiles)
			deleteOldFile(relativePath);
	}

	private void deleteOldFile(String relativePath) {
		File file = new File(targetDebugDir, relativePath);
		if (Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS))
			/* If a file is to be deleted here, it means it was directly in debug folder before. If, however, it is linked now (i.e. some of it's
			 * parents is a link, the actual file now lies elsewhere), we do not want to delete it. This can happen when we do a setup without
			 * debugJs, thus all the js libraries (with .js + .d.ts files) are copied into the debug project's context folder. When we then setup
			 * again, with debugJs, the js library folders change to links (to a common library in user home or even to a project with sources). In
			 * such case, if we were to delete "file", we would delete the actual linked content, i.e. the common library or source. */
			if (!isLinked(file))
				file.delete();
	}

	private boolean isLinked(File file) {
		Path path = file.toPath();
		while (true) {
			path = path.getParent();
			if (path == null || targetPath.equals(path))
				return false;
			if (Files.isSymbolicLink(path))
				return true;
		}
	}

	private void writeWrittenFiles() {
		FileTools.write(autoGeneratedFile).lines(currentlyStoredFiles);
	}

	public static void main(String[] args) {
		File s = new File("C:\\Peter\\Work\\TF\\package-a\\tribefire-master\\debug");
		File t = new File("C:\\Peter\\Work\\TF\\tf-a\\debug");
		merge(s, t);
	}

}
