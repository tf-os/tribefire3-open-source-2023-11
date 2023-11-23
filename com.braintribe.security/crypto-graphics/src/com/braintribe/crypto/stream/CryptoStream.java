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
package com.braintribe.crypto.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.logging.Logger;


/**
 * cryptographic stream with DES password based
 * reads and writes files while decrypting / encrypting
 * 
 * @author pit
 *
 */
public class CryptoStream {
	
	private final static Logger log = Logger.getLogger(CryptoStream.class);
	
	PBEKeySpec pbeKeySpec;
    PBEParameterSpec pbeParamSpec;
    SecretKeyFactory keyFac;

    // Salt
    byte[] salt = {
        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };

    // Iteration count
    int count = 20;
		
	SecretKey pbeKey;
	
	
	/**
	 * constructor
	 * @param pwd password to use for the DES
	 * @throws CryptoException
	 */
	public CryptoStream( String pwd) throws CryptoServiceException{

	    try {
			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);	    
			pbeKeySpec = new PBEKeySpec( pwd.toCharArray());
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			pbeKey = keyFac.generateSecret(pbeKeySpec);
		} 
	    catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new CryptoServiceException("cannot instantiate cryptostream as " + e, e);
		} 
	    catch (InvalidKeySpecException e) {
			e.printStackTrace();
			throw new CryptoServiceException("cannot instantiate cryptostream as " + e, e);
		}		
	    
	    

	}
	
	/**
	 * generates cipher 
	 * @param mode 
	 * @return
	 * @throws CryptoException
	 */
	private Cipher getCipher( int mode) throws CryptoServiceException{
	    // Create PBE Cipher
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance("PBEWithMD5AndDES");

			// Initialize PBE Cipher with key and parameters
			cipher.init( mode, pbeKey, pbeParamSpec);
		} 
		catch (InvalidKeyException e) {		
			e.printStackTrace();
			throw new CryptoServiceException("cannot get cipher as " + e, e);
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new CryptoServiceException("cannot get cipher as " + e, e);
		} 
		catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new CryptoServiceException("cannot get cipher as " + e, e);
		} 
		catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new CryptoServiceException("cannot get cipher as " + e, e);
		}
	    
	    return cipher;
	}

	/**
	 * reads from a file via a CipherInputStream 
	 * @param file the file to read
	 * @return the content as an array of bytes 
	 * @throws CryptoException
	 */
	public byte [] readFromFile(File file) throws CryptoServiceException{
		try {
			Cipher cipher = getCipher( Cipher.DECRYPT_MODE);
			FileInputStream f_inputStream = new FileInputStream( file);
			CipherInputStream c_inputStream = new CipherInputStream( f_inputStream, cipher);
			byte[] bytes = new byte[(int) file.length()];			
			int start = 0; 
			int remaining = bytes.length;
			do {
				int numRead = c_inputStream.read( bytes, start, remaining);
				start += numRead;
				remaining -= numRead;				
				if (numRead < 0)
					break;
				log.info( "Read " + numRead + " bytes, remaining " + remaining);
			} while (start < bytes.length);
			
			c_inputStream.close();
			return bytes;
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			throw new CryptoServiceException("can't read from file '" + file.getAbsolutePath() + "' as " + e, e);
		} catch (IOException e) {			
			e.printStackTrace();
			throw new CryptoServiceException("can't read from file '" + file.getAbsolutePath() + "' as " + e, e);
		}
		
	}
	
	/**
	 * writes an array of bytes to a file via a CipherOutputStream
	 * @param file the file to write to 
	 * @param bytes the bytes to write 
	 * @throws CryptoException
	 */
	public void writeToFile(File file, byte [] bytes) throws CryptoServiceException{
		try {
			Cipher cipher = getCipher( Cipher.ENCRYPT_MODE);
			FileOutputStream f_outputStream = new FileOutputStream( file);
			CipherOutputStream c_outputStream = new CipherOutputStream( f_outputStream, cipher);
			c_outputStream.write( bytes);
			c_outputStream.flush();
			c_outputStream.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			throw new CryptoServiceException("can't write to file '" + file.getAbsolutePath() + "' as " + e, e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CryptoServiceException("can't write to file '" + file.getAbsolutePath() + "' as " + e, e);
		}
	}
	
	/**
	 * reads a file (directly, no cipher) 
	 * @param aFile the file to write to 
	 * @return the bytes read 
	 */
	public byte [] readContentsFromFile( File aFile) {
		byte[] bytes = new byte[(int) aFile.length()];
		try {
			RandomAccessFile ref = new RandomAccessFile( aFile, "r");
			ref.readFully( bytes);
			ref.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return bytes;
	}
	
	
	/**
	 * writes the passed bytes to a file 
	 * @param file the file to write to 
	 * @param bytes the bytes to write 
	 */
	public void writeContentsToFile( File file, byte[] bytes) {
		try {
			FileOutputStream f_outputStream = new FileOutputStream( file);
			f_outputStream.write( bytes);
			f_outputStream.flush();
			f_outputStream.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main( String [] args) {
		  boolean encrypt = true;
		  if (args[0].equalsIgnoreCase( "encrypt")) {
			  encrypt = true;
		  } else {
			  encrypt = false;
		  }
		  File in = new File( args[1]);
		  File out = new File( args[2]);
		  
		  try {
			CryptoStream cryptoStream = new CryptoStream( "Lisa");
			  
			  if (encrypt) {			  
				  byte [] data = cryptoStream.readContentsFromFile( in);
				  cryptoStream.writeToFile( out, data);
			  } else {
				  byte [] data = cryptoStream.readFromFile( in);
				  cryptoStream.writeContentsToFile( out, data);
			  }
		} catch (CryptoServiceException e) {
		
			e.printStackTrace();
		}
		  
	}
	
}
