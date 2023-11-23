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
package com.braintribe.model.access.sql.tools;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

import tribefire.extension.sqlaccess.model.meta.SqlColumnName;
import tribefire.extension.sqlaccess.model.meta.SqlTableName;

/**
 * @author peter.gazdik
 */
public class SqlMappingExpert {

	private final CmdResolver cmdResolver;

	public SqlMappingExpert(CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
	}

	public String resolveDbTableName(EntityType<?> entityType) {
		SqlTableName sqlTableName = cmdResolver.getMetaData().entityType(entityType).meta(SqlTableName.T).exclusive();
		return sqlTableName != null ? sqlTableName.getName() : entityType.getShortName();
	}

	public String resolveDbColumnName(EntityType<?> entityType, Property property) {
		SqlColumnName sqlColumnName = cmdResolver.getMetaData().entityType(entityType).property(property).meta(SqlColumnName.T).exclusive();
		return sqlColumnName != null ? sqlColumnName.getName() : property.getName();
	}

}
