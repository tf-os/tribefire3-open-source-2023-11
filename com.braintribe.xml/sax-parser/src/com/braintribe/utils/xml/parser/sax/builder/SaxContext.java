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
package com.braintribe.utils.xml.parser.sax.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.sax.SaxParserErrorHandler;
import com.braintribe.utils.xml.parser.sax.SaxParserException;


/**
 * the builder construct that configures the SAX parsing and eventually runs the parsing 
 * 
 * @author pit
 *
 */
public class SaxContext {

	private static Logger log = Logger.getLogger(SaxContext.class);
	private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;	
	
	private SAXParserFactory factory;
	private SAXParser parser;
	private XMLReader xmlReader;
	private SchemaFactory schemaFactory;
	private Schema schema;
	private Source schemaSource;
	private Source [] schemaSources;
	private ErrorHandler errorHandler;
	private EntityResolver resolver;
	private LSResourceResolver schemaResolver;
	
	private ContentHandler handler;
	private boolean includeAware = false;
	private boolean namespaceAware = false;
	private boolean validating = false;
	private Map<String, Object> parserProperties = new HashMap<String, Object>();
	private Map<String, Boolean> factoryFeatures = new HashMap<String, Boolean>();
	private Map<String, Boolean> readerFeatures = new HashMap<String, Boolean>();
	private Map<String, Object> readerProperties = new HashMap<String, Object>();
	
