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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

/**
 * An {@link AbstractImpCave} specialized in {@link IncrementalAccess}
 */
public class AccessImpCave extends AbstractImpCave<IncrementalAccess, AccessImp<IncrementalAccess>> {

	public AccessImpCave(PersistenceGmSession session) {
		super(session, "externalId", IncrementalAccess.T);
	}

	public AspectImpCave aspect() {
		return new AspectImpCave(session());
	}

	@Override
	protected AccessImp<IncrementalAccess> buildImp(IncrementalAccess instance) {
		return new AccessImp<IncrementalAccess>(session(), instance);
	}

	public CollaborativeSmoodAccessImp createCsa(String name, String externalId, GmMetaModel metaModel) {
		CollaborativeSmoodAccess access = createIA(CollaborativeSmoodAccess.T, name, externalId, metaModel);
		return new CollaborativeSmoodAccessImp(session(), access);
	}

	public CollaborativeSmoodAccessImp createCsa(String name, String externalId, GmMetaModel metaModel, GmMetaModel serviceModel) {
		CollaborativeSmoodAccess access = createIA(CollaborativeSmoodAccess.T, name, externalId, metaModel, serviceModel);
		return new CollaborativeSmoodAccessImp(session(), access);
	}

	public <T extends IncrementalAccess> AccessImp<T> createIncremental(EntityType<T> accessType, String name, String externalId,
			GmMetaModel metaModel) throws GmSessionException {
		T access = session().create(accessType);
		access.setName(name);
		access.setExternalId(externalId);
		access.setMetaModel(metaModel);
		return new AccessImp<T>(session(), access);
	}

	private <T extends IncrementalAccess> T createIA(EntityType<T> accessType, String name, String externalId, GmMetaModel metaModel)
			throws GmSessionException {
		T access = session().create(accessType);
		access.setName(name);
		access.setExternalId(externalId);
		access.setMetaModel(metaModel);
		return access;
	}

	public <T extends IncrementalAccess> AccessImp<T> createIncremental(EntityType<T> accessType, String name, String externalId,
			GmMetaModel metaModel, GmMetaModel serviceModel) throws GmSessionException {
		T access = createIA(accessType, name, externalId, metaModel);
		access.setServiceModel(serviceModel);
		return new AccessImp<T>(session(), access);
	}

	private <T extends IncrementalAccess> T createIA(EntityType<T> accessType, String name, String externalId, GmMetaModel metaModel,
			GmMetaModel serviceModel) throws GmSessionException {
		T access = createIA(accessType, name, externalId, metaModel);
		access.setServiceModel(serviceModel);
		return access;
	}

}
