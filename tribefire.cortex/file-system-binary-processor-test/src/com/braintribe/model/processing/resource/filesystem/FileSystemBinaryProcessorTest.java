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
package com.braintribe.model.processing.resource.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.resource.filesystem.common.ProcessorConfig;
import com.braintribe.model.processing.resource.filesystem.common.TestFile;
import com.braintribe.model.processing.resource.filesystem.wire.FileSystemBinaryProcessorTestWireModule;
import com.braintribe.model.processing.resource.filesystem.wire.contract.MainContract;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * {@link FileSystemBinaryProcessor} tests.
 * 
 */
public class FileSystemBinaryProcessorTest implements BinaryProcessorTestCommons {

	private static final Logger logger = Logger.getLogger(FileSystemBinaryProcessorTest.class);

	@ClassRule
	public static TemporaryFolder outputFolder = new TemporaryFolder();

	protected static WireContext<MainContract> context;

	@BeforeClass
	public static void beforeClass() throws Exception {
		context = Wire.context(FileSystemBinaryProcessorTestWireModule.INSTANCE);
		context.contract().tempPathHolder().accept(outputFolder.getRoot().toPath());
	}

	@AfterClass
	public static void afterClass() throws Exception {
		context.shutdown();
	}

	@Test
	public void testStore() {

		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();
		Path accessPath = context.contract().access1Path();

		String targetName = "uploaded-" + testFile.name();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, targetName);

		Assert.assertNotNull("response", storeResponse);

		Resource resource = storeResponse.getResource();

		Assert.assertNotNull("response resource", resource);
		Assert.assertNotNull("response resource name", resource.getName());
		Assert.assertEquals("response resource name", targetName, resource.getName());
		Assert.assertTrue("source should have been FileSystemSource, but is: " + resource.getResourceSource(),
				resource.getResourceSource() instanceof FileSystemSource);

		FileSystemSource fileSystemSource = (FileSystemSource) resource.getResourceSource();

		Assert.assertNotNull("resource source path", fileSystemSource.getPath());

		Path resourcePath = accessPath.resolve(fileSystemSource.getPath());

		Assert.assertTrue("no file in expected path: " + resourcePath, Files.exists(resourcePath));

