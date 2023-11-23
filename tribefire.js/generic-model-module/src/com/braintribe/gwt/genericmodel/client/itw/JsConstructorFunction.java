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
package com.braintribe.gwt.genericmodel.client.itw;

import com.google.gwt.core.client.GwtScriptOnly;

@GwtScriptOnly
public class JsConstructorFunction extends GenericJavaScriptObject {

	protected JsConstructorFunction() {

	}

	public final static JsConstructorFunction create(Class<?> clazz, JsConstructorFunction superConstructor) {
		Object superPrototype = superConstructor.getPrototype();

		CastableTypeMap superTypeMap = ScriptOnlyItwTools.getCastableTypeMap(superPrototype);
		CastableTypeMap derivedTypeMap = (CastableTypeMap) ScriptOnlyItwTools.portableObjectCreate(superTypeMap);

		// TODO replace superConstructor with GwtEnhancedEntityStub constructor
		return createConstructor(superConstructor, clazz, derivedTypeMap);
	}

	private static JsConstructorFunction createConstructor(JsConstructorFunction superConstructor, Class<?> javaClass,
			CastableTypeMap castableTypeMap) {
		JsConstructorFunction result = createRawConstructor(superConstructor);

		GenericJavaScriptObject prototype = ScriptOnlyItwTools.portableObjectCreate(superConstructor.getPrototype());

		ScriptOnlyItwTools.setClass(prototype, javaClass);
		ScriptOnlyItwTools.setCastableTypeMap(prototype, castableTypeMap); // TODO we might want to check if this is even needed

		ScriptOnlyItwTools.setPrototype(result, prototype);

		return result;
	}

	// @formatter:off
	private static native JsConstructorFunction createRawConstructor(JsConstructorFunction superConstructor) /*-{
		return function() {
			superConstructor.call(this);
		};
	}-*/;
	
	protected final native <T> T newInstance() /*-{
		return new this;
	}-*/;
	// @formatter:on

	public final GenericJavaScriptObject getPrototype() {
		return ScriptOnlyItwTools.getPrototype(this);
	}

	public final CastableTypeMap getCastableTypeMap() {
		return ScriptOnlyItwTools.getCastableTypeMap(getPrototype());
	}

}
