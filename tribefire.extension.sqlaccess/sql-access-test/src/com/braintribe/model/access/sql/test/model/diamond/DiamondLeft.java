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
package com.braintribe.model.access.sql.test.model.diamond;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface DiamondLeft extends DiamondBase {

	EntityType<DiamondLeft> T = EntityTypes.T(DiamondLeft.class);

	/**
	 * This property is declared in the {@link DiamondRight} too. This means that from {@link DiamondTail} point of
	 * view, this property comes from two different super-types, without a common base. The question is now, how do we
	 * represent this.
	 * 
	 * If we simply have a ManyToMany table for declaration of such property, we would have one table for DiamondLeft
	 * one for DiamondRight. This would mean that instances of DiamondTail would need to be in both tables.
	 */
	Set<DiamondFriend> getDiamondDuplicateSet();
	void setDiamondDuplicateSet(Set<DiamondFriend> diamondDuplicateSet);

}
