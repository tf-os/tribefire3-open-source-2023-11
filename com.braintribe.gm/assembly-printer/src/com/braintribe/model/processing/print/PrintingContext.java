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
package com.braintribe.model.processing.print;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.Property;

/**
 * @author peter.gazdik
 */
public interface PrintingContext {

	void print(Object object);

	void println(Object object);

	void printList(List<?> list);

	void printSet(Set<?> set);

	void printMap(Map<?, ?> map);

	boolean getWriteNulls();

	boolean ignoreProperty(Property property);

	// ################################################################

	void levelUp();

	void levelDown();

	void println(String s);

	void println();

	void print(String s);

}
