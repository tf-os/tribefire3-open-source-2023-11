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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.braintribe.logging.Logger;

/**
 * basic {@link DocumentBuilderFactory} based context for creating documents (creation, loading, copying)<br/><br/>
 * documentation about the settings can be found in the documentation of the {@link DocumentBuilderFactory}<br/>
 * @author Pit
 *
 * @param <T> - sub class of the {@link DocumentContext}
 */
public abstract class DocumentContext<T extends DocumentContext<T>> {
	
	private static Logger log = Logger.getLogger(DocumentContext.class);
	
	protected DocumentBuilderFactory builderFactory;
	private T self;
	
	/**
	 * default constructor
	 */
	public DocumentContext() {
		builderFactory = DocumentBuilderFactory.newInstance();
	}

	/**
	 * constructor with {@link DocumentBuilderFactory}
	 * @param factory - the {@link DocumentBuilderFactory} to use
	 */
	public DocumentContext(DocumentBuilderFactory factory) {
		builderFactory = factory;
	}
	/**
	 * set the correct instance to return (the sub class instance)
	 * @param self
	 */
	protected void setSelf( T self) {
		this.self = self;
	}
	
	/**
	 * parametrizes the {@link DocumentBuilderFactory} to be name space aware
	 * @return
	 */
	public T setNamespaceAware() {
		setNamespaceAware( true);
		return self;
	}
	public T setNamespaceAware( boolean value) {
		builderFactory.setNamespaceAware( value);
		return self;
	}
	
	/**
	 * @return
	 */
	public T setCoalescing() {
		setCoalescing( true);
		return self;
	}
	public T setCoalescing( boolean value) {
		builderFactory.setCoalescing(value);
		return self;
	}
	
	/**
	 * @return
	 */
	public T setExpandEntityReferences() {
		setExpandEntityReferences( true);
		return self;
	}
	public T setExpandEntityReferences( boolean value) {
		builderFactory.setExpandEntityReferences(value);
		return self;
	}
	
	/**
	 * @return
	 */
	public T setIgnoringComments() {
		setIgnoringComments( true);
		return self;
	}
	public T setIgnoringComments( boolean value) {
		builderFactory.setIgnoringComments(value);
		return self;
	}
	
	/**
	 * @return
	 */
	public T setIgnoringElementContentWhitespace() {
		setIgnoringElementContentWhitespace( true);
		return self;
	}
	public T setIgnoringElementContentWhitespace( boolean value) {
		builderFactory.setIgnoringElementContentWhitespace(value);
		return self;
	}
		
	/**
	 * sets a generic attribute to the {@link DocumentBuilderFactory}
	 * @param name - name of the attribute 
	 * @param value - value of the attribute 
	 * @return
	 */
	public T setAttribute( String name, String value){
		builderFactory.setAttribute(name, value);
		return self;
	}
	
	public T setFeature( String name, boolean value) {
		try {
			builderFactory.setFeature(name, value);
		} catch (ParserConfigurationException e) {
			String msg ="cannot set feature [" + name + "] to [" + value + "]";
			log.error( msg,e);
		}
		return self;
	}
	

}
