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
package com.braintribe.codec.string;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

public class GzipCodec implements Codec<byte[],byte[]> {

	protected static Logger logger = Logger.getLogger(GzipCodec.class);
	
	@Override
	public byte[] encode(byte[] value) throws CodecException {
		if (value == null) {
			return null;
		}
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(value.length);
		GZIPOutputStream zipStream = null;
		try {
			zipStream = new GZIPOutputStream(byteStream);
			zipStream.write(value);
		} catch(Exception e) {
			throw new CodecException("Could not GZIP byte array.", e);
		} finally {
			IOTools.closeCloseable(zipStream, logger);
			IOTools.closeCloseable(byteStream, logger);
		}

		byte[] compressedData = byteStream.toByteArray();
		return compressedData;
	}

	@Override
	public byte[] decode(byte[] encodedValue) throws CodecException {
		if (encodedValue == null) {
			return null;
		}

		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(encodedValue);
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		GZIPInputStream unzipStream = null;
		
		try {
			unzipStream = new GZIPInputStream(byteInputStream);
			IOTools.pump(unzipStream, byteOutputStream);
			
		} catch(Exception e) {
			throw new CodecException("Could not GUNZIP byte array.", e);
		} finally {
			IOTools.closeCloseable(unzipStream, logger);
			IOTools.closeCloseable(byteInputStream, logger);
			IOTools.closeCloseable(byteOutputStream, logger);
		}

		byte[] uncompressedData = byteOutputStream.toByteArray();
		return uncompressedData;
	}

	@Override
	public Class<byte[]> getValueClass() {
		return byte[].class;
	}

}
