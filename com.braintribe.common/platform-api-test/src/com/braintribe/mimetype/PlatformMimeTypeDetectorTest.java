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
package com.braintribe.mimetype;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.StringTools;

public class PlatformMimeTypeDetectorTest {

	protected static final String defaultType = "application/octet-stream";

	protected static Map<String, String> fileMap;

	@BeforeClass
	public static void initializeTests() {
		fileMap = new HashMap<>();
		fileMap.put("hello.jpg", "image/jpeg");
		fileMap.put("hello.png", "image/png");
		fileMap.put("hello.pdf", "application/pdf");
		fileMap.put("hello.tif", "image/tiff");
		fileMap.put("hello.txt", "text/plain");
		fileMap.put("hello.html", "text/html");
		fileMap.put("hello.svg", "image/svg+xml");
		fileMap.put("hello.doc", "application/msword");
		fileMap.put("hello.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	}

	@Test
	public void testMappings() throws Exception {

		PlatformMimeTypeDetector detector = PlatformMimeTypeDetector.instance;

		assertEquals("html", detector.getExtensionsForMimeType("text/html").get(0));
		assertEquals("html", detector.getExtensionsForMimeType("text / html").get(0));
		assertEquals("html", detector.getExtensionsForMimeType("text/ HTML").get(0));
		assertEquals("html", detector.getExtensionsForMimeType("text/html;q=1").get(0));
		assertEquals("html", detector.getExtensionsForMimeType("text/html; encoding=UTF-8").get(0));

		assertEquals("text/html", detector.getMimeTypesForExtension("html").get(0));
		assertEquals("text/html", detector.getMimeTypesForExtension("HTML").get(0));
		assertEquals("text/html", detector.getMimeTypesForExtension("htm").get(0));
		assertEquals("text/html", detector.getMimeTypesForExtension(" html ").get(0));

	}

	// Disabling as java.nio.file.Files.probeContentType(Path) is environment sensitive and the assertions may vary in
	// different jdk/os.
	@Test
	@Category(KnownIssue.class)
	public void testInputStreamsWithFilenames() throws Exception {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeWithInputStream(entry.getKey(), entry.getValue(), true, false);
		}
	}

	// Disabling as java.nio.file.Files.probeContentType(Path) is environment sensitive and the assertions may vary in
	// different jdk/os.
	@Test
	@Category(KnownIssue.class)
	public void testInputStreamsWithoutFilenames() throws Exception {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeWithInputStream(entry.getKey(), entry.getValue(), false, true);
		}
	}

	@Test
	public void testFilesWithFilenames() {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeWithFile(entry.getKey(), entry.getValue(), true, false);
		}
	}

	// Disabling as java.nio.file.Files.probeContentType(Path) is environment sensitive and the assertions may vary in
	// different jdk/os.
	@Test
	@Category(KnownIssue.class)
	public void testFilesWithoutFilenames() {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeWithFile(entry.getKey(), entry.getValue(), false, true);
		}
	}

	protected void checkMimeTypeWithInputStream(String source, String expectedMimeType, boolean includeName, boolean allowDefault) throws Exception {
		Path path = Paths.get(source);
		try (InputStream in = new FileInputStream("res/mimeTypeDetection/" + source)) {
			String mimeType = PlatformMimeTypeDetector.instance.getMimeType(in, includeName ? path.getFileName().toString() : null);
			assertMimeType(expectedMimeType, mimeType, allowDefault);
		}

	}

	protected void checkMimeTypeWithFile(String source, String expectedMimeType, boolean includeName, boolean allowDefault) {
		Path path = Paths.get("res/mimeTypeDetection/" + source);
		File file = path.toFile();
		String mimeType = PlatformMimeTypeDetector.instance.getMimeType(file, includeName ? path.getFileName().toString() : null);
		assertMimeType(expectedMimeType, mimeType, allowDefault);
	}

	private void assertMimeType(String expected, String actual, boolean allowDefault) {
		if (allowDefault && !StringTools.isBlank(actual) && actual.equalsIgnoreCase(defaultType)) {
			return;
		}
		Assert.assertEquals(expected, actual);
	}

}
