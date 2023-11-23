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
package com.braintribe.gwt.gmview.action.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.workbench.QueryAction;
import com.braintribe.processing.async.api.AsyncCallback;


/**
 * Provider used for providing a list of {@link QueryAction}s.
 * @author michel.docouto
 *
 */
public class QueryActionsProvider implements Supplier<Future<List<QueryAction>>> {
	
	private ModelEnvironmentDrivenGmSession gmSession;
	
	/**
	 * Configures the required session used for looking for {@link QueryAction}s.
	 */
	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Future<List<QueryAction>> get() {
		Future<List<QueryAction>> future = new Future<>();
		
		if (gmSession.getModelEnvironment().getDataAccessId() == null) {
			future.onSuccess(null);
			return future;
		}
		
		gmSession.query().entities(prepareEntityQuery()).result(AsyncCallback.of( //
				entityQueryResultConvenience -> {
					if (entityQueryResultConvenience == null) {
						future.onSuccess(null);
						return;
					}

					try {
						EntityQueryResult result = entityQueryResultConvenience.result();
						if (result != null) {
							future.onSuccess((List) result.getEntities());
							return;
						}
					} catch (GmSessionException e) {
						future.onFailure(e);
					}

					future.onSuccess(null);
				}, future::onFailure));
		
		return future;
	}
	
	private EntityQuery prepareEntityQuery() {
		return EntityQueryBuilder.from(QueryAction.class).tc(TC.create().negation().joker().done()).done();
	}

}