	// constructors 
	public SaxContext() {
	}	
	public SaxContext( SAXParserFactory factory) {
		this.factory = factory;
	}	
	public SaxContext( SAXParser parser) {
		this.parser = parser;
	}	
	public SaxContext( XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}
	
	
	/**
	 * sets the {@link ContentHandler} which the {@link XMLReader} should use, if not specified, no handler is used 
	 * (probably only useful for sax based validation)
	 * @param handler
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setHandler( ContentHandler handler){
		this.handler = handler;
		return this;
	}	
	
	/**
	 * sets the {@link ErrorHandler} for the {@link XMLReader}. If none is specified, the {@link SaxParserErrorHandler} is used
	 * @param errorHandler - the {@link ErrorHandler} to be used (other that the default)
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setErrorHandler( ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}
	
	/**
	 * sets resource resolver for the {@link SchemaFactory}
	 * @param resolver - a {@link LSResourceResolver} instance 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext resolveSchemaWith( LSResourceResolver resolver) {
		this.schemaResolver = resolver; 
		return this;
	}
	/**
	 * sets the {@link EntityResolver} for the {@link XMLReader} 
	 * @param resolver - an {@link EntityResolver} instance 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext resolveWith(EntityResolver resolver) {
		this.resolver = resolver; 
		return this;
	}
	
	/**
	 * activates the support of inclusions of references to external xml (xs:import / xs:include)  
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setIncludeAware() {
		this.includeAware = true;
		return this;
	}	
	/**
	 * sets whether the parser should support the inclusion of references to external xml (xs:import / xs:include),
	 * default is false. 
	 * @param aware - true for inclusion support, false otherwise
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setIncludeAware(boolean aware) {
		this.includeAware = aware;
		return this;
	}	
	/**
	 * activates the support for namespaces (if a schema is given, then this activated anyhow)
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setNamespaceAware() {
		this.namespaceAware = true;
		return this;
	}	
	/**
	 * sets whether the support for namespaces should be activated (if a schema is given, then this activated anyhow),
	 * default is false 
	 * @param aware - true for namespace support, false otherwise. 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setNamespaceAware(boolean aware) {
		this.namespaceAware = aware;
		return this;
	}
	/**
	 * activates internal validation. If a schema is given, it is deactivated (overrride),
	 * default is false
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setValidating() {
		this.validating = true;
		return this;
	}	
	/**
	 * sets whether internal validation should occur. If a schema is give, it is deactivated (override),
	 * default is false
	 * @param validating - true if internal validation should occur, false otherwise 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setValidating(boolean validating) {
		this.validating = validating;
		return this;
	}

	/**
	 * set a property for the {@link SAXParser}
	 * @param name - the name of the {@link SAXParser} property to set 
	 * @param value - the value to be set 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setParserProperty( String name, Object value){
		parserProperties.put( name, value);
		return this;
	}
	
	/**
	 * set a feature for the {@link SAXParserFactory}
	 * @param name - the feature for the {@link SAXParserFactory} to set 
	 * @param value - {@link Boolean}, so it's activation/deactivation 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setFactoryFeature( String name, boolean value) {
		factoryFeatures.put( name, value);
		return this;
	}
	
	/**
	 * set a feature for the {@link XMLReader}
	 * @param name
	 * @param value
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setReaderFeature( String name, boolean value) {
		readerFeatures.put( name, value);
		return this;
	}
	/**
	 * set a property for the {@link XMLReader}
	 * @param name
	 * @param value
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext setReaderProperty( String name, Object value) {
		readerProperties.put( name, value);
		return this;
	}
	
	
	/**
	 * sets the {@link SchemaFactory} to use
	 * @param schemaFactory - 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext validateWith(SchemaFactory schemaFactory) {
		this.schemaFactory = schemaFactory;
		return this;
	}
	
	/**
	 * sets the {@link Schema} to use 
	 * @param schema
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( Schema schema) {
		this.schema = schema;
		return this;
	}
	
	/**
	 * set the {@link Source} for the {@link Schema}
	 * @param schema
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( Source schema) {
		this.schemaSource = schema;
		return this;
	}	
	/**
	 * sets the {@link Source} array that make up the {@link Schema}
	 * @param schemata - the array of {@link Source} 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( Source... schemata) {
		this.schemaSources = schemata;
		return this;
	}
	
	/**
	 * sets the {@link Schema} from a {@link File}
	 * @param schema - the {@link File} that contains the Schema 
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( File schema) {		
		return schema( new StreamSource( schema));
	}
	
	/**
	 * sets the array of {@link File} that make up the full {@link Schema}
	 * @param schemata
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( File... schemata) {
		Source [] sources = new Source[ schemata.length];
		for (int i = 0; i < schemata.length; i++) {
			sources[i] = new StreamSource( schemata[i]);
		}
		return schema( sources);
	}
	
	/**
	 * sets the input stream that delivers the {@link Schema}
	 * @param schemaStream
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( InputStream schemaStream) {		
		return schema( new StreamSource( schemaStream));
	}	
	/**
	 * sets the array of {@link InputStream} that deliver all {@link Schema}
	 * @param schemata
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( InputStream... schemata) {
		Source [] sources = new Source[ schemata.length];
		for (int i = 0; i < schemata.length; i++) {
			sources[i] = new StreamSource( schemata[i]);
		}
		return schema( sources);
	}
	
	/**
	 * sets the {@link Schema} with a {@link String} containing it
	 * @param contents
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( String contents) {		
		return schema( new StreamSource( new StringReader(contents)));
	}
	/**
	 * sets the {@link Schema} with an Array of {@link String} that make up the full schema
	 * @param schemata
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( String... schemata) {
		Source [] sources = new Source[ schemata.length];
		for (int i = 0; i < schemata.length; i++) {
			sources[i] = new StreamSource( new StringReader(schemata[i]));
		}
		return schema( sources);
	}
		
	/**
	 * sets the schema via the {@link Document} containing it
	 * @param schema
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( Document schema) {		
		return schema(new DOMSource(schema));
	}
	/**
	 * sets the schema with a array of {@link Document} that make it up 
	 * @param schemata
	 * @return - the very same {@link SaxContext}
	 */
	public SaxContext schema( Document... schemata) {
		Source [] sources = new Source[ schemata.length];
		for (int i = 0; i < schemata.length; i++) {
			sources[i] = new DOMSource( schemata[i]);
		}
		return schema( sources);
	}
		
