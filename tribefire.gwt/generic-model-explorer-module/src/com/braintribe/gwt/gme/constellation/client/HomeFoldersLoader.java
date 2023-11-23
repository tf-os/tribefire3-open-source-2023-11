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
package com.braintribe.gwt.gme.constellation.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.model.workbench.WorkbenchPerspective;

/**
 * Loader used for loading folders for the Home constellation.
 * @author michel.docouto
 *
 */
public class HomeFoldersLoader implements Supplier<Future<List<?>>> {
	
	private ModelEnvironmentDrivenGmSession gmSession;
	private String currentAccessId;
	private Future<List<?>> future;
	private Function<KnownWorkenchPerspective, Future<WorkbenchPerspective>> workbenchPerspectiveFutureProvider;
	
	/**
	 * Configures the required provider used for providing the {@link WorkbenchPerspective} for the home folders.
	 */
	@Required
	public void setWorkbenchPerspectiveFutureProvider(Function<KnownWorkenchPerspective, Future<WorkbenchPerspective>> workbenchPerspectiveFuture) {
		this.workbenchPerspectiveFutureProvider = workbenchPerspectiveFuture;
	}
	
	/**
	 * Configures the required workbench session used for checking what is the current data access id.
	 */
	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public Future<List<?>> get() throws RuntimeException {
		if (gmSession.getModelEnvironment() == null)
			return new Future<List<?>>(new ArrayList<>());
		
		String dataAccessId = gmSession.getModelEnvironment().getDataAccessId();
		if (future != null && currentAccessId != null && currentAccessId.equals(dataAccessId))
			return future;
		
		future = new Future<>();
		currentAccessId = dataAccessId;
		
		if (currentAccessId == null) {
			future.onSuccess(Collections.emptyList());
			return future;
		}
		
		workbenchPerspectiveFutureProvider.apply(KnownWorkenchPerspective.homeFolder) //
				.andThen(result -> {
					if (result == null) {
						future.onSuccess(null);
						return;
					}

					gmSession.merge().adoptUnexposed(true).suspendHistory(true).doFor(result.getFolders(),
							com.braintribe.processing.async.api.AsyncCallback.of(future::onSuccess, future::onFailure));
				}).onError(future::onFailure);
		
		return future;
	}

}
