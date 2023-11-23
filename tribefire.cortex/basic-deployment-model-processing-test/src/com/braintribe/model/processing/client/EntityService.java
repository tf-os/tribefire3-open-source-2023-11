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
package com.braintribe.model.processing.client;

import java.util.List;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * Class with some convenience methods for manipulating entities via {@link PersistenceGmSession}
 */
public class EntityService {

	private PersistenceGmSession session;

	public EntityService(PersistenceGmSession session) {
		this.session = session;
	}

	public void deleteEntity(String typeSignature, Object id) {
		System.out.println("\nDeleting " + typeSignature + " where ${id}==" + id);
		GenericEntity ge = get(typeSignature, id);

		if (ge == null) {
			System.out.println("empty");
			return;
		}

		deleteEntity(ge);
	}

	public void deleteEntity(GenericEntity ge) {
		deleteEntity(ge, true);
	}
	
	public void deleteEntity(GenericEntity ge, boolean verbose) {
		if (verbose)
			System.out.println("Deleting: " + ge);
		
		session.deleteEntity(ge);
		try {
			session.commit();
		} catch (GmSessionException e) {
			throw new RuntimeException("Deleting entity failed. Entity: " + ge, e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends GenericEntity> T get(String typeSignature, Object id) {
		String idName = getIdProperty(typeSignature).getName();
		EntityQuery query = EntityQueryBuilder.from(typeSignature).where().property(idName).eq(id).done();
		List<GenericEntity> list = execute(query);

		if (list.isEmpty()) {
			return null;
		}

		return (T) list.get(0);
	}

	public List<GenericEntity> list(String typeSignature) {
		return execute(EntityQueryBuilder.from(typeSignature).done());
	}

	private List<GenericEntity> execute(EntityQuery query) {
		try {
			return session.query().entities(query).list();

		} catch (GmSessionException e) {
			throw new RuntimeException("Query execution failed.", e);
		}
	}

	private Property getIdProperty(String typeSignature) {
		EntityType<? extends GenericEntity> et = GMF.getTypeReflection().getType(typeSignature);

		for (Property p: et.getProperties()) {
			if (p.isIdentifier()) {
				return p;
			}
		}

		throw new RuntimeException("Id property not foun for: " + typeSignature);
	}
}
