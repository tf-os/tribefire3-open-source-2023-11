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
package com.braintribe.model.processing.accessory.impl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.domain.ServiceDomain;

/**
 * Standard service domain bound {@link AbstractDerivingModelAccessory}.
 * 
 */
public class ServiceDomainModelAccessory extends AbstractDerivingModelAccessory {

	// constants
	private static final Logger log = Logger.getLogger(ServiceDomainModelAccessory.class);

	// configurable
	private String serviceDomainId;
	private String defaultServiceDomainId;

	// cached
	private String modelName;

	@Required
	@Configurable
	public void setServiceDomainId(String serviceDomainId) {
		this.serviceDomainId = serviceDomainId;
	}

	@Configurable
	public void setDefaultServiceDomainId(String defaultServiceDomainId) {
		this.defaultServiceDomainId = defaultServiceDomainId;
	}

	@Override
	protected String getModelName() {
		PersistenceGmSession cortexSession = cortexSessionProvider.get();

		modelName = queryServiceModelName(cortexSession, serviceDomainId);

		if (modelName == null && defaultServiceDomainId != null)
			modelName = queryServiceModelName(cortexSession, defaultServiceDomainId);

		if (modelName == null)
			throw new IllegalStateException(misconfigurationMessage());

		return modelName;
	}

	private String queryServiceModelName(PersistenceGmSession cortexSession, String serviceDomainId) {
		SelectQuery query = prepareServiceModelNameQuery(serviceDomainId);

		String modelName = cortexSession.query().select(query).unique();

		if (modelName == null)
			log.debug(() -> "No service model name found for serviceDomainId='" + serviceDomainId + "'");
		else
			log.debug(() -> "Found service model for serviceDomainId='" + serviceDomainId + "': modelName='" + modelName + "'");

		return modelName;
	}

	private SelectQuery prepareServiceModelNameQuery(String serviceDomainId) {
		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
				.from(ServiceDomain.T, "d")
				.where()
					.property("d", ServiceDomain.externalId).eq(serviceDomainId)
				.select("d", ServiceDomain.serviceModel+".name")
			.done();
		return query;
		// @formatter:on
	}

	private String misconfigurationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("No service model could be found based on the service domain '").append(serviceDomainId).append("' ");
		if (defaultServiceDomainId != null) {
			sb.append("or the fallback domain '").append(defaultServiceDomainId).append("'.");
		} else {
			sb.append("and no default fallback domain id is configured.");
		}
		sb.append(" Please ensure that a ").append(ServiceDomain.class.getName());
		sb.append(" instance matching the external id '").append(serviceDomainId).append("'");
		if (defaultServiceDomainId != null) {
			sb.append(" or '").append(defaultServiceDomainId).append("'");
		}
		sb.append(" exists and that the ").append(ServiceDomain.serviceModel).append(" property is set on it.");
		return sb.toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [serviceDomainId='" + serviceDomainId + "', modelName='"
				+ modelName + "']";
	}

}
