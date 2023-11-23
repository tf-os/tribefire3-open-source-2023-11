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
package com.braintribe.codec.dom.genericmodel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.context.CodingContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

public class EnumCodec<T extends Enum<T>> implements Codec<T, Element> {
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private EnumType enumType;
	
	@Configurable
	public void setEnumType(EnumType enumType) {
		this.enumType = enumType;
	}
	
	@Override
	public Element encode(T value) throws CodecException {
	  EncodingContext ctx = EncodingContext.get();
	  Document document = ctx.getDocument();
	  Element enumElement = document.createElement("enum");
	  Text text = document.createTextNode(value.name());
	  EnumType enumType = typeReflection.getEnumType(value.getDeclaringClass());
	  String typeSignature = enumType.getTypeSignature();
	  ctx.registerRequiredType(enumType);
	  enumElement.setAttribute("type", typeSignature);
	  enumElement.appendChild(text);
	  return enumElement;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T decode(Element encodedValue) throws CodecException {
		try {
			EnumType enumType = this.enumType;
			String typeSignature = encodedValue.getAttribute("type");
			if (typeSignature != null && typeSignature.length() > 0) {
				try {
					enumType = typeReflection.getType(typeSignature);
				} catch (GenericModelException e) {
					DecodingContext decodingContext = CodingContext.get();
					if (decodingContext.getLenience().isTypeLenient()) {
						// unknown type --> ignore it
						return null;
					}
					throw e;
				}
			}
			Class<T> enumClass = (Class<T>)enumType.getJavaType();
			T enumValue = null;
			if(encodedValue.getTextContent() != null && encodedValue.getTextContent().length() > 0) {
				try {
					enumValue = Enum.valueOf(enumClass, encodedValue.getTextContent());
				} catch (IllegalArgumentException e) {
					DecodingContext decodingContext = CodingContext.get();
					if (decodingContext.getLenience().isEnumConstantLenient()) {
						// unknown constant --> ignore it
					} else {
						throw e;
					}
				}
				
			}
			return enumValue;
		} catch (Exception e) {
			throw new CodecException("error while creating enum from string", e);
		}
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<T> getValueClass() {
		return (Class) Enum.class;
	}
}
