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
package com.braintribe.model.processing.management;

import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class MetaModelFixer {
	private static final Logger logger = Logger.getLogger(MetaModelFixer.class);
	private PersistenceGmSession gmSession;

	@Configurable
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	public void ensureAll(GmMetaModel model) {
		Objects.requireNonNull(model);
		ensureEssentialTypes(model);
		ensureDeclaringModel(model);
	}

	public void ensureEssentialTypes(GmMetaModel model) {
		Objects.requireNonNull(model);
		ensureBaseType(model);
		ensureSimpleTypes(model);

	}

	public void ensureDeclaringModel(GmMetaModel model) {
		try {

		} catch (Exception e) {
			throw new MetaModelSyncException(
					"Error while detecting declaring model for types of model: " + model.getName(), e);
		}
	}

	protected void ensureSimpleTypes(GmMetaModel gmMetaModel) {
	}

	protected void ensureBaseType(GmMetaModel gmMetaModel) {
	}

	private <T extends GmType> T acquireType(EntityType<T> et, String typeSignature) throws MetaModelSyncException {
		return null;
	}

}
