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
package com.braintribe.model.processing.query.smart.test.model.shared;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartGenericEntity;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;

/**
 * Default partition -> {@link SmartMappingSetup#accessIdA}
 */
public interface SharedFileDescriptor extends SharedEntity, SmartGenericEntity {
	
	EntityType<SharedFileDescriptor> T = EntityTypes.T(SharedFileDescriptor.class);

	SharedFile getFile();
	void setFile(SharedFile file);

}
