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
package com.braintribe.model.processing.smood.id;

import java.math.BigDecimal;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.smood.IdGenerator;

public class BigDecimalIdGenerator implements IdGenerator<BigDecimal> {

	private BigDecimal maxId = BigDecimal.ZERO;

	@Override
	public BigDecimal generateId(GenericEntity entity) {
		return generateId();
	}

	protected synchronized BigDecimal generateId() {
		return maxId = maxId.add(BigDecimal.ONE);
	}

	@Override
	public synchronized void recognizeUsedId(BigDecimal id) {
		if (firstIsBiggerThanSecond(id, maxId)) {
			maxId = id;
		}
	}

	private static boolean firstIsBiggerThanSecond(BigDecimal n1, BigDecimal n2) {
		return n1.compareTo(n2) > 0;
	}
}
