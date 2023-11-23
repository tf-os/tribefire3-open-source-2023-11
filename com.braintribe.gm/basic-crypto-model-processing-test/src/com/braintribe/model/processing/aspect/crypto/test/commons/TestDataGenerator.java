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
package com.braintribe.model.processing.aspect.crypto.test.commons;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.braintribe.crypto.commons.Base64Codec;
import com.braintribe.crypto.commons.HexCodec;
import com.braintribe.utils.IOTools;

/**
 * 
 * 
 * <p>Key store generation commands example:
 * 
 * <pre>
 * rem deleting previous stores
 * del test-keystore.jks
 * del test-keystore.jceks
 * 
 * rem generating JKS type: test-keystore.jks
 * "%JAVA_HOME%/bin/keytool" -genkeypair -alias RSA-entry -keyalg RSA -keysize 2048 -sigalg MD5withRSA -dname "cn=John Smith, ou=R&D, o=Braintribe, c=AT" -keypass RSA-entry-pwd -validity 7300 -storetype jks -keystore test-keystore.jks -storepass test-keystore-pwd -v
 * 
 * rem generating JCEKS type: test-keystore.jceks
 * "%JAVA_HOME%/bin/keytool" -genseckey  -alias AES-entry -keyalg AES -keysize 128 -keypass AES-entry-pwd -storetype jceks -keystore test-keystore.jceks -storepass test-keystore-pwd -v
 * "%JAVA_HOME%/bin/keytool" -genseckey  -alias DES-entry -keyalg DES -keysize 56 -keypass DES-entry-pwd -storetype jceks -keystore test-keystore.jceks -storepass test-keystore-pwd -v
 * "%JAVA_HOME%/bin/keytool" -genseckey  -alias DESede-entry -keyalg DESede -keysize 168 -keypass DESede-entry-pwd -storetype jceks -keystore test-keystore.jceks -storepass test-keystore-pwd -v
 * </pre>
 * 
 *
 */
public class TestDataGenerator {
	
	private static Base64Codec base64Codec = Base64Codec.INSTANCE;
	private static HexCodec hexCodec = new HexCodec();

	public static final String valueInputA = "TEST-INPUT";
	public static final String valueInputB = "TEST-INPUT.";
	public static final String defaultSalt = "SALT";

	public static final Map<String, KeyStoreSymmetricEntry> symmetricKeys = new HashMap<>();
	public static final Map<String, KeyStoreAsymmetricEntry> asymmetricKeys = new HashMap<>();
	public static final Map<String, String> defaultTransformations = new HashMap<>();
	
	static {
		
		symmetricKeys.put("AES", new KeyStoreSymmetricEntry("AES-entry", "AES", "AES-entry-pwd", "jceks", "./res/keystores/test-keystore.jceks", "test-keystore-pwd"));
		symmetricKeys.put("DES", new KeyStoreSymmetricEntry("DES-entry", "DES", "DES-entry-pwd", "jceks", "./res/keystores/test-keystore.jceks", "test-keystore-pwd"));
		symmetricKeys.put("DESede", new KeyStoreSymmetricEntry("DESede-entry", "DESede", "DESede-entry-pwd", "jceks", "./res/keystores/test-keystore.jceks", "test-keystore-pwd"));
		
		asymmetricKeys.put("RSA", new KeyStoreAsymmetricEntry("RSA-entry", "RSA", "RSA-entry-pwd", "jks", "./res/keystores/test-keystore.jks", "test-keystore-pwd"));
		
		defaultTransformations.put("AES", "AES/ECB/PKCS5Padding");
		defaultTransformations.put("DES", "DES/ECB/PKCS5Padding");
		defaultTransformations.put("DESede", "DESede/ECB/PKCS5Padding");
		defaultTransformations.put("RSA", "RSA/ECB/PKCS1Padding");
		
	}
	
