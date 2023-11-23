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
package com.braintribe.codec.marshaller.dom.coder.collection;

import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.codec.marshaller.dom.DomEncodingContext;
import com.braintribe.codec.marshaller.dom.coder.DeferredDecoder;
import com.braintribe.codec.marshaller.dom.coder.DeferredEncoder;
import com.braintribe.codec.marshaller.dom.coder.DomCoder;

public abstract class CollectionDomCoder<T, C extends Collection<T>> implements DomCoder<C> {
	private DomCoder<T> elementCoder;
	private boolean returnNullOnEmptyCollection;
	private String tagName;
	
	public CollectionDomCoder(DomCoder<T> elementCoder, String tagName) {
		this.elementCoder = elementCoder;
		this.tagName = tagName;
	}
	public CollectionDomCoder(DomCoder<T> elementCoder, String tagName, boolean returnNullOnEmptyCollection) {
		this.elementCoder = elementCoder;
		this.tagName = tagName;
		this.returnNullOnEmptyCollection = returnNullOnEmptyCollection;
	}

	@Override
	public C decode(DomDecodingContext context, final Element element) throws CodecException {
		if (element.getTagName().equals("n"))
			return null;
		
		final C collection = createCollection();
		
		final int maxDeferred = context.getMaxDeferred();
		
		if (maxDeferred != -1) {
			context.appendDeferredDecoder(new DeferredDecoder() {
				private Node node = element.getFirstChild();
				@Override
				public boolean continueDecode(DomDecodingContext context) throws CodecException {
					node = decode(context, collection, node, maxDeferred);
					return node != null;
				}
			});
		}
		else {
			Node node = element.getFirstChild();
			decode(context, collection, node, -1);
		}
		
		return collection;
	}
	
	private Node decode(DomDecodingContext context, C collection, Node node, int maxDecode) throws CodecException {
		int i = 0;
		while (node != null) {
			
			if (maxDecode != -1 && i == maxDecode) {
				return node;
			}
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				
				T e = elementCoder.decode(context, childElement);
				collection.add(e);
				
				i++;
			}
			
			node = node.getNextSibling();
		}
		
		return null;
	}
	
	protected abstract C createCollection();
	
	@Override
	public Element encode(DomEncodingContext context, C value) throws CodecException {
		if (value == null)
			return context.getDocument().createElement("n");
		
		if (returnNullOnEmptyCollection && value.isEmpty())
			return null;
		
		final Element element = context.getDocument().createElement(tagName);
		final int maxDeferred = context.getMaxDeferred();
		
		if (maxDeferred != -1) {
			final Iterator<T> it = value.iterator();
			
			context.appendDeferredEncoder(new DeferredEncoder() {
				@Override
				public boolean continueEncode(DomEncodingContext context) throws CodecException {
					return encode(context, it, element, maxDeferred);
				}
			});
		}
		else {
			encode(context, value.iterator(), element, -1);
		}
		
		return element;
	}
	
	private boolean encode(DomEncodingContext context, Iterator<T> it, Element element, int maxEncode) throws CodecException {
		int i = 0;
		while (it.hasNext()) {
			
			if (maxEncode != -1 && i == maxEncode) 
				return true;
			
			T e = it.next();
			element.appendChild(elementCoder.encode(context, e));
			
			i++;
		}
		
		return false;
	}
}
