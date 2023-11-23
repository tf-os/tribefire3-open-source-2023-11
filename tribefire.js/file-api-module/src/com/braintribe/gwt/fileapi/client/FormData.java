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
package com.braintribe.gwt.fileapi.client;

import com.google.gwt.dom.client.FormElement;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class FormData {
	protected FormData() {

	}

	@JsOverlay
	public static FormData create() {
		return FileApiNativeHelper.formDataCreate();
	}

	@JsOverlay
	public static FormData create(FormElement form) {
		return FileApiNativeHelper.formDataCreate(form);
	}

	public final native void append(String name, Object stringOrBlob, String filename);

	@JsOverlay
	public final void append(String name, Blob blob, String fileName) {
		append(name, (Object) blob, fileName);
	}

	@JsOverlay
	public final void append(String name, Blob blob) {
		String fileName = blob instanceof File ? ((File) blob).getName() : null;
		append(name, (Object) blob, fileName);
	}

	@JsOverlay
	public final void append(String name, String value) {
		append(name, value, null);
	}

}
