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
package com.braintribe.utils.xml.parser.sax;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;

import com.braintribe.utils.xml.parser.sax.builder.SaxContext;

/**
 * the new sax parser wrapper <br/>
 * parsing is done via the {@link XMLReader}!<br/>
 * <br/>
 * there are three ways to deal with validation
 * <ul>
 * <li>no validation should take place: neither set the {@link SaxContext#setValidating()} nor supply a schema in any way </li>
 * <li>the xml should by validated by the data contained in the xml (xsi:schemaLocation): set the {@link SaxContext#setValidating()}</li>
 * <li>the xml should validated by the schema you provide : use the appropriate functions on the {@link SaxContext} to specify the schema</li>
 * </ul>
 * <br/>
 * if you do not specify an {@link ErrorHandler}, a default one is injected, the {@link SaxParserErrorHandler}, hooked to System.err for warning messages.<br/>
 * <br/>
 * there is a standard implementation of the {@link ContentHandler} interface, the {@link SaxParserContentHandler} that can be used as a base for your specific implementation. By default, no ContentHandler is injected.<br/>
 * @author pit
 *
 */
public class SaxParser {

	/**
	 * returns a standard {@link SaxContext}, standard {@link SAXParserFactory} and {@link SAXParser} will be used 
	 * @return - a {@link SaxContext}
	 */
	public static SaxContext parse() {
		return new SaxContext();
	}
		
	/**
	 * returns a {@link SaxContext} with a primed {@link SAXParserFactory} which will be used to get the {@link SAXParser}
	 * @param factory - the {@link SAXParserFactory} to be used
	 * @return - a {@link SaxContext}
	 */
	public static SaxContext parse(SAXParserFactory factory) {		
		return new SaxContext( factory);
	}
	
	/**
	 * returns a {@link SaxContext} with a primed {@link SAXParser} (no {@link SAXParserFactory} will ever be created), 
	 * i.e. no features will be set on it, even if you specify any 
	 * @param parser - the {@link SAXParser} to us
	 * @return - a {@link SaxContext}
	 */
	public static SaxContext parse(SAXParser parser) {
		return new SaxContext( parser);
	}
		
	
}
