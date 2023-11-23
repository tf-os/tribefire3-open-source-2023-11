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

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.smood.IdGenerator;

public class DateIdGenerator implements IdGenerator<Date> {

	private Date maxId = new Date(Long.MIN_VALUE);
	private ReentrantLock lock = new ReentrantLock();

	@Override
	public Date generateId(GenericEntity entity) {
		lock.lock();
		try {
			return generateId();
		} finally {
			lock.unlock();
		}
	}

	protected Date generateId() {
		Date result = new Date(); // current date

		if (!firstIsBiggerThanSecond(result, maxId)) {
			result = new Date(maxId.getTime() + 1);
		}

		return maxId = result;
	}

	@Override
	public void recognizeUsedId(Date id) {
		lock.lock();
		try {
			if (firstIsBiggerThanSecond(id, maxId)) {
				maxId = id;
			}
		} finally {
			lock.unlock();
		}
	}

	private static boolean firstIsBiggerThanSecond(Date d1, Date d2) {
		return d1.compareTo(d2) > 0;
	}

}
