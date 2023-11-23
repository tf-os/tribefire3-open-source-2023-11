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
package com.braintribe.gwt.gmview.client.js;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewWindow;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import tribefire.extension.js.model.deployment.JsUxComponent;

@SuppressWarnings("unusable-by-js")
@JsType (namespace = InteropConstants.MODULE_NAMESPACE)
public abstract class TribefireUxModuleContract {
	
	@JsConstructor
	public TribefireUxModuleContract() {
		super();
	}
	
	/**
	 * Implementations which prepare the view in a sync way should use this method.
	 * @param context - the ComponentCreateContext
	 * @param denotation - The denotation type
	 */
	@JsMethod
	public native GmContentView createComponent(ComponentCreateContext context, JsUxComponent denotation);
	
	/**
	 * Implementations which prepare the view in an async way, should use this method instead.
	 * @param context - the ComponentCreateContext
	 * @param denotation - The denotation type
	 */
	@JsMethod
	public native Future<GmContentView> createComponentAsync(ComponentCreateContext context, JsUxComponent denotation);
	
	/**
	 * Implementations should use this method for binding service processors by using the given context.
	 * @param context - the ServiceBindingContext
	 */
	@JsMethod
	public native void bindServiceProcessors(ServiceBindingContext context);
	
	/**
	 * Implementations should use this method for binding the windows (if any) responsible for displaying the views.
	 * @param window - the window
	 */
	@JsMethod
	public native void bindWindow(GmContentViewWindow window);

}
