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
package com.braintribe.model.accessdeployment.hibernate;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Enum representing dialects from org.hibernate.dialect package. For each constant there exists a class with name
 * "org.hibernate.dialect.${constant}".
 * <p>
 * This list was created based on classes listed on
 * <a href="http://docs.jboss.org/hibernate/orm/5.2/javadocs/org/hibernate/dialect/package-tree.html">this hibernate documentation</a>.
 */
public enum HibernateDialect implements EnumBase {

	Cache71Dialect,
	CUBRIDDialect,
	DataDirectOracle9Dialect,
	DB2390Dialect,
	DB2400Dialect,
	DB297Dialect,
	DB2Dialect,
	DerbyDialect,
	DerbyTenFiveDialect,
	DerbyTenSevenDialect,
	DerbyTenSixDialect,
	FirebirdDialect,
	FrontBaseDialect,
	H2Dialect,
	HANAColumnStoreDialect,
	HANARowStoreDialect,
	HSQLDialect,
	Informix10Dialect,
	InformixDialect,
	Ingres10Dialect,
	Ingres9Dialect,
	IngresDialect,
	InterbaseDialect,
	JDataStoreDialect,
	MariaDB53Dialect,
	MariaDBDialect,
	MckoiDialect,
	MimerSQLDialect,
	MySQL55Dialect,
	MySQL57Dialect,
	MySQL57InnoDBDialect,
	MySQL5Dialect,
	MySQL5InnoDBDialect,
	MySQLDialect,
	MySQLInnoDBDialect,
	MySQLMyISAMDialect,
	Oracle10gDialect,
	Oracle12cDialect,
	Oracle8iDialect,
	Oracle9Dialect,
	Oracle9iDialect,
	OracleDialect,
	PointbaseDialect,
	PostgresPlusDialect,
	PostgreSQL81Dialect,
	PostgreSQL82Dialect,
	PostgreSQL91Dialect,
	PostgreSQL92Dialect,
	PostgreSQL93Dialect,
	PostgreSQL94Dialect,
	PostgreSQL95Dialect,
	PostgreSQL9Dialect,
	PostgreSQLDialect,
	ProgressDialect,
	RDMSOS2200Dialect,
	SAPDBDialect,
	SQLServer2005Dialect,
	SQLServer2008Dialect,
	SQLServer2012Dialect,
	SQLServerDialect,
	Sybase11Dialect,
	SybaseAnywhereDialect,
	SybaseASE157Dialect,
	SybaseASE15Dialect,
	SybaseDialect,
	Teradata14Dialect,
	TeradataDialect,
	TimesTenDialect;

	public static final EnumType T = EnumTypes.T(HibernateDialect.class);

	@Override
	public EnumType type() {
		return T;
	}

}
