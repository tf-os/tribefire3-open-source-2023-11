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
package com.braintribe.qa.tribefire.qatests.other;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpApi;

/**
 * This class tests the Resource upload and download.
 *
 */
public class ResourcesTest extends AbstractResourcesTest {

	@Override
	protected PersistenceGmSession newSession() {
		ImpApi imp = apiFactory().build();
		PersistenceGmSession uploadSession = imp.session();
		return uploadSession;
	}

	@Override
	protected String getUserName() {
		return "cortex";
	}
}
