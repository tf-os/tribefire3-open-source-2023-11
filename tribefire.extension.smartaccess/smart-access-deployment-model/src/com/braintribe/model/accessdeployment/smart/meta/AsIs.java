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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Maps all kinds of model elements 1:1 to the delegate. For entities this means we map a smart model type to itself,
 * for properties it means the name and type are the same in smart and delegate models and no value conversion is done.
 * For enums it's analogical.
 * 
 * @author peter.gazdik
 */
public interface AsIs extends EntityAssignment, DirectPropertyAssignment, EnumConstantAssignment {

	EntityType<AsIs> T = EntityTypes.T(AsIs.class);

}
