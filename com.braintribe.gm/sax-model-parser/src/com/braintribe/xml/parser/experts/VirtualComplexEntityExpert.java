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

public class VirtualComplexEntityExpert extends AbstractComplexEntityExpert {

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts) throws SAXException {
		this.instance = parent.getInstance();
		this.type = parent.getType();
		if (type == null) {
			type = instance.entityType();
		}
		
		if (property == null)
			property = qName;
	}

	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName) throws SAXException {
	}

	
}
