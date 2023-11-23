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
package com.braintribe.gwt.genericmodel.client.codec.dom4;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.GmAsyncCodec;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmEncodingContext;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.ProcessingInstruction;
import com.google.gwt.xml.client.XMLParser;

public class GmDomCodec<T> implements Codec<T, Document>, GmAsyncCodec<T, Document> {
	private GenericModelType type;
	private boolean shouldWriteAbsenceInformation = true;
	
	public GmDomCodec(Class<T> valueClass) {
		this.type = GMF.getTypeReflection().getType(valueClass); 
	}
	
	public GmDomCodec(GenericModelType type) {
		this.type = type;
	}
	
	public GmDomCodec() {
		type = GMF.getTypeReflection().getBaseType();
	}
	
	@Configurable
	public void setShouldWriteAbsenceInformation(boolean shouldWriteAbsenceInformation) {
		this.shouldWriteAbsenceInformation = shouldWriteAbsenceInformation;
	}
	
	@Override
	public Class<T> getValueClass() {
		return (Class<T>) type.getJavaType();
	}

	@Override
	public T decode(Document encodedValue) throws CodecException {
		DomDecodingContextImpl context = new DomDecodingContextImpl(encodedValue);
		return (T)context.decodeGmData(encodedValue.getDocumentElement());
	}

	@Override
	public Document encode(T value, GmEncodingContext encodingContext) throws CodecException {
		Document document =  XMLParser.createDocument();
		
		ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"4\"");
 		document.appendChild(gmXmlPi);
		
		DomEncodingContextImpl context = new DomEncodingContextImpl(document, GMF.getTypeReflection(), encodingContext);
		context.setWriteAbsenceInformation(shouldWriteAbsenceInformation);
		context.encodeGmData(value);
		
		return document;
	}
	
	@Override
	public <T1 extends T> T1 decode(Document encodedValue, GmDecodingContext context) throws CodecException {
		DomDecodingContextImpl decodingContext = new DomDecodingContextImpl(encodedValue, context);
		return (T1)decodingContext.decodeGmData(encodedValue.getDocumentElement());
	}
	
	@Override
	public <T1 extends T> Future<T1> decodeAsync(Document encodedValue, GmDecodingContext context) {
		DomDecodingContextImpl decodingContext = new DomDecodingContextImpl(encodedValue, context);
		return (Future<T1>) decodingContext.decodeGmDataAsync(encodedValue);
	}
	
	@Override
	public Future<Document> encodeAsync(T value, GmEncodingContext encodingContext) {
		Document document =  XMLParser.createDocument();
		
		ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"4\"");
 		document.appendChild(gmXmlPi);
		
		DomEncodingContextImpl context = new DomEncodingContextImpl(document, GMF.getTypeReflection(), encodingContext);
		context.setWriteAbsenceInformation(shouldWriteAbsenceInformation);
		return context.encodeGmDataAsync(value);
	}
	
	@Override
	public Document encode(T value) throws CodecException {
		return encode(value, null);
	}
	
}

