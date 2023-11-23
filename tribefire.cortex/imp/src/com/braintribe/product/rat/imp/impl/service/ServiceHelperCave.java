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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.product.rat.imp.AbstractHasSession;

/**
 * An entrance point in the "ServiceRequest" part of the ImpApi. Helps you to create and manage any service requests
 */
public class ServiceHelperCave extends AbstractHasSession {

	public ServiceHelperCave(PersistenceGmSession session) {
		super(session);
	}

	/**
	 * Creates a {@link ServiceHelper} for passed request and result type. If you pass a wrong result type, a
	 * {@link ClassCastException} might happen later during calling the request
	 */
	public <S extends ServiceRequest, R extends GenericEntity> ServiceHelper<S, R> with(S serviceRequest,
			@SuppressWarnings("unused") EntityType<R> serviceResultType) {
		return new ServiceHelper<S, R>(session(), serviceRequest);
	}

	/**
	 * Creates a {@link ServiceHelper} for passed request
	 */
	public <S extends ServiceRequest> ServiceHelper<S, GenericEntity> with(S serviceRequest) {
		return new ServiceHelper<S, GenericEntity>(session(), serviceRequest);
	}

	/**
	 * Use this method if the response type of your service request extends {@link HasNotifications}. This gives you
	 * additional features to work with the response notifications. Otherwise use
	 * {@link #with(ServiceRequest, EntityType)}
	 * <p>
	 * Creates a {@link ServiceHelper} for passed request and result type. If you pass a wrong result type, a
	 * {@link ClassCastException} might happen later during calling the request
	 */
	public <S extends ServiceRequest, R extends HasNotifications> ServiceHelperWithNotificationResponse<S, R> withNotificationResponse(S serviceRequest,
			@SuppressWarnings("unused") EntityType<R> serviceResultType) {
		return new ServiceHelperWithNotificationResponse<S, R>(session(), serviceRequest);
	}

	/**
	 * Use this method if the response type of your service request extends {@link HasNotifications}. This gives you
	 * additional features to work with the response notifications. Otherwise use {@link #with(ServiceRequest)}
	 * <p>
	 * Creates a {@link ServiceHelper} for passed request
	 */
	public <S extends ServiceRequest> ServiceHelperWithNotificationResponse<S, HasNotifications> withNotificationResponse(S serviceRequest) {
		return new ServiceHelperWithNotificationResponse<S, HasNotifications>(session(), serviceRequest);
	}

}
