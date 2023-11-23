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
package com.braintribe.web.multipart.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.ReferencingFileInputStream;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartStreamingStatus;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.streams.AbstractPartInputStream;
import com.braintribe.web.multipart.streams.BoundaryAwareInputStream;
import com.braintribe.web.multipart.streams.ChunkedInputStream;
import com.braintribe.web.multipart.streams.ContentLengthAwareInputStream;
import com.braintribe.web.multipart.streams.ExtendedBufferedInputStream;

public class SequentialFormDataReaderImpl implements FormDataMultipartConstants, SequentialFormDataReader {

	private PartStreamingImpl lastPart;

	private final InputStream formDataInputStream;
	private InputStream frankensteinInputStream;

	// When a part input stream has remaining buffer, this needs to be combined with the
	// formDataInputStream delegate to get a full formDataInputStream. Still we need to
	// be able to reference the parts to avoid too deep structures
	private ByteArrayInputStream partBufferOverhead;

	private byte[] customBoundaryPart;
	private byte[] partBoundary;
	private int lastBoundary;

	private boolean autoCloseInput;

	SequentialFormDataReaderImpl(InputStream in, byte[] customBoundaryPart) {
		this(in, customBoundaryPart, false);
	}

	SequentialFormDataReaderImpl(InputStream in, byte[] customBoundaryPart, boolean autoCloseInput) {
		formDataInputStream = in;
		frankensteinInputStream = in;
		this.autoCloseInput = autoCloseInput;
		partBufferOverhead = new ByteArrayInputStream(new byte[0]);
		this.customBoundaryPart = customBoundaryPart;
		this.partBoundary = mergeArrays("--".getBytes(), customBoundaryPart);

	}

	private static byte readByte(InputStream in) throws IOException {
		int res = in.read();

		if (res == -1) {
			throw new EOFException("unexpected end of file in a multipart body");
		}
		return (byte) res;
	}

	private void expectBoundary() {
		expect(partBoundary, "boundary");
	}

	private void expectNewline() {
		expect(HTTP_LINEBREAK, "newline");
	}

