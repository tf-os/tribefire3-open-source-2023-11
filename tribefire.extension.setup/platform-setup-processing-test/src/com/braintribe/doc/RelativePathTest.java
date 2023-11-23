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
package com.braintribe.doc;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Test;

public class RelativePathTest {
	@Test
	public void testEmptyPath() throws URISyntaxException {
		UniversalPath relativePath = UniversalPath.empty();
		
		assertThat(relativePath.getName()).isEqualTo(null);
		assertThat(relativePath.toSlashPath()).isEqualTo("");
		assertThat(relativePath.toBackslashPath()).isEqualTo("");
		assertThat(relativePath.toFilePath()).isEqualTo("");
		assertThat(relativePath.toString()).isEqualTo("");
		assertThat(relativePath.toFile()).isEqualTo(new File(""));
		assertThat(relativePath.toPath()).isEqualTo(Paths.get(""));
		assertThat(relativePath.toUri()).isEqualTo(new URI(""));
		
		assertThat(relativePath.getNameCount()).isEqualTo(0);
		
		assertThat(relativePath.getParent()).isNull();
		assertThat(relativePath.isEmpty()).isTrue();
	}
	
	@Test
	public void testMonoPath() {
		UniversalPath relativePath = UniversalPath.empty().push("name");
		
		assertThat(relativePath.toSlashPath()).isEqualTo("name");
		assertThat(relativePath.toBackslashPath()).isEqualTo("name");
		assertThat(relativePath.toFilePath()).isEqualTo("name");
		assertThat(relativePath.toString()).isEqualTo("name");
		assertThat(relativePath.toFile()).isEqualTo(new File("name"));
		assertThat(relativePath.toPath()).isEqualTo(Paths.get("name"));
		
		assertThat(relativePath.getNameCount()).isEqualTo(1);
		
		assertThat(relativePath.isEmpty()).isFalse();
		assertThat(relativePath.getParent().getName()).isNull();
	}
	
	@Test
	public void testLongPath() {
		UniversalPath relativePath = UniversalPath.empty()
				.push("name1")
				.push("name2")
				.push("name3")
				.push("name4")
				.push("name5")
				.push("name6")
				.push("name7")
				.push("name8")
				.push("name9")
				.push("name10"); // 10 times
		
		final String tenTimesSlashedName = "name1/name2/name3/name4/name5/name6/name7/name8/name9/name10";
		
		assertThat(relativePath.toSlashPath()).isEqualTo(tenTimesSlashedName);
		assertThat(relativePath.toBackslashPath()).isEqualTo("name1\\name2\\name3\\name4\\name5\\name6\\name7\\name8\\name9\\name10");
		assertThat(relativePath.toFilePath()).isEqualTo(relativePath.toPath().toString());
		assertThat(relativePath.toString()).isEqualTo(tenTimesSlashedName);
		assertThat(relativePath.toFile()).isEqualTo(new File(tenTimesSlashedName));
		assertThat(relativePath.toPath()).isEqualTo(Paths.get(tenTimesSlashedName));
		
		assertThat(relativePath.getNameCount()).isEqualTo(10);

		assertThat(relativePath.getParent().getName()).isEqualTo("name9");
		assertThat(relativePath.isEmpty()).isFalse();

	}
	
	@Test
	public void testPathCreation() {
		String slashyPath = "a/b/c";
		UniversalPath pathFromFile = UniversalPath.from(new File(slashyPath));
		UniversalPath pathFromPath = UniversalPath.from(Paths.get(slashyPath));
		UniversalPath pathFromSlashyString = UniversalPath.empty().pushSlashPath(slashyPath);
		
		assertThat(pathFromFile.toSlashPath()).isEqualTo(slashyPath);
		assertThat(pathFromPath.toSlashPath()).isEqualTo(slashyPath);
		assertThat(pathFromSlashyString.toSlashPath()).isEqualTo(slashyPath);
		
		assertThat(pathFromFile.getName()).isEqualTo("c");
		assertThat(pathFromPath.getName()).isEqualTo("c");
		assertThat(pathFromSlashyString.getName()).isEqualTo("c");
		
		assertThat(pathFromFile.getNameCount()).isEqualTo(3);
		assertThat(pathFromPath.getNameCount()).isEqualTo(3);
		assertThat(pathFromSlashyString.getNameCount()).isEqualTo(3);
		
		// Note: If you use path separators in a simple push they will be used as part of the name
		UniversalPath pathFromString = UniversalPath.empty().push(slashyPath);
		
		assertThat(pathFromString.toSlashPath()).isEqualTo(slashyPath);
		assertThat(pathFromString.toBackslashPath()).isEqualTo(slashyPath); // !
		assertThat(pathFromString.getName()).isEqualTo(slashyPath);
		assertThat(pathFromString.getNameCount()).isEqualTo(1);
		assertThat(pathFromString.isEmpty()).isFalse();

		
	}
	
