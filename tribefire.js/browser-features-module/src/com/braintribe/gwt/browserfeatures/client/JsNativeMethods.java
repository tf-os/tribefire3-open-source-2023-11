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

public class JsNativeMethods {
	
	public static native void reject(Object reject, Object error) /*-{
		reject(error);
	}-*/;
	
	public static native void resolve(Object resolve, Object value) /*-{
		resolve(value);
	}-*/;
	
	public static native Object jsArray(Object list) /*- {
		return list.toArray();
	} -*/;
	
	public static native void log(String msg) /*-{
		console.log(msg);
	}-*/;
	
	public static native void warn(String msg) /*-{
		console.warn(msg);
	}-*/;
	
	public static native void error(String msg, Throwable e) /*-{
   		console.log(msg, e);
	}-*/;	
	
	public static native void time(String process) /*-{
		console.time(process);
	}-*/;

	public static native void timeEnd(String process) /*-{
		console.timeEnd(process);
	}-*/;
	
	public static native void assign(Object object, Object assign) /*-{
		Object.assign(object, assign);
	}-*/;
	
	public static native void debugger() /*-{
		debugger;
	}-*/;

	public static native void alert(String msg) /*-{
		$wnd.alert(msg);
	}-*/;
}
