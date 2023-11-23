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
package com.braintribe.model.processing.manipulation.basic.oracle.base;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.VoidManipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * @author peter.gazdik
 */
public class ManipulationToolsTestBase {

	protected static VoidManipulation VOID_MANIPULATION = VoidManipulation.T.create();
	protected static String ACCESS_ID = "access.test.manipulations";

	protected Manipulation manipulation;
	protected Manipulation inducedManipulation = VOID_MANIPULATION;

	protected SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly(ACCESS_ID, null);
	protected PersistenceGmSession session = newSession();

	protected final ManipulationDriver md = new ManipulationDriver();

	protected PersistenceGmSession newSession() {
		return GmTestTools.newSession(access);
	}

	public ManipulationToolsTestBase() {
		/* This is important, otherwise the explicit partition assignments are ignored and induced manipulations are
		 * generated, which leads to problems when applying these induced manipulations */
		access.getDatabase().setIgnorePartitions(false);
	}

}
