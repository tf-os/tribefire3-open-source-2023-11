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
package com.braintribe.crypto.key.codec;

import java.security.Key;

import javax.crypto.Cipher;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;

/**
 * de- or en-crypts bytes
 * 
 * if you're using RSA cipher, note the following:
 * 	encode - you need to specify the private key 
 *  decode - you need to specify the public key 
 * 
 * @author pit
 *
 */
public class CryptoBytesCodec implements Codec<byte[], byte[]> {
	
	private Logger log = Logger.getLogger( CryptoBytesCodec.class);

	private Key key = null;
	private String cipherName = null;	
	
	public void setKey(Key key) {
		this.key = key;
	}

	public void setCipher(String cipher) {
		this.cipherName = cipher;
	}

	@Override
	public byte[] encode(byte[] value) throws CodecException {
		try {
			Cipher cipher = Cipher.getInstance( cipherName);
			cipher.init(Cipher.ENCRYPT_MODE, key);			
			return cipher.doFinal(value);			
		} catch (Exception e) {
			String msg = "cannot encrypt message as " + e; 
			log.error( msg, e);
			throw new CodecException( msg, e);
		} 		
	}

	@Override
	public byte[] decode(byte[] encodedValue) throws CodecException {
		try {
			Cipher cipher = Cipher.getInstance( cipherName);
			cipher.init(Cipher.DECRYPT_MODE, key);			
			return cipher.doFinal( encodedValue);			
		} catch (Exception e) {
			String msg = "cannot encrypt message as " + e; 
			log.error( msg, e);
			throw new CodecException( msg, e);
		} 		
	}

	@Override
	public Class<byte[]> getValueClass() {

		return byte[].class;
	}

}
