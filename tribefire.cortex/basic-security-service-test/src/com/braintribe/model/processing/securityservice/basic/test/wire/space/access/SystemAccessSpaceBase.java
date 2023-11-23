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
package com.braintribe.model.processing.securityservice.basic.test.wire.space.access;

import java.util.function.Supplier;

import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public abstract class SystemAccessSpaceBase implements WireSpace {

	public abstract String id();
	
	public abstract String modelName();

	public String serviceModelName() {
		return null;
	}

	public abstract IncrementalAccess rawAccess();

	//@Managed(Scope.prototype)
	public PersistenceGmSession lowLevelSession() {
		BasicPersistenceGmSession bean = new BasicPersistenceGmSession();
		bean.setIncrementalAccess(rawAccess());
		return bean;
	}

	protected Smood smood() {
		Smood bean = new Smood(new MutuallyExclusiveReadWriteLock());
		bean.setAccessId(id());
		return bean;
	}

	@Managed
	public Supplier<GmMetaModel> metaModelProvider() {
		return () -> GMF.getTypeReflection().getModel(modelName()).getMetaModel();
	}

}
