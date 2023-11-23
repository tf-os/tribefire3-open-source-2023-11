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
package com.braintribe.utils.xml.parser.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * grants access to loading a document.. <br/>
 * @author Pit
 *
 */
public class LoadDocumentContext extends DocumentContext<LoadDocumentContext> {
	private static Logger log = Logger.getLogger(LoadDocumentContext.class);
	
	/**
	 * 
	 */
	public LoadDocumentContext() {
		setSelf(this);
	}
	
	/**
	 * @param factory
	 */
	public LoadDocumentContext( DocumentBuilderFactory factory) {
		super( factory);
		setSelf(this);
	}
	
	/**
	 * activates support for xinclude statements 
	 * @return - this {@link LoadDocumentContext}
	 */
	public LoadDocumentContext setIncludeAware() {
		setIncludeAware( true);
		return this;
	}
	/**
	 * activates/deactivates xinclude support
	 * @param value - value to set 
	 * @return - this {@link LoadDocumentContext}
	 */
	public LoadDocumentContext setIncludeAware( boolean value) {
		builderFactory.setXIncludeAware(value);
		return this;
	}
	
	/**
	 * activates validation 
	 * @return - this {@link LoadDocumentContext}
	 */
	public LoadDocumentContext setValidating() {
		setValidating( true);
		return this;
	}
	public LoadDocumentContext setValidating( boolean value) {		
		builderFactory.setValidating(value);
		return this;
	}
	
	/**
	 * sets the {@link Schema} to validate with
	 * @param value - the {@link Schema} to use 
	 * @return - this {@link LoadDocumentContext}
	 */
	public LoadDocumentContext setSchema( Schema value) {
		builderFactory.setSchema(value);
		return this;
	}
	
	public Document from( URL url) throws DomParserException  {
		return from( url, null);
	}
	/**
	 * loads the document from the {@link URL} passed 
	 * @param url - the {@link URL}
	 * @return - the {@link Document}
	 * @throws DomParserException - if anything goes wrong 
	 */
	public Document from( URL url, Map<String, String> requestProperties) throws DomParserException {
		InputStream stream = null;
		try {
			DocumentBuilder builder = getDocumentBuilder();
			URLConnection connection = url.openConnection();
			if (requestProperties != null) {
				for (Entry<String, String> entry : requestProperties.entrySet()) {
					connection.setRequestProperty( entry.getKey(), entry.getValue());
				}
			}			
			stream = connection.getInputStream();
			if (connection instanceof HttpURLConnection) {
				// check for redirection ... 
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				int code = httpConnection.getResponseCode();
				if (code == 301 || code == 302) {
					String msg = httpConnection.getHeaderField( "Location");
					URL redirect = new URL( url, msg);
					return from( redirect);					
				}
			}
			
			Document document = builder.parse( stream);		
			return document;
		} catch (Exception e) {
			String msg = "cannot load document from url [" + url + "]";
			log.error( msg, e);
			throw new DomParserException(msg, e);
		} 
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					String msg ="cannot close stream from url [" + url.toString() + "]";
					log.error( msg, e);
				}
			}
		}
	}
	
	/**
	 * loads the document from a {@link InputSource}
	 * @param source - the {@link InputSource}
	 * @return - the {@link Document}
	 * @throws DomParserException - if anything goes wrong
	 */
	public Document from(InputSource source) throws DomParserException {
		try {
			DocumentBuilder builder = getDocumentBuilder();						
			Document document = builder.parse( source);		
			return document;
		} catch (Exception e) {
			String msg = "cannot load document from stream ";
			log.error( msg, e);
			throw new DomParserException(msg, e);
		} 
	}

	private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		
		if (builder.isXIncludeAware()) {
			
			builder.setEntityResolver(new EntityResolver() {
		            @Override
		            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		            	URL url = new URL(systemId);		            
	            		InputSource inputSource = new InputSource( url.openStream());
	            		inputSource.setSystemId(systemId);
						return inputSource;
		            }
		       });
			
		}
		
		return builder;
	}
	
	/**
	 * loads the document from a {@link File}
	 * @param file - the {@link File} to load from 
	 * @return - the {@link Document}
	 * @throws DomParserException - if anything goes wrong 
	 */
	public Document from( File file) throws DomParserException {
		try {
			DocumentBuilder builder = getDocumentBuilder();						
			Document document = builder.parse( file);		
			return document;
		} catch (Exception e) {
			String msg = "cannot load document from stream ";
			log.error( msg, e);
			throw new DomParserException(msg, e);
		} 
	}
	
	/**
	 * loads the document from an {@link InputStream}
	 * @param stream - the {@link InputStream} to load from 
	 * @return - the {@link Document} 
	 * @throws DomParserException - if anything goes wrong 
	 */
	public Document from( InputStream stream) throws DomParserException{
		return from( new InputSource( stream));
	}

	/**
	 * load the {@link Document} from {@link File} with name 
	 * @param filename - the name of the file 
	 * @return - the {@link Document}
	 * @throws DomParserException - if anything goes wrong 
	 */
	public Document fromFilename( String filename) throws DomParserException {
		File file = new File(filename);
		if (file.exists() == false) {
			String msg = "file ["+ filename + "] doesn't exist";
			log.error( msg);
			throw new DomParserException(msg);
		}
		return from(file);
	}

	/**
	 * loads the document from a {@link Reader}
	 * @param reader - the {@link Reader} to load from 
	 * @return - the {@link Document}
	 * @throws DomParserException - if anything goes wrong 
	 */
	public Document from( Reader reader) throws DomParserException {
		return from( new InputSource( reader));
	}
	
	/**
	 * loads the document from the contents of the {@link String} passed
	 * @param contents - the {@link String} to convert to a {@link Document}
	 * @return - the {@link Document}
	 * @throws DomParserException
	 */
	public Document from( String contents) throws DomParserException {
		return from( new StringReader( contents));
	}
	
	
	
	
	
}
