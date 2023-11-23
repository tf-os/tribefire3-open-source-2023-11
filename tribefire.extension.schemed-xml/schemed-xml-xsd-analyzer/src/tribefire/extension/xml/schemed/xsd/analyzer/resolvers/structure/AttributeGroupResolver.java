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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.meta.GmProperty;

import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;

public class AttributeGroupResolver {

	public static List<GmProperty> resolve(SchemaMappingContext context, AttributeGroup group) {
		AttributeGroup actualGroup = AnalyzerCommons.retrieveAttributeGroup(context, group);
		context.currentEntityStack.push(actualGroup);
	
		try {
			List<GmProperty> properties = new ArrayList<>();			
			properties.addAll( ResolverCommons.processAttributes(context, actualGroup));
			properties.addAll( ResolverCommons.processAttributeGroups(context, actualGroup));
			
			return properties;
		}
		finally {
			context.currentEntityStack.pop();
		}		
	}

}
