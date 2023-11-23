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
package com.braintribe.xml.stax.parser.experts;

import java.util.Map;

import org.xml.sax.Attributes;

import com.braintribe.codec.Codec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;


public class ComplexEntityExpert extends AbstractComplexEntityExpert {
	
	public ComplexEntityExpert(EntityType<GenericEntity> type, String property, Map<String, Codec<GenericEntity, String>> codecs) {
		this.type = type;
		this.property = property;
		this.codecs = codecs;
	}
	
	public ComplexEntityExpert(String signature, String property, Map<String, Codec<GenericEntity, String>> codecs) {
		this.type = GMF.getTypeReflection().getEntityType(signature);
		this.property = property;
		this.codecs = codecs;
	}
	
	@Override
	public EntityType<GenericEntity> getType() {
		return type;
	}
	public void setType(EntityType<GenericEntity> type) {
		this.type = type;
	}

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts)  {
		if (property == null)
			property = qName;
	}

	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName) {		
		if (parent != null)
			parent.attach( this);
	}


	
	
	
}
