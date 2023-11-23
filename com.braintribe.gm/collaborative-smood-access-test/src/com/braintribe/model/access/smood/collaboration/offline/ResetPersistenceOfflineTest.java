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
package com.braintribe.model.access.smood.collaboration.offline;

import org.junit.Test;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.smood.collaboration.manager.ResetPersistenceCsaTest;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;

/**
 * @see ResetPersistenceCsaTest
 * @see CollaborativeAccessManager
 *
 * @author peter.gazdik
 */
public class ResetPersistenceOfflineTest extends ResetPersistenceCsaTest {

	@Override
	protected <T> T eval(CollaborativePersistenceRequest request) {
		csaUnit.evalOffline(request);
		redeploy();
		return null;
	}

	@Override
	@Test
	public void resetsMultipleStages() {
		super.resetsMultipleStages();
	}

}
