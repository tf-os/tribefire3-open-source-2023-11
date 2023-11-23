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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import com.google.gwt.dom.client.Element;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 * Extension of the TextArea to handle passwords.
 * @author michel.docouto
 *
 */
public class PasswordTextArea extends TextArea {
	
	public PasswordTextArea() {
		getInputEl().getStyle().setColor("transparent");
		getInputEl().getStyle().setProperty("textShadow", "0 0 4px rgb(0,0,0)");
		getInputEl().getStyle().setProperty("caretColor", "black");
		getInputEl().addClassName("password-textarea");
		getInputEl().setAttribute("spellcheck", "false");
		
		disableCopy(getInputEl());
	}
	
	private native void disableCopy(Element el) /*-{
		el.addEventListener("copy", function(event) {
			event.preventDefault();
			event.stopPropagation();
		});
	}-*/;

}
