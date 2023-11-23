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

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.meta.ProcessWithComponent;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public class CoreDdsaInitializer extends SimplePersistenceInitializer {

	private Map<EntityType<? extends ServiceRequest>, EntityType<? extends Deployable>> requestToComponent;

	@Required
	public void setRequestToComponent(Map<EntityType<? extends ServiceRequest>, EntityType<? extends Deployable>> requestToComponent) {
		this.requestToComponent = requestToComponent;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession session = context.getSession();

		// TODO OPTIMIZE - use only two queries, rather than a pair for each entry in the map
		for (Entry<EntityType<? extends ServiceRequest>, EntityType<? extends Deployable>> entry : requestToComponent.entrySet()) {
			EntityType<?> requestType = entry.getKey();
			EntityType<?> componentType = entry.getValue();

			GmEntityType gmRequestType = queryGmEntityType(session, requestType);
			GmEntityType gmComponentType = queryGmEntityType(session, componentType);

			ProcessWithComponent processWithComponent = session.create(ProcessWithComponent.T);
			processWithComponent.setComponentType(gmComponentType);
			processWithComponent.setGlobalId("process-with-component:" + requestType.getTypeSignature());

			gmRequestType.getMetaData().add(processWithComponent);
		}
	}

	private GmEntityType queryGmEntityType(ManagedGmSession session, EntityType<?> entityType) throws ManipulationPersistenceException {
		try {
			EntityQuery query = buildQueryForType(entityType);
			return session.query().entities(query).unique();

		} catch (GmSessionException e) {
			throw new ManipulationPersistenceException("Error while querying type: " + entityType.getTypeSignature(), e);
		}
	}

	private EntityQuery buildQueryForType(EntityType<?> requestType) {
		return EntityQueryBuilder.from(GmEntityType.class).where().property("typeSignature").eq(requestType.getTypeSignature()).done();
	}

}
