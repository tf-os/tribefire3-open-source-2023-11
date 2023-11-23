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
package com.braintribe.codec.dom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.codec.CodecException;

public class DomListCodec<T> implements DomCodec<List<T>> {
	private String tagName;
	private String childTagName;
	private DomCodec<T> codec;
	private boolean ignoreTagName = false;
	
	public DomListCodec(DomCodec<T> codec) {
		this(codec, null, null);
	}
	
	public DomListCodec(DomCodec<T> codec, String tagName, String childTagName) {
		super();
		this.tagName = tagName;
		this.childTagName = childTagName;
		this.codec = codec;
	}

	public DomListCodec(DomCodec<T> codec, String tagName, String childTagName, boolean ignoreTagName) {
		super();
		this.tagName = tagName;
		this.childTagName = childTagName;
		this.codec = codec;
		this.ignoreTagName = ignoreTagName;
	}

	@Override
	public List<T> decode(Element element) throws CodecException {
		List<T> objects = new ArrayList<T>();
		decode(element, objects);
		return objects;
	}
	
	public void decode(Element element, List<T> into) throws CodecException {
		if (!ignoreTagName && tagName != null && !tagName.equals(element.getTagName()))
			throw new IllegalArgumentException("expected tag name "+tagName+", found "+element.getTagName());
		
		Node node = element.getFirstChild();
		while (node != null) {
			if (node instanceof Element) {
				Element childElement = (Element)node;
				if (childTagName == null || childTagName.equals(childElement.getTagName())) {
					T object = codec.decode(childElement);
					into.add(object);
				}
			}
			node = node.getNextSibling();
		}
	}

	public Element encode(Document doc, T[] list) throws CodecException {
		return encode(doc, Arrays.asList(list));
	}
	
	@Override
	public Element encode(Document doc, List<T> list) throws CodecException {
		String name = tagName;
		if (name == null) name = "List";
		
		Element listElement = doc.createElement(name);
		
		for (T obj : list) {
			Element childElement = codec.encode(doc, obj);
			listElement.appendChild(childElement);
		}
		
		return listElement;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<List<T>> getValueClass() {
	    return (Class)List.class;
	}
}
