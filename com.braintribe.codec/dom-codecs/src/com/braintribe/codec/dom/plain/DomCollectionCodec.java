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

import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.context.CodingContext;
import com.braintribe.codec.dom.DomEncodingContext;

public abstract class DomCollectionCodec<T, C extends Collection<T>> implements Codec<C, Element> {
	private String parentTagName;
	private String tagName;
	private Codec<T, Element> domCodec;
	private boolean ignoreAlternativeTags = false;

	public DomCollectionCodec(String tagName) {
		this(null, tagName);
	}

	public DomCollectionCodec(String parentTagName, String tagName) {
		this.parentTagName = parentTagName;
		this.tagName = tagName;
	}

	public void setIgnoreAlternativeTags(boolean ignoreAlternativeTags) {
		this.ignoreAlternativeTags = ignoreAlternativeTags;
	}

	public void setDomCodec(Codec<T, Element> domCodec) {
		this.domCodec = domCodec;
	}

	protected abstract C createCollection();

	@Override
	public C decode(Element element) throws CodecException {
		String elementTagName = element.getTagName();
		if (parentTagName != null && !parentTagName.equals(elementTagName))
			throw new CodecException("tag name " + parentTagName + " expected and found " + elementTagName);

		C values = createCollection();
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				Element subElement = (Element)node;
				String subElementTagName = subElement.getTagName();
				if (tagName != null) {
					if (!tagName.equals(subElementTagName)) {
						if (ignoreAlternativeTags) continue;
						else
							throw new CodecException("tag name " + tagName + " expected and found " + subElementTagName);
					}
				}

				T value = domCodec.decode(subElement);
				values.add(value);
			}
		}

		return values;
	}

	@Override
	public Element encode(C collectionOfValues) throws CodecException {
		if (parentTagName == null)
			throw new CodecException("parentTagName cannot be null when encoding");

		DomEncodingContext ctx = CodingContext.get();
		Document document = ctx.getDocument();

		Element element = document.createElement(parentTagName);

		for (T value: collectionOfValues) {
			Element subElement = domCodec.encode(value);
			element.appendChild(subElement);
		}

		return element;
	}
}
