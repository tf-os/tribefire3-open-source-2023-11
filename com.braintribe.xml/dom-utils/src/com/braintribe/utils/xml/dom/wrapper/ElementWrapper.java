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
package com.braintribe.utils.xml.dom.wrapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.dom.iterator.FilteringElementIterable;
import com.braintribe.utils.xml.dom.iterator.filters.RegExpTagNameFilter;
import com.braintribe.utils.xml.dom.iterator.filters.TagNameFilter;

/**
 * Wrapper for @link org.w3c.dom.Element. Provides more friendly interface for handling XmlTag.
 */
public class ElementWrapper {

	Element element;

	public ElementWrapper(final Element element) {
		super();
		this.element = element;
	}

	public Element getElement() {
		return this.element;
	}

	public Iterable<Element> getChildrenByName(final String childName) {
		return new FilteringElementIterable(this.element, new TagNameFilter(childName));
	}

	public Iterable<Element> getChildrenByRegExName(final String childName) {
		return new FilteringElementIterable(this.element, new RegExpTagNameFilter(childName));
	}

	public Element getChildByName(final String childName) {
		return DomUtils.getElementByPath(this.element, childName, false);
	}

	public String getAttributeOrNull(final String attributeName) {
		return hasAttribute(attributeName) ? getAttribute(attributeName) : null;
	}

	public String getAttribute(final String attributeName) {
		return this.element.getAttribute(attributeName);
	}

	public boolean hasAttribute(final String attributeName) {
		return this.element.hasAttribute(attributeName);
	}

	public List<Attr> listAllAttributes() {
		return listMatchingAttributes(".*");
	}

	public List<Attr> listMatchingAttributes(final String regex) {
		final List<Attr> result = new LinkedList<Attr>();

		final NamedNodeMap nodeMap = this.element.getAttributes();
		for (int i = 0; i < nodeMap.getLength(); i++) {
			final Node node = nodeMap.item(i);
			if (node instanceof Attr && node.getNodeName().matches(regex)) {
				result.add((Attr) node);
			}
		}

		return result;
	}

	public ElementWrapper appendChildren(final Collection<Element> children) {
		for (final Element child : children) {
			appendChild(child);
		}

		return this;
	}

	public void appendChild(final Element child) {
		this.element.appendChild(child);
	}
}
