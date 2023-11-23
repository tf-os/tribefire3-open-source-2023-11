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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.braintribe.utils.xml.parser.DomParserException;

/**
 * a context to create documents with it <br/><br/>
 * usage : see test artifact..<br/> 
 * basic use : Document document = DomParser.create().setNamespaceAware().makeItSo();
 * @author Pit
 *
 */
public class CreateDocumentContext extends DocumentContext<CreateDocumentContext> {
	
	/**
	 * default constructor
	 */
	public CreateDocumentContext() {
		super();
		setSelf( this);
	}
	
	/**
	 * constructor with passed {@link DocumentBuilderFactory}
	 * @param factory - the {@link DocumentBuilderFactory} to use
	 */
	public CreateDocumentContext( DocumentBuilderFactory factory) {
		super( factory);
		setSelf(this);
	}
	
	/**
	 * creates a new {@link Document} as a copy of the one passed 
	 * @param in - the {@link Document} to copy 
	 * @return - the copy of the document passed 
	 * @throws DomParserException - if anything goes wrong
	 */
	public Document makeItSo( Document in) throws DomParserException {
		Document out = makeItSo();
		try {
			Node importedNode = out.importNode( in.getDocumentElement(), true);
			out.appendChild( importedNode);
		} 
		catch (DOMException e) {
			throw new DomParserException( "Can't create a copy of '" + in.getDocumentURI() + "'", e);
		}		
		return out;
	}
	
	/**
	 * creates a new {@link Document}
	 * @return - the new {@link Document}
	 * @throws DomParserException - if anything goes wrong
	 */
	public Document makeItSo() throws DomParserException{
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();
			return document;
			
		} catch (Exception e) {
			throw new DomParserException( e);				
		}    	    
	}
}
