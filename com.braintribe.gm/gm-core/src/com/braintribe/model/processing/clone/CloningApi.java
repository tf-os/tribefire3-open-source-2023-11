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
package com.braintribe.model.processing.clone;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.LinearCollectionBase;
import com.braintribe.model.generic.collection.ListBase;
import com.braintribe.model.generic.collection.MapBase;
import com.braintribe.model.generic.collection.SetBase;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.SetType;

public interface CloningApi {

	<K, V> MapBase<K, V> cloneMap(MapBase<K, V> map);

	<K, V> MapBase<K, V> cloneMap(Map<K, V> map, MapType mapType);

	<K, V> MapBase<K, V> cloneMap(Map<K, V> map);

	<T> SetBase<T> cloneSet(SetBase<T> set);

	<T> SetBase<T> cloneSet(Set<T> set, SetType setType);

	<T> SetBase<T> cloneSet(Set<T> set);

	<T> ListBase<T> cloneList(ListBase<T> list);

	<T> ListBase<T> cloneList(List<T> list, ListType listType);

	<T> ListBase<T> cloneList(List<T> list);

	<T> LinearCollectionBase<T> cloneCollection(LinearCollectionBase<T> collection);

	<T> LinearCollectionBase<T> cloneCollection(Collection<T> collection, LinearCollectionType collectionType);

	<T> LinearCollectionBase<T> cloneCollection(Collection<T> list);

	<T extends GenericEntity> T cloneEntity(T entity);

	<T> T cloneValue(Object value, GenericModelType type);
	
	<T> T cloneValue(Object value);

}
