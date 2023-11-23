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
package com.braintribe.model.generic.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GmPlatform;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;

/**
 * All the possible essential {@link CollectionType} instances in form of static fields. In this case this means that given types are parameterized
 * with Objects (i.e. {@link BaseType}).
 */
public interface EssentialCollectionTypes {

	@JsProperty(name = "LIST")
	final ListType TYPE_LIST = GmPlatform.INSTANCE.getEssentialType(List.class);
	@JsProperty(name = "SET")
	final SetType TYPE_SET = GmPlatform.INSTANCE.getEssentialType(Set.class);
	@JsProperty(name = "MAP")
	final MapType TYPE_MAP = GmPlatform.INSTANCE.getEssentialType(Map.class);

	@JsIgnore
	final List<CollectionType> TYPES_COLLECTION = Arrays.asList(TYPE_LIST, TYPE_SET, TYPE_MAP);

}
