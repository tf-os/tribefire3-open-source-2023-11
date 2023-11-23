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
package com.braintribe.model.processing.resource.filesystem.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class TestFile {

	static final String ROOT = "res/uploadFiles/";

	private final String resourcePath;
	private byte[] contents;
	private String fingerprint;
	private final String name;
	private final String extension;

	public static TestFile create(String resourcePath) {
		TestFile file = new TestFile(resourcePath);
		return file;
	}
	
	public static TestFile create(String ... pathParts) {
		TestFile file = new TestFile(String.join("/", pathParts));
		return file;
	}

	public TestFile(String resourcePath) {
		this.resourcePath = resourcePath;
		this.name = FileTools.getName(resourcePath);
		this.extension = FileTools.getExtension(name);
	}

	public String name() {
		return name;
	}

	public String extension() {
		return extension;
	}

	public byte[] contents() {
		if (contents == null) {
			load();
		}
		return contents;
	}

	public String fingerprint() {
		if (fingerprint == null) {
			load();
		}
		return fingerprint;
	}

	public InputStreamProvider stream() {

		return new InputStreamProvider() {

			@Override
			public InputStream openInputStream() throws IOException {
				return getClass().getClassLoader().getResourceAsStream(resourcePath);
			}

		};

	}

	public void assertContentEquals(Path path) {
		try (InputStream in = Files.newInputStream(path)) {
			assertContentEquals(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void assertContentEquals(InputStream in) {
		try {
			byte[] pathContents = IOTools.slurpBytes(in);
			assertContentEquals(pathContents);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected void load() {
		MessageDigest md = createMessageDigest("MD5");
		try (InputStream in = new DigestInputStream(stream().openInputStream(), md)) {
			contents = IOTools.slurpBytes(in);
			fingerprint = digest(md);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void assertContentEquals(byte[] contents) {
		Assertions.assertThat(contents).as(name() + " contents are not equals").isEqualTo(contents);
	}

	protected MessageDigest createMessageDigest(String digestAlgorithm) {
		try {
			return MessageDigest.getInstance(digestAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(digestAlgorithm + " digest is not available", e);
		}
	}

	protected String digest(MessageDigest md) {
		return convertToHex(md.digest());
	}

	protected static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

}
