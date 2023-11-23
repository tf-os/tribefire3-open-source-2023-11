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
package com.braintribe.crypto.hash.md5;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.hash.HashGenerator;
import com.braintribe.crypto.utils.TextUtils;
 
/**
 * generates an MD5 hash from a string/bytes/object given.
 * @author pit
 *
 */
public class MD5HashGenerator extends HashGenerator{
 
	
 
	/**
	 * builds an MD5 hash 
	 * @param text - the text to create a hash from  
	 * @return - the hash.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String MD5(String text, String encoding) throws CryptoServiceException  {
		
			try {
				if (encoding != null)
					return MD5(text.getBytes( encoding));
				else
					return MD5(text.getBytes());
			} catch (UnsupportedEncodingException e) {
				throw new CryptoServiceException( e);
			}
		
	}
	
	public static String MD5(byte [] bytes) throws CryptoServiceException  {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance( "MD5");
			byte[] md5hash = new byte[32];
			md.update( bytes, 0, bytes.length);
			md5hash = md.digest();
			return TextUtils.convertToHex( md5hash);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException(e);
		}
	}
	
	public static String MD5( Serializable obj) throws CryptoServiceException {	
		byte[] bytes = convertObjectToBytes( obj);
		return MD5( bytes);		
	}
	
	public static void main( String [] args) {
		for (String name : args) {
			File file = new File( name);
			if (file.exists() == false)
				continue;
			byte [] bytes = TextUtils.getFileBytes(file);
			try {
				String hash = MD5( bytes);
				System.out.println( "File [" + name + "]'s md5 hash is [" + hash + "]");
			} catch (CryptoServiceException e) {
				String msg ="cannot hash [" + name + "] as " + e;
				System.err.println( msg);
			}
		}
	}
}

