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
package com.braintribe.crypto.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import com.braintribe.crypto.base64.Base64;

public class Compressor {
	
	public static String compress( String in) throws IOException {
		byte [] data = in.getBytes();
		byte [] compressedData = compress( data);
		return Base64.encodeBytes(compressedData);
	}
	
	public static byte [] compress( byte [] data) throws IOException {
				
		Deflater deflater = new Deflater();
		deflater.setLevel( Deflater.BEST_COMPRESSION);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		deflaterOutputStream.write( data);
		deflaterOutputStream.flush();
		deflaterOutputStream.close();
		byte [] compressedData = byteArrayOutputStream.toByteArray();
		return compressedData;
		
	}
	public static String decompress( String in) throws IOException {
		byte [] data = Base64.decode( in);
		byte [] uncompressedData = decompress( data);
		return new String( uncompressedData);
	}
	
	public static byte[] decompress( byte [] data) throws IOException {
	
		Inflater inflater = new Inflater();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
		InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream, inflater);
		inflaterOutputStream.write( data);
		inflaterOutputStream.flush();
		inflaterOutputStream.close();		
		byte [] uncompressedData = byteArrayOutputStream.toByteArray();
		return uncompressedData;
		
	}
}
