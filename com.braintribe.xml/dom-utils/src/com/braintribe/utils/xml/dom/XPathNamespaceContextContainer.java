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
package com.braintribe.utils.xml.dom;

import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * a class to implement a javax.xml.NamespaceContext builds the class from a HashMap with
 * namespace-name,namespace-location
 * 
 * @author Pit
 * 
 */
public class XPathNamespaceContextContainer implements NamespaceContext {
	private Map<String, String> namespaces = null;

	/**
	 * creates a new instance
	 * 
	 * @param namespaces
	 *            the hash map to generate the info of
	 */
	public XPathNamespaceContextContainer(final Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(final String prefix) {
		final String result = this.namespaces.get(prefix);
		if (result == null) {
			return XMLConstants.NULL_NS_URI;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(final String arg0) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(final String arg0) {
		throw new UnsupportedOperationException();
	}
}
