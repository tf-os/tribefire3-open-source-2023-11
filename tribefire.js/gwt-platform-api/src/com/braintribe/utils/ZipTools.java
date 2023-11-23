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
package com.braintribe.utils;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.braintribe.exception.Exceptions;

/**
 * @author peter.gazdik
 */
public class ZipTools {

	public static void unzip(File zipFile, File targetDir) {
		unzip(zipFile, targetDir, null);
	}

	public static void unzip(File zipFile, File defaultTargetDir, Function<String, File> targetMapper) {
		requireNonNull(zipFile, "zipFile must not be null.");

		try (FileInputStream fis = new FileInputStream(zipFile)) {
			unzip(fis, defaultTargetDir, targetMapper, zipFile.getAbsolutePath());

		} catch(Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip: " + zipFile.getAbsolutePath());
		}
	}

	public static void unzip(InputStream in, File targetDir, Function<String, File> mapper) {
		unzip(in, targetDir, mapper, null);
	}
	
	public static void unzip(InputStream in, File defaultTargetDir, Function<String, File> targetMapper, String context) {
		FileTools.ensureFolderExists(requireNonNull(defaultTargetDir, "The targetDir must not be null."));

		try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in))) {
			ZipEntry zipEntry = null;

			while ((zipEntry = zin.getNextEntry()) != null) {
				String slashedPathName = zipEntry.getName();

				File targetFile = new File(defaultTargetDir, slashedPathName);

				if (!FileTools.isInSubDirectory(defaultTargetDir, targetFile))
					throw new RuntimeException("The target file " + targetFile.getAbsolutePath() + " is not within the target folder "
							+ defaultTargetDir.getAbsolutePath() + " (entry name: " + slashedPathName + "). This is not allowed.");

				if (targetMapper != null)
					targetFile = targetMapper.apply(slashedPathName);

				if (targetFile == null)
					targetFile = new File(defaultTargetDir, slashedPathName);

				
				if (zipEntry.isDirectory()) {
					// create directory because it maybe empty and it would be an information loss otherwise
					targetFile.mkdirs();
				} else {
					targetFile.getParentFile().mkdirs();

					try (OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
						IOTools.transferBytes(zin, out, IOTools.BUFFER_SUPPLIER_8K);
					}
				}

				zin.closeEntry();
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip "+context);
		}
	}
}
