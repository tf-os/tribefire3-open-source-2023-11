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
package com.braintribe.gwt.genericmodel.client.itw;

import java.util.AbstractSet;
import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * 
 */
public class JsPropertyNameSet extends AbstractSet<String> {

	private final JavaScriptObject jso;

	public JsPropertyNameSet(JavaScriptObject jso) {
		this.jso = jso;
	}

	@Override
	public native boolean contains(Object o)
	/*-{
		//return this.@JsPropertyNameSet::jso.hasOwnProperty(o.@Object::toString());
		return this.@JsPropertyNameSet::jso.hasOwnProperty(o.toString());
	}-*/;

	@Override
	public Iterator<String> iterator() {

		return new Iterator<String>() {
			int nextIndex = 0;
			JsArray<JavaScriptObject> array = getObjectKeysArray();

			@Override
			public boolean hasNext() {
				return nextIndex < array.length();
			}

			@Override
			public String next() {
				return array.get(nextIndex++).toString();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

	@Override
	public native int size()
	/*-{
		return Object.keys(this.@JsPropertyNameSet::jso).length;
	}-*/;

	private native JsArray<JavaScriptObject> getObjectKeysArray()
	/*-{
		return Object.keys(this.@JsPropertyNameSet::jso);
	}-*/;

}
