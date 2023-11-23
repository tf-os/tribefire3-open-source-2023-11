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
package com.braintribe.model.meta.data.mapping;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.meta.data.prompt.Name;

/**
 * This metadata allows to give entity types, property names, enum types and enum constants alternative names.
 * This is to be distinguished to the {@link Name} metadata which is an exclusive metadata while the Alias is a multi metadata.  
 * @author Dirk Scheffler
 *
 */
public interface Alias extends UniversalMetaData, HasName, ModelSkeletonCompatible {

	EntityType<Alias> T = EntityTypes.T(Alias.class);
	
	default Alias name(String name) {
		setName(name);
		return this;
	}
	
	static Alias create(String name) {
		return T.create().name(name);
	}
}
