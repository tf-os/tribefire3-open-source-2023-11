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

import org.hibernate.dialect.DerbyTenSevenDialect;

import com.braintribe.persistence.hibernate.dialects.HibernateDialectMappings;

/**
 * This is an extension of the {@link org.hibernate.dialect.DerbyTenSevenDialect} Hibernate dialect that fixes a problem with CLOBs. Use this dialect
 * to prevent CLOBs to be limited to 255 characters.
 * 
 * @see HibernateDialectMappings#loadDialect(String)
 */
public class SaneDerbyTenSevenDialect extends DerbyTenSevenDialect {

	public SaneDerbyTenSevenDialect() {
		registerColumnType(Types.CLOB, "clob");
	}

}
