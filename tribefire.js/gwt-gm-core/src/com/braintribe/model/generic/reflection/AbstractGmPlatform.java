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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GmPlatform;
import com.braintribe.model.generic.reflection.type.BaseTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.ListTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.MapTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.SetTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.BooleanTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.DateTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.DecimalTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.DoubleTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.FloatTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.IntegerTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.LongTypeImpl;
import com.braintribe.model.generic.reflection.type.simple.StringTypeImpl;

/**
 * @author peter.gazdik
 */
public abstract class AbstractGmPlatform implements GmPlatform {

	private static final Map<Class<?>, GenericModelType> essentialTypeMap = new HashMap<>();

	static {
		register(BaseTypeImpl.INSTANCE);

		register(BooleanTypeImpl.INSTANCE);
		register(IntegerTypeImpl.INSTANCE);
		register(LongTypeImpl.INSTANCE);
		register(FloatTypeImpl.INSTANCE);
		register(DoubleTypeImpl.INSTANCE);
		register(DecimalTypeImpl.INSTANCE);
		register(DateTypeImpl.INSTANCE);
		register(StringTypeImpl.INSTANCE);

		register(new ListTypeImpl(BaseTypeImpl.INSTANCE));
		register(new SetTypeImpl(BaseTypeImpl.INSTANCE));
		register(new MapTypeImpl(BaseTypeImpl.INSTANCE, BaseTypeImpl.INSTANCE));

	}

	private static void register(GenericModelType type) {
		essentialTypeMap.put(type.getJavaType(), type);
	}

	@Override
	public <T extends GenericModelType> T getEssentialType(Class<?> javaType) {
		return (T) essentialTypeMap.get(javaType);
	}

}
