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
package com.braintribe.model.processing.cortex.service.connection;

import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_BOOLEAN;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_DATE;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_DECIMAL;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_DOUBLE;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_FLOAT;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_INTEGER;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_LONG;
import static com.braintribe.model.generic.reflection.SimpleTypes.TYPE_STRING;

//============================================================================
//Braintribe IT-Technologies GmbH - www.braintribe.com
//Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
//It is strictly forbidden to copy, modify, distribute or use this code without written permission
//To this file the Braintribe License Agreement applies.
//============================================================================

import static com.braintribe.utils.lcd.SetTools.asSet;

import java.sql.Types;
import java.util.Set;

import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.utils.StringTools;

public class DbTableProcessingUtils {

	
	private static Set<String> reservedProperties = asSet(GenericEntity.id, GenericEntity.partition, GenericEntity.globalId);
	
	static SimpleType getGmTypeFromSqlType(int type, String typeName) {
		switch (type) {
			case Types.BIT:
			case Types.BOOLEAN:
				return TYPE_BOOLEAN;

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
				return TYPE_INTEGER;

			case Types.BIGINT:
				return TYPE_LONG;

			case Types.FLOAT:
				return TYPE_FLOAT;

			case Types.REAL:
			case Types.DOUBLE:
			case Types.NUMERIC:
				return TYPE_DOUBLE;

			case Types.DECIMAL:
				return TYPE_DECIMAL;

			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return TYPE_DATE;

			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
				return TYPE_STRING;

			case Types.OTHER:
				return getGmTypeFromTypeNameIfPossible(typeName);

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.NULL:
			case Types.JAVA_OBJECT:
			case Types.DISTINCT:
			case Types.STRUCT:
			case Types.ARRAY:
			case Types.BLOB:
			case Types.CLOB:
			case Types.REF:
			case Types.DATALINK:
			case Types.ROWID:
			case Types.NCLOB:
			case Types.SQLXML:
				return null;
		}

		return null;
	}

	static SimpleType getGmTypeFromTypeNameIfPossible(String typeName) {
		typeName = normalizeTypeName(typeName);

		switch (typeName) {
			case "timestamp":
				return TYPE_DATE;

			case "varchar2":
			case "nvarchar2":
				return TYPE_STRING;
		}

		return null;
	}

	private static String normalizeTypeName(String typeName) {
		if (typeName == null) {
			return "";
		}

		int pos = typeName.indexOf('(');
		if (pos > 0) {
			// for cases like TIMESTAMP(4)
			typeName = typeName.substring(0, pos);
		}

		return typeName.toLowerCase();
	}

	public static String getEntitySignatureFrom(GmMetaModel metaModel, DbTable dbTable) {
		String groupId = extractGroupId(metaModel);
		String packageName = groupId + ".";

		String schema = null;
		if (dbTable.getSchema() != null)
			schema = dbTable.getSchema().replace(" ", "");

		if (schema != null) {
			packageName += "$" + schema + ".";
		}

		return packageName + camelCase(dbTable.getName(), true);
	}

	public static String getPropertyName(DbColumn dbColumn) {
		String columnName = dbColumn.getName();
		
		if (dbColumn.getReferencedTable() != null && columnName.toLowerCase().endsWith("_id")) {
			columnName = columnName.substring(0, columnName.length() - 3);
		}

		columnName = ensueUsingJavaLettersOnly(columnName);
		columnName = camelCase(columnName, false);
		columnName = escapeIfStartsWithNumber(columnName);
		

		if (reservedProperties.contains(columnName)) {
			//$custom_id
			//$custom_partition
			//$custom_globalId
			columnName = "$custom_"+columnName;
		}
		
		return columnName;
	}

	private static String ensueUsingJavaLettersOnly(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			char newC = Character.isJavaIdentifierPart(c) ? c : ' ';
			sb.append(newC);
		}

		return sb.toString();
	}

	private static String escapeIfStartsWithNumber(String s) {
		char firstChar = s.charAt(0);
		return (firstChar >= '0' && firstChar <= '9') || (firstChar == '_') ? ("_" + s) : s;
	}

	private static String camelCase(String s, boolean capitalizeFirst) {
		String[] strings = s.split("_|\\s");

		for (int i = 0; i < strings.length; i++) {
			if (capitalizeFirst || (i > 0)) {
				strings[i] = StringTools.capitalize(strings[i]);

			} else if (!capitalizeFirst && i == 0) {
				strings[i] = uncapitalizeInitialCapitalSequence(strings[i]);
			}
		}

		return StringTools.join("", strings);
	}

	private static String uncapitalizeInitialCapitalSequence(String s) {
		int len = s.length();

		if (len < 2) {
			return s.toLowerCase();
		}

		int i = 1;
		while (i < len && Character.isUpperCase(s.charAt(i))) {
			i++;
		}

		return s.substring(0, i).toLowerCase() + s.substring(i);
	}

	private static final String DEFAULT_GROUP_ID = "com.braintribe";

	private static String extractGroupId(GmMetaModel metaModel) {
		String name = metaModel.getName();
		if (name == null) {
			return DEFAULT_GROUP_ID;
		}
		
		int index = name.lastIndexOf(':');
		
		String groupId = index != -1? name.substring(0, index): name;

		return groupId;
	}

}
