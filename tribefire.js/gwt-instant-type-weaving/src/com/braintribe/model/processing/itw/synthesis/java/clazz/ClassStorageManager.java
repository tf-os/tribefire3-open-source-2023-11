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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ClassDataStorage;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.tools.ItwTools;

/**
 * 
 */
public class ClassStorageManager {

	private final ClassDataStorage classDataStorage;
	private final Map<String, Class<?>> loadedClasses;

	private static final Logger log = Logger.getLogger(ClassStorageManager.class);

	public ClassStorageManager(ClassDataStorage classDataStorage) {
		this.classDataStorage = classDataStorage;
		this.loadedClasses = new HashMap<String, Class<?>>();

		indexFiles();
	}

	private void indexFiles() {
		for (String className: listStoredClassNames())
			registerClass(className);
	}

	private Set<String> listStoredClassNames() {
		try {
			return classDataStorage.getQualifiedNamesOfStoredClasses();

		} catch (Exception e) {
			throw new RuntimeException("Unable to retrieve names of stored classes.", e);
		}
	}

	private void registerClass(String className) {
		Class<?> clazz = getClassIfExists(className);
		if (clazz != null)
			loadedClasses.put(className, clazz);
	}

	private Class<?> getClassIfExists(String className) {
		return ItwTools.findClass(className);
	}

	public void onClassCreated(AsmNewClass newClass) {
		if (classDataStorage instanceof FileSystemClassDataStorage) {
			((FileSystemClassDataStorage) classDataStorage).storeClass(newClass);
			return;
		}

		try {
			classDataStorage.storeClass(newClass.getName(), bytecodeInputStream(newClass), getDependencyNames(newClass));

		} catch (Exception e) {
			log.error("Error while trying to store bytes for: " + newClass.getName(), e);
		}
	}

	private InputStream bytecodeInputStream(AsmNewClass newClass) {
		return new ByteArrayInputStream(newClass.getBytes());
	}

	private Set<String> getDependencyNames(AsmNewClass newClass) {
		Set<String> result = asSet(newClass.getSuperClass().getName());

		for (AsmClass iface: newClass.getInterfaces())
			result.add(iface.getName());

		return result;
	}

	public boolean containsLoadedClass(String className) {
		return loadedClasses.containsKey(className);
	}

	public <T> Class<T> getLoadedClass(String className) {
		return (Class<T>) loadedClasses.get(className);
	}

}
