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
package com.braintribe.model.processing.deployment.access.utils;

import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_BOOLEAN;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_DATE;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_DECIMAL;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_DOUBLE;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_FLOAT;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_INTEGER;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_LONG;
import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.TYPE_STRING;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleType;

/**
 * 
 */
public class EntityTools {

	public static boolean isIntegerType(GenericModelType type) {
		if (!(type instanceof SimpleType)) {
			return false;
		}

		String ts = type.getTypeSignature();
		return ts.equals(GenericModelTypeReflection.TYPE_INTEGER.getTypeSignature())
				|| ts.equals(GenericModelTypeReflection.TYPE_LONG.getTypeSignature());
	}

	public static Object getValueForSimpleType(SimpleType pt) {
		if (pt == TYPE_BOOLEAN) {
			return true;
		} else if (pt == TYPE_DATE) {
			return new Date();
		} else if (pt == TYPE_DECIMAL) {
			return new BigDecimal("10000000000000" + System.currentTimeMillis());
		} else if (pt == TYPE_DOUBLE) {
			return (double) System.currentTimeMillis();
		} else if (pt == TYPE_FLOAT) {
			return (float) System.currentTimeMillis();
		} else if (pt == TYPE_INTEGER) {
			return (int) System.currentTimeMillis() % (1 << 30);
		} else if (pt == TYPE_LONG) {
			return System.currentTimeMillis();
		} else if (pt == TYPE_STRING) {
			return "str: " + System.currentTimeMillis();
		}
		return null;
	}

}
