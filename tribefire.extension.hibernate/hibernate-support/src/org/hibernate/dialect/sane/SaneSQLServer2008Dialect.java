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

import org.hibernate.dialect.SQLServer2008Dialect;

import com.braintribe.persistence.hibernate.dialects.HibernateDialectMappings;

/**
 * @see #fixVarcharProblem()
 * 
 * @see HibernateDialectMappings#loadDialect(String)
 */
public class SaneSQLServer2008Dialect extends SQLServer2008Dialect {

	private static final int NVARCHAR_MAX_LENGTH = 4000;

	public SaneSQLServer2008Dialect() {
		fixVarcharProblem();
	}

	/**
	 * Fixes a problem that is especially severe for String IDs. The default Hibernate mapping would pick a 'varchar(255)' as it's column type.
	 * However, the actual value provided (via JDBC) would be a 'nvarchar'.
	 * <p>
	 * This means, that for example on "update where id=..." the index wouldn't be used and instead a sequential processing of an entire index
	 * happens. This scan requires allocating a lock on entire table or at least on some page, and can potentially lead to a deadlock, if a similar
	 * update is happening in parallel.
	 * <p>
	 * This was actually happening when we simply tried to create a Resource and assign it a new ResourceSource in parallel (yes, two transactions
	 * which only tried to write new data ended up in a deadlock).
	 */
	private void fixVarcharProblem() {
		registerColumnType(Types.VARCHAR, NVARCHAR_MAX_LENGTH, "nvarchar($l)");
		registerColumnType(Types.VARCHAR, "nvarchar(MAX)");
	}

}
