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
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * A {@link BasicDeployableImp} specialized in {@link IncrementalAccess}
 *
 * @param <T>
 *            type of access, this imp is specialized in
 */
public class AccessImp<T extends IncrementalAccess> extends BasicDeployableImp<T> {

	public AccessImp(PersistenceGmSession session, T access) {
		super(session, access);
	}

	public AccessImp<T> addAspect(AccessAspect accessAspect) {
		logger.debug("Adding an access aspect '" + accessAspect.getExternalId() + "' to the access '" + instance.getExternalId() + "'");
		AspectConfiguration aspectConfiguration = session().create(AspectConfiguration.T);
		aspectConfiguration.setAspects(CommonTools.getList(accessAspect));
		instance.setAspectConfiguration(aspectConfiguration);
		return this;
	}

}
