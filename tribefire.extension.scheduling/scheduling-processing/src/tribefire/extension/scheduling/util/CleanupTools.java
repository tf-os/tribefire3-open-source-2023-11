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
package tribefire.extension.scheduling.util;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;

public class CleanupTools {

	private static final Logger logger = Logger.getLogger(CleanupTools.class);

	public static Maybe<Boolean> deleteEntityRecursively(Supplier<PersistenceGmSession> sessionSupplier, EntityType<? extends GenericEntity> type,
			String id) {

		try {
			deleteEntityWithDeleteMode(sessionSupplier, type, id, null);
		} catch (Exception e2) {
			logger.debug(() -> "Error while trying to clean up entity: " + type.getShortName() + " / " + id, e2);
			return Maybe.empty(Reason.create("Could not delete entity."));
		}

		return Maybe.complete(true);

	}

	private static void deleteEntityWithDeleteMode(Supplier<PersistenceGmSession> sessionSupplier, EntityType<? extends GenericEntity> type,
			String id, DeleteMode deleteMode) throws Exception {

		PersistenceGmSession session = sessionSupplier.get();
		EntityQuery query = EntityQueryBuilder.from(type).where().property(GenericEntity.id).eq(id).tc(Commons.ALL_TC).done();
		GenericEntity elementToBeDeleted = session.query().entities(query).first();
		if (elementToBeDeleted == null) {
			return;
		}

		EntityCollector collector = new EntityCollector();
		collector.visit(elementToBeDeleted);
		Set<GenericEntity> entities = collector.getEntities();

		Set<Resource> resources = entities.stream().filter(ge -> ge instanceof Resource).map(ge -> (Resource) ge).collect(Collectors.toSet());
		resources.forEach(r -> {
			try {
				deleteResourcePhysically(session, r);
			} catch (Exception e) {
				logger.debug(() -> "Error while trying to physically delete Resource " + r, e);
			}
		});

		for (GenericEntity ge : entities) {
			if (deleteMode == null) {
				session.deleteEntity(ge);
			} else {
				session.deleteEntity(ge, deleteMode);
			}
		}

		session.commit();
	}

	private static void deleteResourcePhysically(PersistenceGmSession session, Resource r) {
		if (r == null) {
			return;
		}
		try {
			DeleteResource deleteResource = DeleteResource.T.create();
			deleteResource.setResource(r);
			deleteResource.eval(session).get();
		} catch (NotFoundException nfe) {
			logger.trace(() -> "Could not find a physical file/entry for resource " + r.getId());
		} catch (Exception e) {
			logger.trace(() -> "Error while invoking DeleteResource for resource " + r.getId(), e);
		}
	}
}
