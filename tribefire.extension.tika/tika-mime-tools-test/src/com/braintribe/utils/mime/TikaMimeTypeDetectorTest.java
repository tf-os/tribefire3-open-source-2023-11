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
package com.braintribe.utils.mime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.WriteOnReadInputStream;

public class TikaMimeTypeDetectorTest {

	protected static Map<String, String> fileMap;
	protected static Map<String, String> fileMapForNameMatching;
	protected static Map<String, String> officeMap;
	protected final TikaMimeTypeDetector sharedDetector = new TikaMimeTypeDetector();

	@BeforeClass
	public static void initializeTests() {

		officeMap = new HashMap<String, String>();
		officeMap.put("res/hello.doc", "application/msword");
		officeMap.put("res/hello.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

		fileMap = new HashMap<String, String>();
		fileMap.put("res/hello.jpg", "image/jpeg");
		fileMap.put("res/hello.png", "image/png");
		fileMap.put("res/hello.pdf", "application/pdf");
		fileMap.put("res/hello.tif", "image/tiff");
		fileMap.put("res/hello.txt", "text/plain");
		fileMap.put("res/hello.html", "text/html");
		fileMap.put("res/hello.svg", "image/svg+xml");
		fileMap.put("res/hello.xml", "application/xml");

		fileMapForNameMatching = new HashMap<String, String>();
		fileMapForNameMatching.put("res/hello.jpg", "image/jpeg");
		fileMapForNameMatching.put("res/hello.png", "image/png");
		fileMapForNameMatching.put("res/hello.pdf", "application/pdf");
		fileMapForNameMatching.put("res/hello.tif", "image/tiff");
		fileMapForNameMatching.put("res/hello.txt", "text/plain");
		fileMapForNameMatching.put("res/hello.html", "text/html");
		fileMapForNameMatching.put("res/hello.svg", "image/svg+xml");
		fileMapForNameMatching.put("res/hello.avro", "avro/binary");
		fileMapForNameMatching.put("res/hello.avsc", "avro/binary");
		fileMapForNameMatching.put("res/hello.xsd", "application/xml");
		fileMapForNameMatching.put("res/hello.yml", "text/x-yaml");
		fileMapForNameMatching.put("res/hello.yaml", "text/x-yaml");
		fileMapForNameMatching.put("res/hello.xml", "application/xml");
	}

	@Test
	public void testFilesWithFilenames() throws Exception {

		for (Map.Entry<String, String> entry : fileMapForNameMatching.entrySet()) {
			this.checkMimeType(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testInputStreamsWithFilenames() throws Exception {
		for (Map.Entry<String, String> entry : fileMapForNameMatching.entrySet()) {
			this.checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), true, false);
		}
	}

	@Test
	public void testFilesWithoutFilenames() throws Exception {

		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeNoFilename(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testInputStreamsWithoutFilenames() throws Exception {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), false, false);
		}
	}

	@Test
	public void testFilesWithoutAnyFilenameInformation() throws Exception {

		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeNoFilenameAtAll(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testOfficeFilesWithFilenames() throws Exception {

		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeType(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testOfficeInputStreamsWithFilenames() throws Exception {
		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), true, false);
		}
	}

	@Test
	public void testOfficeFilesWithoutFilenames() throws Exception {

		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeTypeNoFilename(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testOfficeInputStreamsWithoutFilenames() throws Exception {
		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), false, false);
		}
	}

	@Test
	public void testOfficeFilesWithoutAnyFilenameInformation() throws Exception {

		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeTypeNoFilenameAtAll(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testDetectionOnPump() throws Exception {
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			this.checkMimeTypeOnPump(entry.getKey(), entry.getValue(), false, true);
		}
		for (Map.Entry<String, String> entry : officeMap.entrySet()) {
			this.checkMimeTypeOnPump(entry.getKey(), entry.getValue(), false, true);
		}
	}

	@Ignore
	protected void checkMimeType(String path, String expectedMimeType) throws Exception {
		TikaMimeTypeDetector detector = new TikaMimeTypeDetector();
		File file = new File(path);
		String mimeType = detector.getMimeType(file, file.getName());
		Assert.assertEquals(expectedMimeType, mimeType);
	}

	@Ignore
	protected void checkMimeTypeNoFilename(String path, String expectedMimeType) throws Exception {
		TikaMimeTypeDetector detector = new TikaMimeTypeDetector();
		File file = new File(path);
		String mimeType = detector.getMimeType(file, null);
		Assert.assertEquals(expectedMimeType, mimeType);
	}

	@Ignore
	protected void checkMimeTypeNoFilenameAtAll(String path, String expectedMimeType) throws Exception {
		TikaMimeTypeDetector detector = new TikaMimeTypeDetector();
		File file = new File(path);
		File tmpFile = File.createTempFile("test", null);
		try {
			FileTools.copyFile(file, tmpFile);
			String mimeType = detector.getMimeType(tmpFile, null);
			Assert.assertEquals(expectedMimeType, mimeType);
		} finally {
			tmpFile.delete();
		}
	}

	@Ignore
	protected void checkMimeTypeNoFilenameAtAllSharedDetector(String path, String expectedMimeType) throws Exception {
		File file = new File(path);
		File tmpFile = File.createTempFile("test", null);
		try {
			FileTools.copyFile(file, tmpFile);
			String mimeType = this.sharedDetector.getMimeType(tmpFile, null);
			Assert.assertEquals(expectedMimeType, mimeType);
		} finally {
			tmpFile.delete();
		}
	}

	protected void checkMimeTypeFromInputStream(String source, String expectedMimeType, boolean includeName, boolean useSharedDetector)
			throws Exception {
		TikaMimeTypeDetector detector = useSharedDetector ? sharedDetector : new TikaMimeTypeDetector();
		Path path = Paths.get(source);
		try (InputStream in = Files.newInputStream(path)) {
			String mimeType = detector.getMimeType(in, includeName ? path.getFileName().toString() : null);
			Assert.assertEquals(expectedMimeType, mimeType);
		}
	}

	protected void checkMimeTypeOnPump(String source, String expectedMimeType, boolean includeName, boolean useSharedDetector) throws Exception {

		TikaMimeTypeDetector detector = useSharedDetector ? sharedDetector : new TikaMimeTypeDetector();
		Path path = Paths.get(source);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = Files.newInputStream(path);
				WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);) {
			String mimeType = detector.getMimeType(win, includeName ? path.getFileName().toString() : null);
			Assert.assertEquals(expectedMimeType, mimeType);
			win.consume();
			out.flush();
			try (InputStream rin = Files.newInputStream(path)) {
				Assert.assertArrayEquals(IOTools.slurpBytes(rin), out.toByteArray());
			}
		}

	}

