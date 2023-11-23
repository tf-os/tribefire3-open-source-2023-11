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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.ArrayTools;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartHeaders;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.streams.BasicDelegateOutputStream;
import com.braintribe.web.multipart.streams.ChunkedOutputStream;

public class ChunkedFormDataWriter extends AbstractFormDataWriter {

	private static final byte[] DOUBLE_DASH = "--".getBytes();
	
	public ChunkedFormDataWriter(OutputStream outputStream) {
		super(outputStream);
	}
	
	// TODO: Integrate a close handler in the class structure
	// TODO: Check for open parts before closing
	@Override
	public void close() throws Exception {
		out.write("--a0--\n".getBytes());
		out.flush();
	}
	
	public void writeProprietaryPartHeader(PartHeader header) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(header.getName());
		
		MutablePartHeaderImpl headerCopy = new MutablePartHeaderImpl(header);
		headerCopy.removeContentDispositionParameter("name");
		
		if (headerCopy.getFormDataContentDispositionParameterNames().isEmpty()) {
			headerCopy.setHeader(PartHeaders.CONTENT_DISPOSITION, null);
		}
		
		String otherHeaders = headerCopy.getHeaders()
			.flatMap(e -> e.getValue().stream().map(value -> toQueryParameter(e.getKey(), value)))
			.collect(Collectors.joining("&"));
		
		if (!otherHeaders.isEmpty()) {
			stringBuilder.append("?");
			stringBuilder.append(otherHeaders);
		}
		
		String headerString = stringBuilder.toString();
		
		String chunkSizeAsString = Integer.toHexString(headerString.length()).toUpperCase();
		byte chunkSizeSizeChar = (byte) (chunkSizeAsString.length() - 1 + (byte)'a');
		
		byte[] chunk = (byte[]) ArrayTools.merge(
				DOUBLE_DASH,
				new byte[] {chunkSizeSizeChar}, 
				chunkSizeAsString.getBytes(),
				DOUBLE_DASH,
				headerString.getBytes()
			);
		
		out.write(chunk);
	}

	private String toQueryParameter(String key, String value) {
		try {
			return key + "=" + URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.unchecked(e, "UTF-8 encoding not supported");
		}
	}
	
	@Override
	public PartWriter openPartImpl(PartHeader header) throws IOException {
		writeProprietaryPartHeader(header);

		if (header.getContentLength() == null) {
			return new ChunkedPartWriter(header);
		} else {
			return new DirectPartWriter(header);
		}
	}

	private class ChunkedPartWriter extends DelegatingPartHeader implements PartWriter {

		public ChunkedPartWriter(PartHeader delegate) {
			super(delegate);
		}

		@Override
		public OutputStream outputStream() {
			ChunkedOutputStream cos = ChunkedOutputStream.instance(out, IOTools.SIZE_64K, true);
			cos.setCloseHandler(internalOutputStream -> {
				freeCurrentPartWriter();
				try {
					internalOutputStream.write(LF);
				} catch (IOException e) {
					throw Exceptions.unchecked(e, "Could not write trailing linefeed after part end");
				}
			});

			return cos;
		}
	}
	
	protected class DirectPartWriter extends DelegatingPartHeader implements PartWriter {

		public DirectPartWriter(PartHeader delegate) {
			super(delegate);
			try {
				out.write(LF);
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Could not write initial linefeed to content length aware part");
			}
		}

		@Override
		public OutputStream outputStream() {
			return new BasicDelegateOutputStream(out) {
				private boolean closed;

				@Override
				public void close() throws IOException {
					if (!closed) {
						out.flush();
						freeCurrentPartWriter();
						out.write(LF);
						closed = true;
					}
				}
			};
		}

	}

	
}
