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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.base64.Base64;
import com.braintribe.crypto.utils.TextUtils;
import com.braintribe.logging.Logger;

/**
 * symmetric key generator and encryption/decryption based on Triple DES
 * 
 * @author pit
 *
 */
public class SymmetricKeyGenerator {

	private static Logger log = Logger.getLogger(SymmetricKeyGenerator.class);
	
	private static final String SYMMETRIC_INSTANCE = "DESede"; // triple DES
			
	private SecretKey desEdeKey = null;	
	
	/**
	 * generate key 
	 * 
	 * @throws CryptoServiceException
	 */
	public SymmetricKeyGenerator() throws CryptoServiceException {
	
			try {
				KeyGenerator desEdeGen = KeyGenerator.getInstance( SYMMETRIC_INSTANCE ); // Triple DES
				desEdeKey = desEdeGen.generateKey();               // Generate a key											
			}  catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
			
	/**
	 * extract key 
	 * @return
	 */
	public Key getKey() {	
		return desEdeKey; 			
	}
	
		 
	
	/**
	 * encrypt something (a string) with a symmetric key 
	 * @param input - the string to encrypt 
	 * @param key - the symmetric key to use
	 * @return - the encrypted string 
	 * @throws CryptoServiceException
	 */
	public static String encrypt( String input, SecretKey key, String charset) throws CryptoServiceException{
		try {
			Cipher cipher = Cipher.getInstance( SYMMETRIC_INSTANCE);
			cipher.init(Cipher.ENCRYPT_MODE, key);
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
	 * decrypt a string with a symmetric key 
	 * @param input - the string to decrypt
	 * @param key - the symmetric key to use 
	 * @return - the decrypted text 
	 * @throws CryptoServiceException
	 */
	public static String decrypt( String input, SecretKey key, String charset) throws CryptoServiceException{
		 try {
			 byte[] bytes = Base64.decode( input);
			 Cipher cipher = Cipher.getInstance( SYMMETRIC_INSTANCE);
			 cipher.init(Cipher.DECRYPT_MODE, key);
			 byte[] recoveredBytes = cipher.doFinal( bytes);
			 String recovered = null;
			 if (charset != null)
				 recovered = new String(recoveredBytes, charset);
			 else
				 recovered = new String(recoveredBytes);
			 return recovered;
		} catch (Exception e) {
			String msg = "cannot decrypt message as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 
	}
	
	
	/**
	 * get the key as string (base64 encoding) 
	 * @return - the encoded key 
	 * @throws CryptoServiceException
	 */
	public String getKeyAsString() throws CryptoServiceException {
		return exportKeyAsString( desEdeKey);
	}
	
	/**
	 * export the passed key as base64 string 
	 * @param key - the key to export 
	 * @return - the key as string 
	 * @throws CryptoServiceException
	 */
	public static String exportKeyAsString( SecretKey key) throws CryptoServiceException {
		try {
			SecretKeyFactory desEdeFactory = SecretKeyFactory.getInstance( SYMMETRIC_INSTANCE);
			DESedeKeySpec desEdeSpec = (DESedeKeySpec)desEdeFactory.getKeySpec( key, javax.crypto.spec.DESedeKeySpec.class);
			byte[] rawKey = desEdeSpec.getKey();			
			return Base64.encodeBytes( rawKey, Base64.DONT_BREAK_LINES);
		} catch (NoSuchAlgorithmException e) {
			String msg = "cannot export key as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} catch (InvalidKeySpecException e) {
			String msg = "cannot export key as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		}
	}
	
	/**
	 * import a key from a string 
	 * @param value - the key in it's base64 encoded form  
	 * @return - the key 
	 * @throws CryptoServiceException
	 */
	public static SecretKey importKeyFromString( String value) throws CryptoServiceException{
		try {
			byte [] bytes = Base64.decode( value);
			DESedeKeySpec keyspec = new DESedeKeySpec( bytes);
			SecretKeyFactory desEdeFactory = SecretKeyFactory.getInstance( SYMMETRIC_INSTANCE);
			SecretKey key = desEdeFactory.generateSecret(keyspec);
			return key;
		} catch (InvalidKeyException e) {
			String msg = "cannot import key as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} catch (NoSuchAlgorithmException e) {
			String msg = "cannot import key as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} catch (InvalidKeySpecException e) {
			String msg = "cannot import key as " + e; 
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		}
	}
	
	
	/**
	 * -g : generate secret key and write it to file 
	 * -e : encrypt a file 
	 * 		-e <input> <key> <encrypted output>
	 * -d : decrypt the file  
	 * 		-d <encrypted input> <key> <decrypted output>
	 * @param args
	 */
	public static void main( String [] args) {
		try {
			if (args[0].equalsIgnoreCase( "-g")) {
				SymmetricKeyGenerator generator = new SymmetricKeyGenerator();
				String key = generator.getKeyAsString();
				TextUtils.writeContentsToFile( key, new File("symmetric.key"));				
				return;
			}
			
			String charset = null;
			
			if (args.length > 4)
				charset = args[4];
			
			if (args[0].equalsIgnoreCase( "-e")) {
				String input = TextUtils.readContentsFromFile( new File( args[1]));
				String keyAsString = TextUtils.readContentsFromFile( new File( args[2]));
				SecretKey key = importKeyFromString( keyAsString);				
				String output = encrypt( input, key, charset);
				TextUtils.writeContentsToFile( output, new File( args[3]));
				return;
			}
			
			if (args[0].equalsIgnoreCase( "-d")) {
				String input = TextUtils.readContentsFromFile( new File( args[1]));
				String keyAsString = TextUtils.readContentsFromFile( new File( args[2]));
				SecretKey key = importKeyFromString( keyAsString);				
				String output = decrypt( input, key, charset);
				TextUtils.writeContentsToFile( output, new File( args[3]));
				return;
			}
		} catch (CryptoServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
