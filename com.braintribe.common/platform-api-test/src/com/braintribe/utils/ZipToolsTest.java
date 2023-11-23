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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;

public class ZipToolsTest {

	@Test
	public void testNormalZip() throws Exception {

		String testString = "Test String";

		File zipFile = File.createTempFile("test", ".zip");
		File tempDir = null;
		try {
			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {

				ZipEntry e = new ZipEntry("test/mytest.txt");
				out.putNextEntry(e);

				byte[] data = testString.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}

			tempDir = FileTools.createNewTempDir(UUID.randomUUID().toString());
			ZipTools.unzip(zipFile, tempDir);

			File testFile = new File(tempDir, "test/mytest.txt");
			String actual = IOTools.slurp(testFile, "UTF-8");

			assertThat(actual).isEqualTo(testString);

		} finally {
			FileTools.deleteFileSilently(zipFile);
			FileTools.deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void testUnallowedRelativePathZip() throws Exception {

		String testString = "Test String";

		File zipFile = File.createTempFile("test", ".zip");
		File tempDir = null;
		try {
			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {

				ZipEntry e = new ZipEntry("../mytest.txt");
				out.putNextEntry(e);

				byte[] data = testString.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}

			tempDir = FileTools.createNewTempDir(UUID.randomUUID().toString());
			try {
				ZipTools.unzip(zipFile, tempDir);
				fail("The relative path in the ZIP file should have provoked an exception.");
			} catch (Exception expected) {
				// Nothing to do; all well
			}

		} finally {
			FileTools.deleteFileSilently(zipFile);
			FileTools.deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void testZipBomb() throws Exception {
		int maxEntries = 10000;
		long maxUnzippedTotalLength = Numbers.MEGABYTE * maxEntries;
		long maxUnzippedEntryLength = Numbers.MEGABYTE;
		double thresholdRatio = 100d;

		AtomicInteger entries = new AtomicInteger(0);
		AtomicLong bytesRead = new AtomicLong();
		try (InputStream in = new FileInputStream("res/zip/zip-bomb-do-not-extract.zip")) {
			ZipTools.processZipSafely(in, e -> {
				System.out.println("Processing entry " + e.getName());
				try (InputStream entryInputStream = e.getInputStream()) {
					entries.incrementAndGet();
					int readBytes = 0;
					byte[] buffer = new byte[2048];
					while ((readBytes = entryInputStream.read(buffer)) != -1) {
						bytesRead.addAndGet(readBytes);
					}
				} catch (IOException ioe) {
					throw Exceptions.unchecked(ioe, "Error while reading entry " + e);
				}
			}, maxEntries, maxUnzippedTotalLength, maxUnzippedEntryLength, thresholdRatio);
		} catch (IndexOutOfBoundsException expected) {
			System.out.println("Got expected IndexOutOfBoundsException exception: " + expected.getMessage());
		}
		System.out.println("Read " + entries.get() + " entries with total bytes: " + bytesRead.get());
	}

	@Test
	public void testNormalZipSafely() throws Exception {
		int maxEntries = 10000;
		long maxUnzippedTotalLength = Numbers.MEGABYTE * maxEntries;
		long maxUnzippedEntryLength = Numbers.MEGABYTE;
		double thresholdRatio = 100d;

		AtomicInteger entries = new AtomicInteger(0);
		AtomicLong bytesRead = new AtomicLong();
		try (InputStream in = new FileInputStream("res/zip/test.zip")) {
			ZipTools.processZipSafely(in, e -> {
				System.out.println("Processing entry " + e.getName());
				try (InputStream entryInputStream = e.getInputStream()) {
					entries.incrementAndGet();
					int readBytes = 0;
					byte[] buffer = new byte[2048];
					while ((readBytes = entryInputStream.read(buffer)) != -1) {
						bytesRead.addAndGet(readBytes);
					}
				} catch (IOException ioe) {
					throw Exceptions.unchecked(ioe, "Error while reading entry " + e);
				}
			}, maxEntries, maxUnzippedTotalLength, maxUnzippedEntryLength, thresholdRatio);
		}
		System.out.println("Read " + entries.get() + " entries with total bytes: " + bytesRead.get());
	}
}
