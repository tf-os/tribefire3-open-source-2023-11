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

import org.xml.sax.Attributes;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class EnumValueExpert extends AbstractContentExpert {
	private String enumClass;
	
	public EnumValueExpert( String enumClass, String property) {
		this.enumClass = enumClass;
		this.property = property;
	}

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts) {
		if (property == null) {
			property = qName;
		}
	}

	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName)  {
		parent.attach(this);
	}

	@Override
	public void attach(ContentExpert child) {	
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getPayload() {
		String value = buffer.toString().trim();
		try {
			return Enum.valueOf((Class<? extends Enum>)Class.forName(enumClass), value);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("enum [" + enumClass + "] cannot be found");
		}
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
