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
public class FileReader {

	@JsOverlay
	public static final short EMPTY = 0;
	@JsOverlay
	public static final short LOADING = 1;
	@JsOverlay
	public static final short DONE = 2;

	protected FileReader() {

	}

	public String result;
	public short readyState;

	@JsOverlay
	public static FileReader create() {
		return FileApiNativeHelper.fileReaderCreate();
	}

	@JsOverlay
	public final String getStringResult() {
		return result;
	}

	@JsOverlay
	public final short getReadyState() {
		return readyState;
	}

	@JsOverlay
	public final void readAsText(Blob blob) {
		this.readAsText(blob, null);
	}

	public final native void readAsText(Blob blob, String encoding);

	@JsOverlay
	public final void addEventHandler(ProgressListener progressListener, ProgressEventType type) {
		FileApiNativeHelper.fileReaderAddEventHandler(this, progressListener, type);
	}

	@JsOverlay
	public final void addEventHandler(ProgressListener progressListener, ProgressEventType... types) {
		for (ProgressEventType type : types)
			addEventHandler(progressListener, type);
	}

}