	public static void main(String[] args) throws Exception {

		String[] inputFormats = new String[] { "hex", "base64", "bytes" };
		String[] hashingAlgorithms = new String[] { "MD5", "SHA-1", "SHA-256" };
		String[] keyStringEncodings = new String[] { "hex", "base64" };
		byte[][] salts = new byte[][] { defaultSalt.getBytes("UTF-8"), null };
		

		System.out.println("");
		System.out.println("// ############################################");
		System.out.println("// # .. automatically generated properties .. #");
		System.out.println("// ############################################");
		System.out.println("");

		System.out.println(byteArrayDeclaration("inputA", valueInputA.getBytes("UTF-8")));
		System.out.println(byteArrayDeclaration("inputB", valueInputB.getBytes("UTF-8")));

		System.out.println(stringDeclaration("inputAString", valueInputA));
		System.out.println(stringDeclaration("inputBString", valueInputB));
		
		System.out.println(stringDeclaration("salt", defaultSalt));

		System.out.println("public static final Map<String, String> symmetricKeys = new HashMap<>();");
		System.out.println("public static final Map<String, String> asymmetricKeys = new HashMap<>();");
		System.out.println("public static final Map<String, Object> testValues = new HashMap<>();");
		

		System.out.println("");
		System.out.println("// ##############################################");
		System.out.println("// # .. //automatically generated properties .. #");
		System.out.println("// ##############################################");
		System.out.println("");

		System.out.println();
		System.out.println();

		System.out.println("");
		System.out.println("// ############################################");
		System.out.println("// # .. automatically generated data ........ #");
		System.out.println("// ############################################");
		System.out.println("");
		

		System.out.println("// symmetric key definitions");
		System.out.println();
		
		for (KeyStoreSymmetricEntry entry : symmetricKeys.values()) {
			for (String enc : keyStringEncodings) {
				String encodedKey = null;
				switch (enc) {
				case "hex":
					encodedKey = getAsHex(entry.key.getEncoded());
					break;
				case "base64":
					encodedKey = getAsBase64(entry.key.getEncoded());
					break;
				}
				System.out.println("symmetricKeys.put(\"" + entry.keyalg + "|" + enc + "\", \"" + encodedKey + "\");");
			}
		}

		System.out.println();
		System.out.println("// asymmetric key definitions");
		System.out.println();
		
		for (KeyStoreAsymmetricEntry entry : asymmetricKeys.values()) {
			byte[] pubKey = (entry.keyPair.getPublic() != null) ? entry.keyPair.getPublic().getEncoded() : null;
			byte[] privKey = (entry.keyPair.getPrivate() != null) ? entry.keyPair.getPrivate().getEncoded() : null;
			for (String enc : keyStringEncodings) {
				String encodedPubKey = null;
				String encodedPrivKey = null;
				switch (enc) {
				case "hex":
					encodedPubKey = (pubKey != null) ? getAsHex(pubKey) : null;
					encodedPrivKey = (privKey != null) ? getAsHex(privKey) : null;
					break;
				case "base64":
					encodedPubKey = (pubKey != null) ? getAsBase64(pubKey) : null;
					encodedPrivKey = (privKey != null) ? getAsBase64(privKey) : null;
					break;
				}
				if (encodedPubKey != null) {
					System.out.println("asymmetricKeys.put(\"" + entry.keyalg + "|publ|" + enc + "\", \"" + encodedPubKey + "\");");
				}
				if (encodedPrivKey != null) {
					System.out.println("asymmetricKeys.put(\"" + entry.keyalg + "|priv|" + enc + "\", \"" + encodedPrivKey + "\");");
				}
			}
		}

		Map<String, String> testValues = new HashMap<>();
		testValues.put("testInputA", valueInputA);
		testValues.put("testInputB", valueInputB);

		System.out.println();
		System.out.println("// hashing test data");
		System.out.println();
		
		for (String algo : hashingAlgorithms) {
			for (Map.Entry<String, String> entry : testValues.entrySet()) {
				for (byte[] salt : salts) {
					boolean salted = salt != null;
					byte[] clean = entry.getValue().getBytes("UTF-8");
					byte[] encoded = hash(algo, salt, clean);
					for (String enc : inputFormats) {
						String expectedValue = null;
						switch (enc) {
						case "hex":
							expectedValue = "\"" + getAsHex(encoded) + "\"";
							break;
						case "base64":
							expectedValue = "\"" + getAsBase64(encoded) + "\"";
							break;
						default:
							expectedValue = "new byte[] " + Arrays.toString(encoded).replace('[', '{').replace(']', '}');
						}
						System.out.println("testValues.put(\"" + algo + "|" + salted + "|" + enc + "|" + entry.getValue()+ "\", " + expectedValue + ");");
					}
				}
			}
		}
		
		System.out.println();
		System.out.println("// symmetric encryption test data");
		System.out.println();

		for (KeyStoreSymmetricEntry symmetricKeyEntry : symmetricKeys.values()) {
			for (Map.Entry<String, String> entry : testValues.entrySet()) {
				byte[] clean = entry.getValue().getBytes("UTF-8");
				byte[] encoded = encrypt(symmetricKeyEntry.keyalg, symmetricKeyEntry.key, null, clean);
				for (String enc : inputFormats) {
					String expectedValue = null;
					switch (enc) {
					case "hex":
						expectedValue = "\"" + getAsHex(encoded) + "\"";
						break;
					case "base64":
						expectedValue = "\"" + getAsBase64(encoded) + "\"";
						break;
					default:
						expectedValue = "new byte[] " + Arrays.toString(encoded).replace('[', '{').replace(']', '}');
					}
					System.out.println("testValues.put(\"" + symmetricKeyEntry.keyalg + "|false|" + enc + "|" + entry.getValue() + "\", " + expectedValue + ");");
				}
			}
		}
		
		System.out.println();
		System.out.println("// asymmetric encryption test data");
		System.out.println();
		
		for (KeyStoreAsymmetricEntry asymmetricKeyEntry : asymmetricKeys.values()) {
			for (Map.Entry<String, String> entry : testValues.entrySet()) {
				byte[] clean = entry.getValue().getBytes("UTF-8");
				byte[] encoded = encrypt(asymmetricKeyEntry.keyalg, asymmetricKeyEntry.keyPair.getPublic(), null, clean);
				for (String enc : inputFormats) {
					String expectedValue = null;
					switch (enc) {
					case "hex":
						expectedValue = "\"" + getAsHex(encoded) + "\"";
						break;
					case "base64":
						expectedValue = "\"" + getAsBase64(encoded) + "\"";
						break;
					default:
						expectedValue = "new byte[] " + Arrays.toString(encoded).replace('[', '{').replace(']', '}');
					}
					System.out.println("testValues.put(\"" + asymmetricKeyEntry.keyalg + "|false|" + enc + "|" + entry.getValue() + "\", " + expectedValue + ");");
				}
			}
		}

		System.out.println("");
		System.out.println("// ############################################");
		System.out.println("// # .. //automatically generated data ...... #");
		System.out.println("// ############################################");
		System.out.println("");
		
		
	}

