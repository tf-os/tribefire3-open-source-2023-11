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
package com.braintribe.model.processing.test.jar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 
 */
public class SourceJarReader {

	public static Map<String, String> readSources(ZipInputStream zis) throws IOException {
		Map<String, String> result = new HashMap<String, String>();

		ZipEntry ze;
		while ((ze = nextEntry(zis)) != null) {
			if (isJavaFile(ze)) {
				String name = ze.getName();
				String className = name.substring(0, name.length() - 5).replace("/", ".");

				StringBuilder sb = new StringBuilder();
				BufferedReader b = new BufferedReader(new InputStreamReader(zis));

				String s;
				while ((s = b.readLine()) != null) {
					sb.append(s);
					sb.append("\n");
				}

				result.put(className, sb.toString());
			}
		}

		return result;
	}

	private static boolean isJavaFile(ZipEntry ze) {
		return !ze.isDirectory() && ze.getName().endsWith(".java");
	}

	private static ZipEntry nextEntry(ZipInputStream zis) {
		try {
			return zis.getNextEntry();

		} catch (IOException e) {
			throw new RuntimeException("Some problem: " + e.getMessage(), e);
		}
	}
}
