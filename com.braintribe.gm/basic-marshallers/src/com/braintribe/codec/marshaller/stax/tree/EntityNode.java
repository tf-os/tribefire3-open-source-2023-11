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
package com.braintribe.codec.marshaller.stax.tree;

import java.io.IOException;
import java.io.Writer;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.PrettinessSupport;
import com.braintribe.model.generic.GenericEntity;

public class EntityNode extends StaxNode {
	private static final char[] endEmptyElement = "'/>".toCharArray();
	private static final char[] endElement = "</E>".toCharArray();
	private static final char[] startElement1 = "<E id='".toCharArray();
	private static final char[] startElement2 = "'>".toCharArray();
	public EntityNode next;
	private String[] propertyNames;
	private ValueStaxNode[] propertyValues;
	private int size;
	private String id;
	private GenericEntity entity;
	
	public EntityNode(GenericEntity entity, String id, String[] propertyNames, ValueStaxNode[] propertyValues, int size) {
		super();
		this.entity = entity;
		this.id = id;
		this.propertyNames = propertyNames;
		this.propertyValues = propertyValues;
		this.size = size;
	}

	
	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException, MarshallException {
		prettinessSupport.writeLinefeed(writer, indent);
		
		writer.write(startElement1);
		StringNode.writeEscapedAttribute(writer, id);
		
		if (size > 0) {
			writer.write(startElement2);
			
			int propertyIndent = indent + 1;
			for (int i = 0; i < size; i++) {
				String propertyName = propertyNames[i];
				ValueStaxNode valueNode = propertyValues[i];
				prettinessSupport.writeLinefeed(writer, propertyIndent);
				valueNode.write(writer, prettinessSupport, propertyName, propertyIndent);
			}
			prettinessSupport.writeLinefeed(writer, indent);
			writer.write(endElement);
		}
		else 
			writer.write(endEmptyElement);
	}
	
	public GenericEntity getEntity() {
		return entity;
	}
}
