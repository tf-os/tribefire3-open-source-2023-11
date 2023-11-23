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
import java.nio.file.Path;

/**
 * Second level of a three-step "wizard" to enter your parameters for copying a file or a folder. The individual steps are:
 * <ol>
 * <li>{@link FileCopyingImpl#FileCopyingImpl(File)} source file selection - to pick a source file or directory, i.e. what is being copied</li>
 * <li>{@link FileCopyTargetSelector} - to pick a target file or directory, i.e. the new file folder that will have the same content as the source.
 * Alternatively, when picking {@link FileCopyTargetSelector#toDir(File)} a target will be created as a child of that file and it will have the same
 * name as the source.</li>
 * <li>{@link FileCopying } - to pick any additional parameters for copying as well as providing a method to {@link FileCopying#please do the actual
 * copying}.</li>
 * </ol>
 *
 * @author peter.gazdik
 */
public interface FileCopyTargetSelector {

	/**
	 * Specifies that the content of file or directory being copied will be put into the file or directory specified here. This means they are either
	 * both files or both directories. If you want to copy your file or directory into another directory use {@link #toDir(File)} instead.
	 * <p>
	 * If we are copying files, the source file will overwrite the target file.
	 * <p>
	 * If we are copying directories, files inside the source dir will be copied into the target dir, overwriting only files that already existed and
	 * not deleting anything else.
	 */
	FileCopying as(File fileOrDir);

	/** Equivalent to {@link #as(File) as}(fileOrDirPath.toFile()). */
	default FileCopying as(Path fileOrDirPath) {
		return as(fileOrDirPath.toFile());
	}

	/**
	 * Sets a directory to which the source file or directory will be copied. This means the copy we create will be a child of this directory, and
	 * will be called the same as it's source.
	 */
	FileCopying toDir(File dir);

	/** Equivalent to {@link #toDir(File) toDir}(dirPath.toFile()). */
	default FileCopying toDir(Path dirPath) {
		return toDir(dirPath.toFile());
	}

}
