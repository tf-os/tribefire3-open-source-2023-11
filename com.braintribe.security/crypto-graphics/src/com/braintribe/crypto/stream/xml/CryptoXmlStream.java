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
package com.braintribe.crypto.stream.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

import org.w3c.dom.Document;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * a class to handle encrypted xml files
 * uses Password Based Encryption (as defined in PKCS #5)
 * 
 * loads (and decrypts) an encrypted file into a document
 * saves (and encrypts) a document to a file
 * 
 * @author pit
 *
 */
public class CryptoXmlStream {
	
	private final static Logger log = Logger.getLogger(CryptoXmlStream.class);
	
	
	private static final int MODE_FILE = 0;
	private static final int MODE_STRING = 1;
	
	private static final String ALGORITHM = "PBEWithMD5AndDES";
	private PBEKeySpec pbeKeySpec;
    private PBEParameterSpec pbeParamSpec;
    private SecretKeyFactory keyFac;

    // Salt
    private byte[] salt = {
		        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
		        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	    	};

    // Iteration count
    private int count = 20;
		
	private SecretKey pbeKey;
	
	
	/**
	 * constructor 
	 * @param pwd password to use 
	 * @throws CryptoException
	 */
	public CryptoXmlStream( String pwd) throws CryptoServiceException{
		
		
	    try {
			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);	    
			pbeKeySpec = new PBEKeySpec( pwd.toCharArray());
			keyFac = SecretKeyFactory.getInstance( ALGORITHM);
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
	 * returns a cipher 
	 * @param mode either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @return the appropriate Cipher
	 * @throws CryptoException
	 */
	private Cipher getCipher( int mode) throws CryptoServiceException{
				
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance( ALGORITHM);

			// Initialize PBE Cipher with key and parameters
			cipher.init( mode, pbeKey, pbeParamSpec);
		} 
		catch (InvalidKeyException e) {		
			String msg = "cannot get cipher as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 
		catch (NoSuchAlgorithmException e) {
			String msg = "cannot get cipher as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);			
		} 
		catch (NoSuchPaddingException e) {
			String msg = "cannot get cipher as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 
		catch (InvalidAlgorithmParameterException e) {
			String msg = "cannot get cipher as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		}
	  
	
	    return cipher;
	}


	/**
	 * loads an encrypted xml and decrypts it while reading it 
	 * @param filename file name to load 
	 * @param include whether to include other xmls 
	 * @return the org.w3c.dom.Document
	 * @throws CryptoException
	 */
	public Document loadXml( String filename, boolean include) throws CryptoServiceException {		
	
				
		Cipher cipher = getCipher( Cipher.DECRYPT_MODE);
		File file = new File( filename);
		FileInputStream f_inputStream = null;
		try {
			f_inputStream = new FileInputStream( file);
		} catch (FileNotFoundException e) {
			String msg = "can't load file '" + filename + " as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		}
		CipherInputStream c_inputStream = new CipherInputStream( f_inputStream, cipher);
		
		Document doc = null;
		 try {
			doc = DomParser.load().setIncludeAware(include).from(c_inputStream);			
		} catch (DomParserException e) {			
			e.printStackTrace();
		} 		
		
		try {
			c_inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return doc;
	}
	
	/**
	 * loads an xml document from an encrypted string 
	 * @param content - the encrypted content 
	 * @param encoding - the encoding the actual xml was in 
	 * @return - the document 
	 * @throws CryptoServiceException
	 */
	public Document loadXmlFromString( String content, String encoding) throws CryptoServiceException {		
		
				
		Cipher cipher = getCipher( Cipher.DECRYPT_MODE);		
		ByteArrayInputStream f_inputStream = null;
		
	
		f_inputStream = new ByteArrayInputStream( content.getBytes());
		
		CipherInputStream c_inputStream = new CipherInputStream( f_inputStream, cipher);
		
		Document doc = null;
		 try {
			doc = DomParser.load().from( c_inputStream);
		} catch (DomParserException e) {			
			String msg = "Can't load decrypted xml from string as " + e;
			log.error( msg, e);
			throw new CryptoServiceException( msg, e);
		} 		
		
		try {
			c_inputStream.close();
		} catch (IOException e) {
			String msg = "cannot close cipher input-stream as " + e;
			log.warn( msg);			
		}
		
		return doc;
	}
	
	/**
	 * saves an unencrypted xml and encrypts it while writing 
	 * @param document the org.w3c.dom.Document to save 
	 * @param filename the name of the file to save 
	 * @throws DomUtilsException
	 * @throws CryptoException
	 */
	public void saveXml( Document document, String filename) throws DomParserException,CryptoServiceException {
				
		Cipher cipher = getCipher( Cipher.ENCRYPT_MODE);
		File file = new File( filename);
		FileOutputStream f_outputStream = null;
		try {
			f_outputStream = new FileOutputStream( file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new DomParserException("can't save file '" + filename + " as " + e, e);
		}
		CipherOutputStream c_outputStream = new CipherOutputStream( f_outputStream, cipher);			
		DomParser.write().from(document).to( c_outputStream);
		try {
			c_outputStream.flush();
			c_outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		
	}
	
	/**
	 * saves an xml document into an encrypted string 
	 * @param document
	 * @param encoding
	 * @return
	 * @throws CryptoServiceException
	 */
	public String saveXmlToString( Document document, String encoding) throws CryptoServiceException {
		
				
		Cipher cipher = getCipher( Cipher.ENCRYPT_MODE);
		
		ByteArrayOutputStream f_outputStream = null;
		
		f_outputStream = new ByteArrayOutputStream();
		
		CipherOutputStream c_outputStream = new CipherOutputStream( f_outputStream, cipher);
		
		try {
			DomParser.write().from(document).setOmitDeclaration().setIndent(false).to( c_outputStream);
		} catch (DomParserException e1) {
			String msg = "cannot save xml to string as " + e1;
			log.error( msg, e1);
			throw new CryptoServiceException( msg, e1);
		}
		try {
			c_outputStream.flush();
			c_outputStream.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		return f_outputStream.toString();
			
	}
			

	/**
	 * @param args
	 */
	public static void main(String [] args) {
		
		int mode = MODE_FILE;
		if (args[0].equalsIgnoreCase( "string"))
			mode = MODE_STRING;
		
		switch (mode) {
		
			case MODE_FILE : {
				String modeString = args[1];
				String file1 = args[1];
				String file2 = args[2];
				boolean encrypt = true;
				if ( modeString.equalsIgnoreCase( "encrypt")) {
				  encrypt = true;
				} else {
				  encrypt = false;
				}
				try {
					CryptoXmlStream cryptoXmlStream = new CryptoXmlStream("Lisa");		
					if (encrypt) {
						Document doc = DomParser.load().from(file1);
						cryptoXmlStream.saveXml( doc, file2);
					
					} else {
						Document doc = cryptoXmlStream.loadXml( file1, false);
						DomParser.write().from( doc).to( new File(file2));
					}
				} catch (CryptoServiceException e) {	
					e.printStackTrace();
				} catch (DomParserException e) {	
					e.printStackTrace();
				}
				
				break;
			}
			case MODE_STRING: {
				String pwd = args[1];
				String contents = args[2];
				try {
					CryptoXmlStream cryptoXmlStream = new CryptoXmlStream( pwd);
					Document document = DomParser.load().from(contents);
					String berk = cryptoXmlStream.saveXmlToString(document, "UTF-8");
					Document document2 = cryptoXmlStream.loadXmlFromString(berk, "UTF-8");
					System.out.println( DomParser.write().from(document).setEncoding("UTF-8").to());
				} catch (CryptoServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DomParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			}		
		}
		
		
		
				
		  
	}
}
