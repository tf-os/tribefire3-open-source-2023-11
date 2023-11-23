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
package com.braintribe.model.generic.manipulation;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Specifies how to handle references from other entities to the entity being deleted.
 * 
 * failIfReferenced: fails if the delete entities is referenced anywhere ignoreReferences: delete the entities and ignore the references. This may
 * still result in errors depending on the access' implementation, for example, if an SQL database backs an access and there are foreign keys between
 * entities.
 */
public enum DeleteMode implements EnumBase {

	/**
	 * Drops the references, even if not allowed by constraints such as {@link Mandatory}. (e.g. EntityA.entityb = EntityB and entityb is a mandatory
	 * property, deleting EntityB will set EntityA.entityb to null).
	 * <p>
	 * This might have a bad performance because it might lead to a high number of queries, but is a good choice for "dense" assemblies with many
	 * references between them, as it makes sure to find and drop all the references.
	 */
	dropReferences,

	/**
	 * Drops the references as long as no constraint is violated (e.g. {@link Mandatory}). Throws an exception if a reference exists which cannot be
	 * dropped.
	 */
	dropReferencesIfPossible,

	/** If any reference is detected, exception is thrown. */
	failIfReferenced,

	/**
	 * Do not even detect existing references but proceed with the deletion as if there were none. This might lead to an exception being thrown from a
	 * deeper layer, e.g. deleting an entity in an SQL database might violate a foreign key constraint.
	 * <p>
	 * This is recommended if the user can be reasonably sure there are no references, as the performance could be much worse with the other modes,
	 * due to detecting references using queries.
	 */
	ignoreReferences;

	public static final EnumType T = EnumTypes.T(DeleteMode.class);
	
	@Override
	public EnumType type() {
		return T;
	}
}
