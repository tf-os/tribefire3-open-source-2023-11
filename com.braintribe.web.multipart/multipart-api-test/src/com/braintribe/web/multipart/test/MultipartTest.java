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
package com.braintribe.web.multipart.test;


import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.BasicDelegateInputStream;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartHeaders;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.ChunkedFormDataWriter;
import com.braintribe.web.multipart.impl.Multiparts;
import com.braintribe.web.multipart.impl.SequentialParallelFormDataWriter;
import com.braintribe.web.multipart.test.MultipartTestUtils.PartStreamingMethod;

public class MultipartTest extends AbstractTest{
	private static String boundary = "boundary-" + UUID.randomUUID().toString();
	private static File dataDir = new File("data");

	@Before
	public void init() throws IOException {
		if (dataDir.exists()) {
			FileTools.deleteDirectoryRecursively(dataDir);
		}

		dataDir.mkdirs();
	}

	@Test
	public void testMultipleSmallPartsPerformance() throws Exception {
		File[] generatedFiles = new File[100];
		File generatedFilesDir = new File(dataDir, "generated");

		generatedFilesDir.mkdirs();


		for (int i = 0; i < generatedFiles.length; i++) {
			File generatedFile = new File(generatedFilesDir, "file_" + i);
			MultipartTestUtils.writeRandomText(100, boundary, new FileOutputStream(generatedFile));

			generatedFiles[i] = generatedFile;
		}

		File bodyFile = new File(dataDir, "body");

		Map<PartStreamingMethod, Long> times = new HashMap<>();

		for (PartStreamingMethod method: PartStreamingMethod.values()) {
			times.put(method, 0L);
		}

		for (int i=0; i<10; i++) {
			for (PartStreamingMethod method: PartStreamingMethod.values()) {
				String currentBoundary = boundary;
				if (method == PartStreamingMethod.chunked)
					currentBoundary = null;
				
				File targetFolder = new File(dataDir, "output");
				targetFolder.mkdirs();

				System.out.println("Writing multipart with method: " + method);
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(bodyFile), IOTools.SIZE_32K)) {
					writeMultipart(method, out, currentBoundary, generatedFiles);
				}

				StopWatch stopWatch = new StopWatch();
				transferParts(bodyFile, currentBoundary, targetFolder);

				long newTime = times.get(method) + stopWatch.getElapsedTime();
				times.put(method, newTime);

				for (File file : generatedFiles) {
					File transferredFile = new File(targetFolder, file.getName());
					Assertions.assertThat(file).hasSameContentAs(transferredFile);
				}
				targetFolder.delete();
			}
		}

		for (PartStreamingMethod method: PartStreamingMethod.values()) {
			System.out.println("Reading took for " + method + ": " + times.get(method));
		}

	}

	@Test
	public void testGmSerialization() throws Exception {

		File assemblyFile = new File(dataDir, "assembly.bin");

		try (PrintStream out = new PrintStream(new FileOutputStream(assemblyFile), true, "UTF-8")) {
			for (int i = 0; i < 100_000; i++) {
				out.println(i);
			}
		}

		File bodyFile = new File(dataDir, "body.txt");

		writeMultipart(bodyFile, boundary, assemblyFile, new File("pom.xml"));

		String roundtrippedData = null;

		try (InputStream in = new BufferedInputStream(new FileInputStream(bodyFile))) {
			SequentialFormDataReader formDataReader = Multiparts.formDataReader(in, boundary).sequential();

			PartReader partReader = null;

			while ((partReader = formDataReader.next()) != null) {
				String fileName = partReader.getFileName();
				
				if (fileName == null) {
					assertThat(partReader.getName()).isEqualTo(PartHeaders.PART_ANNOUNCEMENT);
					partReader.consume();
					continue;
				}
				
				switch (fileName) {
					case "assembly.bin":
						try (InputStream partIn = partReader.openStream()) {
							roundtrippedData = IOTools.slurp(partIn, "UTF-8");
						}
						break;
					default:
						partReader.consume();
						break;
				}
			}
		}

		Assertions.assertThat(assemblyFile).hasContent(roundtrippedData);
	}

	private void testPerformance(PartStreamingMethod partStreaming) throws FileNotFoundException, IOException, Exception {
			dataDir.mkdirs();

			File bodyFile = new File(dataDir, "body.txt");
			File targetFile = new File(dataDir, "test-target.bin");

			File generatedFile = new File(dataDir, "test-source.bin");

			int testResourceSize = 100_000_000;
			MultipartTestUtils.writeRandomDataFile(generatedFile, testResourceSize);

			StopWatch stopWatch = new StopWatch();
			try (OutputStream out = new FileOutputStream(bodyFile)) {
				writeMultipart(partStreaming, out, boundary, generatedFile);
			}
			System.out.println(partStreaming + ": time needed for writing parts: " + stopWatch.getElapsedTime() + "ms");


			long s = System.nanoTime();
			try (InputStream in = new FileInputStream(bodyFile);
				SequentialFormDataReader formDataReader = partStreaming == PartStreamingMethod.chunked ? Multiparts.formDataReader(in) : Multiparts.formDataReader(in, boundary).sequential()) {

				PartReader partReader = null;

				while ((partReader = formDataReader.next()) != null) {
					String fileName = partReader.getFileName();
					
					if (fileName == null) {
						assertThat(partReader.getName()).isEqualTo(PartHeaders.PART_ANNOUNCEMENT);
						partReader.consume();
						continue;
					}
					
					switch (fileName) {
						case "test-source.bin":
							long bytes = 0;
							try (InputStream partIn = partReader.openStream(); OutputStream out = new FileOutputStream(targetFile)) {
								bytes = IOTools.transferBytes(partIn, out, IOTools.BUFFER_SUPPLIER_64K);
							}

							break;
						default:
							partReader.consume();
							break;
					}
				}
			}

			long e = System.nanoTime();
			long d = e - s;
			System.out.println(partStreaming + ": time needed for reading " + testResourceSize + " bytes from part: " + d / 1_000_000d + "ms");
	}

	@Test
	public void testPerformance() throws Exception {
		testPerformance(PartStreamingMethod.raw);
		testPerformance(PartStreamingMethod.contentLengthAware);
		testPerformance(PartStreamingMethod.chunked);
	}

	@Test
	public void testEagerPartReading() throws Exception {
		testEagerPartReading(PartStreamingMethod.contentLengthAware);
		testEagerPartReading(PartStreamingMethod.raw);
	}
	public void testEagerPartReading(PartStreamingMethod partStreaming) throws Exception {
		File data = new File(dataDir, "data.txt");
		File partDir = new File(dataDir, "part");
		File targetFolder = new File(dataDir, "output");

		targetFolder.mkdirs();
		partDir.mkdirs();
		generateTextFiles(partDir, 5, 800_000);
		File files[] = Stream.of(partDir.listFiles()).filter(File::isFile).toArray(File[]::new);

		data.createNewFile();

		try (OutputStream out = new FileOutputStream(data);
				FileInputStream in = new FileInputStream(data);
				// Note that both the formDataReader and -Writer work with the same file without closing themselves until everything was read and written
				// This proves that a part can be fully read (and processed) before the next one was even opened
				SequentialFormDataReader formDataReader = Multiparts.formDataReader(in, boundary).sequential();
				FormDataWriter formDataWriter = Multiparts.formDataWriter(out, boundary);
				) {

			for (File generatedPartFile : files) {
				MutablePartHeader partHeader = Multiparts.newPartHeader();

				partHeader.setContentType(PlatformMimeTypeDetector.instance.getMimeType(generatedPartFile, generatedPartFile.getName()));
				partHeader.setFileName(generatedPartFile.getName());
				partHeader.setName(UUID.randomUUID().toString());

				if (partStreaming == PartStreamingMethod.chunked)
					partHeader.setTransferEncoding("chunked");
				else if (partStreaming == PartStreamingMethod.contentLengthAware)
					partHeader.setContentLength("" + generatedPartFile.length());

				// Write the part into the data.txt
				PartWriter openPart = formDataWriter.openPart(partHeader);
				try (InputStream partIn = new FileInputStream(generatedPartFile); OutputStream partOut = openPart.outputStream()) {
					IOTools.transferBytes(partIn, partOut, IOTools.BUFFER_SUPPLIER_64K);
				}

				// Read the part from data.txt
				PartReader partReader = formDataReader.next();
				String fileName = partReader.getFileName();
				File partTargetFile = new File(targetFolder, fileName);
				try (InputStream partIn = partReader.openStream(); OutputStream targetfileout = new BufferedOutputStream(new FileOutputStream(partTargetFile))) {
					// transferBytes() only returns after the inputStream (from partReader) was closed
					IOTools.transferBytes(partIn, targetfileout, IOTools.BUFFER_SUPPLIER_64K);
				}

				// Assert that the part was read correctly
				Assertions.assertThat(partTargetFile).hasSameContentAs(generatedPartFile);
			}
		}
	}
	
	@Test
	public void readProprietaryMultipart() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream("--a6--uuid-1\nb!5\n12345\n--a0--".getBytes());
		SequentialFormDataReader formDataReader = Multiparts.formDataReader(in);
		
		PartReader part = formDataReader.next();
		InputStream partIn = part.openStream();
		
		IOTools.transferBytes(partIn, System.out);
		
		partIn.close();
		formDataReader.close();
		
		File proprietaryFormatMultipartFile = new File(dataDir, "prop.txt");
		try (OutputStream out = new FileOutputStream(proprietaryFormatMultipartFile)){
			writeMultipart(PartStreamingMethod.contentLengthAware, out, null, new File("pom.xml"));
		}
		
		try (InputStream fileIn = new FileInputStream(proprietaryFormatMultipartFile)){
			formDataReader = Multiparts.formDataReader(fileIn);
			
			part = formDataReader.next();
			partIn = part.openStream();
			
			IOTools.transferBytes(partIn, System.out);
			
			partIn.close();
			formDataReader.close();
		}
	}
	
	/**
	 * When this test fails locally at your machine check first if the test file "proprietary1.tmp" was checked out correctly with unix line endings
	 */
	@Test
	public void readProprietaryMultipartFromFile() throws Exception {
		
		File proprietaryFormatMultipartFile = existingTestFile("proprietary1.tmp");
		File expectedOutFile = existingTestFile("expectedPartout");
		File outFile = new File(dataDir, "partout");
		
		try (OutputStream out = new FileOutputStream(outFile);
			InputStream fileIn = new FileInputStream(proprietaryFormatMultipartFile)){
			SequentialFormDataReader formDataReader = Multiparts.formDataReader(fileIn);
			
			PartReader part;
			while ((part = formDataReader.next()) != null) {
				try (InputStream partIn = part.openStream()){
					out.write("### Next Part ###\n".getBytes());
					IOTools.transferBytes(partIn, out);
				}
			}
			
			formDataReader.close();
		}
		
		assertThat(outFile).hasSameContentAs(expectedOutFile);
	}
	
	private class TransparentInputStream extends BasicDelegateInputStream {
		File file = new File(dataDir, "inputstream.txt");
		FileOutputStream fos;

		public TransparentInputStream(InputStream delegate) {
			super(delegate);
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				throw Exceptions.unchecked(e, "Could not create file for TIS");
			}
		}
		
		@Override
		public int read() throws IOException {
			int i = super.read();
			if (i>=0) {
				fos.write(i);
				fos.flush();
			}
			return i;
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			int read = super.read(b);
			if (read > 0) {
				fos.write(b);
				fos.flush();
			}
			return read;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int read = super.read(b, off, len);
			if (read > 0) {
				fos.write(b, off, len);
				fos.flush();
			}
			return read;
		}
	}
	
	@Test
	public void malformedProprietaryMultipart() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream("-!a6--uuid-1\na5\n12345\n\n--\n".getBytes());
		SequentialFormDataReader formDataReader = Multiparts.formDataReader(in);
		
		Assertions.assertThatThrownBy(() -> formDataReader.next()).isExactlyInstanceOf(IOException.class);
		
		ByteArrayInputStream in2 = new ByteArrayInputStream("--a6-!uuid-1\na5\n12345\n\n--\n".getBytes());
		SequentialFormDataReader formDataReader2 = Multiparts.formDataReader(in2);
		
		Assertions.assertThatThrownBy(() -> formDataReader2.next()).isExactlyInstanceOf(IOException.class);
		
	}

	@Test
	public void testPartCorrectness() throws Exception {

		File partDir = new File(dataDir, "parts");

		StopWatch watch = new StopWatch();
		partDir.mkdirs();
		generateTextFiles(partDir, 3, 20_000_000);

		System.out.println("generated files in " + watch.getLastElapsedTime() + " ms");

		File files[] = Stream.of(partDir.listFiles()).filter(File::isFile).toArray(File[]::new);

		File bodyFile = new File(dataDir, "body.txt");

		writeMultipart(bodyFile, boundary, files);

		File partTransferFolder = new File(dataDir, "part-transfers");

		partTransferFolder.mkdirs();

		transferParts(bodyFile, boundary, partTransferFolder);

		for (File file : files) {
			File transferredFile = new File(partTransferFolder, file.getName());
			Assertions.assertThat(file).hasSameContentAs(transferredFile);
		}

	}
	
	@Test
	public void testBlobPartCorrectness() throws Exception {
		
		File partDir = new File(dataDir, "parts");
		
		StopWatch watch = new StopWatch();
		partDir.mkdirs();
		generateTextFiles(partDir, 3, 20_000_000);
		
		System.out.println("generated files in " + watch.getLastElapsedTime() + " ms");
		
		File files[] = Stream.of(partDir.listFiles()).filter(File::isFile).toArray(File[]::new);
		
		File bodyFile = new File(dataDir, "body.txt");
		try (FileOutputStream bodyOut = new FileOutputStream(bodyFile);
			FormDataWriter blobFormDataWriter = Multiparts.blobFormDataWriter(bodyOut, boundary)){
			
			writeMultipart(blobFormDataWriter, files);
		}
		
		File partTransferFolder = new File(dataDir, "part-transfers");
		
		partTransferFolder.mkdirs();
		
		transferParts(bodyFile, boundary, partTransferFolder);
		
		File partAnnouncement = new File(partTransferFolder, "part-anouncement");
		String partsString = IOTools.slurp(partAnnouncement, "UTF-8");
		String[] partsArr = partsString.split("\r\n");
		
		byte[] bodyAsBytes;
		try (FileInputStream inputStream = new FileInputStream(bodyFile)){
			bodyAsBytes = IOTools.inputStreamToByteArray(inputStream);
		}
		
		for (int i=0; i < files.length; i++) {
			String[] parameters = partsArr[i].split("&");
			int start=0, end=0;
			String fileName = null;
			for (String param: parameters) {
				Pair<String, String> pair = StringTools.splitDelimitedPair(param, '=');
				String key = pair.getFirst();
				String value = pair.getSecond();
				if (key.equals("start")) {
					start = Integer.parseInt(value);
				}
				if (key.equals("end")) {
					end = Integer.parseInt(value);
				}
				if (key.equals("name")) {
					fileName = value;
				}
			}
			
			byte[] part = Arrays.copyOfRange(bodyAsBytes, start, end);
			
			File outFile = new File(dataDir, fileName);
			try (FileOutputStream out = new FileOutputStream(outFile)){
				out.write(part);
			}
		}
		
		for (File file : files) {
			File transferredFile = new File(partTransferFolder, file.getName());
//			Assertions.assertThat(file).hasSameContentAs(transferredFile);
		}
	}
	
	@Test
	public void testProprietaryPartCorrectness() throws Exception {
		
		File partDir = new File(dataDir, "parts");
		
		StopWatch watch = new StopWatch();
		partDir.mkdirs();
		generateTextFiles(partDir, 3, 20_000_000);
		
		System.out.println("generated files in " + watch.getLastElapsedTime() + " ms");
		
		File files[] = Stream.of(partDir.listFiles()).filter(File::isFile).toArray(File[]::new);
		
		File bodyFile = new File(dataDir, "body.txt");
		
		try (FileOutputStream out = new FileOutputStream(bodyFile)){
			writeMultipart(PartStreamingMethod.chunked, out, null, files);
		}
		
		File partTransferFolder = new File(dataDir, "part-transfers");
		
		partTransferFolder.mkdirs();
		
		transferParts(bodyFile, null, partTransferFolder);
		
		for (File file : files) {
			File transferredFile = new File(partTransferFolder, file.getName());
			Assertions.assertThat(file).hasSameContentAs(transferredFile);
		}
		
	}
	
	@Test
	public void testMultiplexing() throws Exception {
		File partDir = new File(dataDir, "parts");

		StopWatch watch = new StopWatch();
		partDir.mkdirs();
		generateTextFiles(partDir, 3, 20_000_000);

		System.out.println("generated files in " + watch.getLastElapsedTime() + " ms");

		List<File> files = Stream.of(partDir.listFiles()).filter(File::isFile).collect(Collectors.toList());

		File bodyFile = new File(dataDir, "body.txt");
		
		try (OutputStream out = new FileOutputStream(bodyFile);
			FormDataWriter delegateFormDataWriter = Multiparts.formDataWriter(out, boundary);
			FormDataWriter parallelFormDataWriter = new SequentialParallelFormDataWriter(delegateFormDataWriter, StreamPipes.simpleFactory())){
			
			files.stream() //
				.parallel() //
				.forEach(f -> transferFile(f, parallelFormDataWriter));
		}
		
		File partTransferFolder = new File(dataDir, "part-transfers");
		partTransferFolder.mkdirs();

		transferParts(bodyFile, boundary, partTransferFolder);

		for (File file : files) {
			File transferredFile = new File(partTransferFolder, file.getName());
			Assertions.assertThat(file).hasSameContentAs(transferredFile);
		}

	}
	
	private void transferFile(File file, FormDataWriter formDataWriter) {
		MutablePartHeader partHeader = Multiparts.newPartHeader();
		partHeader.setContentType(PlatformMimeTypeDetector.instance.getMimeType(file, file.getName()));
		partHeader.setFileName(file.getName());
		partHeader.setName(UUID.randomUUID().toString());
		
		try (FileInputStream in = new FileInputStream(file); // 
				OutputStream out = formDataWriter.openPart(partHeader).outputStream()){
			IOTools.pump(in, out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "In test: could not transfer file");
		}
		
	}

	private static void transferParts(File file, String boundary, File targetFolder) throws Exception {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file), IOTools.SIZE_64K)) {
			transferParts(in, boundary, targetFolder);
		}
	}

	private static void transferParts(InputStream in, String boundary, File targetFolder) throws Exception {
		SequentialFormDataReader formDataReader = boundary == null ? Multiparts.formDataReader(in) : Multiparts.formDataReader(in, boundary).sequential();

		PartReader partReader = null;

		while ((partReader = formDataReader.next()) != null) {
			File file;

			String fileName = partReader.getFileName();
			if (fileName == null) {
				String name = partReader.getName();
				if (name != null)
						fileName = name;
				else
					fileName = "UNKNOWNPART_" + UUID.randomUUID().toString();
			}
			file = new File(targetFolder, fileName);
			
			try (InputStream partIn = partReader.openStream(); OutputStream out = new BufferedOutputStream(new FileOutputStream(file), IOTools.SIZE_64K)) {
				IOTools.transferBytes(partIn, out, IOTools.BUFFER_SUPPLIER_64K);
			}
		}
		
		formDataReader.close();
	}

	private static void writeMultipart(File file, String boundary, File... files) throws Exception {
		try (OutputStream out = new FileOutputStream(file)) {
			writeMultipart(PartStreamingMethod.raw, out, boundary, files);
		}
	}

	private static void generateTextFiles(File targetFolder, int number, int minfileSize) throws Exception {

		for (int i = 0; i < number; i++) {
			String name = "content" + i;
			File file = new File(targetFolder, name+".zip");

			try (OutputStream fileOut = new FileOutputStream(file);
					) {
				MultipartTestUtils.writeRandomText(minfileSize, "\r\n--" + boundary + "--", fileOut);
			}
		}
	}
	
	private static void writeMultipart(PartStreamingMethod partStreaming, OutputStream out, String boundary, File... files) throws IOException, Exception {
		FormDataWriter formDataWriter = boundary == null || partStreaming == PartStreamingMethod.chunked ? new ChunkedFormDataWriter(out) : Multiparts.formDataWriter(out, boundary);
		writeMultipart(partStreaming, formDataWriter, files);
	}

	private static void writeMultipart(FormDataWriter formDataWriter, File... files) throws IOException, Exception {
		writeMultipart(PartStreamingMethod.chunked, formDataWriter, files);
	}
	
	private static void writeMultipart(PartStreamingMethod partStreaming, FormDataWriter formDataWriter, File... files) 
			throws IOException, Exception {
		
		for (File file : files) {
			MutablePartHeader partHeader = Multiparts.newPartHeader();

			partHeader.setContentType(PlatformMimeTypeDetector.instance.getMimeType(file, file.getName()));
			partHeader.setFileName(file.getName());
			partHeader.setName(UUID.randomUUID().toString());

			if (partStreaming == PartStreamingMethod.contentLengthAware)
				partHeader.setContentLength("" + file.length());

			PartWriter openPart = formDataWriter.openPart(partHeader);

			try (InputStream partIn = new FileInputStream(file); OutputStream partOut = openPart.outputStream()) {
				IOTools.transferBytes(partIn, partOut, IOTools.BUFFER_SUPPLIER_64K);
			}
		}

		formDataWriter.close();
	}

}
