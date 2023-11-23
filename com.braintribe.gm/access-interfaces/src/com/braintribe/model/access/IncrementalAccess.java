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
package com.braintribe.model.access;

import java.util.List;
import java.util.Set;

import com.braintribe.model.accessapi.CustomPersistenceRequest;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * {@link IncrementalAccess} defines the interface to varying kinds of data persistence layers.
 * 
 * @author gunther.schenk
 * 
 */
public interface IncrementalAccess extends ModelAccess, HasAccessId {

	String DEFAULT_TC_NAME = "default";

	/**
	 * Queries the {@link IncrementalAccess} with the given {@link SelectQuery} and returns a {@link SelectQueryResult}.
	 * 
	 * <h2>Specifications</h2>
	 * 
	 * <h3>Explicit sources</h3>
	 * 
	 * There are two types of Sources for our queries: {@link From}s, which directly represent an entity, and
	 * {@link Join}s, which represent a property of some entity. Given property may be used for a join iff it's of an
	 * entity type, or it is a collection (including collections of simple types) (same as in HQL).
	 * 
	 * <h3>Implicit joins</h3>
	 * 
	 * Besides these explicit joins specified in as sources, there are also implicit joins, which are "done" every time
	 * an entity or collection property is referenced. For example, assuming <tt>Person.address</tt> is an entity
	 * property of type <tt>Address</tt>, the query {@code select p.name, p.address from Person p} contains an implicit
	 * join with <tt>Address</tt>.
	 * <p>
	 * In this case, the semantics is DIFFERENT THAN in HIBERNATE, we assume all implicit joins are left-join (hibernate
	 * uses inner join), EXCEPT COLLECTIONS in SELECT clause. This means, taking the aforementioned query:
	 * {@code select p.name, p.address from Person p}, that in case some person's address is null, our result will
	 * contain a record like this: {@code ["someName", null]} (while in hibernate such record would be removed). This
	 * convention is applied for both the SELECT clause, as well as the WHERE clause.
	 * <p>
	 * The main motivation for left-joins is a somewhat odd behavior (in Hibernate) for a query like this:
	 * {@code from Person p where p.name like ? or p.address.street like ?}). It is possible, that removing the second
	 * condition might lead to actually getting more results, because that condition causes all persons where address is
	 * <tt>null</tt> to be removed from the result (Hibernate does an inner-join).
	 * <p>
	 * The reason why we handle COLLECTION in SELECT clause differently is, that <tt>null</tt> is a valid value for an
	 * entity-property, but not for a collection element. We want that collection property-queries (e.q.
	 * {@code select p.nickNames from Person p where p.id = ?}) return exactly as many result entries, as there are
	 * elements in the collection. In the WHERE clause, on the other hand, we want a left-join, for the same reason as
	 * discussed in previous paragraph (to avoid reducing number of results by adding more operands to a disjunction
	 * condition).
	 * <p>
	 * NOTE that the current implementation of HibernateAccess still applies inner-joins, the plan is to add a flag
	 * later to turn the left-joins on (so that by default it remains backwards compatible).
	 * 
	 * <h3>Retrieving collections</h3>
	 * 
	 * When it comes to projection (the SELECT clause), every value of every "record" retrieved is either an entity, or
	 * a simple-type/enum, never a collection. So for example: {@code select p.friendNames from Person p} returns a list
	 * which is concatenation of all the "friendNames" for all the Persons. This follows from the fact that referencing
	 * a collection property is actually a join. This means the previous query is equivalent to: <br>
	 * {@code select f from Person p join p.friendNames f} <br>
	 * One has to be therefore careful, because a query like
	 * {@code select p.friendNames from Person p join p.friendNames f} is equivalent to: <br>
	 * {@code select f2 from Person p join p.friendNames f join p.friendNames f2} <br>
	 * which for each person returns each friend as many times as there are number of friends.
	 * 
	 * <h3>Joining collections explicitly</h3>
	 * 
	 * When joining a collection property, the alias you give it will refer to an individual element of the collection,
	 * not the whole collection, and this rule is strictly followed. For example, consider this query: <br>
	 * {@code select f from Person p join p.friendNames f where 'John' in f} <br>
	 * While valid in HQL (though semantically equivalent to an equals condition), this is not a valid query in our
	 * context (<tt>f</tt> is not a collection, therefore cannot be a right operand of the <tt>in</tt> condition).
	 */
	SelectQueryResult query(SelectQuery query) throws ModelAccessException;

	/**
	 * Queries for a list of {@link GenericEntity}.
	 */
	EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException;

	/**
	 * Queries for a property of a {@link GenericEntity}.
	 * 
	 * In most cases this simply retrieves the property, but if the type of the property is a {@link Set}, it might be
	 * handled in a special way. In this case we allow conditions(1) and ordering. The ordering is especially tricky,
	 * because in that case we an instance of {@link List} is returned.
	 * 
	 * (1) - I think we should also allow condition for Lists/Maps, at least due to adjustments done by SecurityAspect.
	 */
	PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException;

	/**
	 * Applies the {@link Manipulation} from the given {@link ManipulationRequest} to the persistence layer and returns
	 * subsequent induced manipulation in the {@link ManipulationResponse}.
	 */
	ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException;

	/**
	 * Returns a list of {@link EntityProperty} containing the {@link EntityReference} and the propertyName referencing
	 * the given {@link PersistentEntityReference}
	 */
	ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException;

	/**
	 * Returns all possible values of the {@link GenericEntity#partition} property of entities retrieved via this
	 * access. In other words, every single entity retrieved via this access will have it's <tt>$partition</tt> value as
	 * one of the strings returned by this method.
	 * 
	 * NOTE that the resulting set is never empty and never contains <tt>null</tt>.
	 */
	Set<String> getPartitions() throws ModelAccessException;

	@SuppressWarnings("unused")
	default Object processCustomRequest(ServiceRequestContext context, CustomPersistenceRequest request) {
		throw new UnsupportedOperationException("Method 'processCustomRequest' is not supported for implementation type: " + getClass().getName());
	}

}
