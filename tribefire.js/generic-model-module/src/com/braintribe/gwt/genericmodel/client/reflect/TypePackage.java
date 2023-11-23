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
package com.braintribe.gwt.genericmodel.client.reflect;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.context.JsKeywords;

@SuppressWarnings("unusable-by-js")
public class TypePackage{
	
	public static Map<String, JavaScriptObject> packagesByQualfiedName = newMap();
	
	// WHY IS THIS HERE???
	public static native TypePackage instance() /*-{
		return this;
	}-*/;
	
	public static native JavaScriptObject getRoot() /*-{
		var root = $wnd.$T;
		if(!root){
			root = {};
			$wnd.$T = root;
		}			
		return root;
	}-*/;

	public static final void register(GenericModelType type, Object jsObjectToRegister) {
		String typesig = type.getTypeSignature();
		
		int index = typesig.lastIndexOf('.');
		
		String simpleName = null;
		String packagePath = null;
		
		if (index != -1) {
			simpleName = typesig.substring(index + 1);
			packagePath = typesig.substring(0, index);
		}
		else {
			simpleName = typesig;
		}
		
		JavaScriptObject typePackage = acquireJsObjectForPackagePath(packagePath);
		
		TypePackage.set(typePackage, simpleName, jsObjectToRegister);
		
	}
	
	public static JavaScriptObject acquireJsObjectForPackagePath(String packagePath) {
		JavaScriptObject typePackage = packagePath == null ? getRoot() : packagesByQualfiedName.get(packagePath);

		if (typePackage == null) {
			int index = packagePath.lastIndexOf('.');
			String packageName = packagePath.substring(index + 1);

			String parentTypePackagePath = index != -1 ? packagePath.substring(0, index) : null;

			JavaScriptObject parentTypePackage = acquireJsObjectForPackagePath(parentTypePackagePath);

			typePackage = JavaScriptObject.createObject();
			TypePackage.set(parentTypePackage, JsKeywords.javaIdentifierToJs(packageName), typePackage);

			packagesByQualfiedName.put(packagePath, typePackage);
		}

		return typePackage;
	}

	public static final native JavaScriptObject getPackage(JavaScriptObject parent, String name) /*-{
		return parent[name];
	}-*/;
	
	public static final native void set(JavaScriptObject parent, String name, Object value) /*-{
		parent[name] = value;
	}-*/;  
}
