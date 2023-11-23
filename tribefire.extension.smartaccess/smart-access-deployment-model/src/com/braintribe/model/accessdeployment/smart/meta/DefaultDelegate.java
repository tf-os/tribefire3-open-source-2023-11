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
package com.braintribe.model.accessdeployment.smart.meta;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * Specifies the default access to which a newly instantiated entity should be persisted, in case there is no other way
 * to determine it (explicit assignment, relationships with entities whose access is uniquely determined).
 * 
 * For more information about this partition inference see SmartAccess javadoc (there is a class called AccessResolver).
 */
public interface DefaultDelegate extends EntityTypeMetaData {

	EntityType<DefaultDelegate> T = EntityTypes.T(DefaultDelegate.class);

	IncrementalAccess getAccess();
	void setAccess(IncrementalAccess access);

}
