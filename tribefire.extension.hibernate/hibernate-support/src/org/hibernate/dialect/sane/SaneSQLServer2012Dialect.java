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
package org.hibernate.dialect.sane;

import java.sql.Types;

import org.hibernate.dialect.SQLServer2012Dialect;

import com.braintribe.persistence.hibernate.dialects.HibernateDialectMappings;

/**
 * @see SaneSQLServer2008Dialect
 * @see HibernateDialectMappings#loadDialect(String)
 */
public class SaneSQLServer2012Dialect extends SQLServer2012Dialect {

	private static final int NVARCHAR_MAX_LENGTH = 4000;

	public SaneSQLServer2012Dialect() {
		fixVarcharProblem();
	}

	private void fixVarcharProblem() {
		registerColumnType(Types.VARCHAR, NVARCHAR_MAX_LENGTH, "nvarchar($l)");
		registerColumnType(Types.VARCHAR, "nvarchar(MAX)");
	}

}
