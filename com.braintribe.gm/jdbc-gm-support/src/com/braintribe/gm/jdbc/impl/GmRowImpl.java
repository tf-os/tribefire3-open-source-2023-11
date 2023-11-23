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
package com.braintribe.gm.jdbc.impl;

import java.util.IdentityHashMap;

import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmRow;

/**
 * @author peter.gazdik
 */
public class GmRowImpl implements GmRow {

	// forcing efficient implementation
	private final IdentityHashMap<GmColumn<?>, Object> values;

	public GmRowImpl(IdentityHashMap<GmColumn<?>, Object> values) {
		this.values = values;
	}

	@Override
	public <T> T getValue(GmColumn<T> column) {
		return (T) values.get(column);
	}

}
