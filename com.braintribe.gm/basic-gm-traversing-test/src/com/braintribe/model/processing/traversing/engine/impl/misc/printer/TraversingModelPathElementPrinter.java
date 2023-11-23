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
package com.braintribe.model.processing.traversing.engine.impl.misc.printer;

import com.braintribe.model.generic.path.api.IPropertyRelatedModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

public class TraversingModelPathElementPrinter {
	
	@SuppressWarnings("incomplete-switch")
	public static String format(TraversingModelPathElement pathElement) {
		switch (pathElement.getElementType()) {
			case EntryPoint:
				return "Entry point";
			case ListItem:
				return "List item";
			case MapKey:
				return "Map key";
			case MapValue:
				return "Map value";
			case Property:
				IPropertyRelatedModelPathElement propertyElement = (IPropertyRelatedModelPathElement) pathElement;
				return "Property " + propertyElement.getEntityType().getShortName() + "."
						+ propertyElement.getProperty().getName() + " : "
						+ format(propertyElement.getValue() + "");
			case Root:
				return "Root";
			case SetItem:
				return "Set item";
		}
		return "";
	}

	private static String format(String rawString) {
		if (rawString.startsWith("[") && rawString.endsWith("]")) {
			return "List Reference";
		} else if (rawString.contains("@")) {
			return "Reference";
		} else if (rawString.contains("CET") || rawString.contains("CEST")) {
			return "Date";
		} else {
			return rawString;
		}
	}
}
