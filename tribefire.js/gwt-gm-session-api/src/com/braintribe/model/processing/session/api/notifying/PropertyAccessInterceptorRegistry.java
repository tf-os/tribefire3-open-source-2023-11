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
package com.braintribe.model.processing.session.api.notifying;

import java.util.NoSuchElementException;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.processing.session.api.notifying.interceptors.InterceptorIdentification;

import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface PropertyAccessInterceptorRegistry {

	PropertyAccessInterceptorRegistry with(Class<? extends InterceptorIdentification> identification);
	PropertyAccessInterceptorRegistry before(Class<? extends InterceptorIdentification> identification);
	PropertyAccessInterceptorRegistry after(Class<? extends InterceptorIdentification> identification);

	void add(PropertyAccessInterceptor interceptor);
	void remove(PropertyAccessInterceptor interceptor);

	/**
	 * Experimental. This can be used to replace an interceptor for given identification with another one. 
	 * 
	 * @return original {@link PropertyAccessInterceptor} that we are replacing
	 * 
	 * @throws NoSuchElementException
	 *             if there is no interceptor for given identification to replace
	 */
	PropertyAccessInterceptor replace(Class<? extends InterceptorIdentification> identification, PropertyAccessInterceptor interceptor);

}
