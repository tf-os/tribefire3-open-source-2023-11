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
package com.braintribe.crypto.key;

import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.base64.Base64;
import com.braintribe.crypto.utils.TextUtils;
import com.braintribe.logging.Logger;

/**
 * encryption / decryption based on an asymmetric RSA algorithm
 * 
 * if you're using encryption/decryption remember that you can only 
 * that you must specify a key length equal or greater than the size of the data 
 * you want to encrypt / decrypt (actually it's more complicated.. 
 * (keysize - 8 bytes) >= data size.. )
 * 
 * and remember: asymmetric encryption/decryption is SLOW, better use
 * symmetric keys for that, and only use asymmetric keys to encrypt / decript
 * the symmetric key. 
 * 
 * but asymmetrics are nice for signatures for instance. 
 * 
 * encryption: use public key
 * decryption: use private key 
 * 
 * @author pit
 *
 */
public class AsymmetricKeyGenerator {
	private static Logger log = Logger.getLogger(AsymmetricKeyGenerator.class);
	
	private KeyPair pair = null;
	private KeyPairGenerator keyGenerator = null;
	
	private static final String ALGO_GENERATOR = "RSA";
	private static final String ALGO_RANDOM = "SHA1PRNG";
	private static final String PROVIDER_RANDOM = "SUN";
	
	/**
	 * creates an asymmetric key of the specified length
	 * default implementations of key and random generators 
	 * @param length - the length of the key 
	 * @throws CryptoServiceException
	 */
	public AsymmetricKeyGenerator(int length) throws CryptoServiceException {
		initialize( ALGO_GENERATOR, ALGO_RANDOM, PROVIDER_RANDOM, length);		
	}
	
	/**
	 * creates an asymmetric key of the specified length
	 * @param generatorAlgo - the generator algorithm to use
	 * @param randomAlgo - the random number generator algorithm to use 
	 * @param randomProvider - provider of the random number sequence 
	 * @param length - the lenght of the key  
	 * @throws CryptoServiceException
	 */
	public AsymmetricKeyGenerator(String generatorAlgo, String randomAlgo, String randomProvider, int length) throws CryptoServiceException {
		initialize( generatorAlgo, randomAlgo, randomProvider, length);
	}
	
	/**
	 * initializes the generator 
	 * @param generatorAlgo - the generator algorithm to use
	 * @param randomAlgo - the random number generator algorithm to use 
	 * @param randomProvider - provider of the random number sequence 
	 * @param length - the lenght of the key  
	 * @throws CryptoServiceException
	 */
	private void initialize(String generatorAlgo, String randomAlgo, String randomProvider, int length) throws CryptoServiceException  {
		try {
			keyGenerator = KeyPairGenerator.getInstance( generatorAlgo);
			SecureRandom random = SecureRandom.getInstance( randomAlgo, randomProvider);
			keyGenerator.initialize( length, random);
						
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( "cannot initialize KeyGenerator as " + e, e);			
		} catch (NoSuchProviderException e) {
			throw new CryptoServiceException( "cannot initialize KeyGenerator as " + e, e);
		}
	}
	
	
	/**
	 * generates a asymmetric key pair
	 */
	public void generateKeyPair() {
		pair = keyGenerator.generateKeyPair();	
	}
	
	/**
	 * @return - the pair
	 */
	public KeyPair getPair() {
		return pair;
	}
	
	/**
	 * @return - the private key of the pair
	 */
	public PrivateKey getPrivateKey() {
		return pair.getPrivate();
	}
	
	/**
	 * @return - the public key of the pair
	 */
	public PublicKey getPublicKey() {
		return pair.getPublic();
	}
	
	/**
	 * @return - the encoded public key 
	 */
	public String getPublicKeyAsString() {
		return Base64.encodeBytes( pair.getPublic().getEncoded());
	}
		
	/**
	 * @return - the encoded private key 
	 */
	public String getPrivateKeyAsString() {
		return Base64.encodeBytes( pair.getPrivate().getEncoded());
	}
	
