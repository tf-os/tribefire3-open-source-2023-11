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
package com.braintribe.model.processing.query.smart.test2.shared;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFileDescriptor;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;

/**
 * This is for testing a use-case when a "shared" entity has one of it's properties unmapped for one of the delegates.
 * 
 * @author peter.gazdik
 */
public class SharedPartiallySmartSetup implements SmartTestSetupConstants {

	public static final SmartModelTestSetup SHARED_PARTIALLY_SETUP = get();

	private static SmartModelTestSetup get() {
		return _SharedSetupBase.get(SharedPartiallySmartSetup::configureMappings, "smart.shared-partially");
	}

	private static void configureMappings(SmartMappingEditor mapper) {
		mapper.onEntityType(GenericEntity.T, "root") //
				.allAsIs();

		mapper.onEntityType(SharedFile.T) //
				.forDelegate(accessIdB) //
				.propertyUnmapped(SharedFile.fileDescriptor);

		mapper.onEntityType(SharedFileDescriptor.T) //
				.forDelegate(accessIdB) //
				.entityUnmapped();
	}
}
