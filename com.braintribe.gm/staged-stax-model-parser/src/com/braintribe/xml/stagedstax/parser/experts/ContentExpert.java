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
package com.braintribe.xml.stagedstax.parser.experts;

import java.lang.reflect.Array;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * a {@link ContentExpert} is used to handle a tag found in the XML 
 * @author Pit
 *
 */
public interface ContentExpert{
	/**
	 * called when SAX enters a new tag. Can be used to store the property name (if equaling qName)
	 * @param parent - the {@link ContentExpert} that handles the parent (if any)
	 * @param uri - the URI
	 * @param localName - the local name 
	 * @param qName - the qualified name 
	 * @param atts - any {@link Attributes}
	 * @throws SAXException - thrown if anything goes wrong 
	 */
	void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts) ;
	
	/**
	 * called when SAX is exiting the tag. Must be used the get read to be able to return the recored value 
	 * @param parent  - the {@link ContentExpert} that handles the parent.
	 * @param uri - the URI
	 * @param localName - the local name 
	 * @param qName - the fully qualified name 
	 * @throws SAXException 
	 */
	void endElement(ContentExpert parent, String uri, String localName, String qName) ; 
	
	/**
	 * @param ch - an {@link Array} of char that contain PART of the data. May be called multiple times! 
	 * @param start - start of the sequence
	 * @param length - length of the sequence 
	 * @throws SAXException
	 */
	void characters(char[] ch, int start, int length);
	
	/**
	 * set the XML tag in question 
	 * @param tag - the XML tag
	 */
	void setTag( String tag);
	/**
	 * gets the XML tag in question 
	 * @return - the XML tag 
	 */
	String getTag();
	
	/**
	 * get the property (in the model) that the expert is handling (either override via constructor or qName stored) 
	 * @return - the property name 
	 */
	String getProperty();

	/**
	 * if the {@link ContentExpert} has to handle children, this is used to attach the child's value 
	 * @param child - the {@link ContentExpert} that has processed the child tag 
	 */
	void attach( ContentExpert child);
	
	/**
	 * get a representation of the data that the {@link ContentExpert} has retrieved from the tag 
	 * @return - the data as an {@link Object}
	 */
	Object getPayload();
	
	/**
	 * if the {@link ContentExpert} backs  GenericEntity, it needs to return the instance here 
	 * @return - the {@link GenericEntity} instance the {@link ContentExpert} is handling 
	 */
	GenericEntity getInstance();
	/**
	 * if the {@link ContentExpert} backs a GenericEntity, it needs to return the type here 
	 * @return - the {@link EntityType} of the {@link GenericEntity} that the {@link ContentExpert} is handling
	 */
	EntityType<GenericEntity> getType();
	
}
