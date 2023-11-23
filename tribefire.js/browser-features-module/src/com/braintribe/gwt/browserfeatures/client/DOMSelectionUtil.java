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


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;

public class DOMSelectionUtil {
	private static DOMSelectionUtil instance = GWT.create(DOMSelectionUtil.class);

	private DivElement copyPasteElement;
	
	private DivElement getCopyPasteElement() {
		if (copyPasteElement == null) {
			copyPasteElement = Document.get().createDivElement();
			Style style = copyPasteElement.getStyle();
			style.setPosition(Position.ABSOLUTE);
			style.setOverflow(Overflow.HIDDEN);
			style.setLeft(-1, Unit.PX);
			style.setTop(-1, Unit.PX);
			style.setWidth(1, Unit.PX);
			style.setHeight(1, Unit.PX);
			
			Document.get().getBody().appendChild(copyPasteElement);
		}
		
		return copyPasteElement;
	}

	public void supplyForCopyPaste(String text) {
		DivElement copyPasteElement = getCopyPasteElement();
		copyPasteElement.setInnerText(text);
		selectElementText(copyPasteElement);
	}
	
	public static DOMSelectionUtil getInstance() {
		return instance;
	}
	
	public native void selectElementText(Element element)/*-{
		var range = $doc.createRange();
    	range.selectNode(element);
    	var selection = $wnd.getSelection();
    	selection.removeAllRanges();
    	selection.addRange(range);
	}-*/;
}
