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
package com.braintribe.utils.file.copy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class FileCopyingImpl implements FileCopyTargetSelector, FileCopying {

	private final File source;
	private final Path sourceBasePath;
	private File target;

	/* package */ BiPredicate<File, File> fileOrDirFilter = COPY_ALL;
	/* package */ BiPredicate<File, File> fileFilter = COPY_ALL;
	/* package */ BiPredicate<File, File> dirFilter = COPY_ALL;

	/* package */ BiConsumer<File, File> sourceAndTargetFileSkippedCallback = null;
	/* package */ BiConsumer<File, File> sourceAndTargetFileCopiedCallback = null;

	public FileCopyingImpl(File file) {
		source = Objects.requireNonNull(file, "Copying source cannot be null.");
		sourceBasePath = source.toPath();

		if (!source.exists())
			throw new IllegalArgumentException("Cannot copy non-existet file or directory: " + file.getAbsolutePath());
	}

	@Override
	public FileCopying as(File fileOrDir) {
		return target(fileOrDir);
	}

	@Override
	public FileCopying toDir(File dir) {
		if (dir.exists() && !dir.isDirectory())
			throw new IllegalArgumentException("Cannot copy to directory, file was given: " + dir.getAbsolutePath());

		return as(new File(dir, source.getName()));
	}

	private FileCopyingImpl target(File file) {
		target = Objects.requireNonNull(file, "Copying target cannot be null.");

		if (!file.exists())
			return this;

		boolean isDir = source.isDirectory();
		if (isDir == file.isDirectory())
			return this;

		if (isDir)
			throw new IllegalArgumentException("Cannot copy as a directory, file was given: " + file.getAbsolutePath());
		else
			throw new IllegalArgumentException("Cannot copy as a file, directory was given: " + file.getAbsolutePath());
	}

	@Override
	public FileCopying filter(BiPredicate<File, File> fileOrDirFilter) {
		this.fileOrDirFilter = Objects.requireNonNull(fileOrDirFilter, "Filter cannot be null.");
		return this;
	}

	@Override
	public FileCopying filterFiles(BiPredicate<File, File> fileFilter) {
		this.fileFilter = Objects.requireNonNull(fileFilter, "Filter cannot be null.");
		return this;
	}

	@Override
	public FileCopying filterDirs(BiPredicate<File, File> dirFilter) {
		this.dirFilter = Objects.requireNonNull(dirFilter, "Filter cannot be null.");
		return this;
	}

	@Override
	public FileCopying onFileSkipped(BiConsumer<File, File> sourceAndTargetFileConsumer) {
		this.sourceAndTargetFileSkippedCallback = sourceAndTargetFileConsumer;
		return this;
	}

	@Override
	public FileCopying onFileCopied(BiConsumer<File, File> sourceAndTargetFileConsumer) {
		this.sourceAndTargetFileCopiedCallback = sourceAndTargetFileConsumer;
		return this;
	}

	/**
	 * At this point our source is either a file or a directory, and target is a valid destination for the copy of the source (i.a. also a folder or
	 * also not a folder).
	 */
	@Override
	public void please() {
		FileTools.ensureFolderExists(target.getParentFile());

		try {
			copy(source.toPath(), target.toPath());

		} catch (IOException e) {
			String fileOrDir = source.isDirectory() ? "directory" : "file";
			throw Exceptions.unchecked(e,
					"Error while copying " + fileOrDir + " '" + source.getAbsolutePath() + "' to '" + target.getAbsolutePath() + "'");
		}
	}

	private void copy(Path sourcePath, Path targetPath) throws IOException {
		boolean sourceIsDirectory = Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS);

		if (skipFile(sourcePath, targetPath, sourceIsDirectory)) {
			if (!sourceIsDirectory)
				notifyFiles(sourceAndTargetFileSkippedCallback, sourcePath, targetPath);
			return;
		}

		if (Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS))
			deleteTargetUnlessSourceAndTargetAreDirs(sourceIsDirectory, targetPath);

		if (Files.isSymbolicLink(sourcePath)) {
			copySymbolicLink(sourcePath, targetPath);
			notifyFiles(sourceAndTargetFileCopiedCallback, sourcePath, targetPath);
			return;
		}

		if (!Files.exists(targetPath))
			Files.copy(sourcePath, targetPath);

		if (!sourceIsDirectory) {
			notifyFiles(sourceAndTargetFileCopiedCallback, sourcePath, targetPath);
			return;
		}

		try (Stream<Path> childStream = Files.list(sourcePath)) {
			for (Path sourceChildFile : (Iterable<Path>) (childStream::iterator)) {
				Path name = sourceChildFile.getFileName();
				Path targetChildFile = targetPath.resolve(name);
				copy(sourceChildFile, targetChildFile);
			}
		}
	}

	private void notifyFiles(BiConsumer<File, File> sourceAndTargetFileConsumer, Path sourcePath, Path targetPath) {
		if (sourceAndTargetFileConsumer != null)
			sourceAndTargetFileConsumer.accept(sourcePath.toFile(), targetPath.toFile());
	}

	private boolean skipFile(Path sourcePath, Path targetPath, boolean isDirectory) {
		File s = sourcePath.toFile();
		File t = targetPath.toFile();

		return fileOrDirFilter.test(s, t) || //
				isDirectory ? dirFilter.test(s, t) : fileFilter.test(s, t);
	}

	private void deleteTargetUnlessSourceAndTargetAreDirs(boolean sourceIsDirectory, Path targetPath) throws IOException {
		// if target is not a directory (i.e is regular file or a link), we delete it
		if (!Files.isDirectory(targetPath, LinkOption.NOFOLLOW_LINKS))
			Files.delete(targetPath);

		// if target is a directory, we delete it as long as source is not a directory (i.e. source is a file or a link)
		else if (!sourceIsDirectory)
			FileTools.deleteRecursivelySymbolLinkAware(targetPath.toFile());
	}

	private void copySymbolicLink(Path sourceLink, Path targetLink) throws IOException {
		Path targetFile = resolveTargetLink(sourceLink, targetLink);

		boolean needsDummyTargetFolder = Files.isDirectory(sourceLink) && !Files.exists(targetFile);

		if (!needsDummyTargetFolder) {
			Files.createSymbolicLink(targetLink, targetFile);
			return;
		}

		// targetFile must be relative - if it was absolute then sourceLink points to targetFile, so needsDummyTargetFolder is false
		Path dummyTargetDir = targetLink.getParent().resolve(targetFile);
		Files.createDirectory(dummyTargetDir);

		Files.createSymbolicLink(targetLink, targetFile);

		Files.delete(dummyTargetDir);
	}

	private Path resolveTargetLink(Path sourceLink, Path targetLink) throws IOException {
		Path symbolicLinkPath = Files.readSymbolicLink(sourceLink);

		if (symbolicLinkPath.isAbsolute()) {
			if (symbolicLinkPath.startsWith(sourceBasePath))
				return sourceLink.getParent().relativize(symbolicLinkPath);
			else
				return symbolicLinkPath;

		} else {
			Path symbolLinkTarget = sourceLink.getParent().resolve(symbolicLinkPath).normalize();
			if (symbolLinkTarget.startsWith(sourceBasePath))
				return symbolicLinkPath;
			else
				return targetLink.getParent().relativize(symbolLinkTarget);
		}
	}
}
