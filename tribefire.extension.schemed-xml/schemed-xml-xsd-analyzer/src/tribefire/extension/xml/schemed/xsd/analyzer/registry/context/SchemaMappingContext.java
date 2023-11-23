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
package tribefire.extension.xml.schemed.xsd.analyzer.registry.context;

import java.util.Stack;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;
import tribefire.extension.xml.schemed.xsd.api.analyzer.AnalyzerRegistry;
import tribefire.extension.xml.schemed.xsd.api.analyzer.SchemaRegistry;
import tribefire.extension.xml.schemed.xsd.api.analyzer.naming.QPathGenerator;

public class SchemaMappingContext {
	public MappingContext mappingContext;
		
	public SchemaRegistry registry;
	public Schema schema;
	public AnalyzerRegistry analyzerRegistry;	
	public Stack<SchemaEntity> currentEntityStack = new Stack<>();
	
	public QPathGenerator qpathGenerator;
	
	public SchemaMappingContext( MappingContext context) {
		this.mappingContext = context;
	}
	
	public String print() {
		return schema.getSchemaNamespace().print();
	}
}
