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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * a context for writing {@link Document}<br/>
 * 
 * @author pit
 *
 */
public class WriteDocumentContext { 
	private static Logger log = Logger.getLogger(WriteDocumentContext.class);
	
	private TransformerFactory transformerFactory;
	private StreamSource styleSheet;
	private DOMSource domSource;
	private boolean omitDeclaration = false;
	private boolean suppressIndent; 
	private Map<String, String> outputProperties;
	private int indent = 4;
	private String encoding = "UTF-8";
	
	public WriteDocumentContext() {
		transformerFactory = TransformerFactory.newInstance();
		determineIdentDefault();
	}
	
	public WriteDocumentContext( TransformerFactory factory){
		transformerFactory = factory;
		determineIdentDefault();
	}
	
	/**
	 * as JAVA differs in the identing logic, we must determine the version
	 * of JAVA we're running on.<br/>
	 * JAVA 8 or lower : {@link WriteDocumentContext#suppressIndent} = false <br/>
	 * JAVA 9 or higher : {@link WriteDocumentContext#suppressIndent} = true;
	 */
	private void determineIdentDefault() {
		String javaVersion = System.getProperty("java.version");
		try {
			String parseableJavaVersion = javaVersion.substring(0, javaVersion.indexOf('.') + 2);
			Double javaVersionAsDouble = Double.parseDouble( parseableJavaVersion);
			if (javaVersionAsDouble >= 9) {
				suppressIndent = true;
			}
			else {
				suppressIndent = false;
			}
		} catch (Exception e) {
			log.error( "cannot process version number of current java [" + javaVersion + "], deactivating indenting as per default", e);
			suppressIndent = true;			
		}
	}
	/**
	 * suppresses indentation 
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext suppressIndent(){
		suppressIndent = true;
		return this;
	}
	
	/**
	 * activates or deactivates the indentation<br/> 
	 * a word of warning : if activated since JAVA9 strange newlines will appear,
	 * default false will generate proper indenting 
	 * @param value - TRUE for INDENTATION or FALSE for NONE
	 * @return  - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setIndent(boolean value){
		suppressIndent = !value;
		return this;
	}
	
	/**
	 * activates indent and sets the value to the number of spaces passed<br/>
	 * a word of warning : if activated since JAVA9 strange newlines will appear,
	 * default false will generate proper indenting  
	 * @param indent - number of spaces 
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setIndent( int indent) {
		suppressIndent = false;
		this.indent = indent;
		return this;
	}
	
	/**
	 * deactivates the {@literal<?xml ..>} declaration 
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setOmitDeclaration(){
		this.omitDeclaration = true;
		return this;
	}
	
	/**
	 * activates or deactivates the XML declaration 
	 * @param value - TRUE to DEACTIVATE, FALSE to ACTIVATE
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setOmitDeclaration( boolean value){
		this.omitDeclaration = value;
		return this;
	}
	/**
	 * sets the encoding (default is UTF-8)
	 * @param encoding - the encoding as a {@link String}
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setEncoding( String encoding) {
		this.encoding = encoding;
		return this;
	}
	
	/**
	 * sets the document to write out
	 * @param document - the {@link Document}
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext from( Document document) {
		this.domSource = new DOMSource( document);
		return this;
	}
	
	/**
	 * set the Node (Element or Node) to write out 
	 * @param node - the {@link Node} to write 
	 * @return - the {@link WriteDocumentContext} 
	 */
	public WriteDocumentContext from( Node node) {
		this.domSource = new DOMSource( node);
		return this;
	}
	
	/**
	 * @param source
	 * @return
	 */
	public WriteDocumentContext from( DOMSource source) {
		this.domSource = source;
		return this;
	}
	/**
	 * sets a style sheet to be used while writing (pretty printing)
	 * @param source - the {@link StreamSource}
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setStyleSheet( StreamSource source){
		styleSheet = source;
		suppressIndent = false;
		return this;
	}
	/**
	 * sets a style sheet to be used while writing (pretty printing)
	 * @param stream - the {@link InputStream}
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setStyleSheet( InputStream stream){
		return setStyleSheet( new StreamSource(stream));
	}
	
	/**
	 * sets a style sheet to be used while writing (pretty printing)
	 * @param file
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setStyleSheet( File file) {
		
		return setStyleSheet( new StreamSource( file));
	}
	
	/**
	 * sets a style sheet to be used while writing (pretty printing)
	 * @param reader - the {@link Reader}
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setStyleSheet( Reader reader) {
		return setStyleSheet( new StreamSource( reader));
	}
	/**
	 * sets a style sheet to be used while writing (pretty printing)
	 * @param contents - the contents of the Style Sheet
	 * @return - the {@link WriteDocumentContext}
	 */
	public WriteDocumentContext setStyleSheet( String contents) {
		return setStyleSheet( new StringReader(contents));
	}
	