	private void expect(byte[] expected, String description) {
		try {

			// byte[] CRLFplusBoundary = mergeArrays("\r\n--".getBytes(), customBoundaryPart);
			byte[] expectedBytesBuffer = new byte[expected.length];

			// TODO: Possible to remove readFully?
			// TODO: Make sure the full amount of bytes is read
			IOTools.readFully(frankensteinInputStream, expectedBytesBuffer);

			if (!Arrays.equals(expectedBytesBuffer, expected)) {
				String expectedAsString = new String(expected);
				throw new MalformedMultipartDataException(
						"Expected " + description + " '" + expectedAsString + "' but got '" + new String(expectedBytesBuffer) + "'");
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Map<String, List<String>> parseHeaders() throws IOException {
		Map<String, List<String>> headers = new TreeMap<String, List<String>>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		String line;

		ExtendedBufferedInputStream bufIn = new ExtendedBufferedInputStream(frankensteinInputStream, 256);

		while ((line = readLine(bufIn)).length() != 0) {
			int index = line.indexOf(':');
			if (index == -1) {
				throw new MalformedMultipartDataException("missing : in header");
			}
			String name = line.substring(0, index).toLowerCase();
			String value = line.substring(index + 1).trim();

			List<String> values = headers.get(name);
			if (values == null) {
				values = new ArrayList<String>();
				headers.put(name, values);
			}
			values.add(value);
		}

		fetchBufferOverhead(bufIn);

		return headers;
	}

	private static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while (true) {
				byte b = readByte(in);

				if (b != CR) {
					bos.write(b);
				} else {
					b = readByte(in);

					if (b == LF) {
						bos.close();
						return new String(bos.toByteArray(), "UTF-8");
					} else {
						bos.write(CR);
						bos.write(b);
					}
				}
			}
		} catch (EOFException e) {
			throw new MalformedMultipartDataException("incomplete header", e);
		}
	}

	private void determineBoundaryType() throws IOException {
		byte[] boundaryTypeBuffer = new byte[2];
		IOTools.readFully(frankensteinInputStream, boundaryTypeBuffer);

		byte[] terminalBoundary = "--".getBytes();

		if (Arrays.equals(boundaryTypeBuffer, HTTP_LINEBREAK)) {
			lastBoundary = BOUNDARY_TYPE_PART;
		} else if (Arrays.equals(boundaryTypeBuffer, terminalBoundary)) {
			lastBoundary = BOUNDARY_TYPE_TERMINAL;

			byte[] expectedCRLFBuffer = new byte[HTTP_LINEBREAK.length];
			IOTools.readFully(frankensteinInputStream, expectedCRLFBuffer);

			if (!Arrays.equals(expectedCRLFBuffer, HTTP_LINEBREAK)) {
				throw new MalformedMultipartDataException("Missing CRLF after terminal boundary");
			}

		} else {
			throw new MalformedMultipartDataException(
					"After a boundary must be either an HTTP linebreak or two dashes but got: '" + Arrays.toString(boundaryTypeBuffer) + "'.");
		}
	}

	@Override
	public PartReader next() throws IOException, MalformedMultipartDataException {
		if (lastBoundary == BOUNDARY_TYPE_TERMINAL) {
			return null;
		}

		if (lastPart == null) {
			expectBoundary();
		}

		if (lastPart == null || lastPart.getStatus() == PartStreamingStatus.consumed) {
			determineBoundaryType();

			if (lastBoundary == BOUNDARY_TYPE_TERMINAL) {
				return null;
			}

			lastPart = new PartStreamingImpl(parseHeaders());
		} else {
			throw new IllegalStateException("Please consume currently open part before acquiring next()");
		}

		return lastPart;
	}

	private byte[] mergeArrays(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public void fetchBufferOverhead(RemainingBufferAccess remainingBufferAccess) {
		byte[] unreadBuffer = remainingBufferAccess.getUnreadBuffer();

		if (unreadBuffer == null || unreadBuffer.length == 0)
			return;

		int oldBufferLength = partBufferOverhead.available();

		// TODO: special case when no buffer overhead left

		byte[] unreadPreviousBufferOverhead = new byte[oldBufferLength];
		try {
			partBufferOverhead.read(unreadPreviousBufferOverhead);
		} catch (IOException e) {
			throw new RuntimeException("ByteArrayInputStream threw IOException which was considered impossible.");
		}

		byte[] combinedBufferOverhead = mergeArrays(unreadBuffer, unreadPreviousBufferOverhead);

		partBufferOverhead = new ByteArrayInputStream(combinedBufferOverhead);

		frankensteinInputStream = new SequenceInputStream(partBufferOverhead, formDataInputStream);
	}

	private class PartStreamingImpl implements PartReader {
		private Map<String, List<String>> headers;
		private Map<String, String> formDataContentDispositionParameters;
		private PartStreamingStatus status = PartStreamingStatus.initialized;
		private File backup;
		private InputStream openedIn;

		public PartStreamingImpl(Map<String, List<String>> headers) {
			this.headers = headers;
		}

		@Override
		public Set<String> getHeaderNames() {
			return headers.keySet();
		}

		@Override
		public String getHeader(String name) {
			Collection<String> values = getHeaders(name);
			return values.size() > 0 ? values.iterator().next() : null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			List<String> values = headers.get(name.toLowerCase());
			return values != null ? values : Collections.<String> emptyList();
		}

		@Override
		public Stream<Entry<String, List<String>>> getHeaders() {
			return headers.entrySet().stream();
		}

		@Override
		public String getContentAsString() throws IOException {
			return getContentAsString("UTF-8");
		}

		@Override
		public String getContentAsString(String charset) throws IOException {
			InputStreamReader reader = new InputStreamReader(openStream(), charset);
			int res = 0;
			StringBuilder builder = new StringBuilder();
			while ((res = reader.read()) != -1) {
				builder.append((char) res);
			}

			reader.close();
			return builder.toString();
		}

		@Override
		public void consume() throws IOException {
			switch (status) {
				case opened:
					openedIn.close();
					break;
				case initialized:
					openStream().close();
					break;
				default:
					break;
			}
		}

		private void prepareInputStream() {
			switch (status) {
				case opened:
					throw new IllegalStateException("input stream was opened but not yet consumed in a potential backup");
				case consumed:
					throw new IllegalStateException("input stream was already consumed but not backed up");
				case initialized:
					status = PartStreamingStatus.opened;
					break;
				default:
					throw new IllegalStateException("unsupported status " + status);
			}
		}

		private InputStream openRawInputStream() {
			BoundaryAwareInputStream boundaryAwareInputStream = new BoundaryAwareInputStream(frankensteinInputStream, customBoundaryPart);

			boundaryAwareInputStream.setCloseHandler(itself -> {
				status = PartStreamingStatus.consumed;
				openedIn = null;
				boundaryAwareInputStream.setCloseHandler(null);

				fetchBufferOverhead(itself);
				// expectNewline();
			});

			openedIn = boundaryAwareInputStream;

			return openedIn;

		}

		@Override
		public boolean isFile() {
			return getContentType() != null && getFileName() != null;
		}

		private InputStream openNonBoundaryAwareInputStream(Supplier<AbstractPartInputStream> supplier) {

			AbstractPartInputStream inputStream = supplier.get();

			inputStream.setCloseHandler(itself -> {
				status = PartStreamingStatus.consumed;
				openedIn = null;

				fetchBufferOverhead(itself);

				expectNewline();
				expectBoundary();
			});

			openedIn = inputStream;
			return openedIn;
		}

		@Override
		public InputStream openStream() throws IOException {
			if (isBackedUp()) {
				// Using a ReferencingFileInputStream to ensure that we keep a reference to the file so that
				// it won't get deleted before the inputstream is closed; see use of FileTools.deleteWhenOrphaned down
				// below
				return new ReferencingFileInputStream(backup);
			}

			prepareInputStream();

			if (getTransferEncoding() != null && getTransferEncoding().equals("chunked")) {
				if (getContentLength() != null)
					throw new MalformedMultipartDataException("Transfer-Encoding=chunked and Content-Length were both set in part header");

				return openNonBoundaryAwareInputStream(() -> new ChunkedInputStream(frankensteinInputStream));
			} else if (getContentLength() != null) {
				int contentLength = Integer.parseInt(getContentLength());

				return openNonBoundaryAwareInputStream(() -> new ContentLengthAwareInputStream(frankensteinInputStream, contentLength));
			} else {
				return openRawInputStream();
			}
		}

		@Deprecated
		@Override
		public InputStream openTransferEncodingAwareInputStream() throws IOException {
			return openStream();
		}

		@Override
		public String getName() {
			return getFormDataContentDispositionParameter("name");
		}

		@Override
		public String getFileName() {
			return getFormDataContentDispositionParameter("filename");
		}

		@Override
		public String getTransferEncoding() {
			return getHeader("Transfer-Encoding");
		}

		@Override
		public String getContentType() {
			return getHeader("Content-Type");
		}

		private Map<String, String> getFormDataContentDispositionParameters() {
			if (formDataContentDispositionParameters == null) {
				formDataContentDispositionParameters = new HashMap<String, String>();

				String contentDisposition = getFormDataContentDisposition();

				if (contentDisposition == null) {
					formDataContentDispositionParameters = Collections.emptyMap();
				} else {
					formDataContentDispositionParameters = parseContentDisposition(contentDisposition);
				}
			}

			return formDataContentDispositionParameters;
		}

		private Map<String, String> parseContentDisposition(String contentDisposition) {
			String parts[] = contentDisposition.split(";");
			Map<String, String> parameters = new HashMap<String, String>();
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i].trim();
				int index = part.indexOf("=");

				if (index != -1) {
					String name = part.substring(0, index);
					String value = part.substring(index + 1);

					if (value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 1);
					}
					parameters.put(name, value);
				} else {
					parameters.put(part, null);
				}

			}
			return parameters;
		}

