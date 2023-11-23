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

import java.util.Date;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface ScalarType extends GenericModelType {

	/**
	 * Converts a given instance to a String. Unlike the {@link #instanceToGmString(Object)} method, this String doesn't
	 * <p>
	 * Assumes a compatible value is being passed to the actual {@link ScalarType} (so for example {@link DateType}
	 * would receive an instance of {@link Date}). If this is not the case, the behavior is undefined.
	 */
	String instanceToString(Object value);

	/**
	 * Inverse to {@link #instanceToString(Object)}.
	 * <p>
	 * Assumes the type and encoded value match and thus the value can be parsed. Otherwise, the behavior is undefined.
	 */
	<T> T instanceFromString(String encodedValue);

	/**
	 * Converts given scalar value to a string which also carries the type information. To parse the value back, when
	 * the type is not known, use GmValueCodec from gm-core. Otherwise use the {@link #instanceFromGmString(String)} for
	 * faster decoding.
	 * <p>
	 * Assumes a compatible value is being passed to the actual {@link ScalarType} (so for example {@link DateType}
	 * would receive an instance of {@link Date}). If this is not the case, the behavior is undefined.
	 * <p>
	 * NOTE that this value should (for now) be used as transient only - the actual format of certain types might
	 * change, so it is persisted, you might not be able to parse it later.
	 */
	String instanceToGmString(Object value);

	/**
	 * Inverse to {@link #instanceToGmString(Object)}.
	 * <p>
	 * Assumes the type and encoded value match and thus the value can be parsed. Otherwise, the behavior is undefined.
	 * <p>
	 * NOTE that this value should (for now) be used as transient only - the actual format of certain types might
	 * change, so it is persisted, you might not be able to parse it later.
	 */
	Object instanceFromGmString(String encodedValue);
}