	private static String byteArrayDeclaration(String varName, byte[] byteArray) {
		return "public static final byte[] " + varName + " = new byte[] "
				+ Arrays.toString(byteArray).replace('[', '{').replace(']', '}') + ";";
	}

	private static String stringDeclaration(String varName, String string) {
		return "public static final String " + varName + " = \"" + string + "\";";
	}

	private static String getAsHex(byte[] bytes) throws Exception {
		if (bytes == null) {
			return null;
		}
		return hexCodec.encode(bytes);
	}

	private static String getAsBase64(byte[] bytes) throws Exception {
		if (bytes == null) {
			return null;
		}
		return base64Codec.encode(bytes);
	}

	private static byte[] hash(String algorithm, byte[] salt, byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.reset();
		if (salt != null) {
			digest.update(salt);
		}
		digest.update(input);
		byte[] output = digest.digest();
		return output;
	}

	private static byte[] encrypt(String algorithm, Key key, byte[] salt, byte[] value) throws Exception {
		Cipher cipher = Cipher.getInstance(defaultTransformations.get(algorithm));
		if (salt != null) {
			value = concat(salt, value);
		}
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(value);
	}

	private static byte[] concat(byte[] salt2, byte[] value) {
		byte[] result = Arrays.copyOf(salt2, salt2.length + value.length);
		System.arraycopy(value, 0, result, salt2.length, value.length);
		return result;
	}

