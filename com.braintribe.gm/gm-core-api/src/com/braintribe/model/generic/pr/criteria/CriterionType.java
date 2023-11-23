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
package com.braintribe.model.generic.pr.criteria;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.annotation.GmSystemInterface;

import jsinterop.annotations.JsType;

/**
 * 
 */
@GmSystemInterface
@JsType (namespace = GmCoreApiInteropNamespaces.tc)
public enum CriterionType {
	BASIC, 
	MAP, 
	ROOT, 
	ENTITY, 
	PROPERTY, 
	LIST_ELEMENT, 
	SET_ELEMENT, 
	MAP_ENTRY, 
	MAP_KEY, 
	MAP_VALUE, 
	CONJUNCTION, 
	DISJUNCTION, 
	NEGATION,
	JOKER, 
	VALUE_CONDITION, 
	TYPE_CONDITION, 
	RECURSION, 
	PROPERTY_TYPE, 
	PATTERN,
	ACL,
	PLACEHOLDER,
}
