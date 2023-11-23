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
package com.braintribe.model.processing.query.smart.test.model.smart.shared;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.shared.SourceOwnerA;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedSource;
import com.braintribe.model.processing.query.smart.test.model.smart.StandardSmartIdentifiable;

/**
 * Mapped to {@link SourceOwnerA}.
 */

public interface SmartSourceOwnerA extends StandardSmartIdentifiable {

	EntityType<SmartSourceOwnerA> T = EntityTypes.T(SmartSourceOwnerA.class);

	String getName();
	void setName(String name);

	SharedSource getSharedSource();
	void setSharedSource(SharedSource sharedSource);

	// ###################################
	// ## . . . . . . KPA . . . . . . . ##
	// ###################################

	// based on keyProperty - SharedSourceOwnerA.kpaSharedSourceUuid;
	SharedSource getKpaSharedSource();
	void setKpaSharedSource(SharedSource kpaSharedSource);

	// based on keyProperty - SharedSourceOwnerA.kpaSharedSourceUuidSet;
	Set<SharedSource> getKpaSharedSourceSet();
	void setKpaSharedSourceSet(Set<SharedSource> kpaSharedSourceSet);

}