	static class KeyStoreSymmetricEntry extends KeyStoreTestEntry {
		
		SecretKey key;

		public KeyStoreSymmetricEntry(String alias, String keyalg, String keypass, String storetype, String keystore, String storepass) {
			super(alias, keyalg, keypass, storetype, keystore, storepass);
		}

		@Override
		void load(KeyStore.Entry entry) {
			if (entry instanceof KeyStore.SecretKeyEntry) {
				KeyStore.SecretKeyEntry secEntry = (KeyStore.SecretKeyEntry) entry;
				key = secEntry.getSecretKey();
			} else {
				throw new RuntimeException("Entry "+alias+" is not symmetric");
			}
		}
		
	}

	static class KeyStoreAsymmetricEntry extends KeyStoreTestEntry {

		KeyPair keyPair;
		
		public KeyStoreAsymmetricEntry(String alias, String keyalg, String keypass, String storetype, String keystore, String storepass) {
			super(alias, keyalg, keypass, storetype, keystore, storepass);
		}

		@Override
		void load(KeyStore.Entry entry) {
	
			if (entry instanceof KeyStore.PrivateKeyEntry) {

				KeyStore.PrivateKeyEntry privEntry = (KeyStore.PrivateKeyEntry) entry;

				if (privEntry.getCertificate() == null) {
					throw new RuntimeException("Entry "+alias+" has no associated certificate.");
				}

				if (privEntry.getCertificate().getPublicKey() == null) {
					throw new RuntimeException(
							"Certificate associated with entry "+alias+" has no public key.");
				}
				
				keyPair = new KeyPair(privEntry.getCertificate().getPublicKey(), privEntry.getPrivateKey());

			} else if (entry instanceof KeyStore.TrustedCertificateEntry) {

				KeyStore.TrustedCertificateEntry trustedEntry = (KeyStore.TrustedCertificateEntry) entry;

				if (trustedEntry.getTrustedCertificate().getPublicKey() == null) {
					throw new RuntimeException("Trusted certificate entry "+alias+" has no public key.");
				}
				
				keyPair = new KeyPair(trustedEntry.getTrustedCertificate().getPublicKey(), null);
				
			} else {
				throw new RuntimeException("Entry "+alias+" is not asymmetric: "+entry);
			}
		}
		
	}
	
	public static abstract class KeyStoreTestEntry {
		public String alias;
		public String keyalg;
		public String keypass;
		public String storetype;
		public String keystore;
		public String storepass;

		KeyStoreTestEntry(String alias, String keyalg, String keypass, String storetype, String keystore, String storepass) {
			super();
			this.alias = alias;
			this.keyalg = keyalg;
			this.keypass = keypass;
			this.storetype = storetype;
			this.keystore = keystore;
			this.storepass = storepass;
			
			loadMe();
		}
		
		abstract void load(KeyStore.Entry entry);
		
		void loadMe() {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(keystore);
			if (is == null) {
				throw new RuntimeException("Failed to load "+keystore);
			}
			try { 
				KeyStore keyStore = KeyStore.getInstance(storetype);
				keyStore.load(is, storepass.toCharArray());
				KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(keypass.toCharArray());
				KeyStore.Entry keyStoreEntry = keyStore.getEntry(alias, protectionParam);
				load(keyStoreEntry);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOTools.closeQuietly(is);
			}
		}
		
	}

}
