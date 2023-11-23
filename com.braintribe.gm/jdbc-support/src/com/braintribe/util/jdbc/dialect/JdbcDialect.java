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
package com.braintribe.util.jdbc.dialect;

import java.sql.Types;

import javax.sql.DataSource;

/**
 * @author peter.gazdik
 */
public interface JdbcDialect {

	static JdbcDialect defaultDialect() {
		return JdbcDialectMappings.DEFAULT_DIALECT;
	}

	static JdbcDialect detectDialect(DataSource dataSource) {
		return JdbcDialectAutoSense.findJdbcDialect(dataSource);
	}

	/** Name of the DB, without concrete version details. E.g. "oracle", "postgre", etc */
	String dbVariant();
	/** Same information as {@link #dbVariant()}, but type-safe, for some well known variants. */
	DbVariant knownDbVariant();
	/** Full class name of the Hibernate Dialect which this {@link JdbcDialect} is based on. */
	String hibernateDialect();

	/** Name of {@link Types#BOOLEAN} */
	String booleanType();
	/** Name of {@link Types#INTEGER} */
	String intType();
	/** Name of {@link Types#BIGINT} */
	String longType();
	/** Name of {@link Types#FLOAT} */
	String floatType();
	/** Name of {@link Types#DOUBLE} */
	String doubleType();
	/** Name of {@link Types#TIMESTAMP} */
	String timestampType();
	/** Name of {@link Types#NUMERIC} with precision 19 and scale 2 */
	default String bigDecimalType() {
		return bigDecimalType(19, 2);
	}
	/** Name of {@link Types#NUMERIC} */
	String bigDecimalType(int precision, int scale);
	/** Name of {@link Types#CLOB} */
	String clobType();
	/** Name of {@link Types#BLOB} */
	String blobType();

	/**
	 * Name of {@link Types#NVARCHAR},
	 * <p>
	 * This is tricky, as it can be pretty unlimited in some cases, and only up to 4000 bytes in Oracle.
	 * <p>
	 * In MS SQL the type is nvarchar(MAX), which cannot be used for a primary key. Use {@link #nvarchar255()} instead.
	 */
	String nvarchar(int length);

	/** Name of {@link Types#NVARCHAR} with 255 chars max. This is safe to use as a primary key. */
	String nvarchar255();

}
