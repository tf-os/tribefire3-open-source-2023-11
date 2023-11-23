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
package com.braintribe.product.rat.imp.impl.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.product.rat.imp.AbstractHasSession;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.product.rat.imp.impl.utils.GeneralGmUtils;
import com.braintribe.utils.CollectionTools;

/**
 * A helper class for managing {@link ServiceRequest}. It is not classified as an imp, as its instance does not have to
 * be persisted in its session's access.
 *
 * @param <S>
 *            Type of ServiceRequest managed by this imp
 * @param <R>
 *            Type of ServiceResult that belongs to this imp's ServiceRequest
 */
public class ServiceHelper<S extends ServiceRequest, R> extends AbstractHasSession {

	private final S instance;
	protected R serviceResult;

	protected Function<R, Boolean> verifyingResultBy;
	
	private final Collection<GenericEntity> additionalEntitiesToRefresh;

	public ServiceHelper(PersistenceGmSession session, S serviceRequest) {
		super(session);

		if (serviceRequest == null) {
			throw new ImpException("Invalid arguments: serviceRequest can't be null");
		}

		this.instance = serviceRequest;
		
		this.additionalEntitiesToRefresh = new ArrayList<>();
	}

	/**
	 * Calls/evaluates the service request and tries to make sure everything is up to date in your session afterwards:
	 * <p>
	 * Evaluates passed service requests and then generically refreshes all entities <u>that were passed as
	 * arguments</u> in the service request, which means their properties will hold current values from AFTER the
	 * service call.
	 * <p>
	 * If you set up a verification predicate with {@link #verifyingResultBy} there will also be a warning in the log
	 * output if the result verification failed. If you used a method like
	 * <code>impApiInstance.service().[...]request</code> to create this helper, result verification is already in
	 * place.
	 * <hr>
	 * <i>Hint</i>: In the rare case when the changed entity was not passed as an argument to your service request
	 * directly call {@link GeneralGmUtils#refreshEntity(GenericEntity)} or
	 * {@link GeneralGmUtils#refreshProperties(GenericEntity, String...)}
	 *
	 * @return service result
	 */
	public R call() {
		return call_internal(true);
	}

	/**
	 * Simply evaluates passed service request. notice that any changes done by this service might not be updated
	 * automatically in your session. Also other features of this helper like verifying success and automatically
	 * logging notifications will not be triggered<br>
	 * Use {@link #call()} to fix that <br>
	 * or directly call {@link GeneralGmUtils#refreshEntity(GenericEntity)} or
	 * {@link GeneralGmUtils#refreshProperties(GenericEntity, String...)}
	 * <p>
	 * <b>Note:</b> As result verification often makes no sense if the related entities are not refreshed this is
	 * omitted here as well
	 *
	 * @return service result
	 */
	public R callWithoutRefresh() {
		return call_internal(false);
	}

	private R call_internal(boolean doRefresh) {
		serviceResult = (R) session().eval(instance).get();

		if (doRefresh) {
			doRefresh();

			if (verifyingResultBy != null && verifyingResultBy.apply(serviceResult) != true) {
				logger.warn("There was an issue during the service call of " + instance);
				printWarning();
			}
		}

		return serviceResult;
	}

	private void doRefresh() {
		for (Property property : instance.entityType().getProperties()) {
			// Handle simple entities
			if (property.getType().isEntity()) {
				GenericEntity parameterEntity = property.get(instance);

				if (parameterEntity != null) {
					GeneralGmUtils.refreshEntity(parameterEntity);
				}
			}
			// Handle entities in collections
			else if (property.getType().isCollection()) {
				Set<GenericEntity> entitiesToRefresh = new HashSet<>();

				if (property.get(instance) instanceof Map) {
					Map<?, ?> map = property.get(instance);
					MapType mapType = (MapType) property.getType();

					if (mapType.getKeyType().isEntity()) {
						entitiesToRefresh.addAll((Collection<? extends GenericEntity>) map.keySet());
					}
					if (mapType.getValueType().isEntity()) {
						entitiesToRefresh.addAll((Collection<? extends GenericEntity>) map.values());
					}

				} else {
					CollectionType collectionType = (CollectionType) property.getType();
					Collection<?> collection = property.get(instance);

					if (collectionType.getCollectionElementType().isEntity()) {
						entitiesToRefresh.addAll((Collection<? extends GenericEntity>) collection);
					}
				}

				CollectionTools.removeNulls(entitiesToRefresh);
				entitiesToRefresh.forEach(GeneralGmUtils::refreshEntity);
			}
		}
		
		additionalEntitiesToRefresh.forEach(GeneralGmUtils::refreshEntity);
		
	}

	/**
	 * This lets you define whether a service call was successful or not. If the passed function returns false or null,
	 * there will be a logger.warn output<br>
	 * Note: Depending on how you created this ServiceHelper instance there might already be a default result
	 * verificator which you would override
	 *
	 * @param verifyingResultBy
	 *            a function that takes the service result and returns whether the service call was successful or not.
	 *            null is handled like false
	 * @return itself
	 */
	public ServiceHelper<S, R> verifyingResultBy(Function<R, Boolean> verifyingResultBy) {
		this.verifyingResultBy = verifyingResultBy;
		return this;
	}

	/**
	 * {@link #verifyingResultBy(Function)}
	 */
	public ServiceHelper<S, R> verifyingResultBy(Supplier<Boolean> verifyingResultBy) {
		this.verifyingResultBy = (r) -> verifyingResultBy.get();

		return this;
	}

	protected void printWarning() {
		// Do nothing. Can be Overridden
	}

	public S get() {
		return instance;
	}
	
	public ServiceHelper<S, R> addEntitiesForAutoRefresh(Collection<? extends GenericEntity> entities) {
		additionalEntitiesToRefresh.addAll(entities);
		return this;
	}
}
