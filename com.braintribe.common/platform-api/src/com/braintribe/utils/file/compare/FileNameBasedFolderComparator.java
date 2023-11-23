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
package com.braintribe.utils.file.compare;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;

import java.io.File;
import java.util.Map;

/**
 * @author peter.gazdik
 */
public class FileNameBasedFolderComparator {

	/** @return {@link FolderComparison} of given two folders based on the names of files in those folders only. */
	public static FolderComparison compare(File leftDir, File rightDir) {
		FileNameBasedFolderComparator instance = new FileNameBasedFolderComparator(leftDir, rightDir);
		return instance.compare();
	}

	private final Map<String, File> leftFiles;
	private final Map<String, File> rightFiles;

	private final FolderComparison result = new FolderComparison();

	public FileNameBasedFolderComparator(File leftDir, File rightDir) {
		result.leftDir = leftDir;
		result.rightDir = rightDir;

		this.leftFiles = index(leftDir);
		this.rightFiles = index(rightDir);
	}

	private FolderComparison compare() {
		for (File leftFile : leftFiles.values()) {
			File rightFile = rightFiles.remove(leftFile.getName());

			if (rightMatchesLeft(leftFile, rightFile)) {
				result.leftToRight.put(leftFile, rightFile);
			} else {
				result.onlyLeft.add(leftFile);
			}
		}

		result.onlyRight.addAll(rightFiles.values());

		return result;
	}

	private boolean rightMatchesLeft(File leftFile, File rightFile) {
		return rightFile != null && leftFile.isDirectory() == rightFile.isDirectory();
	}

	private Map<String, File> index(File dir) {
		Map<String, File> files = newLinkedMap();

		for (File file : dir.listFiles()) {
			files.put(file.getName(), file);
		}

		return files;
	}

}