	@Test
	public void testIterableAndStream() {
		String slashyPath = "a/b/c";
		UniversalPath relativePath = UniversalPath.empty().pushSlashPath(slashyPath);
		
		assertThat(relativePath.stream().collect(Collectors.joining("/"))).isEqualTo(slashyPath);
		
		StringBuilder pathToBeGenerated = new StringBuilder();
		// forEach comes from the Iterable interface
		relativePath.forEach(n -> pathToBeGenerated.append("/" + n));
		String generatedPath = pathToBeGenerated.toString().substring(1);
		assertThat(generatedPath).isEqualTo(slashyPath);
	}
	
	@Test
	public void testPop() {
		String slashyPath = "a/b/c";
		UniversalPath relativePath = UniversalPath.empty().pushSlashPath(slashyPath);
		
		UniversalPath poppedPath = relativePath.pop();
		assertThat(poppedPath.getNameCount()).isEqualTo(2);
		assertThat(poppedPath.getName()).isEqualTo("b");
		assertThat(poppedPath.getParent().getName()).isEqualTo("a");
		assertThat(poppedPath.pop().pop().isEmpty()).isTrue();
		assertThat(poppedPath.isEmpty()).isFalse();
	}
	
	@Test
	public void testSubPath() {
		String slashyPath = "a/b/c";
		UniversalPath relativePath = UniversalPath.empty().pushSlashPath(slashyPath);
		
		assertThat(relativePath.subpath(0, 3).toSlashPath()).isEqualTo(slashyPath);
		assertThat(relativePath.subpath(0, 2).toSlashPath()).isEqualTo("a/b");
		assertThat(relativePath.subpath(0, 1).toSlashPath()).isEqualTo("a");
		
		// assertThat(relativePath.subpath(0, 0).isEmpty()).isTrue();
		
		assertThat(relativePath.subpath(1, 2).toSlashPath()).isEqualTo("b");
		assertThat(relativePath.subpath(1, 3).toSlashPath()).isEqualTo("b/c");
		assertThat(relativePath.subpath(2, 3).toSlashPath()).isEqualTo("c");
	}
	
	@Test
	public void testAbsolutePaths() {
		String unixPathString = "/a/b/c";
		UniversalPath absolutePath = UniversalPath.empty().pushSlashPath(unixPathString);
		
		assertThat(absolutePath.toSlashPath()).isEqualTo(unixPathString);
		assertThat(absolutePath.getNameCount()).isEqualTo(4);
		
		absolutePath = absolutePath.pushSlashPath(unixPathString);
		
		assertThat(absolutePath.toSlashPath()).isEqualTo("/a/b/c//a/b/c");
		assertThat(absolutePath.getNameCount()).isEqualTo(8);
		
		String windowsPathString = "C:\\a\\b\\c";
		
		absolutePath = UniversalPath.empty().pushBackSlashPath(windowsPathString);
		
		assertThat(absolutePath.toBackslashPath()).isEqualTo(windowsPathString);
		assertThat(absolutePath.getNameCount()).isEqualTo(4);
		
		absolutePath = absolutePath.pushBackSlashPath(windowsPathString);
		
		assertThat(absolutePath.toBackslashPath()).isEqualTo("C:\\a\\b\\c\\C:\\a\\b\\c");
		assertThat(absolutePath.getNameCount()).isEqualTo(8);

	}
}
