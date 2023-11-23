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
package com.braintribe.model.processing.print.experts;

import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.print.PrintingContext;

/**
 * @author peter.gazdik
 */
public interface PropertyPrinter {

	/**
	 * Prints the property value
	 */
	void print(GenericEntity entity, Object propertyValue, Property property, PrintingContext context);

	/**
	 * @param <V>
	 *            type of the value. Not really needed, just to avoid need for a cast on the callers site in some cases.
	 */
	static <V> PropertyPrinter valuePrinter(Function<V, String> valueStringifier) {
		Function<Object, String> castedStringifier = (Function<Object, String>) valueStringifier;
		return (e, v, p, c) -> c.print(castedStringifier.apply(v));
	}

}
