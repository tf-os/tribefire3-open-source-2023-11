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
package com.braintribe.model.processing.xmi.converter.registry.entries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * the base for all other entries in this package
 * @author pit
 *
 */
public class RegistryEntry {
	private String xmiId;
	private Element element;
	private Document document;

	public String getXmiId() {
		return xmiId;
	}

	public void setXmiId(String xmiId) {
		this.xmiId = xmiId;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public boolean isEqual(Object obj) {
		if (obj instanceof RegistryEntry == false)
			return super.equals(obj);
		RegistryEntry entry = (RegistryEntry) obj;
		return (entry.getClass().getName().equalsIgnoreCase(getClass().getName()));
	}

}
