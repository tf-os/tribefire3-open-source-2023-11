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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.browserfeatures.client.interop.JsMap;
import com.braintribe.gwt.browserfeatures.client.interop.JsSet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 * Uses {@link HashMap}
 * Uses {@link HashSet}
 */
public class JsCollections {

	public static <V> Map<String, V> stringMap() {
		return GWT.isScript() ? new JsStringMap<V>() : new HashMap<String, V>();
	}
	
	public static <V> Map<String, V> stringMap(JavaScriptObject backend) {
		if (GWT.isScript())
			return new JsStringMap<V>(backend);
		else {
			Map<String, V> map = new HashMap<String, V>();
			fill(backend, map);
			return map;
		}
	}
	
	private static native <V> void fill(JavaScriptObject backend, Map<String, V> map) /*-{
		var ks = Object.keys(backend);
		
		for (var i = 0; i < ks.length; i++) {
			var k = keys[i];
			var v = backend[k];
			map.@java.util.Map::put(Ljava/lang/Object;Ljava/lang/Object;)(k,v);
		}
	}-*/;

	public static <V> List<V> nativeList(JsArray<V> backend) {
		if (GWT.isScript()) {
			return new JsArrayList<V>(backend);
		}
		else {
			int length = backend.length();
			List<V> list = new ArrayList<V>(length);
			for (int i = 0; i < length; i++) {
				list.add(backend.get(i));
			}
			return list;
		}
	}

	public static native <K, V> Map<K, V> toJMap(JsMap<K, V> jsMap) /*-{
		var result = new @HashMap::new()();
		jsMap.forEach((v,k)=>result.@java.util.Map::put(Ljava/lang/Object;Ljava/lang/Object;)(k,v););
		return result;
	}-*/;  

	public static native <E> Set<E> toJSet(JsSet<E> jsSet) /*-{
		var result = new @HashSet::new()();
		jsSet.forEach((e)=>result.@java.util.Set::add(Ljava/lang/Object;)(e););
		return result;
	}-*/;  

}