		testFile.assertContentEquals(resourcePath);

	}

	@Test
	public void testDelete() {

		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();
		Path accessPath = context.contract().access1Path();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, null);

		Resource resource = storeResponse.getResource();

		delete(SERVICE_ID_SIMPLE, access, resource);

		FileSystemSource fileSystemSource = (FileSystemSource) resource.getResourceSource();
		Path resourcePath = accessPath.resolve(fileSystemSource.getPath());

		Assert.assertFalse("resource should have been deleted", Files.exists(resourcePath));

	}

	@Test
	public void testStream() {

		ProcessorConfig processorConfig = context.contract().simpleFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, null, null);

		assertStream(processorConfig, testFile, resource, out, streamResponse, true, null, null);

	}

	@Test
	public void testStreamWithRanges() {

		ProcessorConfig processorConfig = context.contract().simpleFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, 0L, -1L);
		assertStream(processorConfig, testFile, resource, out, streamResponse, true, 0L, -1L);

		out = new ByteArrayOutputStream();
		streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, 0L, 1L);
		assertStream(processorConfig, testFile, resource, out, streamResponse, true, 0L, 1L);

		out = new ByteArrayOutputStream();
		streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, 0L, -1L);
		assertStream(processorConfig, testFile, resource, out, streamResponse, true, 0L, -1L);

		out = new ByteArrayOutputStream();
		streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, 0L, Long.MAX_VALUE);
		assertStream(processorConfig, testFile, resource, out, streamResponse, true, 0L, Long.MAX_VALUE);

		out = new ByteArrayOutputStream();
		streamResponse = stream(SERVICE_ID_SIMPLE, access, resource, out, null, 10L, 20L);
		assertStream(processorConfig, testFile, resource, out, streamResponse, true, 10L, 20L);

	}

	@Test
	public void testStreamWithTrueFingerprintMismatch() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		FingerprintMismatch condition = FingerprintMismatch.T.create();
		condition.setFingerprint("non-matching-fingerprint");

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_ENRICHING, access, resource, out, condition, null, null);

		assertStream(processorConfig, testFile, resource, out, streamResponse, true, null, null);

	}

	@Test
	public void testStreamWithFalseFingerprintMismatch() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		FingerprintMismatch condition = FingerprintMismatch.T.create();
		condition.setFingerprint(resource.getMd5());

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_ENRICHING, access, resource, out, condition, null, null);

		assertStream(processorConfig, testFile, resource, out, streamResponse, false, null, null);

	}

	@Test
	public void testStreamWithTrueModifiedSince() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ModifiedSince condition = ModifiedSince.T.create();
		condition.setDate(Date.from(Instant.now().minusSeconds(60)));

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_ENRICHING, access, resource, out, condition, null, null);

		assertStream(processorConfig, testFile, resource, out, streamResponse, true, null, null);

	}

	@Test
	public void testStreamWithFalseModifiedSince() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ModifiedSince condition = ModifiedSince.T.create();
		condition.setDate(Date.from(Instant.now().plusSeconds(60)));

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse streamResponse = stream(SERVICE_ID_ENRICHING, access, resource, out, condition, null, null);

		assertStream(processorConfig, testFile, resource, out, streamResponse, false, null, null);

	}

	@Test
	public void testGet() {

		ProcessorConfig processorConfig = context.contract().simpleFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, null);

		Resource resource = storeResponse.getResource();

		GetBinaryResponse getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, null, null);

		assertGet(processorConfig, resource, getResponse, testFile, true, null, null);

	}

	@Test
	public void testGetWithTrueFingerprintMismatch() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		FingerprintMismatch condition = FingerprintMismatch.T.create();
		condition.setFingerprint("non-matching-fingerprint");

		GetBinaryResponse getResponse = get(SERVICE_ID_ENRICHING, access, resource, condition, null, null);

		assertGet(processorConfig, resource, getResponse, testFile, true, null, null);

	}

	@Test
	public void testGetWithFalseFingerprintMismatch() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		FingerprintMismatch condition = FingerprintMismatch.T.create();
		condition.setFingerprint(resource.getMd5());

		GetBinaryResponse getResponse = get(SERVICE_ID_ENRICHING, access, resource, condition, null, null);

		assertGet(processorConfig, resource, getResponse, testFile, false, null, null);

	}

	@Test
	public void testGetWithTrueModifiedSince() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ModifiedSince condition = ModifiedSince.T.create();
		condition.setDate(Date.from(Instant.now().minusSeconds(60)));

		GetBinaryResponse getResponse = get(SERVICE_ID_ENRICHING, access, resource, condition, null, null);

		assertGet(processorConfig, resource, getResponse, testFile, true, null, null);

	}

	@Test
	public void testGetWithFalseModifiedSince() {

		ProcessorConfig processorConfig = context.contract().enrichingFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_ENRICHING, access, testFile, null);

		Resource resource = storeResponse.getResource();

		ModifiedSince condition = ModifiedSince.T.create();
		condition.setDate(Date.from(Instant.now().plusSeconds(60)));

		GetBinaryResponse getResponse = get(SERVICE_ID_ENRICHING, access, resource, condition, null, null);

		assertGet(processorConfig, resource, getResponse, testFile, false, null, null);

	}

	@Test
	public void testGetWithRanges() {

		ProcessorConfig processorConfig = context.contract().simpleFileSystemBinaryProcessorConfig();
		TestFile testFile = context.contract().testFiles().get("pdf");
		IncrementalAccess access = context.contract().access1();

		StoreBinaryResponse storeResponse = store(SERVICE_ID_SIMPLE, access, testFile, null);

		Resource resource = storeResponse.getResource();

		GetBinaryResponse getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, 0L, 10L);
		assertGet(processorConfig, resource, getResponse, testFile, true, 0L, 10L);

		getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, 0L, 1L);
		assertGet(processorConfig, resource, getResponse, testFile, true, 0L, 1L);

		getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, 0L, -1L);
		assertGet(processorConfig, resource, getResponse, testFile, true, 0L, -1L);

		getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, 0L, Long.MAX_VALUE);
		assertGet(processorConfig, resource, getResponse, testFile, true, 0L, Long.MAX_VALUE);

		getResponse = get(SERVICE_ID_SIMPLE, access, resource, null, 10L, 20L);
		assertGet(processorConfig, resource, getResponse, testFile, true, 10L, 20L);
	}

	@Test
	// Enable test again as soon as adx configuration issues are solved and security fix is enabled again
	@Category(KnownIssue.class)
	// Stream test resource with illegal location in FileSystemSource
	public void knownIssue_testSecurity() {
		IncrementalAccess access = context.contract().access1();

		String restrictedLocation = "../restricted-location.pdf";
		
		// Create respective file to not fail because of "not found"
		File restrictedFile = context.contract().access1Path().resolve(restrictedLocation).toFile();
		FileTools.writeStringToFile(restrictedFile, "Restricted Information");

		// Create a Resource pointing to that restricted location
		FileSystemSource illegalSource = FileSystemSource.T.create();
		illegalSource.setPath(restrictedLocation);
		
		Resource illegalResource = Resource.T.create();
		illegalResource.setResourceSource(illegalSource);
		
		// StreamBinary
		StreamBinary stream = StreamBinary.T.create();
		stream.setResource(illegalResource);
		
		CallStreamCapture capture = CallStreamCapture.T.create();
		capture.setOutputStreamProvider(() -> new ByteArrayOutputStream());

		stream.setCapture(capture);
		stream.setServiceId(SERVICE_ID_SIMPLE);
		stream.setDomainId(access.getAccessId());

		assertThatThrownBy(() -> stream.eval(context.contract().evaluator()).get()).isInstanceOf(IllegalArgumentException.class);
		
		// GetBinary
		GetBinary get = GetBinary.T.create();
		get.setResource(illegalResource);
		get.setServiceId(SERVICE_ID_SIMPLE);
		get.setDomainId(access.getAccessId());
		assertThatThrownBy(() -> get.eval(context.contract().evaluator()).get()).isInstanceOf(IllegalArgumentException.class);
	}
	
	protected void assertStream(ProcessorConfig processorConfig, TestFile testFile, Resource resource, ByteArrayOutputStream out,
			StreamBinaryResponse streamResponse, boolean matchingOrNoCondition, Long rangeStart, Long rangeEndInclusive) {

		Assert.assertNotNull("no response", streamResponse);

		if (matchingOrNoCondition) {

			Assert.assertFalse("null or matching condition was given, it should have been streamed", streamResponse.getNotStreamed());

			if (rangeStart != null && rangeEndInclusive != null) {
				assertThat(streamResponse.getRanged()).isTrue();

				byte[] fullSource = testFile.contents();
				if (rangeEndInclusive <= 0 || rangeEndInclusive >= fullSource.length) {
					rangeEndInclusive = fullSource.length - 1L;
				}
				int len = (int) (rangeEndInclusive - rangeStart) + 1;
				byte[] rangedResult = new byte[len];
				System.arraycopy(fullSource, rangeStart.intValue(), rangedResult, 0, len);

				Assert.assertArrayEquals(testFile.name() + " contents are not equals", rangedResult, out.toByteArray());

				Long actualRangeStart = streamResponse.getRangeStart();
				Long actualRangeEnd = streamResponse.getRangeEnd();
				assertThat(actualRangeStart).isEqualTo(rangeStart);
				assertThat(actualRangeEnd).isEqualTo(rangeEndInclusive);
			} else {
				testFile.assertContentEquals(out.toByteArray());
				assertThat(streamResponse.getRanged()).isFalse();
			}

		} else {

			Assert.assertTrue("non-matching condition was given, it shouldn't have been streamed", streamResponse.getNotStreamed());

			Assert.assertTrue(out.toByteArray().length == 0);

		}

		CacheControl cacheControl = streamResponse.getCacheControl();

		Assert.assertNotNull("no response cache control", cacheControl);

		Assert.assertEquals("response cache control has wrong fingerprint", resource.getMd5(), cacheControl.getFingerprint());
		Assert.assertEquals("response cache control has wrong max age", Integer.valueOf(processorConfig.getCacheOptions().getMaxAge()),
				cacheControl.getMaxAge());
		Assert.assertEquals("response cache control has wrong must revalidate", processorConfig.getCacheOptions().getMustRevalidate(),
				cacheControl.getMustRevalidate());
		Assert.assertEquals("response cache control has wrong cache type", processorConfig.getCacheOptions().getType(), cacheControl.getType());

	}

	protected void assertGet(ProcessorConfig processorConfig, Resource resource, GetBinaryResponse getResponse, TestFile testFile,
			boolean matchingOrNoCondition, Long rangeStart, Long rangeEnd) {

		Assert.assertNotNull("no response", getResponse);

		Resource responseResource = getResponse.getResource();

		if (matchingOrNoCondition) {

			Assert.assertNotNull("null or matching condition was given, it should have returned a resource", responseResource);

			byte[] resourceData = null;
			try (InputStream in = responseResource.openStream()) {
				resourceData = IOTools.slurpBytes(in);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			Assert.assertTrue("response resouce has no data", resourceData != null && resourceData.length > 0);

			if (rangeStart != null && rangeEnd != null) {
				byte[] fullSource = testFile.contents();
				if (rangeEnd <= 0 || rangeEnd >= fullSource.length) {
					rangeEnd = fullSource.length - 1L;
				}
				int len = (int) (rangeEnd - rangeStart) + 1;
				byte[] rangedResult = new byte[len];
				System.arraycopy(fullSource, rangeStart.intValue(), rangedResult, 0, len);

				Assert.assertArrayEquals(testFile.name() + " contents are not equals", rangedResult, resourceData);

				Long actualRangeStart = getResponse.getRangeStart();
				Long actualRangeEnd = getResponse.getRangeEnd();
				assertThat(actualRangeStart).isEqualTo(rangeStart);
				assertThat(actualRangeEnd).isEqualTo(rangeEnd);
				assertThat(responseResource.getFileSize()).isEqualTo(len);

			} else {
				testFile.assertContentEquals(resourceData);
				assertThat(getResponse.getRanged()).isFalse();
			}

		} else {
			Assert.assertNull("non-matching condition was given, it shouldn't have returned a resource", responseResource);
		}

		CacheControl cacheControl = getResponse.getCacheControl();

		Assert.assertNotNull("no response cache control", cacheControl);

		Assert.assertEquals("response cache control has wrong fingerprint", resource.getMd5(), cacheControl.getFingerprint());
		Assert.assertEquals("response cache control has wrong max age", Integer.valueOf(processorConfig.getCacheOptions().getMaxAge()),
				cacheControl.getMaxAge());
		Assert.assertEquals("response cache control has wrong must revalidate", processorConfig.getCacheOptions().getMustRevalidate(),
				cacheControl.getMustRevalidate());
		Assert.assertEquals("response cache control has wrong cache type", processorConfig.getCacheOptions().getType(), cacheControl.getType());

	}

	protected StoreBinaryResponse store(String serviceId, IncrementalAccess access, TestFile testFile, String targetName) {
		String md5;
		try {
			// md5 is needed for testing fingerprint mismatch
			md5 = CommonTools.asString(IOTools.getMD5CheckSum(testFile.stream().openInputStream()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		Resource createFrom = Resource.createTransient(testFile.stream());
		createFrom.setName(targetName == null ? testFile.name() : targetName);
		createFrom.setMd5(md5);

		StoreBinary store = StoreBinary.T.create();
		store.setCreateFrom(createFrom);
		store.setDomainId(access.getAccessId());
		store.setServiceId(serviceId);

		StoreBinaryResponse storeResponse = store.eval(context.contract().evaluator()).get();
		return storeResponse;

	}

	protected DeleteBinaryResponse delete(String serviceId, IncrementalAccess access, Resource resource) {

		DeleteBinary delete = DeleteBinary.T.create();
		delete.setResource(resource);
		delete.setDomainId(access.getAccessId());
		delete.setServiceId(serviceId);

		DeleteBinaryResponse deleteResponse = delete.eval(context.contract().evaluator()).get();

		return deleteResponse;

	}

	protected StreamBinaryResponse stream(String serviceId, IncrementalAccess access, Resource resource, OutputStream out, StreamCondition condition,
			Long rangeStart, Long rangeEnd) {

		CallStreamCapture capture = CallStreamCapture.T.create();
		capture.setOutputStreamProvider(() -> out);

		StreamBinary stream = StreamBinary.T.create();
		stream.setResource(resource);

		stream.setCapture(capture);
		stream.setCondition(condition);
		stream.setServiceId(serviceId);
		stream.setDomainId(access.getAccessId());

		if (rangeStart != null && rangeEnd != null) {
			StreamRange sr = StreamRange.T.create();
			sr.setStart(rangeStart);
			sr.setEnd(rangeEnd);
			stream.setRange(sr);
		}

		StreamBinaryResponse streamResponse = stream.eval(context.contract().evaluator()).get();

		return streamResponse;

	}

	protected GetBinaryResponse get(String serviceId, IncrementalAccess access, Resource resource, StreamCondition condition, Long rangeStart,
			Long rangeEnd) {

		GetBinary get = GetBinary.T.create();
		get.setResource(resource);
		get.setCondition(condition);
		get.setServiceId(serviceId);

		if (rangeStart != null && rangeEnd != null) {
			StreamRange sr = StreamRange.T.create();
			sr.setStart(rangeStart);
			sr.setEnd(rangeEnd);
			get.setRange(sr);
		}

		get.setDomainId(access.getAccessId());
		GetBinaryResponse getResponse = get.eval(context.contract().evaluator()).get();

		return getResponse;

	}

	public static class TestPersistenceGmSessionFactory implements PersistenceGmSessionFactory {

		private final IncrementalAccess access;

		public TestPersistenceGmSessionFactory(IncrementalAccess access) {
			this.access = access;
		}

		@Override
		public PersistenceGmSession newSession(String accessId) throws GmSessionException {
			return new BasicPersistenceGmSession(access);
		}

	}

	@Test
	public void testCollectUnusedAndEmptyParentFolders() throws Exception {

		FileSystemBinaryProcessor processor = context.contract().simpleFileSystemBinaryProcessor();
		String accessId1 = context.contract().access1().getAccessId();
		File access1Base = context.contract().access1Path().toFile();

		List<Path> list = null;

		logger.info("Test 1");
		File folder1 = new File(access1Base, "1901/3114/0000");
		folder1.mkdirs();
		File file1 = new File(folder1, "test.txt");
		list = processor.collectUnusedAndEmptyParentFolders(accessId1, file1.toPath(), "1901/3114/0000");
		assertThat(list).isEmpty();

		logger.info("Test 2");
		File folder2 = new File(access1Base, "1901/3114/0000");
		folder2.mkdirs();
		File file2 = new File(folder2, "test.txt");
		list = processor.collectUnusedAndEmptyParentFolders(accessId1, file2.toPath(), "1901/3114/0001");
		assertThat(list).containsExactly(folder2.toPath());

		logger.info("Test 3");
		File folder3 = new File(access1Base, "1901/3115/0000");
		File folder3b = new File(access1Base, "1901/3115");
		folder3.mkdirs();
		File file3 = new File(folder3, "test.txt");
		logger.info("Folder " + folder3b + " contains: " + StringTools.createStringFromArray(folder3b.listFiles()));
		list = processor.collectUnusedAndEmptyParentFolders(accessId1, file3.toPath(), "1901/3116/0000");
		assertThat(list).containsExactly(folder3.toPath(), folder3b.toPath());

		logger.info("Test 4");
		File folder4 = new File(access1Base, "1901/3117/0000");
		folder4.mkdirs();
		File file4 = new File(folder4, "test.txt");
		FileTools.writeStringToFile(file4, "Hello, world");
		list = processor.collectUnusedAndEmptyParentFolders(accessId1, file4.toPath(), "1901/3117/0001");
		assertThat(list).isEmpty();

	}

}
