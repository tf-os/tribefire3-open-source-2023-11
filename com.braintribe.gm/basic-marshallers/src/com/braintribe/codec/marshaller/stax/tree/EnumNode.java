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
import com.braintribe.codec.marshaller.stax.TypeInfo;

public class EnumNode extends ValueStaxNode {
	private TypeInfo typeInfo;
	private Enum<?> value;
	
	public EnumNode(TypeInfo typeInfo, Enum<?> value) {
		super();
		this.typeInfo = typeInfo;
		this.value = value;
	}

	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException, MarshallException {
		writer.write("<e>");
		writer.write(typeInfo.as);
		writer.write('.');
		writer.write(value.toString());
		writer.write("</e>");
	}
	
	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, String propertyName, int indent) throws IOException, MarshallException {
		writer.write("<e p='");
		writer.write(propertyName);
		writer.write("'>");
		writer.write(typeInfo.as);
		writer.write('.');
		writer.write(value.toString());
		writer.write("</e>");
	}
}
