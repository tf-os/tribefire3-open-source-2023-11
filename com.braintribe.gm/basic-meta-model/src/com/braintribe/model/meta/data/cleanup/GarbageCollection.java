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
package com.braintribe.model.meta.data.cleanup;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * Specifies how entities of the respective type are handled by the <code>GarbageCollection</code>.
 * 
 * @see GarbageCollectionKind
 * 
 * @author michael.lafite
 */
public interface GarbageCollection extends EntityTypeMetaData {

	EntityType<GarbageCollection> T = EntityTypes.T(GarbageCollection.class);

	GarbageCollectionKind getKind();
	void setKind(GarbageCollectionKind kind);

}
