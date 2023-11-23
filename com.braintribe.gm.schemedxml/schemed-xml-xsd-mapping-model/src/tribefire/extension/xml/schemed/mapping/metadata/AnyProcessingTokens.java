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
package tribefire.extension.xml.schemed.mapping.metadata;

public interface AnyProcessingTokens {
	
	static final String MD_ANY_TYPE = "!AnyType";
	static final String MD_ANY_VALUE = "!any.value";
	static final String MD_ANY_NAME = "!any.name";
	static final String MD_ANY_ATTRIBUTES_TYPE = "!AnyAttributes";
	
	static final String MD_ANY_PROPERTIES = "!any.properties";
	static final String MD_ANY_ATTRIBUTES = "!any.attributes";
	
	static final String TYPE_ANY_TYPE = "AnyType";
	static final String TYPE_ANY_ATTRIBUTE_TYPE = "AnyAttribute";
	static final String TYPE_ANY_PROPERTIES = "properties";
	static final String TYPE_ANY_ATTRIBUTES = "attributes";
	static final String TYPE_ANY_NAME = "name";
	static final String TYPE_ANY_VALUE = "value";
	
	static final String COM_BRAINTRIBE_XML = "com.braintribe.xml";
		
	static final String ANY_TYPE_SIGNATURE = COM_BRAINTRIBE_XML + "." + TYPE_ANY_TYPE;
}
