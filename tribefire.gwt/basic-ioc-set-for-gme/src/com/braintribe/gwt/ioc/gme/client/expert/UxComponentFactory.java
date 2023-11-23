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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.gwt.ioc.gme.client.Panels;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Factory for providing GME components to be used inside external JS components.
 *
 */
@JsType(namespace = InteropConstants.MODULE_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class UxComponentFactory {

	private Map<String, GmContentView> componentMap = new HashMap<>();
	private Map<String, Supplier<? extends GmContentView>> supplierMap = new HashMap<>();

	@JsConstructor
	public UxComponentFactory() {
		addComponentSupplier("JsPropertyPanel", Panels.jsPropertyPanelProvider);
		addComponentSupplier("JsThumbnailPanel", Panels.jsThumbnailPanelProvider);
	}

	@JsMethod
	public void addComponentSupplier(String name, Supplier<? extends GmContentView> supplier) {
		supplierMap.put(name, supplier);
	}

	@JsMethod
	public void removeComponentSupplier(String name) {
		supplierMap.remove(name);
	}

	@JsMethod
	public void addComponent(String name, GmContentView gmContentView) {
		componentMap.put(name, gmContentView);
	}

	@JsMethod
	public void removeComponent(String name) {
		componentMap.remove(name);
	}

	@JsMethod
	public GmContentView provideComponent(String name) {
		Supplier<? extends GmContentView> supplier = supplierMap.get(name);
		if (supplier != null)
			return supplier.get();
		
		return componentMap.get(name);
	}
}
