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
package com.braintribe.gwt.browserfeatures.client;

/**
 * Wrapping the HTML 5 WebSocket API using GWT's JsInterop
 *
 */
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.user.client.Event;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Simple JSO wrapper the WebSocket object.
 *
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
@SuppressWarnings("unusable-by-js")
public class WebSocket {

    public static int CLOSED;
    public static int CLOSING;
    public static int CONNECTING;
    public static int OPEN;

    public String binaryType;
    public int bufferedAmount;
    public OnCloseCallback onclose;
    @SuppressWarnings("rawtypes")
	public OnMessageCallback onmessage;
    public OnOpenCallback onopen;
    public OnErrorCallback onerror;
    public int readyState;
    public String url;

    /**
     * @param url - URL of the WS in the services.
     */
    public WebSocket(String url) {
    	//NOP
    }

    public native boolean send(ArrayBufferView data);

    public native boolean send(String data);

    public native boolean send(ArrayBuffer data);

	public native void close();

    @JsFunction
    public interface OnCloseCallback {
        void onClose(Event a);

    }
    @JsFunction
    public interface OnMessageCallback<T> {
        void onMessage(MessageEvent<T> a);

    }
    @JsFunction
    public interface OnOpenCallback {
        void onOpen(Event a);
    }

    @JsFunction
    public interface OnErrorCallback {
        void onError(JavaScriptObject error);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class MessageEvent<T> {
        public T data;
    }
}
