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
package com.braintribe.model.processing.vde.clone.async;

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
import com.braintribe.processing.async.api.AsyncCallback;

public interface AsyncCloning {
	<T> void cloneCollection(Collection<T> collection, AsyncCallback<? super LinearCollectionBase<T>> callback);

	<T> void cloneCollection(Collection<T> collection, LinearCollectionType collectionType, AsyncCallback<? super LinearCollectionBase<T>> callback);

	<T> void cloneCollection(LinearCollectionBase<T> collection, AsyncCallback<? super LinearCollectionBase<T>> callback);

	<T> void cloneList(List<T> list, AsyncCallback<? super ListBase<T>> callback);

	<T> void cloneList(List<T> list, ListType listType, AsyncCallback<? super ListBase<T>> callback);

	<T> void cloneList(ListBase<T> list, AsyncCallback<? super ListBase<T>> callback);

	<T> void cloneSet(Set<T> set, AsyncCallback<? super SetBase<T>> callback);

	<T> void cloneSet(Set<T> set, SetType setType, AsyncCallback<? super SetBase<T>> callback);

	<T> void cloneSet(SetBase<T> set, AsyncCallback<? super SetBase<T>> callback);

	<K, V> void cloneMap(Map<K, V> map, AsyncCallback<? super MapBase<K, V>> callback);

	<K, V> void cloneMap(Map<K, V> map, MapType mapType, AsyncCallback<? super MapBase<K, V>> callback);

	<K, V> void cloneMap(MapBase<K, V> map, AsyncCallback<? super MapBase<K, V>> callback);

	<T extends GenericEntity> void cloneEntity(T entity, AsyncCallback<? super T> callback);

	<T> void cloneValue(Object value, AsyncCallback<? super T> callback);

	<T> void cloneValue(Object value, GenericModelType type, AsyncCallback<? super T> callback);

}