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
package com.braintribe.utils.xml.xpath;

import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.braintribe.logging.Logger;

public class MapBasedNamespaceContext implements NamespaceContext {

	protected static Logger logger = Logger.getLogger(MapBasedNamespaceContext.class);

	protected Map<String,String> namespaceMap = null;

	public MapBasedNamespaceContext(Map<String,String> namespaceMap) {
		this.namespaceMap = namespaceMap;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new NullPointerException("Null prefix");
		}
		if (this.namespaceMap != null) {
			String namespace = this.namespaceMap.get(prefix);
			if (logger.isTraceEnabled()) {
				logger.trace("Resolved prefix "+prefix+" to namespace "+namespace);
			}
			if (namespace != null) {
				return namespace;
			}
		}
		if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
			return XMLConstants.XML_NS_URI;
		}
		return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new NullPointerException("Null namespaceURI");
		}
		if (this.namespaceMap != null) {
			for (Map.Entry<String,String> entry : this.namespaceMap.entrySet()) {
				if (entry.getValue().equals(namespaceURI)) {
					String prefix = entry.getKey();
					return prefix;
				}
			}
		}
		if (namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
			return XMLConstants.XML_NS_PREFIX;
		}
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		if (this.namespaceMap != null) {
			return this.namespaceMap.keySet().iterator();
		}
		return null;
	}

}
