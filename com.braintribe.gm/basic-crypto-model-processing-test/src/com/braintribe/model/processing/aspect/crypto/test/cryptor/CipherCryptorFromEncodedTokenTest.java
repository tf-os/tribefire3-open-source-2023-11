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
package com.braintribe.model.processing.aspect.crypto.test.cryptor;

import org.junit.Test;

import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.crypto.Cryptor;

public class CipherCryptorFromEncodedTokenTest extends TestBase {

	// ########################
	// ## .. AES tests ..... ##
	// ########################

	// hexadecimal encoded key

	@Test
	public void testHexadecimalEncodedAesKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "AES", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedAesKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "AES", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedAesKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "AES", false, TestDataProvider.inputA);
	}

	// base64 encoded key

	@Test
	public void testBase64EncodedAesKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "AES", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedAesKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "AES", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedAesKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "AES", false, TestDataProvider.inputA);
	}

	// ########################
	// ## .. DES tests ..... ##
	// ########################

	// hexadecimal encoded key

	@Test
	public void testHexadecimalEncodedDesKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DES", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedDesKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DES", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedDesKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "DES", false, TestDataProvider.inputA);
	}

	// base64 encoded key

	@Test
	public void testBase64EncodedDesKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DES", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedDesKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DES", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedDesKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "DES", false, TestDataProvider.inputA);
	}

	// ########################
	// ## .. DESede tests .. ##
	// ########################

	// hexadecimal encoded key

	@Test
	public void testHexadecimalEncodedDesEdeKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DESede", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedDesEdeKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DESede", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedDesEdeKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "DESede", false, TestDataProvider.inputA);
	}

	// base64 encoded key

	@Test
	public void testBase64EncodedDesEdeKeyEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DESede", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedDesEdeKeyEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "DESede", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedDesEdeKeyEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "DESede", false, TestDataProvider.inputA);
	}
	
	// ########################
	// ## .. RSA tests ..... ##
	// ########################

	// hexadecimal encoded key

	@Test
	public void testHexadecimalEncodedRsaKeyPairEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "RSA", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedRsaKeyPairEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "RSA", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testHexadecimalEncodedRsaKeyPairEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","hex");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "RSA", false, TestDataProvider.inputA);
	}

	// base64 encoded key

	@Test
	public void testBase64EncodedRsaKeyPairEncryptingHexadecimalString() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "RSA", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedRsaKeyPairEncryptingBase64String() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testStringCrypting(cipherCryptor, "RSA", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testBase64EncodedRsaKeyPairEncryptingByteArray() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA","base64");
		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);
		testBytesCrypting(cipherCryptor, "RSA", false, TestDataProvider.inputA);
	}

}
