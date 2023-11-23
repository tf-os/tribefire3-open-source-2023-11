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
package com.braintribe.xml.parser.experts;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class PropertyExpert extends AbstractContentExpert {
	private EntityType<GenericEntity> type;
	
	public PropertyExpert(String signature) {
		type = GMF.getTypeReflection().getEntityType(signature);
	}

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts) throws SAXException {
	}

	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName) throws SAXException {
		parent.attach( this);
	}

	@Override
	public void attach(ContentExpert child) {
	}

	@Override
	public Object getPayload() {	
		GenericEntity property = type.create();
		Property nameProperty = type.getProperty("name");
		nameProperty.set(property, tag);
		
		Property valueProperty = type.getProperty( "rawValue");
		if (buffer != null) {
			String bufferValue = buffer.toString().trim();
			if (bufferValue != null) {
				valueProperty.setDirect(property, bufferValue);
			}
		}
		return property;
	}

	@Override
	public GenericEntity getInstance() {	
		return null;
	}

	@Override
	public EntityType<GenericEntity> getType() {	
		return null;
	}
	
	

}
