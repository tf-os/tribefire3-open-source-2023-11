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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class CssLoader {
	
	public static void unloadCss(String id) {
		Element cssElement = Document.get().getElementById(id);
		if (cssElement != null)
			cssElement.removeFromParent();
	}
	
	public static native void loadCss(String id, String url) /*-{
    	var l = $doc.createElement("link");
    	l.setAttribute("id", id);
    	l.setAttribute("rel", "stylesheet");
    	l.setAttribute("type", "text/css");
    	l.setAttribute("href", url); // Make sure this request is not cached
    	$doc.getElementsByTagName("head")[0].appendChild(l);
	}-*/;

}
