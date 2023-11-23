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
package com.braintribe.model.meta.data.prompt;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.UniversalMetaData;

/**
 * This metadata should be set on an {@link EntityType} which should have its editions auto committed by the client.
 * This means, users would not need to click the Commit button in order to have its manipulations performed.
 * Erasure is {@link ManualCommit}
 *
 */
public interface AutoCommit extends UniversalMetaData, ExplicitPredicate {
	
	EntityType<AutoCommit> T = EntityTypes.T(AutoCommit.class);

}
