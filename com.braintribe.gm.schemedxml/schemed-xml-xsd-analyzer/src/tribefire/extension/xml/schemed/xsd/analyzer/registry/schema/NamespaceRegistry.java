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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.schema;

import java.util.HashMap;
import java.util.Map;

import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public class NamespaceRegistry {
	private Map<String,String> prefixToNamespaceUriMap = new HashMap<>();
	private Map<String,String> namespaceUriToPrefixMap = new HashMap<>();
	
	public static NamespaceRegistry createFromSchema( Schema schema) {
		NamespaceRegistry registry = new NamespaceRegistry();
		for (Namespace namespace : schema.getNamespaces()) {
			registry.prefixToNamespaceUriMap.put( namespace.getPrefix(), namespace.getUri());
			registry.namespaceUriToPrefixMap.put(namespace.getUri(), namespace.getPrefix());
		}
		return registry;
	}
	
	public String getPrefixForNamespaceUri( String uri) {
		return namespaceUriToPrefixMap.get(uri);
	}
	
	public String getUriForNamespacePrefix( String prefix) {
		return prefixToNamespaceUriMap.get(prefix);
	}
}
