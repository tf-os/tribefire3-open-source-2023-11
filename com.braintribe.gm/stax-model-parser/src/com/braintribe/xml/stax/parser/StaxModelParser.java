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
package com.braintribe.xml.stax.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.logging.Logger;

public class StaxModelParser {
	private static Logger log = Logger.getLogger(StaxModelParser.class);
	private static XMLInputFactory inputFactory;

	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = log.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature "+XMLInputFactory.SUPPORT_DTD+"=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
	}
	
	public <T> T read( File file, ContentHandler<T> handler) throws StaxModelParserException{
		try ( InputStream in = new FileInputStream( file)) {
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
			handler.read(streamReader);
			streamReader.close();
			return handler.getResult();
		} catch (FileNotFoundException e) {
			String msg = "cannot find file [" + file.getAbsolutePath() + "]";
			throw new StaxModelParserException(msg, e);
		} catch (IOException e) {
			String msg = "cannot open input stream to [" + file.getAbsolutePath() + "]";
			throw new StaxModelParserException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "cannot read [" + file.getAbsolutePath() + "]";
			throw new StaxModelParserException(msg, e);
		}
		
	}
	
	public <T> T read( InputStream in, ContentHandler<T> handler) throws StaxModelParserException{
		try  {
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
			handler.read(streamReader);
			streamReader.close();
			return handler.getResult();
		}  catch (XMLStreamException e) {
			String msg = "cannot read input stream";
			throw new StaxModelParserException(msg, e);
		}
		
	}
	
	public <T> T read( String content, ContentHandler<T> handler) throws StaxModelParserException{
		try  {
			StringReader stringReader = new StringReader( content);
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader( stringReader);
			handler.read(streamReader);
			streamReader.close();
			return handler.getResult();
		}  catch (XMLStreamException e) {
			String msg = "cannot read input stream";
			throw new StaxModelParserException(msg, e);
		}
		
	}
}
