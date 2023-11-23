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
package com.braintribe.gwt.browserfeatures.client.mutation;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

public class MutationObserver extends JavaScriptObject{
	protected MutationObserver() {
		
	}
	
	public final native void observe(Element e) /*-{
		options = {
			attributes : true,
			characterData : true,
			childList : true,
			subtree : true,
			attributeOldValue : true,
			characterDataOldValue : true
		}
		this.observe(e, options);
	}-*/;
	
	public final native void observe(String selector) /*-{
		options = {
			attributes : true,
			characterData : true,
			childList : true,
			subtree : true,
			attributeOldValue : true,
			characterDataOldValue : true
		}
		ob = this;
		$doc.querySelectorAll(selector).forEach(function(e){debugger;ob.observe(e,options)});
	}-*/;
	
	public static native MutationObserver create(MutationObserverCallback callback) /*-{
		return new MutationObserver(function(entries) {
			callback(entries);
		});
	}-*/;
}