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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.workbench.InstantiationAction;
import com.google.gwt.core.client.Scheduler;

/**
 * Expert which will return a list of {@link InstantiationAction}s for the given {@link EntityType}.
 * @author michel.couto
 *
 */
public class EntityTypeInstantiationActionsProvider implements Function<EntityType<?>, Future<List<InstantiationAction>>> {
	
	private Supplier<Future<List<InstantiationAction>>> instantiationActionsSupplier;
	private ManagedGmSession gmSession;
	private Future<List<InstantiationAction>> future;
	
	/**
	 * Configures the required {@link Supplier} of {@link InstantiationAction}.
	 */
	@Required
	public void setInstantiationActionsSupplier(Supplier<Future<List<InstantiationAction>>> instantiationActionsSupplier) {
		this.instantiationActionsSupplier = instantiationActionsSupplier;
	}
	
	/**
	 * Configures the session.
	 */
	@Required
	public void setGmSession(ManagedGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public Future<List<InstantiationAction>> apply(EntityType<?> entityType) {
		Future<List<InstantiationAction>> resultFuture = new Future<>();
		
		if (future == null)
			future = instantiationActionsSupplier.get();
		
		future.andThen(instantiationActions -> {
			// Using this to make sure this is async. After the first use, the instantiationActionsProvider returns
			// directly the result.
			Scheduler.get().scheduleDeferred(() -> {
				List<InstantiationAction> result = new ArrayList<>();
				if (instantiationActions != null) {
					for (InstantiationAction instantiationAction : instantiationActions) {
						TraversingCriterion inplaceContextCriterion = instantiationAction.getInplaceContextCriterion();
						if (inplaceContextCriterion instanceof TypeConditionCriterion) {
							GmType gmType = gmSession.getModelAccessory().getOracle().<GmType> findGmType(entityType.getTypeSignature());
							if (gmType != null && ((TypeConditionCriterion) inplaceContextCriterion).getTypeCondition().matches(gmType))
								result.add(instantiationAction);
						}
					}
				}

				resultFuture.onSuccess(result);
			});
		}).onError(resultFuture::onFailure);
		
		return resultFuture;
	}

}
