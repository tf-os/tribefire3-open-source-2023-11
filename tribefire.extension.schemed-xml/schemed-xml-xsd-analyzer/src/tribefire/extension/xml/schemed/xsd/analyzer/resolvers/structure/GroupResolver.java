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

import java.util.List;

import com.braintribe.model.meta.GmProperty;

import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.commons.AnalyzerCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;

public class GroupResolver {

	public static List<GmProperty> resolve(SchemaMappingContext context, Group group, boolean multiple) {
		Group actualGroup = AnalyzerCommons.retrieveGroup(context, group);
		
		List<GmProperty> propertiesFromSequence = ResolverCommons.processSequence(context, actualGroup, multiple);
		List<GmProperty> propertiesFromAll = ResolverCommons.processAll(context, actualGroup, multiple);
		List<GmProperty> propertiesFromChoice = ResolverCommons.processChoice(context, actualGroup, multiple);
		
		return ResolverCommons.combine( propertiesFromSequence.stream(), propertiesFromAll.stream(), propertiesFromChoice.stream());
					
	}

}