	public WriteDocumentContext setOutputProperty( String name, String value) {
		if (outputProperties == null) {
			outputProperties = new HashMap<String, String>(1);
		}
		outputProperties.put(name, value);
		return this;
	}
		
	/**
	 * actually writes the document 
	 * @param result - the {@link StreamResult} to write to 
	 * @throws DomParserException - if anything goes wrong 
	 */
	public void to(StreamResult result) throws DomParserException {
		
		try {
			Transformer transformer = null;
			if (styleSheet != null)
				transformer = transformerFactory.newTransformer( styleSheet);
			else
				transformer = transformerFactory.newTransformer();
			
			if (suppressIndent == false) {
			 	transformer.setOutputProperty(OutputKeys.INDENT,"yes");	        				 	
			 	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indent);
			 	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        } else {
	        	transformer.setOutputProperty(OutputKeys.INDENT,"no");
	        	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
	        	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        }
			transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
									
			if (omitDeclaration) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");			
			}	
			
			// transfer any output properties 
			if (outputProperties != null) {
				for (Entry<String, String> entry : outputProperties.entrySet()) {
					transformer.setOutputProperty(entry.getKey(), entry.getValue());
				}
			}
			
			if (domSource == null) {
				String msg="no DOMSource specified";
				log.error( msg);
				throw new DomParserException( msg);
			}
			transformer.transform(domSource, result);
			
		} catch (Exception e) {
			String msg = "cannot save document";
			log.error( msg, e);
			throw new DomParserException( msg, e);
		}
	}
	
	/**
	 * writes the {@link Document} to a {@link File}
	 * @param file - the {@link File} to write to 
	 * @throws DomParserException
	 */
	public void to( File file) throws DomParserException {
		OutputStream stream = null;
		try {
			stream = new FileOutputStream( file);
			StreamResult result = new StreamResult( new OutputStreamWriter( stream, encoding));
			to(result);
		} catch (FileNotFoundException e) {
			String msg = "cannot save document";
			log.error( msg, e);
			throw new DomParserException( msg, e);
		} catch (UnsupportedEncodingException e) {
			String msg = "cannot save document";
			log.error( msg, e);
			throw new DomParserException( msg, e);
		}
		finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					String msg = "cannot close stream";
					log.error( msg, e);
					throw new DomParserException( msg, e);
				}
		}
	}
	
	/**
	 * writes the {@link Document} to the {@link OutputStream}
	 * @param stream - the {@link OutputStream} to write to 
	 * @throws DomParserException - if anything goes wrong 
	 */
	public void to( OutputStream stream) throws DomParserException {
		StreamResult result = new StreamResult( stream);
		to(result);
	}
	
	/**
	 * writes the {@link Document} to {@link String}
	 * @return - the {@link Document} as a {@link String} 
	 * @throws DomParserException - if anything goes wrong..
	 */
	public String to() throws DomParserException {
		StringWriter writer =  new StringWriter();
		StreamResult result = new StreamResult( writer);
		to(result);
		return writer.toString();
	}
	
	public static void main( String [] args) {
	
		for (String javaVersion : args) {
			boolean suppressIndent;
			try {
				String parseableJavaVersion = javaVersion.substring(0, javaVersion.indexOf('.') + 2);
				Double javaVersionAsDouble = Double.parseDouble( parseableJavaVersion);
				if (javaVersionAsDouble >= 9) {
					suppressIndent = true;
				}
				else {
					suppressIndent = false;
				}
			} catch (Exception e) {
				suppressIndent = true;				
			}
			System.out.println( javaVersion + "-> suppressing indent [" + suppressIndent + "]");
		}
	}
	
}
