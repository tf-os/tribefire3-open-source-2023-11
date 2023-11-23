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
package com.braintribe.crypto.cipher;

import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.crypto.BidiCryptor;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;

/**
 * <p>
 * A {@link BidiCryptor} based on {@link javax.crypto.Cipher}(s).
 * 
 */
public class BidiCipherCryptor implements CipherCryptor, BidiCryptor {

	private Encryptor encryptor;
	private Decryptor decryptor;

	public BidiCipherCryptor(Encryptor encryptor, Decryptor decryptor) {
		this.encryptor = encryptor;
		this.decryptor = decryptor;
	}

	@Override
	public Encryptor forEncrypting() {
		return encryptor;
	}

	@Override
	public Decryptor forDecrypting() {
		return decryptor;
	}

	@Override
	public Encryptor.Processor encrypt(byte[] input) throws CryptorException {
		return encryptor.encrypt(input);
	}

	@Override
	public Encryptor.StringProcessor encrypt(String input) throws CryptorException {
		return encryptor.encrypt(input);
	}

	@Override
	public OutputStream wrap(OutputStream outputStream) throws CryptorException {
		return encryptor.wrap(outputStream);
	}

	@Override
	public Cryptor.Matcher is(byte[] input) throws CryptorException {
		try {
			return encryptor.is(input);
		} catch (UnsupportedOperationException e) {
			return decryptor.is(input);
		}
	}

	@Override
	public Cryptor.StringMatcher is(String input) throws CryptorException {
		try {
			return encryptor.is(input);
		} catch (UnsupportedOperationException e) {
			return decryptor.is(input);
		}
	}

	@Override
	public boolean isDeterministic() {
		return encryptor.isDeterministic();
	}

	@Override
	public Decryptor.Processor decrypt(byte[] input) throws CryptorException {
		return decryptor.decrypt(input);
	}

	@Override
	public Decryptor.StringProcessor decrypt(String input) throws CryptorException {
		return decryptor.decrypt(input);
	}

	@Override
	public InputStream wrap(InputStream inputStream) throws CryptorException {
		return decryptor.wrap(inputStream);
	}

}
