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
package com.braintribe.model.processing.meta.oracle.flat;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;

/**
 * @author peter.gazdik
 */
public class FlatEntityType extends FlatCustomType<GmEntityType, GmEntityTypeInfo> {

	private volatile Map<String, FlatProperty> flatProperties;
	private ReentrantLock flatPropertiesLock = new ReentrantLock();

	public FlatEntityType(GmEntityType type, FlatModel flatModel) {
		super(type, flatModel);
	}

	@Override
	public boolean isEntity() {
		return true;
	}

	public Map<String, FlatProperty> acquireFlatProperties() {
		if (flatProperties == null) {
			flatPropertiesLock.lock();
			try {
				if (flatProperties == null) {
					flatProperties = FlatPropertiesFactory.buildFor(this);
				}
			} finally {
				flatPropertiesLock.unlock();
			}
		}

		return flatProperties;
	}

}
