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
package com.braintribe.model.access.rdbms.deploy;

import java.util.StringJoiner;

import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.access.sql.dialect.SqlDialect;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * @author peter.gazdik
 */
public interface RdbmsBase {

	static String getDataTypeName(GenericModelType type, SqlDialect sqlDialect) {
		switch (type.getTypeCode()) {
			case booleanType:
				return sqlDialect.booleanType();
			case dateType:
				return sqlDialect.dateType();
			case decimalType:
				return sqlDialect.decimalType();
			case doubleType:
				return sqlDialect.doubleType();
			case floatType:
				return sqlDialect.floatType();
			case integerType:
				return sqlDialect.integerType();
			case longType:
				return sqlDialect.longType();
			case stringType:
				return sqlDialect.stringType();
			case enumType:
				return sqlDialect.enumType();
			case entityType:
				return sqlDialect.entityType();
			case listType:
			case setType:
			case mapType:
				throw new IllegalArgumentException("Collection itself has no type. Given collection type:" + type.getTypeSignature());
			default:
				throw new UnsupportedEnumException(type.getTypeCode());
		}
	}

	static String computeHash(DbTable table) {
		// TODO use better hash?
		StringJoiner sj = new StringJoiner("#");

		for (DbColumn dbColumn : table.getColumns()) {
			sj.add(dbColumn.getName());
			sj.add(dbColumn.getDataTypeName());
		}

		return sj.toString();
	}

	// TODO maybe use db schema (learn what exactly it actually is, if it is needed to create one and so on...)
	static String qualifiedTableName(@SuppressWarnings("unused") /* TODO think about this!!! */ String dbOrNamespace, String tableName) {
		return tableName;
	}

}
