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
package com.braintribe.gm.jdbc.impl.column;

import com.braintribe.gm.jdbc.impl.column.SimpleSingularGmColumn.StringColumn;

/**
 * @author peter.gazdik
 */
public class LimitedLengthStringColumn extends StringColumn {

	private final int size;

	public LimitedLengthStringColumn(String name, String sqlType, int size) {
		super(name, sqlType);
		this.size = size;
	}

	@Override
	protected String sqlType() {
		return super.sqlType() + "(" + size + ")";
	}

}
