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
package com.braintribe.codec.marshaller.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.codec.dom.genericmodel.XmlDocumentGmCodec;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.utils.xml.XmlTools;

public class XmlMarshaller implements Marshaller, HasStringCodec {
	
	private GmCodec<Object, Document> codec;
	private GmCodec<Object, String> stringCodec;
	private GenericModelRootDomCodec<Object> defaultCodec;
	
	public XmlMarshaller() {
	}
	
	@Configurable
	public void setCodec(GenericModelRootDomCodec<Object> codec) {
		this.codec = codec;
	}

	@Configurable
	public void setStringCodec(GmCodec<Object, String> stringCodec) {
		this.stringCodec = stringCodec;
	}
	
	/**
	 * This optionally configures the default required types receiver for the default codec.
	 * Note: The default codec is only used if no explicit codec was configured with {@link #setCodec(GenericModelRootDomCodec)} 
	 * @see #getDefaultCodec()
	 * @param requiredTypesReceiver The receiver for the required types
	 */
	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		getDefaultCodec().setRequiredTypesReceiver(requiredTypesReceiver);
	}
	
	public GmCodec<Object, Document> getCodec() {
		if (codec == null) {
			codec = getDefaultCodec();
		}

		return codec;
	}
	
	public GenericModelRootDomCodec<Object> getDefaultCodec() {
		if (defaultCodec == null) {
			defaultCodec = new GenericModelRootDomCodec<Object>();
		}

		return defaultCodec;
	}
	
	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		try {
			Document document = getCodec().encode(value);
			XmlTools.writeXml(document, out, "UTF-8");
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}
	
	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		try {
			Document document = XmlTools.loadXML(in);
			return getCodec().decode(document);
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}
	
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			Document document = getCodec().encode(value, options);
			XmlTools.writeXml(document, out, "UTF-8");
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			Document document = XmlTools.loadXML(in);
			return getCodec().decode(document, options);
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}


	@Override
	public GmCodec<Object, String> getStringCodec() {
		if (stringCodec == null)
			stringCodec = getDefaultStringCodec();
		return stringCodec;
	}

	protected GmCodec<Object, String> getDefaultStringCodec() {
		return new XmlDocumentGmCodec<Object>(getCodec());
	}
}
