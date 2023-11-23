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

import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

/**
 * An {@link AbstractImpCave} specialized in {@link WebTerminal}
 */
public class WebTerminalImpCave extends AbstractImpCave<WebTerminal, WebTerminalImp> {

	public WebTerminalImpCave(PersistenceGmSession session) {
		super(session, "externalId", WebTerminal.T);
	}

	@Override
	public WebTerminalImp buildImp(WebTerminal instance) {
		return new WebTerminalImp(session(), instance);
	}

	public <T extends WebTerminal> BasicDeployableImp<T> create(EntityType<T> entityType, String name, String externalId, String pathIdentifier) {
		logger.info("Creating WebTerminal with the name '" + name + "'");
		T webTerminal = session().create(entityType);
		webTerminal.setName(name);
		webTerminal.setExternalId(externalId);
		webTerminal.setPathIdentifier(pathIdentifier);
		return new BasicDeployableImp<T>(session(), webTerminal);
	}
}
