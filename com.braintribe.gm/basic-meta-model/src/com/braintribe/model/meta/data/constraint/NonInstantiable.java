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
package com.braintribe.model.meta.data.constraint;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.PredicateErasure;

/**
 * This metadata can be used to disable entity instantiation completely.
 * 
 * 
 * If set to <code>true</code>, entity instantiation will not be allowed for the entity type. This setting is stronger than other
 * instantiation related metadata (e.g.;@link FreeEntityInstantiation}, ;@link PropertyCreateEntities}).<br/>
 * Please note though that this property is only used to <b>disable</b> entity instantiation, i.e. setting it to <code>false</code> has no
 * effect which means that in that case the other instantiation related metadata are checked.
 * 
 * @author michael.lafite
 */

public interface NonInstantiable extends Instantiable, PredicateErasure {

	EntityType<NonInstantiable> T = EntityTypes.T(NonInstantiable.class);

}
