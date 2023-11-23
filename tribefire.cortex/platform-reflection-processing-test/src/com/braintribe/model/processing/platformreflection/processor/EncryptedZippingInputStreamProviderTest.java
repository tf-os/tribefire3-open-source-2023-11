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
package com.braintribe.model.processing.platformreflection.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;

import net.lingala.zip4j.ZipFile;

public class EncryptedZippingInputStreamProviderTest {

	private List<File> tempFiles = new ArrayList<>();

	@After
	public void after() {
		tempFiles.forEach(FileTools::deleteFileSilently);
		tempFiles.clear();
	}

	@Test
	public void testEncryptedZip() throws Exception {

		StreamPipeFactory pipeFactory = StreamPipes.simpleFactory();
		File tmpFile = generateTempFile("Hello, world");
		Collection<File> files = CollectionTools2.asList(tmpFile);
		String name = "test";
		String password = "operating";

		EncryptedZippingInputStreamProvider provider = new EncryptedZippingInputStreamProvider(pipeFactory, name, files, false, password);
		assertThat(tmpFile).exists();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream in = provider.openInputStream()) {
			IOTools.transferBytes(in, baos, IOTools.BUFFER_SUPPLIER_64K);
		}
		byte[] zippedData = baos.toByteArray();

		File outFile = File.createTempFile("output", ".zip");
		tempFiles.add(outFile);
		IOTools.inputToFile(new ByteArrayInputStream(zippedData), outFile);

		ZipFile zipFile = new ZipFile(outFile, password.toCharArray());
		assertThat(zipFile.isEncrypted()).isTrue();
		assertThat(zipFile.isValidZipFile()).isTrue();

		File tempFolder = outFile.getParentFile();
		String tempFolderPath = tempFolder.getAbsolutePath();
		zipFile.extractFile(tmpFile.getName(), tempFolderPath, tmpFile.getName() + "-extracted");
		File extractedFile = new File(tempFolderPath, tmpFile.getName() + "-extracted");
		tempFiles.add(extractedFile);
		assertThat(extractedFile).exists();
	}

	@Test
	public void testUnencryptedZip() throws Exception {

		StreamPipeFactory pipeFactory = StreamPipes.simpleFactory();
		File tmpFile = generateTempFile("Hello, world");
		Collection<File> files = CollectionTools2.asList(tmpFile);
		String name = "test";
		String password = null;

		EncryptedZippingInputStreamProvider provider = new EncryptedZippingInputStreamProvider(pipeFactory, name, files, false, password);
		assertThat(tmpFile).exists();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream in = provider.openInputStream()) {
			IOTools.transferBytes(in, baos, IOTools.BUFFER_SUPPLIER_64K);
		}
		byte[] zippedData = baos.toByteArray();

		File outFile = File.createTempFile("output", ".zip");
		tempFiles.add(outFile);
		IOTools.inputToFile(new ByteArrayInputStream(zippedData), outFile);

		ZipFile zipFile = new ZipFile(outFile);
		assertThat(zipFile.isEncrypted()).isFalse();
		assertThat(zipFile.isValidZipFile()).isTrue();

		File tempFolder = outFile.getParentFile();
		String tempFolderPath = tempFolder.getAbsolutePath();
		zipFile.extractFile(tmpFile.getName(), tempFolderPath, tmpFile.getName() + "-extracted");
		File extractedFile = new File(tempFolderPath, tmpFile.getName() + "-extracted");
		tempFiles.add(extractedFile);
		assertThat(extractedFile).exists();
	}

	@Test
	public void testEncryptedZipWithDelete() throws Exception {

		StreamPipeFactory pipeFactory = StreamPipes.simpleFactory();
		File tmpFile = generateTempFile("Hello, world");
		Collection<File> files = CollectionTools2.asList(tmpFile);
		String name = "test";
		String password = "operating";

		EncryptedZippingInputStreamProvider provider = new EncryptedZippingInputStreamProvider(pipeFactory, name, files, true, password);
		assertThat(tmpFile).doesNotExist();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream in = provider.openInputStream()) {
			IOTools.transferBytes(in, baos, IOTools.BUFFER_SUPPLIER_64K);
		}
		byte[] zippedData = baos.toByteArray();

		File outFile = File.createTempFile("output", ".zip");
		tempFiles.add(outFile);
		IOTools.inputToFile(new ByteArrayInputStream(zippedData), outFile);

		ZipFile zipFile = new ZipFile(outFile, password.toCharArray());
		assertThat(zipFile.isEncrypted()).isTrue();
		assertThat(zipFile.isValidZipFile()).isTrue();

		File tempFolder = outFile.getParentFile();
		String tempFolderPath = tempFolder.getAbsolutePath();
		zipFile.extractFile(tmpFile.getName(), tempFolderPath, tmpFile.getName() + "-extracted");
		File extractedFile = new File(tempFolderPath, tmpFile.getName() + "-extracted");
		tempFiles.add(extractedFile);
		assertThat(extractedFile).exists();
	}
	private File generateTempFile(String text) throws IOException {
		File tmpFile = File.createTempFile("test", text == null ? ".bin" : ".txt");
		tempFiles.add(tmpFile);
		if (text == null) {
			Random rnd = new Random();
			byte[] data = new byte[1024];
			rnd.nextBytes(data);
			IOTools.inputToFile(new ByteArrayInputStream(data), tmpFile);
		} else {
			FileTools.writeStringToFile(tmpFile, text, "UTF-8");
		}

		return tmpFile;
	}
}
