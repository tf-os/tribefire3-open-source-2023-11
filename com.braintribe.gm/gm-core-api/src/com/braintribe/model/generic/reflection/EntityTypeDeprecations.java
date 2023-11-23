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
package com.braintribe.model.generic.reflection;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;

/**
 * This will be renamed when BTT-6211 is done. For now we use this name to avoid conflicts with existing classes.
 * 
 * @author peter.gazdik
 */
public interface EntityTypeDeprecations<T extends GenericEntity> {

	/**
	 * Returns all the sub-types which are not abstract, possibly including the {@link EntityTypeDeprecations} on which
	 * this is called.
	 * 
	 * @deprecated we want to drop this all together; why do you need this? Let us know (PGA, DSC)
	 */
	@Deprecated
	Set<EntityType<?>> getInstantiableSubTypes();

	@Deprecated
	String toString(T instance);

	/**
	 * @return direct sub types of this {@link EntityType}
	 * 
	 * @deprecated DO NOT USE, chances are whatever you are doing should be done within the scope of some model, but
	 *             this method might also return entities from other (derived) models. Retrieve this information from
	 *             your GmMetaModel instance if possible. If you really need this, let us know (DSC or PGA) and we can
	 *             "un-deprecate" it.
	 */
	@Deprecated
	Set<EntityType<?>> getSubTypes();

}
