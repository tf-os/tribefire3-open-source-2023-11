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

import tribefire.extension.xml.schemed.model.xsd.Extension;
import tribefire.extension.xml.schemed.model.xsd.SimpleContent;
import tribefire.extension.xml.schemed.model.xsd.SimpleContentRestriction;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.derivations.ExtensionResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type.derivations.SimpleContentRestrictionResolver;

public class SimpleContentResolver {
	public static TypeResolverResponse acquireEntityType(SchemaMappingContext context, SimpleContent content) {
		context.currentEntityStack.push( content);
		try {
			SimpleContentRestriction restriction = content.getRestriction();
			Extension extension = content.getExtension();
			if (restriction != null) {			
				return SimpleContentRestrictionResolver.acquireEntityType(context, restriction);
			}
			else if (extension != null) {
				return ExtensionResolver.acquireEntityType(context, extension);
			}
			else {
				throw new IllegalStateException("no valid content found for simple content in [" + context.print() + "]");
			}	
		}
		finally {
			context.currentEntityStack.pop();
		}
	}
}
