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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.analyzer;

import java.util.HashMap;
import java.util.Map;

import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.xsd.api.analyzer.naming.NamespaceGenerator;

public class BasicNamespaceGenerator implements NamespaceGenerator {
	private static final String INJECTED_NAMESPACE_URI ="InjectedNamespace-";
	private static final String INJECTED_NAMESPACE_PREFIX ="insp-";
	private int count = 0;

	private Map<Namespace, Schema> targetNamespaceToSchemaMap = new HashMap<>();
	private Map<Schema, Namespace> schemaToTargetNamespaceMap = new HashMap<>();

	@Override
	public Namespace createNamespace(Schema schema) {
		Namespace namespace = Namespace.T.create();
		count++;
		namespace.setPrefix( INJECTED_NAMESPACE_PREFIX + count);
		namespace.setUri( INJECTED_NAMESPACE_URI + count);
		targetNamespaceToSchemaMap.put(namespace, schema);
		schemaToTargetNamespaceMap.put(schema, namespace);
		return namespace;
	}
	
	
	
	@Override
	public void acknowledgeNamespace(String namespaceUri, Schema schema) {
		Namespace namespace = Namespace.T.create();
		namespace.setPrefix(namespaceUri);
		namespace.setUri( INJECTED_NAMESPACE_URI + count);
		targetNamespaceToSchemaMap.put(namespace, schema);
		schemaToTargetNamespaceMap.put(schema, namespace);		
	}



	public Map<Namespace, Schema> getTargetNamespaceToSchemaMap() {
		return targetNamespaceToSchemaMap;
	}
	
	public Map<Schema, Namespace> getSchemaToTargetNamespaceMap() {
		return schemaToTargetNamespaceMap;
	}

}
