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
package com.braintribe.codec.dom.plain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.context.CodingContext;
import com.braintribe.codec.dom.DomEncodingContext;
import com.braintribe.utils.xml.XmlTools;

public class DomMapCodec<K, V> implements Codec<Map<K, V>, Element> {
	private String parentTagName = "map";
	private Codec<K, Element> domKeyCodec;
	private Codec<V, Element> domValueCodec;

	public DomMapCodec() {
	}

	public DomMapCodec(String parentTagName) {
		this.parentTagName = parentTagName;
	}

	@Configurable @Required
	public void setDomKeyCodec(Codec<K, Element> domKeyCodec) {
		this.domKeyCodec = domKeyCodec;
	}

	@Configurable @Required
	public void setDomValueCodec(Codec<V, Element> domValueCodec) {
		this.domValueCodec = domValueCodec;
	}

	@Override
	public Map<K, V> decode(Element element) throws CodecException {
		String elementTagName = element.getTagName();
		if (parentTagName != null && !parentTagName.equals(elementTagName))
			throw new CodecException("tag name " + parentTagName + " expected and found " + elementTagName);

		Map<K, V> map = new HashMap<K, V>();

		Iterator<Element> it = XmlTools.getElementIterator(element, "entry");

		while (it.hasNext()) {
			Element entryElement = it.next();
			Element keyElement = XmlTools.getFirstElement(entryElement, "key");
			Element valueElement = XmlTools.getFirstElement(entryElement, "value");

			Element keyValueElement = XmlTools.getFirstElement(keyElement, null);
			Element valueValueElement = XmlTools.getFirstElement(valueElement, null);

			K key = domKeyCodec.decode(keyValueElement);
			V value = domValueCodec.decode(valueValueElement);

			map.put(key, value);
		}

		return map;
	}

	@Override
	public Element encode(Map<K, V> map) throws CodecException {
		if (parentTagName == null)
			throw new CodecException("parentTagName cannot be null when encoding");

		DomEncodingContext ctx = CodingContext.get();
		Document document = ctx.getDocument();

		Element element = document.createElement(parentTagName);

		for (Map.Entry<K, V> entry: map.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();

			Element keyElement = document.createElement("key");
			Element valueElement = document.createElement("value");

			keyElement.appendChild(domKeyCodec.encode(key));
			valueElement.appendChild(domValueCodec.encode(value));

			Element entryElement = document.createElement("entry");
			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);
			element.appendChild(entryElement);
		}

		return element;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Map<K, V>> getValueClass() {
		return (Class)Map.class;
	}
}
