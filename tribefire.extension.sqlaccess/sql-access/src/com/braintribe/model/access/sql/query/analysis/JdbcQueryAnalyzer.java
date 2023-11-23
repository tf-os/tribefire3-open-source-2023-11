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
package com.braintribe.model.access.sql.query.analysis;

import com.braintribe.model.access.sql.query.oracle.JdbcQuery;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public class JdbcQueryAnalyzer {

	@SuppressWarnings("unused")
	private final SelectQuery selectQuery;

	public JdbcQueryAnalyzer(SelectQuery selectQuery) {
		this.selectQuery = selectQuery;
	}

	public static JdbcQuery analyze(SelectQuery selectQuery) {
		return new JdbcQueryAnalyzer(selectQuery).analyze();
	}

	private JdbcQuery analyze() {
		throw new UnsupportedOperationException("Method 'JdbcQueryAnalyzer.analyze' is not implemented yet!");
	}

}
