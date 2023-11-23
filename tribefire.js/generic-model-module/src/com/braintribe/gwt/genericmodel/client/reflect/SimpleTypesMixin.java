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
package com.braintribe.gwt.genericmodel.client.reflect;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.BooleanType;
import com.braintribe.model.generic.reflection.DateType;
import com.braintribe.model.generic.reflection.DecimalType;
import com.braintribe.model.generic.reflection.DoubleType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.FloatType;
import com.braintribe.model.generic.reflection.IntegerType;
import com.braintribe.model.generic.reflection.LongType;
import com.braintribe.model.generic.reflection.StringType;

import jsinterop.annotations.JsProperty;

public interface SimpleTypesMixin {
	
	@JsProperty(name="string", namespace = GmCoreApiInteropNamespaces.type)
	StringType stringType = EssentialTypes.TYPE_STRING;
	
	@JsProperty(name="boolean", namespace = GmCoreApiInteropNamespaces.type)
	BooleanType booleanType = EssentialTypes.TYPE_BOOLEAN;
	
	@JsProperty(name="decimal", namespace = GmCoreApiInteropNamespaces.type)
	DecimalType decimalType = EssentialTypes.TYPE_DECIMAL;
	
	@JsProperty(name="double", namespace = GmCoreApiInteropNamespaces.type)
	DoubleType doubleType = EssentialTypes.TYPE_DOUBLE;
	
	@JsProperty(name="float", namespace = GmCoreApiInteropNamespaces.type)
	FloatType floatType = EssentialTypes.TYPE_FLOAT;
	
	@JsProperty(name="integer", namespace = GmCoreApiInteropNamespaces.type)
	IntegerType integerType = EssentialTypes.TYPE_INTEGER;
	
	@JsProperty(name="long", namespace = GmCoreApiInteropNamespaces.type)
	LongType longType = EssentialTypes.TYPE_LONG;
	
	@JsProperty(name="base", namespace = GmCoreApiInteropNamespaces.type)
	BaseType baseType = EssentialTypes.TYPE_OBJECT;
	
	@JsProperty(name="date", namespace = GmCoreApiInteropNamespaces.type)
	DateType dateType = EssentialTypes.TYPE_DATE;
	
}