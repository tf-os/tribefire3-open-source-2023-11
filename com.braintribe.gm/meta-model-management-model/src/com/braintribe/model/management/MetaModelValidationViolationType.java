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
package com.braintribe.model.management;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum MetaModelValidationViolationType implements EnumBase {
	BASIC_METAMODEL_NULL, 
	BASIC_METAMODEL_NAME_INVALID, 
	BASIC_METAMODEL_VERSION_INVALID, 
	BASIC_DECLARINGMODEL_NULL, 
	BASIC_BASETYPE_NULL, 
	BASIC_BASETYPE_INVALID, 
	BASIC_SIMPLETYPES_NULL, 
	BASIC_SIMPLETYPES_CONTAIN_NULL,
	BASIC_SIMPLETYPE_MISSING, 
	BASIC_SIMPLETYPE_UNEXPECTED,  
	BASIC_TYPES_CONTAIN_NULL, 
	BASIC_TYPESIGNATURE_INVALID, 
	BASIC_TYPESIGNATURE_NOT_UNIQUE,
	BASIC_ENTITYTYPE_SUPERTYPES_CONTAIN_NULL, 
	BASIC_ENTITYTYPE_PROPERTIES_CONTAIN_NULL, 
	BASIC_ENTITYTYPE_PROPERTY_NAME_INVALID, 
	BASIC_ENTITYTYPE_PROPERTY_NAME_NOT_UNIQUE, 
	BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NULL, 
	BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NO_MATCH,
	BASIC_ENUMTYPES_CONTAIN_NULL, 
	BASIC_ENUMTYPE_CONSTANT_NAME_INVALID, 
	BASIC_ENUMTYPE_CONSTANT_NAME_NOT_UNIQUE, 
	BASIC_ENUMTYPE_CONSTANTS_CONTAIN_NULL, 
	BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NULL,
	BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NO_MATCH,
	
	TYPE_NULL_HAS_REFERENCES, 
	TYPE_NOT_DECLARED_HAS_REFERENCES, 
	TYPE_HIERARCHY_LOOP, 
	TYPE_NOT_PLAIN_TYPE_WITH_PLAIN_SUPERTYPE, 
	TYPE_PLAIN_TYPE_WITH_MORE_THAN_ONE_PLAIN_SUPERTYPE,
	
	PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_NULL,
	PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_COLLECTION,
	PROPERTY_OVERLAY_NO_SUPERPROPERTY,
	PROPERTY_CONFLICT_SUPERPROPERTY, 
	PROPERTY_INHERITED_FROM_DIFFERRENT_SUPERTYPES_CLASH_BY_TYPE,
	
	PERSISTENCE_INVALID_COUNT_OF_ID_PROPERTIES,
	PERSISTENCE_ID_PROPERTY_NOT_NULLABLE;

	public static final EnumType T = EnumTypes.T(MetaModelValidationViolationType.class);
	
	@Override
	public EnumType type() {
		return T;
	}

}
