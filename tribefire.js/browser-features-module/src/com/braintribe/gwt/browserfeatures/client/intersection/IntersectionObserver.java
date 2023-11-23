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
package com.braintribe.gwt.browserfeatures.client.intersection;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

public class IntersectionObserver extends JavaScriptObject{
	protected IntersectionObserver() {
		
	}
	
	public final native void observe(Element e) /*-{
		this.observe(e);
	}-*/;
	
	public final native void observe(String selector) /*-{
		this.observe($doc.querySelector(selector));
	}-*/;
	
	public static native IntersectionObserver create(Element el, IntersectionObserverCallback callback) /*-{
		return new IntersectionObserver(function(entries){
			for(i=0; i<entries.length;i++){
				entry = entries[i]
				callback(entry.isIntersecting, entry.target);
			}
		}, {root: el, threshold: [0]});
	}-*/;
	
	public static native boolean isSupported() /*-{
		if($wnd.IntersectionObserver)
			return true;
		else
			return false;
	}-*/;

	public final native void disconnect()/*-{
		this.disconnect();
	}-*/;
}
