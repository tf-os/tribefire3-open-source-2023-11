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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GmPlatform;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;

/**
 * All the possible {@link SimpleType} instances in form of static fields.
 */
public interface SimpleTypes {

	@JsProperty(name = "STRING")
	final StringType TYPE_STRING = GmPlatform.INSTANCE.getEssentialType(String.class);
	@JsProperty(name = "FLOAT")
	final FloatType TYPE_FLOAT = GmPlatform.INSTANCE.getEssentialType(Float.class);
	@JsProperty(name = "DOUBLE")
	final DoubleType TYPE_DOUBLE = GmPlatform.INSTANCE.getEssentialType(Double.class);
	@JsProperty(name = "BOOLEAN")
	final BooleanType TYPE_BOOLEAN = GmPlatform.INSTANCE.getEssentialType(Boolean.class);
	@JsProperty(name = "INTEGER")
	final IntegerType TYPE_INTEGER = GmPlatform.INSTANCE.getEssentialType(Integer.class);
	@JsProperty(name = "LONG")
	final LongType TYPE_LONG = GmPlatform.INSTANCE.getEssentialType(Long.class);
	@JsProperty(name = "DATE")
	final DateType TYPE_DATE = GmPlatform.INSTANCE.getEssentialType(Date.class);
	@JsProperty(name = "DECIMAL")
	final DecimalType TYPE_DECIMAL = GmPlatform.INSTANCE.getEssentialType(BigDecimal.class);

	// @formatter:off
	@JsIgnore
	final List<SimpleType> TYPES_SIMPLE = Arrays.asList(
			TYPE_STRING,
			TYPE_FLOAT,
			TYPE_DOUBLE,
			TYPE_BOOLEAN,
			TYPE_INTEGER,
			TYPE_LONG,
			TYPE_DATE,
			TYPE_DECIMAL
	);
	// @formatter:on

}
