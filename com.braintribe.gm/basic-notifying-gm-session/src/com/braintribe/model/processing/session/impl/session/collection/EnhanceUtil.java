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
package com.braintribe.model.processing.session.impl.session.collection;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;

public class EnhanceUtil {

	public static Object ensureEnhanced(GenericModelType type, Object value) {
		if (value == null)
			return value;

		if (type.isCollection()) {
			if (value instanceof EnhancedCollection) {
				return value;
			}

			CollectionType collectionType = (CollectionType) type;
			return enhanceCollection(collectionType, value);
		}

		return value;
	}

	public static EnhancedCollection enhanceCollection(CollectionType collectionType, Object collection) {
		switch (collectionType.getCollectionKind()) {
			case list:
				return ensureEnhancedList((ListType) collectionType, (List<Object>) collection);
			case set:
				return ensureEnhancedSet((SetType) collectionType, (Set<Object>) collection);
			case map:
				return ensureEnhancedMap((MapType) collectionType, (Map<Object, Object>) collection);
		}

		throw new IllegalArgumentException("Unknown collection type: " + collectionType);
	}

	public static EnhancedCollection cloneCollection(CollectionType collectionType, EnhancedCollection collection) {
		switch (collectionType.getCollectionKind()) {
			case list:
				return ensureEnhancedList(collectionType, newList((List<?>) collection));
			case set:
				return ensureEnhancedSet(collectionType, newLinkedSet((Set<?>) collection));
			case map:
				return ensureEnhancedMap((MapType) collectionType, newLinkedMap((Map<?, ?>) collection));
		}
		return collection;
	}

	public static EnhancedList<?> ensureEnhancedList(CollectionType collectionType, List<Object> list) {
		if (list instanceof EnhancedList<?>)
			return (EnhancedList<?>) list;

		return enhanceList(collectionType, list);
	}

	public static EnhancedList<Object> enhanceList(CollectionType collectionType, List<Object> list) {
		return new EnhancedList<Object>((ListType) collectionType, list);
	}

	public static EnhancedSet<?> ensureEnhancedSet(CollectionType collectionType, Set<Object> set) {
		if (set instanceof EnhancedSet<?>)
			return (EnhancedSet<?>) set;

		return enhanceSet(collectionType, set);
	}

	public static EnhancedSet<?> enhanceSet(CollectionType collectionType, Set<Object> set) {
		return new EnhancedSet<Object>((SetType) collectionType, set);
	}

	public static EnhancedMap<?, ?> ensureEnhancedMap(MapType collectionType, Map<Object, Object> map) {
		if (map instanceof EnhancedMap<?, ?>)
			return (EnhancedMap<?, ?>) map;

		return enhanceMap(collectionType, map);
	}

	public static EnhancedMap<?, ?> enhanceMap(MapType collectionType, Map<Object, Object> map) {
		return new EnhancedMap<Object, Object>(collectionType, map);
	}

	static LocalEntityProperty newLocalOwner(GenericEntity entity, Property p) {
		return ManipulationBuilder.localEntityProperty(entity, p.getName());
	}

	static <T> void loadCollectionLazily(GenericEntity entity, LocalEntityProperty owner, Consumer<T> lazyResultConsumer) {
		GmSession _session = entity.session();
		if (!(_session instanceof PersistenceGmSession))
			return;

		PersistenceGmSession session = (PersistenceGmSession) _session;

		T lazyValue = queryValue(entity, owner, session);
		if (lazyValue != null)
			doWithoutHistory(session, lazyValue, lazyResultConsumer);
	}

	private static <T> T queryValue(GenericEntity entity, LocalEntityProperty owner, PersistenceGmSession session) {
		return (T) queryValue(entity, owner, session, null).getPropertyValue();
	}

	private static PropertyQueryResult queryValue(GenericEntity entity, LocalEntityProperty owner, PersistenceGmSession session, Integer maxResults) {
		PropertyQuery query = preparePropertyQuery(entity, owner, maxResults);
		try {
			return session.query().property(query).result();
		} catch (GmSessionException e) {
			throw new RuntimeException("Property query failed", e);
		}
	}

	private static PropertyQuery preparePropertyQuery(GenericEntity entity, LocalEntityProperty owner, Integer maxResults) {
		PropertyQueryBuilder pqb = PropertyQueryBuilder.forProperty((PersistentEntityReference) entity.reference(), owner.getPropertyName());
		if (maxResults != null)
			pqb = pqb.limit(maxResults);

		return pqb.done();
	}

	private static <T> void doWithoutHistory(PersistenceGmSession session, T lazyValue, Consumer<T> lazyResultConsumer) {
		session.suspendHistory();
		try {
			lazyResultConsumer.accept(lazyValue);
		} finally {
			session.resumeHistory();
		}
	}

	public static boolean isLoaded(Collection<?> c) {
		return c instanceof EnhancedCollection ? isLoaded((EnhancedCollection) c) : true;
	}

	public static boolean isLoaded(Map<?, ?> m) {
		return m instanceof EnhancedCollection ? isLoaded((EnhancedCollection) m) : true;
	}

	private static boolean isLoaded(EnhancedCollection ec) {
		return ec.isLoaded();
	}

}
