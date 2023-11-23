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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;

import tribefire.extension.xml.schemed.model.xsd.List;
import tribefire.extension.xml.schemed.model.xsd.SimpleType;
import tribefire.extension.xml.schemed.model.xsd.SimpleTypeRestriction;
import tribefire.extension.xml.schemed.model.xsd.Union;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.ContextCommons;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.derivations.SimpleTypeRestrictionResolver;

public class SimpleTypeResolver {
	private static Logger log = Logger.getLogger(SimpleTypeResolver.class);
	public static TypeResolverResponse resolve( SchemaMappingContext context, SimpleType simpleType) {
		context.currentEntityStack.push( simpleType);		
		
		try {
			SimpleTypeRestriction restriction = simpleType.getRestriction();
			List list = simpleType.getList();
			Union union = simpleType.getUnion();
			
			String name = simpleType.getName();
			if (name == null) {				
				if (name == null) {  
					name = ContextCommons.getPossibleTypeNameForSimpleType(context);
				}
				name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualPropertyType( name);
			}
			else {
				String overridingName = context.mappingContext.nameMapper.getOverridingName(name, null);
				if (overridingName != null) { 
					name = overridingName;
					log.debug("overriding simple type name [" + name + "] by [" + overridingName + "]");
				}
				
			}
			// intercept here for substitutions
			GmEntityType entityType = context.mappingContext.typeMapper.getSubstitutingType(name); 
			if (entityType != null) {
				TypeResolverResponse response = new TypeResolverResponse();
				response.setGmType(entityType);
				response.setAlreadyAcquired(true);
				response.setActualTypeName("n/a");
				response.setApparentTypeName("n/a");
				return response;
			}
 
			
			
			if (restriction != null) {
				TypeResolverResponse response = SimpleTypeRestrictionResolver.analyze( context, (SimpleTypeRestriction) restriction);
				response.setApparentTypeName(name);
				return response; 
				
			}
			else if (list != null) {
				TypeResolverResponse response = ListResolver.resolve( context, list);
				response.setApparentTypeName(name);
				return response;
				
			}
			else if (union != null) {
				TypeResolverResponse response = UnionResolver.resolve( context, union);
				response.setApparentTypeName(name);
				return response;
			}
			else {
				throw new IllegalStateException("no valid content found for SimpleType [" + name + "] in [" + context.print() + "]");
			}
						
		}
		finally {
			context.currentEntityStack.pop();
		}
	}
}
