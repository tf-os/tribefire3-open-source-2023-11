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

/**
 * Since native types cannot have JSNI {@link JsOverlay}s, we implement them here.
 * 
 * @author peter.gazdik
 */
class FileApiNativeHelper {

	public static native Blob blobCreateFromString(String data, String mimeType) /*-{
		return new $wnd.Blob([data],{type: mimeType});
	}-*/;

	public static native String blobUrl(Blob _this) /*-{
		return $wnd.URL.createObjectURL(_this);
	}-*/;

	public static native FileReader fileReaderCreate() /*-{
		return new $wnd.FileReader();
	}-*/;

	public static native void fileReaderAddEventHandler(FileReader _this, ProgressListener progressListener, ProgressEventType type) /*-{
		var slot = type.@java.lang.Enum::name()();
		_this['on' + slot] = $entry(function(event) {
			progressListener.@ProgressListener::onProgress(*)(event, type);
		});
	}-*/;

	public static native FormData formDataCreate() /*-{
		 return new $wnd.FormData();
	}-*/;

	public static native FormData formDataCreate(FormElement form) /*-{
		 return new $wnd.FormData(form);
	}-*/;

	public static final native String progressEventGetErrorMessage(ProgressEvent _this) /*-{
	 	return _this.error.message;
	}-*/;

}
