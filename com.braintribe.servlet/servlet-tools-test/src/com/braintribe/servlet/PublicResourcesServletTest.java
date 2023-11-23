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
package com.braintribe.servlet;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class PublicResourcesServletTest {
	ResourceServlet servlet;
	Path rootPath = Paths.get("a","b","c");
	
	@Before
	public void init() {
		servlet  = new ResourceServlet();
		servlet.setPublicResourcesDirectory(rootPath);
	}

	@Test
	public void testResourcePathWithSlashes() {
		testResourcePath("/d", "a/b/c/d");
		testResourcePath("d", "a/b/c/d");
		testResourcePath("d/", "a/b/c/d/");
		testResourcePath("d//", "a/b/c/d/");
		
		testResourcePath("//d", "a/b/c/d");
		testResourcePath("///d", "a/b/c/d");
		testResourcePath("/d/", "a/b/c/d/");
		testResourcePath("//d//", "a/b/c/d/");
	}
	
	@Test
	public void testComplexPath() {
		testResourcePath("d/../e", "a/b/c/e");
		testResourcePath("d/../e/f", "a/b/c/e/f");
		testResourcePath("d/./e/f", "a/b/c/d/e/f");
		testResourcePath(".", "a/b/c");
		testResourcePath("a/..", "a/b/c");
	}
	
	@Test
	public void testNoPath() {
		assertExceptionThrownFor("");
		assertExceptionThrownFor("/");
		assertExceptionThrownFor("//");
		assertExceptionThrownFor("///////////////////");
	}
	
	@Test
	public void testSecurePath() {
		assertExceptionThrownForVariantsOf("..");
		assertExceptionThrownForVariantsOf("../hack");
		assertExceptionThrownForVariantsOf("../a/../");
		assertExceptionThrownForVariantsOf("../a/../");
		assertExceptionThrownForVariantsOf("../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../");
		
		assertExceptionThrownForVariantsOf("a/../a/../a/../../hack/");
		assertExceptionThrownForVariantsOf("d/e/f/../../../../../hack");
		
		// The following could be legal because even if the path leaves the public resource root folder it returns back to it
		// However it makes no sense to support this because that folder name is neither known by the typical user nor necessarily fixed 
		assertExceptionThrownForVariantsOf("../" + rootPath.getFileName());
		assertExceptionThrownForVariantsOf("../" + rootPath.getFileName() + "/d");
	}
	
	private void assertExceptionThrownForVariantsOf(String subPath) {
		assertExceptionThrownFor(subPath);
		
		assertExceptionThrownFor("./" + subPath);
		assertExceptionThrownFor("/" + subPath);
		assertExceptionThrownFor("//" + subPath);
		assertExceptionThrownFor(subPath + "/");
		assertExceptionThrownFor(subPath + "//");
		assertExceptionThrownFor("/" + subPath + "/");
	}
	private void assertExceptionThrownFor(String subPath) {
		Assertions.assertThatThrownBy(() -> testResourcePath(subPath, null)).isExactlyInstanceOf(IllegalArgumentException.class);
	}
	
	private void testResourcePath(String subPath, String expectedResult) {
		File resourceFile = servlet.getResourceFile(subPath);
		assertThat(resourceFile).hasSamePathAs(Paths.get(expectedResult).toFile());
	}
}
