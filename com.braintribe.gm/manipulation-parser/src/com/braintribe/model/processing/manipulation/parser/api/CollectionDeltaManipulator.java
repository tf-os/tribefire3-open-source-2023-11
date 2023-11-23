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
package com.braintribe.model.processing.manipulation.parser.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.SetType;

/**
 * @author peter.gazdik
 */
public interface CollectionDeltaManipulator extends GmmlConstants {

	void addToList(List<Object> list, ListType type);

	void addToSet(Set<Object> set, SetType type);

	void addToMap(Map<Object, Object> map, MapType type);

	void removeFromList(List<Object> list, ListType type);

	void removeFromSet(Set<Object> set, SetType type);

	void removeFromMap(Map<Object, Object> map, MapType type);

}
