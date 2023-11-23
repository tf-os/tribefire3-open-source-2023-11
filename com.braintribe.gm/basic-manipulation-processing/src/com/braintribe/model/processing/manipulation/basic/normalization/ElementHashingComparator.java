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
package com.braintribe.model.processing.manipulation.basic.normalization;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.value.EntityReference;

/**
 * 
 */
class ElementHashingComparator implements HashingComparator<Object> {

	public static final ElementHashingComparator INSTANCE = new ElementHashingComparator();

	private static final EntRefHashingComparator referenceComparator = EntRefHashingComparator.INSTANCE;

	private ElementHashingComparator() {
	}

	@Override
	public int computeHash(Object e) {
		if (e == null)
			return 0;

		if (e instanceof EntityReference) {
			return referenceComparator.computeHash((EntityReference) e);

		} else {
			return e.hashCode();
		}
	}

	@Override
	public boolean compare(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}

		if (o2 == null) {
			return false;
		}

		if (o1 instanceof EntityReference) {
			if (o2 instanceof EntityReference) {
				return referenceComparator.compare((EntityReference) o1, (EntityReference) o2);
			} else {
				return false;
			}
		}

		return o1.equals(o2);
	}

}
