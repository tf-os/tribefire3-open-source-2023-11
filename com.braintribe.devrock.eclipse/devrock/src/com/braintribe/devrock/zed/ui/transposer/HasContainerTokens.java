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
package com.braintribe.devrock.zed.ui.transposer;

/**
 * containers are named, and as they need to be found by their associated issues 
 * (surplusMethods et al), they must be tokenized to allow for association via
 * their names
 * @author pit
 */
public interface HasContainerTokens {
	String ANNOTATIONS = "annotations";
	String SUPER_TYPES = "super types";
	String DERIVED_TYPES = "derived types";	
	String IMPLEMENTED_INTERFACES = "implemented interfaces";
	String FIELDS = "fields";
	String METHODS = "methods";
	String SUPER_INTERFACES = "super interfaces";
	String IMPLEMENTING_TYPES = "implementing types";
	String DERIVING_TYPES = "deriving types";
	String TEMPLATE_PARAMETERS = "template parameters";
	String THROWN_EXCEPTIONS = "thrown exceptions";
	String TYPE_REFERENCES_IN_BODY = "type references in body";
	String RETURN_TYPE = "return type";
	String ARGUMENT_TYPES = "argument types";
	String VALUES = "values";
	
}
