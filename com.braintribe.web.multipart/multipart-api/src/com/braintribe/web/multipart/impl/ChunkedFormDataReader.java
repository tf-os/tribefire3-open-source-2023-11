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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;

import com.braintribe.common.lcd.Pair;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.ReferencingFileInputStream;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartStreamingStatus;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.streams.BufferlessChunkedInputStream;
import com.braintribe.web.multipart.streams.ContentLengthAwareInputStream;
import com.braintribe.web.multipart.streams.Dechunker;

public class ChunkedFormDataReader implements FormDataMultipartConstants, SequentialFormDataReader {

	private final InputStream formDataInputStream;
	private final boolean autoCloseInput;
	private final Dechunker dechunker;

	private PartStreamingImpl lastPart;
	private boolean streamTerminated;

	ChunkedFormDataReader(InputStream in) {
		this(in, false);
	}
	
	ChunkedFormDataReader(InputStream in,  boolean autoCloseInput) {
		this.formDataInputStream = in;
		this.autoCloseInput = autoCloseInput;
		this.dechunker = new Dechunker(in, "--", "--");
	}

	private PartStreamingImpl parseHeaders() throws IOException {
		int chunkSize = dechunker.readChunkSize();
		byte[] headerChunk = dechunker.readChunkInMemory(chunkSize);
		
		if (headerChunk.length == 0) {
			return null;
		}
		
		String partHeaderString = new String(headerChunk, "UTF-8");

		Pair<String, String> nameAndHeaders = StringTools.splitDelimitedPair(partHeaderString, '?');
		
		String partName = nameAndHeaders.first();
		String headersAsStr = nameAndHeaders.second();
		
		final String[] headersAsStrings;
		
		if (headersAsStr != null) {
			headersAsStrings = StringTools.splitString(headersAsStr, "&");
		}
		else {
			headersAsStrings = new String[0];
		}

		if (partName.isEmpty()) {
			throw new IllegalStateException("Expected part name but there wasn't anything");
		}
		
		PartStreamingImpl partStreaming = new PartStreamingImpl();
		
		for (int i=0; i<headersAsStrings.length; i++) {
			String header = headersAsStrings[i];
			
			int index = header.indexOf('=');
			
			String name, value;
			if (index == -1) {
				name = header;
				value = null;
			}else {
				String encodedValue = header.substring(index + 1).trim();
				
				name = header.substring(0, index);
				value = URLDecoder.decode(encodedValue, "UTF-8");
			}
			
			partStreaming.addHeader(name, value);
		}
		
		partStreaming.setName(partName);
		
		return partStreaming;
	}

	@Override
	public PartReader next() throws IOException, MalformedMultipartDataException {
		if (streamTerminated == true) {
			return null;
		}
		
		if (lastPart == null || lastPart.getStatus() == PartStreamingStatus.consumed) {
			PartStreamingImpl partStreaming = parseHeaders();
			
			if (partStreaming == null) {
				streamTerminated = true;
				return null;
			}
			
			lastPart = partStreaming;
		} else {
			throw new IllegalStateException("Please consume currently open part before acquiring next(). Part status: " + lastPart.getStatus());
		}

		return lastPart;
	}

	private class PartStreamingImpl extends MutablePartHeaderImpl implements PartReader {
		private PartStreamingStatus status = PartStreamingStatus.initialized;
		private File backup;
		private InputStream openedIn;

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

		@Override
		public boolean isFile() {
			return getContentType() != null && getFileName() != null;
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

			if (getContentLength() != null) {
				int contentLength = Integer.parseInt(getContentLength());
				
				int openingLinefeed = formDataInputStream.read();
				if (openingLinefeed != '\n') {
					throw new IOException("Expected a linefeed before content length aware part but got: '" + openingLinefeed + "'.");
				}

				return new ContentLengthAwareInputStream(formDataInputStream, contentLength) {
					boolean isClosed;
					
					@Override
					public void close() throws IOException {
						if (isClosed)
							return;

						isClosed = true;
						IOTools.consume(this);
						
						super.close();
						
						int closingLinefeed = formDataInputStream.read();
						if (closingLinefeed != '\n') {
							throw new IOException("Expected a linefeed after content length aware part but got: '" + closingLinefeed);
						}
						status = PartStreamingStatus.consumed;
					}
				};
			}
			
			return new BufferlessChunkedInputStream(formDataInputStream) {
				boolean isClosed;
				
				@Override
				public void close() throws IOException {
					if (isClosed)
						return;

					isClosed = true;
					
					IOTools.consume(this);
					super.close();
					int closingLinefeed = formDataInputStream.read();
					if (closingLinefeed != '\n') {
						throw new IOException("Expected a linefeed after content length aware part but got: '" + closingLinefeed + "'.");
					}
					status = PartStreamingStatus.consumed;
				}
			};

		}

		@Deprecated
		@Override
		public InputStream openTransferEncodingAwareInputStream() throws IOException {
			return openStream();
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
		if (!streamTerminated) {
			if (lastPart != null) {
				lastPart.consume();
			}
	
			PartReader partReader = null;
			while ((partReader = next()) != null) {
				partReader.consume();
			}
			
			streamTerminated = true;
		}
		
		if (autoCloseInput)
			formDataInputStream.close();
	}

}
