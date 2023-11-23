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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.junit.Test;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.processing.ddra.endpoints.TestHttpResponse;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequestWithResources;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequestSimple;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipTask;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.Multiparts;

public class ApiV1RestServletMultipartTest extends AbstractApiV1RestServletTest {

	private static final String TEST_VALUE = "valueo12398zjkmasd";

	@Test
	public void testZipperProcessors() throws Exception {
		String sourceId = "zip-resource-987";

		TransientSource resourceSource = TransientSource.T.create();
		resourceSource.setGlobalId(sourceId);

		Resource resource = Resource.T.create();
		resource.setResourceSource(resourceSource);

		ZipRequest request = ZipRequest.T.create();
		ZipTask task = ZipTask.T.create();
		task.setName("test-task");
		task.setGenerateOutput(true);
		task.getInputResources().add(resource);
		request.setTasks(Arrays.asList(task));

		testZipperProcessor(request, sourceId);

		ZipRequestSimple requestSimple = ZipRequestSimple.T.create();
		requestSimple.setName("test-task");

		testZipperProcessor(requestSimple, "resource");
	}

	public void testZipperProcessor(ServiceRequest request, String resPropertyPath) throws Exception {
		String testResContent = "TEST RESOURCE";

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, requestBoundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
			header.setContentType(JSON);
			PartWriter part = formDataWriter.openPart(header);
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			try (OutputStream marshallOut = part.outputStream()) {
				marshaller.marshall(marshallOut, request);
			}

			header.setName(resPropertyPath);
			header.setContentType("application/text");
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(testResContent.getBytes());
			}
		}

		TestHttpResponse response = requests.servicePost(request.entityType()).accept("multipart/form-data")
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary).body(outputStream.toByteArray()).execute();

		Header contentDispisition = response.getResponse().getFirstHeader("Content-Type");
		String boundary = contentDispisition.getElements()[0].getParameterByName("boundary").getValue();
		try (InputStream content = response.getResponse().getEntity().getContent(); //
				SequentialFormDataReader formDataReader = Multiparts.formDataReader(content, boundary).sequential()) {
			PartReader part = formDataReader.next();

			assertEquals(RpcConstants.RPC_MAPKEY_RESPONSE, part.getName());

			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			Resource resource;
			try (InputStream in = part.openStream()) {
				resource = (Resource) marshaller.unmarshall(in);
				assertEquals("test-task.zip", resource.getName());
			}

			// Resource data part
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals(resource.getResourceSource().getGlobalId(), part.getName());

			try (ZipInputStream zipIn = new ZipInputStream(part.openStream())) {
				ZipEntry entry = zipIn.getNextEntry();
				String readString = IOTools.slurp(zipIn, "UTF-8");
				assertEquals(readString, testResContent);
			}

			// part announcement: just assert that it is there
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals("part-anouncement", part.getName());
			part.consume();

			part = formDataReader.next();
			assertNull(part);
		}

	}

	@Test
	public void testMultipartResponseWithoutResources() throws Exception {
		TestHttpResponse response = requests.serviceGet(TestServiceRequest.T).urlParameter("value", TEST_VALUE).accept("multipart/form-data")
				.execute();

		Header contentDispisition = response.getResponse().getFirstHeader("Content-Type");
		String boundary = contentDispisition.getElements()[0].getParameterByName("boundary").getValue();
		try (InputStream content = response.getResponse().getEntity().getContent(); //
				SequentialFormDataReader formDataReader = Multiparts.formDataReader(content, boundary).sequential()) {
			PartReader part = formDataReader.next();

			assertEquals(RpcConstants.RPC_MAPKEY_RESPONSE, part.getName());

			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			try (InputStream in = part.openStream()) {
				TestServiceRequest unmarshalledResponse = (TestServiceRequest) marshaller.unmarshall(in);
				System.out.println(unmarshalledResponse);

				assertEquals(TEST_VALUE, unmarshalledResponse.getValue());
			}

			// part announcement: just assert that it is there
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals("part-anouncement", part.getName());
			part.consume();

			part = formDataReader.next();
			assertNull(part);
		}
	}

	@Test
	public void testMultipartRequestWithoutResources() throws Exception {
		TestServiceRequest request = TestServiceRequest.T.create();
		request.setValue(TEST_VALUE);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String boundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, boundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
			header.setContentType(JSON);
			PartWriter part = formDataWriter.openPart(header);
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			try (OutputStream marshallOut = part.outputStream()) {
				marshaller.marshall(marshallOut, request);
			}
		}

		TestHttpResponse response = requests.servicePost(TestServiceRequest.T).accept(JSON)
				.header("Content-Disposition", "multipart/form-data;boundary=" + boundary).contentType("multipart/form-data;boundary=" + boundary)
				.body(outputStream.toByteArray()).execute();

		InputStream content = response.getResponse().getEntity().getContent();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		TestServiceRequest unmarshalledResponse = (TestServiceRequest) marshaller.unmarshall(content);
		System.out.println(unmarshalledResponse);

		assertEquals(TEST_VALUE, unmarshalledResponse.getValue());

	}

	@Test
	public void testMultipartRequestWithScalarPart() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String boundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, boundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName("value");
			header.setContentType("application/text");
			PartWriter part = formDataWriter.openPart(header);

			try (OutputStream out = part.outputStream()) {
				out.write("MyValue".getBytes());
			}
		}

		TestHttpResponse response = requests.servicePost(TestServiceRequest.T).accept(JSON)
				.header("Content-Disposition", "multipart/form-data;boundary=" + boundary).contentType("multipart/form-data;boundary=" + boundary)
				.body(outputStream.toByteArray()).execute();

		InputStream content = response.getResponse().getEntity().getContent();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		TestServiceRequest unmarshalledResponse = (TestServiceRequest) marshaller.unmarshall(content);
		System.out.println(unmarshalledResponse);

		assertEquals("MyValue", unmarshalledResponse.getValue());

	}

	@Test
	public void testMultipartRequestOverridingWithScalarPart() throws Exception {

		TestServiceRequest request = TestServiceRequest.T.create();
		request.setValue("YourValue");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String boundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, boundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
			header.setContentType(JSON);
			PartWriter part = formDataWriter.openPart(header);
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			try (OutputStream marshallOut = part.outputStream()) {
				marshaller.marshall(marshallOut, request);
			}

			header = Multiparts.newPartHeader();
			header.setName("value");
			header.setContentType("application/text");
			part = formDataWriter.openPart(header);

			try (OutputStream out = part.outputStream()) {
				out.write("MyValue".getBytes());
			}
		}

		TestHttpResponse response = requests.servicePost(TestServiceRequest.T).accept(JSON)
				.header("Content-Disposition", "multipart/form-data;boundary=" + boundary).contentType("multipart/form-data;boundary=" + boundary)
				.body(outputStream.toByteArray()).execute();

		InputStream content = response.getResponse().getEntity().getContent();

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		TestServiceRequest unmarshalledResponse = (TestServiceRequest) marshaller.unmarshall(content);
		System.out.println(unmarshalledResponse);

		assertEquals("MyValue", unmarshalledResponse.getValue());

	}

	@Test
	public void testMultipartResourceRoundtrip() throws Exception {
		TestServiceRequestWithResources request = TestServiceRequestWithResources.T.create();
		request.setValue("MyValue");

		testMultipartResourceRoundtrip(request);
	}

	@Test
	public void testMultipartResourceRoundtripWithDefaultRequest() throws Exception {
		testMultipartResourceRoundtrip(null);
	}

	public void testMultipartResourceRoundtrip(TestServiceRequestWithResources request) throws Exception {
		final String testResourceContent = "Resource Content";
		final int numElementsResourceList = 5;

		Resource requestResource = Resource.createTransient(null);
		requestResource.setMimeType("test/test"); // purposefully assign wrong mime type to make sure it is never overridden or corrected
		String requestResourceHeaderName = request == null ? "resource" : requestResource.getResourceSource().getGlobalId();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, requestBoundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			PartWriter part;

			if (request != null) {
				request.setResource(requestResource);

				header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
				header.setContentType(JSON);
				part = formDataWriter.openPart(header);
				JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

				try (OutputStream marshallOut = part.outputStream()) {
					marshaller.marshall(marshallOut, request);
				}
			}

			header.setName(requestResourceHeaderName);
			header.setContentType("application/text"); // Note that this will/should not show up in the response resource
			header.setFileName("test-file");
			header.setContentLength(String.valueOf(testResourceContent.length()));
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(testResourceContent.getBytes());
			}

			for (int i = 0; i < numElementsResourceList; i++) {
				String length = String.valueOf(testResourceContent.length() + (i / 10 + 1));

				header.setName("resourceList");
				header.setContentType("application/text");
				header.setFileName("file" + i);
				header.setContentLength(length);
				part = formDataWriter.openPart(header);

				try (OutputStream resOut = part.outputStream()) {
					resOut.write((testResourceContent + i).getBytes());
				}
			}
		}

		TestHttpResponse response = requests.servicePost(TestServiceRequestWithResources.T).accept("multipart/form-data")
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary).body(outputStream.toByteArray()).execute();

		assertThat(response.getStatusCode()).isEqualTo(200);

		Header contentDispisition = response.getResponse().getFirstHeader("Content-Type");
		HeaderElement contentDispisitionHeaderElement = contentDispisition.getElements()[0];
		String responseBoundary = contentDispisitionHeaderElement.getParameterByName("boundary").getValue();

		try (InputStream content = response.getResponse().getEntity().getContent(); //
				SequentialFormDataReader formDataReader = Multiparts.formDataReader(content, responseBoundary).sequential()) {
			PartReader part = formDataReader.next();

			assertEquals(RpcConstants.RPC_MAPKEY_RESPONSE, part.getName());

			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			TestServiceRequestWithResources unmarshalledResponse;
			try (InputStream in = part.openStream()) {
				unmarshalledResponse = (TestServiceRequestWithResources) marshaller.unmarshall(in);
				System.out.println(unmarshalledResponse);
			}

			if (request != null) {
				assertEquals(request.getValue(), unmarshalledResponse.getValue());
				assertEquals(unmarshalledResponse.getResource().getMimeType(), "test/test");
			} else {
				assertEquals(unmarshalledResponse.getResource().getMimeType(), "application/text");
			}

			assertThat(unmarshalledResponse.getEmbedded()).isNull();

			List<Resource> resourceList = unmarshalledResponse.getResourceList();
			assertEquals(numElementsResourceList, resourceList.size());

			assertEquals(unmarshalledResponse.getResource().getFileSize(), Long.valueOf(testResourceContent.length()));
			assertEquals(unmarshalledResponse.getResource().getName(), "test-file");

			// the resource
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals(unmarshalledResponse.getResource().getResourceSource().getGlobalId(), part.getName());

			if (request != null) {
				assertEquals(requestResource.getResourceSource().getGlobalId(), part.getName());
			}

			assertEquals(testResourceContent, part.getContentAsString());

			// the resource list
			int i = 0;
			for (Resource resource : resourceList) {
				part = formDataReader.next();
				String contentAsString = part.getContentAsString();
				assertNotNull(part);
				assertEquals(resource.getResourceSource().getGlobalId(), part.getName());
				assertEquals(testResourceContent + i, contentAsString);
				assertEquals(resource.getMimeType(), "application/text");
				assertEquals(resource.getFileSize(), Long.valueOf(contentAsString.length()));
				assertEquals(resource.getName(), "file" + i);
				i++;
			}

			// part announcement: just assert that it is there
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals("part-anouncement", part.getName());
			part.consume();

			part = formDataReader.next();
			assertNull(part);
		}
	}

	@Test
	public void testDirectResourceDownload() throws Exception {
		final String testResourceContent = "Resource Content";

		TestServiceRequestWithResources request = TestServiceRequestWithResources.T.create();
		request.setValue("MyValue");
		Resource requestResource = Resource.createTransient(null);
		request.setResource(requestResource);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, requestBoundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
			header.setContentType(JSON);
			PartWriter part = formDataWriter.openPart(header);
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			try (OutputStream marshallOut = part.outputStream()) {
				marshaller.marshall(marshallOut, request);
			}

			header.setName(requestResource.getResourceSource().getGlobalId());
			header.setContentType("application/text");
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(testResourceContent.getBytes());
			}
		}

		TestHttpResponse response = requests.servicePost(TestServiceRequest.T).accept("application/text")
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary).urlParameter("projection", "resource")
				.urlParameter("downloadResource", "true").urlParameter("responseContentType", "test/test").urlParameter("saveLocally", "true")
				.urlParameter("responseFilename", "test&test.txt").body(outputStream.toByteArray()).execute();

		for (Header header : response.getResponse().getAllHeaders()) {
			System.out.println(header.getName() + "..." + header.getValue());
		}

		assertEquals("test/test", response.getResponse().getFirstHeader("Content-Type").getValue());
		assertEquals("attachment; filename*=UTF-8''test&test.txt; filename=\"test_test.txt\"",
				response.getResponse().getFirstHeader("Content-Disposition").getValue());

		try (InputStream content = response.getResponse().getEntity().getContent()) {
			byte[] b = new byte[testResourceContent.length()];
			content.read(b);
			assertEquals(new String(b), testResourceContent);
		}
	}

	@Test
	public void testEmbeddedWithMultipart() throws Exception {
		TestServiceRequestWithResources request = TestServiceRequestWithResources.T.create();
		TestServiceRequestWithResources embeddedRequest = TestServiceRequestWithResources.T.create();
		request.setEmbedded(embeddedRequest);
		Resource requestResource = Resource.createTransient(null);
		Resource embeddedRequestResource = Resource.createTransient(null);
		request.setResource(requestResource);
		embeddedRequest.setResource(embeddedRequestResource);
		embeddedRequest.setValue("This will be replaced by part content");

		testEmbedded(request);
	}

	@Test
	public void testEmbeddedWithMultipartAndDefaultRequest() throws Exception {
		testEmbedded(null);
	}

	private void testEmbedded(TestServiceRequestWithResources request) throws IOException, Exception, MalformedMultipartDataException {
		final String testResourceContent = "Resource Content";
		final String embeddedResourceContent = "Resource Content Embedded";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, requestBoundary)) {
			MutablePartHeader header = Multiparts.newPartHeader();
			PartWriter part;

			if (request != null) {
				header.setName(RpcConstants.RPC_MAPKEY_REQUEST);
				header.setContentType(JSON);
				part = formDataWriter.openPart(header);
				JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

				try (OutputStream marshallOut = part.outputStream()) {
					marshaller.marshall(marshallOut, request);
				}
			}

			header.setName("embedded.value");
			header.setContentType(null);
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(TEST_VALUE.getBytes());
			}

			header.setName("embedded.resource");
			header.setContentType("test/test");
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(embeddedResourceContent.getBytes());
			}

			header.setName("resource");
			header.setContentType("test/test");
			part = formDataWriter.openPart(header);

			try (OutputStream resOut = part.outputStream()) {
				resOut.write(testResourceContent.getBytes());
			}

		}

		TestHttpResponse response = requests.servicePost(TestServiceRequestWithResources.T).accept("multipart/form-data")
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary).body(outputStream.toByteArray()).execute();

		Header contentDispisition = response.getResponse().getFirstHeader("Content-Type");
		HeaderElement contentDispisitionHeaderElement = contentDispisition.getElements()[0];
		String responseBoundary = contentDispisitionHeaderElement.getParameterByName("boundary").getValue();

		try (InputStream content = response.getResponse().getEntity().getContent(); //
				SequentialFormDataReader formDataReader = Multiparts.formDataReader(content, responseBoundary).sequential()) {
			PartReader part = formDataReader.next();

			assertEquals(RpcConstants.RPC_MAPKEY_RESPONSE, part.getName());

			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

			TestServiceRequestWithResources unmarshalledResponse;
			try (InputStream in = part.openStream()) {
				unmarshalledResponse = (TestServiceRequestWithResources) marshaller.unmarshall(in);
				System.out.println(unmarshalledResponse);
			}

			assertThat(unmarshalledResponse.getEmbedded()).isNotNull();
			assertThat(unmarshalledResponse.getEmbedded().getValue()).isEqualTo(TEST_VALUE);

			if (request != null) {
				assertEquals(request.getValue(), unmarshalledResponse.getValue());
			}
			assertEquals(unmarshalledResponse.getResource().getMimeType(), "test/test");

			assertEquals(unmarshalledResponse.getResource().getFileSize(), null);
			assertEquals(unmarshalledResponse.getResource().getName(), null);

			// the embedded resource
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals(unmarshalledResponse.getEmbedded().getResource().getResourceSource().getGlobalId(), part.getName());

			if (request != null) {
				Resource embeddedRequestResource = request.getEmbedded().getResource();
				assertEquals(embeddedRequestResource.getResourceSource().getGlobalId(), part.getName());
			}

			assertEquals(embeddedResourceContent, part.getContentAsString());

			// the resource
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals(unmarshalledResponse.getResource().getResourceSource().getGlobalId(), part.getName());

			if (request != null) {
				Resource requestResource = request.getResource();
				assertEquals(requestResource.getResourceSource().getGlobalId(), part.getName());
			}

			assertEquals(testResourceContent, part.getContentAsString());

			// part announcement: just assert that it is there
			part = formDataReader.next();
			assertNotNull(part);
			assertEquals("part-anouncement", part.getName());
			part.consume();

			part = formDataReader.next();
			assertNull(part);
		}
	}
}