	/**
	 * import a public key from a String
	 * @param keyAsString - the public key 
	 * @param algorithm - the algorithm that was used to create the key 
	 * @return - the actual public key 
	 * @throws CryptoServiceException
	 */
	public static PublicKey importPublicKey( String keyAsString, String algorithm) throws CryptoServiceException {
		try {
			byte [] encKey = Base64.decode( keyAsString);
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance( algorithm);
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( "cannot import public key as " + e, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoServiceException( "cannot import public key as " + e, e);
		}
	}
	
	
	/**
	 * import a public key generated by a RSA implementation 
	 * @param keyAsString - the public key 
	 * @return
	 * @throws CryptoServiceException
	 */
	public static PublicKey importPublicKey( String keyAsString) throws CryptoServiceException {
		return importPublicKey( keyAsString, ALGO_GENERATOR);
	}
	
	/**
	 * import a public key from a String
	 * @param encodedKey the key, encoded according to the PKCS #8 standard.
	 * @param algorithm - the algorithm that was used to create the key 
	 * @return - the actual public key 
	 * @throws CryptoServiceException
	 */
	public static PublicKey importPublicKey(byte [] encodedKey, String algorithm) throws CryptoServiceException {
		try {
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory keyFactory = KeyFactory.getInstance( algorithm);
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( "cannot import public key as " + e, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoServiceException( "cannot import public key as " + e, e);
		}
	}
	
	/**
	 * import a public key generated by a RSA implementation 
	 * @param encodedKey the key, encoded according to the PKCS #8 standard.
	 * @return
	 * @throws CryptoServiceException
	 */
	public static PublicKey importPublicKey(byte [] encodedKey) throws CryptoServiceException {
		return importPublicKey(encodedKey, ALGO_GENERATOR);
	}
	
	/**
	 * import a private key from a string 
	 * @param keyAsString - the key as a string 
	 * @param algorithm - the algorithm to use
	 * @return - the private key 
	 * @throws CryptoServiceException
	 */
	public static PrivateKey importPrivateKey( String keyAsString, String algorithm) throws CryptoServiceException {
		try {
			byte [] encKey = Base64.decode( keyAsString);
			PKCS8EncodedKeySpec pubKeySpec = new PKCS8EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance( algorithm);
			PrivateKey privKey = keyFactory.generatePrivate(pubKeySpec);
			return privKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( "cannot import private key as " + e, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoServiceException( "cannot import private key as " + e, e);
		}
	}
	
	/**
	 * import a private key created by a RSA implementation
	 * @param keyAsString - the private key as string  
	 * @return - the private key 
	 * @throws CryptoServiceException
	 */
	public static PrivateKey importPrivateKey( String keyAsString) throws CryptoServiceException {
		return importPrivateKey( keyAsString, ALGO_GENERATOR);
	}

	/**
	 * import a private key from its encoded value
	 * @param encodedKey the key, encoded according to the PKCS #8 standard.
	 * @param algorithm - the algorithm to use
	 * @return - the private key 
	 * @throws CryptoServiceException
	 */
	public static PrivateKey importPrivateKey(byte [] encodedKey, String algorithm) throws CryptoServiceException {
		try {
			PKCS8EncodedKeySpec pubKeySpec = new PKCS8EncodedKeySpec(encodedKey);
			KeyFactory keyFactory = KeyFactory.getInstance( algorithm);
			PrivateKey privKey = keyFactory.generatePrivate(pubKeySpec);
			return privKey;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoServiceException( "cannot import private key as " + e, e);
		} catch (InvalidKeySpecException e) {
			throw new CryptoServiceException( "cannot import private key as " + e, e);
		}
	}
	
	/**
	 * import a private key created by a RSA implementation, from its encoded value.
	 * @param encodedKey the key, encoded according to the PKCS #8 standard.
	 * @return - the private key 
	 * @throws CryptoServiceException
	 */
	public static PrivateKey importPrivateKey(byte [] encodedKey) throws CryptoServiceException {
		return importPrivateKey(encodedKey, ALGO_GENERATOR);
	}
	
	/**
	 * exports the public key as a string 
	 * @param publicKey - the public key 
	 * @return - the public key as a string 
	 */
	public static String exportPublicKeyAsString( PublicKey publicKey) {
		return Base64.encodeBytes( publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
	}
	
	/**
	 * exports the private key as a string 
	 * @param privateKey - the private key 
	 * @return - the private key as a string 
	 */
	public static String exportPrivateKeyAsString( PrivateKey privateKey) {
		return Base64.encodeBytes( privateKey.getEncoded(),Base64.DONT_BREAK_LINES);
	}
	
	
	/**
	 * encrypts the input with a RSA public key 
	 * @param input - the input string (not longer than the key length!)
	 * @param publicKey - the public key 
	 * @param charset - the charset to get from the input
	 * @return - the encrypted string 
	 * @throws CryptoServiceException
	 */
	public static String encrypt( String input, Key publicKey, String charset) throws CryptoServiceException {
		try {
			Cipher cipher = Cipher.getInstance( ALGO_GENERATOR);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] inputBytes = null;
			if (charset != null)
				inputBytes = input.getBytes( charset);
			else
				inputBytes = input.getBytes();
			return Base64.encodeBytes( cipher.doFinal(inputBytes), Base64.DONT_BREAK_LINES);			
		} catch (Exception e) {
			String msg = "cannot encrypt message as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 
	}
	
	/**
	 * decripts a string with a private RSA key 
	 * @param input - the input to decrypt 
	 * @param privateKey - the private key to use
	 * @param charset - the charset to extract 
	 * @return - the decripted data 
	 * @throws CryptoServiceException
	 */
	public static String decrypt( String input, Key privateKey, String charset) throws CryptoServiceException {
		try {
			 byte[] bytes = Base64.decode( input);
			 Cipher cipher = Cipher.getInstance( ALGO_GENERATOR);
			 cipher.init(Cipher.DECRYPT_MODE, privateKey);
			 byte[] recoveredBytes = cipher.doFinal( bytes);
			 String recovered = null;
			 if (charset != null)
				 recovered =new String(recoveredBytes, charset);
			 else
				 recovered =new String(recoveredBytes);
			 return recovered;
		} catch (Exception e) {
			String msg = "cannot decrypt message as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 
	}
	
	
	/**
	 * -g : generate public / private key 
	 * 		-q <algo:RSA> <randomAlgo:SHA1PRNG> <randomProvider:SUN> <length>
	 * -e : encrypt
	 * 		-e <input> <public key> <encrypted output>
	 * -d : decrypt
	 * 		-d <encrypted input> <private key> <decrypted output>
	 * 
	 * WARNING: can only encrypt/decrypt data of maximal (length/8-11) bytes !! 
	 * 
	 * @param args
	 */
	public static void main(String [] args) {
		try {
			if (args[0].equalsIgnoreCase( "-g")) {
				String algo = ((args.length < 2) || (args[1] == null) ) ? ALGO_GENERATOR : args[1];				
				String randomAlgo = ((args.length < 3) || (args[3] == null) ) ? ALGO_RANDOM : args[2];
				String randomProvider = ((args.length < 4) || (args[4] == null) ) ? PROVIDER_RANDOM : args[3];
				int length = ((args.length < 5) || (args[4] == null) ) ? 1024 : new Integer(args[4]).intValue();
				AsymmetricKeyGenerator generator = new AsymmetricKeyGenerator( algo, randomAlgo, randomProvider, length);
				generator.generateKeyPair();
				PrivateKey privateKey = generator.getPrivateKey();
				PublicKey publicKey = generator.getPublicKey();
				
				Base64.encodeToFile( privateKey.getEncoded(), "private.key");
				Base64.encodeToFile( publicKey.getEncoded(), "public.key");
				return;
			}
			
			String charset = null;
			if (args.length > 4)
				charset = args[4];
			
			if (args[0].equalsIgnoreCase( "-e")) {
				// args[1] ->  input
				String content = TextUtils.readContentsFromFile( new File( args[1]));
				// args[2] -> public key
				String publicKeyAsString = TextUtils.readContentsFromFile( new File(args[2]));
				PublicKey publicKey = importPublicKey( publicKeyAsString);
				// args[3] -> output
				String output = encrypt( content, publicKey, charset);
				TextUtils.writeContentsToFile( output, new File( args[3]));
				return;
			}
			
			if (args[0].equalsIgnoreCase( "-d")) {
				// args[1] ->  input
				String content = TextUtils.readContentsFromFile( new File( args[1]));
				// args[2] -> private key
				String privateKeyAsString = TextUtils.readContentsFromFile( new File(args[2]));
				PrivateKey privateKey = importPrivateKey( privateKeyAsString);
				// args[3] -> output
				String output = decrypt( content, privateKey, charset);
				TextUtils.writeContentsToFile( output, new File( args[3]));
 
				// args[3] -> output
				return;
			}
			
			
			
		} catch (CryptoServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
