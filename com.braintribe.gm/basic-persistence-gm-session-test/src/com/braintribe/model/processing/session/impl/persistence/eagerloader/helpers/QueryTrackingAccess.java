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
package com.braintribe.model.processing.session.impl.persistence.eagerloader.helpers;

import java.util.Set;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.EagerLoaderSupportingAccess;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * @author peter.gazdik
 */
public class QueryTrackingAccess implements EagerLoaderSupportingAccess {

	private final IncrementalAccess delegate;
	private final AccessQueryListener queryListener;

	public QueryTrackingAccess(IncrementalAccess delegate, AccessQueryListener queryListener) {
		this.delegate = delegate;
		this.queryListener = queryListener;
	}

	@Override
	public String getAccessId() {
		return delegate.getAccessId();
	}

	@Override
	public GmMetaModel getMetaModel() throws GenericModelException {
		return delegate.getMetaModel();
	}

	@Override
	public SelectQueryResult query(SelectQuery query, PersistenceGmSession session) throws ModelAccessException {
		checkCriteriaIsScalarOnly(query);

		return session.query().select(query).result();
	}

	private void checkCriteriaIsScalarOnly(SelectQuery query) {
		if (query.getTraversingCriterion() != PreparedTcs.scalarOnlyTc)
			throw new RuntimeException("ERROR IN TEST. We assume the TC for this query triggered by the EagerLoader only loads scalar properties"
					+ ", but something changed in the implementation.");
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		queryListener.onQuery(query);
		return delegate.query(query);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
		queryListener.onQuery(query);
		return delegate.queryEntities(query);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException {
		queryListener.onQuery(query);
		return delegate.queryProperty(query);
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		return delegate.applyManipulation(manipulationRequest);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		return delegate.getReferences(referencesRequest);
	}

	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		return delegate.getPartitions();
	}

}
