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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import jsinterop.annotations.JsType;
import jsinterop.context.JsInteropNamespaces;

/**
 * 
 */
public class JsStringMap<V> extends AbstractMap<String, V> {

	private JavaScriptObject jso;

	public JsStringMap() {
		this(JavaScriptObject.createObject());
	}

	public JsStringMap(JavaScriptObject jso) {
		this.jso = jso;
	}

	@Override
	public final native boolean containsKey(Object key)
	/*-{
		return this.@JsStringMap::jso.hasOwnProperty(key);
	}-*/;

	@Override
	public final boolean containsValue(Object value) {
		return values().contains(value);
	}

	@Override
	public final native V get(Object key)
	/*-{
		return this.@JsStringMap::jso[key];
	}-*/;

	@Override
	public final native V put(String key, V value)
	/*-{
		var jso = this.@JsStringMap::jso;
		var result = jso[key];
		jso[key] = value;
		return result;
	}-*/;

	@Override
	public final native V remove(Object key)
	/*-{
	    var jso = this.@JsStringMap::jso;
		var result = jso[key];
		delete jso[key];
		return result;
	}-*/;

	@Override
	public final native void clear()
	/*-{
	    var jso = this.@JsStringMap::jso;
		for (var key in jso) {
			delete jso[key];
		}
	}-*/;

	@Override
	public final Collection<V> values() {
		return new ValueCollection<V>(this);
	}

	@Override
	public final Set<String> keySet() {
		return new KeySet<V>(this);
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return new EntrySet<V>(this);
	}

	@Override
	public final native int size()
	/*-{
		return Object.keys(this.@JsStringMap::jso).length;
	}-*/;

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		for (Entry<? extends String, ? extends V> entry: m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

}

@JsType(namespace = JsInteropNamespaces.gm)
abstract class ViewSet<V, P> extends AbstractSet<P> {

	protected JsStringMap<V> map;

	public ViewSet(JsStringMap<V> map) {
		this.map = map;
	}

	@Override
	public Iterator<P> iterator() {

		return new Iterator<P>() {
			Integer lastIndex;
			int nextIndex = 0;
			JsArray<JavaScriptObject> array = getObjectKeysArray();

			@Override
			public boolean hasNext() {
				return nextIndex < array.length();
			}

			@Override
			public P next() {
				return project(array.get(lastIndex = nextIndex++).toString());
			}

			@SuppressWarnings("unlikely-arg-type")
			@Override
			public void remove() {
				if (lastIndex == null) {
					throw new IllegalStateException("Cannot remove entry from map.");
				}

				JavaScriptObject lastKey = array.get(lastIndex);
				map.remove(lastKey);

				lastIndex = null;
			}
		};

	}

	protected abstract P project(String key);

	@Override
	public final int size() {
		return map.size();
	}

	private native JsArray<JavaScriptObject> getObjectKeysArray()
	/*-{
		return Object.keys(this.@ViewSet::map.@JsStringMap::jso);
	}-*/;

}

@JsType(namespace = JsInteropNamespaces.gm)
class EntrySet<V> extends ViewSet<V, Entry<String, V>> {

	public EntrySet(JsStringMap<V> map) {
		super(map);
	}

	@Override
	protected Entry<String, V> project(final String key) {
		return new Entry<String, V>() {

			@Override
			public String getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return map.get(key);
			}

			@Override
			public V setValue(V value) {
				return map.put(key, value);
			}

		};
	}

}

class KeySet<V> extends ViewSet<V, String> {

	public KeySet(JsStringMap<V> map) {
		super(map);
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	protected String project(String key) {
		return key;
	}

}

class ValueCollection<V> extends ViewSet<V, V> {

	public ValueCollection(JsStringMap<V> map) {
		super(map);
	}

	@Override
	protected V project(String key) {
		return map.get(key);
	}

}
