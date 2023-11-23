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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.braintribe.web.multipart.api.FormDataMultipartsBuilder;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.web.multipart.api.MultipartFormat;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.RandomAccessFormDataReader;
import com.braintribe.web.multipart.api.SequentialFormDataReader;

public class Multiparts {
	
	private final static String MULTIPART_PREFIX = "multipart/";
	
	public static MultipartSubFormat getMultipartSubFormat(String contentType) {
		if (contentType.startsWith(MULTIPART_PREFIX)) {
			int index = contentType.indexOf(';');
			String subtype = index != -1? contentType.substring(MULTIPART_PREFIX.length(), index): contentType.substring(MULTIPART_PREFIX.length());
			switch (subtype) {
				case "form-data":
					return MultipartSubFormat.formData;
				case "chunked":
					return MultipartSubFormat.chunked;
				default:
					return MultipartSubFormat.none;
			}
		}
		return MultipartSubFormat.none;
	}
	
	public static MultipartFormat parseFormat(String mimeType) {
		return MultipartMimetypeParser.parse(mimeType);
	}

	public static boolean isFormDataMultipartsContentType(String contentType) {
		return contentType != null && contentType.startsWith("multipart/form-data;");
	}

	public static SequentialFormDataReader formDataReader(InputStream in) {
		return new ChunkedFormDataReader(in);
	}
	
	public static FormDataMultipartsBuilder formDataReader(InputStream in, String boundary) {
		if (in == null) {
			throw new IllegalArgumentException("Can't create FormDataReader: provided InputStream must not be null.");
		}
		if (boundary == null) {
			throw new IllegalArgumentException("Can't create FormDataReader: provided boundary must not be null.");
		}
		
		byte[] boundaryAsBytes;
		try {
			boundaryAsBytes = boundary.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("essential charset ISO-8859-1 missing");
		}
		return formDataReader(in, boundaryAsBytes);
	}

	public static String generateBoundary() {
		return "boundary-" + UUID.randomUUID().toString();
	}

	public static byte[] boundaryAsBytes(String boundary) {
		try {
			return boundary.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void main(String[] args) {
		try {
			System.out.println(generateBoundary());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FormDataWriter formDataWriter(OutputStream output) {
		return formDataWriter(output, generateBoundary());
	}

	public static FormDataWriter formDataWriter(OutputStream out, String boundary) {
		return new FormDataWriterImpl(out, boundary);
	}
	
	public static FormDataWriter blobFormDataWriter(OutputStream output) {
		return blobFormDataWriter(output, generateBoundary());
	}
	
	public static FormDataWriter blobFormDataWriter(OutputStream out, String boundary) {
		return SliceSupportingFormDataWriter.create(out, boundary);
	}
	
	public static FormDataWriter chunkedFormDataWriter(OutputStream out) {
		return new ChunkedFormDataWriter(out);
	}

	public static FormDataWriter formDataWriter(OutputStream out, MultipartSubFormat format) {
		return new ChunkedFormDataWriter(out);
	}
	
	public static FormDataMultipartsBuilder formDataReader(final InputStream in, final byte[] boundary) {
		return buildFormDataReader(in).boundary(boundary);
	}
	
	public static FormDataMultipartsBuilder buildFormDataReader(final InputStream in) {
		return new FormDataMultipartsBuilder() {
			
			private boolean autoCloseInput;
			private MultipartSubFormat subFormat = MultipartSubFormat.formData;
			private byte[] boundary;
			
			@Override
			public FormDataMultipartsBuilder autoCloseInput() {
				this.autoCloseInput = true;
				return this;
			}
			
			@Override
			public FormDataMultipartsBuilder boundary(String boundary) {
				if (boundary != null)
					this.boundary = boundaryAsBytes(boundary);
				else
					this.boundary = null;
				
				return this;
			}
			
			@Override
			public FormDataMultipartsBuilder boundary(byte[] boundary) {
				this.boundary = boundary;
				return this;
			}
			
			@Override
			public FormDataMultipartsBuilder contentType(String contentType) {
				subFormat = getMultipartSubFormat(contentType);
				
				if (boundary == null) {
					switch (subFormat) {
						case formData:
						case sliceableFormData:
							this.boundary = extractBoundaryFromContentType(contentType);
						default:
							break;
					}
				}
				
				return this;
			}
			
			@Override
			public FormDataMultipartsBuilder subFormat(MultipartSubFormat subFormat) {
				this.subFormat = subFormat;
				return this;
			}
			
			@Override
			public SequentialFormDataReader sequential() throws MalformedMultipartDataException, IOException {
				switch (subFormat) {
					case formData:
						byte[] boundary = this.boundary;
						
						if (boundary == null)
							boundary = boundaryAsBytes(generateBoundary());
						
						return new SequentialFormDataReaderImpl(in, boundary, autoCloseInput);
					case chunked:
					case sliceableFormData:
						return new ChunkedFormDataReader(in, autoCloseInput);
					default:
						throw new UnsupportedOperationException("unsupported multipart sub format: " + subFormat);
				}
			}
			
			@Override
			public RandomAccessFormDataReader randomAccess() throws MalformedMultipartDataException, IOException {
				return new RandomAccessFormDataReaderImpl(in, boundary, autoCloseInput);
			}
		};
	}

	/**
	 * Retrieves the boundary from the <code>Content-type</code> header.
	 *
	 * @param contentType
	 *            The value of the content type header from which to extract the boundary value.
	 *
	 * @return The boundary, as a byte array.
	 */
	public static byte[] extractBoundaryFromContentType(String contentType) {
		String parts[] = contentType.split("(,|;)");
		Map<String, String> params = new HashMap<>();
		for (String part : parts) {
			part = part.trim();
			int index = part.indexOf('=');
			if (index != -1) {
				String key = part.substring(0, index);
				String value = part.substring(index + 1);
				params.put(key, value);
			}
		}

		String boundaryStr = params.get("boundary");

		if (boundaryStr == null) {
			return null;
		}
		byte[] boundary;
		try {
			boundary = boundaryStr.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			boundary = boundaryStr.getBytes();
		}
		return boundary;

	}

	public static MutablePartHeader newPartHeader() {
		return new MutablePartHeaderImpl();
	}
}
