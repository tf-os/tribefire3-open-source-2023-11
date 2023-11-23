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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author peter.gazdik
 */
/* package */ class JdbcDialectMappings {

	private static List<JdbcDialectMapping> mappings = newList();

	// This code is generated in hibernate-access-test, class HibernateDialectKnowledgeThief
	// It should be updated when we update Hibernate
	static {
		// @formatter:off
		register("(?i).*db2/nt.*", DbVariant.DB2, "org.hibernate.dialect.DB2Dialect", "smallint", "integer", "bigint", "float", "double", "decimal($p,$s)", "timestamp", "varchar($l)", "varchar(255)", "clob", "blob($l)");
		register("(?i).*db2.*", DbVariant.DB2v7_Host, "org.hibernate.dialect.DB2Dialect", "smallint", "integer", "bigint", "float", "double", "decimal($p,$s)", "timestamp", "varchar($l)", "varchar(255)", "clob", "blob($l)");
		register("(?i).*microsoft sql server.*Version\\:9\\..*", DbVariant.mssql, "org.hibernate.dialect.SQLServer2005Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime", "nvarchar($l)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:10\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2008Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:11\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2012Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:12\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2012Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:13\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2012Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:14\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2012Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*Version\\:15\\..*", DbVariant.mssql, "org.hibernate.dialect.sane.SaneSQLServer2012Dialect", "bit", "int", "bigint", "float", "double precision", "numeric($p,$s)", "datetime2", "nvarchar(MAX)", "nvarchar(255)", "varchar(MAX)", "varbinary(MAX)");
		register("(?i).*microsoft sql server.*", DbVariant.mssql, "org.hibernate.dialect.SQLServerDialect", "bit", "int", "numeric(19,0)", "float", "double precision", "numeric($p,$s)", "datetime", "nvarchar($l)", "nvarchar(255)", "text", "image");
		register("(?i).*oracle.*12.*", DbVariant.oracle, "org.hibernate.dialect.Oracle12cDialect", "number(1,0)", "number(10,0)", "number(19,0)", "float", "double precision", "number($p,$s)", "timestamp", "nvarchar2($l)", "nvarchar2(255)", "clob", "blob");
		register("(?i).*oracle.*10.*", DbVariant.oracle, "org.hibernate.dialect.Oracle10gDialect", "number(1,0)", "number(10,0)", "number(19,0)", "float", "double precision", "number($p,$s)", "timestamp", "nvarchar2($l)", "nvarchar2(255)", "clob", "blob");
		register("(?i).*oracle.*9.*", DbVariant.oracle, "org.hibernate.dialect.Oracle9iDialect", "number(1,0)", "number(10,0)", "number(19,0)", "float", "double precision", "number($p,$s)", "timestamp", "nvarchar2($l)", "nvarchar2(255)", "clob", "blob");
		register("(?i).*oracle.*", DbVariant.oracle, "org.hibernate.dialect.Oracle12cDialect", "number(1,0)", "number(10,0)", "number(19,0)", "float", "double precision", "number($p,$s)", "timestamp", "nvarchar2($l)", "nvarchar2(255)", "clob", "blob");
		register("(?i).*mysql.*version:\\s*([89]|\\d\\d)\\..*", DbVariant.mysql, "org.hibernate.dialect.MySQL8Dialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime(6)", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*mysql.*version:\\s*[67]\\..*", DbVariant.mysql, "org.hibernate.dialect.MySQL57Dialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime(6)", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*mysql.*version:\\s*5\\.[789]\\..*", DbVariant.mysql, "org.hibernate.dialect.MySQL57Dialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime(6)", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*mysql.*version:\\s*5\\.[56]\\..*", DbVariant.mysql, "org.hibernate.dialect.MySQL55Dialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*mysql.*version:\\s*5\\..*", DbVariant.mysql, "org.hibernate.dialect.MySQL5Dialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*mysql.*", DbVariant.mysql, "org.hibernate.dialect.MySQLDialect", "bit", "integer", "bigint", "float", "double precision", "decimal($p,$s)", "datetime", "nvarchar($l)", "nvarchar(255)", "longtext", "longblob");
		register("(?i).*derby.*version:\\s*(10\\.[7-9]|10\\.\\d\\d).*", DbVariant.derby, "org.hibernate.dialect.sane.SaneDerbyTenSevenDialect", "boolean", "integer", "bigint", "float", "double", "decimal($p,$s)", "timestamp", "varchar($l)", "varchar(255)", "clob", "blob");
		register("(?i).*derby.*", DbVariant.derby, "org.hibernate.dialect.DerbyDialect", "smallint", "integer", "bigint", "float", "double", "decimal($p,$s)", "timestamp", "varchar($l)", "varchar(255)", "clob", "blob");
		register("(?i)hsql.*", DbVariant.hsql, "org.hibernate.dialect.HSQLDialect", "boolean", "integer", "bigint", "float", "double", "numeric", "timestamp", "nvarchar($l)", "nvarchar(255)", "longvarchar", "longvarbinary");
		register("(?i)postgre.*version:\\s*\\d\\d.*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQL10Dialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		register("(?i)postgre.*version:\\s*(9\\.[5-9]).*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQL95Dialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		register("(?i)postgre.*version:\\s*9\\..*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQL9Dialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		register("(?i)postgre.*version:\\s*8\\.1.*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQL81Dialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		register("(?i)postgre.*version:\\s*8\\.[234].*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQL82Dialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		register("(?i)postgre.*", DbVariant.postgre, "org.hibernate.dialect.PostgreSQLDialect", "boolean", "int4", "int8", "float4", "float8", "numeric($p, $s)", "timestamp", "varchar($l)", "varchar(255)", "text", "oid");
		// @formatter:on
	}

	public static List<JdbcDialectMapping> dialectMappings() {
		return mappings;
	}

	/* package */ static JdbcDialect DEFAULT_DIALECT = new BasicJdbcDialect( //
			DbVariant.other, "org.hibernate.dialect.Dialect", //
			"boolean", //
			"integer", "bigint", "float", "double", "numeric($p,$s)", //
			"timestamp", "nvarchar(4000)", "nvarchar(255)", "clob", "blob");

	/* package */ static class JdbcDialectMapping {
		Pattern pattern;
		JdbcDialect dialect;

		public JdbcDialectMapping(String regex, JdbcDialect dialect) {
			this.pattern = Pattern.compile(regex);
			this.dialect = dialect;
		}

	}

	private static class BasicJdbcDialect implements JdbcDialect {

		private final DbVariant dbVariant;
		private final String hibernateDialect;
		private final String clobType;
		private final String blobType;
		private final String timestampType;
		private final String nvarcharType;
		private final String nvarchar255Type;
		private final String booleanType;
		private final String intType;
		private final String longType;
		private final String floatType;
		private final String doubleType;
		private final String decimalType;

		BasicJdbcDialect(DbVariant dbVariant, String hibernateDialect, //
				String _boolean, //
				String _int, String _long, String _float, String _double, String decimal, //
				String timestamp, String nvarchar, String nvarchar255, String clob, //
				String blob) {

			this.dbVariant = dbVariant;
			this.hibernateDialect = hibernateDialect;
			this.booleanType = _boolean;
			this.intType = _int;
			this.longType = _long;
			this.floatType = _float;
			this.doubleType = _double;
			this.decimalType = decimal;
			this.timestampType = timestamp;
			this.nvarcharType = nvarchar;
			this.nvarchar255Type = nvarchar255;
			this.clobType = clob;
			this.blobType = blob;
		}

		// @formatter:off
		@Override public String dbVariant() { return dbVariant.name(); }
		@Override public DbVariant knownDbVariant() { return dbVariant; }
		@Override public String hibernateDialect() { return hibernateDialect; }

		@Override public String booleanType() {	return booleanType; }
		@Override public String intType() {	return intType; }
		@Override public String longType() {	return longType; }
		@Override public String floatType() {	return floatType; }
		@Override public String doubleType() {	return doubleType; }
		@Override public String bigDecimalType(int precision, int scale) { return decimalType.replace("$p", "" + precision).replace("$s", "" + scale); }
		@Override public String timestampType() { return timestampType; }
		@Override public String nvarchar(int size) { return nvarcharType.replace("$l", "" + size); }
		@Override public String nvarchar255() { return nvarchar255Type; }
		@Override public String clobType() { return clobType; }
		@Override public String blobType() { return blobType; }
		// @formatter:on

	}

	private static void register(String regex, DbVariant dbVariant, String hibernateDialect, //
			String _boolean, //
			String _int, String _long, String _float, String _double, String _decimal, //
			String timestamp, //
			String nvarchar, String nvarchar255, String clob, //
			String blob) {

		BasicJdbcDialect dialect = new BasicJdbcDialect(dbVariant, hibernateDialect, //
				_boolean, _int, _long, _float, _double, _decimal, timestamp, nvarchar, nvarchar255, clob, blob);

		mappings.add(new JdbcDialectMapping(regex, dialect));
	}

}