	/**
	 * parse a file (is turned into an input stream) 
	 * parse a {@link File} 
	 * @param file
	 * @throws SaxParserException
	 */
	public void parse( File file) throws SaxParserException {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			parse( stream);
		} catch (FileNotFoundException e) {
			String msg ="cannot open stream from file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new SaxParserException(msg, e);
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					String msg ="cannot close stream from file [" + file.getAbsolutePath() + "]";
					log.warn( msg, e);
				}
			}
		}		
	}
	
	/**
	 * parse an {@link URL}
	 * @param url
	 * @throws SaxParserException
	 */
	public void  parse( URL url) throws SaxParserException {
		parse( url, null);
	}
	
	/**
	 * parse an {@link URL} and use the request properties 
	 * @param url
	 * @param requestProperties
	 * @throws SaxParserException
	 */
	public void  parse(URL url, Map<String, String> requestProperties) throws SaxParserException{
	
			InputStream stream = null;
			try {			
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
						parse( redirect);					
					}
				}
			}
			catch( IOException e) {
				
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
	 * parse from an {@link InputStream}<br/>
	 * the stream must be available throughout the read, i.e. it's not buffered into a temporary file)
	 * @param inputStream
	 * @throws SaxParserException
	 */
	public void parse( InputStream inputStream) throws SaxParserException {		
		parse(  new InputSource( inputStream));
	}
	
	/**
	 * parse from a {@link Reader}
	 * @param reader
	 * @throws SaxParserException
	 */
	public void parse( Reader reader) throws SaxParserException {
		parse( new InputSource( reader));
	}
	
	/**
	 * parse from a {@link String}
	 * @param contents
	 * @throws SaxParserException
	 */
	public void parse( String contents) throws SaxParserException{
		 parse( new StringReader( contents));
	}
	
	/**
	 * prime the parser, i.e. instantiate xmlReader, parser and factory respectively 
	 * @throws SaxParserException
	 */
	private void primeParsing() throws SaxParserException {
		if (xmlReader == null) {
			if (parser == null) {
				// standard factory 
				if (factory == null) {
					factory = SAXParserFactory.newInstance();
					for (Entry<String, Boolean> entry : factoryFeatures.entrySet()) {
						try {
							factory.setFeature( entry.getKey(), entry.getValue());
						} catch (Exception e) {
							String msg = "cannot set factory feature [" + entry.getKey() + "] to [" + entry.getValue() + "]";
							log.warn( msg, e);
						}						
					}
					
					if (includeAware)
						factory.setXIncludeAware(includeAware);
					if (namespaceAware)
						factory.setNamespaceAware(namespaceAware);		
					if (validating)
						factory.setValidating(validating);
					
				}
				// validation - create a schema if required  
				if (schema == null) {
					if (schemaFactory == null) {
						schemaFactory = SchemaFactory.newInstance( schemaLanguage);
						if (schemaResolver != null) {
							schemaFactory.setResourceResolver(schemaResolver);
						}
					}
					if (schemaSource != null) {
						try {
							schema = schemaFactory.newSchema(schemaSource);
						} catch (SAXException e) {
							String msg ="cannot create schema from source";
							log.error(msg, e);
							throw new SaxParserException(msg, e);
						}
					}
					else if (schemaSources != null) {
						try {
							schema = schemaFactory.newSchema(schemaSources);
						} catch (SAXException e) {
							String msg ="cannot create schema from sources";
							log.error(msg, e);
							throw new SaxParserException(msg, e);
						}
					}					
				}
				// activate validation via Schema, so DEACTIVATE factory validation 
				if (schema != null) {					
					factory.setSchema(schema);		
					factory.setNamespaceAware( true);
					factory.setValidating(false);
				} 
				else if (validating) {
					factory.setValidating( true);
				}
				// get the parser 
				try {
					parser = factory.newSAXParser();
					for (Entry<String, Object> entry : parserProperties.entrySet()) {
						parser.setProperty( entry.getKey(), entry.getValue());
					}				
					
				} catch (Exception e) {
					String msg="cannot instantiate SaxParser";
					log.error( msg, e);
					throw new SaxParserException(msg, e);
				}
				// get XMLReader 
				try {
					xmlReader = parser.getXMLReader();
					if (errorHandler == null) {
						errorHandler = new SaxParserErrorHandler( System.err);
					}
					xmlReader.setErrorHandler(errorHandler);
					
					if (resolver != null)
						xmlReader.setEntityResolver(resolver);				
					
					if (schema != null) {						
						xmlReader.setFeature("http://apache.org/xml/features/validation/schema", false);
						xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
					}
					else if (validating) {
						xmlReader.setFeature("http://apache.org/xml/features/validation/schema", true);
						xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
					}
					
					for (Entry<String, Boolean> entry : readerFeatures.entrySet()) {
						xmlReader.setFeature( entry.getKey(), entry.getValue());
					}
					for (Entry<String, Object> entry : readerProperties.entrySet()) {
						xmlReader.setProperty( entry.getKey(), entry.getValue());
					}				
					
				} catch (Exception e) {
					String msg="cannot instantiate XMLReader";
					log.error( msg, e);
					throw new SaxParserException(msg, e);
				}				
			}			
		}
	}
	
	/**
	 * actually do the parsing, from an {@link InputSource}
	 * @param inputSource
	 * @throws SaxParserException
	 */
	public void parse( InputSource inputSource) throws SaxParserException{	
		primeParsing();
		try {
			if (handler != null) {
				xmlReader.setContentHandler(handler);
			}			
			xmlReader.parse(inputSource);
		} catch (SAXException e) {
			String msg ="error while parsing";
			log.error( msg, e);
			throw new SaxParserException( msg, e);
		} catch (IOException e) {
			String msg ="cannot parse input source";
			log.error( msg, e);
			throw new SaxParserException( msg, e);
		}
		
		
	}

	
}
