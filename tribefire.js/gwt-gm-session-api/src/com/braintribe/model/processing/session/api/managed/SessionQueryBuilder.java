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
package com.braintribe.model.processing.session.api.managed;

import static com.braintribe.utils.promise.JsPromiseCallback.promisify;
import static java.util.Objects.requireNonNull;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.processing.async.api.JsPromise;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace="$tf.session")
@SuppressWarnings("unusable-by-js")
public interface SessionQueryBuilder {
	
	/**
	 * Creates a {@link SelectQueryExecution} that can be used to execute the passed {@link SelectQuery} instance.
	 */
	@JsMethod(name="selectQueryExecution")
	SelectQueryExecution select(SelectQuery selectQuery);
	
	/**
	 * Creates a {@link SelectQueryExecution} that can be used to execute the {@link SelectQuery} represented by the passed selectQueryString.
	 */
	@JsMethod(name="selectQueryStringExecution")
	SelectQueryExecution select(String selectQueryString);
	
	@JsMethod(name="select")
	default JsPromise<SelectQueryResultConvenience> selectAsync(SelectQuery selectQuery) {
		return promisify(select(selectQuery)::result);
	}
	
	@JsMethod(name="selectString")
	default JsPromise<SelectQueryResultConvenience> selectAsync(String selectQueryString) {
		return promisify(select(selectQueryString)::result);
	}
	
	/**
	 * Creates a {@link EntityQueryExecution} that can be used to execute the passed {@link EntityQuery} instance.
	 */
	@JsMethod(name="entityQueryExecution")
	EntityQueryExecution entities(EntityQuery entityQuery);	
	
	/**
	 * Creates a {@link EntityQueryExecution} that can be used to execute the {@link EntityQuery} represented by the passed entityQueryString.
	 */
	@JsMethod(name="entityQueryStringExecution")
	EntityQueryExecution entities(String entityQueryString);
	
	@JsMethod(name="entities")
	default JsPromise<EntityQueryResultConvenience> entitiesAsync(EntityQuery entityQuery){
		return promisify(entities(entityQuery)::result);
	}
	
	@JsMethod(name="entitiesString")
	default JsPromise<EntityQueryResultConvenience> entitiesAsync(String entityQueryString){
		return promisify(entities(entityQueryString)::result);
	}	
	
	/**
	 * Creates a {@link PropertyQueryExecution} that can be used to execute the passed {@link PropertyQuery} instance.
	 */
	@JsMethod(name="propertyQueryExecution")
	PropertyQueryExecution property(PropertyQuery propertyQuery);

	/**
	 * Creates a {@link PropertyQueryExecution} that can be used to execute the {@link PropertyQuery} represented by the passed propertyQueryString.
	 */
	@JsMethod(name="propertyQueryStringExecution")
	PropertyQueryExecution property(String propertyQueryString);
	
	@JsMethod(name="property")
	default JsPromise<PropertyQueryResultConvenience> propertyAsync(PropertyQuery propertyQuery){
		return promisify(property(propertyQuery)::result);
	}
	
	@JsMethod(name="propertyString")
	default JsPromise<PropertyQueryResultConvenience> propertyAsync(String propertyQueryString){
		return promisify(property(propertyQueryString)::result);
	}
	
	/**
	 * Creates a {@link QueryExecution} that can be used to executed the passed {@link Query} instance.<br/>  
	 * This method provides the option to generically execute any of the three supported derivations of abstract {@link Query}.
	 * 
	 * <li> {@link EntityQuery} </li>
	 * <li> {@link PropertyQuery} </li>
	 * <li> {@link SelectQuery} </li>
	 * 
	 * <br/>
	 */
	QueryExecution abstractQuery(Query query);
	
	/**
	 * Creates a {@link QueryExecution} that can be used to execute the {@link Query} represented by the queryString.<br/> 
	 * This method provides the option to generically execute any of the three supported derivations of abstract {@link Query}.
	 * 
	 * <li> {@link EntityQuery} </li>
	 * <li> {@link PropertyQuery} </li>
	 * <li> {@link SelectQuery} </li>
	 * 
	 * <br/>
	 */
	@JsMethod(name="queryViaString")
	@SuppressWarnings("unusable-by-js")
	QueryExecution abstractQuery(String queryString);
	
	default <T extends GenericEntity> T getEntity(String globalId) {
		return requireNonNull(findEntity(globalId), () -> "Entity not found for globalId: " + globalId);
	}

	/**
	 * @returns entity with given globalId or <tt>null</tt> if no entity found.
	 *
	 * @see #getEntity(String)
	 */
	<T extends GenericEntity> T findEntity(String globalId) throws GmSessionException;

	/**
	 * Works like {@link #entity(EntityReference)}.
	 */
	@JsIgnore
	<T extends GenericEntity> EntityAccessBuilder<T> entity(String typeSignature, Object id);
	
	@JsIgnore
	<T extends GenericEntity> EntityAccessBuilder<T> entity(String typeSignature, Object id, String partition);

	/**
	 * Creates an {@link EntityAccessBuilder} which can be used to query/find the entity specified by the passed
	 * <code>entityReference</code>.
	 */
	@JsIgnore
	<T extends GenericEntity> EntityAccessBuilder<T> entity(EntityReference entityReference);

	/**
	 * Works like {@link #entity(EntityReference)}.
	 */
	@JsIgnore
	<T extends GenericEntity> EntityAccessBuilder<T> entity(EntityType<T> entityType, Object id);
	
	@JsIgnore
	<T extends GenericEntity> EntityAccessBuilder<T> entity(EntityType<T> entityType, Object id, String partition);

	/**
	 * Gets the {@link EntityReference} for the specified <code>entity</code> and then works like
	 * {@link #entity(EntityReference)}.
	 */
	<T extends GenericEntity> EntityAccessBuilder<T> entity(T entity);	
}
