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
package com.braintribe.model.dbs;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${name}")
public interface DbColumn extends IdentifiableDbEntity {

	EntityType<DbColumn> T = EntityTypes.T(DbColumn.class);

	// @formatter:off
	DbTable getOwner();
	void setOwner(DbTable owner);

	@Mandatory
	String getName();
	void setName(String name);

	/** {@link java.sql.Types} code */
	int getDataType();
	void setDataType(int dataType);

	/** Not a value retrieved via jdbc, but just the name of the constant from {@link java.sql.Types}. */
	String getDataTypeName();
	void setDataTypeName(String dataTypeName);

	/** This is the actual TYPE_NAME retrieved via jdbc. */
	String getTypeName();
	void setTypeName(String typeName);

	Boolean getNullable();
	void setNullable(Boolean nullable);

	int getOrdinalPosition();
	void setOrdinalPosition(int ordinalPosition);

	int getColumnSize();
	void setColumnSize(int columnSize);

	String getRemarks();
	void setRemarks(String remarks);

	boolean getIsPrimaryKey();
	void setIsPrimaryKey(boolean isPrimaryKey);

	DbTable getReferencedTable();
	void setReferencedTable(DbTable primaryKeyTable);
	// @formatter:on

}
