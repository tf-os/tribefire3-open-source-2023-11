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
package com.braintribe.gwt.codec.dom.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.xml.client.Element;

public class DomMapCodec<K, V> implements Codec<Map<K, V>, Element> {
	private String parentTagName = "map";
	private Codec<K, Element> domKeyCodec;
	private Codec<V, Element> domValueCodec;
	private String keyTag = "key";
	private String valueTag = "value";
	private String entryTag = "entry";

	public DomMapCodec() {
	}
	
	public DomMapCodec(String parentTagName) {
		this.parentTagName = parentTagName;
	}
	
	/**
	 * Configures whether to use short notation when building XML. Useful for saving space.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseShortNotation(boolean useShortNotation) {
		if (useShortNotation) {
			keyTag = "k";
			valueTag = "v";
			entryTag = "e";
		}
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

		Iterator<Element> it = new ElementIterator(element);
		
		while (it.hasNext()) {
			Element entryElement = it.next();
			Element keyElement = DomCodecUtil.getFirstChildElement(entryElement, keyTag);
			Element valueElement = DomCodecUtil.getFirstChildElement(entryElement, valueTag);

			Element keyValueElement = DomCodecUtil.getFirstChildElement(keyElement, null);
			Element valueValueElement = DomCodecUtil.getFirstChildElement(valueElement, null);
			
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
		
		Element element = DomCodecUtil.createElement(parentTagName);
		
		for (Map.Entry<K, V> entry: map.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			
			Element keyElement = DomCodecUtil.createElement(keyTag);
			Element valueElement = DomCodecUtil.createElement(valueTag);
			
			keyElement.appendChild(domKeyCodec.encode(key));
			valueElement.appendChild(domValueCodec.encode(value));

			Element entryElement = DomCodecUtil.createElement(entryTag);
			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);
			element.appendChild(entryElement);
		}
		
		return element;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<Map<K, V>> getValueClass() {
		return (Class) Map.class;
	}
}
