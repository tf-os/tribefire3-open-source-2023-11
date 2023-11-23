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
package com.braintribe.model.processing.rpc.commons.impl.filter;

import java.util.function.Predicate;

public class TypeAssignabilityFilter implements Predicate<Class<?>> {
	
	private Class<?> type;

	public void setType(Class<?> type) {
		this.type = type;
	}

	@Override
	public boolean test(Class<?> incomingType) {

		if (incomingType == null || type == null) {
			return false;
		}

		return type.isAssignableFrom(incomingType);

	}

}
