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
package tribefire.platform.impl.initializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.tools.PreparedQueries;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.service.domain.ServiceDomain;

public class ServiceDomainInitializer extends SimplePersistenceInitializer {

	private final Map<String, ServiceDomainInfo> serviceDomainModels = new HashMap<>();

	public ServiceDomainInitializer() {
	}

	public ServiceDomainInitializer register(String globalId, String externalId, String modelName, String domainName) {
		serviceDomainModels.put(globalId, new ServiceDomainInfo(externalId, modelName, domainName));
		return this;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {

		ManagedGmSession session = context.getSession();

		for (Entry<String, ServiceDomainInfo> e : serviceDomainModels.entrySet()) {

			GmMetaModel model = queryMetaModel(session, e.getValue().modelName);

			ServiceDomain serviceDomain = session.create(ServiceDomain.T, e.getKey());
			serviceDomain.setExternalId(e.getValue().externalId);
			serviceDomain.setName(e.getValue().domainName);
			serviceDomain.setServiceModel(model);

		}

	}

	private GmMetaModel queryMetaModel(ManagedGmSession session, String modelName) throws ManipulationPersistenceException {
		GmMetaModel result = session.query().select(PreparedQueries.modelByName(modelName)).unique();
		if (result == null) {
			throw new ManipulationPersistenceException("Model not found in the context session: " + modelName);
		}
		return result;
	}

	private static class ServiceDomainInfo {

		ServiceDomainInfo(String externalId, String modelName, String domainName) {
			this.externalId = externalId;
			this.modelName = modelName;
			this.domainName = domainName;
		}

		private final String externalId;
		private final String modelName;
		private final String domainName;

	}

}
