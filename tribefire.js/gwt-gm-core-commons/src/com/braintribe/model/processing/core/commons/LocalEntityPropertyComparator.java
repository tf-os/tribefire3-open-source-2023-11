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
package com.braintribe.model.processing.core.commons;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;

/**
 * 
 */
public class LocalEntityPropertyComparator implements HashingComparator<LocalEntityProperty> {

	public final static LocalEntityPropertyComparator INSTANCE = new LocalEntityPropertyComparator();

	@Override
	public boolean compare(LocalEntityProperty o1, LocalEntityProperty o2) {
		return o1 == o2 || //
				o1.getEntity() == o2.getEntity() && o1.getPropertyName().equals(o2.getPropertyName());
	}

	@Override
	public int computeHash(LocalEntityProperty lep) {
		return 31 * lep.getEntity().hashCode() + lep.getPropertyName().hashCode();
	}

}
