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
package com.braintribe.model.processing.itw.synthesis.java.clazz;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.access.ClassDataStorage;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisRuntimeException;
import com.braintribe.utils.FileTools;

/**
 * 
 */
public class FileSystemClassDataStorage implements ClassDataStorage {

	private final File folder;
	private final Set<String> existingClasses;
	private final Set<String> readOnly_ExistingClasses;

	public FileSystemClassDataStorage(File folder) {
		this.folder = folder;
		this.existingClasses = new HashSet<String>();
		this.readOnly_ExistingClasses = Collections.unmodifiableSet(existingClasses);

		ensureFolder(folder);
		indexFiles();
	}

	private static File ensureFolder(File folder) {
		try {
			return FileTools.ensureDirectoryExists(folder);

		} catch (Exception e) {
			throw new JavaTypeSynthesisRuntimeException("Failed to configure class output folder as: " + folder.getName(), e);
		}
	}

	private void indexFiles() {
		if (!folder.isDirectory()) {
			return;
		}

		for (File f: folder.listFiles()) {
			if (!f.isDirectory()) {
				registerFile(f);
			}
		}
	}

	private void registerFile(File f) {
		String className = f.getName();
		existingClasses.add(className);
	}

	@Override
	public void storeClass(String qualifiedName, InputStream inputStream, Set<String> dependencies) throws Exception {
		throw new UnsupportedOperationException("Method 'FileSystemClassDataStorage.storeClass' is not implemented yet!");
	}

	public void storeClass(AsmNewClass newClass) {
		FileTools.writeBytesToFile(new File(folder, newClass.getName()), newClass.getBytes());
	}

	@Override
	public Set<String> getQualifiedNamesOfStoredClasses() throws Exception {
		return readOnly_ExistingClasses;
	}

}
