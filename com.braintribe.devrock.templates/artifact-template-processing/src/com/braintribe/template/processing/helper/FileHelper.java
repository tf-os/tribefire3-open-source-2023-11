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
package com.braintribe.template.processing.helper;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ZipTools;

public final class FileHelper {

	private FileHelper() {
		// noop
	}

	public static Path createTempPath(String dirPrefix) {
		return createTempDir(dirPrefix).toPath();
	}

	/**
	 * Creates a File object (i.e. nothing is created in file system) denoting a temporary file with given prefix inside "${temp
	 * folder}/DevRock/artifact-templates/" folder.
	 */
	public static File createTempDir(String dirPrefix) {
		dirPrefix = "devrock" + File.separator + "artifact-templates" + File.separator + dirPrefix;
		return FileTools.createNewTempFile(dirPrefix, null);
	}

	public static File createTempFile(String prefix, String extension) {
		prefix = "devrock" + File.separator + "artifact-templates" + File.separator + prefix;
		return FileTools.createNewTempFile(prefix, extension);
	}

	public static Path unzipToTempDir(Resource zip, String dirPrefix) {
		File target = createTempDir(dirPrefix);

		try (InputStream is = zip.openStream()) {
			ZipTools.unzip(is, target);
			return target.toPath();

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while unzipping " + zipLocation(zip) + " into folder: " + target.getAbsolutePath());
		}
	}

	private static String zipLocation(Resource r) {
		if (r instanceof FileResource)
			return ((FileResource) r).getPath();
		else
			return "zipped resource";
	}

	public static void ensureDirExists(Path dir) {
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Failed to create directory '" + dir.toString() + "'");
		}
	}

	public static void copyFile(Path source, Path target) {
		try {
			FileTools.copyFile(source.toFile(), target.toFile());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to copy file '" + source + "' to '" + target + "'");
		}
	}

	public static void copyDir(Path source, Path target) {
		try {
			FileTools.copyDirectory(source.toFile(), target.toFile(), true);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to copy directory '" + source + "' to '" + target + "'");
		}
	}

	public static void deleteFile(Path file) {
		FileTools.deleteFile(file.toFile());
	}

	public static void deleteDir(Path dir) {
		FileTools.deleteDirectoryRecursivelyUnchecked(dir.toFile());
	}

	public static List<Path> collectRelativePaths(Path dir) {
		List<File> files = FileTools.listFilesRecursively(dir.toFile());
		List<Path> filesRelativePaths = new ArrayList<>();
		for (File file : files) {
			String fileRelativePath = dir.relativize(file.toPath()).toString();
			if (StringTools.isEmpty(fileRelativePath)) {
				continue;
			}
			filesRelativePaths.add(Paths.get(fileRelativePath));
		}
		return filesRelativePaths;
	}

	public static List<Path> collectOverwritenRelativePaths(Path overwriting, Path overwritten) {
		List<Path> overwritingPaths = collectRelativePaths(overwriting);
		List<Path> overwrittenPaths = newList();
		for (Path op : overwritingPaths) {
			Path overwrittenFilePath = overwritten.resolve(op);
			if (Files.exists(overwrittenFilePath)) {
				overwrittenPaths.add(op);
			}
		}
		return overwrittenPaths;
	}

}
