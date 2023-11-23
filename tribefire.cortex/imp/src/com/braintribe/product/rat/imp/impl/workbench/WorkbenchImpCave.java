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
package com.braintribe.product.rat.imp.impl.workbench;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;
import com.braintribe.product.rat.imp.Imp;
import com.braintribe.product.rat.imp.impl.deployable.AccessImpCave;
import com.braintribe.product.rat.imp.impl.deployable.WebTerminalImpCave;

// TODO Under Construction
public class WorkbenchImpCave extends AbstractImpCave<Deployable, Imp<Deployable>> {

	public WorkbenchImpCave(PersistenceGmSession session) {
		super(session, "externalId", Deployable.T);
	}

	public AccessImpCave access() {
		return new AccessImpCave(session());
	}

	public WebTerminalImpCave webTerminal() {
		return new WebTerminalImpCave(session());
	}

	// public WorkerImp worker() {
	// return new WorkerImp(session);
	// }

	@Override
	protected Imp<Deployable> buildImp(Deployable instance) {
		// TODO
		throw new NotImplementedException(WorkbenchImpCave.class.getName() + " is not implemented yet. Please try again later.");
	}
}
