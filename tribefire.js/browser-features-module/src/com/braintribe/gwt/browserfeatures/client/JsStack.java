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

import com.google.gwt.core.client.JavaScriptObject;

public class JsStack<T> extends JavaScriptObject {
	
	protected JsStack() {
		
	}
	
	public static final <T1> JsStack<T1> create() {
		return JavaScriptObject.createArray().cast();
	}
	
	public final native void push(T element) /*-{
		this.push(element);
	}-*/;
	
	public final native T pop() /*-{
		return this.pop();
	}-*/;
	
	public final native boolean isEmpty() /*-{
		return this.length == 0;
	}-*/;
	
	public final native boolean hasElements() /*-{
		return this.length != 0;
	}-*/;
}