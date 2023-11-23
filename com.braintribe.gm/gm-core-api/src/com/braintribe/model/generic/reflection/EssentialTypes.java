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

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface EssentialTypes extends SimpleTypes, EssentialCollectionTypes {

	final BaseType TYPE_OBJECT = BaseType.INSTANCE;

	// @formatter:off
	@JsIgnore
	final List<GenericModelType> TYPES_ESSENTIAL = Arrays.<GenericModelType> asList(
			TYPE_OBJECT, 
			
			TYPE_STRING, 
			TYPE_FLOAT,
			TYPE_DOUBLE,
			TYPE_BOOLEAN,
			TYPE_INTEGER,
			TYPE_LONG,
			TYPE_DATE,
			TYPE_DECIMAL,

			TYPE_LIST,
			TYPE_SET,
			TYPE_MAP
	);
	// @formatter:on

}
