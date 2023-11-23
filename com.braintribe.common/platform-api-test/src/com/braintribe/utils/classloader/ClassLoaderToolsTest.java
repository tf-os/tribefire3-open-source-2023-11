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
package com.braintribe.utils.classloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

@Ignore
public class ClassLoaderToolsTest {

	@Test
	public void testGetAllClassesSizeCompare() throws Exception {

		ClassLoader classLoader = ClassLoaderToolsTest.class.getClassLoader();
		Set<String> classes = ClassLoaderTools.getAllClasses(classLoader);

		ClassPath cp = ClassPath.from(classLoader);
		Set<ClassInfo> classesFromGuava = cp.getAllClasses();

		assertThat(classes.size()).isEqualTo(classesFromGuava.size());
	}

	@Test
	public void testGetAllClassesDeepCompare() throws Exception {

		ClassLoader classLoader = ClassLoaderToolsTest.class.getClassLoader();
		Set<String> classes = ClassLoaderTools.getAllClasses(classLoader);
		TreeSet<String> sortedClasses = new TreeSet<>(classes);

		ClassPath cp = ClassPath.from(classLoader);
		Set<ClassInfo> classesFromGuava = cp.getAllClasses();
		TreeSet<String> sortedGuavaClasses = new TreeSet<>();
		for (ClassInfo ci : classesFromGuava) {
			sortedGuavaClasses.add(ci.getName());
		}

		Iterator<String> classIterator = sortedClasses.iterator();
		Iterator<String> guavaIterator = sortedGuavaClasses.iterator();

		while (classIterator.hasNext()) {
			String className = classIterator.next();
			String guavaClassName = guavaIterator.next();
			Assert.assertEquals(guavaClassName, className);
		}
		Assert.assertFalse(guavaIterator.hasNext());
	}
}
