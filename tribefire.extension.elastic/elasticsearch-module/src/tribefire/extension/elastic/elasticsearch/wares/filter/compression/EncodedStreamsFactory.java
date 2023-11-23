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
/*
 *
 *  Copyright 2011 Rajendra Patil
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package tribefire.extension.elastic.elasticsearch.wares.filter.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import tribefire.extension.elastic.elasticsearch.wares.filter.common.Constants;

public abstract class EncodedStreamsFactory {

	private static final EncodedStreamsFactory GZIP_ENCODED_STREAMS_FACTORY = new GZIPEncodedStreamsFactory();

	private static final EncodedStreamsFactory ZIP_ENCODED_STREAMS_FACTORY = new ZIPEncodedStreamsFactory();

	private static final EncodedStreamsFactory DEFLATE_ENCODED_STREAMS_FACTORY = new DeflateEncodedStreamsFactory();

	public static final Map<String, EncodedStreamsFactory> SUPPORTED_ENCODINGS = EncodedStreamsFactory.getSupportedEncodingMap();

	private static Map<String, EncodedStreamsFactory> getSupportedEncodingMap() {

		if (SUPPORTED_ENCODINGS != null) {
			return SUPPORTED_ENCODINGS;
		}

		Map<String, EncodedStreamsFactory> map = new HashMap<>();
		map.put(Constants.CONTENT_ENCODING_GZIP, GZIP_ENCODED_STREAMS_FACTORY);
		map.put(Constants.CONTENT_ENCODING_COMPRESS, ZIP_ENCODED_STREAMS_FACTORY);
		map.put(Constants.CONTENT_ENCODING_DEFLATE, DEFLATE_ENCODED_STREAMS_FACTORY);
		return Collections.unmodifiableMap(map);
	}

	public static boolean isRequestContentEncodingSupported(String contentEncoding) {
		return SUPPORTED_ENCODINGS.containsKey(contentEncoding);
	}

	public static EncodedStreamsFactory getFactoryForContentEncoding(String contentEncoding) {
		return SUPPORTED_ENCODINGS.get(contentEncoding);
	}

	public abstract CompressedOutput getCompressedStream(OutputStream outputStream) throws IOException;

	public abstract CompressedInput getCompressedStream(InputStream inputStream) throws IOException;

}

class GZIPEncodedStreamsFactory extends EncodedStreamsFactory {

	@Override
	public CompressedOutput getCompressedStream(final OutputStream outputStream) throws IOException {
		return new CompressedOutput() {
			private final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

			@Override
			public OutputStream getCompressedOutputStream() {
				return gzipOutputStream;
			}

			@Override
			public void finish() throws IOException {
				gzipOutputStream.finish();
			}
		};
	}

	@Override
	public CompressedInput getCompressedStream(final InputStream inputStream) {

		return new CompressedInput() {

			@Override
			public InputStream getCompressedInputStream() throws IOException {
				return new GZIPInputStream(inputStream);
			}

		};
	}

}

class ZIPEncodedStreamsFactory extends EncodedStreamsFactory {

	@Override
	public CompressedOutput getCompressedStream(final OutputStream outputStream) throws IOException {
		return new CompressedOutput() {
			private final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

			boolean entryAdded = false;

			@Override
			public OutputStream getCompressedOutputStream() {
				if (!entryAdded) {
					try {
						ZipEntry entry = new ZipEntry("compressed-response.out");
						zipOutputStream.putNextEntry(entry);
						entryAdded = true;
					} catch (IOException ioe) {
						// ignore
					}
				}
				return zipOutputStream;
			}

			@Override
			public void finish() throws IOException {
				if (entryAdded) {
					zipOutputStream.closeEntry();
				}
				zipOutputStream.finish();
			}
		};
	}

	@Override
	public CompressedInput getCompressedStream(final InputStream inputStream) {

		return new CompressedInput() {

			@Override
			public InputStream getCompressedInputStream() throws IOException {
				return new ZipInputStream(inputStream);
			}

		};
	}

}

class DeflateEncodedStreamsFactory extends EncodedStreamsFactory {

	@Override
	public CompressedOutput getCompressedStream(final OutputStream outputStream) {
		return new CompressedOutput() {
			private final DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream);

			@Override
			public OutputStream getCompressedOutputStream() {
				return deflaterOutputStream;
			}

			@Override
			public void finish() throws IOException {
				deflaterOutputStream.finish();
			}
		};
	}

	@Override
	public CompressedInput getCompressedStream(final InputStream inputStream) {

		return new CompressedInput() {

			@Override
			public InputStream getCompressedInputStream() throws IOException {
				return new DeflaterInputStream(inputStream);
			}

		};
	}

}
