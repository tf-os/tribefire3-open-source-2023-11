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
package com.braintribe.crypto.hash.sha1;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.hash.HashGenerator;
import com.braintribe.crypto.utils.TextUtils;

public class Sha1HashGenerator extends HashGenerator {
	
	public static String SHA1( String text, String encoding) throws CryptoServiceException  {
		try {
			if (encoding != null)
				return SHA1( text.getBytes( encoding));
			else
				return SHA1( text.getBytes());
		} catch (UnsupportedEncodingException e) {
			throw new CryptoServiceException( e);
		}
	}
	
	public static String SHA1(byte [] bytes) throws CryptoServiceException  {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance( "SHA1");
			byte[] sha1hash = new byte[40];
			md.update(bytes, 0, bytes.length);
			sha1hash = md.digest();
			return TextUtils.convertToHex( sha1hash);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( e);
		}
	}
	
	public static String SHA1( Serializable object) throws CryptoServiceException {
		byte [] bytes = convertObjectToBytes(object);
		return SHA1( bytes);
	}
}
