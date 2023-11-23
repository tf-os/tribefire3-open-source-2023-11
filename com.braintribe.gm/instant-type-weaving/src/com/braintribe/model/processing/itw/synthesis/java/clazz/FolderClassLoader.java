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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.asm.ClassReader;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmExistingSourceClass;
import com.braintribe.model.processing.itw.asm.AsmUtils;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.utils.FileTools;

/**
 * 
 */
public class FolderClassLoader implements InitializationAware {

	private File classFolder;

	private final Map<String, File> existingClasses = new HashMap<String, File>();
	private final AsmClassPool classPool = new AsmClassPool(true);
	private final List<AsmExistingSourceClass> newSourceClasses = new ArrayList<AsmExistingSourceClass>();

	public void setClassFolder(File classFolder) {
		this.classFolder = classFolder;
	}

	@Override
	public void postConstruct() {
		try {
			ensureClasses();
		} catch (JavaTypeSynthesisException e) {
			throw new RuntimeException(e);
		}
	}

	public void ensureClasses() throws JavaTypeSynthesisException {
		existingClasses.clear();

		if (!classFolder.isDirectory()) {
			return;
		}

		indexFolderClasses();
		enusureFolderClasses();
		loadNewSourceClassses();
	}

	private void indexFolderClasses() {
		for (File f: classFolder.listFiles()) {
			if (!f.isDirectory()) {
				existingClasses.put(f.getName(), f);
			}
		}
	}

	private void enusureFolderClasses() throws JavaTypeSynthesisException {
		for (String signature: existingClasses.keySet()) {
			ensureClass(signature);
		}
	}

	private AsmClass ensureClass(String signature) throws JavaTypeSynthesisException {
		AsmClass asmClass = classPool.getIfPresent(signature);

		if (asmClass != null) {
			return asmClass;
		}

		File file = existingClasses.get(signature);

		if (file == null) {
			throw new JavaTypeSynthesisException("No source found for class:" + signature);
		}

		return ensureClassFrom(file);
	}

	private AsmClass ensureClassFrom(File file) throws JavaTypeSynthesisException {
		byte[] bytes = FileTools.readBytesFromFile(file);

		ClassReader cr = new ClassReader(bytes);

		String name = file.getName();
		AsmClass superClass = ensureClass(AsmUtils.fromInternalName(cr.getSuperName()));
		List<AsmClass> interfaces = ensureClasses(cr.getInterfaces());

		AsmExistingSourceClass result = new AsmExistingSourceClass(name, superClass, interfaces, bytes, classPool);
		registerSourceClass(result);

		return result;
	}

	private List<AsmClass> ensureClasses(String[] interfaces) throws JavaTypeSynthesisException {
		List<AsmClass> result = new ArrayList<AsmClass>();

		for (String iface: interfaces) {
			iface = AsmUtils.fromInternalName(iface);
			result.add(ensureClass(iface));
		}

		return result;
	}

	private void registerSourceClass(AsmExistingSourceClass result) {
		classPool.registerPreliminaryClass(result);
		classPool.registerFinishedNewClass(result);
		newSourceClasses.add(result);
	}

	private void loadNewSourceClassses() throws JavaTypeSynthesisException {
		for (AsmExistingSourceClass asmClass: newSourceClasses) {
			classPool.getJvmClass(asmClass);
		}
	}

}
