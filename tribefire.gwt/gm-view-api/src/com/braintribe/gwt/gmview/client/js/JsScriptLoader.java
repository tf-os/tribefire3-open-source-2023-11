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

/**
 * Utility class for loading scripts into the DOM dynamically.
 *
 */
public class JsScriptLoader {

	private String url = "../tribefire-js/tribefire.js"; // Default value
	private String scriptId = "jsScript";
	private boolean scriptLoaded = false;

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSciptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public void loadScript() {
		setSetting();
	}

	private void setSetting() {
		String urlString = this.url;

		unloadScript(scriptId);
		loadNewScript(scriptId, "text/javascript", urlString);
	}

	public void loadNewScript(String id, String type, String src) {
		if (Document.get().getElementById(id) != null) {
			setScriptLoaded(true);
			return;
		}

		if (type == null || type.isEmpty())
			loadScriptNoType(id, src);
		else
			loadScript(id, type, src, this);
	}

	private static boolean contains(String id) {
		return Document.get().getElementById(id) != null;
	}

	public boolean containsScript() {
		return contains(scriptId);
	}

	public void unloadScript(String id) {
		Element element = Document.get().getElementById(id);
		if (element != null) {
			element.removeFromParent();
			setScriptLoaded(false);
		}
	}

	public boolean isScriptLoaded() {
		return this.scriptLoaded;
	}
	
	private void setScriptLoaded(boolean loaded) {
	    this.scriptLoaded = loaded; 	
	}	
	
	private static native void loadScript(String id, String type, String url, Object object) /*-{	
		var onLoad = $entry(function() {
			object.@com.braintribe.gwt.gmview.client.js.JsScriptLoader::setScriptLoaded(Z)(true);
		});

		var script = $doc.createElement('script');
		script.setAttribute("src", url);
		script.setAttribute("id", id);
		script.setAttribute("type", type);
		script.addEventListener('load', onLoad);
		$doc.getElementsByTagName("head")[0].appendChild(script);		
	}-*/;
	

	private static native void loadScriptNoType(String id, String url) /*-{
		var onLoad = $entry(function() {
			object.@com.braintribe.gwt.gmview.client.js.JsScriptLoader::setScriptLoaded(Z)(true);
		});

		var script = $doc.createElement("script");
		script.setAttribute("id", id);
		script.setAttribute("src", url);  
		script.addEventListener('load', onLoad);
		$doc.getElementsByTagName("head")[0].appendChild(script);
	}-*/;

}