	@Test
	public void testConcurrent() throws Exception {
		testConcurrent(true, true);
	}

	@Test
	public void testConcurrentFilesOnly() throws Exception {
		testConcurrent(true, false);
	}

	@Test
	public void testConcurrentInputStreamsOnly() throws Exception {
		testConcurrent(false, true);
	}

	@Ignore
	protected void testConcurrent(boolean files, boolean inputStreams) throws Exception {
		int workerCount = 20;
		int iterations = 20;
		ExecutorService executor = Executors.newFixedThreadPool(workerCount);
		List<Future<Void>> futures = new ArrayList<>(workerCount);
		for (int i = 0; i < workerCount; ++i) {
			futures.add(executor.submit(new Worker(iterations, files, inputStreams)));
		}
		for (Future<Void> f : futures) {
			f.get();
		}
		executor.shutdown();
	}

	private class Worker implements Callable<Void> {

		int iterations;
		boolean files = true;
		boolean inputStreams = true;

		public Worker(int iterations, boolean files, boolean inputStreams) {
			this.iterations = iterations;
			this.files = files;
			this.inputStreams = inputStreams;
		}

		@Override
		public Void call() throws Exception {
			for (int i = 0; i < iterations; ++i) {
				for (Map.Entry<String, String> entry : fileMap.entrySet()) {
					if (files)
						checkMimeTypeNoFilenameAtAllSharedDetector(entry.getKey(), entry.getValue());
					if (inputStreams)
						checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), false, true);
				}
				for (Map.Entry<String, String> entry : officeMap.entrySet()) {
					if (files)
						checkMimeTypeNoFilenameAtAllSharedDetector(entry.getKey(), entry.getValue());
					if (inputStreams)
						checkMimeTypeFromInputStream(entry.getKey(), entry.getValue(), false, true);
				}
			}
			return null;
		}

	}
}