		@Override
		public String getFormDataContentDispositionParameter(String name) {
			return getFormDataContentDispositionParameters().get(name);
		}

		@Override
		public String getFormDataContentDisposition() {
			Collection<String> contentDispositions = getHeaders("Content-Disposition");

			for (String contentDisposition : contentDispositions) {
				if (contentDisposition.startsWith("form-data")) {
					return contentDisposition;
				}
			}
			return null;
		}

		@Override
		public Set<String> getFormDataContentDispositionParameterNames() {
			return getFormDataContentDispositionParameters().keySet();
		}

		@Override
		public boolean isConsumed() {
			return status == PartStreamingStatus.consumed;
		}

		@Override
		public boolean isBackedUp() {
			return backup != null;
		}

		@Override
		public void backup() throws IOException {
			if (backup == null) {
				File file = File.createTempFile("RandomAccessFormData", getName());
				FileTools.deleteFileWhenOrphaned(file);
				try (OutputStream out = new FileOutputStream(file); InputStream in = openStream()) {

					IOTools.pump(in, out);
				}
				backup = file;
			}
		}

		@Override
		public PartStreamingStatus getStatus() {
			return status;
		}

	}

	@Override
	public void close() throws Exception {
		if (lastPart != null) {
			lastPart.consume();
		}

		PartReader partReader = null;
		while ((partReader = next()) != null) {
			partReader.consume();
		}

		if (autoCloseInput)
			formDataInputStream.close();
	}

}
