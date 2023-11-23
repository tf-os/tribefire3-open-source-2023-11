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
package com.braintribe.model.processing.assembly.sync.impl;

import java.util.Set;

import com.braintribe.model.exchange.ExchangePayload;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.assembly.sync.api.AssemblyImportContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author peter.gazdik
 */
public class ExchangeImporterContext implements AssemblyImportContext<ExchangePayload> {

	private final PersistenceGmSession session;
	private final ExchangePayload payload;
	private final boolean includeEnvelope;
	private final boolean requireAllGlobalIds;
	private final Set<GenericEntity> externalReferences;
	private final String defaultPartition;

	public ExchangeImporterContext(PersistenceGmSession session, ExchangePayload payload, boolean includeEnvelope, boolean requireAllGlobalIds,
			String defaultPartition) {

		this.session = session;
		this.payload = payload;
		this.includeEnvelope = includeEnvelope;
		this.requireAllGlobalIds = requireAllGlobalIds;
		this.defaultPartition = defaultPartition;
		this.externalReferences = payload.getExternalReferences();
	}

	@Override
	public ExchangePayload getAssembly() {
		return payload;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public boolean isExternalReference(GenericEntity entity) {
		return externalReferences.contains(entity);
	}

	@Override
	public boolean isEnvelope(GenericEntity entity) {
		return entity == payload;
	}

	@Override
	public boolean requireAllGlobalIds() {
		return requireAllGlobalIds;
	}

	@Override
	public boolean includeEnvelope() {
		return includeEnvelope;
	}

	@Override
	@Deprecated
	public String getDefaultPartition() {
		return defaultPartition;
	}
}
