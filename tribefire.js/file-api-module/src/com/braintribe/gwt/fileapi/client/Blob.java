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

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Blob {
	protected Blob() {
		// To avoid constructor call in Java
	}

	public String type;
	public double size;

	@JsOverlay
	public static Blob createFromString(String data, String mimeType) {
		return FileApiNativeHelper.blobCreateFromString(data, mimeType);
	}

	public final native Blob slice(double start, double end, String mimeType);

	@JsOverlay
	public final Blob slice(int start, int end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final Blob slice(long start, long end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final String type() {
		return type;
	}

	@JsOverlay
	public final long size() {
		return (long) size;
	}

	@JsOverlay
	public final String url() {
		return FileApiNativeHelper.blobUrl(this);
	}
}
