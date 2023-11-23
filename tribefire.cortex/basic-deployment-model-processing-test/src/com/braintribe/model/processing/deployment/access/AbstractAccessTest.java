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
package com.braintribe.model.processing.deployment.access;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.client.EntityService;
import com.braintribe.model.processing.client.SetupTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * 
 */
public abstract class AbstractAccessTest {

	protected static String accessId;

	protected static void listAllEntities(Class<? extends GenericEntity> clazz) {
		listAllEntities(clazz.getName());
	}

	protected static void listAllEntities(String typeSignature) {
		System.out.println("\nListing all instances of " + typeSignature);
		List<GenericEntity> list = queryAllEntities(typeSignature);

		if (list.isEmpty()) {
			System.out.println("empty");
			return;
		}

		for (GenericEntity ge: list) {
			System.out.println(ge);
		}
	}

	protected static void deleteAllEntities(Class<? extends GenericEntity> clazz) {
		String typeSignature = clazz.getName();
		System.out.println("\nDeleting all instances of " + typeSignature);

		EntityService newEntityService = newEntityService();

		for (GenericEntity ge: newEntityService.list(typeSignature)) {
			newEntityService.deleteEntity(ge, false);
		}
	}

	@SuppressWarnings("unchecked")
	protected static <T extends GenericEntity> List<T> queryAllEntities(Class<T> clazz) {
		return (List<T>) newEntityService().list(clazz.getName());
	}

	protected static List<GenericEntity> queryAllEntities(String typeSignature) {
		return newEntityService().list(typeSignature);
	}

	protected <T extends GenericEntity> T queryEntityByProperty(PersistenceGmSession session, Class<T> clazz, String propName,
			String value) {
		EntityQuery query = EntityQueryBuilder.from(clazz).where().property(propName).eq(value).done();
		try {
			List<GenericEntity> list = session.query().entities(query).list();

			return CollectionTools.isEmpty(list) ? null : clazz.cast(list.get(0));

		} catch (GmSessionException e) {
			throw new RuntimeException("Query failed", e);
		}
	}

	protected static EntityService newEntityService() {
		return new EntityService(createNewSession());
	}

	protected static PersistenceGmSession createNewSession() {
		return createNewSession(accessId);
	}

	protected static PersistenceGmSession createNewSession(String accessId) {
		return SetupTools.createNewSession(accessId);
	}

	protected static void commit(PersistenceGmSession session) {
		try {
			session.commit();
		} catch (GmSessionException e) {
			throw new RuntimeException("commit failed", e);
		}
	}

	protected static void assertFailedCommit(String msg, PersistenceGmSession session) {
		try {
			session.commit();
			throw new RuntimeException("Exception was expected. " + msg);

		} catch (GmSessionException ignored) {
			// ignore
		}
	}

}
