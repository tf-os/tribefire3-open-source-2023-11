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
package com.braintribe.gwt.genericmodel.client;

import java.util.function.Function;

import com.braintribe.gwt.genericmodel.client.reflect.AbstractGwtGenericModelTypeReflection;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.AbstractGmPlatform;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.google.gwt.core.client.GWT;

public class GwtGmPlatform extends AbstractGmPlatform {

	private static AbstractGwtGenericModelTypeReflection typeReflection;

	@Override
	public GenericModelTypeReflection getTypeReflection() {
		if (typeReflection == null) {
			typeReflection = GWT.create(GenericModelTypeReflection.class);
		}

		return typeReflection;
	}

	@Override
	public void initialize() {
		typeReflection.initialize();
	}

	@Override
	public boolean isSingleThreaded() {
		return true;
	}

	// @formatter:off
	@Override
	public String newUuid() {
		return S4() + S4() + "-" + S4() + "-4" + 
				S4().substring(0,3) + "-" + S4() + "-" + S4() + S4() + S4();
	}

	public static native String S4() /*-{
		return (((1+Math.random())*0x10000)|0).toString(16).substring(1); 
	}-*/;
	// @formatter:on

	@Override
	public <T extends GenericEntity> void registerStringifier(EntityType<T> baseType, Function<T, String> stringifier) {
		// ignore
	}

	@Override
	public String stringify(GenericEntity entity) {
		return entity.toString();
	}

}
