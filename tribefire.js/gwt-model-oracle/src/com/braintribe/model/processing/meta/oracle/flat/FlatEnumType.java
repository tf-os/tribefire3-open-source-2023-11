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

import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.info.GmEnumTypeInfo;

/**
 * @author peter.gazdik
 */
public class FlatEnumType extends FlatCustomType<GmEnumType, GmEnumTypeInfo> {

	private volatile Map<String, FlatEnumConstant> flatEnumConstant;

	public FlatEnumType(GmEnumType type, FlatModel flatModel) {
		super(type, flatModel);
	}

	@Override
	public boolean isEntity() {
		return false;
	}

	public Map<String, FlatEnumConstant> acquireFlatEnumConstants() {
		if (flatEnumConstant == null)
			ensureFlatEnumConstants();

		return flatEnumConstant;
	}

	private synchronized void ensureFlatEnumConstants() {
		if (flatEnumConstant == null)
			flatEnumConstant = FlatConstantsFactory.buildFor(this);
	}

}
