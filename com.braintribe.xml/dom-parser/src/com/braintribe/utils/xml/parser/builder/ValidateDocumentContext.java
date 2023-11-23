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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * builder context for XML validation <br/><br/>
 * note:<br/>
 * if you pass the schema as documents, the {@link DomParser} (actually the {@link LoadDocumentContext}) must be name space aware otherwise, the schema creation will fail with <b>org.xml.sax.SAXParseException; s4s-elt-schema-ns</b><br/>
 * <br/>
 * <b>not all <b>from</b> functions can be used with all <b>makeItSo</b> functions.</b> <br/>
 * {@link #from(Document)} and {@link #from(Node)} are only compatible with {@link #makeItSo()}.<br/>
 * @author Pit
 *
 */
public class ValidateDocumentContext {
	private static Logger log = Logger.getLogger(ValidateDocumentContext.class);
	
	private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	private SchemaFactory schemaFactory;
	private Schema schema;
	private Validator validator;
	private ErrorHandler errorHandler;
	private Source xmlSource;
	private LSResourceResolver resolver;
	private Map<String, Boolean> features;
	private Map<String, Object> properties;
	
	/**
	 * 
	 */
	public ValidateDocumentContext() {
		schemaFactory = SchemaFactory.newInstance( schemaLanguage);
	}
	
	/**
	 * @param factory
	 */
	public ValidateDocumentContext( SchemaFactory factory) {
		schemaFactory = factory;
	}
	
	/**
	 * set the xml as {@link Source} from a StreamSource 
	 * @param source - the {@link StreamSource} 
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from(StreamSource source) {
		this.xmlSource = source;
		return this;
	}
	
	/**
	 * set the xml as {@link Source} from a {@link InputStream}
	 * @param stream - the {@link InputStream}
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from( InputStream stream) {
		this.xmlSource = new StreamSource( stream);
		return this;
	}
	
	/**
	 * set the xml as {@link Source} from a {@link File}
	 * @param file - the {@link File}
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from( File file) {
		this.xmlSource = new StreamSource( file);
		return this;
	}
	
	/**
	 * set the xml as {@link Source} from a {@link String}
	 * @param str - the string with the contents
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from( String str) {
		this.xmlSource = new StreamSource( new StringReader( str));
		return this;
	}
	
	/**
	 * set the xml as a {@link Source} from a {@link Document} </br>
	 * <b>YOU CANNOT USE THIS FEATURE IF YOU WANT TO HAVE AN OUTPUT DURING VALIDATION</b>.<br/>
	 * A <b>DomSource</b> cannot be used as input if the output is a StreamResult. In that case, use one of the other
	 * method that use a <b>StreamSource</b>, such as {@link #from(File)}, {@link #from(InputStream)}, {@link #from(StreamSource)}, {@link #from(String)} 
	 * @param document - the {@link Document}
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from( Document document) {
		this.xmlSource = new DOMSource( document);
		return this;
	}
	/**
	 * set the xml (partial) as a {@link Source} from a {@link Node}<br/>
	 * <b>YOU CANNOT USE THIS FEATURE IF YOU WANT TO HAVE AN OUTPUT DURING VALIDATION</b>.<br/>
	 * A <b>DomSource</b> cannot be used as input if the output is a StreamResult. In that case, use one of the other
	 * method that use a <b>StreamSource</b>, such as {@link #from(File)}, {@link #from(InputStream)}, {@link #from(StreamSource)}, {@link #from(String)} 
	 * @param node - the {@link Node}
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext from( Node node) {
		this.xmlSource = new DOMSource( node);
		return this;
	}
	
	/**
	 * set a feature the {@link Validator}
	 * @param name - the name of the feature 
	 * @param value - the value for the feature 
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext feature( String name, boolean value) {
		if (features == null) {
			features = new HashMap<String, Boolean>();
		}
		features.put(name, value);
		return this;
	}
	
	/**
	 * pass a full set of features
	 * @param features - a {@link Map} of {@link String} to {@link String}, ie. name value pairse
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext feature( Map<String, Boolean> features) {
		this.features = features;
		return this;
	}
	/**
	 * set a feature the {@link Validator}
	 * @param name - the name of the feature 
	 * @param value - the value for the feature 
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext property( String name, Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(name, value);
		return this;
	}
	
	/**
	 * pass a full set of features
	 * @param properties - a {@link Map} of {@link String} to {@link String}, ie. name value pairse
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext property( Map<String, Object> properties) {
		this.properties = properties;
		return this;
	}

	
	/**
	 * set the {@link Schema} directly 
	 * @param schema - the {@link Schema} to use for validation 
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext schema( Schema schema) {
		this.schema = schema;
		return this;
	}

	/**
	 * set the schema from an Array of schema  
	 * @param schemata - {@link Array} of {@link Schema}
	 * @return
	 * @throws DomParserException
	 */
	public ValidateDocumentContext schema( Source [] schemata) throws DomParserException {
		try {
			this.schema = schemaFactory.newSchema(schemata);
		} catch (SAXException e) {
			String msg = "cannot build schema";
			log.error( msg, e);
			throw new DomParserException(msg, e);
		}
		return this;
	}
	
	/**
	 * set the {@link Schema} from a {@link Source}
	 * @param schema - the {@link Source}
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException - if anything goes wrong
	 */
	public ValidateDocumentContext schema( Source schema) throws DomParserException{
		try {
			this.schema = schemaFactory.newSchema(schema);
		} catch (SAXException e) {
			String msg = "cannot build schema";
			log.error( msg, e);
			throw new DomParserException(msg, e);
		}
		return this;
	}
	
	/**
	 * build the schema from an {@link Array} of {@link InputStream}
	 * @param streams - the {@link Array} of {@link InputStream}
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException
	 */
	public ValidateDocumentContext schema( InputStream [] streams) throws DomParserException {
		if (streams == null || streams.length == 0) {
			String msg = "no valid input data passed";
			log.error( msg);
			throw new DomParserException( msg);
		}
		Source [] sources = new Source[ streams.length];
		for (int i = 0; i < streams.length; i++) {
			sources[i] = new StreamSource( streams[i]);
		}
		return schema( sources);
	}
	
	/**
	 * build the {@link Schema} from an {@link InputStream} 
	 * @param stream - the {@link InputStream}
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException - if anything goes wrong
	 */
	public ValidateDocumentContext schema( InputStream stream) throws DomParserException {
		return schema( new StreamSource( stream));
	}
	
	/**
	 * set the {@link Schema} from a {@link File}
	 * @param file - the {@link File} to get the Schema from
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException - if anything goes wrong 
	 */
	public ValidateDocumentContext schema( File file) throws DomParserException {
		return schema( new StreamSource( file));
	}
	
	/**
	 * sets the {@link Schema} from an {@link Array} of {@link File}
	 * @param files - the {@link Array} of {@link File} to use 
	 * @return - {@link ValidateDocumentContext}
	 * @throws DomParserException - if anything goes wrong
	 */
	public ValidateDocumentContext schema( File [] files) throws DomParserException {
		if (files == null || files.length == 0) {
			String msg = "no valid input data passed";
			log.error( msg);
			throw new DomParserException( msg);
		}
		Source [] sources = new Source[ files.length];
		for (int i = 0; i < files.length; i++) {
			sources[i] = new StreamSource( files[i]);
		}
		return schema( sources);
	}
	
	/**
	 * set the schema from the string 
	 * @param contents - the {@link Schema} as a string 
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException
	 */
	public ValidateDocumentContext schema( String contents) throws DomParserException {
		return schema( new StreamSource( new StringReader(contents)));
	}
	/**
	 * set the {@link Schema} from an {@link Array} of {@link String}
	 * @param contents - the {@link Array} of {@link String} to build the schema from 
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException
	 */
	public ValidateDocumentContext schema( String [] contents) throws DomParserException {
		if (contents == null || contents.length == 0) {
			String msg = "no valid input data passed";
			log.error( msg);
			throw new DomParserException( msg);
		}
		Source [] sources = new Source[ contents.length];
		for (int i = 0; i < contents.length; i++) {
			sources[i] = new StreamSource( new StringReader(contents[i]));
		}
		return schema( sources);
	}
	
	/**
	 * set the {@link Schema} from a {@link Document}<br/>
	 * <i>check the note above for more information</i>
	 * @param document - the {@link Document}
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException
	 */
	public ValidateDocumentContext schema( Document document) throws DomParserException {
		return schema( new DOMSource( document));
	}
	/**
	 * set the {@link Schema} from a {@link Array} of {@link Document}
	 * <i>check the note above for more information</i>
	 * @param documents - the {@link Array} of {@link Document}
	 * @return - the {@link ValidateDocumentContext}
	 * @throws DomParserException - if anything goes wrong
	 */
	public ValidateDocumentContext schema( Document [] documents) throws DomParserException {
		if (documents == null || documents.length == 0) {
			String msg = "no valid input data passed";
			log.error( msg);
			throw new DomParserException( msg);
		}
		Source [] sources = new Source[ documents.length];
		for (int i = 0; i < documents.length; i++) {
			sources[i] = new DOMSource( documents[i]);
		}
		return schema( sources);
	}
	
	public ValidateDocumentContext resolveWith(LSResourceResolver resolver) {
		this.resolver = resolver; 
		return this;
	}
	 
	
	/**
	 * set the {@link Validator} - needs to be fully qualified (supplied with {@link Schema})
	 * @param validator - the {@link Validator} to use 
	 * @return - the {@link ValidateDocumentContext}
	 */
	public ValidateDocumentContext validateWith( Validator validator) {
		this.validator = validator;
		return this;
	}
	
	/**
	 * run the validation and throw an execption if it fails 
	 * @return true if it validates
	 * @throws DomParserException - if validation fails.
	 */
	public boolean makeItSo() throws DomParserException {
		Validator validator = generateValidator();		
		 try {
			validator.validate(xmlSource);
			return true;
		} catch (Exception e) {
			String msg="document doesn't validate";
			log.error( msg,e);
		}
		 return false;
	}
	
	/**
	 * run the validation <br/>
	 * <b>if you use this function, you cannot use {@link #from(Document)} nor {@link #from(Node)}</b><br/>
	 * @param stream - the {@link OutputStream} to write the result to 
	 * @return - true if it validated, false otherwise
	 * @throws DomParserException - if anything goes wrong (NOT WHEN VALIDATION FAILS)
	 */
	public boolean makeItSo( OutputStream stream) throws DomParserException {
		makeItSo();
		return errorHandler.toStream(stream);		
	}
	
	/**
	 * run the validation <br/>
	 * <b>if you use this function, you cannot use {@link #from(Document)} nor {@link #from(Node)}</b><br/>
	 * @param stream - the {@link File} to write the result to 
	 * @return - true if it validated, false otherwise
	 * @throws DomParserException - if anything goes wrong (NOT WHEN VALIDATION FAILS)
	 */
	public boolean makeItSo( File file) throws DomParserException {
		makeItSo();
		return errorHandler.toFile(file);
		
	}
	/**
	 * run the validation<br/>
	 * <b>if you use this function, you cannot use {@link #from(Document)} nor {@link #from(Node)}</b><br/> 
	 * @return - the resulting message from the parser
	 * @throws DomParserException - if anything goes wrong (NOT WHEN VALIDATION FAILS)
	 */
	public String makeItToString() throws DomParserException {
		makeItSo();
		return errorHandler.toString();
	}
		
	/**
	 * generates a validator as customized during the building of the context
	 * @return - the {@link Validator}
	 */
	private Validator generateValidator() {
		Validator validatorToUse = null;
		if (validator == null) 
			validatorToUse = schema.newValidator();
		else
			validatorToUse = validator;
		
		errorHandler = new ErrorHandler();
		validatorToUse.setErrorHandler( errorHandler);
		
		if (resolver != null)
			validatorToUse.setResourceResolver(resolver);
		
		if (features != null) {
			for (Entry<String, Boolean> entry : features.entrySet()) {
				try {
					validatorToUse.setFeature(entry.getKey(), entry.getValue());
				} catch (Exception e) {
					String msg = "feature [" + entry.getKey() + "] with value ["+ entry.getValue() + "] cannot be set";
					log.warn( msg, e);
					continue;
				} 
			}
		}
		if (properties != null) {
			for (Entry<String, Object> entry : properties.entrySet()) {
				try {
					validatorToUse.setProperty(entry.getKey(), entry.getValue());
				} catch (Exception e) {
					String msg = "property [" + entry.getKey() + "] with value ["+ entry.getValue() + "] cannot be set";
					log.warn( msg, e);
					continue;
				} 
			}
		}
		return validatorToUse;
	}
	
	/**
	 * run the validation SILENTLY, i.e. the {@link SAXException} is suppressed<br/>
	 * <b>if you use this function, you cannot use {@link #from(Document)} nor {@link #from(Node)}</b><br/> 
	 * @param result - the {@link Result} to write the output to 
	 * @return - true if it validated, false if it failed. 
	 * @throws DomParserException - if anything other than the validation went wrong.
	 */
	public boolean makeItSo(Result result) throws DomParserException {
		Validator validator = generateValidator();
		 try {
			validator.validate(xmlSource, result);
			 return true;
		} catch (SAXException e) {
			log.warn("validation failed, result's showing it");
			return false;
		} catch (IOException e) {
			String msg="document doesn't validate";
			log.error( msg,e);
			throw new DomParserException( msg, e);
		}		
	}
	
	private enum ErrorQuality {warning, error, fatal}
	
	private class ErrorHandler implements org.xml.sax.ErrorHandler {
		private List<Pair<ErrorQuality, String>> collectedOutput = new ArrayList<>();
		
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			collectedOutput.add( Pair.of( ErrorQuality.warning, exception.getMessage()));
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			collectedOutput.add( Pair.of( ErrorQuality.error, exception.getMessage()));			
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			collectedOutput.add( Pair.of( ErrorQuality.fatal, exception.getMessage()));
		}		
		
		public String toString() {
			StringWriter writer = new StringWriter();
			boolean first = true;
			for (Pair<ErrorQuality, String> pair : collectedOutput) {
				if (first == true)
					first = false;
				else {
					writer.write( System.lineSeparator());
					first = false;
				}
				writer.write( pair.second);
			}
			return writer.toString();
		}
		
		public boolean toStream( OutputStream stream) {
			if (collectedOutput.isEmpty())
				return true;
			boolean first = true;
			try (PrintWriter writer = new PrintWriter(stream, true)) {
				for (Pair<ErrorQuality, String> pair : collectedOutput) {
					if (first == true)
						first = false;
					else {
						writer.write( System.lineSeparator());
						first = false;
					}
					writer.write( pair.second);
				}
			}
			return false;
		}
		
		public boolean toFile( File file) {
			if (collectedOutput.isEmpty())
				return true;
			try (OutputStream out = new FileOutputStream(file)) {
				toStream( out);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}			
			return false;
		}
	}
}
