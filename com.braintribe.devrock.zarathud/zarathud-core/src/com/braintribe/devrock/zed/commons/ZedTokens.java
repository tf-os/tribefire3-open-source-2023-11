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
package com.braintribe.devrock.zed.commons;

public interface ZedTokens {
	String GETTER_PREFIX = "get";
	String SETTER_PREFIX = "set";
	
	String TRANSIENT_ANNOTATION_SIGNATURE = "com.braintribe.model.generic.annotation.Transient";
	String FORWARD_ANNOTATION_SIGNATURE = "com.braintribe.model.generic.annotation.ForwardDeclaration";
	String GMSYSTEMINTERFACE_ANNOTATION_SIGNATURE = "com.braintribe.model.generic.annotation.GmSystemInterface";
	String ABSTRACT_ANNOTATION_SIGNATURE = "com.braintribe.model.generic.annotation.Abstract";
	
	String [] simpleTypes = {
								"java.lang.String", 
								"java.lang.Integer", "java.lang.Long", 
								"java.lang.Float", "java.lang.Double",
								"java.lang.Boolean",
								"java.lang.Object",
								"java.util.Date", "java.math.BigDecimal",
								"int", "long", "float", "double", "boolean",
	};
	String [] collectionTypes = {
			
								"java.util.List", "java.util.Set","java.util.Map",								
	};
	
}
