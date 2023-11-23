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
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.UniversalMetaData;

/**
 * Controls whether a user can change the value of given property. There are three different cases we might want to achieve:
 * 
 * <ul>
 * <li><b>Fully modifiable</b> - user can do changes at any time - <tt>Modifiable</tt> must resolve to <tt>true</tt>.
 * <li><b>Fully non-modifiable</b> - user cannot do changes at any time - <tt>Modifiable</tt> must resolve to <tt>false</tt>. Used for properties
 * meant to be set automatically by the system, e.g. some value computed based on other properties.
 * <li><b>Modifiable on creation only</b> - user can set the initial value but not edit existing instances - Both <tt>Modifiable</tt> and
 * {@link Mandatory} must resolve to <tt>true</tt>.
 * </ul>
 * 
 * Erasure is {@link Unmodifiable}.
 */
public interface Modifiable extends UniversalMetaData, Predicate {

	EntityType<Modifiable> T = EntityTypes.T(Modifiable.class);

}
